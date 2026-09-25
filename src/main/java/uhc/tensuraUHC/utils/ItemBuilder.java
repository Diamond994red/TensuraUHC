package uhc.tensuraUHC.utils;

import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ItemBuilder {

    private final ItemStack item;
    private final ItemMeta meta;

    // Constructeur à partir d'un Material
    public ItemBuilder(Material material) {
        this(material, 1);
    }

    // Constructeur avec quantité
    public ItemBuilder(Material material, int amount) {
        this.item = new ItemStack(material, amount);
        this.meta = item.getItemMeta();
    }

    // Définit le nom de l'item
    public ItemBuilder setName(String name) {
        meta.setDisplayName(name);
        return this;
    }

    // Ajoute plusieurs lignes de Lore (description)
    public ItemBuilder setLore(String... lore) {
        meta.setLore(Arrays.asList(lore));
        return this;
    }

    // Ajoute une liste de Lore
    public ItemBuilder setLore(List<String> lore) {
        meta.setLore(lore);
        return this;
    }

    // Ajoute une ligne de Lore à la suite des préexistantes
    public ItemBuilder addLoreLine(String line) {
        List<String> lore = meta.hasLore() ? meta.getLore() : new ArrayList<>();
        lore.add(line);
        meta.setLore(lore);
        return this;
    }

    // Ajoute un enchantement
    public ItemBuilder addEnchantment(Enchantment enchantment, int level) {
        meta.addEnchant(enchantment, level, true);
        return this;
    }

    // Rend l'item incassable
    public ItemBuilder setUnbreakable(boolean unbreakable) {
        meta.spigot().setUnbreakable(unbreakable);
        return this;
    }

    // Cache les attributs (dégâts d'attaque, armure, etc.) ou les enchantements
    public ItemBuilder hideFlags(ItemFlag... flags) {
        meta.addItemFlags(flags);
        return this;
    }

    // Colore une armure en cuir (ex: armure de rôle Tensura)
    public ItemBuilder setArmorColor(Color color) {
        if (meta instanceof LeatherArmorMeta) {
            ((LeatherArmorMeta) meta).setColor(color);
        }
        return this;
    }

    // Définit la tête de joueur d'un joueur spécifique
    public ItemBuilder setSkullOwner(String owner) {
        if (meta instanceof SkullMeta) {
            ((SkullMeta) meta).setOwner(owner);
        }
        return this;
    }

    // Construit et retourne l'ItemStack final
    public ItemStack build() {
        item.setItemMeta(meta);
        return item;
    }
}