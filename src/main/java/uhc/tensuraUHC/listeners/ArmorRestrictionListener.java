package uhc.tensuraUHC.listeners;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import uhc.tensuraUHC.TensuraUHC;

public class ArmorRestrictionListener implements Listener {

    private final TensuraUHC main;

    public ArmorRestrictionListener(TensuraUHC main) {
        this.main = main;
    }

    // 1. Empêcher l'équipement via Clic Droit depuis la main
    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK) return;

        Player player = event.getPlayer();
        ItemStack item = event.getItem();

        if (item == null || !isDiamondArmor(item.getType())) return;

        if (wouldExceedLimitOnEquip(player, item.getType())) {
            event.setCancelled(true);
            player.sendMessage(ChatColor.RED + "[TensuraUHC] Vous ne pouvez pas porter plus de "
                    + main.getMaxDiamondArmorPieces() + " pièce(s) d'armure en diamant !");
        }
    }

    // 2. Empêcher le placement direct dans les slots d'armure ou le Shift-Clic
    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        Player player = (Player) event.getWhoClicked();

        ItemStack current = event.getCurrentItem();
        ItemStack cursor = event.getCursor();

        // CAS A : Placement manuel dans un slot d'armure (slots 36 à 39 dans l'inventaire joueur)
        if (event.getSlotType() == InventoryType.SlotType.ARMOR) {
            if (cursor != null && isDiamondArmor(cursor.getType())) {
                if (wouldExceedLimitOnEquip(player, cursor.getType())) {
                    event.setCancelled(true);
                    player.sendMessage(ChatColor.RED + "[TensuraUHC] Limite de pièces en diamant atteinte ("
                            + main.getMaxDiamondArmorPieces() + " max) !");
                }
            }
        }

        // CAS B : Shift-Clic sur une pièce d'armure en diamant depuis le contenu de l'inventaire
        if (event.isShiftClick() && current != null && isDiamondArmor(current.getType())) {
            // Vérifier si la pièce tenterait d'aller dans un slot d'armure vide
            if (wouldExceedLimitOnEquip(player, current.getType())) {
                event.setCancelled(true);
                player.sendMessage(ChatColor.RED + "[TensuraUHC] Limite de pièces en diamant atteinte ("
                        + main.getMaxDiamondArmorPieces() + " max) !");
            }
        }
    }

    private boolean isDiamondArmor(Material mat) {
        return mat == Material.DIAMOND_HELMET ||
                mat == Material.DIAMOND_CHESTPLATE ||
                mat == Material.DIAMOND_LEGGINGS ||
                mat == Material.DIAMOND_BOOTS;
    }

    private boolean wouldExceedLimitOnEquip(Player player, Material newArmorMat) {
        int maxAllowed = main.getMaxDiamondArmorPieces();
        int currentEquippedCount = 0;

        for (ItemStack armor : player.getInventory().getArmorContents()) {
            if (armor != null && isDiamondArmor(armor.getType())) {
                currentEquippedCount++;
            }
        }

        // Si le joueur tente de remplacer un morceau de diamant déjà porté, le nombre reste le même
        // Sinon, le nombre augmenterait de 1
        return currentEquippedCount >= maxAllowed;
    }
}