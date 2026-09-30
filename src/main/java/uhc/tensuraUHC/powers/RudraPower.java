//package uhc.tensuraUHC.powers;
//
//import org.bukkit.Bukkit;
//import org.bukkit.ChatColor;
//import org.bukkit.Material;
//import org.bukkit.entity.Player;
//import org.bukkit.event.EventHandler;
//import org.bukkit.event.EventPriority;
//import org.bukkit.event.entity.EntityDamageEvent;
//import org.bukkit.event.entity.EntityRegainHealthEvent;
//import org.bukkit.inventory.ItemStack;
//import org.bukkit.potion.PotionEffect;
//import org.bukkit.potion.PotionEffectType;
//import org.bukkit.scheduler.BukkitRunnable;
//import org.bukkit.scheduler.BukkitTask;
//import org.bukkit.scoreboard.DisplaySlot;
//import org.bukkit.scoreboard.Objective;
//import org.bukkit.scoreboard.Scoreboard;
//import uhc.tensuraUHC.TensuraUHC;
//
//import java.util.ArrayList;
//import java.util.List;
//import java.util.UUID;
//
//public class RudraPower {
//
//    private final TensuraUHC main;
//    private BukkitTask invisibilityTask;
//    static List<Player> players = new ArrayList<>();
//
//    public RudraPower(TensuraUHC main) {
//        this.main = main;
//    }
//
//    public static List<Player> getPlayers() {
//        return players;
//    }
//
//    public static ItemStack createItem() {
//        //sd
//        return null;
//    }
//
//    public void activate(Player player) {
//        players.add(player);
//        startSeeHealth(player);
//    }
//
//    private void setupHealthDisplay(Player rudra) {
//        Scoreboard scoreboard = Bukkit.getScoreboardManager().getNewScoreboard();
//
//        // En 1.8.8, on utilise la chaîne "health"
//        Objective nameHealth = scoreboard.registerNewObjective("showHealthName", "health");
//        nameHealth.setDisplaySlot(DisplaySlot.BELOW_NAME);
//        nameHealth.setDisplayName(ChatColor.RED + "❤");
//
//        // Assigne le scoreboard uniquement à Rudra
//        rudra.setScoreboard(scoreboard);
//
//        // Initialisation des HP de tous les joueurs sur le scoreboard de Rudra
//        for (Player target : Bukkit.getOnlinePlayers()) {
//            nameHealth.getScore(target.getName()).setScore((int) Math.ceil(target.getHealth()));
//        }
//    }
//
//    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
//    public void onPlayerDamage(EntityDamageEvent event) {
//        if (event.getEntity() instanceof Player) {
//            Player victim = ((Player) event.getEntity()).getPlayer();
//            syncHealthForRudra(victim);
//        }
//    }
//
//    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
//    public void onPlayerHeal(EntityRegainHealthEvent event) {
//        if (event.getEntity() instanceof Player) {
//            Player victim = ((Player) event.getEntity()).getPlayer();
//            syncHealthForRudra(victim);
//        }
//    }
//
//    /**
//     * Attend 1 tick pour lire la vie réelle de la victime et met à jour uniquement le scoreboard de Rudra.
//     */
//    private void syncHealthForRudra(Player victim) {
//        Bukkit.getScheduler().runTask(main, () -> {
//            if (!victim.isOnline()) return;
//
//            int healthScore = (int) Math.ceil(victim.getHealth());
//
//            // Recherche le joueur qui a le rôle Rudra dans la partie
//            for (UUID uuid : main.getGameManager().GetActivePlayers()) {
//                if (main.getRoleManager().hasRole(uuid, this)) {
//                    Player rudra = Bukkit.getPlayer(uuid);
//                    if (rudra != null && rudra.isOnline()) {
//                        Objective obj = rudra.getScoreboard().getObjective("showHealthName");
//                        if (obj != null) {
//                            obj.getScore(victim.getName()).setScore(healthScore);
//                        }
//                    }
//                }
//            }
//        });
//    }
//
//    public void reset(Player player) {
//        if (invisibilityTask != null) {
//            invisibilityTask.cancel();
//            invisibilityTask = null;
//        }
//        players.clear();
//    }
//}