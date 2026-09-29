package uhc.tensuraUHC.managers;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.scheduler.BukkitRunnable;
import uhc.tensuraUHC.TensuraUHC;
import uhc.tensuraUHC.roles.Role;
import uhc.tensuraUHC.scenarios.Scenario;
import uhc.tensuraUHC.scenarios.list.MasterLevelScenario;

import java.util.*;

public class GameManager {

    private final TensuraUHC main;
    private BukkitRunnable gameTask;
    private int currentEpisode = 1;
    private int secondsInEpisode = 0;
    private int totalGameSeconds = 0;
    private boolean pvpActive = false;
    private boolean roleRevealed = false;
    private boolean newEp = false;
    public void SetRolesRevealed(boolean b) { this.roleRevealed = b; }
    public boolean GetRolesRevealed() { return roleRevealed; }
    public int GetTotalGameSeconds() { return totalGameSeconds; }
    public int GetTotalAlivePlayers() {
        int alivePlayer = 0;
        if (!GetRolesRevealed()) {
            alivePlayer = Bukkit.getOnlinePlayers().size();
        }
        else {
            for (UUID player : GetActivePlayers())
            {
                if (main.getRoleManager().hasRole(player))
                {
                    alivePlayer++;
                }
            }
        }
        return alivePlayer;
    }
    List<UUID> activePlayers = new ArrayList<>();
    private void SetActivePlayers(List<UUID> players)
    {
        activePlayers = players;
    }
    public List<UUID> GetActivePlayers()
    {
        return activePlayers;
    }
    public void DeleteActivePlayer(UUID player)
    {
        activePlayers.remove(player);
    }
    public GameManager(TensuraUHC main) {
        this.main = main;
    }

    public void startGame() {
        if (main.getGameWorld() == null) {
            main.createGameWorld();
        }
        main.resetMinedDiamonds();
        World world = main.getGameWorld();

        // Utilisation de BorderManager au lancement de la partie
        main.getBorderManager().setupInitialBorder(world);

        main.setGameStarted(true);
        this.currentEpisode = 1;
        this.secondsInEpisode = 0;
        this.totalGameSeconds = 0;
        this.pvpActive = false;

        // Récupération dynamique des scénarios au lancement
        Scenario masterLevel = main.getScenarioManager().getScenario("MasterLevel");
        Scenario catEyes = main.getScenarioManager().getScenario("CatEyes");

        for (Player p : Bukkit.getOnlinePlayers()) {
            p.getInventory().clear();
            p.setGameMode(GameMode.SURVIVAL);
            p.setMaxHealth(20.0);
            p.setHealth(20.0);
            p.setFoodLevel(20);

            // Supprime proprement tous les effets de potion actifs du joueur
            for (PotionEffect effect : p.getActivePotionEffects()) {
                p.removePotionEffect(effect.getType());
            }

            int y = world.getHighestBlockYAt(0, 0) + 2;
            p.teleport(new Location(world, 0, y, 0));

            main.getNoDamagePlayers().add(p.getUniqueId());
            main.getNoFallPlayer().clear();
            p.getInventory().setArmorContents(null);

            if (main.getStarterKit() != null) {
                p.getInventory().setContents(main.getStarterKit());
            }
            if (main.getStarterArmor() != null) {
                p.getInventory().setArmorContents(main.getStarterArmor());
            }

            // Application du scénario MasterLevel au start
            if (masterLevel != null && masterLevel.isEnabled()) {
                ((MasterLevelScenario) masterLevel).applyToAll();
            }

            p.updateInventory();
        }

        // Activation de CatEyes pour tous les joueurs au start
        if (catEyes != null && catEyes.isEnabled()) {
            catEyes.setEnabled(true);
        }

        Bukkit.broadcastMessage(ChatColor.GOLD + "=================================");
        Bukkit.broadcastMessage(ChatColor.GREEN + "   La partie " + main.getGameName() + " commence !");
        Bukkit.broadcastMessage(ChatColor.YELLOW + "   Épisode 1 - Début du minage");
        Bukkit.broadcastMessage(ChatColor.GOLD + "=================================");

        startLoop();
    }

    private void startLoop() {
        if (gameTask != null) gameTask.cancel();
        activePlayers.clear();
        main.getRoleManager().getPlayerRoles().clear();
        main.getRoleManager().getcurrentPlayerRoles().clear();
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (player.getGameMode() == GameMode.SURVIVAL || player.getGameMode() == GameMode.ADVENTURE || player.getGameMode() == GameMode.CREATIVE) {
                activePlayers.add(player.getUniqueId());
                if (main.getRoleManager().hasRole(player.getUniqueId())) {
                    main.getRoleManager().getPlayerRole(player.getUniqueId()).reset(player.getUniqueId());
                }
            }
        }
        gameTask = new BukkitRunnable() {
            @Override
            public void run() {
                if (!main.isGameStarted()) {
                    cancel();
                    return;
                }
                if (main.getRoleManager().getAliveCampsCount(activePlayers) <= 1 && totalGameSeconds >= main.GetRoleTime())
                {
                    WinGame(main.getRoleManager().FinalCamp());
                }
                secondsInEpisode++;
                totalGameSeconds++;
                if (totalGameSeconds == 60) {
                    main.getNoDamagePlayers().clear();
                    Bukkit.broadcastMessage(ChatColor.RED + "[TensuraUHC] Les dégâts sont désormais ACTIFS !");
                }

                if (totalGameSeconds == main.GetPvpTime() && !pvpActive) {
                    pvpActive = true;
                    Bukkit.broadcastMessage(ChatColor.RED + "[TensuraUHC] Le PvP est désormais ACTIF !");
                }

                if (totalGameSeconds == main.GetFinalHealTime()) {
                    for (Player player : Bukkit.getOnlinePlayers()) {
                        player.setHealth(player.getMaxHealth());
                    }
                    Bukkit.broadcastMessage(ChatColor.GOLD + "[TensuraUHC] " + ChatColor.GREEN + "Final Heal ! Tous les joueurs ont été soignés !");
                }
                if (newEp) {
                    newEp = false;
                }

                if (totalGameSeconds == main.GetRoleTime()) {

                    // Distribution des rôles aux joueurs en jeu
                    main.getRoleManager().distributeRoles(activePlayers);
                    SetRolesRevealed(true);
                }

                // Déclenchement du Meetup et lancement de la bordure via BorderManager
                if (totalGameSeconds == main.getMeetupTime() && !main.isMeetupActive()) {
                    main.setMeetupActive(true);
                    Bukkit.broadcastMessage(ChatColor.GOLD + "[TensuraUHC] " + ChatColor.RED + "Le Meetup est actif !");
                    main.getBorderManager().startBorderShrink(main.getGameWorld());
                }

                if (secondsInEpisode >= main.getEpisodeLengthSeconds()) {
                    secondsInEpisode = 0;
                    currentEpisode++;
                    newEp = true;
                    Bukkit.broadcastMessage(ChatColor.GOLD + "================-================");
                    Bukkit.broadcastMessage(ChatColor.YELLOW + "   Début de l'Épisode " + currentEpisode);
                    Bukkit.broadcastMessage(ChatColor.GOLD + "=================================");
                }

                for (Player p : Bukkit.getOnlinePlayers()) {
                    main.getScoreboardManager().updateGameScoreboard(p);
                }
            }
        };

        gameTask.runTaskTimer(main, 20L, 20L);
    }

    public void stopGame(boolean finished) {
        if (!finished) {
            if (gameTask != null) {
                gameTask.cancel();
                gameTask = null;
            }
        }
        for (Role role : main.getRoleManager().getRoles()) {
            role.reset(null);
        }

        // Nettoie la vie et les effets de TOUS les joueurs connectés
        for (Player player : Bukkit.getOnlinePlayers()) {
            player.setMaxHealth(20.0);
            player.setHealth(20.0);
            for (PotionEffect effect : player.getActivePotionEffects()) {
                player.removePotionEffect(effect.getType());
            }
        }

        // Vide complètement les rôles assignés
        main.getRoleManager().getPlayerRoles().clear();
        main.getRoleManager().getcurrentPlayerRoles().clear();

        main.setGameStarted(false);
        Bukkit.broadcastMessage(ChatColor.RED + "[TensuraUHC] La partie et tous les pouvoirs ont été réinitialisés.");
    }

    public void WinGame(Role.Camp finalCamp) {
        if (gameTask != null) {
            gameTask.cancel();
            gameTask = null;
        }
        Role role = null;
        for (UUID player : main.getGameManager().GetActivePlayers()) {
            role = main.getRoleManager().getPlayerRole(player);
        }
        Map<Role.Camp, String> messagesVictoire = new EnumMap<>(Role.Camp.class);
        messagesVictoire.put(Role.Camp.LIMULE, "de Limule et ses alliés !");
        messagesVictoire.put(Role.Camp.MONSTRES, "des Monstres !");
        messagesVictoire.put(Role.Camp.HUMAINS, "des Humains !");
        messagesVictoire.put(Role.Camp.SHIZUE, "de Shizue et Limule !");
        messagesVictoire.put(Role.Camp.OCTAGRAMME, "de l'Octagramme !");
        messagesVictoire.put(Role.Camp.CLOWNS, "des Clowns !");
        if (role != null) {
            messagesVictoire.put(Role.Camp.SOLITAIRE, "de " + role.getName() + " !");
        }
        String message = main.getRoleManager().getCampMessage(finalCamp, messagesVictoire);
        Bukkit.broadcastMessage(ChatColor.GOLD + "[TensuraUHC]" + ChatColor.GREEN +" Victoire " + message);
        stopGame(true);
    }

    public int getCurrentEpisode() { return currentEpisode; }
    public int getSecondsInEpisode() { return secondsInEpisode; }
    public int getTotalGameSeconds() { return totalGameSeconds; }
    public boolean isPvpActive() { return pvpActive; }
    public boolean IsNewEp() {return newEp; }
}