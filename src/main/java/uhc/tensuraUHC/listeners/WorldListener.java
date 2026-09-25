package uhc.tensuraUHC.listeners;

import org.bukkit.Chunk;
import org.bukkit.block.Biome;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerAchievementAwardedEvent;
import org.bukkit.event.weather.WeatherChangeEvent;
import org.bukkit.event.world.ChunkLoadEvent;
import uhc.tensuraUHC.TensuraUHC;

public class WorldListener implements Listener {

    private final TensuraUHC main;

    public WorldListener(TensuraUHC main) {
        this.main = main;
    }

    @EventHandler
    public void onWeatherChange(WeatherChangeEvent event) {
        // Bloque la pluie en permanence sur le serveur
        if (event.toWeatherState()) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onChunkLoad(ChunkLoadEvent event) {
        if (!event.getWorld().getName().equals("uhc_world")) return;

        // Force l'application du biome sélectionné lors de la génération de nouveaux chunks
        if (event.isNewChunk()) {
            Chunk chunk = event.getChunk();
            Biome targetBiome = Biome.ROOFED_FOREST;

            for (int x = 0; x < 16; x++) {
                for (int z = 0; z < 16; z++) {
                    chunk.getBlock(x, 0, z).setBiome(targetBiome);
                }
            }
        }
    }

    @EventHandler
    public void onAchievementAwarded(PlayerAchievementAwardedEvent event) {
        // Bloque le spam des succès Vanilla dans le chat
        event.setCancelled(true);
    }
}