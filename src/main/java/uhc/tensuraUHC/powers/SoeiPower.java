package uhc.tensuraUHC.powers;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import uhc.tensuraUHC.TensuraUHC;
import uhc.tensuraUHC.roles.list.MonstersCamp.SoeiRole;
import uhc.tensuraUHC.roles.list.SoloCamp.YuukiRole;

import java.util.ArrayList;
import java.util.List;

public class SoeiPower {

    private final TensuraUHC main;
    private BukkitTask invisibilityTask;
    private boolean hasArmor =false;
    static List<Player> players = new ArrayList<>();

    public SoeiPower(TensuraUHC main) {
        this.main = main;
    }

    public static List<Player> getPlayers() {
        return players;
    }

    public static ItemStack createItem() {
        //sd
        return null;
    }

    public void activate(Player player) {
        players.add(player);
        startInvisibilityCheck(player);
    }

    private void startInvisibilityCheck(Player player) {
        invisibilityTask = new BukkitRunnable() {
            boolean added = false;
            boolean removed = true;

            @Override

            public void run() {
                if (!player.isOnline()) return;

                for (ItemStack armorPiece : player.getEquipment().getArmorContents()) {
                    if (armorPiece != null && armorPiece.getType() != Material.AIR) {
                        hasArmor = true;
                        break;
                    }
                }

                if (!hasArmor) {
                    player.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, 99999 * 20, 0, false, false));
                    if (!added)
                    {
                        added = true;
                        removed = false;
                        main.getNoFallPlayer().add(player.getUniqueId());
                    }
                } else {
                    if (!removed)
                    {
                        added = false;
                        removed = true;
                        main.getNoFallPlayer().remove(player.getUniqueId());
                    }
                    if (player.hasPotionEffect(PotionEffectType.INVISIBILITY)) {
                        player.removePotionEffect(PotionEffectType.INVISIBILITY);
                    }
                }
            }
        }.runTaskTimer(main, 0L, 20L); // Vérification toutes les secondes
    }


    public void reset(Player player) {
        if (invisibilityTask != null) {
            invisibilityTask.cancel();
            invisibilityTask = null;
        }
        players.clear();
    }
}