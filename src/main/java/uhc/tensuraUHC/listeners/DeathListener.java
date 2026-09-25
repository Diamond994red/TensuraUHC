package uhc.tensuraUHC.listeners;

import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.ItemStack;
import uhc.tensuraUHC.TensuraUHC;
import uhc.tensuraUHC.roles.Role;

import java.util.Random;

public class DeathListener implements Listener {

    private final TensuraUHC main;

    public DeathListener(TensuraUHC main) {
        this.main = main;
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player victim = event.getEntity();
        Player killer = victim.getKiller();

        if (killer != null) {
            main.getKills().put(killer.getUniqueId(), main.getKills().getOrDefault(killer.getUniqueId(), 0) + 1);
        }

        Location deathLocation = victim.getLocation();

        // 1. Vérification stricte
        boolean rolesRevealed = (main.getGameManager() != null && main.getGameManager().GetRolesRevealed())
                || (main.getGameManager().getTotalGameSeconds() >= main.GetRoleTime());

        // 2. MODIFICATIONS SYNCHRONES (Immédiates pendant l'event)
        if (rolesRevealed) {
            event.setDeathMessage("================-================\n    " + victim.getName() + " est mort. Il était " + main.getRoleManager().getPlayerRole(victim).getCamp().getColor() + main.getRoleManager().getPlayerRole(victim).getName() + ".\n=================================");

            // On fait spawner la gapple directement au sol à la position de la mort
            if (deathLocation.getWorld() != null) {
                deathLocation.getWorld().dropItemNaturally(deathLocation, new ItemStack(Material.GOLDEN_APPLE, 1));
            }
        }

        // 3. ACTIONS DIFFÉRÉES (Respawn & Téléportation au tick suivant)
        Bukkit.getScheduler().runTask(main, () -> {
            // Forcer le respawn instantané
            victim.spigot().respawn();

            if (!rolesRevealed) {
                // --- CAS : Rôles NON annoncés -> Respawn Survie ---
                World gameWorld = main.getGameWorld();
                if (gameWorld == null) {
                    gameWorld = Bukkit.getWorld("uhc_world") != null ? Bukkit.getWorld("uhc_world") : victim.getWorld();
                }

                Location randomLoc = getRandomRespawnLocation(gameWorld, 0, main.getBorderManager().getCurrentBorderRadius(gameWorld));

                victim.setGameMode(GameMode.SURVIVAL);
                victim.setHealth(victim.getMaxHealth());
                victim.setFoodLevel(20);
                victim.teleport(randomLoc);

                victim.sendMessage(ChatColor.GOLD + "[TensuraUHC] " + ChatColor.YELLOW + "Les rôles ne sont pas encore annoncés ! Vous avez réapparu à un endroit distant.");
            } else {
                // --- CAS : Rôles DÉJÀ annoncés -> Spectateur ---
                victim.setGameMode(GameMode.SPECTATOR);
                victim.teleport(deathLocation);
                victim.sendMessage(ChatColor.RED + "[TensuraUHC] Vous êtes mort ! Vous êtes désormais en mode spectateur.");
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