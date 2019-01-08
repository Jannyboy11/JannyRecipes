package xyz.janboerman.recipes.api.gui

import org.bukkit.event.inventory.{InventoryClickEvent, InventoryOpenEvent}
import org.bukkit.inventory.ItemStack
import org.bukkit. Material
import xyz.janboerman.guilib.api.ItemBuilder
import xyz.janboerman.guilib.api.menu.{ItemButton, MenuHolder, RedirectItemButton}
import xyz.janboerman.recipes.RecipesPlugin
import xyz.janboerman.recipes.api.JannyRecipesAPI
import xyz.janboerman.recipes.api.recipe.Recipe

import scala.collection.mutable
import scala.collection.mutable.ArrayBuffer

object RecipesMenu {
    private def getIcon[T <: Recipe](recipe: T)(implicit guiShow: GuiShow[T]): ItemStack = {
        guiShow.getRepresentation(recipe)
    }

    val RecipesPerPage: Int = 5 * 9
}

class RecipesMenu(private val api: JannyRecipesAPI) extends MenuHolder(RecipesPlugin, 9 * 6, "Manage Recipes") { self =>
    import RecipesMenu._

    private val guiFactory = api.getGuiFactory()

    private val recipeFilters: mutable.Map[String, Recipe => Boolean] = new mutable.HashMap[String, Recipe => Boolean]()
    private var recipeIterator: Iterator[_ <: Recipe] = api.iterateRecipes()

    private var currentPageNumber = 0

    private val cachedRecipes: mutable.Buffer[Recipe] = new ArrayBuffer[Recipe]()
    private var lastReachedRecipe = -1

    private var firstTimeOpen = true

    override def onOpen(event: InventoryOpenEvent): Unit = {
        refresh()

        if (firstTimeOpen) {
            fillPage()
            firstTimeOpen = false
        }
    }

    def refresh(): Unit = {
        recipeIterator = api.iterateRecipes()
        cachedRecipes.clear()
        lastReachedRecipe = -1

        //TODO are the filters are white-list or black-list? should they work in conjunction or disjunction?
        //TODO I think they should be a white-list - and that a recipe is only shown when it passes all active filters (so conjunction)
        //if (recipeFilters.nonEmpty) recipeIterator = recipeIterator.filter(recipe => recipeFilters.values.exists(_.apply(recipe)))
    }

    def hasPreviousPage(): Boolean = currentPageNumber > 0

    def hasNextPage(): Boolean = recipeIterator.hasNext || currentPageNumber * RecipesPerPage < lastReachedRecipe - RecipesPerPage

    private def fillPage(): Unit = {
        var startIndex = currentPageNumber * RecipesPerPage

        //if the recipes for this page aren't 'loaded' yet, load them all unit here.
        while (lastReachedRecipe < startIndex + RecipesPerPage && recipeIterator.hasNext) {
            val recipe: Recipe = recipeIterator.next()
            cachedRecipes.addOne(recipe)
            lastReachedRecipe += 1
        }

        //if we still didn't reach the startIndex, well then.. just lower the startIndex and currentPageNumber :)
        if (startIndex > lastReachedRecipe) {
            startIndex = Math.max(0, lastReachedRecipe - RecipesPerPage)
            currentPageNumber = startIndex / RecipesPerPage
        }

        //set the buttons in the inventory
        var slotIndex = 0
        var recipeIndex = startIndex
        while (recipeIndex < startIndex + RecipesPerPage) {
            if (recipeIndex < cachedRecipes.size) {
                val recipe = cachedRecipes(recipeIndex)

                val recipeEditor = guiFactory.newRecipeEditor(recipe, this)
                setButton(slotIndex, new RedirectItemButton[RecipesMenu](recipeEditor.getIcon().getOrElse(RecipeEditor.UnknownRecipeIcon), () => recipeEditor.getInventory()))
            } else {
                unsetButton(slotIndex)
            }

            recipeIndex += 1
            slotIndex += 1
        }

        val previousNextBuilder = new ItemBuilder(Material.MAGENTA_GLAZED_TERRACOTTA)
        if (hasPreviousPage()) {
            setButton(RecipesPerPage, new ItemButton[RecipesMenu](previousNextBuilder.name("Previous page").build()) {
                override def onClick(holder: RecipesMenu, event: InventoryClickEvent): Unit = {
                    assert(holder == self, "Holder of the previous-page button is not the same holder of the opened inventory.")
                    holder.currentPageNumber -= 1
                    holder.fillPage()
                }
            })
        } else {
            unsetButton(RecipesPerPage)
        }
        if (hasNextPage()) {
            setButton(RecipesPerPage + 8, new ItemButton[RecipesMenu](previousNextBuilder.name("Next page").build()) {
                override def onClick(holder: RecipesMenu, event: InventoryClickEvent): Unit = {
                    assert(holder == self, "Holder of the next-page button is not the same holder of the opened inventory.")
                    holder.currentPageNumber += 1
                    holder.fillPage()
                }
            })
        } else {
            unsetButton(RecipesPerPage + 8)
        }
    }

}
