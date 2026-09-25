package uhc.tensuraUHC.managers;

import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.generator.BlockPopulator;
import org.bukkit.scheduler.BukkitRunnable;
import uhc.tensuraUHC.TensuraUHC;

import java.io.File;
import java.util.Random;

public class WorldManager {

    private final TensuraUHC main;
    private World gameWorld;

    public WorldManager(TensuraUHC main) {
        this.main = main;
    }

    public World createGameWorld() {
        String worldName = "uhc_world";

        World existingWorld = Bukkit.getWorld(worldName);
        if (existingWorld != null) {
            Bukkit.unloadWorld(existingWorld, false);
        }

        File worldFolder = new File(Bukkit.getWorldContainer(), worldName);
        if (worldFolder.exists()) {
            deleteFolder(worldFolder);
        }

        // --- RENDER & VARIABLE MAP FOR JSON CONFIG ---
        int caveChance = Math.max(1, (100 - main.getCaveSizePercent()) / 10);
        int coalCount = (int) Math.round(20 * (main.getCoalPercent() / 100.0));
        int ironCount = (int) Math.round(20 * (main.getIronPercent() / 100.0));
        int goldCount = (int) Math.round(2 * (main.getGoldPercent() / 100.0));
        int redstoneCount = (int) Math.round(8 * (main.getRedstonePercent() / 100.0));
        int diamondCount = (int) Math.round(1 * (main.getDiamondPercent() / 100.0));
        int lapisCount = (int) Math.round(1 * (main.getLapisPercent() / 100.0));

// Si le pourcentage est > 0 mais que le calcul donne 0, on force à 1
        if (main.getCoalPercent() > 0 && coalCount == 0) coalCount = 1;
        if (main.getIronPercent() > 0 && ironCount == 0) ironCount = 1;
        if (main.getGoldPercent() > 0 && goldCount == 0) goldCount = 1;
        if (main.getRedstonePercent() > 0 && redstoneCount == 0) redstoneCount = 1;
        if (main.getDiamondPercent() > 0 && diamondCount == 0) diamondCount = 1;
        if (main.getLapisPercent() > 0 && lapisCount == 0) lapisCount = 1;
        int biomeId = -1; // -1 pour garder les biomes naturels variés (ou modifie si fixe)

        String customSettings = "{"
                + "\"coordinateScale\":684.412,\"heightScale\":684.412,\"lowerLimitScale\":512.0,\"upperLimitScale\":512.0,"
                + "\"depthNoiseScaleX\":200.0,\"depthNoiseScaleZ\":200.0,\"depthNoiseScaleExponent\":0.5,\"mainNoiseScaleX\":80.0,"
                + "\"mainNoiseScaleY\":160.0,\"mainNoiseScaleZ\":80.0,\"baseSize\":8.5,\"stretchY\":12.0,\"biomeDepthWeight\":1.0,"
                + "\"biomeDepthOffset\":0.0,\"biomeScaleWeight\":1.0,\"biomeScaleOffset\":0.0,\"seaLevel\":63,"
                + "\"useCaves\":true,\"caveChance\":" + caveChance + ",\"useDungeons\":true,\"dungeonChance\":8,"
                + "\"useStrongholds\":true,\"useVillages\":true,\"useMineShafts\":true,\"useTemples\":true,"
                + "\"useMonasteries\":false,\"useOceanMonuments\":true,\"useRavines\":true,\"useWaterLakes\":true,"
                + "\"waterLakeChance\":4,\"useLavaLakes\":true,\"lavaLakeChance\":80,\"useLavaOceans\":false,"
                + "\"fixedBiome\":" + biomeId + ",\"useFixedBiome\":false,\"biomeSize\":4,\"riverSize\":4,"
                + "\"dirtSize\":33,\"dirtCount\":10,\"dirtMinHeight\":0,\"dirtMaxHeight\":256,"
                + "\"gravelSize\":33,\"gravelCount\":8,\"gravelMinHeight\":0,\"gravelMaxHeight\":256,"
                + "\"graniteSize\":33,\"graniteCount\":10,\"graniteMinHeight\":0,\"graniteMaxHeight\":80,"
                + "\"dioriteSize\":33,\"dioriteCount\":10,\"dioriteMinHeight\":0,\"dioriteMaxHeight\":80,"
                + "\"andesiteSize\":33,\"andesiteCount\":10,\"andesiteMinHeight\":0,\"andesiteMaxHeight\":80,"
                + "\"coalSize\":17,\"coalCount\":" + coalCount + ",\"coalMinHeight\":0,\"coalMaxHeight\":128,"
                + "\"ironSize\":9,\"ironCount\":" + ironCount + ",\"ironMinHeight\":0,\"ironMaxHeight\":64,"
                + "\"goldSize\":9,\"goldCount\":" + goldCount + ",\"goldMinHeight\":0,\"goldMaxHeight\":32,"
                + "\"redstoneSize\":8,\"redstoneCount\":" + redstoneCount + ",\"redstoneMinHeight\":0,\"redstoneMaxHeight\":16,"
                + "\"diamondSize\":8,\"diamondCount\":" + diamondCount + ",\"diamondMinHeight\":0,\"diamondMaxHeight\":16,"
                + "\"lapisSize\":7,\"lapisCount\":" + lapisCount + ",\"lapisCenterHeight\":16,\"lapisSpread\":16"
                + "}";

        WorldCreator creator = new WorldCreator(worldName);
        creator.environment(World.Environment.NORMAL);
        creator.type(WorldType.CUSTOMIZED);
        creator.generatorSettings(customSettings);
        creator.seed(new Random().nextLong());

        gameWorld = creator.createWorld();
        if (gameWorld != null) {

            gameWorld.setGameRuleValue("naturalRegeneration", "false");
            gameWorld.setGameRuleValue("doDaylightCycle", "true");
            gameWorld.setTime(1000);

            WorldBorder border = gameWorld.getWorldBorder();
            border.setCenter(0, 0);
            border.setSize(1000);
        }
        return gameWorld;
    }

    private boolean deleteFolder(File path) {
        if (path.exists()) {
            File[] files = path.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (file.isDirectory()) deleteFolder(file);
                    else file.delete();
                }
            }
        }
        return path.delete();
    }

    public void pregenerateWorld(World world, int radius, Runnable onComplete) {
        int minX = -radius >> 4;
        int maxX = radius >> 4;
        int minZ = -radius >> 4;
        int maxZ = radius >> 4;
        int totalChunks = (maxX - minX + 1) * (maxZ - minZ + 1);

        new BukkitRunnable() {
            int currentX = minX;
            int currentZ = minZ;
            int generated = 0;

            @Override
            public void run() {
                int chunksPerTick = 16;
                for (int i = 0; i < chunksPerTick; i++) {
                    if (currentX > maxX) {
                        currentX = minX;
                        currentZ++;
                    }

                    if (currentZ > maxZ) {
                        cancel();
                        if (onComplete != null) onComplete.run();
                        return;
                    }

                    if (!world.isChunkLoaded(currentX, currentZ)) {
                        world.loadChunk(currentX, currentZ, true);
                    }

                    // Unload et sauvegarde propre du chunk sans fuite mémoire
                    world.unloadChunk(currentX, currentZ, true, true);

                    generated++;
                    currentX++;
                }

                if (generated % 200 == 0 || generated >= totalChunks) {
                    int percent = Math.min(100, (generated * 100) / totalChunks);
                    Bukkit.broadcastMessage(ChatColor.GOLD + "[TensuraUHC] " + ChatColor.YELLOW + "Pré-génération du monde : " + percent + "%");
                }
            }
        }.runTaskTimer(main, 1L, 1L);
    }

    public World getGameWorld() {
        return gameWorld;
    }
}