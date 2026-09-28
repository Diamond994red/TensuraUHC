package uhc.tensuraUHC.commands;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import uhc.tensuraUHC.TensuraUHC;
import uhc.tensuraUHC.roles.Role;
import uhc.tensuraUHC.roles.list.SoloCamp.YuukiRole;

import java.util.ArrayList;
import java.util.List;

public class TensuraCommand implements CommandExecutor {

    private final TensuraUHC main;

    public TensuraCommand(TensuraUHC main) {
        this.main = main;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "Seul un joueur peut exécuter cette commande.");
            return true;
        }

        Player player = (Player) sender;

        if (args.length == 0) {
            sendHelp(player);
            return true;
        }

        String subCommand = args[0].toLowerCase();
        Role playerRole = main.getRoleManager().getPlayerRole(player.getUniqueId());


        switch (subCommand) {
            case "role":
            case "me":
                handleRoleCommand(player);
                break;
            case "choisir":
            case "choose":
                handleChoisirCommand(player, args);
                break;

            case "claim":
                handleClaimCommand(player);
                break;
            case "help":
            case "?":
                sendHelp(player);
                break;
            case "color":
                handleColorCommand(player, args);
                break;
            default:
                player.sendMessage(ChatColor.RED + "Sous-commande inconnue. Tapez /tr help pour voir la liste complète.");
                break;
        }

        return true;
    }
    private void handleColorCommand(Player sender, String[] args) {
        // Si aucun argument : /tr color -> Ouvre le GUI des têtes de joueurs
        if (args.length == 1) {
            main.getGuiManager().openPlayerSelectionMenu(sender);
            return;
        }

        // Sinon : /tr color <joueur1> [joueur2] ...
        List<Player> targets = new ArrayList<>();
        for (int i = 1; i < args.length; i++) {
            Player target = Bukkit.getPlayer(args[i]);
            if (target != null) {
                targets.add(target);
            } else {
                sender.sendMessage(ChatColor.RED + "Joueur introuvable : " + args[i]);
            }
        }

        if (targets.isEmpty()) {
            sender.sendMessage(ChatColor.RED + "Aucun joueur valide spécifié.");
            return;
        }

        main.getGuiManager().openColorPickerMenu(sender, targets);
    }
    // --- /tr role ---
    private void handleRoleCommand(Player player) {
        if (!main.isGameStarted()) {
            player.sendMessage(ChatColor.RED + "La partie n'a pas encore commencé !");
            return;
        }

        Role role = main.getRoleManager().getPlayerRole(player.getUniqueId());

        if (role == null) {
            player.sendMessage(ChatColor.RED + "Vous n'avez aucun rôle attribué.");
            return;
        }

        // Ré-affiche le message complet du rôle au joueur
        role.GetRoleDescription(player);
    }
    // --- /tr choisir <nomDuRole> ---
    private void handleChoisirCommand(Player player, String[] args) {
        Role playerRole = main.getRoleManager().getPlayerRole(player.getUniqueId());

        if (!(playerRole instanceof YuukiRole)) {
            player.sendMessage(ChatColor.RED + "Vous ne pouvez pas exécuter cette commande.");
            return;
        }

        YuukiRole yuuki = (YuukiRole) playerRole;

        if (yuuki.getFakeRoleName() != null) {
            player.sendMessage(ChatColor.RED + "Vous avez déjà choisi votre rôle de façade (" + yuuki.getFakeRoleName() + ") !");
            return;
        }

        // Ouverture du GUI de sélection
        main.getGuiManager().openYuukiFacadeMenu(player);
    }

    // --- /tr claim ---
    private void handleClaimCommand(Player player) {
        Role playerRole = main.getRoleManager().getPlayerRole(player.getUniqueId());

        if (!(playerRole instanceof YuukiRole)) {
            player.sendMessage(ChatColor.RED + "Vous ne pouvez pas exécuter cette commande.");
            return;
        }

        YuukiRole yuuki = (YuukiRole) playerRole;
        List<ItemStack> powers = yuuki.getClaimedPowers();

        if (powers.isEmpty()) {
            player.sendMessage(ChatColor.RED + "Vous n'avez aucun pouvoir ou item à réclamer pour le moment.");
            return;
        }

        // Donne tous les items récupérés au joueur
        for (ItemStack item : powers) {
            player.getInventory().addItem(item);
        }

        player.sendMessage(ChatColor.GOLD + "[Yuuki - Mammon] " + ChatColor.GREEN + "Vous avez récupéré " + powers.size() + " item(s)/pouvoir(s) de vos victimes !");
        powers.clear(); // Vide la réserve après récupération
    }
    // --- /tr help ---
    private void sendHelp(Player player) {
        player.sendMessage(ChatColor.GOLD + "================= [ Tensura UHC ] =================");
        player.sendMessage(ChatColor.YELLOW + "/tr role " + ChatColor.GRAY + "- Affiche les informations de votre rôle et vos pouvoirs.");
        player.sendMessage(ChatColor.YELLOW + "/tr color [joueur]... " + ChatColor.GRAY + "- Colore le pseudo d'un joueur (visible uniquement par vous).");
        player.sendMessage(ChatColor.YELLOW + "/tr help " + ChatColor.GRAY + "- Affiche ce menu d'aide.");
        player.sendMessage(ChatColor.GOLD + "==================================================");
    }
}