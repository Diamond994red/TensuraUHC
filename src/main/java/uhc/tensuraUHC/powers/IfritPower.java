package uhc.tensuraUHC.powers;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import uhc.tensuraUHC.TensuraUHC;

import java.util.ArrayList;
import java.util.List;

public class IfritPower {

    private final TensuraUHC main;
    private int usesLeft = 2;
    private boolean inUse = false;
    private BukkitTask activeTask;
    private BukkitTask auraTask;

    public IfritPower(TensuraUHC main) {
        this.main = main;
    }

    public static ItemStack createItem() {
        ItemStack ifritItem = new ItemStack(Material.NETHER_STAR);
        ItemMeta ifritMeta = ifritItem.getItemMeta();
        if (ifritMeta != null) {
            ifritMeta.setDisplayName(ChatColor.RED + "Ifrit");
            List<String> lore = new ArrayList<>();
            lore.add(ChatColor.GRAY + "Clic droit pour déchaîner le pouvoir d'Ifrit.");
            lore.add(ChatColor.DARK_RED + "2x par partie.");
            ifritMeta.setLore(lore);
            ifritItem.setItemMeta(ifritMeta);
        }
        return ifritItem;
    }

    public void activate(Player player) {
        if (usesLeft <= 0) {
            player.sendMessage(ChatColor.RED + "Vous avez déjà utilisé le pouvoir d'Ifrit 2 fois dans cette partie !");
            return;
        }
        if (inUse) {
            player.sendMessage(ChatColor.RED + "Le pouvoir d'Ifrit est déjà en cours d'utilisation !");
            return;
        }

        usesLeft--;
        startCycle(player);
    }

    private void startCycle(Player player) {
        final int formDuration = 5 * 60 * 20;
        inUse = true;

        player.removePotionEffect(PotionEffectType.SPEED);
        applyForm1(player);

        activeTask = Bukkit.getScheduler().runTaskLater(main, () -> {
            if (player.isOnline()) {
                clearFormEffects(player);
                applyForm2(player);

                activeTask = Bukkit.getScheduler().runTaskLater(main, () -> {
                    if (player.isOnline()) {
                        clearFormEffects(player);
                        applyForm3(player);

                        activeTask = Bukkit.getScheduler().runTaskLater(main, () -> {
                            if (player.isOnline()) {
                                clearFormEffects(player);
                                player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 99999 * 20, 0, false, false));
                                player.sendMessage(ChatColor.GOLD + "[Ifrit] " + ChatColor.RED + "L'esprit d'Ifrit s'apaise. Le pouvoir prend fin.");
                                player.playSound(player.getLocation(), Sound.FIZZ, 1.0f, 0.5f);
                            }
                            inUse = false;
                            if (auraTask != null) auraTask.cancel();
                        }, formDuration);
                    }
                }, formDuration);
            }
        }, formDuration);
    }

    private void applyForm1(Player player) {
        player.sendMessage(ChatColor.GOLD + "[Ifrit] " + ChatColor.RED + "Forme 1 active !");
        player.playSound(player.getLocation(), Sound.ENDERDRAGON_GROWL, 1.0f, 1.0f);
        player.addPotionEffect(new PotionEffect(PotionEffectType.INCREASE_DAMAGE, 5 * 60 * 20, 1, false, false));
        player.addPotionEffect(new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, 5 * 60 * 20, 1, false, false));
        player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 5 * 60 * 20, 1, false, false));
    }

    private void applyForm2(Player player) {
        player.sendMessage(ChatColor.GOLD + "[Ifrit] " + ChatColor.GOLD + "Forme 2 active !");
        player.playSound(player.getLocation(), Sound.WITHER_SHOOT, 1.0f, 0.8f);
        player.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, 5 * 60 * 20, 0, false, false));
        player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 5 * 60 * 20, 1, false, false));
    }

    private void applyForm3(Player player) {
        player.sendMessage(ChatColor.GOLD + "[Ifrit] " + ChatColor.DARK_RED + "Forme 3 active !");
        player.playSound(player.getLocation(), Sound.GHAST_CHARGE, 1.0f, 1.2f);

        auraTask = new BukkitRunnable() {
            int ticks = 0;

            @Override
            public void run() {
                if (!player.isOnline() || player.isDead() || ticks >= 5 * 60 * 20) {
                    this.cancel();
                    return;
                }

                for (Entity entity : player.getNearbyEntities(4.0, 4.0, 4.0)) {
                    if (entity instanceof Player) {
                        Player target = (Player) entity;
                        if (!target.hasPotionEffect(PotionEffectType.FIRE_RESISTANCE)) {
                            target.setFireTicks(40);
                            target.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, 40, 0, false, false));
                        }
                    }
                }
                ticks += 10;
            }
        }.runTaskTimer(main, 0L, 10L);
    }

    public void clearFormEffects(Player player) {
        if (player == null || !player.isOnline()) return;
        player.removePotionEffect(PotionEffectType.INCREASE_DAMAGE);
        player.removePotionEffect(PotionEffectType.DAMAGE_RESISTANCE);
        player.removePotionEffect(PotionEffectType.SLOW);
        player.removePotionEffect(PotionEffectType.WEAKNESS);
        player.removePotionEffect(PotionEffectType.SPEED);
    }

    public void reset(Player player) {
        if (activeTask != null) {
            activeTask.cancel();
            activeTask = null;
        }
        if (auraTask != null) {
            auraTask.cancel();
            auraTask = null;
        }
        clearFormEffects(player);
        this.inUse = false;
        this.usesLeft = 2;
    }
}