package uhc.tensuraUHC.roles;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import uhc.tensuraUHC.TensuraUHC;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class Role implements Listener {

    public abstract void FakeRoleMessage(Player sender);

    public enum Camp {
        MONSTRES(ChatColor.GREEN + "les monstres de Jura", ChatColor.GREEN),
        HUMAINS(ChatColor.AQUA + "le royaume de Falmuth", ChatColor.AQUA),
        SOLITAIRE(ChatColor.GOLD + "Solitaire", ChatColor.GOLD),
        OCTAGRAMME(ChatColor.DARK_PURPLE + "les rois démons", ChatColor.DARK_PURPLE),
        LIMULE(ChatColor.DARK_GREEN + "Limule et ses alliés (hors fédération de Jura)", ChatColor.DARK_GREEN),
        SHIZUE(ChatColor.LIGHT_PURPLE + "Votre âme soeur", ChatColor.LIGHT_PURPLE),
        CLOWNS(ChatColor.DARK_BLUE + "les clowns", ChatColor.DARK_BLUE);
        private final String displayName;
        private final ChatColor color;

        Camp(String displayName, ChatColor color) {
            this.displayName = displayName;
            this.color = color;
        }

        public String getDisplayName() {
            return displayName;
        }
        public ChatColor getColor() { return color; }
    }

    protected final TensuraUHC main;
    private final String name;
    private Camp camp;
    private Camp initialCamp;
    private final String description;
    private final List<PotionEffect> passiveEffects;
    private final List<String> powers;
    private final List<ItemStack> itemsToGive;
    private final Map<Enchantment, Integer> enchantBypasses;
    private int count = 0; // 0 = désactivé, 1+ = nombre d'exemplaires

    public Role(TensuraUHC main, String name, Camp camp, String description) {
        this.main = main;
        this.name = name;
        this.camp = camp;
        this.initialCamp = camp;
        this.description = description;
        this.passiveEffects = new ArrayList<>();
        this.powers = new ArrayList<>();
        this.itemsToGive = new ArrayList<>();
        this.enchantBypasses = new HashMap<>();
    }

    // Méthode appelée lors de l'attribution du rôle
    public void giveRole(Player player) {
        GetRoleDescription(player);

        // Application des effets passifs
        for (PotionEffect effect : passiveEffects) {
            player.addPotionEffect(effect);
        }

        // Distribution des items dans l'inventaire (ou au sol si plein)
        for (ItemStack item : itemsToGive) {
            player.getInventory().addItem(item).values().forEach(
                    overflow -> player.getWorld().dropItemNaturally(player.getLocation(), overflow)
            );
        }
    }

    // Méthodes utilitaires pour construire le rôle
    protected void addPassiveEffect(PotionEffectType type, int amplifier) {
        this.passiveEffects.add(new PotionEffect(type, 99999 * 20, amplifier, false, false));
    }

    protected void addPower(String powerName, String powerDescription) {
        this.powers.add(powerName + " : " + ChatColor.WHITE + powerDescription);
    }

    protected void addItem(ItemStack item) {
        this.itemsToGive.add(item);
    }

    public Role addEnchantBypass(Enchantment enchantment, int maxLevel) {
        this.enchantBypasses.put(enchantment, maxLevel);
        return this;
    }

    // Getters et Setters
    public String getName() { return name; }
    public Camp getCamp() { return camp; }
    public Camp getInitialCamp() { return initialCamp; }
    public void setCamp(Camp camp) { this.camp = camp; }
    public String getDescription() { return description; }
    public List<PotionEffect> getPassiveEffects() { return passiveEffects; }
    public List<String> getPowers() { return powers; }
    public List<ItemStack> getItemsToGive() { return itemsToGive; }
    public Map<Enchantment, Integer> getEnchantBypasses() { return enchantBypasses; }

    public int getCount() { return count; }
    public void setCount(int count) { this.count = Math.max(0, count); }
    public boolean isEnabled() { return count > 0; }
    public void incrementCount() { this.count++; }
    public void decrementCount() { if (this.count > 0) this.count--; }

    public void sendActionBar(Player player, String message) {
        try {
            Object nmsPlayer = player.getClass().getMethod("getHandle").invoke(player);
            Object connection = nmsPlayer.getClass().getField("playerConnection").get(nmsPlayer);

            Class<?> chatComponentClass = Class.forName("net.minecraft.server." + getNMSVersion() + ".IChatBaseComponent$ChatSerializer");
            Class<?> chatComponent = Class.forName("net.minecraft.server." + getNMSVersion() + ".IChatBaseComponent");
            Class<?> packetClass = Class.forName("net.minecraft.server." + getNMSVersion() + ".PacketPlayOutChat");

            String cleanMessage = message.replace("&", "§");
            Object serializedJson = chatComponentClass.getMethod("a", String.class).invoke(null, "{\"text\":\"" + cleanMessage + "\"}");

            Object packet = packetClass.getConstructor(chatComponent, byte.class).newInstance(serializedJson, (byte) 2);

            connection.getClass().getMethod("sendPacket", Class.forName("net.minecraft.server." + getNMSVersion() + ".Packet")).invoke(connection, packet);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String getNMSVersion() {
        return Bukkit.getServer().getClass().getPackage().getName().split("\\.")[3];
    }

    private String getPotionEffectName(PotionEffectType type) {
        if (type.equals(PotionEffectType.SPEED)) return ChatColor.AQUA + "Speed";
        if (type.equals(PotionEffectType.INCREASE_DAMAGE)) return ChatColor.DARK_RED + "Force";
        if (type.equals(PotionEffectType.JUMP)) return ChatColor.GREEN + "Jump boost";
        if (type.equals(PotionEffectType.DAMAGE_RESISTANCE)) return ChatColor.GRAY + "Resistance";
        if (type.equals(PotionEffectType.FIRE_RESISTANCE)) return ChatColor.GOLD + "Fire Resistance";
        if (type.equals(PotionEffectType.INVISIBILITY)) return ChatColor.DARK_AQUA + "Invisibilité";
        if (type.equals(PotionEffectType.WEAKNESS)) return ChatColor.GRAY + "Faiblesse";
        if (type.equals(PotionEffectType.HEALTH_BOOST)) return ChatColor.RED + "Coeurs supplémentaires";
        return type.getName();
    }

    private String toRoman(int number) {
        switch (number) {
            case 1: return "I";
            case 2: return "II";
            case 3: return "III";
            case 4: return "IV";
            case 5: return "V";
            default: return String.valueOf(number);
        }
    }
    public String formatTime(int totalSeconds) {
        int minutes = totalSeconds / 60;
        int seconds = totalSeconds % 60;
        return (seconds > 0) ? String.format("%d min %d s", minutes, seconds) : minutes + " min";
    }
    public void reset(Player player) {
        if (player != null && player.isOnline()) {
            player.setMaxHealth(20.0);
            player.setHealth(Math.min(player.getHealth(), 20.0));
            for (PotionEffect effect : passiveEffects) {
                player.removePotionEffect(effect.getType());
            }
        }
    }

    public void GetRoleDescription(Player player) {
        player.sendMessage(ChatColor.GOLD + "================-================");
        player.sendMessage(ChatColor.YELLOW + "Vous êtes : " + ChatColor.BOLD + (camp == Camp.LIMULE ? ChatColor.DARK_GREEN : camp == Camp.SHIZUE ? ChatColor.LIGHT_PURPLE : camp == Camp.MONSTRES ? ChatColor.GREEN : camp == Camp.HUMAINS ? ChatColor.AQUA : camp == Camp.SOLITAIRE ? ChatColor.GOLD : ChatColor.DARK_PURPLE) + name);
        player.sendMessage("Vous devez gagner" + (camp == Camp.SOLITAIRE ? ChatColor.GOLD + " Seul" : " avec " +
                (camp == Camp.LIMULE ? ChatColor.DARK_GREEN :
                        camp == Camp.SHIZUE ? ChatColor.LIGHT_PURPLE :
                                camp == Camp.MONSTRES ? ChatColor.GREEN :
                                        camp == Camp.HUMAINS ? ChatColor.AQUA : ChatColor.DARK_PURPLE) + camp.getDisplayName()));
        player.sendMessage("");
        if (!passiveEffects.isEmpty()) {
            List<String> effectTexts = new ArrayList<>();

            for (PotionEffect effect : passiveEffects) {
                String effectName = getPotionEffectName(effect.getType());
                int level = effect.getAmplifier() + 1;
                effectTexts.add(ChatColor.WHITE + effectName + " " + toRoman(level));
            }

            String formattedEffects = String.join(ChatColor.GRAY + ", ", effectTexts);
            player.sendMessage("Pour vous aider, vous avez les effets " + formattedEffects);
        }
        player.sendMessage(ChatColor.AQUA + "--- Description ---");
        player.sendMessage(ChatColor.GRAY + description);

        if (!powers.isEmpty()) {
            player.sendMessage("");
            player.sendMessage(ChatColor.GREEN + "--- Pouvoirs ---");
            for (String power : powers) {
                player.sendMessage(ChatColor.GREEN + "• " + power);
            }
        }
        player.sendMessage(ChatColor.GOLD + "=================================");
        FakeRoleMessage(player);
    }


}