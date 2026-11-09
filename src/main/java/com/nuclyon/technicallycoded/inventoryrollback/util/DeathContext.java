package com.nuclyon.technicallycoded.inventoryrollback.util;

import me.danjono.inventoryrollback.config.ConfigData;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import java.lang.reflect.Method;
import java.util.Locale;

/**
 * Datos extra que se guardan al morir: montura, mascota activa y clan.
 *
 * - Montura: entidad en la que iba montado (no necesita ningun plugin).
 * - Mascota y clan: se leen con placeholders de PlaceholderAPI configurables en config.yml
 *   (death-context.pet-placeholder y death-context.clan-placeholder).
 */
public final class DeathContext {

    public final String vehicle;
    public final String pet;
    public final String clan;

    public DeathContext(String vehicle, String pet, String clan) {
        this.vehicle = clean(vehicle);
        this.pet = clean(pet);
        this.clan = clean(clan);
    }

    public boolean isEmpty() {
        return vehicle == null && pet == null && clan == null;
    }

    private static String clean(String value) {
        if (value == null) return null;
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    // ------------------------------------------------------------- captura

    /** Debe llamarse en el hilo principal (lee entidades y placeholders). */
    public static DeathContext capture(Player player) {
        if (!ConfigData.isDeathContextEnabled()) return null;

        String vehicle = null;
        try {
            Entity entity = player.getVehicle();
            if (entity != null) {
                String type = entity.getType().name().toLowerCase(Locale.ROOT).replace('_', ' ');
                type = type.substring(0, 1).toUpperCase(Locale.ROOT) + type.substring(1);

                String customName = entity.getCustomName();
                vehicle = (customName != null && !customName.trim().isEmpty())
                        ? ColorUtil.strip(customName) + " (" + type + ")"
                        : type;
            }
        } catch (Exception ignored) {
            // Si algo falla al leer la montura, simplemente no se guarda
        }

        String pet = parse(player, ConfigData.getDeathContextPetPlaceholder());
        String clan = parse(player, ConfigData.getDeathContextClanPlaceholder());

        DeathContext context = new DeathContext(vehicle, pet, clan);
        return context.isEmpty() ? null : context;
    }

    // ------------------------------------------------------ PlaceholderAPI

    private static Method setPlaceholders;
    private static boolean papiChecked;

    private static String parse(Player player, String placeholder) {
        if (placeholder == null || placeholder.trim().isEmpty()) return null;

        try {
            if (!papiChecked) {
                papiChecked = true;
                if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
                    Class<?> papi = Class.forName("me.clip.placeholderapi.PlaceholderAPI");
                    setPlaceholders = papi.getMethod("setPlaceholders", OfflinePlayer.class, String.class);
                }
            }
            if (setPlaceholders == null) return null;

            String result = (String) setPlaceholders.invoke(null, player, placeholder);
            if (result == null) return null;

            result = ColorUtil.strip(result).trim();

            // Placeholder sin resolver, vacio o "sin clan"
            if (result.isEmpty() || result.equalsIgnoreCase(placeholder.trim()) || result.contains("%")) return null;
            if (result.toLowerCase(Locale.ROOT).contains("sinclan")) return null;
            return result;
        } catch (Exception e) {
            return null;
        }
    }

}
