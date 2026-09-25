package uhc.tensuraUHC.listeners;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import uhc.tensuraUHC.TensuraUHC;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class LimitsListener implements Listener {

    private final TensuraUHC main;
    private final Map<UUID, Integer> minedDiamonds = new HashMap<>();

    public LimitsListener(TensuraUHC main) {
        this.main = main;
    }

    // --- 1. BLOQUER LE MINAGE EXCESSIF ---
    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        if (!main.isGameStarted()) return;

        Player player = event.getPlayer();
        Block block = event.getBlock();
        UUID uuid = player.getUniqueId();

        // Limite Diamant
        if (block.getType() == Material.DIAMOND_ORE) {
            int current = minedDiamonds.getOrDefault(uuid, 0);
            if (current >= main.getMaxMinedDiamonds()) {
                event.setCancelled(true);
                player.sendMessage(ChatColor.RED + "[TensuraUHC] Vous avez atteint la limite de diamants minés (" + main.getMaxMinedDiamonds() + ") !");
                return;
            }
            minedDiamonds.put(uuid, current + 1);
        }
    }

    // --- 2. RESTRICTION DU NOMBRE DE PIÈCES D'ARMURE EN DIAMANT ---
    @EventHandler
    public void onArmorEquip(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        Player player = (Player) event.getWhoClicked();

        // Exécuter la vérification au tick suivant le changement d'inventaire
        main.getServer().getScheduler().runTask(main, () -> checkDiamondArmorLimit(player));
    }

    @EventHandler
    public void onArmorInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        main.getServer().getScheduler().runTask(main, () -> checkDiamondArmorLimit(player));
    }

    private void checkDiamondArmorLimit(Player player) {
        ItemStack[] armor = player.getInventory().getArmorContents();
        int diamondPieces = 0;

        for (ItemStack item : armor) {
            if (item != null && isDiamondArmor(item.getType())) {
                diamondPieces++;
            }
        }

        if (diamondPieces > main.getMaxDiamondArmorPieces()) {
            // Retirer la dernière pièce équipée en trop et la remettre dans l'inventaire/au sol
            for (int i = 0; i < armor.length; i++) {
                if (armor[i] != null && isDiamondArmor(armor[i].getType())) {
                    ItemStack extraArmor = armor[i];
                    armor[i] = null;
                    player.getInventory().setArmorContents(armor);

                    HashMap<Integer, ItemStack> overflow = player.getInventory().addItem(extraArmor);
                    for (ItemStack item : overflow.values()) {
                        player.getWorld().dropItemNaturally(player.getLocation(), item);
                    }

                    player.sendMessage(ChatColor.RED + "[TensuraUHC] Limite d'armure diamant dépassée ! (Max : " + main.getMaxDiamondArmorPieces() + ")");
                    break;
                }
            }
        }
    }

    private boolean isDiamondArmor(Material mat) {
        return mat == Material.DIAMOND_HELMET || mat == Material.DIAMOND_CHESTPLATE
                || mat == Material.DIAMOND_LEGGINGS || mat == Material.DIAMOND_BOOTS;
    }
}