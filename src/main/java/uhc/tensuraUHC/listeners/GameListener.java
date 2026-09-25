package uhc.tensuraUHC.listeners;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import uhc.tensuraUHC.TensuraUHC;

public class GameListener implements Listener {

    private final TensuraUHC main;

    public GameListener(TensuraUHC main) {
        this.main = main;
    }

    @EventHandler
    public void onCreatureSpawn(CreatureSpawnEvent event) {
        if (!main.isGameStarted()) return;

        // Bloque l'apparition naturelle de monstres/animaux si souhaité
        if (event.getSpawnReason() == CreatureSpawnEvent.SpawnReason.NATURAL) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        // Bloque le PvP tant qu'il n'est pas activé globalement dans les règles UHC
        if (event.getEntity() instanceof Player && event.getDamager() instanceof Player) {
            if (!main.isPvpEnabled()) {
                event.setCancelled(true);
                ((Player) event.getDamager()).sendMessage(ChatColor.RED + "Le PvP est désactivé pour le moment !");
            }
        }
    }

    @EventHandler
    public void onEntityDamage(EntityDamageEvent event) {
        if (event.getEntity() instanceof Player) {
            Player player = (Player) event.getEntity();

            // 1. Invulnérabilité absolue tant que la partie n'est pas lancée
            if (!main.isGameStarted()) {
                event.setCancelled(true);
                return;
            }

            // 2. Annulation des dégâts temporaires
            if (main.getNoDamagePlayers().contains(player.getUniqueId())) {
                event.setCancelled(true);
            }

            if (main.getNoFallPlayer().contains(player.getUniqueId()) && EntityDamageEvent.DamageCause.FALL == event.getCause())
            {
                event.setCancelled(true);
            }
        }
    }
}