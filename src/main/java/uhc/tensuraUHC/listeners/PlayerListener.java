package uhc.tensuraUHC.listeners;

import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;
import uhc.tensuraUHC.TensuraUHC;

import java.util.Random;

public class PlayerListener implements Listener {

    private final TensuraUHC main;

    public PlayerListener(TensuraUHC main) {
        this.main = main;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        // Désactive la régénération naturelle du monde si la partie est lancée
        player.getWorld().setGameRuleValue("naturalRegeneration", "false");

        if (!main.isGameStarted()) {
            player.setGameMode(GameMode.ADVENTURE);
            //player.getInventory().clear();

            // Retire tous les effets de potion si la partie n'est pas en cours
            for (org.bukkit.potion.PotionEffect effect : player.getActivePotionEffects()) {
                player.removePotionEffect(effect.getType());
            }

            Location spawnLoc = new Location(player.getWorld(), 0, 202, 0);
            player.teleport(spawnLoc);
            main.buildGlassCage(spawnLoc);
            main.giveMenuItem(player);
        }

        if (main.getHostUUID() == null && player.isOp()) {
            main.setHostUUID(player.getUniqueId());
            player.sendMessage(ChatColor.GOLD + "[TensuraUHC] Vous êtes automatiquement défini comme Host !");
        }

        main.updatePlayerPrefix(player);
        main.getScoreboardManager().updateLobbyScoreboard(player);

        for (Player p : Bukkit.getOnlinePlayers()) {
            if (!main.isGameStarted()) main.getScoreboardManager().updateLobbyScoreboard(p);
        }

        event.setJoinMessage(ChatColor.GOLD + "[TensuraUHC] " + ChatColor.GREEN + player.getName() + ChatColor.GRAY + " [" + ChatColor.WHITE + Bukkit.getOnlinePlayers().size() + ChatColor.GRAY + "]");
    }

    // =========================================================================
    // BOOST D'EXPÉRIENCE (XP BOOST)
    // =========================================================================
    @EventHandler
    public void onExpChange(PlayerExpChangeEvent event) {
        // Multiplie l'XP gagnée selon la valeur configurée dans main (ex: 2.0 pour +100% / 2x)
        double xpMultiplier = main.getXpPercent();
        xpMultiplier /= 100;
        if (xpMultiplier > 1.0) {
            int originalAmount = event.getAmount();
            int newAmount = (int) Math.round(originalAmount * xpMultiplier);
            event.setAmount(newAmount);
        }
    }

    // =========================================================================
    // DÉSACTIVATION DE LA RÉGÉNÉRATION AUTOMATIQUE (UHC MODE)
    // =========================================================================
    @EventHandler
    public void onHealthRegen(EntityRegainHealthEvent event) {
        if (event.getEntity() instanceof Player) {
            // Bloque la régénération causée par la nourriture (satiété) ou la régénération naturelle
            if (event.getRegainReason() == EntityRegainHealthEvent.RegainReason.SATIATED
                    || event.getRegainReason() == EntityRegainHealthEvent.RegainReason.REGEN) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        int remainingCount = Bukkit.getOnlinePlayers().size() - 1;
        event.setQuitMessage(ChatColor.GOLD + "[" + remainingCount + "] " + ChatColor.RED + player.getName() + " a quitté le jeu !");

        Bukkit.getScheduler().runTaskLater(main, () -> {
            if (!main.isGameStarted()) {
                for (Player p : Bukkit.getOnlinePlayers()) {
                    main.getScoreboardManager().updateLobbyScoreboard(p);
                }
            }
        }, 1L);
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        ItemStack item = event.getItem();

        if (item != null && item.getType() == Material.NETHER_STAR && item.hasItemMeta()) {
            if (item.getItemMeta().getDisplayName().equals(ChatColor.GREEN + "Menu")) {
                event.setCancelled(true);
                if (main.isHostOrCoHost(player)) {
                    main.getGuiManager().openUHCMenu(player);
                }
            }
        }
    }

    @EventHandler
    public void onChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();

        if (main.isAwaitingGameNameInput() && main.isHostOrCoHost(player)) {
            event.setCancelled(true);
            main.setAwaitingGameNameInput(false);

            String newName = ChatColor.translateAlternateColorCodes('&', event.getMessage());
            main.setGameName(newName);

            player.sendMessage(ChatColor.GOLD + "[TensuraUHC] " + ChatColor.GREEN + "Nom de la partie défini sur : " + ChatColor.RESET + newName);

            Bukkit.getScheduler().runTask(main, () -> {
                for (Player p : Bukkit.getOnlinePlayers()) {
                    if (!main.isGameStarted()) main.getScoreboardManager().updateLobbyScoreboard(p);
                }
            });
            return;
        }

        if (main.isChatMuted()) {
            event.setCancelled(true);
            player.sendMessage(ChatColor.RED + "[TensuraUHC] Le chat global est actuellement désactivé !");
            return;
        }

        Scoreboard mainBoard = Bukkit.getScoreboardManager().getMainScoreboard();
        Team team = mainBoard.getEntryTeam(player.getName());

        String prefix = "";
        if (team != null && team.getPrefix() != null) {
            prefix = team.getPrefix();
        }

        event.setFormat(prefix + player.getName() + " :" + ChatColor.GRAY + " %2$s");
    }

    @EventHandler
    public void onLobbyFoodLevelChange(FoodLevelChangeEvent event) {
        if (!main.isGameStarted() && event.getEntity() instanceof Player) {
            Player player = (Player) event.getEntity();
            player.setFoodLevel(20);
            player.setSaturation(20f);
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlayerDropItem(PlayerDropItemEvent event) {
        if (!main.isGameStarted()) event.setCancelled(true);
    }

    @EventHandler
    public void onPlayerPickupItem(PlayerPickupItemEvent event) {
        if (!main.isGameStarted()) event.setCancelled(true);
    }
}