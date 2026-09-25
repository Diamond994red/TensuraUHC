package uhc.tensuraUHC.commands;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import uhc.tensuraUHC.TensuraUHC;

public class LobbyCommand implements CommandExecutor {

    private final TensuraUHC main;

    public LobbyCommand(TensuraUHC main) {
        this.main = main;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        Player player = (Player) sender;
        if (!(sender instanceof Player)) {
            if (!main.isHostOrCoHost(player)) {
                player.sendMessage(ChatColor.RED + "Seuls les Hosts et Co-Hosts peuvent utiliser cette commande.");
                return true;
            }
        }

        if (main.isGameStarted()) {
            sender.sendMessage(ChatColor.RED + "Une partie est en cours.");
            return true;
        }
        Location spawnLoc = new Location(Bukkit.getWorld("world"), 0, 202, 0);

        player.teleport(spawnLoc);

        if (!main.isGameStarted()) {
            player.setGameMode(GameMode.ADVENTURE);
            main.giveMenuItem(player);
        }

        player.sendMessage(ChatColor.GREEN + "[TensuraUHC] Vous avez été téléporté au Lobby.");
        return true;
    }
}