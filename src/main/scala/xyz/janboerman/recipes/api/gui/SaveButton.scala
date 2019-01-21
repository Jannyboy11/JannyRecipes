package xyz.janboerman.recipes.api.gui

import org.bukkit.{ChatColor, Keyed, Material}
import org.bukkit.configuration.serialization.ConfigurationSerializable
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.inventory.{Inventory, ItemStack}
import org.bukkit.plugin.Plugin
import org.bukkit.scheduler.BukkitTask
import xyz.janboerman.guilib.api.ItemBuilder
import xyz.janboerman.guilib.api.menu.RedirectItemButton
import xyz.janboerman.recipes.api.JannyRecipesAPI
import xyz.janboerman.recipes.api.recipe.Recipe

class SaveButton[P <: Plugin, R <: Recipe, RE <: RecipeEditor[P, R]](icon: ItemStack, redirect: () => Inventory)(implicit api: JannyRecipesAPI, implicit val recipesMenu: RecipesMenu[P])
    extends RedirectItemButton[RE](
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

    override def onClick(recipeEditor: RE, event: InventoryClickEvent): Unit = {
        recipeEditor.makeRecipe() match {
            case Some(recipe) => //TODO match on Some(recipe: R with ConfigurationSerializable) or something?
                val player = event.getWhoClicked

                val cachedRecipe: Recipe = recipeEditor.getCachedRecipe() match {
                    case Some(r) => r
                    case None => null
                }

                //if the cached recipe has the same key as the current recipe, then delete it.
                if (cachedRecipe.isInstanceOf[Keyed] && recipe.isInstanceOf[Keyed]) {
                    val cachedKeyedRecipe = cachedRecipe.asInstanceOf[Recipe with Keyed]
                    val newKeyedRecipe = recipe.asInstanceOf[Recipe with Keyed]

                    if (cachedKeyedRecipe.getKey == newKeyedRecipe.getKey) {
                        //remove from cache.
                        api.removeRecipe(cachedKeyedRecipe)
                    }
                }

                if (api.addRecipe(recipe)) {
                    if (recipe.isInstanceOf[ConfigurationSerializable]) {
                        val r = recipe.asInstanceOf[R with ConfigurationSerializable]

                        val either = save(r)
                        if (either.isLeft) {
                            player.sendMessage(ChatColor.RED + either.swap.toOption.get)
                        } else {
                            if (r.isInstanceOf[Keyed] && cachedRecipe.isInstanceOf[ConfigurationSerializable with Keyed]) {
                                //if the keys are NOT equal, remove the old recipe from persistent storage
                                val keyedRecipe = r.asInstanceOf[R with ConfigurationSerializable with Keyed]
                                val cachedKeyedRecipe = cachedRecipe.asInstanceOf[Recipe with ConfigurationSerializable with Keyed]
                                if (keyedRecipe.getKey != cachedKeyedRecipe.getKey) {
                                    recipeStorage.deleteRecipe(cachedKeyedRecipe) match {
                                        case Right(()) => player.sendMessage(ChatColor.GREEN + "Replaced successfully.")
                                        case Left(couldNotDeleteOldRecipeMessage) => player.sendMessage(ChatColor.RED + couldNotDeleteOldRecipeMessage)
                                    }
                                }
                            } else {
                                player.sendMessage(ChatColor.GREEN + "Saved successfully.")
                            }
                        }
                    } else {
                        player.sendMessage(ChatColor.RED + "This type of recipe cannot be saved.")
                    }
                    recipesMenu.scheduleRefresh()
                    val plugin = recipeEditor.getPlugin
                    plugin.getServer.getScheduler.runTask(plugin, (_: BukkitTask) => {
                        event.getView.close()
                        player.openInventory(redirect())
                    })
                } else {
                    player.sendMessage(ChatColor.RED + "Couldn't add the recipe. Is its key already in use?")
                }

            case None => event.getWhoClicked.sendMessage(ChatColor.RED + "The inputs do not make a valid recipe.")
        }
    }

}
