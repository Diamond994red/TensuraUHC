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
    private void setupHealthDisplay(Player Kondou) {
        Scoreboard scoreboard = Bukkit.getScoreboardManager().getNewScoreboard();

        // En 1.8.8, on utilise la chaîne "health"
        Objective nameHealth = scoreboard.registerNewObjective("showHealthName", "health");
        nameHealth.setDisplaySlot(DisplaySlot.BELOW_NAME);
        nameHealth.setDisplayName(ChatColor.RED + "❤");

        // Assigne le scoreboard uniquement à Rudra
        Kondou.setScoreboard(scoreboard);

        // Initialisation des HP de tous les joueurs sur le scoreboard de Rudra
        for (Player target : Bukkit.getOnlinePlayers()) {
            nameHealth.getScore(target.getName()).setScore((int) Math.ceil(target.getHealth()));
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerDamage(EntityDamageEvent event) {
        if (event.getEntity() instanceof Player) {
            Player victim = (Player) event.getEntity();

            // On vérifie si les dégâts proviennent d'une autre entité/joueur
            if (event instanceof EntityDamageByEntityEvent) {
                EntityDamageByEntityEvent damageByEntityEvent = (EntityDamageByEntityEvent) event;

                // On s'assure que l'attaquant est un joueur
                if (damageByEntityEvent.getDamager() instanceof Player) {
                    Player damager = (Player) damageByEntityEvent.getDamager();

                    Role damagerRole = main.getRoleManager().getRole(damager.getUniqueId());
                    if (victim.getHealth() <= 5.0 && damagerRole instanceof KondouRole) {
                        victim.setHealth(0);
                    }
                }
            }
            syncHealthForKondou(victim);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerHeal(EntityRegainHealthEvent event) {
        if (event.getEntity() instanceof Player) {
            Player victim = ((Player) event.getEntity()).getPlayer();
            syncHealthForKondou(victim);
        }
    }

    /**
     * Attend 1 tick pour lire la vie réelle de la victime et met à jour uniquement le scoreboard de Rudra.
     */
    private void syncHealthForKondou(Player victim) {
        Bukkit.getScheduler().runTask(main, () -> {
            if (!victim.isOnline()) return;

            int healthScore = (int) Math.ceil(victim.getHealth());

            for (UUID uuid : main.getGameManager().GetActivePlayers()) {
                if (main.getRoleManager().hasRole(uuid, this)) {
                    Player kondou = Bukkit.getPlayer(uuid);
                    if (kondou != null && kondou.isOnline()) {
                        Objective obj = kondou.getScoreboard().getObjective("showHealthName");
                        if (obj != null) {
                            obj.getScore(victim.getName()).setScore(healthScore);
                        }
                    }
                }
            }
        });
    }
}