
package de.ultimaterpg.manager;

import de.ultimaterpg.UltimateRPG;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class PlayerDataManager {

    private final UltimateRPG plugin;
    private final DatabaseManager database;

    private final Map<UUID, DatabaseManager.PlayerRecord> profiles =
            new ConcurrentHashMap<>();

    public PlayerDataManager(
            UltimateRPG plugin,
            DatabaseManager database
    ) {
        this.plugin = plugin;
        this.database = database;

        plugin.getServer().getPluginManager().registerEvents(
                new org.bukkit.event.Listener() {
                    @org.bukkit.event.EventHandler
                    public void onJoin(
                            org.bukkit.event.player.PlayerJoinEvent event
                    ) {
                        load(event.getPlayer());
                    }

                    @org.bukkit.event.EventHandler
                    public void onQuit(
                            org.bukkit.event.player.PlayerQuitEvent event
                    ) {
                        saveAndUnload(event.getPlayer());
                    }
                },
                plugin
        );

        long interval = Math.max(
                20L,
                plugin.getConfig().getLong(
                        "database.autosave-interval", 300
                ) * 20L
        );

        Bukkit.getScheduler().runTaskTimer(
                plugin,
                this::saveAll,
                interval,
                interval
        );
    }

    public void load(Player player) {
        UUID uuid = player.getUniqueId();

        database.createPlayer(uuid)
                .thenCompose(ignored -> database.loadPlayer(uuid))
                .thenAccept(profile -> Bukkit.getScheduler().runTask(
                        plugin,
                        () -> {
                            if (!player.isOnline()) {
                                return;
                            }

                            profiles.put(uuid, profile);

                            player.sendMessage(
                                    "§8[§6UltimateRPG§8] §a"
                                    + "Dein RPG-Profil wurde geladen."
                            );
                        }
                ))
                .exceptionally(error -> {
                    plugin.getLogger().severe(
                            "Could not load profile for " + uuid
                                    + ": " + error.getMessage()
                    );

                    Bukkit.getScheduler().runTask(
                            plugin,
                            () -> {
                                if (player.isOnline()) {
                                    player.sendMessage(
                                            "§cDein RPG-Profil konnte "
                                                    + "nicht geladen werden."
                                    );
                                    player.kickPlayer(
                                            "RPG-Daten konnten nicht geladen werden."
                                    );
                                }
                            }
                    );

                    return null;
                });
    }

    public DatabaseManager.PlayerRecord getProfile(UUID uuid) {
        return profiles.get(uuid);
    }

    public boolean isLoaded(UUID uuid) {
        return profiles.containsKey(uuid);
    }

    public void updateProfile(
            UUID uuid,
            DatabaseManager.PlayerRecord profile
    ) {
        profiles.put(uuid, profile);
    }

    public void saveAll() {
        for (DatabaseManager.PlayerRecord profile : profiles.values()) {
            database.savePlayer(profile)
                    .exceptionally(error -> {
                        plugin.getLogger().severe(
                                "Could not save player "
                                        + profile.uuid()
                                        + ": " + error.getMessage()
                        );
                        return null;
                    });
        }
    }

    private void saveAndUnload(Player player) {
        UUID uuid = player.getUniqueId();

        DatabaseManager.PlayerRecord profile = profiles.get(uuid);

        if (profile == null) {
            return;
        }

        database.savePlayer(profile)
                .thenRun(() -> profiles.remove(uuid, profile))
                .exceptionally(error -> {
                    plugin.getLogger().severe(
                            "Could not save quitting player "
                                    + uuid + ": " + error.getMessage()
                    );
                    return null;
                });
    }
}
