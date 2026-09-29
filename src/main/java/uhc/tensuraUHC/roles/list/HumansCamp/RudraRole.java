package uhc.tensuraUHC.roles.list.HumansCamp;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.bukkit.plugin.Plugin;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
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
        startHumainLeftCheck(player.getUniqueId());
        super.giveRole(player);
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        if (!main.getRoleManager().hasRole(player.getUniqueId(), this)) return;
        if (event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK) return;

        ItemStack item = event.getItem();
        if (item == null || !item.hasItemMeta()) return;

//        if (item.getType() == Material.NETHER_STAR && (ChatColor.RED + "Ifrit").equals(item.getItemMeta().getDisplayName())) {
//            event.setCancelled(true);
//            ifritPower.activate(player);
//        }
    }

    @EventHandler
    public void onDeath(PlayerDeathEvent event) {
        Player victim = event.getEntity();
        isSomeoneDead = true;
        if (main.getRoleManager().hasRole(victim.getUniqueId(), this)) return;
        {

        }
    }

    @Override
    public void reset(UUID pl) {
        Player player = Bukkit.getPlayer(pl);
        super.reset(pl);
    }

    void startHumainLeftCheck(UUID pl)
    {
        humanTask = new BukkitRunnable() {
            int resLvl = 0;
            @Override
            public void run() {
                Player player = Bukkit.getPlayer(pl);
                if (player == null || !player.isOnline()) return;
                if (isSomeoneDead)
                {
                    getPassiveEffects().clear();
                    player.removePotionEffect(PotionEffectType.DAMAGE_RESISTANCE);
                    for (UUID pls : main.getGameManager().GetActivePlayers()) {
                        if (main.getRoleManager().getPlayerRole(pls).getCamp() == Camp.HUMAINS) {
                            resLvl = (resLvl >= 9 ? resLvl : resLvl + 1);
                        }
                    }
                    resLvl /= 3;
                    addPassiveEffect(PotionEffectType.DAMAGE_RESISTANCE, resLvl);
                }
            }
        }.runTaskTimer(main, 0L, 20L);
    }
}