package com.janboerman.recipes.command

import org.bukkit.{ChatColor, Keyed}
import org.bukkit.command.{Command, CommandExecutor, CommandSender}
import com.janboerman.recipes.RecipesPlugin
import com.janboerman.recipes.RecipesPlugin.{addRecipe, getLogger}

object ReloadRecipesCommandExecutor extends CommandExecutor {
    override def onCommand(sender: CommandSender, command: Command, label: String, args: Array[String]): Boolean = {
        RecipesPlugin.persist().loadRecipes() match {
            case Right(iterator) =>
                var successes = 0
                var errors = 0
                for (recipe <- iterator) {
                    val successfullyAdded = addRecipe(recipe)
                    if (!successfullyAdded) {
                        getLogger.warning("Could not register recipe: " + recipe)
                        if (recipe.isInstanceOf[Keyed]) {
                            val key = recipe.asInstanceOf[Keyed].getKey
                            getLogger.warning("It's key is: " + key + ". Is that key already registered?")
                        }
                        errors += 1
                    } else {
                        successes += 1
                    }
                }
                if (successes > 0) getLogger.info(s"${ChatColor.GREEN} Loaded $successes recipes.")
                if (errors > 0) getLogger.severe(s"${ChatColor.RED} $errors recipes failed to load.")

            case Left(errorMessage) => sender.sendMessage(ChatColor.RED + errorMessage)
        }

        true
    }
}
