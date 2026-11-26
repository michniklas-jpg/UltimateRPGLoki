
package de.ultimaterpg;

import de.ultimaterpg.commands.RPGCommand;
import de.ultimaterpg.manager.DatabaseManager;
import de.ultimaterpg.manager.PlayerDataManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Objects;

public final class UltimateRPG extends JavaPlugin {

    private static UltimateRPG instance;

    private DatabaseManager databaseManager;
    private PlayerDataManager playerDataManager;

    @Override
    public void onEnable() {
        instance = this;

        saveDefaultConfig();
        saveResourceIfMissing("weapons.yml");
        saveResourceIfMissing("bosses.yml");
        saveResourceIfMissing("quests.yml");
        saveResourceIfMissing("battlepass.yml");
        saveResourceIfMissing("rifts.yml");
        saveResourceIfMissing("meteors.yml");
        saveResourceIfMissing("skills.yml");

        getLogger().info("Starting UltimateRPG...");

        try {
            databaseManager = new DatabaseManager(this);
            databaseManager.initialize().join();

            playerDataManager = new PlayerDataManager(this, databaseManager);

            Objects.requireNonNull(getCommand("rpg"))
                    .setExecutor(new RPGCommand(this));

            Objects.requireNonNull(getCommand("rpg"))
                    .setTabCompleter(new RPGCommand(this));

            getLogger().info("UltimateRPG successfully enabled!");

        } catch (Exception exception) {
            getLogger().severe("Could not initialize UltimateRPG!");
            exception.printStackTrace();
            getServer().getPluginManager().disablePlugin(this);
        }
    }

    @Override
    public void onDisable() {
        if (databaseManager != null) {
            databaseManager.shutdown();
        }

        getLogger().info("UltimateRPG disabled.");
    }

    private void saveResourceIfMissing(String resource) {
        if (!getDataFolder().toPath().resolve(resource).toFile().exists()) {
            saveResource(resource, false);
        }
    }

    public static UltimateRPG getInstance() {
        return instance;
    }

    public DatabaseManager getDatabaseManager() {
        return databaseManager;
    }

    public PlayerDataManager getPlayerDataManager() {
        return playerDataManager;
    }
}

