package uhc.tensuraUHC.roles.list.OctagramCamp;

import org.bukkit.*;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import uhc.tensuraUHC.TensuraUHC;
import uhc.tensuraUHC.roles.Role;

import java.util.*;

public class MilimRole extends Role {

    private final Map<UUID, Double> furyGauge = new HashMap<>();
    private final Map<UUID, Long> bypassResiUntil = new HashMap<>();
    private final Set<UUID> globalMessageTriggered = new HashSet<>();

    public MilimRole(TensuraUHC main) {
        super(main, "Milim Nava", Camp.OCTAGRAMME,
                "Vous êtes l'un des plus anciens et puissants Rois Démons. Votre fureur augmente au combat et débloque des pouvoirs dévastateurs.");

        // Effets passifs permanents : Force I et Speed I
        addPassiveEffect(PotionEffectType.INCREASE_DAMAGE, 0);
        addPassiveEffect(PotionEffectType.SPEED, 0);

        // Pouvoirs
        addPower("Jauge de Fureur", "+5% par coup d'épée reçu, +1% par coup d'épée donné, +1% par minute.");
        addPower("Étoile de Fureur", "Cliquez avec votre 'Étoile de Fureur' pour consommer votre jauge :\n" +
                " - 10% : Résistance I (10 sec)\n" +
                " - 25% : Vol (20 sec)\n" +
                " - 50% : Vos coups traversent la Résistance ennemie (30 sec)\n" +
                " - 75% : Résistance II (1 min)\n" +
                " - 100% : Drago-Nova (Rayon dévastateur de 10 cœurs de dégâts bruts en zone + Cécité)");
    }

    @Override
    public void FakeRoleMessage(Player sender) {
    }

    @Override
    public void giveRole(Player player) {
        reset(player.getUniqueId());
        getItemsToGive().clear();

        // Give de l'item unique "Étoile de Fureur"
        ItemStack furyStar = new ItemStack(org.bukkit.Material.NETHER_STAR);
        ItemMeta meta = furyStar.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(ChatColor.LIGHT_PURPLE + "Étoile de Fureur");
            meta.setLore(Arrays.asList(
                    ChatColor.GRAY + "Clic droit : Consommer votre Fureur",
                    ChatColor.YELLOW + "Jauge actuelle : 0%"
            ));
            furyStar.setItemMeta(meta);
        }
        addItem(furyStar);

        super.giveRole(player);

        // Initialisation de la jauge à 0%
        furyGauge.put(player.getUniqueId(), 0.0);

        // Tâche périodique : +1% de fureur par minute & Particules à partir de 50%
        BukkitTask task = new BukkitRunnable() {
            int minuteTimer = 0;

            @Override
            public void run() {
                Player p = Bukkit.getPlayer(player.getUniqueId());
                if (p == null || !p.isOnline()) {
                    cancel();
                    return;
                }

                minuteTimer += 20;

                // +1% par minute (toutes les 60 secondes = 1200 ticks)
                if (minuteTimer % 60 == 0) {
                    addFury(p, 1.0);
                }

                double currentFury = furyGauge.getOrDefault(p.getUniqueId(), 0.0);

                // À partir de 50%, apparition de particules rose/violettes toutes les 5 minutes (300 secondes)
                if (currentFury >= 50.0 && minuteTimer % 300 == 0) {
                    p.getWorld().spigot().playEffect(
                            p.getLocation().add(0, 1, 0),
                            Effect.WITCH_MAGIC, // Ou SPELL, HEART, VILLAGER_THUNDER, etc.
                            0, 0,
                            0.5f, 0.5f, 0.5f, // Offsets X, Y, Z
                            0.1f, // Vitesse
                            30, // Nombre de particules
                            16  // Rayon de visibilité
                    );
                }
            }
        }.runTaskTimer(main, 20L, 20L);

        addTask(player.getUniqueId(), "milim_fury_loop", task);
    }

    private void addFury(Player player, double amount) {
        UUID uuid = player.getUniqueId();
        double current = furyGauge.getOrDefault(uuid, 0.0);
        double newAmount = Math.min(100.0, current + amount);
        furyGauge.put(uuid, newAmount);

        // Affichage en Action Bar
        sendActionBar(player, ChatColor.LIGHT_PURPLE + "Fureur : " + String.format("%.0f", newAmount) + "%");

        // Message global à 100% (Déclenché uniquement la première fois)
        if (newAmount >= 100.0 && !globalMessageTriggered.contains(uuid)) {
            globalMessageTriggered.add(uuid);
            Bukkit.broadcastMessage(ChatColor.GOLD + "================-================");
            Bukkit.broadcastMessage(ChatColor.LIGHT_PURPLE + "" + ChatColor.BOLD + "La colère de Milim Nava a atteint son paroxysme !");
            Bukkit.broadcastMessage(ChatColor.GOLD + "=================================");
        }

        updateItemLore(player);
    }

    private void updateItemLore(Player player) {
        for (ItemStack item : player.getInventory().getContents()) {
            if (item != null && item.getType() == org.bukkit.Material.NETHER_STAR && item.hasItemMeta()) {
                ItemMeta meta = item.getItemMeta();
                if (meta != null && meta.hasDisplayName() && meta.getDisplayName().contains("Étoile de Fureur")) {
                    double current = furyGauge.getOrDefault(player.getUniqueId(), 0.0);
                    meta.setLore(Arrays.asList(
                            ChatColor.GRAY + "Clic droit : Consommer votre Fureur",
                            ChatColor.YELLOW + "Jauge actuelle : " + String.format("%.0f", current) + "%"
                    ));
                    item.setItemMeta(meta);
                }
            }
        }
    }

    @EventHandler
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        // Dégâts subis à l'épée (+5%)
        if (event.getEntity() instanceof Player) {
            Player victim = (Player) event.getEntity();
            if (main.getRoleManager().hasRole(victim.getUniqueId(), this)) {
                if (event.getDamager() instanceof Player) {
                    Player attacker = (Player) event.getDamager();
                    if (isSword(attacker.getItemInHand())) {
                        addFury(victim, 5.0);
                    }
                }
            }
        }

        // Dégâts infligés à l'épée (+1%) & Contournement de la résistance
        if (event.getDamager() instanceof Player) {
            Player attacker = (Player) event.getDamager();
            if (main.getRoleManager().hasRole(attacker.getUniqueId(), this)) {
                if (isSword(attacker.getItemInHand())) {
                    addFury(attacker, 1.0);
                }

                // Si l'effet du palier 50% est actif : Ignorer la Résistance de la cible
                if (bypassResiUntil.getOrDefault(attacker.getUniqueId(), 0L) > System.currentTimeMillis()) {
                    if (event.getEntity() instanceof Player) {
                        Player victim = (Player) event.getEntity();
                        if (victim.hasPotionEffect(PotionEffectType.DAMAGE_RESISTANCE)) {
                            int amp = getResistanceAmplifier(victim);
                            double multiplier = 1.0 / (1.0 - (0.20 * (amp + 1)));
                            event.setDamage(event.getDamage() * multiplier);
                        }
                    }
                }
            }
        }
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        if (!main.getRoleManager().hasRole(player.getUniqueId(), this)) return;
        if (event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK) return;

        ItemStack item = event.getItem();
        if (item == null || item.getType() != org.bukkit.Material.NETHER_STAR || !item.hasItemMeta()) return;

        if (item.getItemMeta().getDisplayName().contains("Étoile de Fureur")) {
            event.setCancelled(true);

            double current = furyGauge.getOrDefault(player.getUniqueId(), 0.0);

            if (current >= 100.0) {
                // Palier 100% : Drago-Nova
                furyGauge.put(player.getUniqueId(), 0.0);
                player.sendMessage(ChatColor.LIGHT_PURPLE + "Vous déchaînez le " + ChatColor.BOLD + "DRAGO-NOVA" + ChatColor.LIGHT_PURPLE + " !");

                Location loc = player.getLocation();
                player.getWorld().playSound(loc, Sound.EXPLODE, 2.0f, 0.5f);

                // Particule d'explosion géante (1.8.8)
                player.getWorld().spigot().playEffect(
                        loc,
                        org.bukkit.Effect.EXPLOSION_HUGE,
                        0, 0,
                        0.0f, 0.0f, 0.0f,
                        0.0f,
                        5,
                        32
                );

                for (Entity entity : player.getNearbyEntities(8.0, 8.0, 8.0)) {
                    if (entity instanceof Player && !entity.getUniqueId().equals(player.getUniqueId())) {
                        Player target = (Player) entity;
                        target.damage(10.0);
                        target.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 5 * 20, 0));
                        target.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 5 * 20, 1));
                        target.sendMessage(ChatColor.RED + "Vous êtes frappé par le Drago-Nova de Milim !");
                    }
                }
            } else if (current >= 75.0) {
                // Palier 75% : Résistance II (1 min)
                furyGauge.put(player.getUniqueId(), current - 75.0);
                player.addPotionEffect(new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, 60 * 20, 1, false, false));
                player.sendMessage(ChatColor.GREEN + "Vous utilisez 75% de Fureur : Résistance II activée pendant 1 minute !");

            } else if (current >= 50.0) {
                // Palier 50% : Les coups traversent la Résistance (30 sec)
                furyGauge.put(player.getUniqueId(), current - 50.0);
                bypassResiUntil.put(player.getUniqueId(), System.currentTimeMillis() + (30 * 1000));
                player.sendMessage(ChatColor.GREEN + "Vous utilisez 50% de Fureur : Vos coups pénètrent la Résistance pendant 30 secondes !");

            } else if (current >= 25.0) {
                // Palier 25% : Peut voler (20 sec)
                furyGauge.put(player.getUniqueId(), current - 25.0);

                // Force la vitesse de vol par défaut (bloque les boosts de speed)
                player.setFlySpeed(0.1f);
                player.setAllowFlight(true);
                player.setFlying(true);
                player.sendMessage(ChatColor.GREEN + "Vous utilisez 25% de Fureur : Vol activé pendant 20 secondes !");

                Bukkit.getScheduler().runTaskLater(main, () -> {
                    if (player.isOnline()) {
                        player.setFlying(false);
                        player.setAllowFlight(false);
                        player.setFlySpeed(0.1f); // Réinitialisation de sécurité
                        player.sendMessage(ChatColor.RED + "L'effet de vol est terminé.");
                    }
                }, 20 * 20L);
            } else if (current >= 10.0) {
                // Palier 10% : Résistance I (10 sec)
                furyGauge.put(player.getUniqueId(), current - 10.0);
                player.addPotionEffect(new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, 10 * 20, 0, false, false));
                player.sendMessage(ChatColor.GREEN + "Vous utilisez 10% de Fureur : Résistance I activée pendant 10 secondes !");

            } else {
                player.sendMessage(ChatColor.RED + "Vous n'avez pas assez de Fureur (10% minimum requis).");
            }

            updateItemLore(player);
        }
    }

    private boolean isSword(ItemStack item) {
        if (item == null) return false;
        String type = item.getType().name();
        return type.endsWith("_SWORD");
    }

    private int getResistanceAmplifier(Player player) {
        for (PotionEffect effect : player.getActivePotionEffects()) {
            if (effect.getType().equals(PotionEffectType.DAMAGE_RESISTANCE)) {
                return effect.getAmplifier();
            }
        }
        return 0;
    }

    @Override
    public void reset(UUID pl) {
        Player player = Bukkit.getPlayer(pl);
        super.reset(pl);
        if (player != null && player.isOnline()) {
            player.setAllowFlight(false);
            player.setFlying(false);
        }
        furyGauge.remove(pl);
        bypassResiUntil.remove(pl);
        globalMessageTriggered.remove(pl);
    }
    @EventHandler
    public void onPlayerMove(org.bukkit.event.player.PlayerMoveEvent event) {
        Player player = event.getPlayer();
        if (player.isFlying()) {
            // Si le client modifie sa flySpeed côté client
            if (player.getFlySpeed() != 0.1f) {
                player.setFlySpeed(0.1f);
            }
        }
    }
}