package uhc.tensuraUHC.roles.list.SoloCamp;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.bukkit.enchantments.Enchantment;
import uhc.tensuraUHC.TensuraUHC;
import uhc.tensuraUHC.powers.*;
import uhc.tensuraUHC.roles.Role;
import uhc.tensuraUHC.roles.list.MonstersCamp.SoeiRole;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class YuukiRole extends Role {

    private String fakeRoleName = null;
    private final List<ItemStack> claimedPowers = new ArrayList<>();

    // Instance indépendante d'Ifrit pour Yuuki
    private IfritPower stolenIfritPower = null;
    private SoeiInvisiblePower stolenSoeiPower = null;
    public YuukiRole(TensuraUHC main) {
        super(main, "Yuuki", Camp.SOLITAIRE, "Expert des arts martiaux et grand manipulateur tapi dans l'ombre, vous recevez un livre Sharpness III, " +
                "et pourrez avec /tr choisir, choisir un rôle de façade parmi tous les monstres disponibles du mode de jeu.");

        addPower("Mammon", "À chaque kill que vous faites, vous obtenez les items/pouvoirs non-utilisés de vos victimes (récupérables avec /tr claim). " +
                "S'il n'en avait pas, vous récupérerez 1 demi-coeur permanent.");


    }
    @Override
    public void giveRole(Player player) {
        reset(player.getUniqueId());

        getItemsToGive().clear();

        // --- Livre Sharpness III ---
        ItemStack sharp = new ItemStack(Material.ENCHANTED_BOOK);
        EnchantmentStorageMeta meta = (EnchantmentStorageMeta) sharp.getItemMeta();

        if (meta != null) {
            meta.addStoredEnchant(Enchantment.DAMAGE_ALL, 3, true);
            sharp.setItemMeta(meta);
        }

        addEnchantBypass(Enchantment.DAMAGE_ALL, 4);
        addItem(sharp);
        super.giveRole(player);
    }

    // --- POUVOIR MAMMON (Vol de pouvoirs au kill) ---
    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player victim = event.getEntity();
        Player killer = victim.getKiller();

        // Vérifie si le tueur existe et qu'il s'agit bien de Yuuki
        if (killer == null || !main.getRoleManager().hasRole(killer.getUniqueId(), this)) return;

        Role victimRole = main.getRoleManager().getPlayerRole(victim.getUniqueId());
        if (victimRole == null) return;

        killer.sendMessage(ChatColor.GOLD + "[Yuuki - Mammon] " + ChatColor.GREEN + "Vous avez volé les capacités/items de " + victim.getName() + " !");

        if (victimRole instanceof ShizuRole) {
            this.stolenIfritPower = new IfritPower(main);
            claimedPowers.add(IfritPower.createItem());
            killer.sendMessage(ChatColor.GREEN + "Tapez /tr claim pour les récupérer.");
        }
        else if (victimRole instanceof SoeiRole) {
            this.stolenSoeiPower = new SoeiInvisiblePower(main);
            this.stolenSoeiPower.activate(killer);
            killer.sendMessage(ChatColor.GREEN + "Vous pouvez maintenant vous mettre invisible en enlevant votre armure.");
        }
        else {
            // Gains de santé passifs si aucun pouvoir volé
            killer.setMaxHealth(killer.getMaxHealth() + 1.0);
        }
    }

    // --- GESTION DES INTERACTION DU POUVOIR VOLÉ ---
    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        if (!main.getRoleManager().hasRole(player.getUniqueId(), this) || stolenIfritPower == null) return;
        if (event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK) return;

        ItemStack item = event.getItem();
        if (item == null || !item.hasItemMeta()) return;

        // Activation de l'Ifrit de Yuuki
        if (item.getType() == Material.NETHER_STAR && (ChatColor.RED + "Ifrit").equals(item.getItemMeta().getDisplayName())) {
            event.setCancelled(true);
            stolenIfritPower.activate(player);
        }
    }

    // --- GETTERS & SETTERS ---

    public String getFakeRoleName() {
        return fakeRoleName;
    }

    public void setFakeRoleName(String fakeRoleName) {
        this.fakeRoleName = fakeRoleName;
    }

    public List<ItemStack> getClaimedPowers() {
        return claimedPowers;
    }

    @Override
    public void reset(UUID pl) {
        Player player = Bukkit.getPlayer(pl);
        super.reset(pl);

        // Réinitialisation d'Ifrit s'il a été volé
        if (stolenIfritPower != null) {
            stolenIfritPower.reset(player);
            stolenIfritPower = null;
        }
        if (stolenSoeiPower != null) {
            stolenSoeiPower.reset(player);
            stolenSoeiPower = null;
        }

        this.setFakeRoleName(null);
        this.getClaimedPowers().clear();
    }

    @Override
    public void FakeRoleMessage(Player sender) {
        sender.sendMessage("Votre role de façade : " + ChatColor.GOLD + getFakeRoleName());
    }
}