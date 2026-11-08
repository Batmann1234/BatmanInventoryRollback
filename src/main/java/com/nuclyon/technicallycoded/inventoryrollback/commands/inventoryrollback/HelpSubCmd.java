package com.nuclyon.technicallycoded.inventoryrollback.commands.inventoryrollback;

import com.nuclyon.technicallycoded.inventoryrollback.InventoryRollbackPlus;
import com.nuclyon.technicallycoded.inventoryrollback.commands.IRPCommand;
import com.nuclyon.technicallycoded.inventoryrollback.util.ColorUtil;
import me.danjono.inventoryrollback.config.MessageData;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

public class HelpSubCmd extends IRPCommand {

    public HelpSubCmd(InventoryRollbackPlus mainIn) {
        super(mainIn);
    }

    @Override
    public void onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (sender.hasPermission("batmaninventoryrollback.help")) {
            this.sendHelp(sender);
        } else {
            sender.sendMessage(MessageData.getPluginPrefix() + MessageData.getNoPermission());
        }
        return;
    }

    public void sendHelp(CommandSender sender) {
        sender.sendMessage(
                MessageData.getPluginPrefix() + ColorUtil.colorize("<#2ECC71>&lBatmanInventoryRollback\n") +
                        ColorUtil.colorize("<#FFFFFF>  Comandos disponibles:\n") +
                        ColorUtil.colorize("<#2ECC71>    /bir restore [jugador]<#FFFFFF> - Abre el menú de respaldos (opcional: jugador)\n") +
                        ColorUtil.colorize("<#2ECC71>    /bir forcebackup <all/player> [jugador]<#FFFFFF> - Fuerza un respaldo del inventario\n") +
                        ColorUtil.colorize("<#2ECC71>    /bir enable<#FFFFFF> - Activa el plugin\n") +
                        ColorUtil.colorize("<#2ECC71>    /bir disable<#FFFFFF> - Desactiva el plugin\n") +
                        ColorUtil.colorize("<#2ECC71>    /bir reload<#FFFFFF> - Recarga el plugin\n") +
                        ColorUtil.colorize("<#2ECC71>    /bir help<#FFFFFF> - Muestra este mensaje\n") +
                        ColorUtil.colorize("<#2ECC71>    /bir version<#FFFFFF> - Muestra la información y versión del plugin\n"));
    }

}
