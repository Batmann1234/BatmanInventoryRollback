package me.danjono.inventoryrollback.gui;

import com.nuclyon.technicallycoded.inventoryrollback.util.ColorUtil;
import me.danjono.inventoryrollback.config.ConfigData;
import me.danjono.inventoryrollback.config.MessageData;

public enum InventoryName {

    MAIN_MENU("menus.titles.main-menu", 54),
    PLAYER_MENU("menus.titles.player-menu", 9),
    ROLLBACK_LIST("menus.titles.rollback-list", ConfigData.getBackupLinesVisible() * 9 + 9),
    MAIN_BACKUP("menus.titles.main-backup", 54),
    ENDER_CHEST_BACKUP("menus.titles.ender-chest-backup", 36);

    private final String messageKey;
    private final int size;

    private InventoryName(String messageKey, int size) {
        this.messageKey = messageKey;
        this.size = size;
    }

    /** Titulo del menu (con colores, configurable en messages.yml). */
    public String getName() {
        return MessageData.menu(this.messageKey);
    }

    public int getSize() {
        return this.size;
    }

    /**
     * Compara el titulo de una vista con este menu ignorando colores,
     * asi los titulos con hex funcionan sin importar como el servidor los serialice.
     */
    public boolean is(String title) {
        if (title == null) return false;
        return ColorUtil.strip(title).equalsIgnoreCase(ColorUtil.strip(getName()));
    }

}
