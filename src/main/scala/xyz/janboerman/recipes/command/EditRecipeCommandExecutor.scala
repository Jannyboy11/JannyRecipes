package xyz.janboerman.recipes.command

import org.bukkit.ChatColor
import org.bukkit.command.{Command, CommandExecutor, CommandSender}
import org.bukkit.entity.Player
import xyz.janboerman.recipes.RecipesPlugin
import xyz.janboerman.recipes.api.JannyRecipesAPI
import xyz.janboerman.recipes.api.gui.RecipeGuiFactory

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
        mainMenu.setSearch(itemInMainHand.getType.name())
        player.openInventory(mainMenu.getInventory)

        true
    }
}
