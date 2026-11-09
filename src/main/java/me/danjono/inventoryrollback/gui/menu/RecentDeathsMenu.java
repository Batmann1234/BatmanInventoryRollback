package me.danjono.inventoryrollback.gui.menu;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import com.nuclyon.technicallycoded.inventoryrollback.customdata.CustomDataItemEditor;
import com.nuclyon.technicallycoded.inventoryrollback.util.DeathExtrasStore;
import com.nuclyon.technicallycoded.inventoryrollback.util.PlayerKey;
import com.nuclyon.technicallycoded.inventoryrollback.util.RecentDeathsStore;
import com.nuclyon.technicallycoded.inventoryrollback.util.PingStore;

import me.danjono.inventoryrollback.config.MessageData;
import me.danjono.inventoryrollback.data.LogType;
import me.danjono.inventoryrollback.data.PlayerData;
import me.danjono.inventoryrollback.gui.Buttons;
import me.danjono.inventoryrollback.gui.InventoryName;

/**
 * Muertes recientes del servidor (de todos los jugadores, las ultimas 50).
 * Cada cabeza abre directamente el respaldo de esa muerte.
 */
public class RecentDeathsMenu {

    public static final int SLOT_PREVIOUS = 45;
    public static final int SLOT_BACK = 49;
    public static final int SLOT_NEXT = 53;

    private final Player staff;
    private final String staffKey;
    private final Buttons buttons;

    private int pageNumber;
    private int pagesRequired;

    private final List<RecentDeathsStore.Entry> entries;
    private Inventory inventory;

    public RecentDeathsMenu(Player staff, int pageNumber) {
        this.staff = staff;
        this.staffKey = PlayerKey.of(staff);
        this.pageNumber = pageNumber;
        this.buttons = new Buttons(staff);
        this.entries = RecentDeathsStore.list();

        pagesRequired = Math.max(1, (int) Math.ceil(entries.size() / (double) MainMenu.HEAD_SLOTS.length));
        if (this.pageNumber > pagesRequired) this.pageNumber = pagesRequired;
        else if (this.pageNumber <= 0) this.pageNumber = 1;

        createInventory();
    }

    public void createInventory() {
        inventory = Bukkit.createInventory(staff, InventoryName.RECENT_DEATHS.getSize(), InventoryName.RECENT_DEATHS.getName());

        // Marco decorativo (verde y blanco alternados)
        for (int slot = 0; slot < inventory.getSize(); slot++) {
            int row = slot / 9;
            int col = slot % 9;
            if (row == 0 || row == 5 || col == 0 || col == 8) {
                ItemStack pane = Buttons.decorationPane(slot % 2 == 0);
                if (pane != null) inventory.setItem(slot, pane);
            }
        }

        inventory.setItem(SLOT_BACK, Buttons.actionButton(Buttons.icon("ARROW"),
                MessageData.menu("menus.recent.back-name"),
                Buttons.splitLines(MessageData.menu("menus.recent.back-lore")), "back", staffKey));

        if (pageNumber > 1) {
            List<String> lore = new ArrayList<>();
            lore.add(Buttons.pageLore(pageNumber - 1));
            inventory.setItem(SLOT_PREVIOUS, buttons.backButton(MessageData.getPreviousPageButton(), LogType.UNKNOWN, pageNumber - 1, lore));
        }
    }

    public Inventory getInventory() {
        return this.inventory;
    }

    /** Se ejecuta de forma asincrona: lee los datos de cada muerte y coloca las cabezas. */
    public void showDeaths() {
        if (entries.isEmpty()) {
            inventory.setItem(22, Buttons.actionButton(Buttons.icon("BARRIER"), MessageData.menu("menus.recent.empty"), null, "none", staffKey));
            return;
        }

        int start = (pageNumber - 1) * MainMenu.HEAD_SLOTS.length;
        int slotIndex = 0;

        for (int i = start; i < entries.size() && slotIndex < MainMenu.HEAD_SLOTS.length; i++) {
            ItemStack head = buildEntry(entries.get(i));
            if (head == null) continue; // el respaldo ya no existe

            inventory.setItem(MainMenu.HEAD_SLOTS[slotIndex], head);
            slotIndex++;
        }

        if (pageNumber < pagesRequired) {
            List<String> lore = new ArrayList<>();
            lore.add(Buttons.pageLore(pageNumber + 1));
            inventory.setItem(SLOT_NEXT, buttons.nextButton(MessageData.getNextPageButton(), LogType.UNKNOWN, pageNumber + 1, lore));
        }
    }

    private ItemStack buildEntry(RecentDeathsStore.Entry entry) {
        try {
            String key = PlayerKey.of(entry.playerName);

            PlayerData data = new PlayerData(key, LogType.DEATH, entry.timestamp);
            data.getRollbackMenuData();

            String world = data.getWorld();
            if (world == null) return null;

            double x = data.getX();
            double y = data.getY();
            double z = data.getZ();

            List<String> lore = new ArrayList<>();
            lore.add(MessageData.getDeathTime(PlayerData.getTime(entry.timestamp)));

            String reason = data.getDeathReason();
            if (reason != null) lore.add(MessageData.getDeathReason(reason));

            lore.add(MessageData.menu("menus.recent.location")
                    .replace("%WORLD%", world)
                    .replace("%X%", String.valueOf(Math.round(x)))
                    .replace("%Y%", String.valueOf(Math.round(y)))
                    .replace("%Z%", String.valueOf(Math.round(z))));

            Integer ping = PingStore.get(key, entry.timestamp);
            lore.add(ping != null && ping >= 0
                    ? MessageData.menu("menus.ping.list-line").replace("%PING%", String.valueOf(ping))
                    : MessageData.menu("menus.ping.list-line-unknown"));

            lore.addAll(Buttons.contextLore(DeathExtrasStore.get(key, entry.timestamp)));

            lore.add("");
            lore.add(MessageData.menu("menus.recent.click"));

            OfflinePlayer target = Bukkit.getOfflinePlayer(entry.playerName);
            ItemStack head = new Buttons(target).playerHead(lore, true);

            // Datos que usa el clic para abrir el respaldo de esta muerte
            CustomDataItemEditor nbt = CustomDataItemEditor.editItem(head);
            nbt.setLong("timestamp", entry.timestamp);
            nbt.setString("logType", LogType.DEATH.name());
            nbt.setString("location", world + "," + x + "," + y + "," + z);
            return nbt.setItemData();
        } catch (Exception e) {
            return null;
        }
    }

}
