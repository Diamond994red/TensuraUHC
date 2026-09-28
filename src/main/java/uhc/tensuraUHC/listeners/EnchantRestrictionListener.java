package uhc.tensuraUHC.listeners;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.enchantment.EnchantItemEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.bukkit.inventory.meta.ItemMeta;
import uhc.tensuraUHC.TensuraUHC;
import uhc.tensuraUHC.roles.Role;

import java.util.Map;

public class EnchantRestrictionListener implements Listener {

    private final TensuraUHC main;

    public EnchantRestrictionListener(TensuraUHC main) {
        this.main = main;
    }

    // A. Bloquer/Cap la table d'enchantement
    @EventHandler
    public void onEnchantItem(EnchantItemEvent event) {
        ItemStack item = event.getItem();
        Map<Enchantment, Integer> enchants = event.getEnchantsToAdd();
        Player player = event.getEnchanter();
        boolean modified = false;

        for (Map.Entry<Enchantment, Integer> entry : enchants.entrySet()) {
            Enchantment ench = entry.getKey();
            int currentLvl = entry.getValue();
            int maxAllowed = getMaxAllowed(player, item.getType(), ench);

            if (currentLvl > maxAllowed) {
                enchants.put(ench, maxAllowed);
                modified = true;
            }
        }

        if (modified && player != null) {
            player.sendMessage(ChatColor.GOLD + "[TensuraUHC] " + ChatColor.RED +
                    "L'enchantement dépasse la limite autorisée et a été réduit au niveau max autorisé!");
        }
    }

    // B. Bloquer/Cap la fusion sur l'enclume (Compatible 1.8)
    @EventHandler
    public void onAnvilClick(InventoryClickEvent event) {
        if (event.getInventory().getType() != InventoryType.ANVIL) return;
        if (event.getRawSlot() != 2) return;

        ItemStack result = event.getCurrentItem();
        if (result == null || result.getType() == Material.AIR) return;
        if (!(event.getWhoClicked() instanceof Player)) return;

        Player player = (Player) event.getWhoClicked();
        ItemMeta meta = result.getItemMeta();
        boolean modified = false;

        if (meta instanceof EnchantmentStorageMeta) {
            EnchantmentStorageMeta bookMeta = (EnchantmentStorageMeta) meta;
            for (Map.Entry<Enchantment, Integer> entry : bookMeta.getStoredEnchants().entrySet()) {
                Enchantment ench = entry.getKey();
                int lvl = entry.getValue();
                int maxAllowed = getMaxAllowed(player, Material.DIAMOND_SWORD, ench);

                if (lvl > maxAllowed) {
                    bookMeta.addStoredEnchant(ench, maxAllowed, true);
                    modified = true;
                }
            }
        } else {
            for (Map.Entry<Enchantment, Integer> entry : meta.getEnchants().entrySet()) {
                Enchantment ench = entry.getKey();
                int lvl = entry.getValue();
                int maxAllowed = getMaxAllowed(player, result.getType(), ench);

                if (lvl > maxAllowed) {
                    meta.addEnchant(ench, maxAllowed, true);
                    modified = true;
                }
            }
        }

        if (modified) {
            result.setItemMeta(meta);
            player.sendMessage(ChatColor.GOLD + "[TensuraUHC] " + ChatColor.RED +
                    "Le niveau maximal autorisé pour votre rôle a été appliqué !");
        }
    }

    private int getMaxAllowed(Player player, Material mat, Enchantment ench) {
        // 1. Vérification si le rôle du joueur possède un dépassement spécifique
        if (player != null && main.getRoleManager() != null) {
            Role role = main.getRoleManager().getPlayerRole(player.getUniqueId());
            if (role != null && role.getEnchantBypasses().containsKey(ench)) {
                return role.getEnchantBypasses().get(ench);
            }
        }

        // 2. Sinon, application des limites standard définies par l'host
        String name = mat.name();

        boolean isIron = name.startsWith("IRON_");
        boolean isDiamond = name.startsWith("DIAMOND_");
        boolean isBow = (mat == Material.BOW);

        // --- ENCHANTEMENTS ÉPÉE / ARMURE ---
        if (ench.equals(Enchantment.DAMAGE_ALL)) { // Sharpness / Tranchant
            if (isIron) return main.getIronSharpnessMax();
            if (isDiamond) return main.getDiamondSharpnessMax();
        } else if (ench.equals(Enchantment.PROTECTION_ENVIRONMENTAL)) { // Protection
            if (isIron) return main.getIronProtectionMax();
            if (isDiamond) return main.getDiamondProtectionMax();
        } else if (ench.equals(Enchantment.KNOCKBACK)) { // Knockback / Recul
            if (isIron) return main.getIronKnockbackMax();
            if (isDiamond) return main.getDiamondKnockbackMax();
        } else if (ench.equals(Enchantment.FIRE_ASPECT)) { // Fire Aspect / Aura de feu
            if (isIron) return main.getIronFireMax();
            if (isDiamond) return main.getDiamondFireMax();
        }

        // --- ENCHANTEMENTS ARC ---
        else if (isBow) {
            if (ench.equals(Enchantment.ARROW_DAMAGE)) { // Power / Puissance
                return main.getBowPowerMax();
            } else if (ench.equals(Enchantment.ARROW_FIRE)) { // Flame / Flamme
                return main.getBowFlameMax();
            } else if (ench.equals(Enchantment.ARROW_KNOCKBACK)) { // Punch / Frappe
                return main.getBowPunchMax();
            }
        }

        // Par défaut, retourne le niveau d'enchantement vanilla maximum
        return ench.getMaxLevel();
    }
}