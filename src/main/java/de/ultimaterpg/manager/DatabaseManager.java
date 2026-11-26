
package de.ultimaterpg.manager;

import de.ultimaterpg.UltimateRPG;

import java.io.File;
import java.sql.*;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class DatabaseManager {

    private final UltimateRPG plugin;
    private final ExecutorService executor;

    private Connection connection;

    public DatabaseManager(UltimateRPG plugin) {
        this.plugin = plugin;
        this.executor = Executors.newSingleThreadExecutor(r -> {
            Thread thread = new Thread(r, "UltimateRPG-Database");
            thread.setDaemon(true);
            return thread;
        });
    }

    public CompletableFuture<Void> initialize() {
        return CompletableFuture.runAsync(() -> {
            try {
                File databaseFile = new File(
                        plugin.getDataFolder(),
                        plugin.getConfig().getString(
                                "database.file", "database.db"
                        )
                );

                File parent = databaseFile.getParentFile();

                if (parent != null && !parent.exists()
                        && !parent.mkdirs()) {
                    throw new SQLException("Could not create database directory.");
                }

                connection = DriverManager.getConnection(
                        "jdbc:sqlite:" + databaseFile.getAbsolutePath()
                );

                try (Statement statement = connection.createStatement()) {
                    statement.execute("PRAGMA journal_mode=WAL");
                    statement.execute("PRAGMA foreign_keys=ON");
                    statement.execute("PRAGMA busy_timeout=5000");

                    statement.executeUpdate("""
                        CREATE TABLE IF NOT EXISTS players (
                            uuid TEXT PRIMARY KEY,
                            coins INTEGER NOT NULL DEFAULT 100,
                            level INTEGER NOT NULL DEFAULT 1,
                            xp INTEGER NOT NULL DEFAULT 0,
                            skill_points INTEGER NOT NULL DEFAULT 0,
                            battlepass_xp INTEGER NOT NULL DEFAULT 0,
                            battlepass_level INTEGER NOT NULL DEFAULT 1,
                            premium INTEGER NOT NULL DEFAULT 0,
                            created_at INTEGER NOT NULL,
                            last_seen INTEGER NOT NULL
                        )
                    """);

                    statement.executeUpdate("""
                        CREATE TABLE IF NOT EXISTS player_skills (
                            uuid TEXT NOT NULL,
                            skill_id TEXT NOT NULL,
                            level INTEGER NOT NULL DEFAULT 0,
                            PRIMARY KEY (uuid, skill_id),
                            FOREIGN KEY (uuid) REFERENCES players(uuid)
                            ON DELETE CASCADE
                        )
                    """);

                    statement.executeUpdate("""
                        CREATE TABLE IF NOT EXISTS quest_progress (
                            uuid TEXT NOT NULL,
                            quest_id TEXT NOT NULL,
                            progress INTEGER NOT NULL DEFAULT 0,
                            completed INTEGER NOT NULL DEFAULT 0,
                            period_key TEXT NOT NULL,
                            PRIMARY KEY (uuid, quest_id, period_key),
                            FOREIGN KEY (uuid) REFERENCES players(uuid)
                            ON DELETE CASCADE
                        )
                    """);

                    statement.executeUpdate("""
                        CREATE TABLE IF NOT EXISTS claimed_rewards (
                            uuid TEXT NOT NULL,
                            reward_id TEXT NOT NULL,
                            PRIMARY KEY (uuid, reward_id),
                            FOREIGN KEY (uuid) REFERENCES players(uuid)
                            ON DELETE CASCADE
                        )
                    """);

                    statement.executeUpdate("""
                        CREATE TABLE IF NOT EXISTS player_stats (
                            uuid TEXT NOT NULL,
                            stat_id TEXT NOT NULL,
                            value INTEGER NOT NULL DEFAULT 0,
                            PRIMARY KEY (uuid, stat_id),
                            FOREIGN KEY (uuid) REFERENCES players(uuid)
                            ON DELETE CASCADE
                        )
                    """);
                }

                plugin.getLogger().info("SQLite database initialized.");

            } catch (SQLException exception) {
                throw new RuntimeException(
                        "Database initialization failed", exception
                );
            }
        }, executor);
    }

    public CompletableFuture<Void> createPlayer(UUID uuid) {
        return CompletableFuture.runAsync(() -> {
            String sql = """
                INSERT OR IGNORE INTO players
                (uuid, created_at, last_seen)
                VALUES (?, ?, ?)
            """;

            long now = System.currentTimeMillis();

            try (PreparedStatement statement =
                         connection.prepareStatement(sql)) {

                statement.setString(1, uuid.toString());
                statement.setLong(2, now);
                statement.setLong(3, now);
                statement.executeUpdate();

            } catch (SQLException exception) {
                throw new RuntimeException(exception);
            }
        }, executor);
    }

    public CompletableFuture<PlayerRecord> loadPlayer(UUID uuid) {
        return CompletableFuture.supplyAsync(() -> {
            String sql = """
                SELECT coins, level, xp, skill_points,
                       battlepass_xp, battlepass_level, premium
                FROM players WHERE uuid = ?
            """;

            try (PreparedStatement statement =
                         connection.prepareStatement(sql)) {

                statement.setString(1, uuid.toString());

                try (ResultSet result = statement.executeQuery()) {
                    if (!result.next()) {
                        throw new SQLException(
                                "Player not found: " + uuid
                        );
                    }

                    return new PlayerRecord(
                            uuid,
                            result.getLong("coins"),
                            result.getInt("level"),
                            result.getLong("xp"),
                            result.getInt("skill_points"),
                            result.getLong("battlepass_xp"),
                            result.getInt("battlepass_level"),
                            result.getBoolean("premium")
                    );
                }

            } catch (SQLException exception) {
                throw new RuntimeException(exception);
            }
        }, executor);
    }

    public CompletableFuture<Void> savePlayer(PlayerRecord player) {
        return CompletableFuture.runAsync(() -> {
            String sql = """
                UPDATE players SET
                    coins = ?,
                    level = ?,
                    xp = ?,
                    skill_points = ?,
                    battlepass_xp = ?,
                    battlepass_level = ?,
                    premium = ?,
                    last_seen = ?
                WHERE uuid = ?
            """;

            try (PreparedStatement statement =
                         connection.prepareStatement(sql)) {

                statement.setLong(1, player.coins());
                statement.setInt(2, player.level());
                statement.setLong(3, player.xp());
                statement.setInt(4, player.skillPoints());
                statement.setLong(5, player.battlepassXp());
                statement.setInt(6, player.battlepassLevel());
                statement.setBoolean(7, player.premium());
                statement.setLong(8, System.currentTimeMillis());
                statement.setString(9, player.uuid().toString());

                statement.executeUpdate();

            } catch (SQLException exception) {
                throw new RuntimeException(exception);
            }
        }, executor);
    }

    public void shutdown() {
        try {
            executor.submit(() -> {
                try {
                    if (connection != null && !connection.isClosed()) {
                        connection.close();
                    }
                } catch (SQLException exception) {
                    plugin.getLogger().severe(
                            "Database shutdown failed: "
                                    + exception.getMessage()
                    );
                }
            }).get();

        } catch (Exception exception) {
            plugin.getLogger().severe(
                    "Could not finish database shutdown: "
                            + exception.getMessage()
            );
        } finally {
            executor.shutdown();
        }
    }

    public record PlayerRecord(
            UUID uuid,
            long coins,
            int level,
            long xp,
            int skillPoints,
            long battlepassXp,
            int battlepassLevel,
            boolean premium
    ) {}
}
