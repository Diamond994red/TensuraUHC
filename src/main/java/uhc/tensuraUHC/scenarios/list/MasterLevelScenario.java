package uhc.tensuraUHC.scenarios.list;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;
import uhc.tensuraUHC.TensuraUHC;
import uhc.tensuraUHC.scenarios.Scenario;

public class MasterLevelScenario extends Scenario {

    public MasterLevelScenario(TensuraUHC main) {
        super(main, "MasterLevel", Material.ANVIL, "Donne 10 000 niveaux d'XP et 64 enclume à tous les joueurs.");
    }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        if (enabled && main.isGameStarted()) {
            applyToAll();
        }
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        if (!isEnabled()) return;
        giveMasterLevel(event.getPlayer());
    }

    public void applyToAll() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            giveMasterLevel(player);
        }
    }

    private void giveMasterLevel(Player player) {
        player.setLevel(10000);

        if (!player.getInventory().contains(Material.ANVIL)) {
            player.getInventory().addItem(new ItemStack(Material.ANVIL, 64));
        }
    }
}