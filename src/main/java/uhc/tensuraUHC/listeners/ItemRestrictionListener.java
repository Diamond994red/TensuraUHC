package uhc.tensuraUHC.listeners;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.PrepareItemCraftEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import uhc.tensuraUHC.TensuraUHC;
import uhc.tensuraUHC.powers.SoeiPower;

public class ItemRestrictionListener implements Listener {

    private final TensuraUHC main;

    public ItemRestrictionListener(TensuraUHC main) {
        this.main = main;
    }

    // =========================================================================
    // 1. BLOQUER LE CRAFT DE LA POMME DE NOTCH (Durabilité 1)
    // =========================================================================
    @EventHandler
    public void onCraft(PrepareItemCraftEvent event) {
        if (event.getRecipe() == null) return;

        ItemStack result = event.getRecipe().getResult();
        if (result == null) return;

        // Si le craft donne une Pomme de Notch (Durabilité 1)
        if (result.getType() == Material.GOLDEN_APPLE && result.getDurability() == 1) {
            if (main.isNotchAppleDisabled()) {
                event.getInventory().setResult(new ItemStack(Material.AIR));
                return;
            }
        }

        // Pour les autres items classiques du menu
        if (result.getType() != Material.GOLDEN_APPLE && main.isItemDisabled(result.getType())) {
            event.getInventory().setResult(new ItemStack(Material.AIR));
        }
    }

    // =========================================================================
    // 2. BLOQUER L'INTERACTION
    // =========================================================================
    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        ItemStack item = event.getItem();
        if (item == null) return;

        Player player = event.getPlayer();
        Material type = item.getType();

        // Gestion des lits dans le Nether / End
        if (event.getClickedBlock() != null && event.getClickedBlock().getType() == Material.BED) {
            if (main.isItemDisabled(Material.BED)) {
                event.setCancelled(true);
                player.sendMessage(ChatColor.RED + "[TensuraUHC] L'utilisation des lits est désactivée !");
                return;
            }
        }

        // Bloquer l'utilisation de la Pomme de Notch en clic droit
        if (type == Material.GOLDEN_APPLE && item.getDurability() == 1 && main.isNotchAppleDisabled()) {
            if (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK) {
                event.setCancelled(true);
                player.sendMessage(ChatColor.RED + "[TensuraUHC] La Pomme de Notch est désactivée !");
                return;
            }
        }

        // Bloquer les autres items généraux (Pearl, Lava Bucket, etc.)
        if (type != Material.GOLDEN_APPLE && main.isItemDisabled(type)) {
            if (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK) {
                event.setCancelled(true);
                player.sendMessage(ChatColor.RED + "[TensuraUHC] Cet objet (" + type.name() + ") est désactivé !");
            }
        }
    }

    // =========================================================================
    // 3. BLOQUER LA CONSOMMATION DE LA POMME DE NOTCH
    // =========================================================================
    @EventHandler
    public void onConsume(PlayerItemConsumeEvent event) {
        ItemStack item = event.getItem();
        if (item == null) return;

        // Pomme de Notch (Durabilité = 1)
        if (item.getType() == Material.GOLDEN_APPLE && item.getDurability() == 1) {
            if (main.isNotchAppleDisabled()) {
                event.setCancelled(true);
                event.getPlayer().sendMessage(ChatColor.RED + "[TensuraUHC] La Pomme de Notch est désactivée !");
                return;
            }
        }
        if (item.getType() == Material.MILK_BUCKET) {
            event.setCancelled(true);
            event.getPlayer().sendMessage(ChatColor.RED + "[TensuraUHC] Cet objet est désactivé !");
        }

        // Bloquer la consommation pour d'autres objets désactivés si besoin
        if (item.getType() != Material.GOLDEN_APPLE && main.isItemDisabled(item.getType())) {
            event.setCancelled(true);
            event.getPlayer().sendMessage(ChatColor.RED + "[TensuraUHC] Cet objet est désactivé !");
        }
        if (event.getItem().getType() == Material.GOLDEN_APPLE && event.getItem().getDurability() == 0) {
            Player player = event.getPlayer();

            boolean eligible = false;
            boolean hasArmor = false;
            for (Player pl : SoeiPower.getPlayers()) {
                if (pl == player) {
                    eligible = true;
                }
            }
            for (ItemStack armorPiece : player.getEquipment().getArmorContents()) {
                if (armorPiece != null && armorPiece.getType() != Material.AIR) {
                    hasArmor = true;
                    break;
                }
            }
            if (eligible && !hasArmor) {
                event.setCancelled(true);

                // Retirer 1 item correctement du stack dans la main du joueur
                ItemStack inHand = player.getItemInHand();
                if (inHand != null && inHand.getAmount() > 1) {
                    inHand.setAmount(inHand.getAmount() - 1);
                    player.setItemInHand(inHand);
                } else {
                    player.setItemInHand(null);
                }

                player.updateInventory();

                player.setFoodLevel(Math.min(20, player.getFoodLevel() + 4));
                player.setSaturation(Math.min(player.getFoodLevel(), player.getSaturation() + 9.6f));

                Bukkit.getScheduler().runTask(main, () -> {
                    player.addPotionEffect(new PotionEffect(PotionEffectType.ABSORPTION, 120 * 20, 0, false, false));
                    player.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 5 * 20, 1, false, false));
                });
            }
        }
    }


    // =========================================================================
    // 4. INTERACTION SUR ENTITÉ (Canne à pêche, etc.)
    // =========================================================================
    @EventHandler
    public void onInteractEntity(PlayerInteractEntityEvent event) {
        Player player = event.getPlayer();
        ItemStack item = player.getItemInHand();

        if (item != null && item.getType() != Material.GOLDEN_APPLE && main.isItemDisabled(item.getType())) {
            event.setCancelled(true);
            player.sendMessage(ChatColor.RED + "[TensuraUHC] Cet objet est désactivé !");
        }
    }
}