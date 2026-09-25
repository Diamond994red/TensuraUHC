package uhc.tensuraUHC.managers;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.*;
import uhc.tensuraUHC.TensuraUHC;

public class ScoreboardManager {

    private final TensuraUHC main;

    public ScoreboardManager(TensuraUHC main) {
        this.main = main;
    }

    // Scoreboard dans le Lobby
    public void updateLobbyScoreboard(Player player) {
        Scoreboard board = getOrCreateScoreboard(player);
        Objective obj = getOrCreateObjective(board, "lobby", ChatColor.GOLD + "" + ChatColor.BOLD + main.getGameName());

        // Nettoyage des anciens scores pour éviter la superposition lors des updates
        for (String entry : board.getEntries()) {
            if (obj.getScore(entry).isScoreSet()) {
                board.resetScores(entry);
            }
        }

        obj.getScore(ChatColor.GRAY + "------------------").setScore(6);
        obj.getScore(ChatColor.YELLOW + "Joueurs : " + ChatColor.WHITE + Bukkit.getOnlinePlayers().size()).setScore(5);
        obj.getScore(ChatColor.YELLOW + "Host : " + ChatColor.WHITE + getHostName()).setScore(4);
        obj.getScore(ChatColor.GRAY + " ").setScore(3);
        obj.getScore(ChatColor.GREEN + "En attente du début...").setScore(2);
        obj.getScore(ChatColor.GRAY + "-------------------").setScore(1);
    }

    // Scoreboard pendant la partie UHC
    public void updateGameScoreboard(Player player) {
        Scoreboard board = getOrCreateScoreboard(player);
        Objective obj = getOrCreateObjective(board, "game", ChatColor.GOLD + "" + ChatColor.BOLD + main.getGameName());

        // Nettoyage des anciens scores dynamiques
        for (String entry : board.getEntries()) {
            if (obj.getScore(entry).isScoreSet()) {
                board.resetScores(entry);
            }
        }

        int remainingSeconds = main.getGameManager().getTotalGameSeconds();
        String timeFormatted = String.format("%02d:%02d", remainingSeconds / 60, remainingSeconds % 60);
        int kills = main.getKills().getOrDefault(player.getUniqueId(), 0);

        obj.getScore(ChatColor.GRAY + "------------------").setScore(9);
        obj.getScore(ChatColor.YELLOW + "Épisode : " + ChatColor.GREEN + main.getGameManager().getCurrentEpisode()).setScore(8);
        obj.getScore(ChatColor.YELLOW + "Timer : " + ChatColor.WHITE + timeFormatted).setScore(7);
        obj.getScore(ChatColor.YELLOW + "Groupe : " + ChatColor.RED + main.getGroupSize()).setScore(6);
        obj.getScore(ChatColor.YELLOW + "Cycle : " + (main.isDay() ? ChatColor.GOLD + "Jour" : ChatColor.DARK_BLUE + "Nuit")).setScore(5);
        obj.getScore(ChatColor.YELLOW + "Kills : " + ChatColor.RED + kills).setScore(4);
        obj.getScore(ChatColor.YELLOW + "PvP : " + (main.getGameManager().isPvpActive() ? ChatColor.GREEN + "Oui" : ChatColor.RED + "Non")).setScore(3);
        obj.getScore(ChatColor.YELLOW + "Joueurs : " + ChatColor.WHITE + main.getGameManager().GetTotalAlivePlayers()).setScore(2);
        obj.getScore(ChatColor.GRAY + "-------------------").setScore(1);
    }

    // Récupère le scoreboard actuel du joueur ou lui en assigne un unique S'IL N'EN A PAS
    private Scoreboard getOrCreateScoreboard(Player player) {
        Scoreboard board = player.getScoreboard();
        if (board == Bukkit.getScoreboardManager().getMainScoreboard()) {
            board = Bukkit.getScoreboardManager().getNewScoreboard();
            player.setScoreboard(board);
        }
        return board;
    }

    // Récupère ou crée l'Objective Sidebar sur le scoreboard du joueur
    private Objective getOrCreateObjective(Scoreboard board, String name, String displayName) {
        Objective obj = board.getObjective(name);
        if (obj == null) {
            // Unregister l'ancien objective si on change de mode (ex: lobby vers game)
            Objective oldObj = board.getObjective(DisplaySlot.SIDEBAR);
            if (oldObj != null) {
                oldObj.unregister();
            }
            obj = board.registerNewObjective(name, "dummy");
            obj.setDisplaySlot(DisplaySlot.SIDEBAR);
        }
        obj.setDisplayName(displayName);
        return obj;
    }

    private String getHostName() {
        if (main.getHostUUID() == null) return "Aucun";
        Player host = Bukkit.getPlayer(main.getHostUUID());
        return (host != null) ? host.getName() : "Hors-ligne";
    }
}