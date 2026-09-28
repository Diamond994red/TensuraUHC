package uhc.tensuraUHC.GUIs;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import uhc.tensuraUHC.TensuraUHC;
import uhc.tensuraUHC.roles.Role;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;

public class GUIManager {

    private final TensuraUHC main;

    public GUIManager(TensuraUHC main) {
        this.main = main;
    }

    // 1. Menu Principal
    public void openUHCMenu(Player player) {
        Inventory gui = Bukkit.createInventory(null, 27, ChatColor.DARK_GRAY + "Menu Principal");

        gui.setItem(11, createItem(Material.GRASS, ChatColor.GREEN + "Configuration du Monde",
                ChatColor.GRAY + "Gérer la map, les biomes",
                ChatColor.GRAY + "et le taux des minerais."));

        gui.setItem(13, createItem(Material.REDSTONE_COMPARATOR, ChatColor.GOLD + "Paramètres de la Partie",
                ChatColor.GRAY + "Configurer les timers, le nom",
                ChatColor.GRAY + "de la partie et les règles."));

        gui.setItem(15, createItem(Material.EMERALD_BLOCK, ChatColor.GOLD + "" + ChatColor.BOLD + "Commencer la partie",
                ChatColor.YELLOW + "Cliquez pour lancer le jeu !"));

        player.openInventory(gui);
    }

    public ItemStack createCustomHead(String base64Texture, String name, String... loreLines) {
        ItemStack head = new ItemStack(Material.SKULL_ITEM, 1, (short) 3);
        SkullMeta meta = (SkullMeta) head.getItemMeta();

        if (meta != null) {
            meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', name));

            if (loreLines != null && loreLines.length > 0) {
                List<String> lore = new ArrayList<>();
                for (String line : loreLines) {
                    lore.add(ChatColor.translateAlternateColorCodes('&', line));
                }
                meta.setLore(lore);
            }

            // Nettoyage rigoureux de la chaîne base64
            String cleanBase64 = base64Texture.trim().replaceAll("\\s+", "");

            // Création du profil avec un UUID aléatoire unique pour éviter les conflits de cache
            GameProfile profile = new GameProfile(UUID.randomUUID(), null);
            profile.getProperties().put("textures", new Property("textures", cleanBase64));

            try {
                Field profileField = meta.getClass().getDeclaredField("profile");
                profileField.setAccessible(true);
                profileField.set(meta, profile);
            } catch (Exception e) {
                e.printStackTrace();
            }

            head.setItemMeta(meta);
        }
        return head;
    }

    // 2. Paramètres de la Partie (Mise à jour sans appel prématuré à openInventory)
    public void openGameConfigMenu(Player player) {
        Inventory gui = Bukkit.createInventory(null, 27, ChatColor.DARK_GRAY + "Paramètres de la Partie");

        gui.setItem(4, createItem(Material.BOOK, ChatColor.AQUA + "Scénarios",
                ChatColor.GRAY + "Configurer les règles de jeu",
                ChatColor.GRAY + "et options complémentaires."));

        gui.setItem(10, createItem(Material.WATCH, ChatColor.GOLD + "Timers de la partie",
                ChatColor.GRAY + "Ajuster la durée des épisodes,",
                ChatColor.GRAY + "des rôles et du PvP."));

        gui.setItem(11, createItem(Material.ANVIL, ChatColor.GOLD + "Limites de la Partie",
                ChatColor.GRAY + "Définir la limite d'armure en diamant",
                ChatColor.GRAY + "et le quota de minerais minés."));

        gui.setItem(12, createItem(Material.CHEST, ChatColor.GREEN + "Inventaire de Départ",
                ChatColor.GRAY + "Définir les objets donnés à",
                ChatColor.GRAY + "tous les joueurs au lancement."));

        gui.setItem(14, createItem(Material.NAME_TAG, ChatColor.YELLOW + "Nom de la partie : " + ChatColor.WHITE + main.getGameName(),
                ChatColor.GRAY + "Cliquez pour modifier le nom dans le chat."));

        ItemStack damageItem = new ItemStack(Material.DIAMOND_SWORD);
        ItemMeta critMeta = damageItem.getItemMeta();
        if (critMeta != null) {
            critMeta.setDisplayName(ChatColor.GOLD + "Dégâts et effets");
            List<String> lore = new ArrayList<>();
            lore.add(ChatColor.GRAY + "Modification des critiques,");
            lore.add(ChatColor.GRAY + "de la force et d'autres effets");
            critMeta.setLore(lore);
            damageItem.setItemMeta(critMeta);
        }
        gui.setItem(15, damageItem);

        ItemStack itemRestriction = new ItemStack(Material.HOPPER);
        ItemMeta restrictionMeta = itemRestriction.getItemMeta();
        if (restrictionMeta != null) {
            restrictionMeta.setDisplayName(ChatColor.RED + "Restriction des Objets");
            List<String> lore = new ArrayList<>();
            lore.add(ChatColor.GRAY + "Activer ou désactiver certains objets");
            restrictionMeta.setLore(lore);
            itemRestriction.setItemMeta(restrictionMeta);
        }
        gui.setItem(16, itemRestriction);

        gui.setItem(18, createItem(Material.BARRIER, ChatColor.RED + "Retour au menu principal"));

        // Tête personnalisée du menu Rôles
        String base64Texture = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvN2NhNmQ1ZTI1NTc5N2Q1N2UxYTBmY2QxZWQ3ODgxYTFiMjVlYWI5ZTM4OGNjZjdjNjVlOTFkZTBhNGVmNDk1ZiJ9fX0=";
        gui.setItem(22, createCustomHead(
                base64Texture,
                ChatColor.AQUA + "Rôles UHC",
                ChatColor.GRAY + "Configurer le mode de jeu"
        ));

        // Un seul appel final pour ouvrir le GUI préparé
        player.openInventory(gui);
    }

    // 3. Configuration des Timers
    public void openTimersConfigMenu(Player player) {
        Inventory gui = Bukkit.createInventory(null, 27, ChatColor.DARK_GRAY + "Configuration des Timers");

        gui.setItem(10, createItem(Material.WATCH, ChatColor.GOLD + "Durée de l'Épisode : " + ChatColor.GREEN + formatTime(main.getEpisodeLengthSeconds()),
                ChatColor.GRAY + "Cliquez pour modifier la durée",
                ChatColor.GRAY + "des épisodes."));

        gui.setItem(11, createItem(Material.NETHER_STAR, ChatColor.LIGHT_PURPLE + "Annonce des Rôles : " + ChatColor.GREEN + formatTime(main.GetRoleTime()),
                ChatColor.GRAY + "Cliquez pour modifier le moment",
                ChatColor.GRAY + "d'attribution des rôles."));

        gui.setItem(12, createItem(Material.REDSTONE, ChatColor.GOLD + "Timer Meetup : " + ChatColor.GREEN + formatTime(main.getMeetupTime()),
                ChatColor.GRAY + "Cliquez pour modifier le timer Meetup."));

        gui.setItem(13, createItem(Material.WATCH, ChatColor.YELLOW + "Cycle Jour / Nuit",
                ChatColor.GRAY + "Cliquez pour modifier le cycle."));

        gui.setItem(14, createItem(Material.BARRIER, ChatColor.RED + "Gestion de la Bordure",
                ChatColor.GRAY + "Taille max, min, vitesse",
                ChatColor.GRAY + "et type de réduction."));

        gui.setItem(15, createItem(Material.DIAMOND_SWORD, ChatColor.RED + "Activation du PvP : " + ChatColor.GREEN + formatTime(main.GetPvpTime()),
                ChatColor.GRAY + "Cliquez pour modifier le moment",
                ChatColor.GRAY + "d'activation du PvP."));

        gui.setItem(16, createItem(Material.GOLDEN_APPLE, ChatColor.GOLD + "Timer FinalHeal : " + ChatColor.GREEN + formatTime(main.GetFinalHealTime()),
                ChatColor.GRAY + "Cliquez pour modifier le timer FinalHeal."));

        gui.setItem(18, createItem(Material.BARRIER, ChatColor.RED + "Retour aux paramètres"));

        player.openInventory(gui);
    }

    public void openBorderConfigMenu(Player player) {
        Inventory gui = Bukkit.createInventory(null, 27, ChatColor.DARK_GRAY + "Réglage : Bordure");

        gui.setItem(10, createButtonItem(Material.GRASS, ChatColor.GOLD + "Taille Max : " + ChatColor.GREEN + main.getBorderInitialSize() + "x" + main.getBorderInitialSize(),
                ChatColor.GRAY + "Clic gauche: +100 blocs",
                ChatColor.GRAY + "Clic droit: -100 blocs"));

        gui.setItem(12, createButtonItem(Material.BEDROCK, ChatColor.RED + "Taille Min : " + ChatColor.GREEN + main.getBorderFinalSize() + "x" + main.getBorderFinalSize(),
                ChatColor.GRAY + "Clic gauche: +50 blocs",
                ChatColor.GRAY + "Clic droit: -50 blocs"));

        gui.setItem(14, createButtonItem(Material.WATCH, ChatColor.YELLOW + "Temps de Réduction : " + ChatColor.GREEN + formatTime(main.getBorderShrinkDuration()),
                ChatColor.GRAY + "Clic gauche: +1 min",
                ChatColor.GRAY + "Clic droit: -1 min",
                ChatColor.DARK_RED + "Se déclenche au Meetup"));

        String modeName = main.isBorderInstant() ? ChatColor.RED + "Instantannée" : ChatColor.GREEN + "Progressive";
        gui.setItem(16, createButtonItem(Material.COMPASS, ChatColor.AQUA + "Type : " + modeName,
                ChatColor.GRAY + "Cliquez pour alterner entre",
                ChatColor.GRAY + "Progressif et Instantané."));

        gui.setItem(18, createButtonItem(Material.BARRIER, ChatColor.RED + "Retour aux timers"));

        player.openInventory(gui);
    }

    public void openEpisodeTimerEditMenu(Player player) {
        Inventory gui = Bukkit.createInventory(null, 27, ChatColor.DARK_GRAY + "Réglage : Épisodes");
        gui.setItem(4, createItem(Material.WATCH, ChatColor.GOLD + "Durée Actuelle : " + ChatColor.GREEN + formatTime(main.getEpisodeLengthSeconds())));
        setupTimerEditItems(gui);
        player.openInventory(gui);
    }

    public void openRoleTimerEditMenu(Player player) {
        Inventory gui = Bukkit.createInventory(null, 27, ChatColor.DARK_GRAY + "Réglage : Rôles");
        gui.setItem(4, createItem(Material.NETHER_STAR, ChatColor.LIGHT_PURPLE + "Annonce Actuelle : " + ChatColor.GREEN + formatTime(main.GetRoleTime())));
        setupTimerEditItems(gui);
        player.openInventory(gui);
    }

    public void openFinalHealTimerEditMenu(Player player) {
        Inventory gui = Bukkit.createInventory(null, 27, ChatColor.DARK_GRAY + "Réglage : FinalHeal");
        gui.setItem(4, createItem(Material.GOLDEN_APPLE, ChatColor.GOLD + "Timer Actuel : " + ChatColor.GREEN + formatTime(main.GetFinalHealTime())));
        setupTimerEditItems(gui);
        player.openInventory(gui);
    }

    public void openPvpTimerEditMenu(Player player) {
        Inventory gui = Bukkit.createInventory(null, 27, ChatColor.DARK_GRAY + "Réglage : PvP");
        gui.setItem(4, createItem(Material.DIAMOND_SWORD, ChatColor.RED + "Activation Actuelle : " + ChatColor.GREEN + formatTime(main.GetPvpTime())));
        setupTimerEditItems(gui);
        player.openInventory(gui);
    }

    private void setupTimerEditItems(Inventory gui) {
        gui.setItem(11, createItem(Material.REDSTONE, ChatColor.YELLOW + "Ajuster de 15 Secondes",
                ChatColor.GREEN + "Clic Gauche : +15s",
                ChatColor.RED + "Clic Droit : -15s"));

        gui.setItem(13, createItem(Material.GLOWSTONE_DUST, ChatColor.YELLOW + "Ajuster de 1 Minute",
                ChatColor.GREEN + "Clic Gauche : +1 min",
                ChatColor.RED + "Clic Droit : -1 min"));

        gui.setItem(15, createItem(Material.SUGAR, ChatColor.YELLOW + "Ajuster de 10 Minutes",
                ChatColor.GREEN + "Clic Gauche : +10 min",
                ChatColor.RED + "Clic Droit : -10 min"));

        gui.setItem(18, createItem(Material.BARRIER, ChatColor.RED + "Retour aux timers"));
    }

    public void openWorldConfigMenu(Player player) {
        Inventory gui = Bukkit.createInventory(null, 27, ChatColor.DARK_GRAY + "Configuration du Monde");

        gui.setItem(10, createItem(Material.STONE, ChatColor.GOLD + "Taille des Grottes : " + ChatColor.GREEN + main.getCaveSizePercent() + "%",
                ChatColor.GREEN + "Clic Gauche : +50%",
                ChatColor.RED + "Clic Droit : -50%"));

        gui.setItem(12, createItem(Material.DIAMOND_ORE, ChatColor.AQUA + "Réglage des Minerais",
                ChatColor.GRAY + "Ajuster le taux d'apparition des minerais."));

        gui.setItem(14, createItem(Material.SAPLING, ChatColor.GREEN + "Biome Centre : " + ChatColor.WHITE + "ROOFED_FOREST"));

        gui.setItem(16, createItem(Material.EMPTY_MAP, ChatColor.YELLOW + "Générer & Téléporter au Monde",
                ChatColor.GRAY + "Génère le monde et vous téléporte pour prévisu."));

        gui.setItem(18, createItem(Material.BARRIER, ChatColor.RED + "Retour au menu principal"));

        player.openInventory(gui);
    }

    public void openOreConfigMenu(Player player) {
        Inventory gui = Bukkit.createInventory(null, 27, ChatColor.DARK_GRAY + "Configuration Minerais");

        gui.setItem(4, createOreItem(Material.EXP_BOTTLE, "XP", main.getXpPercent()));
        gui.setItem(10, createOreItem(Material.DIAMOND_ORE, "Diamant", main.getDiamondPercent()));
        gui.setItem(11, createOreItem(Material.GOLD_ORE, "Or", main.getGoldPercent()));
        gui.setItem(12, createOreItem(Material.IRON_ORE, "Fer", main.getIronPercent()));
        gui.setItem(13, createOreItem(Material.REDSTONE_ORE, "Redstone", main.getRedstonePercent()));
        gui.setItem(14, createOreItem(Material.LAPIS_ORE, "Lapis-Lazuli", main.getLapisPercent()));
        gui.setItem(15, createOreItem(Material.COAL_ORE, "Charbon", main.getCoalPercent()));
        gui.setItem(16, createOreItem(Material.EMERALD_ORE, "Émeraude", main.getEmeraldPercent()));
        gui.setItem(18, createItem(Material.BARRIER, ChatColor.RED + "Retour"));
        gui.setItem(25, createItem(Material.BARRIER, ChatColor.DARK_RED + "Actuellement indisponible"));

        player.openInventory(gui);
    }

    private String formatTime(int totalSeconds) {
        int minutes = totalSeconds / 60;
        int seconds = totalSeconds % 60;
        return (seconds > 0) ? String.format("%d min %d s", minutes, seconds) : minutes + " min";
    }

    private ItemStack createItem(Material mat, String name, String... lore) {
        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(name);
            if (lore.length > 0) {
                meta.setLore(Arrays.asList(lore));
            }
            item.setItemMeta(meta);
        }
        return item;
    }

    public void openStarterKitEditor(Player player) {
        player.closeInventory();

        Bukkit.getScheduler().runTaskLater(main, () -> {
            main.setEditingStarterKit(true);

            player.getInventory().clear();
            player.getInventory().setArmorContents(null);
            player.setGameMode(GameMode.CREATIVE);

            player.sendMessage(ChatColor.GOLD + "=================================");
            player.sendMessage(ChatColor.GREEN + "Mode Édition du Starter Kit activé !");
            player.sendMessage(ChatColor.YELLOW + "1. Prenez dans l'inventaire créatif les items du kit.");
            player.sendMessage(ChatColor.YELLOW + "2. Tapez " + ChatColor.AQUA + "/saveinv " + ChatColor.YELLOW + "pour enregistrer.");
            player.sendMessage(ChatColor.GOLD + "=================================");
        }, 1L);
    }

    private ItemStack createOreItem(Material mat, String name, int percent) {
        return createItem(mat, ChatColor.GOLD + name + " : " + ChatColor.GREEN + percent + "%",
                ChatColor.GREEN + "Clic Gauche : +50%",
                ChatColor.RED + "Clic Droit : -50%");
    }

    public void openMeetupTimerEditMenu(Player player) {
        Inventory gui = Bukkit.createInventory(null, 27, ChatColor.DARK_GRAY + "Réglage : Meetup");
        gui.setItem(4, createItem(Material.REDSTONE, ChatColor.RED + "Activation Actuelle : " + ChatColor.GREEN + formatTime(main.getMeetupTime())));
        setupTimerEditItems(gui);
        gui.setItem(18, createButtonItem(Material.BARRIER, ChatColor.RED + "Retour aux timers"));
        player.openInventory(gui);
    }

    public void openDayNightConfigMenu(Player player) {
        Inventory gui = Bukkit.createInventory(null, 27, ChatColor.DARK_GRAY + "Réglage : Jour / Nuit");

        gui.setItem(11, createButtonItem(Material.WATCH, ChatColor.GOLD + "Durée du cycle : " + ChatColor.YELLOW + (main.getDayNightCycleSeconds() / 60) + " min",
                ChatColor.GRAY + "Clic gauche: +1 min", ChatColor.GRAY + "Clic droit: -1 min"));

        gui.setItem(15, createButtonItem(Material.DOUBLE_PLANT, ChatColor.YELLOW + "% Jour : " + ChatColor.GREEN + main.getDayPercent() + "%" + ChatColor.DARK_GRAY + " / " + ChatColor.DARK_PURPLE + (100 - main.getDayPercent()) + "% Nuit",
                ChatColor.GRAY + "Clic gauche: +5% Jour", ChatColor.GRAY + "Clic droit: -5% Jour"));

        gui.setItem(18, createButtonItem(Material.BARRIER, ChatColor.RED + "Retour aux timers"));

        player.openInventory(gui);
    }

    private ItemStack createButtonItem(Material mat, String name, String... lore) {
        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(name);
            if (lore.length > 0) meta.setLore(Arrays.asList(lore));
            item.setItemMeta(meta);
        }
        return item;
    }

    public void openGiveAllMenu(Player player, int currentAmount) {
        Inventory gui = Bukkit.createInventory(null, 27, ChatColor.DARK_GRAY + "GiveAll - Quantité : " + currentAmount);

        gui.setItem(1, createGuiItem(Material.STAINED_GLASS_PANE, ChatColor.RED + "-16", (byte) 14));
        gui.setItem(2, createGuiItem(Material.STAINED_GLASS_PANE, ChatColor.RED + "-1", (byte) 14));
        gui.setItem(4, createGuiItem(Material.NETHER_STAR, ChatColor.GOLD + "Quantité : " + ChatColor.GREEN + currentAmount));
        gui.setItem(6, createGuiItem(Material.STAINED_GLASS_PANE, ChatColor.GREEN + "+1", (byte) 5));
        gui.setItem(7, createGuiItem(Material.STAINED_GLASS_PANE, ChatColor.GREEN + "+16", (byte) 5));

        gui.setItem(10, createGuiItem(Material.GOLDEN_APPLE, ChatColor.GOLD + "Pomme d'Or"));
        gui.setItem(11, createGuiItem(Material.LOG_2, ChatColor.DARK_GREEN + "Bûche de Chêne Noir", (byte) 1));
        gui.setItem(12, createGuiItem(Material.GOLDEN_CARROT, ChatColor.YELLOW + "Carotte Dorée"));
        gui.setItem(13, createGuiItem(Material.EXP_BOTTLE, ChatColor.GREEN + "Niveaux d'XP"));
        gui.setItem(14, createGuiItem(Material.ARROW, ChatColor.WHITE + "Flèche"));
        gui.setItem(15, createGuiItem(Material.BOOK, ChatColor.AQUA + "Livre"));
        gui.setItem(16, createGuiItem(Material.DIAMOND, ChatColor.BLUE + "Diamant"));

        player.openInventory(gui);
    }

    private ItemStack createGuiItem(Material mat, String name, byte data) {
        ItemStack item = new ItemStack(mat, 1, data);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(name);
            item.setItemMeta(meta);
        }
        return item;
    }

    private ItemStack createGuiItem(Material mat, String name) {
        return createGuiItem(mat, name, (byte) 0);
    }

    public void openItemRestrictionMenu(Player player) {
        Inventory gui = Bukkit.createInventory(null, 27, ChatColor.DARK_GRAY + "Restriction des Objets");

        Material[] itemsToToggle = new Material[] {
                Material.ENDER_PEARL,
                Material.FISHING_ROD,
                Material.LAVA_BUCKET,
                Material.BED,
                Material.GOLDEN_APPLE,
                Material.FLINT_AND_STEEL,
                Material.BOW,
                Material.BREWING_STAND
        };

        int[] slots = {10, 11, 12, 13, 14, 15, 16, 19};

        for (int i = 0; i < itemsToToggle.length; i++) {
            Material mat = itemsToToggle[i];

            short durability = (mat == Material.GOLDEN_APPLE) ? (short) 1 : 0;
            boolean isDisabled = (mat == Material.GOLDEN_APPLE) ? main.isNotchAppleDisabled() : main.isItemDisabled(mat);

            ItemStack item = new ItemStack(mat, 1, durability);
            ItemMeta meta = item.getItemMeta();
            if (meta != null) {
                String name = (mat == Material.GOLDEN_APPLE) ? "Pomme de Notch" : mat.name().replace("_", " ");
                meta.setDisplayName((isDisabled ? ChatColor.RED : ChatColor.GREEN) + name);

                List<String> lore = new ArrayList<>();
                lore.add(ChatColor.GRAY + "Statut : " + (isDisabled ? ChatColor.RED + "DÉSACTIVÉ" : ChatColor.GREEN + "ACTIVÉ"));
                lore.add(ChatColor.YELLOW + "Cliquez pour modifier");
                meta.setLore(lore);

                item.setItemMeta(meta);
            }
            gui.setItem(slots[i], item);
        }

        gui.setItem(18, createButtonItem(Material.BARRIER, ChatColor.RED + "Retour"));
        player.openInventory(gui);
    }

    public void openIronEnchantMenu(Player player) {
        Inventory gui = Bukkit.createInventory(null, 27, ChatColor.DARK_GRAY + "Enchants Max : FER");

        gui.setItem(0, createItem(Material.IRON_CHESTPLATE, ChatColor.GREEN + "► Équipement en Fer ◄", Collections.singletonList(ChatColor.GRAY + "Onglet actuel")));
        gui.setItem(4, createItem(Material.BOW, ChatColor.YELLOW + "Basculer vers ARC", Collections.singletonList(ChatColor.YELLOW + "Cliquez pour voir l'équipement en Arc")));
        gui.setItem(8, createItem(Material.DIAMOND_CHESTPLATE, ChatColor.YELLOW + "Basculer vers DIAMANT", Collections.singletonList(ChatColor.YELLOW + "Cliquez pour voir l'équipement en Diamant")));

        gui.setItem(10, createEnchantItem("Sharpness", main.getIronSharpnessMax(), 5));
        gui.setItem(12, createEnchantItem("Protection", main.getIronProtectionMax(), 4));
        gui.setItem(14, createEnchantItem("Fire Aspect", main.getIronFireMax(), 2));
        gui.setItem(16, createEnchantItem("Knockback", main.getIronKnockbackMax(), 2));

        gui.setItem(26, createItem(Material.ARROW, ChatColor.RED + "Retour", Collections.singletonList(ChatColor.GRAY + "Retour aux limites de la partie")));

        player.openInventory(gui);
    }

    public void openDiamondEnchantMenu(Player player) {
        Inventory gui = Bukkit.createInventory(null, 27, ChatColor.DARK_GRAY + "Enchants Max : DIAMANT");

        gui.setItem(0, createItem(Material.IRON_CHESTPLATE, ChatColor.YELLOW + "Basculer vers FER", Collections.singletonList(ChatColor.YELLOW + "Cliquez pour voir l'équipement en Fer")));
        gui.setItem(4, createItem(Material.BOW, ChatColor.YELLOW + "Basculer vers ARC", Collections.singletonList(ChatColor.YELLOW + "Cliquez pour voir l'équipement en Arc")));
        gui.setItem(8, createItem(Material.DIAMOND_CHESTPLATE, ChatColor.GREEN + "► Équipement en Diamant ◄", Collections.singletonList(ChatColor.GRAY + "Onglet actuel")));

        gui.setItem(10, createEnchantItem("Sharpness", main.getDiamondSharpnessMax(), 5));
        gui.setItem(12, createEnchantItem("Protection", main.getDiamondProtectionMax(), 4));
        gui.setItem(14, createEnchantItem("Fire Aspect", main.getDiamondFireMax(), 2));
        gui.setItem(16, createEnchantItem("Knockback", main.getDiamondKnockbackMax(), 2));

        gui.setItem(26, createItem(Material.ARROW, ChatColor.RED + "Retour", Collections.singletonList(ChatColor.GRAY + "Retour aux limites de la partie")));

        player.openInventory(gui);
    }

    public void openBowEnchantMenu(Player player) {
        Inventory gui = Bukkit.createInventory(null, 27, ChatColor.DARK_GRAY + "Enchants Max : ARC");

        gui.setItem(0, createItem(Material.IRON_CHESTPLATE, ChatColor.YELLOW + "Basculer vers FER", Collections.singletonList(ChatColor.YELLOW + "Cliquez pour voir l'équipement en Fer")));
        gui.setItem(4, createItem(Material.BOW, ChatColor.GREEN + "► Équipement en Arc ◄", Collections.singletonList(ChatColor.GRAY + "Onglet actuel")));
        gui.setItem(8, createItem(Material.DIAMOND_CHESTPLATE, ChatColor.YELLOW + "Basculer vers DIAMANT", Collections.singletonList(ChatColor.YELLOW + "Cliquez pour voir l'équipement en Diamant")));

        gui.setItem(11, createEnchantItem("Power", main.getBowPowerMax(), 5));
        gui.setItem(13, createEnchantItem("Flame", main.getBowFlameMax(), 2));
        gui.setItem(15, createEnchantItem("Punch", main.getBowPunchMax(), 2));

        gui.setItem(26, createItem(Material.ARROW, ChatColor.RED + "Retour", Collections.singletonList(ChatColor.GRAY + "Retour aux limites de la partie")));

        player.openInventory(gui);
    }

    private ItemStack createEnchantItem(String enchantName, int currentLevel, int maxPossible) {
        ItemStack item = new ItemStack(Material.ENCHANTED_BOOK);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(ChatColor.AQUA + enchantName);
            List<String> lore = new ArrayList<>();
            lore.add(ChatColor.GRAY + "Niveau Max : " + ChatColor.WHITE + currentLevel + " / " + maxPossible);
            lore.add("");
            lore.add(ChatColor.GREEN + "Clic Gauche : +1");
            lore.add(ChatColor.RED + "Clic Droit : -1");
            meta.setLore(lore);
            item.setItemMeta(meta);
        }
        return item;
    }

    private ItemStack createItem(Material mat, String name, List<String> lore) {
        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(name);
            meta.setLore(lore);
            item.setItemMeta(meta);
        }
        return item;
    }

    public void openLimitsConfigMenu(Player player) {
        Inventory gui = Bukkit.createInventory(null, 27, ChatColor.DARK_GRAY + "Limites de la Partie");

        gui.setItem(4, createButtonItem(Material.FLINT, ChatColor.GOLD + "Drop custom",
                ChatColor.GRAY + "Cliquez pour configurer les pourcentages de drop"));

        gui.setItem(11, createButtonItem(Material.DIAMOND_CHESTPLATE, ChatColor.AQUA + "Pièces Diamant Max : " + ChatColor.GREEN + main.getMaxDiamondArmorPieces(),
                ChatColor.GRAY + "Clic Gauche : +1 pièce",
                ChatColor.GRAY + "Clic Droit : -1 pièce",
                ChatColor.DARK_GRAY + "Maximum : 4 pièces"));

        gui.setItem(15, createButtonItem(Material.ENCHANTED_BOOK, ChatColor.GOLD + "Limites d'Enchantement",
                ChatColor.GRAY + "Cliquez pour configurer les enchants max Fer / Diamant"));

        gui.setItem(18, createButtonItem(Material.BARRIER, ChatColor.RED + "Retour aux paramètres"));

        gui.setItem(22, createButtonItem(Material.DIAMOND, ChatColor.BLUE + "Diamants Minés Max : " + ChatColor.GREEN + main.getMaxMinedDiamonds(),
                ChatColor.GRAY + "Clic Gauche : +1",
                ChatColor.GRAY + "Clic Droit : -1"));

        player.openInventory(gui);
    }

    public void openDropMenu(Player player) {
        Inventory gui = Bukkit.createInventory(null, 27, ChatColor.DARK_GRAY + "Configuration des Drops");

        gui.setItem(10, createDropItem(Material.APPLE, "Pommes", main.getAppleDropPercent()));
        gui.setItem(12, createDropItem(Material.FLINT, "Silex", main.getFlintDropPercent()));
        gui.setItem(14, createDropItem(Material.ENDER_PEARL, "Ender Pearls", main.getEnderPearlDropPercent()));
        gui.setItem(18, createItem(Material.BARRIER, ChatColor.RED + "Retour aux paramètres"));

        player.openInventory(gui);
    }

    private ItemStack createDropItem(Material material, String name, int percent) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(ChatColor.GOLD + "Drop " + name + " : " + ChatColor.GREEN + percent + "%");
            List<String> lore = new ArrayList<>();
            lore.add(ChatColor.GRAY + "Clic gauche : +5%");
            lore.add(ChatColor.GRAY + "Clic droit : -5%");
            lore.add(ChatColor.GRAY + "Shift + Clic gauche : +10%");
            lore.add(ChatColor.GRAY + "Shift + Clic droit : -10%");
            meta.setLore(lore);
            item.setItemMeta(meta);
        }
        return item;
    }
    // Menu principal des Rôles UHC
    public void openRolesMenu(Player player) {
        Inventory gui = Bukkit.createInventory(null, 27, ChatColor.DARK_GRAY + "Configuration des Rôles");

        gui.setItem(9, createItem(Material.REDSTONE_COMPARATOR, ChatColor.DARK_PURPLE + "Paramètres Tensura", ChatColor.GRAY + "Ajuster les règles spécifiques au mode de jeu."));
        gui.setItem(17, createItem(Material.COMMAND, ChatColor.DARK_PURPLE + "Events Aléatoires", ChatColor.GRAY + "Gérer les événements pendant la partie."));
        gui.setItem(4, createItem(Material.NETHER_STAR, ChatColor.GOLD + "Solos", ChatColor.GRAY + "Configurer les rôles Solos."));
        gui.setItem(12, createItem(Material.ROTTEN_FLESH, ChatColor.DARK_GREEN + "Monstres", ChatColor.GRAY + "Configurer le camp des Monstres."));
        gui.setItem(22, createItem(Material.BEACON, ChatColor.YELLOW + "Octagramme", ChatColor.GRAY + "Configurer le camp de l'Octagramme."));
        gui.setItem(13, createItem(Material.ENDER_STONE, ChatColor.DARK_BLUE + "Clowns", ChatColor.GRAY + "Configurer le camp des clowns"));
        gui.setItem(14, createItem(Material.IRON_CHESTPLATE, ChatColor.AQUA + "Humains", ChatColor.GRAY + "Configurer le camp des Humains."));
        gui.setItem(26, createItem(Material.BANNER, ChatColor.AQUA + "Composition", ChatColor.GRAY + "Gérer les événements pendant la partie."));
        // Bouton Retour au menu des paramètres de la partie
        gui.setItem(18, createItem(Material.BARRIER, ChatColor.RED + "Retour aux paramètres"));

        player.openInventory(gui);
    }

    // Méthode générique pour ouvrir les sous-menus de chaque camp
    public void openRoleCategoryMenu(Player player, String categoryTitle, Role.Camp camp) {
        Inventory gui = Bukkit.createInventory(null, 27, ChatColor.DARK_GRAY + categoryTitle);

        int slot = 0;
        for (Role role : main.getRoleManager().getRoles()) {
            if (camp != null && role.getInitialCamp() != camp) {
                continue;
            }

            boolean active = role.isEnabled();
            int count = role.getCount();

            ChatColor nameColor = active ? ChatColor.GREEN : ChatColor.GRAY;

            // Quantité de l'item = nombre du rôle (1 si 0 pour que l'item reste visible)
            int itemAmount = Math.max(1, count);

            // Colorant : Vert si count > 0, Gris si count = 0
            ItemStack item = new ItemStack(Material.INK_SACK, itemAmount, (short) (active ? 10 : 8));
            ItemMeta meta = item.getItemMeta();

            if (meta != null) {
                meta.setDisplayName(nameColor + role.getName());

                List<String> lore = new ArrayList<>();
                lore.add(ChatColor.GRAY + "Nombre : " + (active ? ChatColor.GREEN + String.valueOf(count) : ChatColor.RED + "0"));
                lore.add("");
                lore.add(ChatColor.YELLOW + "Clic Gauche : " + ChatColor.GREEN + "+1");
                lore.add(ChatColor.YELLOW + "Clic Droit : " + ChatColor.RED + "-1");

                meta.setLore(lore);
                item.setItemMeta(meta);
            }

            gui.setItem(slot, item);
            slot++;
            if (slot >= 18) break;
        }

        // Bouton Retour
        gui.setItem(18, createItem(Material.BARRIER, ChatColor.RED + "Retour aux rôles"));

        player.openInventory(gui);
    }

    public void openYuukiFacadeMenu(Player player) {
        Inventory gui = Bukkit.createInventory(null, 27, ChatColor.DARK_GRAY + "Façade : Rôles Monstres");

        int slot = 0;
        for (Role role : main.getRoleManager().getRoles()) {
            if (role.getCamp() == Role.Camp.MONSTRES) {
                ItemStack item = new ItemStack(Material.PAPER);
                ItemMeta meta = item.getItemMeta();

                if (meta != null) {
                    meta.setDisplayName(ChatColor.GOLD + role.getName());
                    List<String> lore = new ArrayList<>();
                    lore.add(ChatColor.GRAY + "Camp : " + ChatColor.GREEN + "Monstres");
                    lore.add("");
                    lore.add(ChatColor.YELLOW + "Clic pour choisir ce rôle de façade.");
                    meta.setLore(lore);
                    item.setItemMeta(meta);
                }

                gui.setItem(slot, item);
                slot++;
                if (slot >= 27) break;
            }
        }

        player.openInventory(gui);
    }
    public void openTensuraSettingsMenu(Player player) {
        Inventory gui = Bukkit.createInventory(null, 27, ChatColor.DARK_GRAY + "Configuration des Paramètres");

        // Bouton Retour au menu des paramètres de la partie
        gui.setItem(18, createItem(Material.BARRIER, ChatColor.RED + "Retour aux paramètres"));

        player.openInventory(gui);
    }

    public void openRandomEventsMenu(Player player) {
        Inventory gui = Bukkit.createInventory(null, 27, ChatColor.DARK_GRAY + "Configuration des Events");
        gui.setItem(18, createItem(Material.BARRIER, ChatColor.RED + "Retour aux paramètres"));

        player.openInventory(gui);
    }
    private final Map<UUID, List<Player>> pendingColorTargets = new HashMap<>();

    public void openColorPickerMenu(Player player, List<Player> targets) {
        pendingColorTargets.put(player.getUniqueId(), targets);

        Inventory gui = Bukkit.createInventory(null, 18, ChatColor.DARK_GRAY + "Choisir une couleur");

        // Laines colorées (Wool meta / bytes en 1.8)
        gui.setItem(0, createColorItem(Material.WOOL, 14, ChatColor.RED + "Rouge", ChatColor.RED));
        gui.setItem(1, createColorItem(Material.WOOL, 1, ChatColor.GOLD + "Orange / Or", ChatColor.GOLD));
        gui.setItem(2, createColorItem(Material.WOOL, 4, ChatColor.YELLOW + "Jaune", ChatColor.YELLOW));
        gui.setItem(3, createColorItem(Material.WOOL, 5, ChatColor.GREEN + "Vert Clair", ChatColor.GREEN));
        gui.setItem(4, createColorItem(Material.WOOL, 13, ChatColor.DARK_GREEN + "Vert Foncé", ChatColor.DARK_GREEN));
        gui.setItem(5, createColorItem(Material.WOOL, 3, ChatColor.AQUA + "Cyan", ChatColor.AQUA));
        gui.setItem(6, createColorItem(Material.WOOL, 11, ChatColor.BLUE + "Bleu", ChatColor.BLUE));
        gui.setItem(7, createColorItem(Material.WOOL, 10, ChatColor.DARK_PURPLE + "Violet", ChatColor.DARK_PURPLE));
        gui.setItem(8, createColorItem(Material.WOOL, 2, ChatColor.LIGHT_PURPLE + "Rose", ChatColor.LIGHT_PURPLE));

        gui.setItem(12, createColorItem(Material.WOOL, 0, ChatColor.WHITE + "Blanc", ChatColor.WHITE));
        gui.setItem(13, createColorItem(Material.WOOL, 8, ChatColor.GRAY + "Gris", ChatColor.GRAY));
        gui.setItem(14, createColorItem(Material.WOOL, 15, ChatColor.BLACK + "Reset (Noir/Normal)", ChatColor.RESET));

        player.openInventory(gui);
    }

    private ItemStack createColorItem(Material mat, int data, String name, ChatColor color) {
        ItemStack item = new ItemStack(mat, 1, (short) data);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(name);
            // Stocker la couleur brute dans le Lore pour la lire facilement au clic
            List<String> lore = new ArrayList<>();
            lore.add(color.toString());
            meta.setLore(lore);
            item.setItemMeta(meta);
        }
        return item;
    }

    public List<Player> getPendingColorTargets(Player player) {
        return pendingColorTargets.remove(player.getUniqueId());
    }
    public void openPlayerSelectionMenu(Player player) {
        Inventory gui = Bukkit.createInventory(null, 54, ChatColor.DARK_GRAY + "Sélectionner un joueur");

        int slot = 0;
        for (UUID p : main.getGameManager().GetActivePlayers()) {
            Player pl = Bukkit.getPlayer(p);
            // Filtre : uniquement les joueurs ayant un rôle attribué via le RoleManager
            if (main.getRoleManager().getPlayerRole(p) != null) {
                ItemStack skull = new ItemStack(Material.SKULL_ITEM, 1, (short) 3); // 3 = Tête de joueur
                SkullMeta meta = (SkullMeta) skull.getItemMeta();

                if (meta != null) {
                    meta.setOwner(pl.getName());
                    meta.setDisplayName(ChatColor.YELLOW + pl.getName());

                    List<String> lore = new ArrayList<>();
                    lore.add(ChatColor.GRAY + "Clic pour choisir la couleur de ce joueur.");
                    meta.setLore(lore);

                    skull.setItemMeta(meta);
                }

                gui.setItem(slot, skull);
                slot++;
                if (slot >= 54) break;
            }
        }

        player.openInventory(gui);
    }

    public void openDamageMenu(Player player) {
        Inventory gui = Bukkit.createInventory(null, 27, ChatColor.DARK_GRAY + "Dégâts et effets");
        gui.setItem(10, createItem(Material.DIAMOND_SWORD, ChatColor.BLUE + "Dégâts Critiques : " + main.getCritDamageMultiplier() + "x"));
        gui.setItem(12, createItem(Material.POTION, ChatColor.RED + "Force : " + main.getStrengthMultiplier() + "x"));
        gui.setItem(14, createItem(Material.POTION, ChatColor.GRAY + "Résistance : " + main.getResistanceMultiplier() + "x"));
        gui.setItem(16, createItem(Material.POTION, ChatColor.AQUA + "Speed : "));
        gui.setItem(18, createItem(Material.BARRIER, ChatColor.RED + "Retour aux paramètres"));
        player.openInventory(gui);
    }
}