package me.danjono.inventoryrollback.gui;

import com.nuclyon.technicallycoded.inventoryrollback.InventoryRollbackPlus;
import com.nuclyon.technicallycoded.inventoryrollback.customdata.CustomDataItemEditor;
import com.tcoded.lightlibs.bukkitversion.BukkitVersion;
import me.danjono.inventoryrollback.config.MessageData;
import me.danjono.inventoryrollback.data.LogType;
import me.danjono.inventoryrollback.inventory.RestoreInventory;
import org.bukkit.*;
import org.bukkit.block.banner.Pattern;
import org.bukkit.block.banner.PatternType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BannerMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.jetbrains.annotations.NotNull;

import com.nuclyon.technicallycoded.inventoryrollback.util.PlayerKey;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

public class Buttons {

    /** Nombre del jugador en minusculas; se guarda en el item (tag "uuid" por compatibilidad). */
    private String playerKey;
    private OfflinePlayer player;

    private static final Material death = Material.BONE;

    private static final Material join =
            InventoryRollbackPlus.getInstance().getVersion().greaterOrEqThan(BukkitVersion.v1_13_R1) ?
                    Material.OAK_SAPLING : Material.getMaterial("SAPLING");

    private static final Material quit =
            InventoryRollbackPlus.getInstance().getVersion().greaterOrEqThan(BukkitVersion.v1_13_R1) ?
                    Material.RED_BED : Material.getMaterial("BED");

    private static final Material worldChange = Material.COMPASS;

    private static final Material forceSave = Material.DIAMOND;


    private static final Material pageSelector =
            InventoryRollbackPlus.getInstance().getVersion().greaterOrEqThan(BukkitVersion.v1_13_R1) ?
                    Material.WHITE_BANNER : Material.getMaterial("BANNER");

    private static final Material teleport = Material.ENDER_PEARL;

    private static final Material enderChest = Material.ENDER_CHEST;

    private static final Material health =
            InventoryRollbackPlus.getInstance().getVersion().greaterOrEqThan(BukkitVersion.v1_13_R1) ?
                    Material.MELON_SLICE : Material.getMaterial("MELON");

    private static final Material hunger = Material.ROTTEN_FLESH;

    private static final Material experience =
            InventoryRollbackPlus.getInstance().getVersion().greaterOrEqThan(BukkitVersion.v1_13_R1) ?
                    Material.EXPERIENCE_BOTTLE : Material.getMaterial("EXP_BOTTLE");

    private static final Material pingIcon =
            InventoryRollbackPlus.getInstance().getVersion().greaterOrEqThan(BukkitVersion.v1_13_R1) ?
                    Material.CLOCK : Material.getMaterial("WATCH");

    private static final Material restoreAllInventory = Material.NETHER_STAR;

    private static final Material restoreAllInventoryDisabled = Material.REDSTONE_BLOCK;

    private static final Material shulkerBox =
            InventoryRollbackPlus.getInstance().getVersion().greaterOrEqThan(BukkitVersion.v1_11_R1) ?
                    Material.SHULKER_BOX : Material.BARRIER;


    public Buttons(String playerKey) {
        this.playerKey = playerKey;
    }
    
    public Buttons(OfflinePlayer player) {
        this.player = player;
        this.playerKey = PlayerKey.of(player);
    }
    
    public static Material getDeathLogIcon() {
        return death;
    }
    
    public static Material getJoinLogIcon() {
        return join;
    }
    
    public static Material getQuitLogIcon() {
        return quit;
    }
    
    public static Material getWorldChangeLogIcon() {
        return worldChange;
    }
    
    public static Material getForceSaveLogIcon() {
        return forceSave;
    }

    public static Material getPageSelectorIcon() {
        return pageSelector;
    }

    public static Material getTeleportLocationIcon() {
        return teleport;
    }

    public static Material getEnderChestIcon() {
        return enderChest;
    }

    public static Material getHealthIcon() {
        return health;
    }

    public static Material getHungerIcon() {
        return hunger;
    }

    public static Material getExperienceIcon() {
        return experience;
    }

    public static Material getPingIcon() {
        return pingIcon;
    }

    public static Material getRestoreAllInventoryIcon() {
        return restoreAllInventory;
    }

    public static Material getRestoreAllInventoryDisabledIcon() {
        return restoreAllInventoryDisabled;
    }

    public static Material getGiveShulkerBoxIcon() {
        return shulkerBox;
    }

    public ItemStack nextButton(String displayName, LogType logType, int page, List<String> lore) {
        ItemStack button = new ItemStack(getPageSelectorIcon());
        BannerMeta meta = (BannerMeta) button.getItemMeta();

        List<Pattern> patterns = createBannerPatterns(true);

        assert meta != null;
        meta.setPatterns(patterns);

        if (InventoryRollbackPlus.getInstance().getVersion().greaterOrEqThan(BukkitVersion.v1_20_R4)) meta.addItemFlags(ItemFlag.HIDE_ADDITIONAL_TOOLTIP);
        else meta.addItemFlags(ItemFlag.valueOf("HIDE_POTION_EFFECTS"));

        if (displayName != null) {
            meta.setDisplayName(displayName);
        }

        meta.setLore(lore);

        button.setItemMeta(meta);

        CustomDataItemEditor editor = CustomDataItemEditor.editItem(button);

        editor.setString("uuid", playerKey);
        editor.setString("logType", logType.name());
        editor.setInt("page", page);
        button = editor.setItemData();

        return button;
    }

    public ItemStack backButton(String displayName, LogType logType, int page, List<String> lore) {
        ItemStack button = new ItemStack(getPageSelectorIcon());
        BannerMeta meta = (BannerMeta) button.getItemMeta();

        List<Pattern> patterns = createBannerPatterns(false);

        assert meta != null;
        meta.setPatterns(patterns);

        if (InventoryRollbackPlus.getInstance().getVersion().greaterOrEqThan(BukkitVersion.v1_20_R4)) meta.addItemFlags(ItemFlag.HIDE_ADDITIONAL_TOOLTIP);
        else meta.addItemFlags(ItemFlag.valueOf("HIDE_POTION_EFFECTS"));

        if (displayName != null) {
            meta.setDisplayName(displayName);
        }

        if (lore != null) {
            meta.setLore(lore);
        }

        button.setItemMeta(meta);

        CustomDataItemEditor nbt = CustomDataItemEditor.editItem(button);

        nbt.setString("uuid", playerKey);
        nbt.setString("logType", logType.name());
        nbt.setInt("page", page);
        button = nbt.setItemData();

        return button;
    }

    public ItemStack enderChestNextButton(String displayName, LogType logType, int page, Long timestamp, List<String> lore) {
        ItemStack button = new ItemStack(getPageSelectorIcon());
        BannerMeta meta = (BannerMeta) button.getItemMeta();

        List<Pattern> patterns = createBannerPatterns(true);

        assert meta != null;
        meta.setPatterns(patterns);

        if (InventoryRollbackPlus.getInstance().getVersion().greaterOrEqThan(BukkitVersion.v1_20_R4)) meta.addItemFlags(ItemFlag.HIDE_ADDITIONAL_TOOLTIP);
        else meta.addItemFlags(ItemFlag.valueOf("HIDE_POTION_EFFECTS"));

        if (displayName != null) {
            meta.setDisplayName(displayName);
        }

        meta.setLore(lore);

        button.setItemMeta(meta);

        CustomDataItemEditor nbt = CustomDataItemEditor.editItem(button);

        nbt.setString("uuid", playerKey);
        nbt.setString("logType", logType.name());
        nbt.setLong("timestamp", timestamp);
        nbt.setInt("page", page);
        button = nbt.setItemData();

        return button;
    }

    public ItemStack enderChestBackButton(String displayName, LogType logType, int page, Long timestamp, List<String> lore) {
        ItemStack button = new ItemStack(getPageSelectorIcon());
        BannerMeta meta = (BannerMeta) button.getItemMeta();

        List<Pattern> patterns = createBannerPatterns(false);

        assert meta != null;
        meta.setPatterns(patterns);

        if (InventoryRollbackPlus.getInstance().getVersion().greaterOrEqThan(BukkitVersion.v1_20_R4)) meta.addItemFlags(ItemFlag.HIDE_ADDITIONAL_TOOLTIP);
        else meta.addItemFlags(ItemFlag.valueOf("HIDE_POTION_EFFECTS"));

        if (displayName != null) {
            meta.setDisplayName(displayName);
        }

        if (lore != null) {
            meta.setLore(lore);
        }

        button.setItemMeta(meta);

        CustomDataItemEditor nbt = CustomDataItemEditor.editItem(button);

        nbt.setString("uuid", playerKey);
        nbt.setString("logType", logType.name());
        nbt.setLong("timestamp", timestamp);
        nbt.setInt("page", page);
        button = nbt.setItemData();

        return button;
    }

    public ItemStack mainMenuBackButton(String displayName) {
        ItemStack button = new ItemStack(getPageSelectorIcon());
        BannerMeta meta = (BannerMeta) button.getItemMeta();

        List<Pattern> patterns = createBannerPatterns(false);

        assert meta != null;
        meta.setPatterns(patterns);

//        meta.addItemFlags(ItemFlag.HIDE_POTION_EFFECTS);
        if (InventoryRollbackPlus.getInstance().getVersion().greaterOrEqThan(BukkitVersion.v1_20_R4)) meta.addItemFlags(ItemFlag.HIDE_ADDITIONAL_TOOLTIP);
        else meta.addItemFlags(ItemFlag.valueOf("HIDE_POTION_EFFECTS"));

        if (displayName != null) {
            meta.setDisplayName(displayName);
        }

        button.setItemMeta(meta);

        CustomDataItemEditor nbt = CustomDataItemEditor.editItem(button);

        nbt.setString("uuid", playerKey);
        button = nbt.setItemData();

        return button;
    }

    public ItemStack inventoryMenuBackButton(String displayName, LogType logType, Long timestamp) {
        ItemStack button = new ItemStack(getPageSelectorIcon());
        BannerMeta meta = (BannerMeta) button.getItemMeta();

        List<Pattern> patterns = createBannerPatterns(false);

        assert meta != null;
        meta.setPatterns(patterns);

        if (InventoryRollbackPlus.getInstance().getVersion().greaterOrEqThan(BukkitVersion.v1_20_R4)) meta.addItemFlags(ItemFlag.HIDE_ADDITIONAL_TOOLTIP);
        else meta.addItemFlags(ItemFlag.valueOf("HIDE_POTION_EFFECTS"));

        if (displayName != null) {
            meta.setDisplayName(displayName);
        }

        button.setItemMeta(meta);

        CustomDataItemEditor nbt = CustomDataItemEditor.editItem(button);

        nbt.setString("uuid", playerKey);
        nbt.setString("logType", logType.name());
        nbt.setLong("timestamp", timestamp);
        nbt.setInt("page", 0);

        button = nbt.setItemData();

        return button;
    }

    public ItemStack createInventoryButton(ItemStack item, LogType logType, String location, Long time, String displayName, List<String> lore) {    	
        ItemMeta meta = item.getItemMeta();

        assert meta != null;
        if (lore != null) {
            meta.setLore(lore);
        }

        meta.setDisplayName(displayName);

        item.setItemMeta(meta);

        CustomDataItemEditor nbt = CustomDataItemEditor.editItem(item);

        nbt.setString("uuid", playerKey);
        nbt.setString("logType", logType.name());
        nbt.setLong("timestamp", time);
        nbt.setString("location", location);
        item = nbt.setItemData();

        return item;
    }

    public ItemStack createDeathLogButton(LogType logType, List<String> lore) {    	
        ItemStack item = new ItemStack(getDeathLogIcon());        
        ItemMeta meta = item.getItemMeta();

        assert meta != null;
        if (lore != null) {         
            meta.setLore(lore);
        }
        
        meta.setDisplayName(MessageData.menu("menus.log-buttons.deaths"));

        item.setItemMeta(meta);

        CustomDataItemEditor nbt = CustomDataItemEditor.editItem(item);

        nbt.setString("uuid", playerKey);
        nbt.setString("logType", logType.name());
        item = nbt.setItemData();

        return item;
    }

    public ItemStack createJoinLogButton(LogType logType, List<String> lore) {      
        ItemStack item = new ItemStack(getJoinLogIcon());        
        ItemMeta meta = item.getItemMeta();

        assert meta != null;
        if (lore != null) {         
            meta.setLore(lore);
        }
        
        meta.setDisplayName(MessageData.menu("menus.log-buttons.joins"));

        item.setItemMeta(meta);

        CustomDataItemEditor nbt = CustomDataItemEditor.editItem(item);

        nbt.setString("uuid", playerKey);
        nbt.setString("logType", logType.name());
        item = nbt.setItemData();

        return item;
    }

    public ItemStack createQuitLogButton(LogType logType, List<String> lore) {      
        ItemStack item = new ItemStack(getQuitLogIcon());        
        ItemMeta meta = item.getItemMeta();

        assert meta != null;
        if (lore != null) {         
            meta.setLore(lore);
        }
        
        meta.setDisplayName(MessageData.menu("menus.log-buttons.quits"));

        item.setItemMeta(meta);

        CustomDataItemEditor nbt = CustomDataItemEditor.editItem(item);

        nbt.setString("uuid", playerKey);
        nbt.setString("logType", logType.name());
        item = nbt.setItemData();

        return item;
    }

    public ItemStack createWorldChangeLogButton(LogType logType, List<String> lore) {      
        ItemStack item = new ItemStack(getWorldChangeLogIcon());        
        ItemMeta meta = item.getItemMeta();

        assert meta != null;
        if (lore != null) {         
            meta.setLore(lore);
        }
        
        meta.setDisplayName(MessageData.menu("menus.log-buttons.world-changes"));

        item.setItemMeta(meta);

        CustomDataItemEditor nbt = CustomDataItemEditor.editItem(item);

        nbt.setString("uuid", playerKey);
        nbt.setString("logType", logType.name());
        item = nbt.setItemData();

        return item;
    }

    public ItemStack createForceSaveLogButton(LogType logType, List<String> lore) {      
        ItemStack item = new ItemStack(getForceSaveLogIcon());        
        ItemMeta meta = item.getItemMeta();

        assert meta != null;
        if (lore != null) {         
            meta.setLore(lore);
        }
        
        meta.setDisplayName(MessageData.menu("menus.log-buttons.force-saves"));

        item.setItemMeta(meta);

        CustomDataItemEditor nbt = CustomDataItemEditor.editItem(item);

        nbt.setString("uuid", playerKey);
        nbt.setString("logType", logType.name());
        item = nbt.setItemData();

        return item;
    }

    public ItemStack playerHead(List<String> lore, boolean setSkin) {    	
        if (playerKey == null)
            return null;
        
        ItemStack skull = null;
        OfflinePlayer player = this.player != null ? this.player : Bukkit.getOfflinePlayer(playerKey);
        
        if (InventoryRollbackPlus.getInstance().getVersion().greaterOrEqThan(BukkitVersion.v1_13_R1)) {
            skull = new ItemStack(Material.PLAYER_HEAD);
        } else {
            Constructor<?> itemStackConstructor;
            
            try {
                itemStackConstructor = Class.forName("org.bukkit.inventory.ItemStack").getConstructor(Material.class, int.class, short.class);
                skull = (ItemStack) itemStackConstructor.newInstance(Material.getMaterial("SKULL_ITEM"), 1, (short) 3);
            } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | IllegalArgumentException
                    | InvocationTargetException | NoSuchMethodException | SecurityException e) {
                e.printStackTrace();
            }
        }

        assert skull != null;
        SkullMeta skullMeta = (SkullMeta) skull.getItemMeta();

        assert skullMeta != null;
        if (setSkin) {
            try {
                if (InventoryRollbackPlus.getInstance().getVersion().greaterOrEqThan(BukkitVersion.v1_13_R1)) {
                    skullMeta.setOwningPlayer(player);
                } else {
                    Method method = skullMeta.getClass().getMethod("setOwner", String.class);
                    method.setAccessible(true);
                    method.invoke(skullMeta, player.getName());
                    method.setAccessible(false);
                }
            } catch (IllegalAccessException | IllegalArgumentException | SecurityException | NoSuchMethodException | InvocationTargetException e) {
                e.printStackTrace();
            }

            skullMeta.setDisplayName(ChatColor.RESET + player.getName());
        }

        if (lore != null) {	    	
            skullMeta.setLore(lore);
        }

        skull.setItemMeta(skullMeta);
        
        CustomDataItemEditor nbt = CustomDataItemEditor.editItem(skull);
        
        nbt.setString("uuid", playerKey);
        skull = nbt.setItemData();

        return skull;
    }

    public ItemStack enderPearlButton(LogType logType, String location) {    	
        ItemStack item = new ItemStack(getTeleportLocationIcon());

        ItemMeta meta = item.getItemMeta();
        assert meta != null;
        meta.setDisplayName(MessageData.getDeathLocation());

        List<String> lore = new ArrayList<>();
        if (location != null) {
            String[] loc = location.split(",");
            lore.add(MessageData.menu("menus.lore.world").replace("%VALUE%", loc[0]));
            lore.add(MessageData.menu("menus.lore.x").replace("%VALUE%", loc[1]));
            lore.add(MessageData.menu("menus.lore.y").replace("%VALUE%", loc[2]));
            lore.add(MessageData.menu("menus.lore.z").replace("%VALUE%", loc[3]));
        } else {
            lore.add(MessageData.menu("menus.lore.no-location"));
        }
        meta.setLore(lore);

        item.setItemMeta(meta);

        CustomDataItemEditor nbt = CustomDataItemEditor.editItem(item);

        nbt.setString("uuid", playerKey);
        nbt.setString("logType", logType.name());
        nbt.setString("location", location);
        item = nbt.setItemData();

        return item;
    }

    public ItemStack enderChestButton(LogType logType, Long timestamp, ItemStack[] enderChest) {    	
        ItemStack item = new ItemStack(getEnderChestIcon());

        ItemMeta meta = item.getItemMeta();
        assert meta != null;
        meta.setDisplayName(MessageData.getEnderChestRestoreButton());

        List<String> lore = new ArrayList<>();

        if (enderChest != null && enderChest.length > 1)
            lore.add(MessageData.menu("menus.lore.ender-items"));
        else {
            lore.add(MessageData.menu("menus.lore.ender-empty"));
        }

        meta.setLore(lore);
        item.setItemMeta(meta);

        CustomDataItemEditor nbt = CustomDataItemEditor.editItem(item);

        nbt.setString("uuid", playerKey);
        nbt.setString("logType", logType.name());
        nbt.setLong("timestamp", timestamp);
        item = nbt.setItemData();

        return item;
    }

    public ItemStack healthButton(LogType logType, Double health) {    	
        ItemStack item = new ItemStack(getHealthIcon());

        ItemMeta meta = item.getItemMeta();
        assert meta != null;
        meta.setDisplayName(MessageData.getHealthRestoreButton());

        item.setItemMeta(meta);

        CustomDataItemEditor nbt = CustomDataItemEditor.editItem(item);

        nbt.setString("uuid", playerKey);
        nbt.setString("logType", logType.name());
        nbt.setDouble("health", health);
        item = nbt.setItemData();

        return item;
    }

    public ItemStack hungerButton(LogType logType, int hunger, float saturation) {    	
        ItemStack item = new ItemStack(getHungerIcon());

        ItemMeta meta = item.getItemMeta();
        assert meta != null;
        meta.setDisplayName(MessageData.getHungerRestoreButton());

        item.setItemMeta(meta);

        CustomDataItemEditor nbt = CustomDataItemEditor.editItem(item);

        nbt.setString("uuid", playerKey);
        nbt.setString("logType", logType.name());
        nbt.setInt("hunger", hunger);
        nbt.setFloat("saturation", saturation);
        item = nbt.setItemData();

        return item;
    }

    public ItemStack experiencePotion(LogType logType, float xp) {    	
        ItemStack item = new ItemStack(getExperienceIcon());

        ItemMeta meta = item.getItemMeta();
        assert meta != null;
        meta.setDisplayName(MessageData.getExperienceRestoreButton());

        List<String> lore = new ArrayList<>();
        lore.add(MessageData.getExperienceRestoreLevel((int) RestoreInventory.getLevel(xp)));
        meta.setLore(lore);

        item.setItemMeta(meta);

        CustomDataItemEditor nbt = CustomDataItemEditor.editItem(item);

        nbt.setString("uuid", playerKey);
        nbt.setString("logType", logType.name());
        nbt.setFloat("xp", xp);
        item = nbt.setItemData();

        return item;
    }

    public ItemStack restoreAllInventory(LogType logType, Long timestamp) {       
        ItemStack item = new ItemStack(getRestoreAllInventoryIcon());

        ItemMeta meta = item.getItemMeta();
        assert meta != null;
        meta.setDisplayName(MessageData.getMainInventoryRestoreButton());

        item.setItemMeta(meta);

        CustomDataItemEditor nbt = CustomDataItemEditor.editItem(item);

        nbt.setString("uuid", playerKey);
        nbt.setString("logType", logType.name());
        nbt.setLong("timestamp", timestamp);
        item = nbt.setItemData();

        return item;
    }

    public ItemStack restoreAllInventoryDisabled(LogType logType, Long timestamp) {
        ItemStack item = new ItemStack(getRestoreAllInventoryDisabledIcon());

        ItemMeta meta = item.getItemMeta();
        assert meta != null;

        String[] nameParts = MessageData.getMainInventoryDisabledButton().split("\\\\n");
        String titlePart = nameParts[0];
        ArrayList<String> loreParts = new ArrayList<>();

        meta.setDisplayName(titlePart);
        for (int i = 1; i < nameParts.length; i ++) {
            loreParts.add(nameParts[i]);
        }
        meta.setLore(loreParts);

        item.setItemMeta(meta);

        CustomDataItemEditor nbt = CustomDataItemEditor.editItem(item);

        nbt.setString("uuid", playerKey);
        nbt.setString("logType", logType.name());
        nbt.setLong("timestamp", timestamp);
        item = nbt.setItemData();

        return item;
    }

    private static @NotNull List<Pattern> createBannerPatterns(boolean isNext) {
        List<Pattern> patterns = new ArrayList<>();
        if (InventoryRollbackPlus.getInstance().getVersion().greaterOrEqThan(BukkitVersion.v1_20_R4)) {
            patterns.add(new Pattern(DyeColor.BLACK, PatternType.BASE));
            patterns.add(new Pattern(DyeColor.WHITE, PatternType.RHOMBUS));
            if (isNext) patterns.add(new Pattern(DyeColor.BLACK, PatternType.HALF_VERTICAL));
            else patterns.add(new Pattern(DyeColor.BLACK, PatternType.HALF_VERTICAL_RIGHT));
            patterns.add(new Pattern(DyeColor.GRAY, PatternType.BORDER));

        } else {
            patterns.add(new Pattern(DyeColor.BLACK, PatternType.BASE));
            patterns.add(new Pattern(DyeColor.WHITE, PatternType.valueOf("RHOMBUS_MIDDLE")));
            if (isNext) patterns.add(new Pattern(DyeColor.BLACK, PatternType.HALF_VERTICAL));
            else patterns.add(new Pattern(DyeColor.BLACK, PatternType.valueOf("HALF_VERTICAL_MIRROR")));
            patterns.add(new Pattern(DyeColor.GRAY, PatternType.BORDER));
        }
        return patterns;
    }

    /** Boton informativo con el ping que tenia el jugador al morir. */
    public ItemStack pingButton(Integer ping) {
        ItemStack item = new ItemStack(getPingIcon());

        ItemMeta meta = item.getItemMeta();
        assert meta != null;
        meta.setDisplayName(MessageData.menu("menus.ping.button-name"));

        List<String> lore = new ArrayList<>();
        if (ping != null && ping >= 0) {
            lore.add(MessageData.menu("menus.ping.button-lore").replace("%PING%", String.valueOf(ping)));
        } else {
            lore.add(MessageData.menu("menus.ping.button-lore-unknown"));
        }
        meta.setLore(lore);

        item.setItemMeta(meta);
        return item;
    }

    /** Cristal decorativo sin nombre (verde o blanco). */
    public static ItemStack decorationPane(boolean green) {
        ItemStack pane;

        Material modern = Material.matchMaterial(green ? "LIME_STAINED_GLASS_PANE" : "WHITE_STAINED_GLASS_PANE");
        if (modern != null) {
            pane = new ItemStack(modern);
        } else {
            Material legacy = Material.getMaterial("STAINED_GLASS_PANE");
            if (legacy == null) return null;
            pane = new ItemStack(legacy, 1, (short) (green ? 5 : 0));
        }

        ItemMeta meta = pane.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(ChatColor.RESET + " ");
            pane.setItemMeta(meta);
        }

        return pane;
    }

    /** Rellena un rango de slots vacios con cristales alternando verde y blanco. */
    public static void fillDecoration(Inventory inventory, int fromSlot, int toSlotExclusive) {
        for (int slot = fromSlot; slot < toSlotExclusive; slot++) {
            if (slot < 0 || slot >= inventory.getSize()) continue;
            if (inventory.getItem(slot) != null) continue;

            ItemStack pane = decorationPane((slot - fromSlot) % 2 == 0);
            if (pane != null) inventory.setItem(slot, pane);
        }
    }

    /** Primer material que exista en esta version del servidor (si ninguno existe, papel). */
    public static Material icon(String... names) {
        for (String name : names) {
            Material material = Material.matchMaterial(name);
            if (material != null) return material;
        }
        return Material.PAPER;
    }

    /** Divide un texto de menu en lineas (acepta "\\n" escrito en el yml o saltos reales). */
    public static List<String> splitLines(String text) {
        List<String> lines = new ArrayList<>();
        for (String line : text.split("\\n|\n")) lines.add(line);
        return lines;
    }

    /**
     * Boton de accion para el menu principal. Guarda en el item la accion y datos extra
     * (el tag "uuid" se rellena siempre porque el listener lo usa para reconocer los botones del plugin).
     */
    public static ItemStack actionButton(Material material, String name, List<String> lore, String action, String ownerKey, String... extraKeyValues) {
        ItemStack item = new ItemStack(material);

        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(name);
            if (lore != null) meta.setLore(lore);
            meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
            item.setItemMeta(meta);
        }

        CustomDataItemEditor nbt = CustomDataItemEditor.editItem(item);
        nbt.setString("uuid", ownerKey);
        nbt.setString("action", action);
        for (int i = 0; i + 1 < extraKeyValues.length; i += 2) {
            nbt.setString(extraKeyValues[i], extraKeyValues[i + 1]);
        }
        return nbt.setItemData();
    }

    /** Linea de lore para indicar la pagina. */
    public static String pageLore(int page) {
        return MessageData.menu("menus.lore.page").replace("%PAGE%", String.valueOf(page));
    }

    public ItemStack giveShulkerBox(LogType logType, Long timestamp) {
        ItemStack item = new ItemStack(getGiveShulkerBoxIcon());

        ItemMeta meta = item.getItemMeta();
        assert meta != null;

        String[] nameParts = MessageData.getShulkerBoxButton().split("\\\\n");
        String titlePart = nameParts[0];
        ArrayList<String> loreParts = new ArrayList<>();

        meta.setDisplayName(titlePart);
        for (int i = 1; i < nameParts.length; i ++) {
            loreParts.add(nameParts[i]);
        }
        meta.setLore(loreParts);

        item.setItemMeta(meta);

        CustomDataItemEditor nbt = CustomDataItemEditor.editItem(item);

        nbt.setString("uuid", playerKey);
        nbt.setString("logType", logType.name());
        nbt.setLong("timestamp", timestamp);
        item = nbt.setItemData();

        return item;
    }

}
