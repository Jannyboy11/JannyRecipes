package xyz.janboerman.recipes.api.gui

import org.bukkit.{ChatColor, Material}
import org.bukkit.event.inventory.{InventoryClickEvent, InventoryOpenEvent}
import org.bukkit.inventory.{Inventory, ItemStack}
import org.bukkit.plugin.Plugin
import xyz.janboerman.guilib.api.ItemBuilder
import xyz.janboerman.guilib.api.menu.MenuHolder
import xyz.janboerman.recipes.api.{JannyRecipesAPI, PlayerUtils}
import xyz.janboerman.recipes.api.recipe.{Recipe, ShapedRecipe}

object RecipeEditor {

    //TODO create some kind of Map<Class<? extends Recipe>, Supplier<Option<ItemStack, ? extends RecipeEditor>>> to which other plugins can register their custom recipe types
    //TODO I might be able to use the type-class pattern here? yes but no cause I also want an UnkownRecipeEditor that just supports the delete operation for unknown recipes.

    lazy val UnknownRecipeIcon: ItemStack = new ItemBuilder(Material.STRUCTURE_VOID).name(ChatColor.RED + "Unknown recipe").build()


}

abstract class RecipeEditor[P <: Plugin, R <: Recipe](protected var recipe: R,
                                                      private val inventory: Inventory)
                                                     (implicit protected val recipesMenu: RecipesMenu[P],
                                                      implicit protected val api: JannyRecipesAPI,
                                                      implicit protected val plugin: P)
    extends MenuHolder[P](plugin, inventory) {

    private var firstTimeOpen = true

    override def onOpen(event: InventoryOpenEvent): Unit = {
        if (firstTimeOpen) {
            layoutBorder()
            layoutRecipe()
            layoutButtons()

            firstTimeOpen = false
        }

        super.onOpen(event)
    }

    override def onClick(event: InventoryClickEvent): Unit = {
        event.setCancelled(false)
        super.onClick(event)

        PlayerUtils.updateInventory(event.getWhoClicked, getPlugin) //needed to update the client's bad shift-click predictions
    }

    def getCachedRecipe(): Option[R] = {
        Option(recipe)
    }

    protected def layoutBorder(): Unit
    protected def layoutRecipe(): Unit
    protected def layoutButtons(): Unit


    def makeRecipe(): Option[R] = {
        throw new NotImplementedError("The current editor (" + this + ") doesn't know how to make a new recipe from inventory contents (yet). it should override makeRecipe")
    }
    //TODO let ShapedEditor, FurnaceEditor override this method.


    //TODO does this belong here? it doesn't have to
//    def saveRecipe(): Boolean //TODO can i share some code here across implementations?
//    def deleteRecipe(): Boolean //TODO can i share some code here across implementations?

    def getIcon(): Option[ItemStack] = None
    //TODO can FixedResult be a type-class? ShapedRecipe, FurnaceRecipe and ShapelessRecipes can all have instances defined
    //TODO then maybe modified recipes can implement FixedResult if their base recipes implement it.

}
