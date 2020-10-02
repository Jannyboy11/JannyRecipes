package com.janboerman.recipes.command

import com.janboerman.recipes.api.JannyRecipesAPI
import com.janboerman.recipes.api.gui.{RecipeGuiFactory, RecipesMenu}
import org.bukkit.command.{Command, CommandExecutor, CommandSender}
import org.bukkit.entity.Player
import com.janboerman.recipes.RecipesPlugin
import com.janboerman.recipes.api.JannyRecipesAPI
import com.janboerman.recipes.api.gui.{NewMenu, RecipeGuiFactory, RecipesMenu}
import com.janboerman.recipes.listeners.GuiInventoryHolderListener

object AddRecipeCommandExecutor extends CommandExecutor {

    override def onCommand(sender: CommandSender, command: Command, label: String, args: Array[String]): Boolean = {
        if (!sender.isInstanceOf[Player]) {
            sender.sendMessage("You can only use this command as a player.")
            return true
        }

        val player = sender.asInstanceOf[Player]

        implicit val recipesPlugin: RecipesPlugin.type = RecipesPlugin
        implicit val api: JannyRecipesAPI = recipesPlugin
        implicit val guiFactory: RecipeGuiFactory[RecipesPlugin.type] = api.getGuiFactory[RecipesPlugin.type]()

        implicit val recipesMenu: RecipesMenu[RecipesPlugin.type] = GuiInventoryHolderListener.getLastOpenedGui(player) match {
            case Some(mainMenu: RecipesMenu[RecipesPlugin.type]) => mainMenu
            case _ => guiFactory.newRecipesMenu()
        }

        player.openInventory(new NewMenu[RecipesPlugin.type]().getInventory)

        true
    }
}
