package com.nuclyon.technicallycoded.inventoryrollback.util;

import com.nuclyon.technicallycoded.inventoryrollback.InventoryRollbackPlus;
import me.danjono.inventoryrollback.config.ConfigData;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;

/**
 * Guarda el contexto de cada muerte (montura, mascota, clan) en death-extras.yml.
 * Funciona igual con YAML y MySQL. Formato: <nombre-en-minusculas>.<timestamp>.vehicle|pet|clan
 */
public final class DeathExtrasStore {

    private static final String FILE_NAME = "death-extras.yml";
    private static final int DEFAULT_MAX_PER_PLAYER = 200;

    private static YamlConfiguration config;
    private static File file;

    private DeathExtrasStore() {}

    private static synchronized void load() {
        if (config != null) return;
        file = new File(InventoryRollbackPlus.getInstance().getDataFolder(), FILE_NAME);
        config = YamlConfiguration.loadConfiguration(file);
    }

    public static synchronized void set(String playerKey, long timestamp, DeathContext context) {
        if (context == null || context.isEmpty()) return;
        load();

        String base = playerKey + "." + timestamp;
        if (context.vehicle != null) config.set(base + ".vehicle", context.vehicle);
        if (context.pet != null) config.set(base + ".pet", context.pet);
        if (context.clan != null) config.set(base + ".clan", context.clan);

        prune(playerKey);
        save();
    }

    /** @return el contexto guardado, o null si no hay registro. */
    public static synchronized DeathContext get(String playerKey, long timestamp) {
        load();
        ConfigurationSection section = config.getConfigurationSection(playerKey + "." + timestamp);
        if (section == null) return null;
        return new DeathContext(section.getString("vehicle"), section.getString("pet"), section.getString("clan"));
    }

    private static void prune(String base) {
        ConfigurationSection section = config.getConfigurationSection(base);
        if (section == null) return;

        int max = ConfigData.getMaxSavesDeath();
        if (max <= 0) max = DEFAULT_MAX_PER_PLAYER;

        List<Long> times = new ArrayList<>();
        for (String key : section.getKeys(false)) {
            try {
                times.add(Long.parseLong(key));
            } catch (NumberFormatException ignored) {
                // Entrada invalida, se ignora
            }
        }
        if (times.size() <= max) return;
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
            InventoryRollbackPlus.getInstance().getLogger().log(Level.WARNING, "No se pudo guardar " + FILE_NAME, e);
        }
    }

}
