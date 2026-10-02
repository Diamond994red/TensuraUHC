package uhc.tensuraUHC.roles.list.ClownsCamp;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import uhc.tensuraUHC.TensuraUHC;
import uhc.tensuraUHC.roles.Role;

import java.util.*;

public class ClaymanRole extends Role implements Listener {

    private final Map<UUID, Long> targetCooldowns = new HashMap<>();
    private final Map<UUID, Integer> clownKillReductions = new HashMap<>(); // Stocke la réduction de conversion appliquée
    private UUID currentTargetUUID = null;

    public ClaymanRole(TensuraUHC main) {
        super(main, "Clayman", Camp.CLOWNS,
                "Vous êtes le leader fantoche des clowns. Vous désignez des cibles à éliminer pour étendre votre influence et renforcer vos sbires.");

        addPassiveEffect(PotionEffectType.INCREASE_DAMAGE, 0); // Force I
        addPower("Marionnettiste (/tr clown <pseudo>)", "Désigne une cible toutes les 20 minutes. Tous les Clowns infligent +20% de dégâts et subissent -10% de dégâts face à elle.");
        addPower("Chat des Clowns (/tr clownchat <message>)", "Communiquez discrètement avec l'ensemble des membres du camp des Clowns.");
        addPower("Règne de Terreur", "Obtenez un effet de Résistance I dès que vous atteignez 3 éliminations.");
    }

    @Override
    public void FakeRoleMessage(Player sender) {
        sender.sendMessage(ChatColor.DARK_BLUE + "[Clowns] " + ChatColor.WHITE + "Vos sbires clowns :");
        for (Player p : Bukkit.getOnlinePlayers()) {
            Role role = main.getRoleManager().getRole(p.getUniqueId());
            if (role != null && role.getCamp() == Camp.CLOWNS) {
                sender.sendMessage(ChatColor.DARK_BLUE + "- " + ChatColor.AQUA + p.getName() + " (" + role.getName() + ")");
            }
        }
    }

    @Override
    public void giveRole(Player player) {
        reset(player.getUniqueId());
        getItemsToGive().clear();
        super.giveRole(player);
        checkKillsForEffects(player);
    }

    @EventHandler
    public void onCommand(PlayerCommandPreprocessEvent event) {
        Player player = event.getPlayer();
        String message = event.getMessage();
        String[] args = message.split(" ");

        // Gestion de /tr clown <pseudo>
        if (args[0].equalsIgnoreCase("/tr") && args.length >= 2 && args[1].equalsIgnoreCase("clown")) {
            event.setCancelled(true);

            if (!main.getRoleManager().hasRole(player.getUniqueId(), this)) {
                player.sendMessage(ChatColor.RED + "Vous n'avez pas la permission d'utiliser cette commande.");
                return;
            }

            if (args.length < 3) {
                player.sendMessage(ChatColor.RED + "Utilisation : /tr clown <joueur>");
                return;
            }

            long currentTime = System.currentTimeMillis();
            long lastUsed = targetCooldowns.getOrDefault(player.getUniqueId(), 0L);
            long cooldownTime = 20 * 60 * 1000; // 20 minutes

            if (currentTime - lastUsed < cooldownTime) {
                long remainingSeconds = (cooldownTime - (currentTime - lastUsed)) / 1000;
                player.sendMessage(ChatColor.RED + "Vous devez attendre " + formatTime((int) remainingSeconds) + " avant de re-désigner une cible.");
                return;
            }

            Player target = Bukkit.getPlayer(args[2]);
            if (target == null || !target.isOnline()) {
                player.sendMessage(ChatColor.RED + "Joueur introuvable.");
                return;
            }

            if (target.getUniqueId().equals(player.getUniqueId())) {
                player.sendMessage(ChatColor.RED + "Vous ne pouvez pas vous désigner vous-même.");
                return;
            }

            currentTargetUUID = target.getUniqueId();
            targetCooldowns.put(player.getUniqueId(), currentTime);

            broadcastToClowns(ChatColor.DARK_BLUE + "[Clowns] " + ChatColor.GOLD + player.getName() +
                    " a désigné " + ChatColor.RED + target.getName() + ChatColor.GOLD + " comme cible prioritaire !");
            return;
        }

        // Gestion de /tr clownchat <message>
        if (args[0].equalsIgnoreCase("/tr") && args.length >= 2 && args[1].equalsIgnoreCase("clownchat")) {
            event.setCancelled(true);

            Role role = main.getRoleManager().getRole(player.getUniqueId());
            if (role == null || role.getCamp() != Camp.CLOWNS) {
                player.sendMessage(ChatColor.RED + "Vous n'êtes pas un Clown pour utiliser ce chat.");
                return;
            }

            if (args.length < 3) {
                player.sendMessage(ChatColor.RED + "Utilisation : /tr clownchat <message>");
                return;
            }

            String clownMsg = String.join(" ", Arrays.copyOfRange(args, 2, args.length));
            broadcastToClowns(ChatColor.DARK_BLUE + "[Chat Clown] " + ChatColor.AQUA + player.getName() + " : " + ChatColor.WHITE + clownMsg);
        }
    }

    @EventHandler
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        if (currentTargetUUID == null) return;

        // Augmentation des dégâts infligés à la cible par les Clowns (+20%)
        if (event.getEntity() instanceof Player && event.getDamager() instanceof Player) {
            Player victim = (Player) event.getEntity();
            Player attacker = (Player) event.getDamager();

            if (victim.getUniqueId().equals(currentTargetUUID)) {
                Role attackerRole = main.getRoleManager().getRole(attacker.getUniqueId());
                if (attackerRole != null && attackerRole.getCamp() == Camp.CLOWNS) {
                    event.setDamage(event.getDamage() * 1.20);
                }
            }

            // Réduction des dégâts reçus de la cible par les Clowns (-10%)
            if (attacker.getUniqueId().equals(currentTargetUUID)) {
                Role victimRole = main.getRoleManager().getRole(victim.getUniqueId());
                if (victimRole != null && victimRole.getCamp() == Camp.CLOWNS) {
                    event.setDamage(event.getDamage() * 0.90);
                }
            }
        }
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player victim = event.getEntity();
        Player killer = victim.getKiller();

        if (killer == null) return;

        Role killerRole = main.getRoleManager().getRole(killer.getUniqueId());

        // Actualisation des effets passifs pour Clayman s'il fait des kills
        if (killerRole instanceof ClaymanRole) {
            checkKillsForEffects(killer);
        }

        // Vérification de la conversion de la cible désignée
        if (currentTargetUUID != null && victim.getUniqueId().equals(currentTargetUUID)) {
            if (killerRole != null && killerRole.getCamp() == Camp.CLOWNS) {

                boolean isClayman = killerRole instanceof ClaymanRole;
                int baseChance = isClayman ? 100 : 50;

                int currentReduction = clownKillReductions.getOrDefault(killer.getUniqueId(), 0);
                int finalChance = Math.max(0, baseChance - currentReduction);

                // Application du malus pour les futurs kills (-10% pour Clayman, -5% pour un autre clown)
                clownKillReductions.put(killer.getUniqueId(), currentReduction + (isClayman ? 10 : 5));

                // Tirage de la probabilité
                if (new Random().nextInt(100) < finalChance) {
                    Role victimRole = main.getRoleManager().getRole(victim.getUniqueId());
                    if (victimRole != null) {
                        victimRole.setCamp(Camp.CLOWNS);
                        victim.sendMessage(ChatColor.DARK_BLUE + "À votre mort, l'emprise des Clowns prend le dessus. Vous rejoignez le camp des CLOWNS !");
                        broadcastToClowns(ChatColor.DARK_BLUE + "[Clowns] " + ChatColor.AQUA + victim.getName() + " a rejoint les Clowns !");
                    }
                } else {
                    broadcastToClowns(ChatColor.DARK_BLUE + "[Clowns] " + ChatColor.RED + "La tentative de conversion de " + victim.getName() + " a échoué (" + finalChance + "% de chances).");
                }

                currentTargetUUID = null; // Réinitialisation de la cible
            }
        }
    }

    private void checkKillsForEffects(Player player) {
        if (main.getKills().getOrDefault(player.getUniqueId(), 0) >= 3) {
            player.addPotionEffect(new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, 99999 * 20, 0, false, false));
        }
    }

    private void broadcastToClowns(String message) {
        for (Player p : Bukkit.getOnlinePlayers()) {
            Role role = main.getRoleManager().getRole(p.getUniqueId());
            if (role != null && role.getCamp() == Camp.CLOWNS) {
                p.sendMessage(message);
            }
        }
    }

    @Override
    public void reset(UUID pl) {
        Player player = Bukkit.getPlayer(pl);
        super.reset(pl);
        if (player != null) {
            player.removePotionEffect(PotionEffectType.DAMAGE_RESISTANCE);
        }
        targetCooldowns.remove(pl);
        clownKillReductions.remove(pl);
        currentTargetUUID = null;
    }
}