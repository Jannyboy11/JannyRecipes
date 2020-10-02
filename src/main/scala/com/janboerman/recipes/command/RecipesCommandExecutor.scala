package com.janboerman.recipes.command

import org.bukkit.command.{Command, CommandExecutor, CommandSender}
import org.bukkit.entity.Player
import com.janboerman.recipes.RecipesPlugin
import com.janboerman.recipes.api.gui.RecipesMenu

object RecipesCommandExecutor extends CommandExecutor {
    override def onCommand(sender: CommandSender, command: Command, label: String, args: Array[String]): Boolean = {
        if (!sender.isInstanceOf[Player]) {
            sender.sendMessage("You can only use this command as a player.")
            return true
        }

        implicit val recipesPlugin = RecipesPlugin
        implicit val api = RecipesPlugin.getAPI()

        val player = sender.asInstanceOf[Player]
        player.openInventory(new RecipesMenu[RecipesPlugin.type]().getInventory)

        true
    }
}
