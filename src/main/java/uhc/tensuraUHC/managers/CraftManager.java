package uhc.tensuraUHC.managers;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.ShapelessRecipe;
import uhc.tensuraUHC.TensuraUHC;

public class CraftManager {

    private final TensuraUHC main;

    public CraftManager(TensuraUHC main) {
        this.main = main;
    }

    public void registerCrafts() {
        registerDiamondToGoldCraft();
        registerWoolToStringCraft();
    }

    // 1. Craft : 1 Diamant -> 1 Lingot d'or (Sans forme)
    private void registerDiamondToGoldCraft() {
        ShapelessRecipe recipe = new ShapelessRecipe(new ItemStack(Material.GOLD_INGOT, 1));
        recipe.addIngredient(Material.DIAMOND);

        Bukkit.addRecipe(recipe);
    }

    // 2. Craft : 4 Laines -> 1 Ficelle (Carré 2x2)
    private void registerWoolToStringCraft() {
        ShapedRecipe recipe = new ShapedRecipe(new ItemStack(Material.STRING, 1));

        recipe.shape("WW", "WW");
        recipe.setIngredient('W', Material.WOOL); // Fonctionne pour toutes les couleurs de laine

        Bukkit.addRecipe(recipe);
    }
}