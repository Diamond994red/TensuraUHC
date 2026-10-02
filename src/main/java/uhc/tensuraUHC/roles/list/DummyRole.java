package uhc.tensuraUHC.roles.list;

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

public class DummyRole extends Role {


    public DummyRole(TensuraUHC main) {
        super(main, "Dummy", Camp.OCTAGRAMME, "");
    }

    @Override
    public void FakeRoleMessage(Player sender) {

    }

    @Override
    public void giveRole(Player player) {
        reset(player.getUniqueId());

        getItemsToGive().clear();

        super.giveRole(player);
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player victim = event.getEntity();
        Player killer = victim.getKiller();
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        if (!main.getRoleManager().hasRole(player.getUniqueId(), this)) return;
        if (event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK) return;

        ItemStack item = event.getItem();
        if (item == null || !item.hasItemMeta()) return;
    }

    @Override
    public void reset(UUID pl) {
        Player player = Bukkit.getPlayer(pl);
        super.reset(pl);
    }
}