package uhc.tensuraUHC.commands;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import uhc.tensuraUHC.TensuraUHC;

import java.util.UUID;

public class HostCommand implements CommandExecutor {

    private final TensuraUHC main;

    public HostCommand(TensuraUHC main) {
        this.main = main;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        // ==========================================
        // 1. COMMANDE /HOST <joueur>
        // ==========================================
        if (label.equalsIgnoreCase("host")) {
            if (sender instanceof Player && !((Player) sender).isOp()) {
                sender.sendMessage(ChatColor.RED + "Vous n'avez pas la permission d'exécuter cette commande.");
                return true;
            }

            if (args.length != 1) {
                sender.sendMessage(ChatColor.RED + "Utilisation : /host <joueur>");
                return true;
            }

            Player target = Bukkit.getPlayer(args[0]);
            if (target == null) {
                sender.sendMessage(ChatColor.RED + "Joueur introuvable.");
                return true;
            }

            main.setHostUUID(target.getUniqueId());
            main.updatePlayerPrefix(target);

            Bukkit.broadcastMessage(ChatColor.GOLD + "[TensuraUHC] " + ChatColor.GREEN + target.getName() + " est désormais l'Host de la partie !");
            return true;
        }

        // ==========================================
        // 2. COMMANDE /COHOST <add|remove> <joueur>
        // ==========================================
        if (label.equalsIgnoreCase("cohost")) {
            if (sender instanceof Player) {
                Player player = (Player) sender;
                if (!main.isHost(player) && !player.isOp()) {
                    player.sendMessage(ChatColor.RED + "Seul l'Host ou un OP peut utiliser cette commande.");
                    return true;
                }
            }

            if (args.length != 2) {
                sender.sendMessage(ChatColor.RED + "Utilisation : /cohost <add|remove> <joueur>");
                return true;
            }

            Player target = Bukkit.getPlayer(args[1]);
            if (target == null) {
                sender.sendMessage(ChatColor.RED + "Joueur introuvable.");
                return true;
            }

            if (args[0].equalsIgnoreCase("add")) {
                if (main.getCoHostUUIDs().contains(target.getUniqueId())) {
                    sender.sendMessage(ChatColor.RED + target.getName() + " est déjà Co-Host.");
                    return true;
                }
                main.getCoHostUUIDs().add(target.getUniqueId());
                main.updatePlayerPrefix(target);
                sender.sendMessage(ChatColor.GREEN + target.getName() + " est maintenant Co-Host.");
                target.sendMessage(ChatColor.GOLD + "[TensuraUHC] Vous avez été nommé Co-Host.");
            } else if (args[0].equalsIgnoreCase("remove")) {
                if (!main.getCoHostUUIDs().contains(target.getUniqueId())) {
                    sender.sendMessage(ChatColor.RED + target.getName() + " n'est pas Co-Host.");
                    return true;
                }
                main.getCoHostUUIDs().remove(target.getUniqueId());
                main.updatePlayerPrefix(target);
                sender.sendMessage(ChatColor.YELLOW + target.getName() + " n'est plus Co-Host.");
                target.sendMessage(ChatColor.RED + "[TensuraUHC] Vous n'êtes plus Co-Host.");
            } else {
                sender.sendMessage(ChatColor.RED + "Sous-commande invalide. Utilisez 'add' ou 'remove'.");
            }
            return true;
        }
        // ==========================================
// COMMANDE /REPLY <id> <message> ou /RHELPOP
// ==========================================
        if (label.equalsIgnoreCase("reply") || label.equalsIgnoreCase("rhelpop")) {
            if (sender instanceof Player) {
                Player player = (Player) sender;
                if (!main.isHostOrCoHost(player)) {
                    player.sendMessage(ChatColor.RED + "Seuls les Hosts et Co-Hosts peuvent utiliser cette commande.");
                    return true;
                }
            }

            if (args.length < 2) {
                sender.sendMessage(ChatColor.RED + "Utilisation : /" + label + " <numéro> <message>");
                return true;
            }

            try {
                int ticketId = Integer.parseInt(args[0]);
                UUID targetUUID = main.getHelpOpManager().getSenderUUID(ticketId);

                if (targetUUID == null) {
                    sender.sendMessage(ChatColor.RED + "Aucun ticket d'aide trouvé avec le numéro #" + ticketId);
                    return true;
                }

                Player target = Bukkit.getPlayer(targetUUID);
                if (target == null || !target.isOnline()) {
                    sender.sendMessage(ChatColor.RED + "Le joueur ayant ouvert le ticket #" + ticketId + " n'est plus en ligne.");
                    return true;
                }

                // Assemblage du message de réponse
                StringBuilder response = new StringBuilder();
                for (int i = 1; i < args.length; i++) {
                    response.append(args[i]).append(" ");
                }

                String senderName = (sender instanceof Player) ? sender.getName() : "Console";

                // Message au joueur
                target.sendMessage(ChatColor.DARK_BLUE + "[HELPOP #" + ticketId + "] "
                        + ChatColor.GOLD + senderName + " (Host) : "
                        + ChatColor.YELLOW + response.toString().trim());

                // Confirmation à l'Host
                sender.sendMessage(ChatColor.GREEN + "Réponse envoyée au ticket #" + ticketId + " (" + target.getName() + ").");

            } catch (NumberFormatException e) {
                sender.sendMessage(ChatColor.RED + "Le numéro de ticket doit être un entier valide.");
            }
            return true;
        }

        // ==========================================
        // 3. COMMANDE /REVIVE <joueur>
        // ==========================================
        if (label.equalsIgnoreCase("revive")) {
            if (sender instanceof Player) {
                Player player = (Player) sender;
                if (!main.isHostOrCoHost(player)) {
                    player.sendMessage(ChatColor.RED + "Seuls les Hosts et Co-Hosts peuvent utiliser cette commande.");
                    return true;
                }
            }

            if (args.length != 1) {
                sender.sendMessage(ChatColor.RED + "Utilisation : /revive <joueur>");
                return true;
            }

            Player target = Bukkit.getPlayer(args[0]);
            if (target == null) {
                sender.sendMessage(ChatColor.RED + "Joueur introuvable.");
                return true;
            }

            if (!main.isGameStarted()) {
                sender.sendMessage(ChatColor.RED + "La partie n'a pas encore commencé.");
                return true;
            }

            // Remet le joueur en SURVIVAL et le téléporte sur la carte UHC
            main.getRoleManager().assignRole(target, main.getRoleManager().getcurrentPlayerRoles().get(target.getUniqueId()));
            target.setGameMode(GameMode.SURVIVAL);
            int y = main.getGameWorld().getHighestBlockYAt(0, 0) + 2;
            target.teleport(new Location(main.getGameWorld(), 0, y, 0));

            // Protection contre les dégâts de chute temporaires
            main.getNoDamagePlayers().add(target.getUniqueId());
            Bukkit.getScheduler().runTaskLater(main, () -> main.getNoDamagePlayers().remove(target.getUniqueId()), 60L);

            Bukkit.broadcastMessage(ChatColor.GOLD + "[TensuraUHC] " + ChatColor.GREEN + target.getName() + " a été ressuscité par l'Host !");
            return true;
        }

        // ==========================================
        // 4. COMMANDE /STOPUHC
        // ==========================================
        if (label.equalsIgnoreCase("stopuhc")) {
            if (sender instanceof Player) {
                Player player = (Player) sender;
                if (!main.isHostOrCoHost(player)) {
                    player.sendMessage(ChatColor.RED + "Seuls les Hosts et Co-Hosts peuvent utiliser cette commande.");
                    return true;
                }
            }

            if (!main.isGameStarted()) {
                sender.sendMessage(ChatColor.RED + "Aucune partie n'est en cours.");
                return true;
            }

            // Arrêt de la boucle de jeu

            Location lobbyLoc = new Location(Bukkit.getWorld("world"), 0, 202, 0);

            // Téléportation et réinitialisation de tous les joueurs au Lobby
            for (Player p : Bukkit.getOnlinePlayers()) {
                p.teleport(lobbyLoc);
                p.setGameMode(GameMode.ADVENTURE);
                p.getInventory().clear();
                p.setHealth(20.0);
                p.setFoodLevel(20);
                main.giveMenuItem(p);
                main.getScoreboardManager().updateLobbyScoreboard(p);
                main.getGameManager().stopGame(false);
            }

            Bukkit.broadcastMessage(ChatColor.RED + "=================================");
            Bukkit.broadcastMessage(ChatColor.RED + "   La partie a été interrompue !");
            Bukkit.broadcastMessage(ChatColor.YELLOW + "   Tous les joueurs ont été téléportés au Lobby.");
            Bukkit.broadcastMessage(ChatColor.RED + "=================================");

            return true;
        }

        // ==========================================
        // 5. COMMANDE /SAY
        // ==========================================
        if (label.equalsIgnoreCase("say")) {
            if (sender instanceof Player) {
                Player player = (Player) sender;
                if (!main.isHostOrCoHost(player)) {
                    player.sendMessage(ChatColor.RED + "Seuls les Hosts et Co-Hosts peuvent utiliser cette commande.");
                    return true;
                }

                if (args.length == 0) {
                    player.sendMessage(ChatColor.RED + "Usage: /say <message>");
                    return true;
                }

                StringBuilder message = new StringBuilder();
                for (String arg : args) {
                    message.append(arg).append(" ");
                }

                String prefix = player.getUniqueId().equals(main.getHostUUID()) ? "[Host] " : "[Co-Host] ";
                Bukkit.broadcastMessage(ChatColor.RED + "\n" + ChatColor.BOLD + prefix + player.getName() + " : " + ChatColor.YELLOW + message.toString().trim() + "\n ");
                return true;
            }
            return true;
        }

        // ==========================================
        // 6. COMMANDE /SAVEINV
        // ==========================================
        if (label.equalsIgnoreCase("saveinv")) {
            if (!(sender instanceof Player)) {
                sender.sendMessage("Seul un joueur peut exécuter cette commande.");
                return true;
            }

            Player player = (Player) sender;

            if (!main.isHostOrCoHost(player)) {
                player.sendMessage(ChatColor.RED + "Seuls les Hosts et Co-Hosts peuvent exécuter cette commande.");
                return true;
            }

            // VÉRIFICATION : Est-ce que le joueur est en train de créer le kit ?
            if (!main.isEditingStarterKit()) {
                player.sendMessage(ChatColor.RED + "Vous devez passer par le menu (Paramètres -> Inventaire de Départ) pour modifier le kit.");
                return true;
            }

            // Sauvegarde avec copie
            ItemStack[] contents = player.getInventory().getContents();
            ItemStack[] armor = player.getInventory().getArmorContents();

            ItemStack[] savedContents = new ItemStack[contents.length];
            for (int i = 0; i < contents.length; i++) {
                if (contents[i] != null) savedContents[i] = contents[i].clone();
            }

            ItemStack[] savedArmor = new ItemStack[armor.length];
            for (int i = 0; i < armor.length; i++) {
                if (armor[i] != null) savedArmor[i] = armor[i].clone();
            }

            main.setStarterKit(savedContents);
            main.setStarterArmor(savedArmor);
            main.setEditingStarterKit(false); // <--- DESACTIVATION DU MODE ÉDITION

            // Remise en état du joueur
            player.getInventory().clear();
            player.getInventory().setArmorContents(null);
            player.setGameMode(GameMode.ADVENTURE);

            main.giveMenuItem(player);

            player.sendMessage(ChatColor.GOLD + "[TensuraUHC] " + ChatColor.GREEN + "L'inventaire de départ a été sauvegardé avec succès !");
            return true;
        }
        // ==========================================
        // 7. COMMANDE /ENCHANT (Seulement pendant l'édition de kit)
        // ==========================================
        if (label.equalsIgnoreCase("enchant")) {
            if (!(sender instanceof Player)) {
                sender.sendMessage("Seul un joueur peut exécuter cette commande.");
                return true;
            }

            Player player = (Player) sender;

            if (!main.isHostOrCoHost(player)) {
                player.sendMessage(ChatColor.RED + "Seuls les Hosts et Co-Hosts peuvent exécuter cette commande.");
                return true;
            }

            if (!main.isEditingStarterKit()) {
                player.sendMessage(ChatColor.RED + "La commande /enchant n'est disponible que pendant la création de l'inventaire de départ.");
                return true;
            }

            uhc.tensuraUHC.GUIs.EnchantGUIManager.openEnchantMenu(player);
            return true;
        }

        // ==========================================
        // 8. COMMANDE /SETGROUP <nombre>
        // ==========================================
        if (label.equalsIgnoreCase("setgroup")) {
            if (sender instanceof Player) {
                Player player = (Player) sender;
                if (!main.isHostOrCoHost(player)) {
                    player.sendMessage(ChatColor.RED + "Seuls les Hosts et Co-Hosts peuvent utiliser cette commande.");
                    return true;
                }
            }

            if (args.length != 1) {
                sender.sendMessage(ChatColor.RED + "Utilisation : /setgroup <taille>");
                return true;
            }

            try {
                int groupSize = Integer.parseInt(args[0]);
                if (groupSize <= 0) {
                    sender.sendMessage(ChatColor.RED + "La taille du groupe doit être supérieure à 0.");
                    return true;
                }

                main.setGroupSize(groupSize);


                // Envoi du Title et Subtitle à tous les joueurs en ligne
                String titleText = ChatColor.GOLD + "" + ChatColor.BOLD + "GROUPES DE " + groupSize;
                String subtitleText = ChatColor.YELLOW + "Respectez la taille des groupes";

                // On applique les titres à tous les joueurs
                for (Player p : Bukkit.getOnlinePlayers()) {
                    p.sendTitle(titleText, subtitleText);
                }

            } catch (NumberFormatException e) {
                sender.sendMessage(ChatColor.RED + "Veuillez entrer un nombre valide.");
            }
            return true;
        }
        // ==========================================
        // 9. COMMANDE /giveall
        // ==========================================
        if (label.equalsIgnoreCase("giveall")) {
            if (sender instanceof Player && !((Player) sender).isOp()) {
                sender.sendMessage(ChatColor.RED + "Vous n'avez pas la permission d'exécuter cette commande.");
                return true;
            }

            if (!(sender instanceof Player)) {
                sender.sendMessage(ChatColor.RED + "Seul un joueur peut exécuter cette commande.");
                return true;
            }

            Player player = (Player) sender;
            main.getGuiManager().openGiveAllMenu(player, 1); // Quantité initiale : 1
            return true;
        }
        return false;
    }
}