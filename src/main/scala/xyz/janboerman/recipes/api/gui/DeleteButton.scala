package xyz.janboerman.recipes.api.gui

import org.bukkit.{ChatColor, Material}
import org.bukkit.configuration.serialization.ConfigurationSerializable
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.inventory.{Inventory, ItemStack}
import org.bukkit.plugin.Plugin
import org.bukkit.scheduler.BukkitTask
import xyz.janboerman.guilib.api.ItemBuilder
import xyz.janboerman.guilib.api.menu.{RedirectItemButton, YesNoMenu}
import xyz.janboerman.recipes.api.JannyRecipesAPI
import xyz.janboerman.recipes.api.recipe.Recipe

class DeleteButton[P <: Plugin, R <: Recipe, RE <: RecipeEditor[P, R]]
    (icon: ItemStack, redirect: () => Inventory)
    (implicit api: JannyRecipesAPI, recipesMenu: RecipesMenu[P])

    extends RedirectItemButton[RE](icon, (recipeEditor: RE, event: InventoryClickEvent) =>
        new YesNoMenu[P](recipeEditor.getPlugin, yesClick => {
            recipeEditor.getPlugin.getServer.getScheduler
                .runTask(recipeEditor.getPlugin, (_: BukkitTask) => {
                    yesClick.getView.close()
                    val player = yesClick.getWhoClicked

                    val deleted = recipeEditor.getCachedRecipe() match {
                        case Some(recipe: R with ConfigurationSerializable) =>
                            api.persist().deleteRecipe(recipe) match {
                                case Right(_) =>
                                    if(api.removeRecipe(recipe)) {
                                        player.sendMessage(ChatColor.GREEN + "The recipe was deleted successfully.")
                                        true
                                    } else false
                                case Left(message) =>
                                    player.sendMessage(ChatColor.RED + message)
                                    false
                            }
                        case Some(recipe) =>
                            api.removeRecipe(recipe)
                            player.sendMessage(ChatColor.RED + "Could not delete the recipe from persistent storage.")
                            true
                        case None =>
                            player.sendMessage(ChatColor.RED + "No recipe present.")
                            false
                    }

                    if (deleted) {
                        recipesMenu.scheduleRefresh()
                        player.openInventory(recipesMenu.getInventory)
                    } else {
                        player.openInventory(recipeEditor.getInventory)
                    }
                })
        }, noClick => {
            recipeEditor.getPlugin.getServer.getScheduler
                .runTask(recipeEditor.getPlugin, (_: BukkitTask) => {
                    noClick.getView.close()
                    noClick.getWhoClicked.openInventory(recipeEditor.getInventory)
                })
        }).getInventory) {

    def this(redirect: () => Inventory)
            (implicit api: JannyRecipesAPI, recipesMenu: RecipesMenu[P]) =
        this(new ItemBuilder(Material.BARRIER)
                .name(interactable(Delete))
                .build(),
            redirect)

    def this(icon: ItemStack)
            (implicit api: JannyRecipesAPI, recipesMenu: RecipesMenu[P]) =
            this(icon, () => recipesMenu.getInventory())

    def this()(implicit api: JannyRecipesAPI, recipesMenu: RecipesMenu[P]) =
        this(new ItemBuilder(Material.BARRIER)
            .name(interactable(Delete))
            .build(), () => recipesMenu.getInventory())

}
