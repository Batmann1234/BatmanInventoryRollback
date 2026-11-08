package com.nuclyon.technicallycoded.inventoryrollback.util;

import org.bukkit.OfflinePlayer;

import java.util.Locale;
import java.util.UUID;

/**
 * Clave con la que se guardan los backups de un jugador.
 *
 * Antes se usaba el UUID, pero en servidores con premium + no premium (nLogin) el UUID
 * cambia cuando un jugador pasa de no premium a premium, y se perdia el acceso a su historial.
 * Ahora la clave es el NOMBRE del jugador en minusculas.
 */
public final class PlayerKey {

    private PlayerKey() {}

    /** Clave a partir de un jugador (online u offline). */
    public static String of(OfflinePlayer player) {
        String name = player.getName();
        // Si por algun motivo no se conoce el nombre, se usa el UUID como ultimo recurso
        if (name == null || name.isEmpty()) return player.getUniqueId().toString();
        return of(name);
    }

    /** Clave a partir de un nombre: minusculas y segura para usarse como nombre de carpeta. */
    public static String of(String name) {
        String key = name.trim().toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9_.\\-]", "_");
        if (key.isEmpty() || key.equals(".") || key.equals("..")) return "_";
        return key;
    }

    /** @return true si la clave tiene forma de UUID (dato viejo, aun sin migrar). */
    public static boolean looksLikeUuid(String key) {
        if (key == null || key.length() != 36) return false;
        try {
            UUID.fromString(key);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

}
