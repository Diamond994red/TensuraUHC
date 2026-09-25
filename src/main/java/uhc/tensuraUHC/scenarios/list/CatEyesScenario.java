package uhc.tensuraUHC.scenarios.list;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import uhc.tensuraUHC.TensuraUHC;
import uhc.tensuraUHC.scenarios.Scenario;

public class CatEyesScenario extends Scenario {

    public CatEyesScenario(TensuraUHC main) {
        super(main, "CatEyes", Material.EYE_OF_ENDER, "Confère l'effet Vision Nocturne permanente à tous les joueurs.");
    }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);

        if (enabled) {
            for (Player player : Bukkit.getOnlinePlayers()) {
                applyEffect(player);
            }
        } else {
            for (Player player : Bukkit.getOnlinePlayers()) {
                player.removePotionEffect(PotionEffectType.NIGHT_VISION);
            }
        }
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        if (!isEnabled()) return;
        applyEffect(event.getPlayer());
    }

    private void applyEffect(Player player) {
        // Retrait préalable puis ajout avec 999999 ticks (~14 heures) au lieu d'Integer.MAX_VALUE
        player.removePotionEffect(PotionEffectType.NIGHT_VISION);
        player.addPotionEffect(new PotionEffect(PotionEffectType.NIGHT_VISION, 999999, 0, true, false));
    }
    @EventHandler
    public void onConsume(org.bukkit.event.player.PlayerItemConsumeEvent event) {
        if (!isEnabled()) return;

        // Si le joueur boit du lait
        if (event.getItem().getType() == Material.MILK_BUCKET) {
            Player player = event.getPlayer();

            // Attend 1 tick que Bukkit retire les effets, puis réapplique Night Vision
            Bukkit.getScheduler().runTaskLater(main, () -> applyEffect(player), 1L);
        }
    }
}