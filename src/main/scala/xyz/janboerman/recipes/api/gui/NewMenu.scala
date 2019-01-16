package xyz.janboerman.recipes.api.gui

import org.bukkit.event.inventory.InventoryOpenEvent
import org.bukkit.inventory.ItemFlag
import org.bukkit.plugin.Plugin
import xyz.janboerman.guilib.api.ItemBuilder
import xyz.janboerman.guilib.api.menu.{MenuHolder, RedirectItemButton}
import xyz.janboerman.recipes.api.recipe.RecipeType

object NewMenu {
    private def getInventorySize(): Int = {
        val typeOfRecipes = RecipeType.Values.size
        val rest = typeOfRecipes % 9
        val dividedByNine = typeOfRecipes / 9

        if (typeOfRecipes > 54) {
            54 //TODO think about paging later.
        } else {
            dividedByNine * 9 + (if (rest == 0) 0 else 9)
        }
    }
}
import NewMenu._

class NewMenu[P <: Plugin](implicit plugin: P,
                           implicit val mainMenu: RecipesMenu[P],
                           implicit val recipeGuiFactory: RecipeGuiFactory[P])
    extends MenuHolder[P](plugin, getInventorySize(), "Select a recipe type") {

    override def onOpen(event: InventoryOpenEvent): Unit = {
        for ((recipeType, i) <- RecipeType.Values.zipWithIndex.take(getInventory.getSize)) {

            val button = new RedirectItemButton[NewMenu[P]](new ItemBuilder(recipeType.getIcon)
                .name(recipeType.getName)
                .flags(ItemFlag.values(): _*)
                .build(), () => recipeGuiFactory.newRecipeEditor(recipeType).getInventory)

            setButton(i, button)
        }
    }

}
