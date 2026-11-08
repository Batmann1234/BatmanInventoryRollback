package me.danjono.inventoryrollback.gui.menu;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import com.nuclyon.technicallycoded.inventoryrollback.customdata.CustomDataItemEditor;
import com.nuclyon.technicallycoded.inventoryrollback.util.PingUtil;
import com.nuclyon.technicallycoded.inventoryrollback.util.PlayerKey;

import me.danjono.inventoryrollback.InventoryRollback;
import me.danjono.inventoryrollback.config.ConfigData;
import me.danjono.inventoryrollback.config.MessageData;
import me.danjono.inventoryrollback.config.SoundData;
import me.danjono.inventoryrollback.data.LogType;
import me.danjono.inventoryrollback.data.PlayerData;
import me.danjono.inventoryrollback.gui.Buttons;
import me.danjono.inventoryrollback.gui.InventoryName;

/**
 * Panel de control del plugin (54 slots).
 *
 * Fila superior: cambiar lista (conectados / desconectados), informacion general y "mis respaldos".
 * Zona central: cabezas de jugadores con informacion (28 por pagina).
 * Fila inferior: paginas y herramientas de administrador (guardar todos, actualizar, activar/desactivar, recargar, cerrar).
 */
public class MainMenu {

    // Slots de la zona de jugadores (4 filas de 7)
    public static final int[] HEAD_SLOTS = {
            10, 11, 12, 13, 14, 15, 16,
            19, 20, 21, 22, 23, 24, 25,
            28, 29, 30, 31, 32, 33, 34,
            37, 38, 39, 40, 41, 42, 43
    };

    // Botones
    public static final int SLOT_MODE = 2;
    public static final int SLOT_INFO = 4;
    public static final int SLOT_SELF = 6;
    public static final int SLOT_PREVIOUS = 45;
    public static final int SLOT_SAVE_ALL = 47;
    public static final int SLOT_REFRESH = 48;
    public static final int SLOT_TOGGLE = 49;
    public static final int SLOT_RELOAD = 50;
    public static final int SLOT_CLOSE = 51;
    public static final int SLOT_NEXT = 53;

    private final Player staff;
    private final String staffKey;
    private final boolean offlineMode;
    private final boolean silent;
    private final Buttons buttons;

    private int pageNumber;
    private int pagesRequired;

    private List<OfflinePlayer> entries;
    private Inventory inventory;

    public MainMenu(Player staff, int pageNumber) {
        this(staff, pageNumber, false, false);
    }

    /**
     * @param offlineMode true para listar jugadores desconectados (por ultima conexion)
     * @param silent      true para no reproducir el sonido de apertura (al cambiar de pagina, lista, etc.)
     */
    public MainMenu(Player staff, int pageNumber, boolean offlineMode, boolean silent) {
        this.staff = staff;
        this.staffKey = PlayerKey.of(staff);
        this.pageNumber = pageNumber;
        this.offlineMode = offlineMode;
        this.silent = silent;
        this.buttons = new Buttons(staff);

        loadEntries();
        createInventory();
    }

    // ------------------------------------------------------------------ datos

    private void loadEntries() {
        entries = new ArrayList<>();

        if (offlineMode) {
            for (OfflinePlayer player : Bukkit.getOfflinePlayers()) {
                if (player.isOnline() || player.getName() == null) continue;
                if (!player.hasPlayedBefore()) continue;
                entries.add(player);
            }
            entries.sort(Comparator.comparingLong(OfflinePlayer::getLastPlayed).reversed());
        } else {
            for (Player player : Bukkit.getOnlinePlayers()) entries.add(player);
            entries.sort(Comparator.comparing(p -> p.getName().toLowerCase(Locale.ROOT)));
        }

        int pageSize = HEAD_SLOTS.length;
        pagesRequired = Math.max(1, (int) Math.ceil(entries.size() / (double) pageSize));

        if (pageNumber > pagesRequired) pageNumber = pagesRequired;
        else if (pageNumber <= 0) pageNumber = 1;
    }

    // ------------------------------------------------------------ inventario

    public void createInventory() {
        inventory = Bukkit.createInventory(staff, InventoryName.MAIN_MENU.getSize(), InventoryName.MAIN_MENU.getName());

        // Marco decorativo (verde y blanco alternados)
        for (int slot = 0; slot < inventory.getSize(); slot++) {
            int row = slot / 9;
            int col = slot % 9;
            if (row == 0 || row == 5 || col == 0 || col == 8) {
                ItemStack pane = Buttons.decorationPane(slot % 2 == 0);
                if (pane != null) inventory.setItem(slot, pane);
            }
        }

        // ---- Fila superior
        inventory.setItem(SLOT_MODE, modeButton());
        inventory.setItem(SLOT_SELF, selfButton());

        // ---- Fila inferior
        if (pageNumber > 1) {
            List<String> lore = new ArrayList<>();
            lore.add(Buttons.pageLore(pageNumber - 1));
            inventory.setItem(SLOT_PREVIOUS, withMode(buttons.backButton(MessageData.getPreviousPageButton(), LogType.UNKNOWN, pageNumber - 1, lore)));
        }

        inventory.setItem(SLOT_SAVE_ALL, saveAllButton());
        inventory.setItem(SLOT_REFRESH, refreshButton());
        inventory.setItem(SLOT_TOGGLE, toggleButton());
        inventory.setItem(SLOT_RELOAD, reloadButton());
        inventory.setItem(SLOT_CLOSE, closeButton());
    }

    public Inventory getInventory() {
        return this.inventory;
    }

    /** Se ejecuta de forma asincrona: coloca la informacion general y las cabezas (consultan datos). */
    public void getMainMenu() {
        if (!silent) {
            Bukkit.getScheduler().runTask(InventoryRollback.getInstance(), () -> SoundData.playMenu(staff, "open"));
        }

        inventory.setItem(SLOT_INFO, infoButton());

        if (entries.isEmpty()) {
            String text = MessageData.menu(offlineMode ? "menus.main.empty-offline" : "menus.main.empty-online");
            inventory.setItem(22, Buttons.actionButton(Buttons.icon("BARRIER"), text, null, "none", staffKey));
        }

        int start = (pageNumber - 1) * HEAD_SLOTS.length;
        for (int i = 0; i < HEAD_SLOTS.length; i++) {
            int index = start + i;
            if (index >= entries.size()) break;

            OfflinePlayer player = entries.get(index);
            inventory.setItem(HEAD_SLOTS[i], new Buttons(player).playerHead(headLore(player), true));
        }

        if (pageNumber < pagesRequired) {
            List<String> lore = new ArrayList<>();
            lore.add(Buttons.pageLore(pageNumber + 1));
            inventory.setItem(SLOT_NEXT, withMode(buttons.nextButton(MessageData.getNextPageButton(), LogType.UNKNOWN, pageNumber + 1, lore)));
        }
    }

    // ----------------------------------------------------------------- botones

    /** Anade al boton de pagina la lista actual, para que al cambiar de pagina no cambie de lista. */
    private ItemStack withMode(ItemStack item) {
        CustomDataItemEditor nbt = CustomDataItemEditor.editItem(item);
        nbt.setString("mode", modeValue());
        return nbt.setItemData();
    }

    private String modeValue() {
        return offlineMode ? "OFFLINE" : "ONLINE";
    }

    private ItemStack infoButton() {
        String status = MessageData.menu(ConfigData.isEnabled() ? "menus.main.status-on" : "menus.main.status-off");

        List<String> lore = new ArrayList<>();
        lore.add(MessageData.menu("menus.main.info-status").replace("%VALUE%", status));
        lore.add(MessageData.menu("menus.main.info-storage").replace("%VALUE%", ConfigData.getSaveType().getName()));
        lore.add(MessageData.menu("menus.main.info-online").replace("%VALUE%", String.valueOf(Bukkit.getOnlinePlayers().size())));
        lore.add(MessageData.menu("menus.main.info-registered").replace("%VALUE%", String.valueOf(Bukkit.getOfflinePlayers().length)));
        lore.add(MessageData.menu("menus.main.info-version").replace("%VALUE%", String.valueOf(InventoryRollback.getPluginVersion())));
        lore.add("");
        lore.add(MessageData.menu("menus.main.info-showing").replace("%VALUE%",
                MessageData.menu(offlineMode ? "menus.main.showing-offline" : "menus.main.showing-online")));
        lore.add(Buttons.pageLore(pageNumber) + " \u00a77/ " + pagesRequired);

        return Buttons.actionButton(Buttons.icon("NETHER_STAR"), MessageData.menu("menus.main.info-name"), lore, "info", staffKey);
    }

    private ItemStack modeButton() {
        List<String> lore = new ArrayList<>(Buttons.splitLines(MessageData.menu(offlineMode ? "menus.main.mode-online-lore" : "menus.main.mode-offline-lore")));
        lore.add("");
        lore.add(MessageData.menu("menus.main.mode-click"));

        String name = MessageData.menu(offlineMode ? "menus.main.mode-online-name" : "menus.main.mode-offline-name");
        Material icon = offlineMode ? Buttons.icon("LIME_DYE", "GREEN_DYE") : Buttons.icon("GRAY_DYE", "LIGHT_GRAY_DYE");

        // El valor "mode" es la lista a la que se cambia al hacer clic
        return Buttons.actionButton(icon, name, lore, "mode", staffKey, "mode", offlineMode ? "ONLINE" : "OFFLINE");
    }

    private ItemStack selfButton() {
        List<String> lore = new ArrayList<>(Buttons.splitLines(MessageData.menu("menus.main.self-lore")));
        lore.add("");
        lore.add(MessageData.menu("menus.main.self-click"));
        return Buttons.actionButton(Buttons.icon("CHEST"), MessageData.menu("menus.main.self-name"), lore, "self", staffKey);
    }

    private ItemStack refreshButton() {
        List<String> lore = new ArrayList<>(Buttons.splitLines(MessageData.menu("menus.main.refresh-lore")));
        lore.add("");
        lore.add(MessageData.menu("menus.main.refresh-click"));
        return Buttons.actionButton(Buttons.icon("CLOCK", "WATCH"), MessageData.menu("menus.main.refresh-name"), lore, "refresh", staffKey,
                "mode", modeValue(), "page", String.valueOf(pageNumber));
    }

    private ItemStack saveAllButton() {
        List<String> lore = new ArrayList<>();
        lore.add(MessageData.menu("menus.main.saveall-lore").replace("%VALUE%", String.valueOf(Bukkit.getOnlinePlayers().size())));
        lore.add("");
        lore.add(staff.hasPermission("batmaninventoryrollback.forcebackup")
                ? MessageData.menu("menus.main.saveall-click")
                : MessageData.menu("menus.main.no-permission-lore"));
        return Buttons.actionButton(Buttons.icon("EMERALD_BLOCK", "EMERALD"), MessageData.menu("menus.main.saveall-name"), lore, "saveall", staffKey,
                "mode", modeValue(), "page", String.valueOf(pageNumber));
    }

    private ItemStack toggleButton() {
        boolean enabled = ConfigData.isEnabled();
        String perm = enabled ? "batmaninventoryrollback.disable" : "batmaninventoryrollback.enable";

        List<String> lore = new ArrayList<>(Buttons.splitLines(MessageData.menu(enabled ? "menus.main.toggle-on-lore" : "menus.main.toggle-off-lore")));
        lore.add("");
        lore.add(staff.hasPermission(perm)
                ? MessageData.menu(enabled ? "menus.main.toggle-on-click" : "menus.main.toggle-off-click")
                : MessageData.menu("menus.main.no-permission-lore"));

        Material icon = enabled ? Buttons.icon("LIME_CONCRETE", "EMERALD") : Buttons.icon("RED_CONCRETE", "REDSTONE_BLOCK");
        return Buttons.actionButton(icon, MessageData.menu(enabled ? "menus.main.toggle-on-name" : "menus.main.toggle-off-name"),
                lore, "toggle", staffKey, "mode", modeValue(), "page", String.valueOf(pageNumber));
    }

    private ItemStack reloadButton() {
        List<String> lore = new ArrayList<>(Buttons.splitLines(MessageData.menu("menus.main.reload-lore")));
        lore.add("");
        lore.add(staff.hasPermission("batmaninventoryrollback.reload")
                ? MessageData.menu("menus.main.reload-click")
                : MessageData.menu("menus.main.no-permission-lore"));
        return Buttons.actionButton(Buttons.icon("COMPARATOR", "REDSTONE_COMPARATOR"), MessageData.menu("menus.main.reload-name"), lore, "reload", staffKey,
                "mode", modeValue(), "page", String.valueOf(pageNumber));
    }

    private ItemStack closeButton() {
        List<String> lore = new ArrayList<>(Buttons.splitLines(MessageData.menu("menus.main.close-lore")));
        return Buttons.actionButton(Buttons.icon("BARRIER"), MessageData.menu("menus.main.close-name"), lore, "close", staffKey);
    }

    // ------------------------------------------------------------ cabezas

    @SuppressWarnings("deprecation")
    private List<String> headLore(OfflinePlayer target) {
        List<String> lore = new ArrayList<>();

        if (target.isOnline() && target.getPlayer() != null) {
            Player online = target.getPlayer();

            lore.add(MessageData.menu("menus.main.head-online"));
            lore.add(MessageData.menu("menus.main.head-world").replace("%VALUE%", online.getWorld().getName()));
            lore.add(MessageData.menu("menus.main.head-position")
                    .replace("%X%", String.valueOf(online.getLocation().getBlockX()))
                    .replace("%Y%", String.valueOf(online.getLocation().getBlockY()))
                    .replace("%Z%", String.valueOf(online.getLocation().getBlockZ())));

            int ping = PingUtil.getPing(online);
            if (ping >= 0) lore.add(MessageData.menu("menus.main.head-ping").replace("%VALUE%", String.valueOf(ping)));

            lore.add(MessageData.menu("menus.main.head-health")
                    .replace("%VALUE%", Math.round(online.getHealth()) + "/" + Math.round(online.getMaxHealth())));

            String mode = online.getGameMode().name();
            lore.add(MessageData.menu("menus.main.head-gamemode")
                    .replace("%VALUE%", mode.charAt(0) + mode.substring(1).toLowerCase(Locale.ROOT)));
        } else {
            lore.add(MessageData.menu("menus.main.head-offline"));

            String date = MessageData.menu("menus.lore.unknown");
            if (target.getLastPlayed() != 0) date = PlayerData.getTime(target.getLastPlayed());
            lore.add(MessageData.menu("menus.main.head-lastseen").replace("%VALUE%", date));
        }

        try {
            int deaths = new PlayerData(PlayerKey.of(target), LogType.DEATH, null).getAmountOfBackups();
            lore.add(MessageData.menu("menus.main.head-deaths").replace("%VALUE%", String.valueOf(deaths)));
        } catch (Exception ignored) {
            // Si la consulta falla, simplemente no se muestra el dato
        }

        lore.add("");
        lore.add(MessageData.menu("menus.main.head-click"));
        return lore;
    }

}
