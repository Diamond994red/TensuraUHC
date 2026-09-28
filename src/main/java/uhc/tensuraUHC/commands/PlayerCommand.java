package uhc.tensuraUHC.commands;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import uhc.tensuraUHC.TensuraUHC;

import java.util.ArrayList;
import java.util.List;

public class PlayerCommand implements CommandExecutor {

    private final TensuraUHC main;
    public static final String INV_VIEWER_TITLE = ChatColor.DARK_GRAY + "Visualisation : Starter Kit";
    public static final String RULES_GUI_TITLE = ChatColor.DARK_GRAY + "Règles & Configurations";
    public static final String RESTRICTIONS_GUI_TITLE = ChatColor.DARK_RED + "Restrictions d'Équipement";

    public PlayerCommand(TensuraUHC main) {
        this.main = main;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        String cmd = command.getName();

        // ==========================================
        // COMMANDE /INV
        // ==========================================
        if (cmd.equalsIgnoreCase("inv") || cmd.equalsIgnoreCase("starterkit")) {
            if (!(sender instanceof Player)) {
                sender.sendMessage("Seul un joueur peut exécuter cette commande.");
                return true;
            }

            Player player = (Player) sender;
            ItemStack[] kit = main.getStarterKit();
            ItemStack[] armor = main.getStarterArmor();

            if ((kit == null || isInventoryEmpty(kit)) && (armor == null || isInventoryEmpty(armor))) {
                player.sendMessage(ChatColor.GOLD + "[TensuraUHC] " + ChatColor.RED + "Aucun inventaire de départ n'a été défini par l'Host.");
                return true;
            }

            Inventory previewGui = Bukkit.createInventory(null, 45, INV_VIEWER_TITLE);

            if (kit != null) {
                for (int i = 0; i < kit.length; i++) {
                    if (kit[i] != null && kit[i].getType() != Material.AIR) {
                        previewGui.setItem(i, kit[i].clone());
                    }
                }
            }

            if (armor != null) {
                if (armor.length > 0 && armor[0] != null) previewGui.setItem(36, armor[0].clone());
                if (armor.length > 1 && armor[1] != null) previewGui.setItem(37, armor[1].clone());
                if (armor.length > 2 && armor[2] != null) previewGui.setItem(38, armor[2].clone());
                if (armor.length > 3 && armor[3] != null) previewGui.setItem(39, armor[3].clone());
            }

            player.openInventory(previewGui);
            return true;
        }

        // ==========================================
// COMMANDE /HELPOP
// ==========================================
        if (cmd.equalsIgnoreCase("helpop")) {
            if (!(sender instanceof Player)) {
                sender.sendMessage("Seul un joueur peut exécuter cette commande.");
                return true;
            }

            Player player = (Player) sender;

            if (args.length == 0) {
                player.sendMessage(ChatColor.RED + "Usage: /helpop <message>");
                return true;
            }

            StringBuilder message = new StringBuilder();
            for (String arg : args) {
                message.append(arg).append(" ");
            }

            // Création du ticket numéroté
            int ticketId = main.getHelpOpManager().createTicket(player);

            player.sendMessage(ChatColor.DARK_BLUE + "[HELPOP #" + ticketId + "]" + ChatColor.BLUE + " Le message a bien été envoyé à l'host.");

            // Notification aux Hosts avec le numéro du ticket
            for (Player pHost : main.getServer().getOnlinePlayers()) {
                if (main.isHostOrCoHost(pHost)) {
                    pHost.sendMessage(ChatColor.DARK_BLUE + "[HELPOP #" + ticketId + "] "
                            + ChatColor.AQUA + player.getName() + " : "
                            + ChatColor.BLUE + message.toString().trim());
                }
            }
            return true;
        }

        // ==========================================
        // COMMANDE /RULES & /RULE
        // ==========================================
        if (cmd.equalsIgnoreCase("rule") || cmd.equalsIgnoreCase("rules")) {
            if (!(sender instanceof Player)) {
                sender.sendMessage("Seul un joueur peut exécuter cette commande.");
                return true;
            }

            Player player = (Player) sender;
            Inventory rulesGui = Bukkit.createInventory(null, 27, RULES_GUI_TITLE);

            // Item 1 : Timers (Montre)
            List<String> timerLore = new ArrayList<>();
            timerLore.add(ChatColor.GRAY + "• PvP : " + ChatColor.WHITE + formatTime(main.GetPvpTime()));
            timerLore.add(ChatColor.GRAY + "• MeetUp : " + ChatColor.WHITE + formatTime(main.getMeetupTime()));
            timerLore.add(ChatColor.GRAY + "• FinalHeal : " + ChatColor.WHITE + formatTime(main.GetFinalHealTime()));
            rulesGui.setItem(10, createRuleItem(Material.WATCH, ChatColor.GOLD + "Timers de la partie", timerLore));

            // Item 2 : Limites & Borne (Boussole)
            List<String> limitLore = new ArrayList<>();
            limitLore.add(ChatColor.GRAY + "• Taille de la carte : " + ChatColor.WHITE + main.getBorderInitialSize() + "x" + main.getBorderInitialSize());
            limitLore.add(ChatColor.GRAY + "• Taille finale : " + ChatColor.WHITE + main.getBorderFinalSize() + "x" + main.getBorderFinalSize());
            limitLore.add(ChatColor.GRAY + "• Vitesse bordure : " + ChatColor.WHITE + (main.getBorderInitialSize() - main.getBorderFinalSize()) / main.getBorderShrinkDuration() + " bloc/s");
            rulesGui.setItem(12, createRuleItem(Material.COMPASS, ChatColor.YELLOW + "Limites de la Carte", limitLore));

            // Item 3 : Restrictions
            List<String> restrictionLore = new ArrayList<>();
            restrictionLore.add(ChatColor.GRAY + "Cliquez pour voir l'ensemble des restrictions");
            rulesGui.setItem(14, createRuleItem(Material.DIAMOND_CHESTPLATE, ChatColor.RED + "Restrictions", restrictionLore));

            // Item 4 : Minerais & Grottes (Pioche en Diamant)
            List<String> miningLore = new ArrayList<>();
            miningLore.add(ChatColor.GRAY + "• Taux de Diamants : " + ChatColor.AQUA + main.getDiamondPercent() + "%");
            miningLore.add(ChatColor.GRAY + "• Taux de Redstone : " + ChatColor.RED + main.getRedstonePercent() + "%");
            miningLore.add(ChatColor.GRAY + "• Taux de Lapis : " + ChatColor.BLUE + main.getLapisPercent() + "%");
            miningLore.add(ChatColor.GRAY + "• Taux d'Or : " + ChatColor.GOLD + main.getGoldPercent() + "%");
            miningLore.add(ChatColor.GRAY + "• Taux de Fer : " + ChatColor.WHITE + main.getIronPercent() + "%");
            miningLore.add(ChatColor.GRAY + "• Taux de Charbon : " + ChatColor.GRAY + main.getCoalPercent() + "%");
            miningLore.add(ChatColor.GRAY + "• Taux d'émeraude : " + ChatColor.GRAY + main.getEmeraldPercent() + "%");
            miningLore.add(ChatColor.GRAY + "• Taux d'exp : " + ChatColor.GRAY + main.getXpPercent() + "%");
            miningLore.add(ChatColor.GRAY + "• Densité des grottes : " + ChatColor.GREEN + main.getCaveSizePercent() + "%");
            rulesGui.setItem(16, createRuleItem(Material.DIAMOND_PICKAXE, ChatColor.AQUA + "Grottes & Minerais", miningLore));

            player.openInventory(rulesGui);
            return true;
        }

        return false;
    }

    // Méthode pour ouvrir le sous-menu des restrictions
    public static void openRestrictionsMenu(Player player, TensuraUHC main) {
        Inventory gui = Bukkit.createInventory(null, 27, RESTRICTIONS_GUI_TITLE);

        // Item Diamant (Pièces & Limite Minage) - Slot 10
        List<String> diamondLore = new ArrayList<>();
        diamondLore.add(ChatColor.GRAY + "• Pièces max autorisées : " + ChatColor.AQUA + main.getMaxDiamondArmorPieces());
        diamondLore.add(ChatColor.GRAY + "• Diamants minables max : " + ChatColor.AQUA + main.getMaxMinedDiamonds());
        gui.setItem(10, createRuleItem(Material.DIAMOND, ChatColor.AQUA + "Limites Diamant", diamondLore));

        // Item Enchants Diamant - Slot 12
        List<String> diamondEnchantLore = new ArrayList<>();
        diamondEnchantLore.add(ChatColor.GRAY + "• Sharpness max : " + ChatColor.WHITE + main.getDiamondSharpnessMax());
        diamondEnchantLore.add(ChatColor.GRAY + "• Protection max : " + ChatColor.WHITE + main.getDiamondProtectionMax());
        diamondEnchantLore.add(ChatColor.GRAY + "• Fire Aspect max : " + ChatColor.WHITE + main.getDiamondFireMax());
        diamondEnchantLore.add(ChatColor.GRAY + "• Knockback max : " + ChatColor.WHITE + main.getDiamondKnockbackMax());
        gui.setItem(12, createRuleItem(Material.DIAMOND_SWORD, ChatColor.AQUA + "Enchants Diamant Max", diamondEnchantLore));

        // Item Enchants Fer - Slot 14
        List<String> ironEnchantLore = new ArrayList<>();
        ironEnchantLore.add(ChatColor.GRAY + "• Sharpness max : " + ChatColor.WHITE + main.getIronSharpnessMax());
        ironEnchantLore.add(ChatColor.GRAY + "• Protection max : " + ChatColor.WHITE + main.getIronProtectionMax());
        ironEnchantLore.add(ChatColor.GRAY + "• Fire Aspect max : " + ChatColor.WHITE + main.getIronFireMax());
        ironEnchantLore.add(ChatColor.GRAY + "• Knockback max : " + ChatColor.WHITE + main.getIronKnockbackMax());
        gui.setItem(14, createRuleItem(Material.IRON_SWORD, ChatColor.WHITE + "Enchants Fer Max", ironEnchantLore));

        // Item Enchants Arc - Slot 16 (NOUVEAU)
        List<String> bowEnchantLore = new ArrayList<>();
        bowEnchantLore.add(ChatColor.GRAY + "• Power max : " + ChatColor.WHITE + main.getBowPowerMax());
        bowEnchantLore.add(ChatColor.GRAY + "• Flame max : " + ChatColor.WHITE + main.getBowFlameMax());
        bowEnchantLore.add(ChatColor.GRAY + "• Punch max : " + ChatColor.WHITE + main.getBowPunchMax());
        gui.setItem(16, createRuleItem(Material.BOW, ChatColor.GOLD + "Enchants Arc Max", bowEnchantLore));

        // Bouton Retour - Slot 26
        List<String> backLore = new ArrayList<>();
        backLore.add(ChatColor.GRAY + "Revenir au menu principal des règles");
        gui.setItem(26, createRuleItem(Material.ARROW, ChatColor.RED + "Retour", backLore));

        player.openInventory(gui);
    }

    // Méthode utilitaire pour créer des items de menu avec lore
    public static ItemStack createRuleItem(Material material, String name, List<String> lore) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(name);
            meta.setLore(lore);
            item.setItemMeta(meta);
        }
        return item;
    }

    // Méthode utilitaire pour vérifier si le kit est vide
    private boolean isInventoryEmpty(ItemStack[] items) {
        if (items == null) return true;
        for (ItemStack item : items) {
            if (item != null && item.getType() != Material.AIR) {
                return false;
            }
        }
        return true;
    }
    private String formatTime(int totalSeconds) {
        int minutes = totalSeconds / 60;
        int seconds = totalSeconds % 60;
        return (seconds > 0) ? String.format("%d min %d s", minutes, seconds) : minutes + " min";
    }
}