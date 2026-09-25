package uhc.tensuraUHC;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import uhc.tensuraUHC.GUIs.GUIManager;
import uhc.tensuraUHC.commands.*;
import uhc.tensuraUHC.listeners.*;
import uhc.tensuraUHC.managers.*;
import uhc.tensuraUHC.managers.RoleManager;
import uhc.tensuraUHC.utils.ItemBuilder;

import java.util.*;

public class TensuraUHC extends JavaPlugin {

    // --- Managers ---
    private BorderManager borderManager;
    private GUIManager guiManager;
    private GameManager gameManager;
    private WorldManager worldManager;
    private ScoreboardManager scoreboardManager;
    private RoleManager roleManager;
    private ScenarioManager scenarioManager;
    // --- Variables de Configuration ---
    private String gameName = "Tensura UHC";
    private int episodeLengthSeconds = 1200; // 20 minutes par défaut
    private int RoleTime = 1200;
    private int FinalHealTime = 1170;
    private int PvpTime = 1200;
    private boolean awaitingGameNameInput = false;
    private boolean gameStarted = false;

    // --- Configuration du Monde et des Minerais ---
    private int caveSizePercent = 100;
    private int diamondPercent = 100;
    private int goldPercent = 100;
    private int ironPercent = 100;
    private int redstonePercent = 100;
    private int lapisPercent = 100;
    private int coalPercent = 100;
    private int emeraldPercent = 100;
    private int xpPercent = 100;

    // --- Gestion des Rôles & Sécurité ---
    private UUID hostUUID;
    private final List<UUID> coHostUUIDs = new ArrayList<>();
    private final List<UUID> noDamagePlayers = new ArrayList<>();
    private final List<UUID> noFallPlayers = new ArrayList<>();
    private final Map<UUID, Integer> kills = new HashMap<>();

    @Override
    public void onEnable() {
        // 1. Initialisation des Managers
        this.borderManager = new BorderManager(this);
        this.guiManager = new GUIManager(this);
        this.gameManager = new GameManager(this);
        this.worldManager = new WorldManager(this);
        this.scoreboardManager = new ScoreboardManager(this);
        this.roleManager = new RoleManager(this);
        this.scenarioManager = new ScenarioManager(this);
        // 2. Enregistrement des Listeners
        getServer().getPluginManager().registerEvents(new PlayerListener(this), this);
        getServer().getPluginManager().registerEvents(new InventoryListener(this), this);
        getServer().getPluginManager().registerEvents(new WorldListener(this), this);
        getServer().getPluginManager().registerEvents(new GameListener(this), this);
        getServer().getPluginManager().registerEvents(new ItemListener(this), this);
        getServer().getPluginManager().registerEvents(new BlockBreakListener(this), this);
        getServer().getPluginManager().registerEvents(new DeathListener(this),this);
        getServer().getPluginManager().registerEvents(new DamageListener(this),this);
        getServer().getPluginManager().registerEvents(new ItemRestrictionListener(this),this);
        getServer().getPluginManager().registerEvents(new ArmorRestrictionListener(this), this);
        getServer().getPluginManager().registerEvents(new RulesClickListener(this), this);
        getServer().getPluginManager().registerEvents(new EnchantRestrictionListener(this), this);
        getServer().getPluginManager().registerEvents(new DropListener(this), this);
        // 3. Enregistrement des Commandes
        HostCommand hostCmd = new HostCommand(this);
        getCommand("host").setExecutor(hostCmd);
        getCommand("cohost").setExecutor(hostCmd);
        getCommand("revive").setExecutor(hostCmd);
        getCommand("stopuhc").setExecutor(hostCmd);
        getCommand("lobby").setExecutor(new LobbyCommand(this));
        getCommand("say").setExecutor(hostCmd);
        getCommand("saveinv").setExecutor(new HostCommand(this));
        getCommand("enchant").setExecutor(new HostCommand(this));
        getCommand("setgroup").setExecutor(new HostCommand(this));
        getCommand("giveall").setExecutor(new HostCommand(this));
        PlayerCommand playerCommand = new PlayerCommand(this);
        getCommand("inv").setExecutor(playerCommand);
        getCommand("helpop").setExecutor(playerCommand);
        getCommand("rule").setExecutor(playerCommand);
        getCommand("tr").setExecutor(new TensuraCommand(this));
        getCommand("testdeath").setExecutor(new testCommand(this));
        CraftManager craftManager = new CraftManager(this);
        craftManager.registerCrafts();

        saveDefaultConfig();
        loadConfiguration();

        getLogger().info("TensuraUHC a ete active avec succes !");
    }

    @Override
    public void onDisable() {
        saveConfiguration();
        getLogger().info("TensuraUHC a ete desactive.");
    }

    // --- Méthodes Utilitaires ---

    /**
     * Donne l'étoile du Nether "Menu" dans le slot 4 si le joueur est Host ou Co-Host.
     */
    public void giveMenuItem(Player player) {
        if (isHostOrCoHost(player)) {
            ItemStack menuStar = new ItemBuilder(Material.NETHER_STAR)
                    .setName(ChatColor.GREEN + "Menu")
                    .setLore(ChatColor.GRAY + "Clic droit pour ouvrir le menu de configuration.")
                    .build();
            player.getInventory().setItem(4, menuStar);
        } else {
            player.getInventory().setItem(4, null);
        }
    }

    /**
     * Met à jour le préfixe [HOST] ou [CO-HOST] du joueur sur le Scoreboard général et le Tablist.
     */
    public void updatePlayerPrefix(Player player) {
        Scoreboard board = Bukkit.getScoreboardManager().getMainScoreboard();

        Team hostTeam = board.getTeam("001Host");
        if (hostTeam == null) {
            hostTeam = board.registerNewTeam("001Host");
            hostTeam.setPrefix(ChatColor.DARK_RED + "[HOST] " + ChatColor.RESET);
        }

        Team coHostTeam = board.getTeam("002CoHost");
        if (coHostTeam == null) {
            coHostTeam = board.registerNewTeam("002CoHost");
            coHostTeam.setPrefix(ChatColor.RED + "[CO-HOST] " + ChatColor.RESET);
        }

        Team playerTeam = board.getTeam("003Player");
        if (playerTeam == null) {
            playerTeam = board.registerNewTeam("003Player");
            playerTeam.setPrefix(ChatColor.GRAY + "");
        }

        // Retrait des équipes précédentes
        hostTeam.removeEntry(player.getName());
        coHostTeam.removeEntry(player.getName());
        playerTeam.removeEntry(player.getName());

        if (isHost(player)) {
            hostTeam.addEntry(player.getName());
        } else if (isCoHost(player)) {
            coHostTeam.addEntry(player.getName());
        } else {
            playerTeam.addEntry(player.getName());
        }

        giveMenuItem(player);
    }

    /**
     * Construit la plateforme/cage en verre du Lobby d'attente au point d'apparition.
     */
    public void buildGlassCage(Location center) {
        World world = center.getWorld();
        if (world == null) return;

        int cx = center.getBlockX();
        int cy = center.getBlockY();
        int cz = center.getBlockZ();

        int radius = 10;
        int height = 5;

        for (int x = -radius; x <= radius; x++) {
            for (int z = -radius; z <= radius; z++) {
                // Sol en verre
                world.getBlockAt(cx + x, cy - 1, cz + z).setType(Material.STAINED_GLASS);

                // Murs
                if (Math.abs(x) == radius || Math.abs(z) == radius) {
                    for (int y = 0; y < height; y++) {
                        world.getBlockAt(cx + x, cy + y, cz + z).setType(Material.STAINED_GLASS);
                    }
                }
            }
        }
    }

    /**
     * Vérifie si un joueur est Host principal.
     */
    public boolean isHost(Player player) {
        return hostUUID != null && hostUUID.equals(player.getUniqueId());
    }

    /**
     * Vérifie si un joueur est Co-Host.
     */
    public boolean isCoHost(Player player) {
        return coHostUUIDs.contains(player.getUniqueId());
    }

    /**
     * Vérifie si un joueur est Host ou Co-Host (ou OP).
     */
    public boolean isHostOrCoHost(Player player) {
        return isHost(player) || isCoHost(player) || player.isOp();
    }

    // --- Raccourcis Délégués vers les Managers ---

    public void startGame() {
        gameManager.startGame();
    }

    public World createGameWorld() {
        return worldManager.createGameWorld();
    }

    public void pregenerateWorld(World world, int radius, Runnable onComplete) {
        worldManager.pregenerateWorld(world, radius, onComplete);
    }

    public World getGameWorld() {
        return worldManager.getGameWorld();
    }

    public boolean isPvpEnabled() {
        return gameManager.isPvpActive();
    }

    // --- Getters et Setters ---


    public BorderManager getBorderManager() { return borderManager; }
    public GUIManager getGuiManager() { return guiManager; }
    public GameManager getGameManager() { return gameManager; }
    public WorldManager getWorldManager() { return worldManager; }
    public ScoreboardManager getScoreboardManager() { return scoreboardManager; }
    public RoleManager getRoleManager() { return roleManager; }

    public String getGameName() { return gameName; }
    public void setGameName(String gameName) { this.gameName = gameName; }

    public int getEpisodeLengthSeconds() { return episodeLengthSeconds; }
    public void setEpisodeLengthSeconds(int episodeLengthSeconds) { this.episodeLengthSeconds = episodeLengthSeconds; }

    public int GetRoleTime() { return RoleTime; }
    public void setRoleTime(int RoleTimeSeconds) { this.RoleTime = RoleTimeSeconds; }

    public int GetFinalHealTime() {return FinalHealTime; }
    public void setFinalHealTime(int FinalHealSeconds) { this.FinalHealTime = FinalHealSeconds; }

    public int GetPvpTime() { return PvpTime; }
    public void setPvPTime(int PvPTimeSeconds) { this.PvpTime = PvPTimeSeconds; }

    public boolean isAwaitingGameNameInput() { return awaitingGameNameInput; }
    public void setAwaitingGameNameInput(boolean awaitingGameNameInput) { this.awaitingGameNameInput = awaitingGameNameInput; }

    public boolean isGameStarted() { return gameStarted; }
    public void setGameStarted(boolean gameStarted) { this.gameStarted = gameStarted; }

    public int getCaveSizePercent() { return caveSizePercent; }
    public void setCaveSizePercent(int caveSizePercent) { this.caveSizePercent = caveSizePercent; }

    public int getDiamondPercent() { return diamondPercent; }
    public void setDiamondPercent(int diamondPercent) { this.diamondPercent = diamondPercent; }

    public int getGoldPercent() { return goldPercent; }
    public void setGoldPercent(int goldPercent) { this.goldPercent = goldPercent; }

    public int getIronPercent() { return ironPercent; }
    public void setIronPercent(int ironPercent) { this.ironPercent = ironPercent; }

    public int getRedstonePercent() { return redstonePercent; }
    public void setRedstonePercent(int redstonePercent) { this.redstonePercent = redstonePercent; }

    public int getLapisPercent() { return lapisPercent; }
    public void setLapisPercent(int lapisPercent) { this.lapisPercent = lapisPercent; }

    public int getCoalPercent() { return coalPercent; }
    public void setCoalPercent(int coalPercent) { this.coalPercent = coalPercent; }

    public int getXpPercent() { return xpPercent; }
    public void setXpPercent(int xpPercent) { this.xpPercent = xpPercent; }

    public int getEmeraldPercent() { return emeraldPercent; }
    public void setEmeraldPercent(int emeraldPercent) { this.emeraldPercent = emeraldPercent; }

    public UUID getHostUUID() { return hostUUID; }
    public void setHostUUID(UUID hostUUID) { this.hostUUID = hostUUID; }

    public List<UUID> getCoHostUUIDs() { return coHostUUIDs; }
    public List<UUID> getNoDamagePlayers() { return noDamagePlayers; }
    public List<UUID> getNoFallPlayer() {return noFallPlayers; }
    public Map<UUID, Integer> getKills() { return kills; }

    private ItemStack[] starterKit;
    private ItemStack[] starterArmor;

    public ItemStack[] getStarterKit() { return starterKit; }
    public void setStarterKit(ItemStack[] starterKit) { this.starterKit = starterKit; }

    public ItemStack[] getStarterArmor() { return starterArmor; }
    public void setStarterArmor(ItemStack[] starterArmor) { this.starterArmor = starterArmor; }

    private boolean editingStarterKit = false;

    public boolean isEditingStarterKit() { return editingStarterKit; }
    public void setEditingStarterKit(boolean editingStarterKit) { this.editingStarterKit = editingStarterKit; }

    private int groupSize = 5;

    public int getGroupSize() { return groupSize; }
    public void setGroupSize(int groupSize) { this.groupSize = groupSize; }

    // --- TIMER MEETUP ---
    private int meetupTime = 3600; // En secondes (ex: 60 min)
    private boolean isMeetupActive = false;

    // --- CYCLE JOUR / NUIT ---
    private int dayNightCycleSeconds = 600; // Durée totale du cycle (ex: 10 min)
    private int dayPercent = 50; // 50% de jour / 50% de nuit

    // Getters et Setters pour le Meetup
    public int getMeetupTime() { return meetupTime; }
    public void setMeetupTime(int meetupTime) { this.meetupTime = meetupTime; }
    public boolean isMeetupActive() { return isMeetupActive; }
    public void setMeetupActive(boolean active) { this.isMeetupActive = active; }

    // Getters et Setters pour le Cycle Jour/Nuit
    public int getDayNightCycleSeconds() { return dayNightCycleSeconds; }
    public void setDayNightCycleSeconds(int seconds) { this.dayNightCycleSeconds = seconds; }
    public int getDayPercent() { return dayPercent; }
    public void setDayPercent(int percent) { this.dayPercent = percent; }
    public boolean isDay() {
        if (dayNightCycleSeconds <= 0) return true; // Sécurité division par 0

        // Position actuelle dans le cycle récurrent (ex: 750s % 600s = 150s)
        int currentCycleTime = getGameManager().GetTotalGameSeconds() % dayNightCycleSeconds;

        // Calcul de la durée exacte du jour dans un cycle (ex: 600s * 70% = 420s)
        int dayDurationSeconds = (dayNightCycleSeconds * dayPercent) / 100;

        // Si le temps dans le cycle est inférieur à la durée du jour, il fait jour
        return currentCycleTime < dayDurationSeconds;
    }

    private int borderInitialSize = 2000;
    private int borderFinalSize = 200;
    private int borderShrinkDuration = 1200; // en secondes
    private boolean borderInstant = false;

    public int getBorderInitialSize() { return borderInitialSize; }
    public void setBorderInitialSize(int size) { this.borderInitialSize = size; }

    public int getBorderFinalSize() { return borderFinalSize; }
    public void setBorderFinalSize(int size) { this.borderFinalSize = size; }

    public int getBorderShrinkDuration() { return borderShrinkDuration; }
    public void setBorderShrinkDuration(int seconds) { this.borderShrinkDuration = seconds; }

    public boolean isBorderInstant() { return borderInstant; }
    public void setBorderInstant(boolean borderInstant) { this.borderInstant = borderInstant; }
    private double critDamageMultiplier = 1.2; //

    public double getCritDamageMultiplier() {
        return critDamageMultiplier;
    }

    public void setCritDamageMultiplier(double critDamageMultiplier) {
        // Arrondit à 2 décimales pour éviter les imprécisions de calcul
        this.critDamageMultiplier = Math.round(critDamageMultiplier * 100.0) / 100.0;
    }

    // Liste des objets désactivés (si le Material est dedans = INTERDIT)
    private final Set<Material> disabledItems = new HashSet<>();

    public Set<Material> getDisabledItems() {
        return disabledItems;
    }

    public boolean isItemDisabled(Material material) {
        return disabledItems.contains(material);
    }

    public void toggleItemDisabled(Material material) {
        if (disabledItems.contains(material)) {
            disabledItems.remove(material);
        } else {
            disabledItems.add(material);
        }
    }
    private boolean notchAppleDisabled = false;

    public boolean isNotchAppleDisabled() {
        return notchAppleDisabled;
    }

    public void toggleNotchAppleDisabled() {
        this.notchAppleDisabled = !this.notchAppleDisabled;
    }
    private boolean chatMuted = false;

    public boolean isChatMuted() {
        return chatMuted;
    }

    public void setChatMuted(boolean chatMuted) {
        this.chatMuted = chatMuted;
    }
    private int maxDiamondArmorPieces = 2; // Exemple: Max 2 pièces en diamant (ex: Casque + Plastron)
    private int maxMinedDiamonds = 19;     // Limite de diamants minés par joueur

    public int getMaxDiamondArmorPieces() { return maxDiamondArmorPieces; }
    public void setMaxDiamondArmorPieces(int max) { this.maxDiamondArmorPieces = Math.max(0, Math.min(4, max)); }

    public int getMinedDiamonds(Player player) {
        return minedDiamondsMap.getOrDefault(player.getUniqueId(), 0);
    }

    private final Map<UUID, Integer> minedDiamondsMap = new HashMap<>();
    /**
     * Incrémente de 1 le nombre de diamants minés par le joueur.
     */
    public void incrementMinedDiamonds(Player player) {
        int current = getMinedDiamonds(player);
        minedDiamondsMap.put(player.getUniqueId(), current + 1);
    }

    /**
     * Réinitialise les données des diamants (à appeler au lancement ou à la fin d'une partie).
     */
    public void resetMinedDiamonds() {
        minedDiamondsMap.clear();
    }

    // Getteurs / Setteurs pour le max
    public int getMaxMinedDiamonds() {
        return maxMinedDiamonds;
    }

    public void setMaxMinedDiamonds(int maxMinedDiamonds) {
        this.maxMinedDiamonds = maxMinedDiamonds;
    }

    // ==========================================
// VARIABLES & VALEURS PAR DÉFAUT (ENCHANTS)
// ==========================================
    private int ironSharpnessMax = 2;
    private int ironProtectionMax = 3;
    private int ironKnockbackMax = 0;
    private int ironFireMax = 0;

    private int diamondSharpnessMax = 3;
    private int diamondProtectionMax = 2;
    private int diamondKnockbackMax = 0;
    private int diamondFireMax = 0;

    // ==========================================
// GETTERS & SETTERS : ENCHANTS FER
// ==========================================
    public int getIronSharpnessMax() {

        return ironSharpnessMax;
    }

    public void setIronSharpnessMax(int ironSharpnessMax) {

        this.ironSharpnessMax = ironSharpnessMax;
    }

    public int getIronProtectionMax() {
        return ironProtectionMax;
    }

    public void setIronProtectionMax(int ironProtectionMax) {
        this.ironProtectionMax = ironProtectionMax;
    }

    public int getIronKnockbackMax() {
        return ironKnockbackMax;
    }

    public void setIronKnockbackMax(int ironKnockbackMax) {
        this.ironKnockbackMax = ironKnockbackMax;
    }

    public int getIronFireMax() {
        return ironFireMax;
    }

    public void setIronFireMax(int ironFireMax) {
        this.ironFireMax = ironFireMax;
    }

    // ==========================================
// GETTERS & SETTERS : ENCHANTS DIAMANT
// ==========================================
    public int getDiamondSharpnessMax() {
        return diamondSharpnessMax;
    }

    public void setDiamondSharpnessMax(int diamondSharpnessMax) {
        this.diamondSharpnessMax = diamondSharpnessMax;
    }

    public int getDiamondProtectionMax() {
        return diamondProtectionMax;
    }

    public void setDiamondProtectionMax(int diamondProtectionMax) {
        this.diamondProtectionMax = diamondProtectionMax;
    }

    public int getDiamondKnockbackMax() {
        return diamondKnockbackMax;
    }

    public void setDiamondKnockbackMax(int diamondKnockbackMax) {
        this.diamondKnockbackMax = diamondKnockbackMax;
    }

    public int getDiamondFireMax() {
        return diamondFireMax;
    }

    public void setDiamondFireMax(int diamondFireMax) {

        this.diamondFireMax = diamondFireMax;
    }
    public void loadConfiguration() {
        this.ironSharpnessMax = getConfig().getInt("enchants.iron.sharpness", 3);
        this.ironProtectionMax = getConfig().getInt("enchants.iron.protection", 3);
        this.ironKnockbackMax = getConfig().getInt("enchants.iron.knockback", 0);
        this.ironFireMax = getConfig().getInt("enchants.iron.fire", 0);

        this.diamondSharpnessMax = getConfig().getInt("enchants.diamond.sharpness", 3);
        this.diamondProtectionMax = getConfig().getInt("enchants.diamond.protection", 2);
        this.diamondKnockbackMax = getConfig().getInt("enchants.diamond.knockback", 0);
        this.diamondFireMax = getConfig().getInt("enchants.diamond.fire", 0);

        this.BowPowerMax = getConfig().getInt("enchants.bow.power", 3);
        this.BowPunchMax = getConfig().getInt("enchants.bow.punch", 0);
        this.bowFlameMax = getConfig().getInt("enchants.bow.flame", 0);
    }

    public void saveConfiguration() {
        getConfig().set("enchants.iron.sharpness", ironSharpnessMax);
        getConfig().set("enchants.iron.protection", ironProtectionMax);
        getConfig().set("enchants.iron.knockback", ironKnockbackMax);
        getConfig().set("enchants.iron.fire", ironFireMax);

        getConfig().set("enchants.diamond.sharpness", diamondSharpnessMax);
        getConfig().set("enchants.diamond.protection", diamondProtectionMax);
        getConfig().set("enchants.diamond.knockback", diamondKnockbackMax);
        getConfig().set("enchants.diamond.fire", diamondFireMax);

        getConfig().getInt("enchants.bow.power", BowPowerMax);
        getConfig().getInt("enchants.bow.punch", BowPunchMax);
        getConfig().getInt("enchants.bow.flame", bowFlameMax);

        saveConfig();
    }

    private int BowPowerMax = 3;
    private int BowPunchMax = 0;
    private int bowFlameMax = 0;

    public void setBowPowerMax(int BowPowerMax) {
        this.BowPowerMax = BowPowerMax;
    }

    public int getBowPowerMax() {
        return BowPowerMax;
    }

    public int getBowFlameMax() {
        return bowFlameMax;
    }

    public int getBowPunchMax() {
        return BowPunchMax;
    }

    public void setBowFlameMax(int bowFlameMax) {
        this.bowFlameMax = bowFlameMax;
    }

    public void setBowPunchMax(int BowPunchMax) {
        this.BowPunchMax = BowPunchMax;
    }

    public ScenarioManager getScenarioManager() {
        return scenarioManager;
    }

    // --- VARIABLES POUR LES RATES DE DROP (Valeurs par défaut en %) ---
    private int appleDropPercent = 10;
    private int flintDropPercent = 50;
    private int enderPearlDropPercent = 10;

    // --- GETTERS & SETTERS ---
    public int getAppleDropPercent() {
        return appleDropPercent;
    }

    public void setAppleDropPercent(int appleDropPercent) {
        this.appleDropPercent = appleDropPercent;
    }

    public int getFlintDropPercent() {
        return flintDropPercent;
    }

    public void setFlintDropPercent(int flintDropPercent) {
        this.flintDropPercent = flintDropPercent;
    }

    public int getEnderPearlDropPercent() {
        return enderPearlDropPercent;
    }

    public void setEnderPearlDropPercent(int enderPearlDropPercent) {
        this.enderPearlDropPercent = enderPearlDropPercent;
    }

    private double resistanceMult = 0.2;
    private double strenghtMult = 0.6;
    public double getResistanceMultiplier() {
        return resistanceMult;
    }

    public double getStrengthMultiplier() {
        return strenghtMult;
    }

    public void setResistanceMultiplier(double max) {
        this.resistanceMult = Math.round(max * 100.0) / 100.0;
    }

    public void setStrengthMultiplier(double max) {
        this.strenghtMult = Math.round(max * 100.0) / 100.0;
    }
}