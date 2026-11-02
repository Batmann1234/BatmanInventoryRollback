package com.nuclyon.technicallycoded.inventoryrollback.util;

import org.bukkit.entity.Player;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * Obtiene el ping de un jugador de forma compatible entre versiones.
 */
public final class PingUtil {

    private PingUtil() {}

    /** @return ping en ms, o -1 si no se pudo obtener. */
    public static int getPing(Player player) {
        if (player == null) return -1;

        // API moderna: Player#getPing()
        try {
            Method method = player.getClass().getMethod("getPing");
            Object result = method.invoke(player);
            if (result instanceof Number) return ((Number) result).intValue();
        } catch (Exception ignored) {
            // Intentar metodo alternativo
        }

        // Fallback: campo "ping" del EntityPlayer (versiones antiguas)
        try {
            Object handle = player.getClass().getMethod("getHandle").invoke(player);
            Field field = handle.getClass().getField("ping");
            return field.getInt(handle);
        } catch (Exception ignored) {
            return -1;
        }
    }

}
