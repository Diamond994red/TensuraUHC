package uhc.tensuraUHC.managers;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import uhc.tensuraUHC.TensuraUHC;
import uhc.tensuraUHC.scenarios.Scenario;
import uhc.tensuraUHC.scenarios.list.*;

import java.util.*;

public class ScenarioManager {

    private final TensuraUHC main;
    private final Map<String, Scenario> scenarios = new LinkedHashMap<>();
    public static final String GUI_TITLE = ChatColor.DARK_GRAY + "Configuration des Scénarios";

    public ScenarioManager(TensuraUHC main) {
        this.main = main;
        registerScenarios();
    }

    private void registerScenarios() {
        addScenario(new CutCleanScenario(main));
        addScenario(new CatEyesScenario(main));
        addScenario(new MasterLevelScenario(main));
        addScenario(new OreMagnetScenario(main));
        addScenario(new SafeMinerScenario(main));
        addScenario(new BetaZombieScenario(main));
    }
    private void addScenario(Scenario scenario) {
        scenarios.put(scenario.getName().toLowerCase(), scenario);
        Bukkit.getPluginManager().registerEvents(scenario, main);
    }

    public Scenario getScenario(String name) {
        return scenarios.get(name.toLowerCase());
    }

    public Collection<Scenario> getScenarios() {
        return scenarios.values();
    }

    // ==========================================
    // GUI DES SCÉNARIOS
    // ==========================================
    public void openScenarioMenu(Player player) {
        Inventory gui = Bukkit.createInventory(null, 27, GUI_TITLE);

        int slot = 0;
        for (Scenario scenario : scenarios.values()) {
            ItemStack item = new ItemStack(scenario.getIcon());
            ItemMeta meta = item.getItemMeta();

            String status = scenario.isEnabled() ? ChatColor.GREEN + "✔ Activé" : ChatColor.RED + "✖ Désactivé";
            meta.setDisplayName((scenario.isEnabled() ? ChatColor.GREEN : ChatColor.RED) + scenario.getName());

            List<String> lore = new ArrayList<>();
            lore.add(ChatColor.GRAY + scenario.getDescription());
            lore.add("");
            lore.add(ChatColor.GRAY + "Statut : " + status);
            lore.add(ChatColor.YELLOW + "Cliquez pour basculer.");
            meta.setLore(lore);

            item.setItemMeta(meta);
            gui.setItem(slot++, item);
        }

        // Bouton Retour
        ItemStack back = new ItemStack(Material.BARRIER);
        ItemMeta backMeta = back.getItemMeta();
        backMeta.setDisplayName(ChatColor.RED + "Retour");
        back.setItemMeta(backMeta);
        gui.setItem(26, back);

        player.openInventory(gui);
    }
}