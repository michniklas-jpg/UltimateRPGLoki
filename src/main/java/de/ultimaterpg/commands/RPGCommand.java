
package de.ultimaterpg.commands;

import de.ultimaterpg.UltimateRPG;
import de.ultimaterpg.manager.DatabaseManager;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.*;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class RPGCommand implements CommandExecutor, TabCompleter {

    private final UltimateRPG plugin;

    public RPGCommand(UltimateRPG plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(
            CommandSender sender,
            Command command,
            String label,
            String[] args
    ) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Only players can use this command.");
            return true;
        }

        if (!player.hasPermission("ultimaterpg.use")) {
            player.sendMessage("§cKeine Berechtigung.");
            return true;
        }

        if (args.length == 0) {
            openMenu(player);
            return true;
        }

        if (args[0].equalsIgnoreCase("stats")) {
            showStats(player);
            return true;
        }

        if (args[0].equalsIgnoreCase("reload")) {
            if (!player.hasPermission("ultimaterpg.admin")) {
                player.sendMessage("§cKeine Berechtigung.");
                return true;
            }

            plugin.reloadConfig();
            player.sendMessage("§aUltimateRPG Config neu geladen.");
            return true;
        }

        player.sendMessage("§cVerwendung: /rpg [stats|reload]");
        return true;
    }

    private void openMenu(Player player) {
        Inventory inventory = Bukkit.createInventory(
                null,
                54,
                "§8✦ §6UltimateRPG"
        );

        addItem(inventory, 10, Material.NETHERITE_SWORD,
                "§cBoss-Waffen",
                "§7Einzigartige Boss-Waffen");

        addItem(inventory, 12, Material.WRITABLE_BOOK,
                "§eQuests",
                "§7Deine täglichen und wöchentlichen Aufgaben");

        addItem(inventory, 14, Material.GOLD_INGOT,
                "§6Battle Pass",
                "§7Deine Season-Belohnungen");

        addItem(inventory, 16, Material.END_PORTAL_FRAME,
                "§5Rifts",
                "§7Betritt magische Dungeons");

        addItem(inventory, 29, Material.METEOR,
                "§bMeteoriten",
                "§7Entdecke seltene Ressourcen");

        addItem(inventory, 31, Material.ENCHANTED_BOOK,
                "§dSkill Tree",
                "§7Verbessere deine Fähigkeiten");

        addItem(inventory, 33, Material.PLAYER_HEAD,
                "§aSpielerprofil",
                "§7Deine RPG-Statistiken");

        player.openInventory(inventory);
    }

    private void addItem(
            Inventory inventory,
            int slot,
            Material material,
            String name,
            String lore
    ) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();

        if (meta != null) {
            meta.setDisplayName(name);
            meta.setLore(List.of("", lore, "", "§eKlicke zum Öffnen"));
            item.setItemMeta(meta);
        }

        inventory.setItem(slot, item);
    }

    private void showStats(Player player) {
        DatabaseManager.PlayerRecord profile =
                plugin.getPlayerDataManager().getProfile(
                        player.getUniqueId()
                );

        if (profile == null) {
            player.sendMessage("§cDein Profil wird noch geladen.");
            return;
        }

        player.sendMessage("§8§m------------------------");
        player.sendMessage("§6UltimateRPG §7- Profil");
        player.sendMessage("§7Level: §e" + profile.level());
        player.sendMessage("§7Coins: §6" + profile.coins());
        player.sendMessage("§7XP: §b" + profile.xp());
        player.sendMessage(
                "§7Skillpunkte: §d" + profile.skillPoints()
        );
        player.sendMessage(
                "§7Battle Pass: §e" + profile.battlepassLevel()
        );
        player.sendMessage("§8§m------------------------");
    }

    @Override
    public List<String> onTabComplete(
            CommandSender sender,
            Command command,
            String alias,
            String[] args
    ) {
        if (args.length == 1) {
            List<String> options = new ArrayList<>(
                    List.of("stats")
            );

            if (sender.hasPermission("ultimaterpg.admin")) {
                options.add("reload");
            }

            return options.stream()
                    .filter(option -> option.startsWith(
                            args[0].toLowerCase()
                    ))
                    .toList();
        }

        return List.of();
    }
}
