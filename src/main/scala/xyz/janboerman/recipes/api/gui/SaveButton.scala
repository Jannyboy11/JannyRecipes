package xyz.janboerman.recipes.api.gui

import org.bukkit.{ChatColor, Material}
import org.bukkit.configuration.serialization.ConfigurationSerializable
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.inventory.{Inventory, ItemStack}
import org.bukkit.plugin.Plugin
import org.bukkit.scheduler.BukkitTask
import xyz.janboerman.guilib.api.ItemBuilder
import xyz.janboerman.guilib.api.menu.RedirectItemButton
import xyz.janboerman.recipes.api.JannyRecipesAPI
import xyz.janboerman.recipes.api.recipe.Recipe

class SaveButton[P <: Plugin, R <: Recipe, MH <: RecipeEditor[P, R]](icon: ItemStack, redirect: () => Inventory)(implicit api: JannyRecipesAPI, implicit val recipesMenu: RecipesMenu[P])
    extends RedirectItemButton[MH](
    icon, () => redirect()) {

    private implicit val recipeStorage = api.persist()

    def this(redirect: () => Inventory)(implicit api: JannyRecipesAPI, recipesMenu: RecipesMenu[P]) = this(new ItemBuilder(Material.LIME_CONCRETE)
        .name(interactable(SaveAndExit))
        .build(), redirect)

    def save(recipe: R with ConfigurationSerializable): Either[String, Unit] = {
        //TODO check whether the recipe is vanilla? if so, do something different?
        if (recipe != null) {
            recipeStorage.saveRecipe(recipe)
        } else {
            Left("The inputs do not make a valid recipe.")
        }
    }

    override def onClick(menuHolder: MH, event: InventoryClickEvent): Unit = {
        menuHolder.makeRecipe() match {
            case Some(recipe) => //TODO match on Some(recipe: R with ConfigurationSerializable) or something :P
                val player = event.getWhoClicked
                if (recipe.isInstanceOf[ConfigurationSerializable]) {
                    val r = recipe.asInstanceOf[R with ConfigurationSerializable]
                    val either = save(r)
                    if (either.isLeft) {
                        player.sendMessage(ChatColor.RED + either.swap.toOption.get)
                    } else {
                        api.addRecipe(recipe)
                        player.sendMessage(ChatColor.GREEN + "Saved successfully.") //TODO include the recipe type in this message?

                        recipesMenu.scheduleRefresh()

                        val plugin = menuHolder.getPlugin
                        plugin.getServer.getScheduler.runTask(plugin, (_: BukkitTask) => {
                            event.getView.close()
                            player.openInventory(redirect())
                        })
                    }
                } else {
                    player.sendMessage(ChatColor.RED + "This type of recipe cannot be saved.")
                }

            case None => event.getWhoClicked.sendMessage(ChatColor.RED + "The inputs do not make a valid recipe.")
        }

        //TODO return to the main menu.
    }

}
