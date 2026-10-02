package uhc.tensuraUHC.listeners;

import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.ItemStack;
import uhc.tensuraUHC.TensuraUHC;
import uhc.tensuraUHC.roles.Role;

import java.util.Random;
import java.util.UUID;

public class DeathListener implements Listener {

    private final TensuraUHC main;

    public DeathListener(TensuraUHC main) {
        this.main = main;
    }

    // Execution en priority HIGH pour laisser les Listeners de Rôles traiter les effets/conversions au préalable (NORMAL/LOW)
    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player victimPlayer = event.getEntity();
        UUID victim = victimPlayer.getUniqueId();

        // Gestion du killer avec vérification null-safe
        if (victimPlayer.getKiller() != null) {
            UUID killer = victimPlayer.getKiller().getUniqueId();
            main.getKills().put(killer, main.getKills().getOrDefault(killer, 0) + 1);
        }

        Location deathLocation = victimPlayer.getLocation();

        // 1. Vérification de l'annonce des rôles
        boolean rolesRevealed = (main.getGameManager() != null && main.getGameManager().GetRolesRevealed())
                || (main.getGameManager() != null && main.getGameManager().getTotalGameSeconds() >= main.GetRoleTime());

        // 2. MODIFICATIONS SYNCHRONES (Immédiates pendant l'event)
        if (rolesRevealed) {
            Role victimRole = main.getRoleManager().getPlayerRole(victim);

            String roleName = (victimRole != null) ? victimRole.getName() : "Inconnu";
            String campColor = (victimRole != null && victimRole.getCamp() != null) ? victimRole.getCamp().getColor().toString() : ChatColor.GRAY.toString();

            event.setDeathMessage("================-================\n    " +
                    victimPlayer.getName() + " est mort. Il était " +
                    campColor + roleName + ChatColor.WHITE +
                    ".\n=================================");

            // Drop de la Gapple
            if (deathLocation.getWorld() != null) {
                deathLocation.getWorld().dropItemNaturally(deathLocation, new ItemStack(Material.GOLDEN_APPLE, 1));
            }

            // On retire le rôle et l'état de joueur actif seulement si les rôles sont révélés
            main.getRoleManager().removeRole(victim);
            main.getGameManager().DeleteActivePlayer(victim);
        }

        // 3. ACTIONS DIFFÉRÉES (Respawn & Téléportation au tick suivant avec UUID)
        Bukkit.getScheduler().runTask(main, () -> {
            Player p = Bukkit.getPlayer(victim);
            if (p == null || !p.isOnline()) return;

            // Force le respawn instantané via l'API Spigot
            p.spigot().respawn();

            if (!rolesRevealed) {
                // --- CAS : Rôles NON annoncés -> Respawn Survie ---
                World gameWorld = main.getGameWorld();
                if (gameWorld == null) {
                    gameWorld = Bukkit.getWorld("uhc_world") != null ? Bukkit.getWorld("uhc_world") : p.getWorld();
                }

                Location randomLoc = getRandomRespawnLocation(gameWorld, 0, main.getBorderManager().getCurrentBorderRadius(gameWorld));

                p.setGameMode(GameMode.SURVIVAL);
                p.setHealth(p.getMaxHealth());
                p.setFoodLevel(20);
                p.setSaturation(5.0f);
                p.teleport(randomLoc);

                p.sendMessage(ChatColor.GOLD + "[TensuraUHC] " + ChatColor.YELLOW + "Les rôles ne sont pas encore annoncés ! Vous avez réapparu à un endroit distant.");
            } else {
                // --- CAS : Rôles DÉJÀ annoncés -> Spectateur ---
                p.setGameMode(GameMode.SPECTATOR);
                p.teleport(deathLocation);
                p.sendMessage(ChatColor.RED + "[TensuraUHC] Vous êtes mort ! Vous êtes désormais en mode spectateur.");
            }
        });
    }

    private Location getRandomRespawnLocation(World world, int minRadius, int maxRadius) {
        Random random = new Random();
        int distance = minRadius + random.nextInt(Math.max(1, maxRadius - minRadius));
        double angle = random.nextDouble() * 2 * Math.PI;

        int x = (int) (Math.cos(angle) * distance);
        int z = (int) (Math.sin(angle) * distance);
        int y = world.getHighestBlockYAt(x, z) + 1;

        return new Location(world, x + 0.5, y, z + 0.5);
    }
}