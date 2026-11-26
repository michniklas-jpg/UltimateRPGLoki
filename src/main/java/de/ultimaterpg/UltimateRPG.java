
package de.ultimaterpg;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;

public final class UltimateRPG extends JavaPlugin {

    private static UltimateRPG instance;

    @Override
    public void onEnable() {
        instance = this;

        saveDefaultConfig();

        getLogger().info("================================");
        getLogger().info("       UltimateRPG");
        getLogger().info("================================");
        getLogger().info("Plugin erfolgreich gestartet!");
        getLogger().info("Minecraft: Paper 1.21.11");
        getLogger().info("================================");
    }

    @Override
    public void onDisable() {
        getLogger().info("UltimateRPG wurde beendet!");
        instance = null;
    }

    public static UltimateRPG getInstance() {
        return instance;
    }

    @Override
    public boolean onCommand(
            CommandSender sender,
            Command command,
            String label,
            String[] args
    ) {
        if (command.getName().equalsIgnoreCase("urpg")) {

            sender.sendMessage("");
            sender.sendMessage(ChatColor.DARK_PURPLE
                    + "====== UltimateRPG ======");

            sender.sendMessage(ChatColor.LIGHT_PURPLE
                    + "Version: 1.0.0");

            sender.sendMessage(ChatColor.GRAY
                    + "Das RPG-Abenteuer beginnt!");

            sender.sendMessage(ChatColor.DARK_PURPLE
                    + "=========================");

            return true;
        }

        return false;
    }
}
