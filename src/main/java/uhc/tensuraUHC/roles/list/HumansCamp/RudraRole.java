package uhc.tensuraUHC.roles.list.HumansCamp;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;
import uhc.tensuraUHC.TensuraUHC;
import uhc.tensuraUHC.roles.Role;

import java.util.UUID;

public class RudraRole extends Role {

    private boolean isSomeoneDead = false;

    public RudraRole(TensuraUHC main) {
        super(main, "Rudra", Camp.HUMAINS, "");
    }

    @Override
    public void FakeRoleMessage(Player sender) {

    }

    @Override
    public void giveRole(Player player) {
        reset(player.getUniqueId());
        getItemsToGive().clear();
        isSomeoneDead = true;

        // Active l'affichage de la vie uniquement pour Rudra
        setupHealthDisplay(player);

        startHumainLeftCheck(player.getUniqueId());
        super.giveRole(player);
    }

    /**
     * Crée un Scoreboard privé pour Rudra lui permettant d'observer
     * la vie (HP) des autres joueurs sous leur pseudo.
     */
    private void setupHealthDisplay(Player rudra) {
        Scoreboard scoreboard = Bukkit.getScoreboardManager().getNewScoreboard();

        // En 1.8.8, on utilise la chaîne "health"
        Objective nameHealth = scoreboard.registerNewObjective("showHealthName", "health");
        nameHealth.setDisplaySlot(DisplaySlot.BELOW_NAME);
        nameHealth.setDisplayName(ChatColor.RED + "❤");

        // Assigne le scoreboard uniquement à Rudra
        rudra.setScoreboard(scoreboard);

        // Initialisation des HP de tous les joueurs sur le scoreboard de Rudra
        for (Player target : Bukkit.getOnlinePlayers()) {
            nameHealth.getScore(target.getName()).setScore((int) Math.ceil(target.getHealth()));
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerDamage(EntityDamageEvent event) {
        if (event.getEntity() instanceof Player) {
            Player victim = ((Player) event.getEntity()).getPlayer();
            syncHealthForRudra(victim);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerHeal(EntityRegainHealthEvent event) {
        if (event.getEntity() instanceof Player) {
            Player victim = ((Player) event.getEntity()).getPlayer();
            syncHealthForRudra(victim);
        }
    }

    /**
     * Attend 1 tick pour lire la vie réelle de la victime et met à jour uniquement le scoreboard de Rudra.
     */
    private void syncHealthForRudra(Player victim) {
        Bukkit.getScheduler().runTask(main, () -> {
            if (!victim.isOnline()) return;

            int healthScore = (int) Math.ceil(victim.getHealth());

            for (UUID uuid : main.getGameManager().GetActivePlayers()) {
                // S'assure que le joueur est en ligne ET qu'il a TOUJOURS ce rôle
                if (main.getRoleManager().hasRole(uuid, this)) {
                    Player rudra = Bukkit.getPlayer(uuid);
                    if (rudra != null && rudra.isOnline()) {
                        Objective obj = rudra.getScoreboard().getObjective("showHealthName");
                        if (obj != null) {
                            obj.getScore(victim.getName()).setScore(healthScore);
                        }
                    }
                }
            }
        });
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        if (!main.getRoleManager().hasRole(player.getUniqueId(), this)) return;
        if (event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK) return;

        ItemStack item = event.getItem();
        if (item == null || !item.hasItemMeta()) return;
    }

    @EventHandler
    public void onDeath(PlayerDeathEvent event) {
        Player victim = event.getEntity();

        // Active la mise à jour des effets si un joueur de la partie meurt
        if (main.getGameManager() != null && main.getGameManager().GetActivePlayers().contains(victim.getUniqueId())) {
            isSomeoneDead = true;
        }
    }

    @Override
    public void reset(UUID pl) {
        Player player = Bukkit.getPlayer(pl);
        // 3. Appel de la méthode parente (annule les tâches Bukkit et réinitialise le camp)
        super.reset(pl);
    }

    void startHumainLeftCheck(UUID pl) {

        BukkitTask humanTask = new BukkitRunnable() {
            @Override
            public void run() {
                Player player = Bukkit.getPlayer(pl);

                if (player == null || !player.isOnline()) {
                    return;
                }
                if (!main.getRoleManager().hasRole(pl, RudraRole.this)) {
                    cancel();
                    return;
                }

                int humanCount = 0;

                if (main.getGameManager() != null && main.getRoleManager() != null) {
                    for (UUID pls : main.getGameManager().GetActivePlayers()) {
                        Role role = main.getRoleManager().getPlayerRole(pls);
                        if (role != null && role.getCamp() == Camp.HUMAINS) {
                            humanCount++;
                        }
                    }
                }

                // 1. Retrait des anciens effets enregistrés dans passiveEffects
                for (PotionEffect effect : getPassiveEffects()) {
                    player.removePotionEffect(effect.getType());
                }

                // Clear de la liste avant d'ajouter le nouveau niveau
                getPassiveEffects().clear();

                // 2. Calcul et enregistrement du nouvel effet si des humains sont en vie
                if (humanCount > 0) {
                    int cappedCount = Math.min(humanCount, 6);
                    int resLvl = cappedCount / 3;

                    // addPassiveEffect() ajoute l'effet dans la liste passiveEffects
                    addPassiveEffect(PotionEffectType.DAMAGE_RESISTANCE, resLvl);
                }

                // 3. Application des nouveaux effets passifs au joueur
                for (PotionEffect effect : getPassiveEffects()) {
                    player.addPotionEffect(effect);
                }
            }
        }.runTaskTimer(main, 0L, 20L);
        addTask(pl,"humains",humanTask);
    }
}