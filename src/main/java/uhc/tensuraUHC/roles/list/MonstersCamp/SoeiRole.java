package uhc.tensuraUHC.roles.list.MonstersCamp;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import uhc.tensuraUHC.TensuraUHC;
import uhc.tensuraUHC.powers.SoeiPower;
import uhc.tensuraUHC.roles.Role;

import java.util.*;

public class SoeiRole extends Role {

    private SoeiPower soeiPower = null;
    private BukkitTask threadCheckTask;
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
        addPower("Fil d'écoute", "Posez votre fil au sol pour révéler le rôle, le camp et les effets des 3 prochains joueurs s'en approchant.");
    }

    @Override
    public void FakeRoleMessage(Player sender) {

    }

    @Override
    public void giveRole(Player player) {
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
        soeiPower = new SoeiPower(main);
        soeiPower.activate(player);
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
        if (meta.hasDisplayName() && meta.getDisplayName().equals(ChatColor.RED + "Fil d'écoute") && usedThread >= 2) {
            Location placedLoc = event.getBlockPlaced().getLocation();
            activeTraps.add(new ThreadTrap(placedLoc));
            usedThread ++;
            player.sendMessage(ChatColor.GREEN + "[Soei] Fil posé en X: " + placedLoc.getBlockX()
                    + " Y: " + placedLoc.getBlockY() + " Z: " + placedLoc.getBlockZ() + " !");
        }
    }

    private void startThreadCheck(Player player) {
        threadCheckTask = new BukkitRunnable() {
            @Override
            public void run() {
                if (!player.isOnline() || activeTraps.isEmpty()) return;

                Iterator<ThreadTrap> iterator = activeTraps.iterator();
                while (iterator.hasNext()) {
                    ThreadTrap trap = iterator.next();
                    Location trapLoc = trap.location;

                    for (Player target : trapLoc.getWorld().getPlayers()) {
                        // Soei ne se détecte pas lui-même
                        if (target.equals(player)) continue;

                        // Vérifie si le joueur est à 3 blocs ou moins du fil
                        if (target.getLocation().distance(trapLoc) <= 3.0) {
                            if (!trap.detectedPlayers.contains(target.getUniqueId())) {
                                trap.detectedPlayers.add(target.getUniqueId());
                                trap.usesLeft--;

                                // Récupération des informations du joueur
                                Role targetRole = main.getRoleManager().getPlayerRole(target.getUniqueId());
                                String roleName = (targetRole != null) ? targetRole.getName() : "Aucun";
                                String campName = (targetRole != null && targetRole.getCamp() != null) ? targetRole.getCamp().name() : "Inconnu";

                                StringBuilder effectsStr = new StringBuilder();
                                for (PotionEffect effect : target.getActivePotionEffects()) {
                                    if (effectsStr.length() > 0) effectsStr.append(", ");
                                    effectsStr.append(effect.getType().getName())
                                            .append(" ")
                                            .append(effect.getAmplifier() + 1);
                                }
                                if (effectsStr.length() == 0) {
                                    effectsStr.append("Aucun effet");
                                }

                                // Envoi des informations à Soei
                                player.sendMessage(ChatColor.GOLD + "========== [Soei - Rapport Fil] ==========");
                                player.sendMessage(ChatColor.YELLOW + "Joueur repéré : " + ChatColor.WHITE + target.getName());
                                player.sendMessage(ChatColor.GRAY + "Rôle : " + ChatColor.GREEN + roleName);
                                player.sendMessage(ChatColor.GRAY + "Camp : " + ChatColor.AQUA + campName);
                                player.sendMessage(ChatColor.GRAY + "Effets : " + ChatColor.LIGHT_PURPLE + effectsStr.toString());
                                player.sendMessage(ChatColor.GOLD + "==========================================");

                                // Si le fil a atteint 3 utilisations, on le détruit
                                if (trap.usesLeft <= 0) {
                                    if (trapLoc.getBlock().getType() == Material.STRING || trapLoc.getBlock().getType() == Material.TRIPWIRE) {
                                        trapLoc.getBlock().setType(Material.AIR);
                                    }
                                    player.sendMessage(ChatColor.RED + "[Soei] L'un de vos fils d'infiltration a été entièrement consumé.");
                                    iterator.remove();
                                    break;
                                }
                            }
                        }
                    }
                }
            }
        }.runTaskTimer(main, 0L, 10L); // Vérification 2 fois par seconde
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
    public void reset(Player player) {
        super.reset(player);

        if (threadCheckTask != null) threadCheckTask.cancel();

        // Nettoyage des fils posés au sol
        for (ThreadTrap trap : activeTraps) {
            if (trap.location.getBlock().getType() == Material.STRING || trap.location.getBlock().getType() == Material.TRIPWIRE) {
                trap.location.getBlock().setType(Material.AIR);
            }
        }
        activeTraps.clear();
        if (soeiPower != null) {
            soeiPower.reset(player);
            soeiPower = null;
        }
        if (player != null && player.isOnline()) {
            player.removePotionEffect(PotionEffectType.INVISIBILITY);
        }
    }
}