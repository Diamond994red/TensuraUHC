package uhc.tensuraUHC.commands;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import uhc.tensuraUHC.TensuraUHC;
import uhc.tensuraUHC.roles.Role;
import uhc.tensuraUHC.roles.list.LimuleRole;

public class testCommand implements CommandExecutor {

    private final TensuraUHC main;

    public testCommand(TensuraUHC main) {
        this.main = main;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("Seul un joueur peut utiliser cette commande.");
            return true;
        }

        Player player = (Player) sender;
        Role role = main.getRoleManager().getPlayerRole(player);

        if (!(role instanceof LimuleRole)) {
            player.sendMessage(ChatColor.RED + "Seul Limule peut exécuter cette commande de test !");
            return true;
        }

        LimuleRole limule = (LimuleRole) role;
        limule.addTestDeathLocation(player.getLocation());

        player.sendMessage(ChatColor.GREEN + "[Test] Emplacement de mort ajouté à votre position (X: "
                + player.getLocation().getBlockX() + ", Y: "
                + player.getLocation().getBlockY() + ", Z: "
                + player.getLocation().getBlockZ() + ").");
        player.sendMessage(ChatColor.YELLOW + "Utilisez votre Nether Star 'Prédateur' pour l'absorber !");

        return true;
    }
}