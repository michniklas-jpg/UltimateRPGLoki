
package de.ultimaterpg.manager;

import de.ultimaterpg.UltimateRPG;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class ConfigManager {

    private final UltimateRPG plugin;
    private final Map<String, FileConfiguration> configs = new HashMap<>();
    private final Map<String, File> files = new HashMap<>();

    public ConfigManager(UltimateRPG plugin) {
        this.plugin = plugin;
    }

    public void loadConfigs() {
        loadConfig("weapons.yml");
        loadConfig("bosses.yml");
        loadConfig("quests.yml");
        loadConfig("skills.yml");
        loadConfig("events.yml");
    }

    private void loadConfig(String name) {
        File file = new File(plugin.getDataFolder(), name);

        if (!file.exists()) {
            plugin.saveResource(name, false);
        }

        file = new File(plugin.getDataFolder(), name);

        FileConfiguration config =
                YamlConfiguration.loadConfiguration(file);

        files.put(name, file);
        configs.put(name, config);
    }

    public FileConfiguration getConfig(String name) {
        return configs.get(name);
    }

    public void saveConfig(String name) {
        FileConfiguration config = configs.get(name);
        File file = files.get(name);

        if (config == null || file == null) {
            plugin.getLogger().warning(
                    "Config nicht gefunden: " + name
            );
            return;
        }

        try {
            config.save(file);
        } catch (IOException exception) {
            plugin.getLogger().severe(
                    "Fehler beim Speichern von " + name
            );
            exception.printStackTrace();
        }
    }

    public void reloadConfigs() {
        configs.clear();
        files.clear();
        loadConfigs();
        plugin.reloadConfig();
    }
}
