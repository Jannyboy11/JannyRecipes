package xyz.janboerman.recipes.command

import org.bukkit.command.{Command, CommandExecutor, CommandSender}
import org.bukkit.entity.Player
import xyz.janboerman.recipes.RecipesPlugin
import xyz.janboerman.recipes.api.gui.RecipesMenu

object RecipesCommandExecutor extends CommandExecutor {
    override def onCommand(sender: CommandSender, command: Command, label: String, args: Array[String]): Boolean = {
        if (!sender.isInstanceOf[Player]) {
            sender.sendMessage("You can only use this command as a player.")
            return true
        }

        val player = sender.asInstanceOf[Player]
        player.openInventory(new RecipesMenu(RecipesPlugin, RecipesPlugin).getInventory)

        true
    }
}
