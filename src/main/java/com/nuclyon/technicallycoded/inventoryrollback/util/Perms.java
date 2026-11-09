package com.nuclyon.technicallycoded.inventoryrollback.util;

import org.bukkit.entity.Player;

/**
 * Permisos con "modo solo lectura": quien tenga batmaninventoryrollback.viewonly puede mirar
 * los respaldos pero no restaurar, sobrescribir, teletransportarse ni guardar/recargar,
 * aunque tambien tenga batmaninventoryrollback.* o batmaninventoryrollback.restore.
 */
public final class Perms {

    public static final String VIEW_ONLY = "batmaninventoryrollback.viewonly";

    private Perms() {}

    public static boolean isViewOnly(Player player) {
        return player.hasPermission(VIEW_ONLY);
    }

    /** Tiene el permiso y no esta en modo solo lectura. */
    public static boolean can(Player player, String permission) {
        return player.hasPermission(permission) && !isViewOnly(player);
    }

}
