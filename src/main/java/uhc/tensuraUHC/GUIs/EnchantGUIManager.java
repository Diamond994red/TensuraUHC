package uhc.tensuraUHC.GUIs;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class EnchantGUIManager {

    public static final String GUI_TITLE = ChatColor.DARK_GRAY + "Enchanter l'item en main";

    public static void openEnchantMenu(Player player) {
        ItemStack heldItem = player.getItemInHand();
        if (heldItem == null || heldItem.getType() == Material.AIR) {
            player.sendMessage(ChatColor.RED + "Vous devez tenir un objet en main pour l'enchanter !");
            return;
        }

        Inventory gui = Bukkit.createInventory(null, 27, GUI_TITLE);

        // Enchantements Prioritaires
        gui.setItem(10, createEnchantItem(Material.ENCHANTED_BOOK, ChatColor.RED + "Tranchant (Sharpness)", Enchantment.DAMAGE_ALL, 5));
        gui.setItem(11, createEnchantItem(Material.ENCHANTED_BOOK, ChatColor.BLUE + "Protection", Enchantment.PROTECTION_ENVIRONMENTAL, 4));
        gui.setItem(12, createEnchantItem(Material.ENCHANTED_BOOK, ChatColor.GOLD + "Puissance (Power)", Enchantment.ARROW_DAMAGE, 5));
        gui.setItem(13, createEnchantItem(Material.ENCHANTED_BOOK, ChatColor.GREEN + "Solidité (Unbreaking)", Enchantment.DURABILITY, 3));

        // Enchantements Secondaires Très Utiles
        gui.setItem(14, createEnchantItem(Material.ENCHANTED_BOOK, ChatColor.AQUA + "Frappe (Punch)", Enchantment.ARROW_KNOCKBACK, 2));
        gui.setItem(15, createEnchantItem(Material.ENCHANTED_BOOK, ChatColor.DARK_PURPLE + "Recul (Knockback)", Enchantment.KNOCKBACK, 2));
        gui.setItem(16, createEnchantItem(Material.ENCHANTED_BOOK, ChatColor.YELLOW + "Efficacité (Efficiency)", Enchantment.DIG_SPEED, 5));

        // Fermer / Annuler
        gui.setItem(26, createEnchantItem(Material.BARRIER, ChatColor.RED + "Fermer", null, 0));

        player.openInventory(gui);
    }

    private static ItemStack createEnchantItem(Material mat, String name, Enchantment enchant, int maxLevel) {
        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(name);
            List<String> lore = new ArrayList<>();
            if (enchant != null) {
                lore.add(ChatColor.GRAY + "Clic Gauche : +1 Niveau");
                lore.add(ChatColor.GRAY + "Clic Droit : Retirer l'enchantement");
                lore.add(ChatColor.DARK_GRAY + "Niveau Max : " + maxLevel);
            }
            meta.setLore(lore);
            item.setItemMeta(meta);
        }
        return item;
    }
}