package com.janboerman.recipes.command

import org.bukkit.command.{Command, CommandExecutor, CommandSender}
import org.bukkit.entity.Player
import com.janboerman.recipes.listeners.GuiInventoryHolderListener

object ReOpenCommandExecutor extends CommandExecutor {
    override def onCommand(sender: CommandSender, command: Command, label: String, args: Array[String]): Boolean = {
        if (!sender.isInstanceOf[Player]) {
            sender.sendMessage("You can only use this command as a player.")
            return true
        }

        val player = sender.asInstanceOf[Player]
        GuiInventoryHolderListener.getLastOpenedGui(player).foreach(holder => player.openInventory(holder.getInventory))

        true
    }
}
