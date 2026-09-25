package uhc.tensuraUHC.scenarios.list;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;
import uhc.tensuraUHC.TensuraUHC;
import uhc.tensuraUHC.scenarios.Scenario;

public class CutCleanScenario extends Scenario {

    public CutCleanScenario(TensuraUHC main) {
        super(main, "CutClean", Material.IRON_INGOT, "Les minerais et la nourriture sont automatiquement cuits.");
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        if (!isEnabled()) return;

        // Si OreMagnet est aussi activé, c'est lui qui gère le cassage et le drop direct
        Scenario oreMagnet = main.getScenarioManager().getScenario("OreMagnet");
        if (oreMagnet != null && oreMagnet.isEnabled()) return;

        Block block = event.getBlock();
        Material type = block.getType();

        if (type == Material.IRON_ORE) {
            event.setCancelled(true);
            block.setType(Material.AIR);
            block.getWorld().dropItemNaturally(block.getLocation(), new ItemStack(Material.IRON_INGOT));
        } else if (type == Material.GOLD_ORE) {
            event.setCancelled(true);
            block.setType(Material.AIR);
            block.getWorld().dropItemNaturally(block.getLocation(), new ItemStack(Material.GOLD_INGOT));
        }
    }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);

        // Désactive OreMagnet si CutClean passe à false
        if (!enabled) {
            Scenario oreMagnet = main.getScenarioManager().getScenario("OreMagnet");
            if (oreMagnet != null && oreMagnet.isEnabled()) {
                oreMagnet.setEnabled(false);
            }
        }
    }
}