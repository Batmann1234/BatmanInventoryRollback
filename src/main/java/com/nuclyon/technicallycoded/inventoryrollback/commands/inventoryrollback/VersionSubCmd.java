package com.nuclyon.technicallycoded.inventoryrollback.commands.inventoryrollback;

import com.nuclyon.technicallycoded.inventoryrollback.InventoryRollbackPlus;
import com.nuclyon.technicallycoded.inventoryrollback.commands.IRPCommand;
import me.danjono.inventoryrollback.InventoryRollback;
import com.nuclyon.technicallycoded.inventoryrollback.util.ColorUtil;
import me.danjono.inventoryrollback.config.MessageData;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

public class VersionSubCmd extends IRPCommand {

    public VersionSubCmd(InventoryRollbackPlus mainIn) {
        super(mainIn);
    }

    @Override
    public void onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        StringBuilder strb = new StringBuilder(MessageData.getPluginPrefix());
        boolean hasVersionPerm = sender.hasPermission("inventoryrollbackplus.version");

        strb.append("\n")
            .append(ColorUtil.colorize("<#FFFFFF>Plugin:")).append("\n")
            .append(ColorUtil.colorize("<#2ECC71>  BatmanInventoryRollback"));
        // Can see version?
        if (hasVersionPerm) strb.append(" v").append(InventoryRollback.getPluginVersion());
        strb.append("\n");
        // Else show warning
        if (!hasVersionPerm)
            strb.append(ColorUtil.colorize("<#FFFFFF>  (Versión no visible, falta el permiso)"))
                .append("\n");

        strb.append(ColorUtil.colorize("<#FFFFFF>Créditos:")).append("\n")
            .append(ColorUtil.colorize("<#2ECC71>  - Basado en InventoryRollbackPlus de TechnicallyCoded")).append("\n")
            .append(ColorUtil.colorize("<#2ECC71>  - Autor original: danjono")).append("\n");

        // Send
        sender.sendMessage(strb.toString());
    }

}
