package uhc.tensuraUHC.listeners;

import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;
import uhc.tensuraUHC.TensuraUHC;
import uhc.tensuraUHC.managers.ScenarioManager;
import uhc.tensuraUHC.roles.Role;
import uhc.tensuraUHC.roles.list.SoloCamp.YuukiRole;
import uhc.tensuraUHC.scenarios.Scenario;

import java.util.Collections;
import java.util.List;

public class InventoryListener implements Listener {

    private final TensuraUHC main;

    public InventoryListener(TensuraUHC main) {
        this.main = main;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        Player player = (Player) event.getWhoClicked();
        String title = event.getView().getTitle();

        // =========================================================================
        // A. PRIORITÉ ABSOLUE : GUI d'Enchantement
        // =========================================================================
        if (title.contains("Enchanter") && title.contains("item en main")) {
            event.setCancelled(true);

            ItemStack current = event.getCurrentItem();
            if (current == null || !current.hasItemMeta()) return;

            ItemStack heldItem = player.getInventory().getItemInHand();
            if (heldItem == null || heldItem.getType() == Material.AIR) {
                player.sendMessage(ChatColor.RED + "Vous ne tenez aucun objet en main !");
                player.closeInventory();
                return;
            }

            String name = current.getItemMeta().getDisplayName();

            if (name.contains("Tranchant")) {
                applyEnchantment(player, heldItem, org.bukkit.enchantments.Enchantment.DAMAGE_ALL, 5, event.isLeftClick());
            } else if (name.contains("Protection")) {
                applyEnchantment(player, heldItem, org.bukkit.enchantments.Enchantment.PROTECTION_ENVIRONMENTAL, 4, event.isLeftClick());
            } else if (name.contains("Puissance")) {
                applyEnchantment(player, heldItem, org.bukkit.enchantments.Enchantment.ARROW_DAMAGE, 5, event.isLeftClick());
            } else if (name.contains("Solidité")) {
                applyEnchantment(player, heldItem, org.bukkit.enchantments.Enchantment.DURABILITY, 3, event.isLeftClick());
            } else if (name.contains("Frappe")) {
                applyEnchantment(player, heldItem, org.bukkit.enchantments.Enchantment.ARROW_KNOCKBACK, 2, event.isLeftClick());
            } else if (name.contains("Recul")) {
                applyEnchantment(player, heldItem, org.bukkit.enchantments.Enchantment.KNOCKBACK, 2, event.isLeftClick());
            } else if (name.contains("Efficacité")) {
                applyEnchantment(player, heldItem, org.bukkit.enchantments.Enchantment.DIG_SPEED, 5, event.isLeftClick());
            } else if (name.contains("Fermer")) {
                player.closeInventory();
            }

            player.updateInventory();
            return;
        }

        // =========================================================================
        // A.2. SÉCURITÉ : Visualisation du Kit via /inv et /rules
        // =========================================================================
        if (title.equals(uhc.tensuraUHC.commands.PlayerCommand.INV_VIEWER_TITLE) ||
                title.equals(uhc.tensuraUHC.commands.PlayerCommand.RULES_GUI_TITLE)) {
            event.setCancelled(true);
            return;
        }

        // =========================================================================
        // A.3. GUI /giveall
        // =========================================================================
        if (title.startsWith(ChatColor.DARK_GRAY + "GiveAll - Quantité : ")) {
            event.setCancelled(true);

            ItemStack current = event.getCurrentItem();
            if (current == null || !current.hasItemMeta()) return;

            int amount = 1;
            try {
                String rawTitle = ChatColor.stripColor(title);
                amount = Integer.parseInt(rawTitle.replace("GiveAll - Quantité : ", "").trim());
            } catch (Exception ignored) {}

            int slot = event.getSlot();

            if (slot == 1) {
                openGiveAllMenu(player, Math.max(1, amount - 16));
                return;
            } else if (slot == 2) {
                openGiveAllMenu(player, Math.max(1, amount - 1));
                return;
            } else if (slot == 6) {
                openGiveAllMenu(player, Math.min(64, amount + 1));
                return;
            } else if (slot == 7) {
                openGiveAllMenu(player, Math.min(64, amount + 16));
                return;
            }

            if (slot == 13) {
                for (Player target : Bukkit.getOnlinePlayers()) {
                    if (target.getGameMode() == GameMode.SURVIVAL || target.getGameMode() == GameMode.ADVENTURE) {
                        target.setLevel(target.getLevel() + amount);
                    }
                }
                Bukkit.broadcastMessage(ChatColor.GOLD + "[GiveAll] " + ChatColor.GREEN + player.getName() +
                        " a donné " + ChatColor.YELLOW + amount + " Niveau(x) d'XP" + ChatColor.GREEN + " à tous les joueurs !");
                openGiveAllMenu(player, amount);
                return;
            }

            if ((slot >= 10 && slot <= 12) || (slot >= 14 && slot <= 16)) {
                ItemStack itemToGive = current.clone();
                itemToGive.setAmount(amount);

                ItemMeta meta = itemToGive.getItemMeta();
                meta.setDisplayName(null);
                itemToGive.setItemMeta(meta);

                for (Player target : Bukkit.getOnlinePlayers()) {
                    if (target.getGameMode() == GameMode.SURVIVAL || target.getGameMode() == GameMode.ADVENTURE) {
                        target.getInventory().addItem(itemToGive.clone());
                    }
                }

                Bukkit.broadcastMessage(ChatColor.GOLD + "[GiveAll] " + ChatColor.GREEN + player.getName() +
                        " a donné x" + amount + " " + current.getItemMeta().getDisplayName() + ChatColor.GREEN + " à tous les joueurs !");
                openGiveAllMenu(player, amount);
                return;
            }
            return;
        }

        // =========================================================================
        // B. AUTORISER le mode Créatif pour son propre inventaire
        // =========================================================================
        if (player.getGameMode() == GameMode.CREATIVE) {
            return;
        }

        if (!main.isGameStarted() && !title.contains(ChatColor.DARK_GRAY + "")) {
            event.setCancelled(true);
        }

        // 1. Menu Principal
        if (title.equals(ChatColor.DARK_GRAY + "Menu Principal")) {
            event.setCancelled(true);
            ItemStack current = event.getCurrentItem();
            if (current == null || !current.hasItemMeta()) return;

            String name = current.getItemMeta().getDisplayName();
            if (name.equals(ChatColor.GREEN + "Configuration du Monde")) {
                main.getGuiManager().openWorldConfigMenu(player);
            } else if (name.equals(ChatColor.GOLD + "Paramètres de la Partie")) {
                main.getGuiManager().openGameConfigMenu(player);
            } else if (name.equals(ChatColor.GOLD + "" + ChatColor.BOLD + "Commencer la partie")) {
                player.closeInventory();
                main.startGame();
            }
        }

        // 2. Menu Paramètres de la Partie
        else if (title.equals(ChatColor.DARK_GRAY + "Paramètres de la Partie")) {
            event.setCancelled(true);
            ItemStack current = event.getCurrentItem();
            if (current == null || !current.hasItemMeta()) return;

            String name = current.getItemMeta().getDisplayName();

            if (name.equals(ChatColor.GOLD + "Timers de la partie")) {
                main.getGuiManager().openTimersConfigMenu(player);
            } else if (name.equals(ChatColor.GREEN + "Inventaire de Départ")) {
                main.getGuiManager().openStarterKitEditor(player);
            } else if (name.startsWith(ChatColor.YELLOW + "Nom de la partie")) {
                player.closeInventory();
                main.setAwaitingGameNameInput(true);
                player.sendMessage(ChatColor.GOLD + "[TensuraUHC] " + ChatColor.YELLOW + "Entrez le nouveau nom de la partie dans le chat :");
            } else if (name.equals(ChatColor.RED + "Retour au menu principal")) {
                main.getGuiManager().openUHCMenu(player);
            } else if (name.startsWith(ChatColor.GOLD + "Dégâts et effets")) {
                main.getGuiManager().openDamageMenu(player);
            } else if (name.equals(ChatColor.RED + "Restriction des Objets")) {
                main.getGuiManager().openItemRestrictionMenu(player);
            } else if (name.contains("Limites de la Partie")) {
                main.getGuiManager().openLimitsConfigMenu(player);
            }
            else if (name.contains("Scénarios")) {
                main.getScenarioManager().openScenarioMenu(player);
            }
            else if (name.contains("Rôles UHC")) {
                main.getGuiManager().openRolesMenu(player);
            }
        }
        else if (title.contains("Dégâts et effets")) {
            event.setCancelled(true);
            ItemStack current = event.getCurrentItem();
            String name = current.getItemMeta().getDisplayName();
            if (name.contains("Dégâts Critiques :")) {
                if (event.isLeftClick()) {
                    main.setCritDamageMultiplier(main.getCritDamageMultiplier() + 0.2);
                } else if (event.isRightClick()) {
                    main.setCritDamageMultiplier(Math.max(1.0, main.getCritDamageMultiplier() - 0.2));
                }
            }
            if (name.contains("Force :")) {
                if (event.isLeftClick()) {
                    main.setStrengthMultiplier(main.getStrengthMultiplier() + 0.1);
                } else if (event.isRightClick()) {
                    main.setStrengthMultiplier(Math.max(0.1, main.getStrengthMultiplier() - 0.1));
                }
            }
            if (name.contains("Résistance :")) {
                if (event.isLeftClick()) {
                    main.setResistanceMultiplier(main.getResistanceMultiplier() + 0.1);
                } else if (event.isRightClick()) {
                    main.setResistanceMultiplier(Math.max(0.1, main.getResistanceMultiplier() - 0.1));
                }
            }
            if (name.contains("Speed :")) {
                if (event.isLeftClick()) {
                    main.setCritDamageMultiplier(main.getCritDamageMultiplier() + 0.1);
                } else if (event.isRightClick()) {
                    main.setCritDamageMultiplier(Math.max(0.1, main.getCritDamageMultiplier() - 0.1));
                }
            }
            main.getGuiManager().openDamageMenu(player);
            if (name.contains("Retour")) {
                main.getGuiManager().openGameConfigMenu(player);
            }
        }
        else if (title.contains("Configuration des Rôles")) {
            event.setCancelled(true);
            ItemStack current = event.getCurrentItem();
            if (current == null || !current.hasItemMeta()) return;

            int slot = event.getSlot();

            if (slot == 18) {
                main.getGuiManager().openGameConfigMenu(player);
            } else if (slot == 9) {
                main.getGuiManager().openTensuraSettingsMenu(player); // Ton menu de règles
            } else if (slot == 17) {
                main.getGuiManager().openRandomEventsMenu(player); // Ton menu d'events
            } else if (slot == 4) {
                main.getGuiManager().openRoleCategoryMenu(player, "Camp : Solos",Role.Camp.SOLITAIRE);
            } else if (slot == 12) {
                main.getGuiManager().openRoleCategoryMenu(player, "Camp : Monstres",Role.Camp.MONSTRES);}
            else if (slot == 13) {
                main.getGuiManager().openRoleCategoryMenu(player, "Camp : Clowns",Role.Camp.CLOWNS);
            } else if (slot == 22) {
                main.getGuiManager().openRoleCategoryMenu(player, "Camp : Octagramme",Role.Camp.OCTAGRAMME);
            } else if (slot == 14) {
                main.getGuiManager().openRoleCategoryMenu(player, "Camp : Humains",Role.Camp.HUMAINS);
            }
        }
        ItemStack currentItem = event.getCurrentItem();

// Clics pour retourner en arrière depuis un sous-menu de camp
        if (title.startsWith(ChatColor.DARK_GRAY + "Camp : ")) {
            event.setCancelled(true);

            if (currentItem == null || currentItem.getType() == Material.AIR) return;

            // Bouton retour
            if (currentItem.getType() == Material.BARRIER) {
                main.getGuiManager().openRolesMenu(player);
                player.playSound(player.getLocation(), Sound.CLICK, 1.0f, 0.8f);
                return;
            }

            // Modifier la quantité du rôle
            if (currentItem.hasItemMeta() && currentItem.getItemMeta().hasDisplayName()) {
                String rawName = ChatColor.stripColor(currentItem.getItemMeta().getDisplayName());
                Role role = main.getRoleManager().getRoleByName(rawName);

                if (role != null) {
                    // Clic Gauche = +1 / Clic Droit = -1
                    if (event.isLeftClick()) {
                        role.incrementCount();
                        player.playSound(player.getLocation(), Sound.CLICK, 1.0f, 1.5f);
                    } else if (event.isRightClick()) {
                        role.decrementCount();
                        player.playSound(player.getLocation(), Sound.CLICK, 1.0f, 0.7f);
                    }

                    // Réouverture du menu pour rafraîchir la quantité
                    String rawTitle = title.substring((ChatColor.DARK_GRAY.toString()).length());
                    Role.Camp campToPass = rawTitle.equals("Rôles : Composition") ? null : role.getCamp();

                    main.getGuiManager().openRoleCategoryMenu(player, rawTitle, campToPass);
                }
            }
        }
        if (title.equals(ChatColor.DARK_GRAY + "Choisir une couleur")) {
            event.setCancelled(true);

            if (currentItem == null || !currentItem.hasItemMeta() || !currentItem.getItemMeta().hasLore()) return;

            // Récupérer les joueurs ciblés par cette ouverture de GUI
            List<Player> targets = main.getGuiManager().getPendingColorTargets(player);
            if (targets == null || targets.isEmpty()) {
                player.closeInventory();
                return;
            }

            // Récupérer la ChatColor stockée dans le lore
            String colorCode = currentItem.getItemMeta().getLore().get(0);
            ChatColor color = ChatColor.getByChar(colorCode.replace("§", ""));

            // Créer/Récupérer le Scoreboard privé
            if (player.getScoreboard() == Bukkit.getScoreboardManager().getMainScoreboard()) {
                player.setScoreboard(Bukkit.getScoreboardManager().getNewScoreboard());
            }
            Scoreboard personalBoard = player.getScoreboard();

            // Appliquer la couleur à tous les joueurs sélectionnés
            for (Player target : targets) {
                String name = target.getName();
                String safeName = name.length() > 14 ? name.substring(0, 14) : name;
                String teamName = "c_" + safeName;

                Team team = personalBoard.getTeam(teamName);
                if (team == null) {
                    team = personalBoard.registerNewTeam(teamName);
                }
                team.setCanSeeFriendlyInvisibles(false);
// On ajoute bien le VRAI nom du joueur dans la team (pas le nom raccourci)
                team.addEntry(target.getName());
                team.setPrefix(color != null ? color.toString() : "");
            }

            player.playSound(player.getLocation(), Sound.LEVEL_UP, 1.0f, 1.5f);
            player.sendMessage(ChatColor.GOLD + "[TensuraUHC] " + ChatColor.GREEN + "Couleur appliquée à "
                    + targets.size() + " joueur(s).");

            // Fermeture automatique unique du GUI après le choix
            player.closeInventory();
        }
        if (title.equals(ChatColor.DARK_GRAY + "Sélectionner un joueur")) {
            event.setCancelled(true);

            if (currentItem == null || currentItem.getType() != Material.SKULL_ITEM || !currentItem.hasItemMeta()) return;

            // Récupère le nom du joueur ciblé via le nom de l'item
            String targetName = ChatColor.stripColor(currentItem.getItemMeta().getDisplayName());
            Player target = Bukkit.getPlayer(targetName);

            if (target != null) {
                player.playSound(player.getLocation(), Sound.CLICK, 1.0f, 1.0f);
                // Ouvre le GUI des couleurs pour ce joueur spécifique
                main.getGuiManager().openColorPickerMenu(player, Collections.singletonList(target));
            } else {
                player.sendMessage(ChatColor.RED + "Ce joueur n'est plus en ligne.");
                player.closeInventory();
            }
        }
        // -------------------------------------------------------------
        // 3. MENU "Configuration des Paramètres" (Tensura Settings)
        // -------------------------------------------------------------
        if (title.equals(ChatColor.DARK_GRAY + "Configuration des Paramètres")) {
            event.setCancelled(true);

            if (currentItem.getType() == Material.BARRIER) {
                main.getGuiManager().openRolesMenu(player);
                player.playSound(player.getLocation(), Sound.CLICK, 1.0f, 0.8f);
            } else {
                // TODO: Ajouter la logique de tes paramètres Tensura
            }
            return;
        }

        // -------------------------------------------------------------
        // 4. MENU "Configuration des Events" (Random Events)
        // -------------------------------------------------------------
        if (title.equals(ChatColor.DARK_GRAY + "Configuration des Events")) {
            event.setCancelled(true);

            if (currentItem.getType() == Material.BARRIER) {
                main.getGuiManager().openRolesMenu(player);
                player.playSound(player.getLocation(), Sound.CLICK, 1.0f, 0.8f);
            } else {
                // TODO: Ajouter la logique de tes évènements aléatoires
            }
            return;
        }
        // 3. Sous-Menu Limites de la Partie
        else if (title.equals(ChatColor.DARK_GRAY + "Limites de la Partie")) {
            event.setCancelled(true);
            ItemStack current = event.getCurrentItem();
            if (current == null || current.getType() == Material.AIR) return;

            int slot = event.getSlot();

            if (current.getType() == Material.BARRIER) {
                main.getGuiManager().openGameConfigMenu(player);
                return;
            }

            if (slot == 11) { // Pièces de diamant
                int change = event.isLeftClick() ? 1 : -1;
                main.setMaxDiamondArmorPieces(main.getMaxDiamondArmorPieces() + change);
                main.getGuiManager().openLimitsConfigMenu(player);
            } else if (slot == 22) { // Diamants minés max
                int change = event.isLeftClick() ? 1 : -1;
                main.setMaxMinedDiamonds(main.getMaxMinedDiamonds() + change);
                main.getGuiManager().openLimitsConfigMenu(player);
            } else if (slot == 15) { // Limite des Enchantements (2 slots après le diamant miné)
                main.getGuiManager().openIronEnchantMenu(player);
            } else if (slot == 4) { // drop custom
                main.getGuiManager().openDropMenu(player);
            }
        }
        else if (title.contains("Configuration des Drops")) {
            event.setCancelled(true);
            ItemStack current = event.getCurrentItem();
            if (current == null || !current.hasItemMeta()) return;

            int slot = event.getSlot();

            if (slot == 18) { // Bouton Retour
                main.getGuiManager().openGameConfigMenu(player);
                return;
            }

            // Détermination de la variation en fonction du clic (+/- 5% ou +/- 10%)
            int delta = 0;
            if (event.isLeftClick()) {
                delta = event.isShiftClick() ? 10 : 5;
            } else if (event.isRightClick()) {
                delta = event.isShiftClick() ? -10 : -5;
            }

            if (delta == 0) return;

            if (slot == 10) {
                int newPercent = Math.max(0, Math.min(100, main.getAppleDropPercent() + delta));
                main.setAppleDropPercent(newPercent);
            } else if (slot == 12) {
                int newPercent = Math.max(0, Math.min(100, main.getFlintDropPercent() + delta));
                main.setFlintDropPercent(newPercent);
            } else if (slot == 14) {
                int newPercent = Math.max(0, Math.min(100, main.getEnderPearlDropPercent() + delta));
                main.setEnderPearlDropPercent(newPercent);
            }

            // Rafraîchit le menu après modification
            main.getGuiManager().openDropMenu(player);
        }
        // 4. Sous-menu Configuration des Timers
        else if (title.equals(ChatColor.DARK_GRAY + "Configuration des Timers")) {
            event.setCancelled(true);
            ItemStack current = event.getCurrentItem();
            if (current == null || !current.hasItemMeta()) return;

            String name = current.getItemMeta().getDisplayName();

            if (name.startsWith(ChatColor.GOLD + "Durée de l'Épisode")) {
                main.getGuiManager().openEpisodeTimerEditMenu(player);
            } else if (name.startsWith(ChatColor.LIGHT_PURPLE + "Annonce des Rôles")) {
                main.getGuiManager().openRoleTimerEditMenu(player);
            } else if (name.startsWith(ChatColor.RED + "Activation du PvP")) {
                main.getGuiManager().openPvpTimerEditMenu(player);
            } else if (name.startsWith(ChatColor.GOLD + "Timer Meetup")) {
                main.getGuiManager().openMeetupTimerEditMenu(player);
            } else if (name.equals(ChatColor.YELLOW + "Cycle Jour / Nuit")) {
                main.getGuiManager().openDayNightConfigMenu(player);
            } else if (name.equals(ChatColor.RED + "Gestion de la Bordure")) {
                main.getGuiManager().openBorderConfigMenu(player);
            } else if (name.equals(ChatColor.RED + "Retour aux paramètres")) {
                main.getGuiManager().openGameConfigMenu(player);
            } else if (name.startsWith(ChatColor.GOLD + "Timer FinalHeal")) {
                main.getGuiManager().openFinalHealTimerEditMenu(player);
            }
        }

        // 4.1. Réglage : Bordure
        else if (title.equals(ChatColor.DARK_GRAY + "Réglage : Bordure")) {
            event.setCancelled(true);
            ItemStack current = event.getCurrentItem();
            if (current == null || !current.hasItemMeta()) return;

            String name = current.getItemMeta().getDisplayName();
            boolean isLeft = event.isLeftClick();
            boolean isRight = event.isRightClick();

            if (name.startsWith(ChatColor.GOLD + "Taille Max")) {
                if (isLeft) main.setBorderInitialSize(main.getBorderInitialSize() + 100);
                else if (isRight) main.setBorderInitialSize(Math.max(100, main.getBorderInitialSize() - 100));
                main.getGuiManager().openBorderConfigMenu(player);
            } else if (name.startsWith(ChatColor.RED + "Taille Min")) {
                if (isLeft) main.setBorderFinalSize(main.getBorderFinalSize() + 50);
                else if (isRight) main.setBorderFinalSize(Math.max(10, main.getBorderFinalSize() - 50));
                main.getGuiManager().openBorderConfigMenu(player);
            } else if (name.startsWith(ChatColor.YELLOW + "Temps de Réduction")) {
                if (isLeft) main.setBorderShrinkDuration(main.getBorderShrinkDuration() + 60);
                else if (isRight) main.setBorderShrinkDuration(Math.max(60, main.getBorderShrinkDuration() - 60));
                main.getGuiManager().openBorderConfigMenu(player);
            } else if (name.startsWith(ChatColor.AQUA + "Type :")) {
                main.setBorderInstant(!main.isBorderInstant());
                main.getGuiManager().openBorderConfigMenu(player);
            } else if (name.equals(ChatColor.RED + "Retour aux timers")) {
                main.getGuiManager().openTimersConfigMenu(player);
            }
        }

        // 5. Réglage : Épisodes
        else if (title.equals(ChatColor.DARK_GRAY + "Réglage : Épisodes")) {
            event.setCancelled(true);
            ItemStack current = event.getCurrentItem();
            if (current == null || !current.hasItemMeta()) return;

            String name = current.getItemMeta().getDisplayName();
            boolean isLeft = event.isLeftClick();
            boolean isRight = event.isRightClick();

            if (name.contains("15 Secondes")) {
                if (isLeft) main.setEpisodeLengthSeconds(main.getEpisodeLengthSeconds() + 15);
                else if (isRight) main.setEpisodeLengthSeconds(Math.max(15, main.getEpisodeLengthSeconds() - 15));
                main.getGuiManager().openEpisodeTimerEditMenu(player);
            } else if (name.contains("1 Minute")) {
                if (isLeft) main.setEpisodeLengthSeconds(main.getEpisodeLengthSeconds() + 60);
                else if (isRight) main.setEpisodeLengthSeconds(Math.max(15, main.getEpisodeLengthSeconds() - 60));
                main.getGuiManager().openEpisodeTimerEditMenu(player);
            } else if (name.contains("10 Minutes")) {
                if (isLeft) main.setEpisodeLengthSeconds(main.getEpisodeLengthSeconds() + 600);
                else if (isRight) main.setEpisodeLengthSeconds(Math.max(15, main.getEpisodeLengthSeconds() - 600));
                main.getGuiManager().openEpisodeTimerEditMenu(player);
            } else if (name.equals(ChatColor.RED + "Retour aux timers")) {
                main.getGuiManager().openTimersConfigMenu(player);
            }
        }

        // 6. Réglage : Rôles
        else if (title.equals(ChatColor.DARK_GRAY + "Réglage : Rôles")) {
            event.setCancelled(true);
            ItemStack current = event.getCurrentItem();
            if (current == null || !current.hasItemMeta()) return;

            String name = current.getItemMeta().getDisplayName();
            boolean isLeft = event.isLeftClick();
            boolean isRight = event.isRightClick();

            if (name.contains("15 Secondes")) {
                if (isLeft) main.setRoleTime(main.GetRoleTime() + 15);
                else if (isRight) main.setRoleTime(Math.max(15, main.GetRoleTime() - 15));
                main.getGuiManager().openRoleTimerEditMenu(player);
            } else if (name.contains("1 Minute")) {
                if (isLeft) main.setRoleTime(main.GetRoleTime() + 60);
                else if (isRight) main.setRoleTime(Math.max(15, main.GetRoleTime() - 60));
                main.getGuiManager().openRoleTimerEditMenu(player);
            } else if (name.contains("10 Minutes")) {
                if (isLeft) main.setRoleTime(main.GetRoleTime() + 600);
                else if (isRight) main.setRoleTime(Math.max(15, main.GetRoleTime() - 600));
                main.getGuiManager().openRoleTimerEditMenu(player);
            } else if (name.equals(ChatColor.RED + "Retour aux timers")) {
                main.getGuiManager().openTimersConfigMenu(player);
            }
        }

        else if (title.equals(ChatColor.DARK_GRAY + "Réglage : FinalHeal")) {
            event.setCancelled(true);
            ItemStack current = event.getCurrentItem();
            if (current == null || !current.hasItemMeta()) return;

            String name = current.getItemMeta().getDisplayName();
            boolean isLeft = event.isLeftClick();
            boolean isRight = event.isRightClick();

            if (name.contains("15 Secondes")) {
                if (isLeft) main.setFinalHealTime(main.GetFinalHealTime() + 15);
                else if (isRight) main.setFinalHealTime(Math.max(15, main.GetFinalHealTime() - 15));
                main.getGuiManager().openFinalHealTimerEditMenu(player);
            } else if (name.contains("1 Minute")) {
                if (isLeft) main.setFinalHealTime(main.GetFinalHealTime() + 60);
                else if (isRight) main.setFinalHealTime(Math.max(15, main.GetFinalHealTime() - 60));
                main.getGuiManager().openFinalHealTimerEditMenu(player);
            } else if (name.contains("10 Minutes")) {
                if (isLeft) main.setFinalHealTime(main.GetFinalHealTime() + 600);
                else if (isRight) main.setFinalHealTime(Math.max(15, main.GetFinalHealTime() - 600));
                main.getGuiManager().openFinalHealTimerEditMenu(player);
            } else if (name.equals(ChatColor.RED + "Retour aux timers")) {
                main.getGuiManager().openTimersConfigMenu(player);
            }
        }
        // 7. Réglage : PvP
        else if (title.equals(ChatColor.DARK_GRAY + "Réglage : PvP")) {
            event.setCancelled(true);
            ItemStack current = event.getCurrentItem();
            if (current == null || !current.hasItemMeta()) return;

            String name = current.getItemMeta().getDisplayName();
            boolean isLeft = event.isLeftClick();
            boolean isRight = event.isRightClick();

            if (name.contains("15 Secondes")) {
                if (isLeft) main.setPvPTime(main.GetPvpTime() + 15);
                else if (isRight) main.setPvPTime(Math.max(15, main.GetPvpTime() - 15));
                main.getGuiManager().openPvpTimerEditMenu(player);
            } else if (name.contains("1 Minute")) {
                if (isLeft) main.setPvPTime(main.GetPvpTime() + 60);
                else if (isRight) main.setPvPTime(Math.max(15, main.GetPvpTime() - 60));
                main.getGuiManager().openPvpTimerEditMenu(player);
            } else if (name.contains("10 Minutes")) {
                if (isLeft) main.setPvPTime(main.GetPvpTime() + 600);
                else if (isRight) main.setPvPTime(Math.max(15, main.GetPvpTime() - 600));
                main.getGuiManager().openPvpTimerEditMenu(player);
            } else if (name.equals(ChatColor.RED + "Retour aux timers")) {
                main.getGuiManager().openTimersConfigMenu(player);
            }
        }

        // 8. Réglage : Meetup
        else if (title.equals(ChatColor.DARK_GRAY + "Réglage : Meetup")) {
            event.setCancelled(true);
            ItemStack current = event.getCurrentItem();
            if (current == null || !current.hasItemMeta()) return;

            String name = current.getItemMeta().getDisplayName();
            boolean isLeft = event.isLeftClick();
            boolean isRight = event.isRightClick();

            if (name.contains("15 Secondes")) {
                if (isLeft) main.setMeetupTime(main.getMeetupTime() + 15);
                else if (isRight) main.setMeetupTime(Math.max(15, main.getMeetupTime() - 15));
                main.getGuiManager().openMeetupTimerEditMenu(player);
            } else if (name.contains("1 Minute")) {
                if (isLeft) main.setMeetupTime(main.getMeetupTime() + 60);
                else if (isRight) main.setMeetupTime(Math.max(15, main.getMeetupTime() - 60));
                main.getGuiManager().openMeetupTimerEditMenu(player);
            } else if (name.contains("10 Minutes")) {
                if (isLeft) main.setMeetupTime(main.getMeetupTime() + 600);
                else if (isRight) main.setMeetupTime(Math.max(15, main.getMeetupTime() - 600));
                main.getGuiManager().openMeetupTimerEditMenu(player);
            } else if (name.equals(ChatColor.RED + "Retour aux timers")) {
                main.getGuiManager().openTimersConfigMenu(player);
            }
        }

        // 9. Réglage : Jour / Nuit
        else if (title.equals(ChatColor.DARK_GRAY + "Réglage : Jour / Nuit")) {
            event.setCancelled(true);
            ItemStack current = event.getCurrentItem();
            if (current == null || !current.hasItemMeta()) return;

            String name = current.getItemMeta().getDisplayName();
            boolean isLeft = event.isLeftClick();
            boolean isRight = event.isRightClick();

            if (name.contains("Durée du cycle")) {
                if (isLeft) main.setDayNightCycleSeconds(main.getDayNightCycleSeconds() + 60);
                else if (isRight) main.setDayNightCycleSeconds(Math.max(60, main.getDayNightCycleSeconds() - 60));
                main.getGuiManager().openDayNightConfigMenu(player);
            } else if (name.contains("% Jour")) {
                if (isLeft) main.setDayPercent(Math.min(95, main.getDayPercent() + 5));
                else if (isRight) main.setDayPercent(Math.max(5, main.getDayPercent() - 5));
                main.getGuiManager().openDayNightConfigMenu(player);
            } else if (name.equals(ChatColor.RED + "Retour aux timers")) {
                main.getGuiManager().openTimersConfigMenu(player);
            }
        }

        // 10. Sous-Menu Monde
        else if (title.equals(ChatColor.DARK_GRAY + "Configuration du Monde")) {
            event.setCancelled(true);
            ItemStack current = event.getCurrentItem();
            if (current == null || !current.hasItemMeta()) return;

            String name = current.getItemMeta().getDisplayName();

            if (name.startsWith(ChatColor.GOLD + "Taille des Grottes")) {
                if (event.isLeftClick()) main.setCaveSizePercent(Math.min(300, main.getCaveSizePercent() + 50));
                else if (event.isRightClick()) main.setCaveSizePercent(Math.max(50, main.getCaveSizePercent() - 50));
                main.getGuiManager().openWorldConfigMenu(player);
            } else if (name.equals(ChatColor.AQUA + "Réglage des Minerais")) {
                main.getGuiManager().openOreConfigMenu(player);
            } else if (name.startsWith(ChatColor.GREEN + "Biome Centre")) {
                main.getGuiManager().openWorldConfigMenu(player);
            } else if (name.equals(ChatColor.YELLOW + "Générer & Téléporter au Monde")) {
                player.closeInventory();
                main.createGameWorld();

                World world = main.getGameWorld();

                main.getBorderManager().setupInitialBorder(world);

                int borderRadius = main.getBorderInitialSize() / 2;
                main.pregenerateWorld(world, borderRadius, () -> {
                    int y = world.getHighestBlockYAt(0, 0) + 10;
                    Location previewLoc = new Location(world, 0, y, 0);
                    player.teleport(previewLoc);
                    player.setGameMode(GameMode.SPECTATOR);

                    player.sendMessage(ChatColor.GREEN + "[TensuraUHC] Le monde est entièrement prêt !");
                    player.sendMessage((ChatColor.GRAY + "Pour revenir au lobby, écrivez /lobby"));
                });
            } else if (name.equals(ChatColor.RED + "Retour au menu principal")) {
                main.getGuiManager().openUHCMenu(player);
            }
        }

        // 11. Sous-Menu Minerais
        else if (title.equals(ChatColor.DARK_GRAY + "Configuration Minerais")) {
            event.setCancelled(true);
            ItemStack current = event.getCurrentItem();
            if (current == null || !current.hasItemMeta()) return;

            String name = current.getItemMeta().getDisplayName();
            boolean isLeft = event.isLeftClick();
            boolean isRight = event.isRightClick();

            if (name.contains("Diamant")) {
                if (isLeft) main.setDiamondPercent(Math.min(300, main.getDiamondPercent() + 50));
                else if (isRight) main.setDiamondPercent(Math.max(0, main.getDiamondPercent() - 50));
                main.getGuiManager().openOreConfigMenu(player);
            } else if (name.contains("Or")) {
                if (isLeft) main.setGoldPercent(Math.min(300, main.getGoldPercent() + 50));
                else if (isRight) main.setGoldPercent(Math.max(0, main.getGoldPercent() - 50));
                main.getGuiManager().openOreConfigMenu(player);
            } else if (name.contains("Fer")) {
                if (isLeft) main.setIronPercent(Math.min(300, main.getIronPercent() + 50));
                else if (isRight) main.setIronPercent(Math.max(0, main.getIronPercent() - 50));
                main.getGuiManager().openOreConfigMenu(player);
            } else if (name.contains("Redstone")) {
                if (isLeft) main.setRedstonePercent(Math.min(300, main.getRedstonePercent() + 50));
                else if (isRight) main.setRedstonePercent(Math.max(0, main.getRedstonePercent() - 50));
                main.getGuiManager().openOreConfigMenu(player);
            } else if (name.contains("Lapis-Lazuli")) {
                if (isLeft) main.setLapisPercent(Math.min(300, main.getLapisPercent() + 50));
                else if (isRight) main.setLapisPercent(Math.max(0, main.getLapisPercent() - 50));
                main.getGuiManager().openOreConfigMenu(player);
            } else if (name.contains("Charbon")) {
                if (isLeft) main.setCoalPercent(Math.min(300, main.getCoalPercent() + 50));
                else if (isRight) main.setCoalPercent(Math.max(0, main.getCoalPercent() - 50));
                main.getGuiManager().openOreConfigMenu(player);
            } else if (name.contains("Émeraude")) {
                if (isLeft) main.setEmeraldPercent(Math.min(300, main.getEmeraldPercent() + 50));
                else if (isRight) main.setEmeraldPercent(Math.max(0, main.getEmeraldPercent() - 50));
                main.getGuiManager().openOreConfigMenu(player);
            } else if (name.contains("XP")) {
                if (isLeft) main.setXpPercent(Math.min(300, main.getXpPercent() + 50));
                else if (isRight) main.setXpPercent(Math.max(0, main.getXpPercent() - 50));
                main.getGuiManager().openOreConfigMenu(player);
            } else if (name.equals(ChatColor.RED + "Retour")) {
                main.getGuiManager().openWorldConfigMenu(player);
            }
        }
        else if (title.equals(ScenarioManager.GUI_TITLE)) {
            event.setCancelled(true);
            ItemStack current = event.getCurrentItem();
            if (current == null || !current.hasItemMeta()) return;

            int slot = event.getSlot();

            if (slot == 26) { // Bouton Retour
                main.getGuiManager().openGameConfigMenu(player);
                return;
            }

            String displayName = ChatColor.stripColor(current.getItemMeta().getDisplayName());
            Scenario scenario = main.getScenarioManager().getScenario(displayName);

            if (scenario != null) {
                // --- RESTRICTION ORE MAGNET ---
                if (scenario.getName().equalsIgnoreCase("OreMagnet") && !scenario.isEnabled()) {
                    Scenario cutClean = main.getScenarioManager().getScenario("CutClean");
                    if (cutClean == null || !cutClean.isEnabled()) {
                        player.sendMessage(ChatColor.RED + " [TensuraUHC] Le scénario OreMagnet nécessite l'activation préalable de CutClean !");
                        return; // Empêche l'activation
                    }
                }
                // ------------------------------

                scenario.toggle();
                main.getScenarioManager().openScenarioMenu(player); // Rafraîchit le menu
            }
        }
        // 12. Restriction des Objets
        else if (title.equals(ChatColor.DARK_GRAY + "Restriction des Objets")) {
            event.setCancelled(true);
            ItemStack current = event.getCurrentItem();
            if (current == null || current.getType() == Material.AIR) return;

            if (current.hasItemMeta() && current.getType() == Material.BARRIER && current.getItemMeta().getDisplayName().contains("Retour")) {
                main.getGuiManager().openGameConfigMenu(player);
                return;
            }

            Material clickedMat = current.getType();

            if (clickedMat == Material.GOLDEN_APPLE && current.getDurability() == 1) {
                main.toggleNotchAppleDisabled();
            } else {
                main.toggleItemDisabled(clickedMat);
            }

            main.getGuiManager().openItemRestrictionMenu(player);
        }

        // 13. Sous-Menu Enchants Max : FER
        else if (title.equals(ChatColor.DARK_GRAY + "Enchants Max : FER")) {
            event.setCancelled(true);
            int slot = event.getSlot();

            // Switch vers ARC (Slot 4) ou DIAMANT (Slot 8)
            if (slot == 4) {
                main.getGuiManager().openBowEnchantMenu(player);
                return;
            } else if (slot == 8) {
                main.getGuiManager().openDiamondEnchantMenu(player);
                return;
            }

            // Retour aux Limites de la Partie (Slot 26)
            if (slot == 26) {
                main.getGuiManager().openLimitsConfigMenu(player);
                return;
            }

            // Gestion de l'incrémentation/décrémentation des enchants
            if (slot == 10 || slot == 12 || slot == 14 || slot == 16) {
                int delta = event.isLeftClick() ? 1 : -1;
                if (slot == 10) main.setIronSharpnessMax(clamp(main.getIronSharpnessMax() + delta, 0, 5));
                if (slot == 12) main.setIronProtectionMax(clamp(main.getIronProtectionMax() + delta, 0, 4));
                if (slot == 14) main.setIronFireMax(clamp(main.getIronFireMax() + delta, 0, 2));
                if (slot == 16) main.setIronKnockbackMax(clamp(main.getIronKnockbackMax() + delta, 0, 2));
                main.getGuiManager().openIronEnchantMenu(player);
            }
        }

// 14. Sous-Menu Enchants Max : DIAMANT
        else if (title.equals(ChatColor.DARK_GRAY + "Enchants Max : DIAMANT")) {
            event.setCancelled(true);
            int slot = event.getSlot();

            // Switch vers FER (Slot 0) ou ARC (Slot 4)
            if (slot == 0) {
                main.getGuiManager().openIronEnchantMenu(player);
                return;
            } else if (slot == 4) {
                main.getGuiManager().openBowEnchantMenu(player);
                return;
            }

            // Retour aux Limites de la Partie (Slot 26)
            if (slot == 26) {
                main.getGuiManager().openLimitsConfigMenu(player);
                return;
            }

            // Gestion de l'incrémentation/décrémentation des enchants
            if (slot == 10 || slot == 12 || slot == 14 || slot == 16) {
                int delta = event.isLeftClick() ? 1 : -1;
                if (slot == 10) main.setDiamondSharpnessMax(clamp(main.getDiamondSharpnessMax() + delta, 0, 5));
                if (slot == 12) main.setDiamondProtectionMax(clamp(main.getDiamondProtectionMax() + delta, 0, 4));
                if (slot == 14) main.setDiamondFireMax(clamp(main.getDiamondFireMax() + delta, 0, 2));
                if (slot == 16) main.setDiamondKnockbackMax(clamp(main.getDiamondKnockbackMax() + delta, 0, 2));
                main.getGuiManager().openDiamondEnchantMenu(player);
            }
        }
        // --- GUI FAÇADE YUUKI ---
        if (title.equals(ChatColor.DARK_GRAY + "Façade : Rôles Monstres")) {
            event.setCancelled(true);

            if (currentItem == null || !currentItem.hasItemMeta() || !currentItem.getItemMeta().hasDisplayName()) return;

            Role playerRole = main.getRoleManager().getPlayerRole(player.getUniqueId());
            if (!(playerRole instanceof YuukiRole)) {
                player.closeInventory();
                return;
            }

            YuukiRole yuuki = (YuukiRole) playerRole;
            String chosenRoleName = ChatColor.stripColor(currentItem.getItemMeta().getDisplayName());
            Role targetRole = main.getRoleManager().getRoleByName(chosenRoleName);

            if (targetRole != null) {
                yuuki.setFakeRoleName(targetRole.getName());
                player.playSound(player.getLocation(), Sound.LEVEL_UP, 1.0f, 1.2f);
                player.sendMessage(ChatColor.GOLD + "[Yuuki] " + ChatColor.GREEN + "Votre rôle de façade est désormais : "
                        + ChatColor.YELLOW + targetRole.getName() + ChatColor.GREEN + ".");
            }

            player.closeInventory();
        }

// 15. Sous-Menu Enchants Max : ARC
        else if (title.equals(ChatColor.DARK_GRAY + "Enchants Max : ARC")) {
            event.setCancelled(true);
            int slot = event.getSlot();

            // Switch vers FER (Slot 0) ou DIAMANT (Slot 8)
            if (slot == 0) {
                main.getGuiManager().openIronEnchantMenu(player);
                return;
            } else if (slot == 8) {
                main.getGuiManager().openDiamondEnchantMenu(player);
                return;
            }

            // Retour aux Limites de la Partie (Slot 26)
            if (slot == 26) {
                main.getGuiManager().openLimitsConfigMenu(player);
                return;
            }

            // Gestion de l'incrémentation/décrémentation des enchants d'arc
            if (slot == 11 || slot == 13 || slot == 15) {
                int delta = event.isLeftClick() ? 1 : -1;
                if (slot == 11) main.setBowPowerMax(clamp(main.getBowPowerMax() + delta, 0, 5));
                if (slot == 13) main.setBowFlameMax(clamp(main.getBowFlameMax() + delta, 0, 2));
                if (slot == 15) main.setBowPunchMax(clamp(main.getBowPunchMax() + delta, 0, 2));
                main.getGuiManager().openBowEnchantMenu(player);
            }
        }
    }

    @EventHandler
    public void onInventoryClose(org.bukkit.event.inventory.InventoryCloseEvent event) {
        String title = event.getView().getTitle();

        if (title.equals(ChatColor.DARK_GRAY + "Éditeur : Starter Kit")) {
            ItemStack[] items = event.getInventory().getContents();
            main.setStarterKit(items);
            event.getPlayer().sendMessage(ChatColor.GOLD + "[TensuraUHC] " + ChatColor.GREEN + "L'inventaire de départ a été sauvegardé avec succès !");
        }
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

    private void applyEnchantment(Player player, ItemStack item, org.bukkit.enchantments.Enchantment enchant, int maxLevel, boolean isLeftClick) {
        int currentLevel = item.getEnchantmentLevel(enchant);

        if (isLeftClick) {
            int newLevel = (currentLevel >= maxLevel) ? 1 : currentLevel + 1;
            item.addUnsafeEnchantment(enchant, newLevel);
            player.sendMessage(ChatColor.GOLD + "[TensuraUHC] " + ChatColor.GREEN + enchant.getName() + " défini au niveau " + newLevel);
        } else {
            item.removeEnchantment(enchant);
            player.sendMessage(ChatColor.GOLD + "[TensuraUHC] " + ChatColor.RED + enchant.getName() + " retiré de l'objet.");
        }
    }

    private int clamp(int val, int min, int max) {
        return Math.max(min, Math.min(max, val));
    }
}