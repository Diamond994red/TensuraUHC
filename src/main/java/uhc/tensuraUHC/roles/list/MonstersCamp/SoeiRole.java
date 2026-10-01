package uhc.tensuraUHC.roles.list.MonstersCamp;

import jdk.internal.org.jline.utils.Log;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.permissions.BroadcastPermissions;
import uhc.tensuraUHC.TensuraUHC;
import uhc.tensuraUHC.powers.SoeiInvisiblePower;
import uhc.tensuraUHC.roles.Role;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

public class SoeiRole extends Role {

    private SoeiInvisiblePower soeiInvisiblePower = null;
    private int usedThread = 0;
    // Liste des pièges/fils actifs sur la carte
    private final List<ThreadTrap> activeTraps = new ArrayList<>();

    public SoeiRole(TensuraUHC main) {
        super(main, "Soei", Camp.MONSTRES,
                "Ninja et espion hors-pair au service de Limule.\n" +
                        "Vous êtes invisible tant que vous ne portez aucune armure.\n" +
                        "Posez votre fil au sol pour analyser les 3 prochains joueurs qui passeront à proximité.");
        addPassiveEffect(PotionEffectType.SPEED, 0);
        addPower("Infiltration", "Vous devenez invisible lorsque vous ne portez aucune pièce d'armure.");
        addPower("Fil d'écoute", "Posez votre fil au sol pour révéler un des effets des 3 prochains joueurs s'en approchant.");
    }

    @Override
    public void FakeRoleMessage(Player sender) {

    }

    @Override
    public void giveRole(Player player) {
        super.reset(player.getUniqueId());
        // Vider la liste pour éviter les doublons
        getItemsToGive().clear();

        // Création de l'item Fil
        ItemStack fil = new ItemStack(Material.STRING);
        ItemMeta meta = fil.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(ChatColor.RED + "Fil d'écoute");
            List<String> lore = new ArrayList<>();
            meta.setLore(lore);
            fil.setItemMeta(meta);
        }
        addItem(fil);

        // Distribution de l'item au joueur
        soeiInvisiblePower = new SoeiInvisiblePower(main);
        soeiInvisiblePower.activate(player);
        startThreadCheck(player);
        super.giveRole(player);
    }
    // ==========================================
    // POSE ET GESTION DU FIL
    // ==========================================

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        Player player = event.getPlayer();
        if (!main.getRoleManager().hasRole(player.getUniqueId(), this)) return;

        ItemStack item = event.getItemInHand();
        if (item == null || item.getType() != Material.STRING || !item.hasItemMeta()) return;

        ItemMeta meta = item.getItemMeta();
        if (meta.hasDisplayName() && meta.getDisplayName().equals(ChatColor.RED + "Fil d'écoute") && usedThread <= 2) {
            Location placedLoc = event.getBlockPlaced().getLocation();
            World world = placedLoc.getWorld();
            world.getBlockAt(placedLoc).setType(Material.AIR);
            activeTraps.add(new ThreadTrap(placedLoc));
            usedThread ++;
            player.sendMessage(ChatColor.GREEN + "[Soei] Fil posé en X: " + placedLoc.getBlockX()
                    + " Y: " + placedLoc.getBlockY() + " Z: " + placedLoc.getBlockZ() + " !");
        }
    }

    private void startThreadCheck(Player player) {
        UUID pl = player.getUniqueId();
        BukkitTask threadCheckTask = new BukkitRunnable() {
            @Override
            public void run() {

                if (activeTraps.isEmpty()) return;
                if (main.getRoleManager().getPlayerRole(player.getUniqueId()) != SoeiRole.this) {
                    cancel();
                    return;
                }
                if (!player.isOnline()) return;
                Iterator<ThreadTrap> iterator = activeTraps.iterator();
                while (iterator.hasNext()) {
                    ThreadTrap trap = iterator.next();
                    Location trapLoc = trap.location;

                    for (Player target : trapLoc.getWorld().getPlayers()) {
                        // Soei ne se détecte pas lui-même
                        if (target.equals(Bukkit.getPlayer(pl))) continue;

                        // Vérifie si le joueur est à 3 blocs ou moins du fil
                        if (target.getLocation().distance(trapLoc) <= 3.0) {
                            if (!trap.detectedPlayers.contains(target.getUniqueId())) {
                                trap.detectedPlayers.add(target.getUniqueId());
                                trap.usesLeft--;

                                // Récupération des informations du joueur
                                Collection<PotionEffect> activeEffects = target.getActivePotionEffects();
                                String effectStr;

                                if (!activeEffects.isEmpty()) {
                                    // Convertir en liste pour pouvoir sélectionner par index
                                    List<PotionEffect> effectsList = new ArrayList<>(activeEffects);

                                    // Tirer un effet au sort
                                    PotionEffect randomEffect = effectsList.get(ThreadLocalRandom.current().nextInt(effectsList.size()));

                                    effectStr = randomEffect.getType().getName();
                                } else {
                                    effectStr = "Aucun effet";
                                }

                                // Envoi des informations à Soei
                                Bukkit.getPlayer(pl).sendMessage(ChatColor.GOLD + "========== [Soei - Rapport Fil] ==========");
                                Bukkit.getPlayer(pl).sendMessage(ChatColor.YELLOW + "Joueur repéré : " + ChatColor.WHITE + target.getName());
                                Bukkit.getPlayer(pl).sendMessage(ChatColor.GRAY + "Effets : " + ChatColor.LIGHT_PURPLE + effectStr);
                                Bukkit.getPlayer(pl).sendMessage(ChatColor.GOLD + "==========================================");

                                // Si le fil a atteint 3 utilisations, on le détruit
                                if (trap.usesLeft <= 0) {
                                    if (trapLoc.getBlock().getType() == Material.STRING || trapLoc.getBlock().getType() == Material.TRIPWIRE) {
                                        trapLoc.getBlock().setType(Material.AIR);
                                    }
                                    Bukkit.getPlayer(pl).sendMessage(ChatColor.RED + "[TensuraUHC] L'un de vos fils d'infiltration a été entièrement consumé.");
                                    iterator.remove();
                                    break;
                                }
                            }
                        }
                    }
                }
            }
        }.runTaskTimer(main, 0L, 10L); // Vérification 2 fois par seconde
        addTask(pl, "filsSoei",threadCheckTask);
    }

    // ==========================================
    // CLASSE INTERNE PIÈGE
    // ==========================================

    private static class ThreadTrap {
        private final Location location;
        private int usesLeft = 3;
        private final Set<UUID> detectedPlayers = new HashSet<>();

        public ThreadTrap(Location location) {
            this.location = location;
        }
    }

    @Override
    public void reset(UUID pl) {
        Player player = Bukkit.getPlayer(pl);
        super.reset(pl);

        // Nettoyage des fils posés au sol
        for (ThreadTrap trap : activeTraps) {
            if (trap.location.getBlock().getType() == Material.STRING || trap.location.getBlock().getType() == Material.TRIPWIRE) {
                trap.location.getBlock().setType(Material.AIR);
            }
        }
        activeTraps.clear();
        if (soeiInvisiblePower != null) {
            soeiInvisiblePower.reset(player);
            soeiInvisiblePower = null;
        }
        if (player != null && player.isOnline()) {
            player.removePotionEffect(PotionEffectType.INVISIBILITY);
        }
    }
}