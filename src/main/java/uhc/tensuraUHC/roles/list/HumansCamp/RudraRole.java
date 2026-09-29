package uhc.tensuraUHC.roles.list.HumansCamp;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
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

    private BukkitTask humanTask;
    private boolean isSomeoneDead = false;

    public RudraRole(TensuraUHC main) {
        super(main, "Rudra", Camp.HUMAINS, "");
    }

    @Override
    public void FakeRoleMessage(Player sender) {

    }

    @Override
    public void giveRole(Player player) {
        getItemsToGive().clear();
        isSomeoneDead = true;

        // Active l'affichage de la vie uniquement pour Rudra
        setupHealthDisplay(player);

        startHumainLeftCheck(player.getUniqueId());
        super.giveRole(player);
    }

    /**
     * Crée un Scoreboard privé pour Rudra lui permettant d'observer
     * la vie (HP) des autres joueurs au-dessus de leur tête et dans le Tablist.
     */
    private void setupHealthDisplay(Player player) {
        Scoreboard scoreboard = Bukkit.getScoreboardManager().getNewScoreboard();

        // Objectif 1 : Affichage au-dessus du pseudo (Name Tag)
        Objective nameHealth = scoreboard.registerNewObjective("showHealthName", "health");
        nameHealth.setDisplaySlot(DisplaySlot.BELOW_NAME);
        nameHealth.setDisplayName(ChatColor.RED + "❤");

        // Objectif 2 : Affichage dans le menu TAB
        Objective tabHealth = scoreboard.registerNewObjective("showHealthTab", "health");
        tabHealth.setDisplaySlot(DisplaySlot.PLAYER_LIST);

        // Assigne le scoreboard uniquement au joueur ayant ce rôle
        player.setScoreboard(scoreboard);
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
        if (player != null && player.isOnline()) {
            // Re-met le scoreboard par défaut lors d'un reset
            player.setScoreboard(Bukkit.getScoreboardManager().getMainScoreboard());
        }

        if (humanTask != null) {
            humanTask.cancel();
            humanTask = null;
        }

        super.reset(pl);
    }

    void startHumainLeftCheck(UUID pl) {
        if (humanTask != null) {
            humanTask.cancel();
        }

        humanTask = new BukkitRunnable() {
            @Override
            public void run() {
                Player player = Bukkit.getPlayer(pl);

                // Annulation de la tâche si le joueur est hors-ligne
                if (player == null || !player.isOnline()) {
                    cancel();
                    return;
                }

                if (isSomeoneDead) {
                    int humanCount = 0;

                    // Vérification de sécurité pour éviter les NullPointerException
                    if (main != null && main.getGameManager() != null && main.getRoleManager() != null) {
                        for (UUID pls : main.getGameManager().GetActivePlayers()) {
                            Role role = main.getRoleManager().getPlayerRole(pls);
                            // Vérifie qu'un rôle est bien assigné avant d'appeler getCamp()
                            if (role != null && role.getCamp() == Camp.HUMAINS) {
                                humanCount++;
                            }
                        }
                    }

                    // Calcul du niveau de résistance
                    int cappedCount = Math.min(humanCount, 9);
                    int resLvl = cappedCount / 3;

                    if (getPassiveEffects() != null) {
                        getPassiveEffects().clear();
                    }

                    player.removePotionEffect(PotionEffectType.DAMAGE_RESISTANCE);

                    // Application du nouvel effet (Amplifier 0 = Resistance 1)
                    if (resLvl > 0) {
                        addPassiveEffect(PotionEffectType.DAMAGE_RESISTANCE, resLvl - 1);
                    }

                    isSomeoneDead = false;
                }
            }
        }.runTaskTimer(main, 0L, 20L);
    }
}