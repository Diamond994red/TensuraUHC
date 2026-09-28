package uhc.tensuraUHC.managers;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import uhc.tensuraUHC.TensuraUHC;
import uhc.tensuraUHC.roles.Role;
import uhc.tensuraUHC.roles.list.ClownsCamp.*;
import uhc.tensuraUHC.roles.list.HumansCamp.*;
import uhc.tensuraUHC.roles.list.MonstersCamp.*;
import uhc.tensuraUHC.roles.list.OctagramCamp.*;
import uhc.tensuraUHC.roles.list.SoloCamp.*;

import java.util.*;

public class RoleManager {

    private final TensuraUHC main;
    private final List<Role> roles = new ArrayList<>();
    private final Map<UUID, Role> playerRoles = new HashMap<>();
    private final Map<UUID, Role> currentPlayerRoles = new HashMap<>();

    public RoleManager(TensuraUHC main) {
        this.main = main;
        registerRoles();
    }

    /**
     * Enregistre tous les rôles du plugin et leurs événements.
     */
    private void registerRoles() {
        addRole(new ShizuRole(main));
        addRole(new YuukiRole(main));
        addRole(new LimuleRole(main));
        addRole(new SoeiRole((main)));
    }

    /**
     * Ajoute un rôle et enregistre ses Listener Bukkit.
     */
    public void addRole(Role role) {
        roles.add(role);
        Bukkit.getPluginManager().registerEvents(role, main);
    }

    /**
     * Distribue aléatoirement les rôles configurés aux joueurs.
     */
    public void distributeRoles(List<Player> players) {
        if (players.isEmpty() || roles.isEmpty()) return;

        // 1. Construit le pool de rôles actifs selon le nombre d'exemplaires défini
        List<Role> activePool = new ArrayList<>();
        for (Role role : roles) {
            for (int i = 0; i < role.getCount(); i++) {
                activePool.add(role);
            }
        }

        // Si aucun rôle n'a été sélectionné dans le GUI (count = 0 partout)
        if (activePool.isEmpty()) {
            Bukkit.broadcastMessage("§c[TensuraUHC] Aucun rôle n'a été activé dans la composition !");
            return;
        }

        // 2. Mélange aléatoire des rôles et des joueurs
        Collections.shuffle(activePool);
        Collections.shuffle(players);

        // 3. Distribution aux joueurs
        for (int i = 0; i < players.size(); i++) {
            // Reboucle sur la liste si le nombre de joueurs est supérieur au nombre de rôles configurés
            Role roleToGive = activePool.get(i % activePool.size());
            assignRole(players.get(i), roleToGive);
            currentPlayerRoles.put(players.get(i).getUniqueId(), roleToGive);
            if (roleToGive instanceof YuukiRole)
            {
                players.get(i).setMaxHealth(26.0);
            }
            else
            {
                players.get(i).setMaxHealth(20.0);
            }
        }
    }

    /**
     * Assigne un rôle spécifique à un joueur.
     */
    public void assignRole(Player player, Role role) {
        playerRoles.put(player.getUniqueId(), role);
        role.giveRole(player);
    }

    /**
     * Retire le rôle d'un joueur.
     */
    public void removeRole(Player player) {
        playerRoles.remove(player.getUniqueId());
    }

    /**
     * Récupère le rôle d'un joueur.
     */
    public Role getPlayerRole(Player player) {
        return playerRoles.get(player.getUniqueId());
    }

    public int getAliveCampsCount(List<Player> players) {
        int aliveCampsCount = 0;

        for (Player player : players) {
            if (player != null && player.isOnline() && !player.isDead()) {
                Role role = main.getRoleManager().getPlayerRole(player);
                if (role != null && role.getCamp() != null && role.getCamp() != Role.Camp.SOLITAIRE) {
                    aliveCampsCount++;
                }
            }
        }
        for (Player player : players) {
            if (player != null && player.isOnline() && !player.isDead()) {
                Role role = main.getRoleManager().getPlayerRole(player);
                if (role != null && role.getCamp() == Role.Camp.SOLITAIRE) {
                    aliveCampsCount++;
                }
            }
        }

        return aliveCampsCount;
    }

    public boolean hasRole(Player player, Role role) {
        Role currentRole = getPlayerRole(player);
        return currentRole != null && currentRole.equals(role);
    }
    public boolean hasRole(Player player)
    {
        return getPlayerRole(player) != null;
    }

    public String getCampMessage(Role.Camp camp, Map<Role.Camp, String> messages) {
        if (camp == null || messages == null) return "";
        // Retourne le message correspondant au camp, ou une chaîne vide par défaut
        return messages.getOrDefault(camp, "");
    }

    public Role getRoleByName(String name) {
        for (Role role : roles) {
            if (role.getName().equalsIgnoreCase(name)) {
                return role;
            }
        }
        return null;
    }

    public List<Role> getRoles() {
        return roles;
    }

    public Map<UUID, Role> getPlayerRoles() {
        return playerRoles;
    }

    public Role.Camp FinalCamp() {
        Role.Camp aliveCamp = null;
        for (Player player : Bukkit.getOnlinePlayers())
        {
            if (!player.isDead())
            {
                aliveCamp = main.getRoleManager().getPlayerRole(player).getCamp();
                break;
            }
        }
        return aliveCamp;
    }

    public Map<UUID, Role> getcurrentPlayerRoles() {
        return currentPlayerRoles;
    }
}