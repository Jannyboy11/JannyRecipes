package xyz.janboerman.recipes.command

import org.bukkit.ChatColor
import org.bukkit.command.{Command, CommandExecutor, CommandSender}
import xyz.janboerman.recipes.RecipesPlugin

object ReloadRecipesExecutor extends CommandExecutor {
    override def onCommand(sender: CommandSender, command: Command, label: String, args: Array[String]): Boolean = {
        RecipesPlugin.persist().loadRecipes() match {
            case Right(iterator) =>
                var allSuccess = true
                for (recipe <- iterator) {
                    allSuccess |= RecipesPlugin.addRecipe(recipe)
                }

                if (allSuccess) {
                    sender.sendMessage(ChatColor.GREEN + "Reloaded custom recipes!")
                } else {
                    //TODO HOW THE FUCK IS THIS BRANCH TAKEN WHEN WE DON'T EVEN HAVE RECIPES IN THE CONFIGS?!
                    sender.sendMessage(ChatColor.RED + "Something went wrong when loading recipes. Please check your console for errors.")
                }
            case Left(errorMessage) =>
                sender.sendMessage(ChatColor.RED + errorMessage)
        }

        true
    }
}
