package uhc.tensuraUHC.powers;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scoreboard.NameTagVisibility;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;
import uhc.tensuraUHC.TensuraUHC;
import java.util.ArrayList;
import java.util.List;

public class SoeiInvisiblePower {

    private final TensuraUHC main;
    private BukkitTask invisibilityTask;
    static List<Player> players = new ArrayList<>();

    public SoeiInvisiblePower(TensuraUHC main) {
        this.main = main;
    }

    public static List<Player> getPlayers() {
        return players;
    }

    public static ItemStack createItem() {
        return null;
    }

    public void activate(Player player) {
        players.add(player);
        startInvisibilityCheck(player);
    }

    private void startInvisibilityCheck(Player player) {
        invisibilityTask = new BukkitRunnable() {
            boolean added = false;
            boolean removed = true;

            @Override
            public void run() {
                if (!player.isOnline()) return;

                boolean hasArmor = false;
                for (ItemStack armorPiece : player.getEquipment().getArmorContents()) {
                    if (armorPiece != null && armorPiece.getType() != Material.AIR) {
                        hasArmor = true;
                        break;
                    }
                }

                if (!hasArmor) {
                    if (!added) {
                        added = true;
                        removed = false;

                        // 1. Appliquer l'effet d'invisibilité (sans particules)
                        player.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, 99999 * 20, 0, false, false));
                        main.getNoFallPlayer().add(player.getUniqueId());

                        // 2. Masquer le nametag via une Team globale
                        hideNametag(player);
                    }
                } else {
                    if (!removed) {
                        added = false;
                        removed = true;

                        // 1. Retirer l'effet et la protection no-fall
                        if (player.hasPotionEffect(PotionEffectType.INVISIBILITY)) {
                            player.removePotionEffect(PotionEffectType.INVISIBILITY);
                        }
                        main.getNoFallPlayer().remove(player.getUniqueId());

                        // 2. Restaurer le nametag
                        showNametag(player);
                    }
                }
            }
        }.runTaskTimer(main, 0L, 20L);
    }

    // --- GESTION DU NAMETAG ---

    private void hideNametag(Player player) {
        Scoreboard board = Bukkit.getScoreboardManager().getMainScoreboard();
        Team team = board.getTeam("soei_invis");
        if (team == null) {
            team = board.registerNewTeam("soei_invis");
            // Méthode spécifique à la 1.8.8 :
            team.setNameTagVisibility(NameTagVisibility.NEVER);
        }
        team.addEntry(player.getName());
    }

    private void showNametag(Player player) {
        Scoreboard board = Bukkit.getScoreboardManager().getMainScoreboard();
        Team team = board.getTeam("soei_invis");
        if (team != null && team.hasEntry(player.getName())) {
            team.removeEntry(player.getName());
        }
    }

    public void reset(Player player) {
        if (invisibilityTask != null) {
            invisibilityTask.cancel();
            invisibilityTask = null;
        }
        if (player != null && player.isOnline()) {
            player.removePotionEffect(PotionEffectType.INVISIBILITY);
            showNametag(player);
        }
        players.clear();
    }
}