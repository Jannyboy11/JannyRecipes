package com.janboerman.recipes.command

import com.janboerman.recipes.api.JannyRecipesAPI
import com.janboerman.recipes.api.gui.RecipeGuiFactory
import org.bukkit.ChatColor
import org.bukkit.command.{Command, CommandExecutor, CommandSender}
import org.bukkit.entity.Player
import com.janboerman.recipes.RecipesPlugin
import com.janboerman.recipes.api.JannyRecipesAPI
import com.janboerman.recipes.api.gui.{IngredientSearchProperty, RecipeGuiFactory, ResultSearchProperty}
import com.janboerman.recipes.api.recipe.{FixedIngredients, FixedResult}

object EditRecipeCommandExecutor extends CommandExecutor {

    override def onCommand(sender: CommandSender, command: Command, label: String, args: Array[String]): Boolean = {
        if (!sender.isInstanceOf[Player]) {
            sender.sendMessage("You can only use this command as a player.")
            return true
        }

        val player = sender.asInstanceOf[Player]
        val itemInMainHand = player.getInventory.getItemInMainHand
        if (itemInMainHand == null) {
            player.sendMessage(ChatColor.RED + "Please hold the item for which you want to edit the recipe in your main hand.")
            return true
        }

        implicit val recipesPlugin: RecipesPlugin.type = RecipesPlugin
        implicit val api: JannyRecipesAPI = recipesPlugin
        implicit val guiFactory: RecipeGuiFactory[RecipesPlugin.type] = api.getGuiFactory[RecipesPlugin.type]()

        val mainMenu = guiFactory.newRecipesMenu()
        mainMenu.addSearchFilter(ResultSearchProperty, {
            case recipe: FixedResult => recipe.hasResultType(itemInMainHand.getType)
            case _ => false
        })
        mainMenu.addSearchFilter(IngredientSearchProperty, {
            case recipe: FixedIngredients => recipe.hasIngredient(itemInMainHand.getType)
            case _ => false
        })
        player.openInventory(mainMenu.getInventory)

        true
    }
}
