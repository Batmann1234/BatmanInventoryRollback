package me.danjono.inventoryrollback.listeners;

import com.nuclyon.technicallycoded.inventoryrollback.InventoryRollbackPlus;
import com.nuclyon.technicallycoded.inventoryrollback.customdata.CustomDataItemEditor;
import com.tcoded.lightlibs.bukkitversion.BukkitVersion;
import com.tcoded.lightlibs.bukkitversion.MCVersion;
import io.papermc.lib.PaperLib;
import me.danjono.inventoryrollback.InventoryRollback;
import me.danjono.inventoryrollback.config.ConfigData;
import me.danjono.inventoryrollback.config.MessageData;
import me.danjono.inventoryrollback.config.SoundData;
import me.danjono.inventoryrollback.data.LogType;
import me.danjono.inventoryrollback.inventory.SaveInventory;
import me.danjono.inventoryrollback.data.PlayerData;
import me.danjono.inventoryrollback.gui.Buttons;
import me.danjono.inventoryrollback.gui.InventoryName;
import me.danjono.inventoryrollback.gui.menu.*;
import me.danjono.inventoryrollback.inventory.RestoreInventory;
import org.bukkit.*;
import org.bukkit.block.ShulkerBox;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BlockStateMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

public class ClickGUI implements Listener {

    private final InventoryRollbackPlus main;

    private static boolean isLocationAvailable(Location location) {
        return location != null;
    }

    public ClickGUI() {
        this.main = InventoryRollbackPlus.getInstance();
    }

    @EventHandler
    public void onInventoryDrag(InventoryDragEvent e) {
        //Cancel listener if the event is not for an EpicFishing GUI menu
        String title = e.getView().getTitle();
        if (!InventoryName.MAIN_MENU.is(title) 
                && !InventoryName.PLAYER_MENU.is(title) 
                && !InventoryName.ROLLBACK_LIST.is(title)
                && !InventoryName.MAIN_BACKUP.is(title)
                && !InventoryName.ENDER_CHEST_BACKUP.is(title))
            return;

        e.setCancelled(true);

        //Check if inventory is a virtual one and not one that has the same name on a player chest
        if (this.main.getVersion().greaterOrEqThan(BukkitVersion.v1_9_R1) && isLocationAvailable(e.getInventory().getLocation())) {
            e.setCancelled(false);
            return;
        }

        for (Integer slot : e.getRawSlots()) {            
            if (slot < e.getInventory().getSize()) {
                return;
            }
        }

        e.setCancelled(false);
    }



    @EventHandler
    public void onInventoryClick(InventoryClickEvent e) {
        String title = e.getView().getTitle();
        if (!InventoryName.MAIN_MENU.is(title) 
                && !InventoryName.PLAYER_MENU.is(title) 
                && !InventoryName.ROLLBACK_LIST.is(title)
                && !InventoryName.MAIN_BACKUP.is(title)
                && !InventoryName.ENDER_CHEST_BACKUP.is(title))
            return;

        //Check if inventory is a virtual one and not one that has the same name on a player chest
        if (this.main.getVersion().greaterOrEqThan(BukkitVersion.v1_9_R1) && isLocationAvailable(e.getInventory().getLocation())) {
            return;
        }

        e.setCancelled(true);

        Player staff = (Player) e.getWhoClicked();
        ItemStack icon = e.getCurrentItem();

        //Listener for player menu
        if (InventoryName.MAIN_MENU.is(title)) {
            mainMenu(e,staff, icon);
        }

        //Listener for player menu
        else if (InventoryName.PLAYER_MENU.is(title)) {
            playerMenu(e,staff, icon);
        }

        //Listener for rollback list menu
        else if (InventoryName.ROLLBACK_LIST.is(title)) {
            rollbackMenu(e,staff, icon);
        }

        //Listener for main inventory backup menu
        else if (InventoryName.MAIN_BACKUP.is(title)) {
            mainBackupMenu(e,staff, icon);
        }

        //Listener for enderchest backup menu
        else if (InventoryName.ENDER_CHEST_BACKUP.is(title)) {
            enderChestBackupMenu(e,staff, icon);
        }

        else {
            e.setCancelled(true);
        }
    }

    private void openMainMenu(Player staff, int page, boolean offline, boolean silent) {
        MainMenu menu = new MainMenu(staff, page, offline, silent);

        staff.openInventory(menu.getInventory());
        Bukkit.getScheduler().runTaskAsynchronously(InventoryRollback.getInstance(), menu::getMainMenu);
    }

    private void mainMenu(InventoryClickEvent e, Player staff, ItemStack icon) {
        if ((e.getRawSlot() >= 0 && e.getRawSlot() < InventoryName.MAIN_MENU.getSize())) {
            if (icon == null)
                return;

            CustomDataItemEditor nbt = CustomDataItemEditor.editItem(icon);
            if (!nbt.hasUUID())
                return;

            String action = nbt.getString("action");
            boolean offline = "OFFLINE".equals(nbt.getString("mode"));
            int page = Math.max(1, nbt.getInt("page"));

            //Clicked one of the panel buttons
            if (action != null && !action.isEmpty()) {
                switch (action) {
                    case "mode":
                        SoundData.playMenu(staff, "mode");
                        openMainMenu(staff, 1, offline, true);
                        break;

                    case "self":
                        SoundData.playMenu(staff, "self");
                        PlayerMenu selfMenu = new PlayerMenu(staff, staff);
                        staff.openInventory(selfMenu.getInventory());
                        Bukkit.getScheduler().runTaskAsynchronously(InventoryRollback.getInstance(), selfMenu::getPlayerMenu);
                        break;

                    case "refresh":
                        SoundData.playMenu(staff, "refresh");
                        openMainMenu(staff, page, offline, true);
                        break;

                    case "saveall": {
                        if (!staff.hasPermission("batmaninventoryrollback.forcebackup")) {
                            SoundData.playMenu(staff, "error");
                            staff.sendMessage(MessageData.getPluginPrefix() + MessageData.getNoPermission());
                            break;
                        }

                        int saved = 0;
                        for (Player online : Bukkit.getOnlinePlayers()) {
                            new SaveInventory(online, LogType.FORCE, null, null)
                                    .snapshotAndSave(online.getInventory(), online.getEnderChest(), true);
                            saved++;
                        }

                        SoundData.playMenu(staff, "save-all");
                        staff.sendMessage(MessageData.getPluginPrefix()
                                + MessageData.menu("menus.main.saved-all").replace("%VALUE%", String.valueOf(saved)));
                        openMainMenu(staff, page, offline, true);
                        break;
                    }

                    case "toggle": {
                        boolean enabled = ConfigData.isEnabled();
                        String permission = enabled ? "batmaninventoryrollback.disable" : "batmaninventoryrollback.enable";
                        if (!staff.hasPermission(permission)) {
                            SoundData.playMenu(staff, "error");
                            staff.sendMessage(MessageData.getPluginPrefix() + MessageData.getNoPermission());
                            break;
                        }

                        this.main.getConfigData().persistEnabled(!enabled);

                        SoundData.playMenu(staff, enabled ? "toggle-off" : "toggle-on");
                        staff.sendMessage(MessageData.getPluginPrefix()
                                + MessageData.menu(enabled ? "menus.main.toggled-off" : "menus.main.toggled-on"));
                        openMainMenu(staff, page, offline, true);
                        break;
                    }

                    case "reload": {
                        if (!staff.hasPermission("batmaninventoryrollback.reload")) {
                            SoundData.playMenu(staff, "error");
                            staff.sendMessage(MessageData.getPluginPrefix() + MessageData.getNoPermission());
                            break;
                        }

                        ConfigData config = this.main.getConfigData();
                        config.generateConfigFile();
                        config.setVariables();
                        this.main.startupTasks();

                        SoundData.playMenu(staff, "reload");
                        staff.sendMessage(MessageData.getPluginPrefix() + MessageData.getPluginReload());
                        openMainMenu(staff, page, offline, true);
                        break;
                    }

                    case "close":
                        SoundData.playMenu(staff, "close");
                        staff.closeInventory();
                        break;

                    case "info":
                        SoundData.playMenu(staff, "info");
                        break;

                    default:
                        break;
                }
            }
            //Clicked a page button
            else if (icon.getType().equals(Buttons.getPageSelectorIcon())) {
                SoundData.playMenu(staff, "page");
                openMainMenu(staff, nbt.getInt("page"), offline, true);
            } 
            //Clicked a player head
            else {
                SoundData.playMenu(staff, "player");

                OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(nbt.getString("uuid"));
                PlayerMenu menu = new PlayerMenu(staff, offlinePlayer);

                staff.openInventory(menu.getInventory());
                Bukkit.getScheduler().runTaskAsynchronously(InventoryRollback.getInstance(), menu::getPlayerMenu);
            }
        } else {
            if (e.getRawSlot() >= e.getInventory().getSize() && !e.isShiftClick()) {
                e.setCancelled(false);
            }
        }
    }

    private void playerMenu(InventoryClickEvent e, Player staff, ItemStack icon) {
        //Return if a blank slot is selected
        if (icon == null)
            return;

        if ((e.getRawSlot() >= 0 && e.getRawSlot() < InventoryName.PLAYER_MENU.getSize())) {				
            CustomDataItemEditor nbt = CustomDataItemEditor.editItem(icon);
            if (!nbt.hasUUID())
                return;

            OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(nbt.getString("uuid"));

            //Clicked player head
            if (e.getRawSlot() == 0) {
                MainMenu menu = new MainMenu(staff, 1);

                staff.openInventory(menu.getInventory());
                Bukkit.getScheduler().runTaskAsynchronously(InventoryRollback.getInstance(), menu::getMainMenu);
            } else {
                LogType logType = LogType.valueOf(nbt.getString("logType"));
                RollbackListMenu menu = new RollbackListMenu(staff, offlinePlayer, logType, 1);

                staff.openInventory(menu.getInventory());
                Bukkit.getScheduler().runTaskAsynchronously(InventoryRollback.getInstance(), menu::showBackups);
            }

        } else {
            if (e.getRawSlot() >= e.getInventory().getSize() && !e.isShiftClick()) {
                e.setCancelled(false);
            }
        }
    }

    private void rollbackMenu(InventoryClickEvent e, Player staff, ItemStack icon) {
        if (e.getRawSlot() >= 0 && e.getRawSlot() < InventoryName.ROLLBACK_LIST.getSize()) {
            if (icon == null) return;

            CustomDataItemEditor nbt = CustomDataItemEditor.editItem(icon);
            if (!nbt.hasUUID())
                return;

            //Player has selected a backup to open
            if (icon.getType().equals(Material.CHEST)) {
                String playerKey = nbt.getString("uuid");
                Long timestamp = nbt.getLong("timestamp");
                LogType logType = LogType.valueOf(nbt.getString("logType"));
                String location = nbt.getString("location");

                // Run all data retrieval operations async to avoid tick lag
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        // Init from MySQL or, if YAML, init & load config file
                        PlayerData data = new PlayerData(playerKey, logType, timestamp);

                        // Get from MySQL
                        if (ConfigData.getSaveType() == ConfigData.SaveType.MYSQL) {
                            try {
                                data.getAllBackupData().get();
                            } catch (ExecutionException | InterruptedException ex) {
                                ex.printStackTrace();
                            }
                        }

                        // Create inventory
                        MainInventoryBackupMenu menu = new MainInventoryBackupMenu(staff, data, location);

                        // Display inventory to player
                        Future<InventoryView> inventoryViewFuture =
                                main.getServer().getScheduler().callSyncMethod(main,
                                        () -> staff.openInventory(menu.getInventory()));
                        //If the backup file is invalid it will return null, we want to catch it here
                        try {
                            inventoryViewFuture.get();
                            // Start placing items in the inventory async
                            menu.showBackupItems();
                        } catch (NullPointerException | ExecutionException | InterruptedException ex) {
                            ex.printStackTrace();
                        }
                    }
                }.runTaskAsynchronously(main);
            } 

            //Player has selected a page icon
            else if (icon.getType().equals(Buttons.getPageSelectorIcon())) {
                int page = nbt.getInt("page");

                //Selected to go back to main menu
                OfflinePlayer player = Bukkit.getOfflinePlayer(nbt.getString("uuid"));
                if (page == 0) {
                    PlayerMenu menu = new PlayerMenu(staff, player);

                    staff.openInventory(menu.getInventory());
                    Bukkit.getScheduler().runTaskAsynchronously(InventoryRollback.getInstance(), menu::getPlayerMenu);
                } else {
                    LogType logType = LogType.valueOf(nbt.getString("logType"));
                    RollbackListMenu menu = new RollbackListMenu(staff, player, logType, page);

                    staff.openInventory(menu.getInventory());
                    Bukkit.getScheduler().runTaskAsynchronously(InventoryRollback.getInstance(), menu::showBackups);
                }
            }	
        } else {
            if (e.getRawSlot() >= e.getInventory().getSize() && !e.isShiftClick()) {
                e.setCancelled(false);
            }
        }
    }

    private void mainBackupMenu(InventoryClickEvent e, Player staff, ItemStack icon) {
        if (!InventoryName.MAIN_BACKUP.is(e.getView().getTitle()))
            return;

        if (e.getRawSlot() >= (InventoryName.MAIN_BACKUP.getSize() - 9) && e.getRawSlot() < InventoryName.MAIN_BACKUP.getSize()) {
            CustomDataItemEditor nbt = CustomDataItemEditor.editItem(icon);
            if (!nbt.hasUUID())
                return;

            OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(nbt.getString("uuid"));            
            LogType logType = LogType.valueOf(nbt.getString("logType"));
            Long timestamp = nbt.getLong("timestamp");

            //Click on page selector button to go back to rollback menu
            if (icon.getType().equals(Buttons.getPageSelectorIcon())) {
                RollbackListMenu menu = new RollbackListMenu(staff, offlinePlayer, logType, 1);

                staff.openInventory(menu.getInventory());
                Bukkit.getScheduler().runTaskAsynchronously(InventoryRollback.getInstance(), menu::showBackups);
            }

            //Click on page selector button to go back to rollback menu
            else if (e.getRawSlot() == MainInventoryBackupMenu.GIVE_SHULKERS_BUTTON_SLOT) {
                // Perm check
                if (!staff.hasPermission("batmaninventoryrollback.restore")) {
                    staff.sendMessage(MessageData.getPluginPrefix() + MessageData.getNoPermission());
                    return;
                }

                Bukkit.getScheduler().runTaskAsynchronously(InventoryRollback.getInstance(), () -> {
                    // Unsupported on older versions
                    if (main.getVersion().lessThan(MCVersion.v1_11.toBukkitVersion())) {
                        return;
                    }

                    // Give shulkers

                    // Init from MySQL or, if YAML, init & load config file
                    PlayerData data = new PlayerData(offlinePlayer, logType, timestamp);

                    // Get data if using MySQL
                    if (ConfigData.getSaveType() == ConfigData.SaveType.MYSQL) {
                        try {
                            data.getAllBackupData().get();
                        } catch (ExecutionException | InterruptedException ex) {
                            ex.printStackTrace();
                        }
                    }

                    ItemStack[] mainInventory = data.getMainInventory();
                    ItemStack[] extraItems = data.getArmour();

                    if (extraItems == null || extraItems.length == 0) {
                        int extraItemsLen = mainInventory.length - 36;
                        extraItems = new ItemStack[extraItemsLen];
                        System.arraycopy(mainInventory, 36, extraItems, 0, extraItemsLen);
                    }

                    ItemStack[] hotBar = Arrays.copyOfRange(mainInventory, 0, Math.min(mainInventory.length, 9));
                    ItemStack[] invContents = mainInventory.length >= 9
                            ? Arrays.copyOfRange(mainInventory, 9, mainInventory.length)
                            : new ItemStack[36];

                    ItemStack[] firstShulkerContents = new ItemStack[27];
                    ItemStack[] secondShulkerContents = new ItemStack[27];

                    System.arraycopy(hotBar, 0, firstShulkerContents, 0, hotBar.length);
                    System.arraycopy(extraItems, 0, firstShulkerContents, 9, Math.min(extraItems.length, 18));

                    System.arraycopy(invContents, 0, secondShulkerContents, 0, Math.min(invContents.length, 27));

                    ItemStack firstShulker = new ItemStack(Material.SHULKER_BOX);
                    ItemStack secondShulker = new ItemStack(Material.SHULKER_BOX);

                    ItemMeta firstMeta = firstShulker.getItemMeta();
                    if (firstMeta instanceof BlockStateMeta) {
                        BlockStateMeta blockMeta = (BlockStateMeta) firstMeta;
                        if (blockMeta.getBlockState() instanceof ShulkerBox) {
                            ShulkerBox shulkerBox = (ShulkerBox) blockMeta.getBlockState();
                            shulkerBox.getInventory().setContents(firstShulkerContents);
                            blockMeta.setBlockState(shulkerBox);
                            blockMeta.setDisplayName(MessageData.getShulkerBoxFirstShulkerName());
                            blockMeta.setLore(MessageData.getShulkerBoxFirstShulkerLore());
                            firstShulker.setItemMeta(blockMeta);
                        }
                    }

                    ItemMeta secondMeta = secondShulker.getItemMeta();
                    if (secondMeta instanceof BlockStateMeta) {
                        BlockStateMeta blockMeta = (BlockStateMeta) secondMeta;
                        if (blockMeta.getBlockState() instanceof ShulkerBox) {
                            ShulkerBox shulkerBox = (ShulkerBox) blockMeta.getBlockState();
                            shulkerBox.getInventory().setContents(secondShulkerContents);
                            blockMeta.setBlockState(shulkerBox);
                            blockMeta.setDisplayName(MessageData.getShulkerBoxSecondShulkerName());
                            blockMeta.setLore(MessageData.getShulkerBoxSecondShulkerLore());
                            secondShulker.setItemMeta(blockMeta);
                        }
                    }

                    Bukkit.getScheduler().runTask(main, t -> {
                        staff.getInventory().addItem(firstShulker, secondShulker);
                        staff.closeInventory();
                    });
                });
            }

            //Clicked icon to overwrite player inventory with backup data
            else if (icon.getType().equals(Buttons.getRestoreAllInventoryIcon())) {
                // Perm check
                if (!staff.hasPermission("batmaninventoryrollback.restore")) {
                    staff.sendMessage(MessageData.getPluginPrefix() + MessageData.getNoPermission());
                    return;
                }

                if (offlinePlayer.isOnline()) {
                    Player player = (Player) offlinePlayer;

                    new BukkitRunnable() {
                        @Override
                        public void run() {
                            // Init from MySQL or, if YAML, init & load config file
                            PlayerData data = new PlayerData(offlinePlayer, logType, timestamp);

                            // Get data if using MySQL
                            if (ConfigData.getSaveType() == ConfigData.SaveType.MYSQL) {
                                try {
                                    data.getAllBackupData().get();
                                } catch (ExecutionException | InterruptedException ex) {
                                    ex.printStackTrace();
                                }
                            }

                            ItemStack[] inventory = data.getMainInventory();
                            ItemStack[] armour = data.getArmour();

                            // Place inventory items sync (compressed code)
                            Future<Void> futureSetInv = main.getServer().getScheduler().callSyncMethod(main,
                                    () -> { player.getInventory().setContents(inventory); return null; });
                            try { futureSetInv.get(); }
                            catch (ExecutionException | InterruptedException ex) { ex.printStackTrace(); }

                            // If 1.8, place armor contents separately
                            if (main.getVersion().lessOrEqThan(BukkitVersion.v1_8_R3)) {
                                // Place items sync (compressed code)
                                Future<Void> futureSetArmor = main.getServer().getScheduler().callSyncMethod(main,
                                        () -> { player.getInventory().setArmorContents(armour); return null; });
                                try { futureSetArmor.get(); }
                                catch (ExecutionException | InterruptedException ex) { ex.printStackTrace(); }
                            }

                            // Play sound effect is enabled
                            if (SoundData.isInventoryRestoreEnabled()) {
                                // Play sound sync (compressed code)
                                Future<Void> futurePlaySound = main.getServer().getScheduler().callSyncMethod(main,
                                        () -> { player.playSound(player.getLocation(), SoundData.getInventoryRestored(), 1, 1); return null; });
                                try { futurePlaySound.get(); }
                                catch (ExecutionException | InterruptedException ex) { ex.printStackTrace(); }
                            }

                            // Send player & staff feedback
                            player.sendMessage(MessageData.getPluginPrefix() + MessageData.getMainInventoryRestoredPlayer(staff.getName()));
                            if (!staff.getUniqueId().equals(player.getUniqueId()))
                                staff.sendMessage(MessageData.getPluginPrefix() + MessageData.getMainInventoryRestored(offlinePlayer.getName()));
                        }
                    }.runTaskAsynchronously(main);

                } else {
                    staff.sendMessage(MessageData.getPluginPrefix() + MessageData.getMainInventoryNotOnline(offlinePlayer.getName()));
                }
            }

            // Clicked icon to teleport player to backup coordinates
            else if (icon.getType().equals(Buttons.getTeleportLocationIcon())) {
                // Perm check
                if (!staff.hasPermission("batmaninventoryrollback.restore.teleport")) {
                    staff.sendMessage(MessageData.getPluginPrefix() + MessageData.getNoPermission());
                    return;
                }

                String[] location = nbt.getString("location").split(",");			
                World world = Bukkit.getWorld(location[0]);

                if (world == null) {
                    //World is not available
                    staff.sendMessage(MessageData.getPluginPrefix() + MessageData.getDeathLocationInvalidWorldError(location[0]));
                    return;
                }

                Location loc = new Location(world, 
                        Math.floor(Double.parseDouble(location[1])), 
                        Math.floor(Double.parseDouble(location[2])), 
                        Math.floor(Double.parseDouble(location[3])))
                        .add(0.5, 0.5, 0.5);				

                // Teleport player on a slight delay to block the teleport icon glitching out into the player inventory
                Bukkit.getScheduler().runTaskLater(InventoryRollback.getInstance(), () -> {
                    e.getWhoClicked().closeInventory();
                    PaperLib.teleportAsync(staff,loc).thenAccept((result) -> {
                        if (SoundData.isTeleportEnabled())
                            staff.playSound(loc, SoundData.getTeleport(), 1, 1);

                        staff.sendMessage(MessageData.getPluginPrefix() + MessageData.getDeathLocationTeleport(loc));
                    });
                }, 1L);
            } 

            // Clicked icon to restore backup players ender chest
            else if (icon.getType().equals(Buttons.getEnderChestIcon())) {

                new BukkitRunnable() {
                    @Override
                    public void run() {
                        // Init from MySQL or, if YAML, init & load config file
                        PlayerData data = new PlayerData(offlinePlayer, logType, timestamp);

                        // Get data if using MySQL
                        if (ConfigData.getSaveType() == ConfigData.SaveType.MYSQL) {
                            try {
                                data.getAllBackupData().get();
                            } catch (ExecutionException | InterruptedException ex) {
                                ex.printStackTrace();
                            }
                        }

                        // Create Inventory
                        EnderChestBackupMenu menu = new EnderChestBackupMenu(staff, data, 1);

                        // Open inventory sync (compressed code)
                        Future<Void> futureOpenInv = main.getServer().getScheduler().callSyncMethod(main,
                                () -> {
                                    staff.openInventory(menu.getInventory());
                                    return null;
                                });
                        try {
                            futureOpenInv.get();
                        } catch (ExecutionException | InterruptedException ex) {
                            ex.printStackTrace();
                        }

                        // Place items async
                        menu.showEnderChestItems();
                    }
                }.runTaskAsynchronously(this.main);
            }

            // Clicked icon to restore backup players health
            else if (icon.getType().equals(Buttons.getHealthIcon())) {
                // Perm check
                if (!staff.hasPermission("batmaninventoryrollback.restore")) {
                    staff.sendMessage(MessageData.getPluginPrefix() + MessageData.getNoPermission());
                    return;
                }

                if (offlinePlayer.isOnline()) {
                    Player player = (Player) offlinePlayer;	
                    double health = nbt.getDouble("health");

                    player.setHealth(health);

                    if (SoundData.isFoodRestoredEnabled())
                        player.playSound(player.getLocation(), SoundData.getFoodRestored(), 1, 1);

                    player.sendMessage(MessageData.getPluginPrefix() + MessageData.getHealthRestoredPlayer(staff.getName()));
                    if (!staff.getUniqueId().equals(player.getUniqueId()))
                        staff.sendMessage(MessageData.getPluginPrefix() + MessageData.getHealthRestored(player.getName()));
                } else {
                    staff.sendMessage(MessageData.getPluginPrefix() + MessageData.getHealthNotOnline(offlinePlayer.getName()));
                }
            } 

            //Clicked icon to restore backup players hunger
            else if (icon.getType().equals(Buttons.getHungerIcon())) {
                // Perm check
                if (!staff.hasPermission("batmaninventoryrollback.restore")) {
                    staff.sendMessage(MessageData.getPluginPrefix() + MessageData.getNoPermission());
                    return;
                }

                if (offlinePlayer.isOnline()) {
                    Player player = (Player) offlinePlayer;	
                    int hunger = nbt.getInt("hunger");
                    Float saturation = nbt.getFloat("saturation");

                    player.setFoodLevel(hunger);
                    player.setSaturation(saturation);

                    if (SoundData.isHungerRestoredEnabled())
                        player.playSound(player.getLocation(), SoundData.getHungerRestored(), 1, 1);

                    player.sendMessage(MessageData.getPluginPrefix() + MessageData.getHungerRestoredPlayer(staff.getName()));
                    if (!staff.getUniqueId().equals(player.getUniqueId()))
                        staff.sendMessage(MessageData.getPluginPrefix() + MessageData.getHungerRestored(player.getName()));
                } else {
                    staff.sendMessage(MessageData.getPluginPrefix() + MessageData.getHungerNotOnline(offlinePlayer.getName()));
                }
            } 

            //Clicked icon to restore backup players experience
            else if (icon.getType().equals(Buttons.getExperienceIcon())) {
                // Perm check
                if (!staff.hasPermission("batmaninventoryrollback.restore")) {
                    staff.sendMessage(MessageData.getPluginPrefix() + MessageData.getNoPermission());
                    return;
                }

                if (offlinePlayer.isOnline()) {				
                    Player player = (Player) offlinePlayer;	
                    Float xp = nbt.getFloat("xp");

                    RestoreInventory.setTotalExperience(player, xp);

                    if (SoundData.isExperienceRestoredEnabled())
                        player.playSound(player.getLocation(), SoundData.getExperienceSound(), 1, 1);

                    int level = (int) RestoreInventory.getLevel(xp);
                    player.sendMessage(MessageData.getPluginPrefix() + MessageData.getExperienceRestoredPlayer(staff.getName(), level));
                    if (!staff.getUniqueId().equals(player.getUniqueId()))
                        staff.sendMessage(MessageData.getPluginPrefix() + MessageData.getExperienceRestored(player.getName(), level));
                } else {				    
                    staff.sendMessage(MessageData.getPluginPrefix() + MessageData.getExperienceNotOnlinePlayer(offlinePlayer.getName()));
                }
            }
        } else {
            int slotIndex = e.getRawSlot();
            int topInvSize = e.getView().getTopInventory().getSize();
            boolean clickIsWithinPlayerInventory = slotIndex >= topInvSize;

            boolean clickIsWithinMainBackupInv = slotIndex < topInvSize - 18;
            boolean notInLastLine = slotIndex < topInvSize - 9;
            boolean notBeforeArmorSlots = slotIndex > topInvSize - 15;

            boolean clickIsWithinArmorOrOffHandSlots = notInLastLine && notBeforeArmorSlots;
            boolean isValidBackupMenuInteraction = clickIsWithinMainBackupInv || clickIsWithinArmorOrOffHandSlots;

            //Allow items to be grabbed in the top inventory except the bottom line AND NOT player inventory items to be shift clicked to top inventory
            if (clickIsWithinPlayerInventory && !e.isShiftClick()) {
                e.setCancelled(false);
            } else if (isValidBackupMenuInteraction) {
                if (staff.hasPermission("batmaninventoryrollback.restore")) {
                    e.setCancelled(false);
                } else {
                    staff.sendMessage(MessageData.getPluginPrefix() + MessageData.getNoPermission());
                }
            }
        }
    }

    private void enderChestBackupMenu(InventoryClickEvent e, Player staff, ItemStack icon) {
        if (!InventoryName.ENDER_CHEST_BACKUP.is(e.getView().getTitle()))
            return;

        if (e.getRawSlot() >= (InventoryName.ENDER_CHEST_BACKUP.getSize() - 9) && e.getRawSlot() < InventoryName.ENDER_CHEST_BACKUP.getSize()) {
            CustomDataItemEditor nbt = CustomDataItemEditor.editItem(icon);
            if (!nbt.hasUUID())
                return;

            OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(nbt.getString("uuid"));
            LogType logType = LogType.valueOf(nbt.getString("logType"));
            Long timestamp = nbt.getLong("timestamp");

            // Click on page selector button to go back to backup menu
            if (icon.getType().equals(Buttons.getPageSelectorIcon())) {

                //Player has selected a page icon
                int page = nbt.getInt("page");

                //Selected to go back to main menu
                if (page == 0) {

                    // Run all data retrieval operations async to avoid tick lag
                    new BukkitRunnable() {
                        @Override
                        public void run() {
                            // Init from MySQL or, if YAML, init & load config file
                            PlayerData data = new PlayerData(offlinePlayer, logType, timestamp);

                            // Get data if using MySQL
                            if (ConfigData.getSaveType() == ConfigData.SaveType.MYSQL) {
                                try {
                                    data.getAllBackupData().get();
                                } catch (ExecutionException | InterruptedException ex) {
                                    ex.printStackTrace();
                                }
                            }

                            // Get location of where the backup was made from data
                            String location = data.getWorld() + "," + data.getX() + "," + data.getY() + "," + data.getZ();

                            // Create inventory
                            MainInventoryBackupMenu menu = new MainInventoryBackupMenu(staff, data, location);

                            // Display inventory to player
                            Future<InventoryView> inventoryViewFuture = main.getServer().getScheduler().callSyncMethod(main,
                                    () -> staff.openInventory(menu.getInventory()));
                            //If the backup file is invalid it will return null, we want to catch it here
                            try {
                                inventoryViewFuture.get();
                                // Start placing items in the inventory async
                                menu.showBackupItems();
                            } catch (NullPointerException | ExecutionException | InterruptedException ex) {
                                ex.printStackTrace();
                            }
                        }
                    }.runTaskAsynchronously(main);

                } else {

                    new BukkitRunnable() {
                        @Override
                        public void run() {
                            // Init from MySQL or, if YAML, init & load config file
                            PlayerData data = new PlayerData(offlinePlayer, logType, timestamp);

                            // Get data if using MySQL
                            if (ConfigData.getSaveType() == ConfigData.SaveType.MYSQL) {
                                try {
                                    data.getAllBackupData().get();
                                } catch (ExecutionException | InterruptedException ex) {
                                    ex.printStackTrace();
                                }
                            }

                            // Create Inventory
                            EnderChestBackupMenu menu = new EnderChestBackupMenu(staff, data, page);

                            // Open inventory sync (compressed code)
                            Future<Void> futureOpenInv = main.getServer().getScheduler().callSyncMethod(main,
                                    () -> {
                                        staff.openInventory(menu.getInventory());
                                        return null;
                                    });
                            try {
                                futureOpenInv.get();
                            } catch (ExecutionException | InterruptedException ex) {
                                ex.printStackTrace();
                            }

                            // Place items async
                            menu.showEnderChestItems();
                        }
                    }.runTaskAsynchronously(this.main);
                }
            }

            //Click on page selector button to go back to rollback menu
            else if (e.getRawSlot() == EnderChestBackupMenu.GIVE_SHULKERS_BUTTON_SLOT) {
                // Perm check
                if (!staff.hasPermission("batmaninventoryrollback.restore")) {
                    staff.sendMessage(MessageData.getPluginPrefix() + MessageData.getNoPermission());
                    return;
                }

                Bukkit.getScheduler().runTaskAsynchronously(InventoryRollback.getInstance(), () -> {
                    // Unsupported on older versions
                    if (main.getVersion().lessThan(MCVersion.v1_11.toBukkitVersion())) {
                        return;
                    }

                    // Give shulkers

                    // Init from MySQL or, if YAML, init & load config file
                    PlayerData data = new PlayerData(offlinePlayer, logType, timestamp);

                    // Get data if using MySQL
                    if (ConfigData.getSaveType() == ConfigData.SaveType.MYSQL) {
                        try {
                            data.getAllBackupData().get();
                        } catch (ExecutionException | InterruptedException ex) {
                            ex.printStackTrace();
                        }
                    }

                    ItemStack[] enderChest = data.getEnderChest();

                    List<ItemStack> shulkers = new ArrayList<>();
                    if (enderChest != null && enderChest.length != 0) {
                        int totalItems = enderChest.length;
                        int shulkerCount = (int) Math.ceil(totalItems / 27.0);

                        for (int i = 0; i < shulkerCount; i++) {
                            int start = i * 27;
                            int end = Math.min(start + 27, totalItems);

                            ItemStack[] shulkerContents = new ItemStack[27];
                            System.arraycopy(enderChest, start, shulkerContents, 0, end - start);

                            ItemStack shulker = new ItemStack(Material.SHULKER_BOX);
                            ItemMeta meta = shulker.getItemMeta();

                            if (meta instanceof BlockStateMeta) {
                                BlockStateMeta blockMeta = (BlockStateMeta) meta;
                                if (blockMeta.getBlockState() instanceof ShulkerBox) {
                                    ShulkerBox shulkerBox = (ShulkerBox) blockMeta.getBlockState();
                                    shulkerBox.getInventory().setContents(shulkerContents);
                                    blockMeta.setBlockState(shulkerBox);
                                    String name = (shulkerCount == 1)
                                            ? MessageData.getShulkerBoxEnderChestShulkerName()
                                            : (MessageData.getShulkerBoxEnderChestShulkerName() + MessageData.getShulkerBoxEnderChestShulkerExtraShulkers(i+1));
                                    blockMeta.setDisplayName(name);
                                    blockMeta.setLore(MessageData.getShulkerBoxEnderChestShulkerLore());
                                    shulker.setItemMeta(blockMeta);
                                }
                            }

                            shulkers.add(shulker);
                        }
                    };

                    Bukkit.getScheduler().runTask(main, t -> {
                        staff.getInventory().addItem(shulkers.toArray(new ItemStack[0]));
                        staff.closeInventory();
                    });
                });
            }

            //Clicked icon to overwrite player ender chest with backup data
            else if (icon.getType().equals(Buttons.getRestoreAllInventoryIcon())) {
                // Perm check
                if (!staff.hasPermission("batmaninventoryrollback.restore")) {
                    staff.sendMessage(MessageData.getPluginPrefix() + MessageData.getNoPermission());
                    return;
                }

                if (offlinePlayer.isOnline()) {
                    Player player = (Player) offlinePlayer;

                    // Run all data retrieval operations async to avoid tick lag
                    new BukkitRunnable() {
                        @Override
                        public void run() {
                            // Init from MySQL or, if YAML, init & load config file
                            PlayerData data = new PlayerData(offlinePlayer, logType, timestamp);

                            // Get from MySQL
                            if (ConfigData.getSaveType() == ConfigData.SaveType.MYSQL) {
                                try {
                                    data.getAllBackupData().get();
                                } catch (ExecutionException | InterruptedException ex) {
                                    ex.printStackTrace();
                                }
                            }

                            // Display inventory to player
                            Future<Void> inventoryReplaceFuture = main.getServer().getScheduler().callSyncMethod(main,
                                    () -> {
                                        ItemStack[] enderChest = data.getEnderChest();
                                        if (enderChest == null) enderChest = new ItemStack[0];
                                        player.getEnderChest().setContents(enderChest);
                                        return null;
                                    });

                            //If the backup file is invalid it will return null, we want to catch it here
                            try {
                                inventoryReplaceFuture.get();
                            } catch (NullPointerException | ExecutionException | InterruptedException ex) {
                                ex.printStackTrace();
                            }
                        }
                    }.runTaskAsynchronously(main);

                    if (SoundData.isInventoryRestoreEnabled())
                        player.playSound(player.getLocation(), SoundData.getInventoryRestored(), 1, 1); 

                    player.sendMessage(MessageData.getPluginPrefix() + MessageData.getEnderChestRestoredPlayer(staff.getName()));
                    if (!staff.getUniqueId().equals(player.getUniqueId()))
                        staff.sendMessage(MessageData.getPluginPrefix() + MessageData.getEnderChestRestored(offlinePlayer.getName()));
                } else {
                    staff.sendMessage(MessageData.getPluginPrefix() + MessageData.getEnderChestNotOnline(offlinePlayer.getName()));
                }
            }
        } else {
            int slotIndex = e.getRawSlot();
            int topInvSize = e.getView().getTopInventory().getSize();
            boolean clickIsWithinPlayerInventory = slotIndex >= topInvSize;

            if (clickIsWithinPlayerInventory && !e.isShiftClick()) {
                e.setCancelled(false);
            } else if (slotIndex < topInvSize - 9) {
                // Perm check
                if (!staff.hasPermission("batmaninventoryrollback.restore")) {
                    staff.sendMessage(MessageData.getPluginPrefix() + MessageData.getNoPermission());
                    return;
                }
                e.setCancelled(false);
            }
        }
    }

}