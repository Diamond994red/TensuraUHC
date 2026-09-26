package uhc.tensuraUHC.roles.list.MonstersCamp;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import uhc.tensuraUHC.TensuraUHC;
import uhc.tensuraUHC.roles.Role;

import java.util.*;

public class LimuleRole extends Role {

    private int chosenPact = -1;
    private int predatorUses = 0;
    private long lastPredatorTime = 0;
    private final Map<Location, Long> deathLocations = new HashMap<>();

    private BukkitTask pactTimer;
    private BukkitTask revealTimer;
    private BukkitTask proximityTask;
    private BukkitTask actionbarTask;

    public LimuleRole(TensuraUHC main) {
        super(main, "Limule", Camp.MONSTRES,
                "Leader charismatique capable de s'adapter et de sceller des pactes puissants.");

        addPower("Prédateur", "Absorbez l'essence d'un joueur mort à proximité pour gagner des effets permanents.");
        addPower("Pactes", "Choisissez votre voie de victoire.");
    }

    @Override
    public void FakeRoleMessage(Player sender) {

    }

    @Override
    public void giveRole(Player player) {
        getItemsToGive().clear();

        // 1. Création de l'item Prédateur
        ItemStack predator = new ItemStack(Material.NETHER_STAR);
        ItemMeta predatorMeta = predator.getItemMeta();
        if (predatorMeta != null) {
            predatorMeta.setDisplayName(ChatColor.RED + "Prédateur");
            List<String> lore = new ArrayList<>();
            lore.add(ChatColor.GRAY + "Clic droit pour absorber l'essence d'un joueur mort à proximité.");
            predatorMeta.setLore(lore);
            predator.setItemMeta(predatorMeta);
        }
        addItem(predator);

        // 2. Création de l'item Pactes
        ItemStack pact = new ItemStack(Material.CHEST);
        ItemMeta pactMeta = pact.getItemMeta();
        if (pactMeta != null) {
            pactMeta.setDisplayName(ChatColor.RED + "Pactes");
            List<String> lore = new ArrayList<>();
            lore.add(ChatColor.GRAY + "Choisissez votre voie de victoire.");
            pactMeta.setLore(lore);
            pact.setItemMeta(pactMeta);
        }
        addItem(pact);

        super.giveRole(player);

        // Lancement des boucles de jeu
        startPactChoiceTimer(player);
        startProximityCheck(player);
        startDeathProximityCheck(player);
    }

    // ==========================================
    // SELECTION DES PACTES & GUI
    // ==========================================
    private void startDeathProximityCheck(Player player) {
        actionbarTask = new BukkitRunnable() {
            @Override
            public void run() {
                if (!player.isOnline()) return;

                boolean nearDeathLocation = false;

                for (Location loc : deathLocations.keySet()) {
                    if (loc.getWorld().equals(player.getWorld()) && loc.distance(player.getLocation()) <= 10.0) {
                        nearDeathLocation = true;
                        break;
                    }
                }

                if (nearDeathLocation) {
                    sendActionBar(player, ChatColor.GOLD + "§l[Prédateur] " + ChatColor.GREEN + "Corps à proximité !");
                }
            }
        }.runTaskTimer(main, 0L, 10L);
    }

    public void openPactGUI(Player player) {
        if (chosenPact != -1) return;
        Inventory gui = Bukkit.createInventory(null, 9, ChatColor.DARK_PURPLE + "Choix du Pacte");

        gui.setItem(1, createPactItem(Material.DRAGON_EGG, "Pacte 1 : Alliance avec Veldra", "Victoire : Limule & Veldra"));
        gui.setItem(3, createPactItem(Material.FEATHER, "Pacte 2 : Alliance avec Ranga", "Victoire : Limule & Ranga"));
        gui.setItem(5, createPactItem(Material.NETHER_STAR, "Pacte 3 : Alliance Trio", "Victoire : Limule, Veldra & Ranga"));
        gui.setItem(7, createPactItem(Material.SLIME_BALL, "Pacte 4 : Faction Monstres", "Victoire : Monstres de Jura"));

        player.openInventory(gui);
    }

    private ItemStack createPactItem(Material material, String name, String lore) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(ChatColor.GOLD + name);
            meta.setLore(Collections.singletonList(ChatColor.GRAY + lore));
            item.setItemMeta(meta);
        }
        return item;
    }

    private void startPactChoiceTimer(Player player) {
        pactTimer = new BukkitRunnable() {
            int remaining = 300;

            @Override
            public void run() {
                if (chosenPact != -1) {
                    cancel();
                    return;
                }
                if (remaining <= 0) {
                    selectPact(player, new Random().nextInt(4) + 1);
                    cancel();
                    return;
                }
                if (remaining == 300 || remaining == 60 || remaining == 10) {
                    player.sendMessage(ChatColor.YELLOW + "[Limule] Il vous reste " + (remaining / 60) + " min "
                            + (remaining % 60) + "s pour choisir votre pacte.");
                }
                remaining--;
            }
        }.runTaskTimer(main, 0L, 20L);
    }

    public void selectPact(Player player, int pactNumber) {
        this.chosenPact = pactNumber;
        player.sendMessage(ChatColor.GREEN + "Vous avez choisi le Pacte " + pactNumber + " !");

        if (pactNumber != 4) {
            setCamp(Camp.LIMULE);
            startRevealTimer(player);
        }
    }

    private void startRevealTimer(Player player) {
        revealTimer = new BukkitRunnable() {
            @Override
            public void run() {
                if (new Random().nextInt(100) < 5) {
                    Bukkit.broadcastMessage(ChatColor.RED + "[Alerte] Limule Tempest a été repéré en position X: " + player.getLocation().getBlockX());
                }
            }
        }.runTaskTimer(main, 12000L, 12000L);
    }

    // ==========================================
    // INTERACTIONS ET UTILISATIONS
    // ==========================================

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player victim = event.getEntity();

        deathLocations.put(victim.getLocation(), System.currentTimeMillis());

        for (Player player : Bukkit.getOnlinePlayers()) {
            Role playerRole = main.getRoleManager().getPlayerRole(player);
            if (playerRole != null && playerRole.getClass().equals(this.getClass())) {
                boolean sendX = new Random().nextBoolean();
                int coordinate = sendX ? victim.getLocation().getBlockX() : victim.getLocation().getBlockZ();
                String axisName = sendX ? "X" : "Z";

                player.sendMessage(ChatColor.GOLD + "[Prédateur] " + ChatColor.AQUA
                        + "Un joueur est mort ! Axe " + axisName + " : " + coordinate);
            }
        }
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        if (!main.getRoleManager().hasRole(player, this)) return;

        ItemStack item = event.getItem();
        if (item == null || !item.hasItemMeta()) return;

        String displayName = item.getItemMeta().getDisplayName();
        if (displayName == null) return;

        if (item.getType() == Material.NETHER_STAR && displayName.equals(ChatColor.RED + "Prédateur")) {
            event.setCancelled(true);

            // Empêche de relancer si une absorption est déjà en cours
            if (isAbsorbing) {
                player.sendMessage(ChatColor.RED + "Vous êtes déjà en train d'absorber une cible !");
                return;
            }

            if (System.currentTimeMillis() - lastPredatorTime < 60000) { // 1 minute
                player.sendMessage(ChatColor.RED + "Prédateur est en rechargement.");
                return;
            }

            int maxUses = (chosenPact == 4) ? 3 : (chosenPact == 3) ? 4 : 5;
            if (predatorUses >= maxUses) {
                player.sendMessage(ChatColor.RED + "Vous avez atteint la limite d'utilisations de Prédateur.");
                return;
            }

            for (Map.Entry<Location, Long> entry : deathLocations.entrySet()) {
                Location loc = entry.getKey();
                if (loc.getWorld().equals(player.getWorld()) && loc.distance(player.getLocation()) <= 5) {
                    startPredatorAbsorption(player, loc);
                    break;
                }
            }
        }

        if (item.getType() == Material.CHEST && displayName.equals(ChatColor.RED + "Pactes")) {
            event.setCancelled(true);
            openPactGUI(player);
        }
    }

    private boolean isAbsorbing = false;
    private BukkitTask absorptionTask; // Pour pouvoir l'annuler au reset()

    // 2. Remplace la méthode startPredatorAbsorption :
    private void startPredatorAbsorption(Player player, Location deathLoc) {
        this.isAbsorbing = true;
        player.sendMessage(ChatColor.GREEN + "Absorption en cours... Ne bougez pas pendant 20 secondes !");

        this.absorptionTask = new BukkitRunnable() {
            int timer = 20;
            final Location startLoc = player.getLocation().clone();

            @Override
            public void run() {
                // Vérification si le joueur a bougé
                if (!player.isOnline() || player.getLocation().distance(startLoc) > 0.5) {
                    player.sendMessage(ChatColor.RED + "Absorption annulée : vous avez bougé !");
                    isAbsorbing = false;
                    cancel();
                    return;
                }

                if (timer <= 0) {
                    predatorUses++;
                    deathLocations.remove(deathLoc);
                    applyPredatorBuffs(player);

                    player.sendMessage(ChatColor.GOLD + "[Prédateur] " + ChatColor.GREEN + "Compétence réussie.");
                    lastPredatorTime = System.currentTimeMillis(); // Cooldown appliqué dès le début !
                    isAbsorbing = false;
                    cancel();
                    return;
                }
                timer--;
            }
        }.runTaskTimer(main, 0L, 20L);
    }

    private void applyPredatorBuffs(Player player) {
        // Vider la liste avant de réappliquer pour éviter d'empiler des doublons
        getPassiveEffects().clear();

        switch (predatorUses) {
            case 1:
                addPassiveEffect(PotionEffectType.SPEED, 0);
                break;
            case 2:
                addPassiveEffect(PotionEffectType.SPEED, 0);
                addPassiveEffect(PotionEffectType.DAMAGE_RESISTANCE, 0);
                break;
            case 3:
                addPassiveEffect(PotionEffectType.SPEED, 0);
                addPassiveEffect(PotionEffectType.DAMAGE_RESISTANCE, 0);
                addPassiveEffect(PotionEffectType.INCREASE_DAMAGE, 0);
                break;
            case 4:
                addPassiveEffect(PotionEffectType.SPEED, 1);
                addPassiveEffect(PotionEffectType.DAMAGE_RESISTANCE, 0);
                addPassiveEffect(PotionEffectType.INCREASE_DAMAGE, 0);
                break;
            case 5:
                addPassiveEffect(PotionEffectType.SPEED, 1);
                addPassiveEffect(PotionEffectType.DAMAGE_RESISTANCE, 1);
                addPassiveEffect(PotionEffectType.INCREASE_DAMAGE, 0);
                break;
        }

        for (PotionEffect effect : getPassiveEffects()) {
            player.addPotionEffect(effect, true);
        }
    }

    // ==========================================
    // BOOSTS DE PROXIMITÉ & GUI LISTENER
    // ==========================================

    private void startProximityCheck(Player player) {
        proximityTask = new BukkitRunnable() {
            @Override
            public void run() {
                if (!player.isOnline()) return;

                boolean boostSpeed = false;
                boolean boostResist = false;
                boolean boostForce = false;

                for (Player nearby : player.getWorld().getPlayers()) {
                    if (nearby.equals(player) || nearby.getLocation().distance(player.getLocation()) > 15) continue;

                    Role nearbyRole = main.getRoleManager().getPlayerRole(nearby);
                    if (nearbyRole == null) continue;

                    String roleName = nearbyRole.getName();

                    if (roleName.equals("Ranga") && (chosenPact == 2 || chosenPact == 3)) {
                        boostSpeed = true;
                    }
                    if (roleName.equals("Shizu") && chosenPact == 4) {
                        boostResist = true;
                    }
                    if (roleName.equals("Veldra") && (chosenPact == 1 || chosenPact == 3)) {
                        boostForce = true;
                    }
                }

                applyProximityEffect(player, PotionEffectType.SPEED, boostSpeed);
                applyProximityEffect(player, PotionEffectType.DAMAGE_RESISTANCE, boostResist);
                applyProximityEffect(player, PotionEffectType.INCREASE_DAMAGE, boostForce);
            }
        }.runTaskTimer(main, 0L, 40L);
    }

    private void applyProximityEffect(Player player, PotionEffectType type, boolean apply) {
        if (apply) {
            player.addPotionEffect(new PotionEffect(type, 60, 0, false, false));
        }
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!event.getView().getTitle().equals(ChatColor.DARK_PURPLE + "Choix du Pacte")) return;

        event.setCancelled(true);
        if (!(event.getWhoClicked() instanceof Player)) return;
        Player player = (Player) event.getWhoClicked();

        ItemStack currentItem = event.getCurrentItem();
        if (currentItem == null || !currentItem.hasItemMeta()) return;

        Role role = main.getRoleManager().getPlayerRole(player);
        if (!(role instanceof LimuleRole)) return;

        LimuleRole limuleRole = (LimuleRole) role;

        switch (event.getSlot()) {
            case 1:
                limuleRole.selectPact(player, 1);
                break;
            case 3:
                limuleRole.selectPact(player, 2);
                break;
            case 5:
                limuleRole.selectPact(player, 3);
                break;
            case 7:
                limuleRole.selectPact(player, 4);
                break;
            default:
                return;
        }

        player.closeInventory();
    }

    @Override
    public void reset(Player player) {
        // 1. Annulation des tâches répétitives
        if (pactTimer != null) { pactTimer.cancel(); pactTimer = null; }
        if (revealTimer != null) { revealTimer.cancel(); revealTimer = null; }
        if (proximityTask != null) { proximityTask.cancel(); proximityTask = null; }
        if (actionbarTask != null) { actionbarTask.cancel(); actionbarTask = null; }

        // 2. Réinitialisation des variables de rôle
        this.chosenPact = -1;
        this.predatorUses = 0;
        this.lastPredatorTime = 0;
        this.deathLocations.clear();

        // 3. Nettoyage des effets passifs dynamiques ajoutés
        if (player != null && player.isOnline()) {
            for (PotionEffect effect : getPassiveEffects()) {
                player.removePotionEffect(effect.getType());
            }
        }
        getPassiveEffects().clear();
        if (absorptionTask != null) {
            absorptionTask.cancel();
            absorptionTask = null;
        }
        this.isAbsorbing = false;

        // 4. Appel du reset parent pour remettre la vie par défaut
        super.reset(player);
    }

    public void addTestDeathLocation(Location location) {
        this.deathLocations.put(location, System.currentTimeMillis());
    }
}