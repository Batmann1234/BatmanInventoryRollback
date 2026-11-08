package com.nuclyon.technicallycoded.inventoryrollback.util;

import com.nuclyon.technicallycoded.inventoryrollback.InventoryRollbackPlus;
import me.danjono.inventoryrollback.config.ConfigData;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;

/**
 * Guarda el ping de cada muerte en pings.yml (funciona tanto con YAML como con MySQL).
 * Formato: <nombre-en-minusculas>.<timestamp>: ping
 */
public final class PingStore {

    private static final String FILE_NAME = "pings.yml";
    private static final int DEFAULT_MAX_PER_PLAYER = 200;

    private static YamlConfiguration config;
    private static File file;

    private PingStore() {}

    private static synchronized void load() {
        if (config != null) return;
        file = new File(InventoryRollbackPlus.getInstance().getDataFolder(), FILE_NAME);
        config = YamlConfiguration.loadConfiguration(file);
    }

    public static synchronized void set(String playerKey, long timestamp, int ping) {
        load();
        String base = playerKey;
        config.set(base + "." + timestamp, ping);
        prune(base);
        save();
    }

    /** @return ping guardado, o null si no hay registro. */
    public static synchronized Integer get(String playerKey, long timestamp) {
        load();
        String path = playerKey + "." + timestamp;
        if (!config.contains(path)) return null;
        return config.getInt(path);
    }

    /** Claves actuales de primer nivel (usado por la migracion UUID -> nombre). */
    public static synchronized List<String> getKeys() {
        load();
        return new ArrayList<>(config.getKeys(false));
    }

    /** Mueve los pings de una clave a otra, mezclandolos si la nueva ya existe. */
    public static synchronized void renameKey(String from, String to) {
        load();
        if (from.equals(to) || config.getConfigurationSection(from) == null) return;

        for (String time : config.getConfigurationSection(from).getKeys(false)) {
            String target = to + "." + time;
            if (!config.contains(target)) config.set(target, config.get(from + "." + time));
        }
        config.set(from, null);
        prune(to);
        save();
    }

    private static void prune(String base) {
        if (config.getConfigurationSection(base) == null) return;

        int max = ConfigData.getMaxSavesDeath();
        if (max <= 0) max = DEFAULT_MAX_PER_PLAYER;

        List<String> keys = new ArrayList<>(config.getConfigurationSection(base).getKeys(false));
        if (keys.size() <= max) return;

        List<Long> times = new ArrayList<>();
        for (String key : keys) {
            try {
                times.add(Long.parseLong(key));
            } catch (NumberFormatException ignored) {
                // Entrada invalida, se ignora
            }
        }
        Collections.sort(times);

        int toRemove = times.size() - max;
        for (int i = 0; i < toRemove; i++) {
            config.set(base + "." + times.get(i), null);
        }
    }

    private static void save() {
        try {
            config.save(file);
        } catch (IOException e) {
            InventoryRollbackPlus.getInstance().getLogger().log(Level.WARNING, "No se pudo guardar pings.yml", e);
        }
    }

}
