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

        super(main, "Rudra", Camp.HUMAINS, "Empereur suprême de l'Empire d'Orient et Premier Héros, " +
                "vous tirez votre force de la survie de vos sujets.");
    }

    @Override
    public void FakeRoleMessage(Player sender) {

    }

    @Override
    public void giveRole(Player player) {
        reset(player.getUniqueId());
        getItemsToGive().clear();
        isSomeoneDead = true;
        startHumainLeftCheck(player.getUniqueId());
        giveTeammatesList(player);
        super.giveRole(player);
    }

    private void giveTeammatesList(Player player) {
        StringBuilder str = new StringBuilder();

        for (UUID pl : main.getGameManager().GetActivePlayers())
        {
            if (main.getRoleManager().getPlayerRole(pl).getCamp() == Camp.HUMAINS)
            {
                str.append(" ");
                str.append(Bukkit.getPlayer(pl).getName());
            }
        }
        addPower("Liste des humains", str.toString());
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