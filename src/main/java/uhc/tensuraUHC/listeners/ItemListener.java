package uhc.tensuraUHC.listeners;

import org.bukkit.entity.Item;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityCombustEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import uhc.tensuraUHC.TensuraUHC;

public class ItemListener implements Listener {

    private final TensuraUHC main;

    public ItemListener(TensuraUHC main) {
        this.main = main;
    }

    // 1. Annule les dégâts de feu/lave/combustion subis par les objets tombés au sol
    @EventHandler
    public void onItemDamage(EntityDamageEvent event) {
        if (event.getEntity() instanceof Item) {
            EntityDamageEvent.DamageCause cause = event.getCause();
            if (cause == EntityDamageEvent.DamageCause.FIRE
                    || cause == EntityDamageEvent.DamageCause.FIRE_TICK
                    || cause == EntityDamageEvent.DamageCause.LAVA
                    || cause == EntityDamageEvent.DamageCause.BLOCK_EXPLOSION
                    || cause == EntityDamageEvent.DamageCause.ENTITY_EXPLOSION) {
                event.setCancelled(true);
            }
        }
    }

    // 2. Empêche l'item de prendre feu (effet visuel de combustion)
    @EventHandler
    public void onItemCombust(EntityCombustEvent event) {
        if (event.getEntity() instanceof Item) {
            event.setCancelled(true);
        }
    }
}