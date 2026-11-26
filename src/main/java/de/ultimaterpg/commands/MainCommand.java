
package de.ultimaterpg.commands;

import de.ultimaterpg.UltimateRPG;
import de.ultimaterpg.gui.MainMenu;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class MainCommand implements CommandExecutor {

    private final UltimateRPG plugin;
    private final MainMenu mainMenu;

    public MainCommand(UltimateRPG plugin) {
        this.plugin = plugin;
        this.mainMenu = new MainMenu(plugin);
    }

    @Override
    public boolean onCommand(
            CommandSender sender,
            Command command,
            String label,
            String[] args
    ) {

        if (args.length == 0 || args[0].equalsIgnoreCase("menu")) {

            if (!(sender instanceof Player player)) {
                sender.sendMessage("Nur Spieler können dieses Menü öffnen.");
                return true;
            }

            mainMenu.open(player);
            return true;
        }

        if (args[0].equalsIgnoreCase("help")) {
            sender.sendMessage(ChatColor.LIGHT_PURPLE + "UltimateRPG Commands");
            sender.sendMessage("/urpg");
            sender.sendMessage("/urpg menu");
            sender.sendMessage("/urpg stats");
            sender.sendMessage("/urpg quests");
            sender.sendMessage("/urpg battlepass");
            sender.sendMessage("/urpg weapons");
            sender.sendMessage("/urpg skills");
            sender.sendMessage("/urpg rift");
            sender.sendMessage("/urpg boss");
            sender.sendMessage("/urpg meteorite");
            return true;
        }

        if (args[0].equalsIgnoreCase("reload")) {

            if (!sender.hasPermission("ultimaterpg.admin")) {
                sender.sendMessage(ChatColor.RED + "Keine Berechtigung!");
                return true;
            }

            plugin.getConfigManager().reloadConfigs();
            sender.sendMessage(ChatColor.GREEN + "Konfiguration neu geladen!");
            return true;
        }

        sender.sendMessage(ChatColor.RED + "Unbekannter Befehl!");
        sender.sendMessage(ChatColor.GRAY + "Benutze /urpg help");

        return true;
    }
}
