package uhc.tensuraUHC.listeners;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Enderman;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.ItemStack;
import uhc.tensuraUHC.TensuraUHC;

import java.util.Random;

public class DropListener implements Listener {

    private final TensuraUHC main;
    private final Random random = new Random();

    public DropListener(TensuraUHC main) {
        this.main = main;
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onBlockBreak(BlockBreakEvent event) {
        if (event.isCancelled()) return;

        Block block = event.getBlock();
        Material type = block.getType();

        // 1. GESTION DES POMMES (Feuilles d'arbres - LEAVES et LEAVES_2)
        if (type == Material.LEAVES || type == Material.LEAVES_2) {
            Player player = event.getPlayer();
            ItemStack heldItem = player.getItemInHand();
            boolean isUsingShears = heldItem != null && heldItem.getType() == Material.SHEARS;

            // Annule l'événement Vanilla pour contrôler à 100% les drops
            event.setCancelled(true);
            block.setType(Material.AIR);

            // Si le joueur utilise une cisaille, on lui donne le bloc de feuille
            if (isUsingShears) {
                block.getWorld().dropItemNaturally(block.getLocation().add(0.5, 0.5, 0.5), new ItemStack(type, 1, block.getData()));
            }

            // Gestion du drop de Pomme selon le % configuré
            int chance = main.getAppleDropPercent();
            if (chance >= 100 || (chance > 0 && random.nextInt(100) < chance)) {
                block.getWorld().dropItemNaturally(block.getLocation().add(0.5, 0.5, 0.5), new ItemStack(Material.APPLE, 1));
            }
        }

        // 2. GESTION DU SILEX (Gravier)
        else if (type == Material.GRAVEL) {
            event.setCancelled(true);
            block.setType(Material.AIR);

            int chance = main.getFlintDropPercent();
            if (chance >= 100 || (chance > 0 && random.nextInt(100) < chance)) {
                block.getWorld().dropItemNaturally(block.getLocation().add(0.5, 0.5, 0.5), new ItemStack(Material.FLINT, 1));
            } else {
                block.getWorld().dropItemNaturally(block.getLocation().add(0.5, 0.5, 0.5), new ItemStack(Material.GRAVEL, 1));
            }
        }
    }

    // 3. GESTION DES ENDER PEARLS
    @EventHandler
    public void onEntityDeath(EntityDeathEvent event) {
        if (event.getEntity() instanceof Enderman) {
            event.getDrops().clear();

            int chance = main.getEnderPearlDropPercent();
            if (chance >= 100 || (chance > 0 && random.nextInt(100) < chance)) {
                event.getDrops().add(new ItemStack(Material.ENDER_PEARL, 1));
            }
        }
    }
}