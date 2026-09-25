package uhc.tensuraUHC.listeners;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import uhc.tensuraUHC.TensuraUHC;

public class DamageListener implements Listener {

    private final TensuraUHC main;

    public DamageListener(TensuraUHC main) {
        this.main = main;
    }

    @EventHandler
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        // Vérification et cast classique Java 8
        if (!(event.getDamager() instanceof Player)) return;
        Player attacker = (Player) event.getDamager();

        // Détection du coup critique vanilla
        boolean isCritical = attacker.getFallDistance() > 0.0F
                && !attacker.isOnGround()
                && !attacker.isInsideVehicle()
                && !attacker.hasPotionEffect(PotionEffectType.BLINDNESS)
                && attacker.getLocation().getBlock().getType() != Material.LADDER;

        // 1. CALCUL DE L'ATTAQUANT (Critique & Force)
        double vanillaCritMultiplier = isCritical ? 1.5 : 1.0;

        int strengthAmp = getEffectAmplifier(attacker, PotionEffectType.INCREASE_DAMAGE);
        double vanillaStrengthMultiplier = (strengthAmp != -1) ? 1.0 + (1.30 * (strengthAmp + 1)) : 1.0;

        // Annulation des bonus vanilla -> Obtention des dégâts bruts de l'arme
        double baseDamage = event.getDamage() / (vanillaCritMultiplier * vanillaStrengthMultiplier);

        // Application de tes propres ratios
        double customCritMultiplier = isCritical ? main.getCritDamageMultiplier() : 1.0;
        double customStrengthMultiplier = (strengthAmp != -1) ? 1.0 + (main.getStrengthMultiplier() * (strengthAmp + 1)) : 1.0;

        double damageAfterAttacker = baseDamage * customCritMultiplier * customStrengthMultiplier;

        // 2. CALCUL DE LA VICTIME (Résistance)
        double finalDamage = damageAfterAttacker;

        if (event.getEntity() instanceof Player) {
            Player victim = (Player) event.getEntity(); // Cast classique Java 8
            int resiAmp = getEffectAmplifier(victim, PotionEffectType.DAMAGE_RESISTANCE);

            if (resiAmp != -1) {
                double vanillaResistanceReduction = 1.0 - (0.20 * (resiAmp + 1));

                // Annulation de la réduction vanilla
                double damageBeforeResi = finalDamage / vanillaResistanceReduction;

                // Application de ta propre réduction
                double customResistanceReduction = 1.0 - (main.getResistanceMultiplier() * (resiAmp + 1));
                finalDamage = damageBeforeResi * customResistanceReduction;
            }
        }

        event.setDamage(finalDamage);
    }

    private int getEffectAmplifier(Player player, PotionEffectType type) {
        for (PotionEffect effect : player.getActivePotionEffects()) {
            if (effect.getType().equals(type)) {
                return effect.getAmplifier();
            }
        }
        return -1;
    }
}