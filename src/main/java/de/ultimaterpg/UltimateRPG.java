
package de.ultimaterpg;

import de.ultimaterpg.commands.MainCommand;
import de.ultimaterpg.manager.ConfigManager;

import org.bukkit.plugin.java.JavaPlugin;

public final class UltimateRPG extends JavaPlugin {

    private static UltimateRPG instance;
    private ConfigManager configManager;

    @Override
    public void onEnable() {

        instance = this;

        saveDefaultConfig();

        configManager = new ConfigManager(this);
        configManager.loadConfigs();

        getCommand("urpg").setExecutor(new MainCommand(this));

        getLogger().info("UltimateRPG erfolgreich gestartet!");
    }

    @Override
    public void onDisable() {

        getLogger().info("UltimateRPG wurde beendet!");

        instance = null;
    }

    public static UltimateRPG getInstance() {
        return instance;
    }

    public ConfigManager getConfigManager() {
        return configManager;
    }
}
