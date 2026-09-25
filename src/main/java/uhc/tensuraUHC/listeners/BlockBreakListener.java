package uhc.tensuraUHC.listeners;

import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;
import uhc.tensuraUHC.TensuraUHC;

import java.util.Collection;

public class BlockBreakListener implements Listener {

    private final TensuraUHC main;

    public BlockBreakListener(TensuraUHC main) {
        this.main = main;
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        Block block = event.getBlock();

        // 1. Gestion des variantes de Stone (Granite, Diorite, Andésite) -> Cobblestone
        if (isStoneVariant(block)) {
            if (player.getGameMode() == GameMode.CREATIVE) return;

            // On casse le bloc en ignorant ses drops vanilla
            event.setCancelled(true);
            block.setType(Material.AIR);

            // On fait drop la Cobblestone
            block.getWorld().dropItemNaturally(block.getLocation(), new ItemStack(Material.COBBLESTONE, 1));
            return;
        }

        // 2. Réduction par 2 des minerais si le Meetup est actif
        if (main.isMeetupActive()) {
            Material type = block.getType();
            if (isOre(type)) {
                event.setCancelled(true);
                Collection<ItemStack> drops = block.getDrops(player.getItemInHand());
                block.setType(Material.AIR);

                for (ItemStack drop : drops) {
                    int halvedAmount = Math.max(1, drop.getAmount() / 2);
                    drop.setAmount(halvedAmount);
                    block.getWorld().dropItemNaturally(block.getLocation(), drop);
                }
            }
        }
        if (block.getType() == Material.DIAMOND_ORE) {
            int max = main.getMaxMinedDiamonds();
            int currentMined = main.getMinedDiamonds(player);

            // -------------------------------------------------------------------------
            // CAS 1 : LIMITE DÉJÀ ATTEINTE (Substitution par de l'Or)
            // -------------------------------------------------------------------------
            if (currentMined >= max) {
                event.setCancelled(true);
                block.setType(Material.AIR); // Supprime le bloc de diamant

                // Drop du lingot d'or
                block.getWorld().dropItemNaturally(block.getLocation(), new ItemStack(Material.GOLD_INGOT, 1));

                // Subtitle + Message d'avertissement
                player.sendMessage(ChatColor.GOLD + "[TensuraUHC] " + ChatColor.RED +
                        "Limite de diamants minés atteinte (" + max + ") ! Vous recevez de l'or.");

                // Afficher un Subtitle d'avertissement (Titre vide, Subtitle rouge)
                // Timings: fadeIn = 5 ticks, stay = 30 ticks, fadeOut = 10 ticks (en 1.8.8)
                player.sendTitle("", ChatColor.RED + "Limite atteinte ! (" + max + "/" + max + " 💎) " + ChatColor.GOLD + "+1 🪙");
                return;
            }

            // -------------------------------------------------------------------------
            // CAS 2 : VALIDE - Incrémentation et affichage du Subtitle
            // -------------------------------------------------------------------------
            main.incrementMinedDiamonds(player);
            int newCount = main.getMinedDiamonds(player);

            // Subtitle vert/cyan indiquant le quota (ex: "Diamants : 3 / 10 💎")
            String subtitleText = ChatColor.AQUA + "Diamants minés : "
                    + ChatColor.GREEN + newCount + ChatColor.GRAY + " / " + ChatColor.DARK_AQUA + max + ChatColor.AQUA + " 💎";

            // Envoi du Title (Titre principal vide = "", Subtitle = texte)
            player.sendTitle("", subtitleText);
        }
    }

    // Détection robuste des variantes de roche (1.8.8 jusqu'en 1.20+)
    private boolean isStoneVariant(Block block) {
        // Test pour la 1.8.8 - 1.12 (Material.STONE + Data 1 à 6)
        if (block.getType() == Material.STONE) {
            @SuppressWarnings("deprecation")
            byte data = block.getData();
            return data >= 1 && data <= 6;
        }

        // Test pour la 1.13+ (Noms distincts)
        String name = block.getType().name();
        return name.contains("GRANITE") || name.contains("DIORITE") || name.contains("ANDESITE");
    }

    private boolean isOre(Material material) {
        return material == Material.DIAMOND_ORE || material == Material.GOLD_ORE
                || material == Material.IRON_ORE || material == Material.COAL_ORE
                || material == Material.REDSTONE_ORE || material == Material.LAPIS_ORE;
    }
}