package com.nuclyon.technicallycoded.inventoryrollback.util;

import com.nuclyon.technicallycoded.inventoryrollback.InventoryRollbackPlus;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

/**
 * Indice global con las ultimas muertes del servidor (recent-deaths.yml), de todos los jugadores.
 * Cada entrada es "NombreDelJugador;timestamp", la mas reciente primero.
 */
public final class RecentDeathsStore {

    public static final int MAX_ENTRIES = 50;

    private static final String FILE_NAME = "recent-deaths.yml";

    private static YamlConfiguration config;
    private static File file;

    private RecentDeathsStore() {}

    /** Una muerte reciente. */
    public static final class Entry {
        public final String playerName;
        public final long timestamp;

        Entry(String playerName, long timestamp) {
            this.playerName = playerName;
            this.timestamp = timestamp;
        }
    }

    private static synchronized void load() {
        if (config != null) return;
        file = new File(InventoryRollbackPlus.getInstance().getDataFolder(), FILE_NAME);
        config = YamlConfiguration.loadConfiguration(file);
    }

    public static synchronized void add(String playerName, long timestamp) {
        load();

        List<String> raw = new ArrayList<>(config.getStringList("deaths"));
        raw.add(0, playerName + ";" + timestamp);
        while (raw.size() > MAX_ENTRIES) raw.remove(raw.size() - 1);

        config.set("deaths", raw);
        save();
    }

    public static synchronized List<Entry> list() {
        load();

        List<Entry> entries = new ArrayList<>();
        for (String line : config.getStringList("deaths")) {
            int split = line.lastIndexOf(';');
            if (split <= 0) continue;
            try {
                entries.add(new Entry(line.substring(0, split), Long.parseLong(line.substring(split + 1))));
            } catch (NumberFormatException ignored) {
                // Linea invalida, se ignora
            }
        }
        return entries;
    }

    private static void save() {
        try {
            config.save(file);
        } catch (IOException e) {
            InventoryRollbackPlus.getInstance().getLogger().log(Level.WARNING, "No se pudo guardar " + FILE_NAME, e);
        }
    }

}
