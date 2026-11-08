package com.nuclyon.technicallycoded.inventoryrollback.util;

import com.nuclyon.technicallycoded.inventoryrollback.InventoryRollbackPlus;
import me.danjono.inventoryrollback.config.ConfigData;
import me.danjono.inventoryrollback.data.LogType;
import me.danjono.inventoryrollback.data.MySQL;
import me.danjono.inventoryrollback.data.YAML;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.sql.SQLException;
import java.util.UUID;
import java.util.logging.Logger;

/**
 * Migra los backups guardados por UUID (versiones anteriores) a la clave por nombre.
 *
 * - Se ejecuta en cada arranque (es barato si no queda nada por migrar).
 * - Si dos UUID distintos (por ejemplo el de no premium y el de premium) resuelven al mismo
 *   nombre, sus historiales se juntan en uno solo.
 * - Los UUID cuyo nombre no se puede resolver (no estan en usercache.json) se dejan como estan
 *   y se reintentan en el siguiente arranque.
 */
public final class PlayerKeyMigration {

    private static final LogType[] TYPES = {
            LogType.JOIN, LogType.QUIT, LogType.DEATH, LogType.WORLD_CHANGE, LogType.FORCE
    };

    private PlayerKeyMigration() {}

    /** Lanza la migracion en un hilo asincrono para no frenar el arranque del servidor. */
    public static void runAsync() {
        InventoryRollbackPlus plugin = InventoryRollbackPlus.getInstance();
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                run(plugin.getLogger());
            } catch (Exception e) {
                plugin.getLogger().severe("Error migrando los backups de UUID a nombre: " + e.getMessage());
                e.printStackTrace();
            }
        });
    }

    private static synchronized void run(Logger log) {
        int[] stats = new int[2]; // [0] migrados, [1] sin resolver

        if (ConfigData.getSaveType() == ConfigData.SaveType.YAML) {
            migrateYaml(log, stats);
        } else if (ConfigData.getSaveType() == ConfigData.SaveType.MYSQL) {
            migrateMySql(log, stats);
        }
        migratePings(stats);

        if (stats[0] > 0 || stats[1] > 0) {
            log.info("Migracion de backups UUID -> nombre: " + stats[0] + " migrados, "
                    + stats[1] + " sin nombre conocido (se reintentara en el proximo arranque).");
        }
    }

    /** @return la clave (nombre en minusculas) de un UUID, o null si el nombre no se conoce. */
    private static String resolveKey(String uuidStr) {
        UUID uuid;
        try {
            uuid = UUID.fromString(uuidStr);
        } catch (IllegalArgumentException e) {
            return null;
        }
        OfflinePlayer player = Bukkit.getOfflinePlayer(uuid);
        String name = player.getName();
        if (name == null || name.isEmpty()) return null;
        return PlayerKey.of(name);
    }

    // ---------------------------------------------------------------- YAML

    private static void migrateYaml(Logger log, int[] stats) {
        for (LogType type : TYPES) {
            File typeFolder = YAML.getLogTypeFolder(type);
            File[] folders = typeFolder.listFiles(File::isDirectory);
            if (folders == null) continue;

            for (File folder : folders) {
                if (!PlayerKey.looksLikeUuid(folder.getName())) continue;

                String key = resolveKey(folder.getName());
                if (key == null) {
                    stats[1]++;
                    continue;
                }

                try {
                    moveFolder(folder, new File(typeFolder, key));
                    stats[0]++;
                } catch (IOException e) {
                    log.warning("No se pudo migrar " + folder.getAbsolutePath() + " -> " + key + ": " + e.getMessage());
                }
            }
        }
    }

    private static void moveFolder(File from, File to) throws IOException {
        // Carpeta nueva: basta con renombrar
        if (!to.exists()) {
            Files.move(from.toPath(), to.toPath(), StandardCopyOption.ATOMIC_MOVE);
            return;
        }

        // Ya existe (mismo nombre con otro UUID): se juntan los archivos
        File[] files = from.listFiles();
        if (files != null) {
            for (File file : files) {
                File target = new File(to, file.getName());
                // Si ya hay un backup con el mismo timestamp, se desplaza 1 ms hasta encontrar hueco
                long bump = 0;
                while (target.exists()) {
                    bump++;
                    target = new File(to, bumpedName(file.getName(), bump));
                }
                Files.move(file.toPath(), target.toPath());
            }
        }

        File[] left = from.listFiles();
        if (left == null || left.length == 0) Files.deleteIfExists(from.toPath());
    }

    private static String bumpedName(String fileName, long bump) {
        int dot = fileName.lastIndexOf('.');
        String base = dot == -1 ? fileName : fileName.substring(0, dot);
        String ext = dot == -1 ? "" : fileName.substring(dot);
        try {
            return (Long.parseLong(base) + bump) + ext;
        } catch (NumberFormatException e) {
            return base + "_" + bump + ext;
        }
    }

    // --------------------------------------------------------------- MySQL

    private static void migrateMySql(Logger log, int[] stats) {
        for (MySQL.BackupTable table : MySQL.BackupTable.values()) {
            try {
                for (String stored : MySQL.getDistinctPlayerKeys(table)) {
                    if (!PlayerKey.looksLikeUuid(stored)) continue;

                    String key = resolveKey(stored);
                    if (key == null) {
                        stats[1]++;
                        continue;
                    }

                    MySQL.renamePlayerKey(table, stored, key);
                    stats[0]++;
                }
            } catch (SQLException e) {
                log.warning("No se pudo migrar la tabla " + table.getTableName() + ": " + e.getMessage());
            }
        }
    }

    // --------------------------------------------------------------- pings

    private static void migratePings(int[] stats) {
        for (String stored : PingStore.getKeys()) {
            if (!PlayerKey.looksLikeUuid(stored)) continue;

            String key = resolveKey(stored);
            if (key == null) continue; // ya se cuenta como "sin resolver" en los backups

            PingStore.renameKey(stored, key);
        }
    }

}
