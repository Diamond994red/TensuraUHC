package uhc.tensuraUHC.scenarios.list;

import org.bukkit.Material;
import org.bukkit.entity.Creeper;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import uhc.tensuraUHC.TensuraUHC;
import uhc.tensuraUHC.scenarios.Scenario;

public class SafeMinerScenario extends Scenario {

    public SafeMinerScenario(TensuraUHC main) {
        super(main, "SafeMiner", Material.IRON_PICKAXE, "Annule les dégâts de chute, feu, lave, suffocation et Creepers.");
    }

    @EventHandler
    public void onEntityDamage(EntityDamageEvent event) {
        if (!isEnabled()) return;
        if (!(event.getEntity() instanceof Player)) return;

        EntityDamageEvent.DamageCause cause = event.getCause();

        // Annule les dégâts de mine classiques (chute, feu, lave, suffocation)
        if (cause == EntityDamageEvent.DamageCause.FALL ||
                cause == EntityDamageEvent.DamageCause.LAVA ||
                cause == EntityDamageEvent.DamageCause.FIRE ||
                cause == EntityDamageEvent.DamageCause.FIRE_TICK ||
                cause == EntityDamageEvent.DamageCause.SUFFOCATION) {

            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        if (!isEnabled()) return;
        if (!(event.getEntity() instanceof Player)) return;

        // Annule les dégâts causés par les explosions de Creepers
        if (event.getDamager() instanceof Creeper) {
            event.setCancelled(true);
        }
    }
}