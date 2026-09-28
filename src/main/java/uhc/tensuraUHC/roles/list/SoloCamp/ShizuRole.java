package uhc.tensuraUHC.roles.list.SoloCamp;

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
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import uhc.tensuraUHC.TensuraUHC;
import uhc.tensuraUHC.powers.IfritPower;
import uhc.tensuraUHC.roles.Role;

import java.util.UUID;

public class ShizuRole extends Role {
    private BukkitTask proximityTask;
    private final IfritPower ifritPower;

    public ShizuRole(TensuraUHC main) {
        super(main, "Shizue", Camp.SOLITAIRE, "Maîtresse des flammes, vous recevez un livre Flame et Fire Aspect.\n" +
                "Ancienne héroïne légendaire, vous portez en vous le fardeau d'" + ChatColor.GOLD + "Ifrit.\n" + ChatColor.GRAY +
                "Vous possédez un lien d'âme unique avec " + ChatColor.GREEN + "Limule.\n" + ChatColor.GRAY +
                "Si vous restez 15 minutes (cumulées) à côté de lui, vous devrez alors gagner avec lui et ses alliés.");

        this.ifritPower = new IfritPower(main);

        addPassiveEffect(PotionEffectType.FIRE_RESISTANCE, 0);
        addPassiveEffect(PotionEffectType.SPEED, 0);

        addPower("Ifrit", "Permet d'alterner entre trois états toutes les 5 minutes (2x par partie) :\n" +
                "   - Forme 1 : Force II, Résistance II, Lenteur II\n" +
                "   - Forme 2 : Faiblesse I, Speed II\n" +
                "   - Forme 3 : Brûle et donne Faiblesse aux joueurs à - de 4 blocs (sauf si Fire Resistance)");

        addEnchantBypass(Enchantment.ARROW_FIRE, 1);
        addEnchantBypass(Enchantment.FIRE_ASPECT, 1);
        addEnchantBypass(Enchantment.DAMAGE_ALL, 4);
    }

    @Override
    public void FakeRoleMessage(Player sender) {

    }

    @Override
    public void giveRole(Player player) {
        getItemsToGive().clear();

        ItemStack fAspect = new ItemStack(Material.ENCHANTED_BOOK);
        ItemStack flame = new ItemStack(Material.ENCHANTED_BOOK);

        EnchantmentStorageMeta meta = (EnchantmentStorageMeta) fAspect.getItemMeta();
        EnchantmentStorageMeta meta2 = (EnchantmentStorageMeta) flame.getItemMeta();

        if (meta != null) {
            meta.addStoredEnchant(Enchantment.FIRE_ASPECT, 1, true);
            fAspect.setItemMeta(meta);
        }
        if (meta2 != null) {
            meta2.addStoredEnchant(Enchantment.ARROW_FIRE, 1, true);
            flame.setItemMeta(meta2);
        }

        addItem(fAspect);
        addItem(flame);
        addItem(IfritPower.createItem());
        startProximityCheck(player);
        super.giveRole(player);
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        if (!main.getRoleManager().hasRole(player.getUniqueId(), this)) return;
        if (event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK) return;

        ItemStack item = event.getItem();
        if (item == null || !item.hasItemMeta()) return;

        if (item.getType() == Material.NETHER_STAR && (ChatColor.RED + "Ifrit").equals(item.getItemMeta().getDisplayName())) {
            event.setCancelled(true);
            ifritPower.activate(player);
        }
    }

    @EventHandler
    public void onDeath(PlayerDeathEvent event) {
        Player victim = event.getEntity();
        if (main.getRoleManager().hasRole(victim.getUniqueId(), this)) return;
        {
            ifritPower.reset(victim);
        }
    }

    @Override
    public void reset(Player player) {
        super.reset(player);
        ifritPower.reset(player);
        if (proximityTask != null) {
            proximityTask.cancel();
            proximityTask = null;
        }
    }

    private void startProximityCheck(Player initialPlayer) {
        UUID playerUUID = initialPlayer.getUniqueId(); // Stocker l'UUID

        proximityTask = new BukkitRunnable() {
            int limuleTime = 0;
            int CurrentLimuleTime = 0;
            boolean campChanged = false;

            @Override
            public void run() {
                Player player = Bukkit.getPlayer(playerUUID);
                if (player == null || !player.isOnline()) return;

                if (!campChanged) {
                    for (Player nearby : player.getWorld().getPlayers()) {
                        if (nearby.equals(player)) continue;

                        if (!nearby.getWorld().equals(player.getWorld())) continue;

                        if (nearby.getLocation().distance(player.getLocation()) > 15) continue;

                        Role nearbyRole = main.getRoleManager().getPlayerRole(nearby.getUniqueId());
                        if (nearbyRole == null) continue;

                        if (nearbyRole.getName().equals("Limule")) {
                            limuleTime++;
                            CurrentLimuleTime++;
                            if (limuleTime >= 60 * 2) { // remettre à 15 * 60
                                if (nearbyRole.getCamp() == Camp.LIMULE) {
                                    setCamp(Camp.LIMULE);
                                    for (Player pl : player.getWorld().getPlayers()) {
                                        Role plRole = main.getRoleManager().getPlayerRole(pl.getUniqueId());
                                        if (plRole != null && plRole.getCamp() == Camp.LIMULE) {
                                            pl.sendMessage(ChatColor.GOLD + "Shizue" + ChatColor.GREEN + " est devenue votre coéquipière !");
                                        }
                                    }
                                    player.sendMessage(ChatColor.GREEN + "Vous n'êtes plus seule...");
                                    campChanged = true;
                                } else if (nearbyRole.getCamp() == Camp.MONSTRES) {
                                    setCamp(Camp.SHIZUE);
                                    nearby.sendMessage(ChatColor.GOLD + "Shizue" + ChatColor.GREEN + " est devenue votre seule coéquipière !");
                                    player.sendMessage(ChatColor.GREEN + "Vous n'êtes plus seule...");
                                    nearbyRole.setCamp(Camp.SHIZUE);
                                    campChanged = true;
                                }
                            }
                        }
                    }
                }
                if (main.getGameManager().IsNewEp()) {
                    player.sendMessage(ChatColor.GREEN + "Vous êtes restée " + formatTime(CurrentLimuleTime) + " avec Limule cet épisode.");
                    CurrentLimuleTime = 0;
                }
            }
        }.runTaskTimer(main, 0L, 20L);
    }
}