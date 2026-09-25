package uhc.tensuraUHC.managers;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.WorldBorder;
import uhc.tensuraUHC.TensuraUHC;

public class BorderManager {

    private final TensuraUHC main;

    public BorderManager(TensuraUHC main) {
        this.main = main;
    }

    /**
     * Initialise la bordure au centre (0,0)
     */
    public void setupInitialBorder(World world) {
        if (world == null) return;

        WorldBorder border = world.getWorldBorder();
        border.setCenter(0.0, 0.0);

        // setSize prend la largeur TOTALE (diamètre).
        border.setSize(main.getBorderInitialSize()*2);

        border.setDamageAmount(0.2);
        border.setDamageBuffer(5.0);
        border.setWarningDistance(15);
    }

    /**
     * Déclenche la réduction de la bordure
     */
    public void startBorderShrink(World world) {
        if (world == null) return;

        WorldBorder border = world.getWorldBorder();
        int finalSize = main.getBorderFinalSize();
        int durationSeconds = main.getBorderShrinkDuration();

        if (main.isBorderInstant()) {
            border.setSize(finalSize*2);
            Bukkit.broadcastMessage(ChatColor.GOLD + "[TensuraUHC] " + ChatColor.RED + "La bordure s'est réduite instantanément à " + finalSize + "x" + finalSize + " blocs !");
        } else {
            border.setSize(finalSize*2, durationSeconds);
            Bukkit.broadcastMessage(ChatColor.GOLD + "[TensuraUHC] " + ChatColor.YELLOW + "La bordure se réduit vers " + finalSize + "x" + finalSize + " blocs pendant " + (durationSeconds / 60) + " minute(s) !");
        }
    }

    public int getCurrentBorderRadius(World world) {
        if (world == null) return 0;
        return (int) (world.getWorldBorder().getSize() / 2.0);
    }

    public int getCurrentBorderSize(World world) {
        if (world == null) return 0;
        return (int) world.getWorldBorder().getSize();
    }
}