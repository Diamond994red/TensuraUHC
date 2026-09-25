package uhc.tensuraUHC.scenarios.list;

import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.ExperienceOrb;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;
import uhc.tensuraUHC.TensuraUHC;
import uhc.tensuraUHC.scenarios.Scenario;

import java.util.Collection;
import java.util.Random;

public class OreMagnetScenario extends Scenario {

    private final Random random = new Random();

    public OreMagnetScenario(TensuraUHC main) {
        super(main, "OreMagnet", Material.HOPPER, "Téléporte directement les minerais cassés dans l'inventaire.");
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onBlockBreak(BlockBreakEvent event) {
        if (!isEnabled() || event.isCancelled()) return;

        Player player = event.getPlayer();
        if (player.getGameMode() == GameMode.CREATIVE) return;

        Block block = event.getBlock();
        Material type = block.getType();

        if (!isOre(type)) return;

        Scenario cutClean = main.getScenarioManager().getScenario("CutClean");
        boolean isCutCleanActive = cutClean != null && cutClean.isEnabled();

        // Récupération de l'XP que le bloc devait donner de base
        int expToDrop = getOreExperience(type);

        event.setCancelled(true);
        block.setType(Material.AIR);

        if (isCutCleanActive) {
            ItemStack drop = getSmeltedDrop(type);
            if (drop != null) {
                player.getInventory().addItem(drop);
            }
        } else {
            Collection<ItemStack> drops = block.getDrops(player.getItemInHand());
            for (ItemStack drop : drops) {
                player.getInventory().addItem(drop);
            }
        }

        // Give d'XP au joueur s'il y en a
        if (expToDrop > 0) {
            ExperienceOrb orb = (ExperienceOrb) block.getWorld().spawnEntity(player.getLocation(), EntityType.EXPERIENCE_ORB);
            orb.setExperience(expToDrop);
        }
    }

    private int getOreExperience(Material type) {
        switch (type) {
            case COAL_ORE:
                return random.nextInt(3); // 0 à 2 XP
            case DIAMOND_ORE:
            case EMERALD_ORE:
                return 3 + random.nextInt(5); // 3 à 7 XP
            case LAPIS_ORE:
            case REDSTONE_ORE:
            case GLOWING_REDSTONE_ORE:
                return 2 + random.nextInt(4); // 2 à 5 XP
            case IRON_ORE:
            case GOLD_ORE:
                // Donne 1 XP (comme quand on cuit au four sous CutClean)
                return 1;
            default:
                return 0;
        }
    }

    private ItemStack getSmeltedDrop(Material type) {
        if (type == Material.IRON_ORE) {
            return new ItemStack(Material.IRON_INGOT, 1);
        } else if (type == Material.GOLD_ORE) {
            return new ItemStack(Material.GOLD_INGOT, 1);
        } else if (type == Material.DIAMOND_ORE) {
            return new ItemStack(Material.DIAMOND, 1);
        } else if (type == Material.COAL_ORE) {
            return new ItemStack(Material.COAL, 1);
        } else if (type == Material.LAPIS_ORE) {
            return new ItemStack(Material.INK_SACK, 4, (short) 4);
        } else if (type == Material.REDSTONE_ORE || type == Material.GLOWING_REDSTONE_ORE) {
            return new ItemStack(Material.REDSTONE, 4);
        } else if (type == Material.EMERALD_ORE) {
            return new ItemStack(Material.EMERALD, 1);
        }
        return null;
    }

    private boolean isOre(Material material) {
        return material == Material.COAL_ORE
                || material == Material.IRON_ORE
                || material == Material.GOLD_ORE
                || material == Material.DIAMOND_ORE
                || material == Material.REDSTONE_ORE
                || material == Material.GLOWING_REDSTONE_ORE
                || material == Material.LAPIS_ORE
                || material == Material.EMERALD_ORE;
    }
}