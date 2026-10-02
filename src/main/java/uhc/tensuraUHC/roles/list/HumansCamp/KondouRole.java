package uhc.tensuraUHC.roles.list.HumansCamp;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;
import uhc.tensuraUHC.TensuraUHC;
import uhc.tensuraUHC.powers.*;
import uhc.tensuraUHC.roles.Role;
import uhc.tensuraUHC.roles.list.MonstersCamp.SoeiRole;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class KondouRole extends Role {


    public KondouRole(TensuraUHC main) {
        super(main, "Kondou", Camp.HUMAINS, "Froid et calculateur, vous êtes un exécutant hors pair. " +
                "Votre capacité à percevoir l'état de vos ennemis vous permet d'achever instantanément les joueurs déjà affaiblis.");
        addPower("Vision de la vie", "Permet de voir la vie de tous les joueurs");
        addPower("Execution", "Tue les joueurs que vous touchez si ils ont moins de 2,5 coeurs");
    }

    @Override
    public void FakeRoleMessage(Player sender) {

    }

    @Override
    public void giveRole(Player player) {
        reset(player.getUniqueId());

        getItemsToGive().clear();

        setupHealthDisplay(player);
        super.giveRole(player);
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player victim = event.getEntity();
        Player killer = victim.getKiller();
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        if (!main.getRoleManager().hasRole(player.getUniqueId(), this)) return;
        if (event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK) return;

        ItemStack item = event.getItem();
        if (item == null || !item.hasItemMeta()) return;
    }

    @Override
    public void reset(UUID pl) {
        Player player = Bukkit.getPlayer(pl);
        super.reset(pl);
    }
    /**
     * Crée un Scoreboard privé pour Rudra lui permettant d'observer
     * la vie (HP) des autres joueurs sous leur pseudo.
     */
    private void setupHealthDisplay(Player kondou) {
        Scoreboard scoreboard = Bukkit.getScoreboardManager().getNewScoreboard();

        // En 1.8.8, on utilise la chaîne "health"
        Objective nameHealth = scoreboard.registerNewObjective("showHealthName", "health");
        nameHealth.setDisplaySlot(DisplaySlot.BELOW_NAME);
        nameHealth.setDisplayName(ChatColor.RED + "❤");

        // Assigne le scoreboard uniquement à Kondou
        kondou.setScoreboard(scoreboard);

        // Initialisation des HP des joueurs visibles sur le scoreboard de Kondou
        for (Player target : Bukkit.getOnlinePlayers()) {
            if (!target.hasPotionEffect(PotionEffectType.INVISIBILITY)) {
                nameHealth.getScore(target.getName()).setScore((int) Math.ceil(target.getHealth()));
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerDamage(EntityDamageEvent event) {
        if (event.getEntity() instanceof Player) {
            Player victim = (Player) event.getEntity();
            syncHealthForKondou(victim);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerHeal(EntityRegainHealthEvent event) {
        if (event.getEntity() instanceof Player) {
            Player victim = (Player) event.getEntity();
            syncHealthForKondou(victim);
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onPlayerDamaged(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player)) return;

        Player victim = (Player) event.getEntity();
        if (victim.isDead()) return; // Sécurité anti-double mort

        if (event instanceof EntityDamageByEntityEvent) {
            EntityDamageByEntityEvent damageByEntityEvent = (EntityDamageByEntityEvent) event;

            if (damageByEntityEvent.getDamager() instanceof Player) {
                Player damager = (Player) damageByEntityEvent.getDamager();
                Role damagerRole = main.getRoleManager().getRole(damager.getUniqueId());

                if (damagerRole instanceof KondouRole) {
                    double finalDamage = damageByEntityEvent.getFinalDamage();
                    double remainingHealth = victim.getHealth() - finalDamage;

                    // Si le coup tue DÉJÀ le joueur, on ne fait rien pour éviter de doubler la mort
                    if (remainingHealth <= 0) {
                        return;
                    }

                    // Si le coup le fait passer sous les 5.0 (2.5 cœurs), on ajuste les dégâts pour le tuer
                    if (remainingHealth <= 5.0) {
                        damageByEntityEvent.setDamage(1000.0);
                    }
                }
            }
        }
    }

    /**
     * Attend 1 tick pour lire la vie réelle de la victime et met à jour uniquement le scoreboard de Kondou.
     */
    private void syncHealthForKondou(Player victim) {
        Bukkit.getScheduler().runTask(main, () -> {
            if (!victim.isOnline()) return;

            int healthScore = (int) Math.ceil(victim.getHealth());
            boolean isInvisible = victim.hasPotionEffect(PotionEffectType.INVISIBILITY);

            for (UUID uuid : main.getGameManager().GetActivePlayers()) {
                if (main.getRoleManager().hasRole(uuid, this)) {
                    Player kondou = Bukkit.getPlayer(uuid);
                    if (kondou != null && kondou.isOnline()) {
                        Objective obj = kondou.getScoreboard().getObjective("showHealthName");
                        if (obj != null) {
                            if (isInvisible) {
                                // Supprime le score de la victime pour retirer l'affichage sous sa tête
                                kondou.getScoreboard().resetScores(victim.getName());
                            } else {
                                // Affiche la vie normalement si le joueur est visible
                                obj.getScore(victim.getName()).setScore(healthScore);
                            }
                        }
                    }
                }
            }
        });
    }
}