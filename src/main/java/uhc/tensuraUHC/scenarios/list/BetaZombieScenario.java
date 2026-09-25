package uhc.tensuraUHC.scenarios.list;

import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.ItemStack;
import uhc.tensuraUHC.TensuraUHC;
import uhc.tensuraUHC.scenarios.Scenario;

public class BetaZombieScenario extends Scenario {

    public BetaZombieScenario(TensuraUHC main) {
        super(main, "BetaZombie", Material.FEATHER, "Les zombies font uniquement tomber des plumes à leur mort (style Beta).");
    }

    @EventHandler
    public void onEntityDeath(EntityDeathEvent event) {
        if (!isEnabled()) return;

        // Vérifie s'il s'agit d'un Zombie
        if (event.getEntityType() == EntityType.ZOMBIE) {
            // Vide les loots de base (chair perimée, etc.)
            event.getDrops().clear();

            // Ajoute de 1 à 2 plumes au loot
            event.getDrops().add(new ItemStack(Material.FEATHER, 1));
        }
    }
}