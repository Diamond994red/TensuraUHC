package uhc.tensuraUHC.listeners;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import uhc.tensuraUHC.TensuraUHC;
import uhc.tensuraUHC.commands.PlayerCommand;

public class RulesClickListener implements Listener {

    private final TensuraUHC main;

    public RulesClickListener(TensuraUHC main) {
        this.main = main;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        String title = event.getView().getTitle();

        // Bloquer la prise d'items et gérer les clics dans les GUI de règles
        if (title.equals(PlayerCommand.RULES_GUI_TITLE)) {
            event.setCancelled(true);

            ItemStack current = event.getCurrentItem();
            if (current == null || !current.hasItemMeta()) return;

            Player player = (Player) event.getWhoClicked();

            // Clic sur Restrictions (Slot 14)
            if (event.getSlot() == 14) {
                PlayerCommand.openRestrictionsMenu(player, main);
            }
        } else if (title.equals(PlayerCommand.RESTRICTIONS_GUI_TITLE)) {
            event.setCancelled(true);

            ItemStack current = event.getCurrentItem();
            if (current == null || !current.hasItemMeta()) return;

            Player player = (Player) event.getWhoClicked();

            // Clic sur Retour (Slot 26)
            if (event.getSlot() == 26) {
                player.performCommand("rule");
            }
        } else if (title.equals(PlayerCommand.INV_VIEWER_TITLE)) {
            // Sécurité : empêche aussi de voler les items du Starter Kit
            event.setCancelled(true);
        }
    }
}