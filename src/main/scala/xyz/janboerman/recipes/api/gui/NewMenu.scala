package xyz.janboerman.recipes.api.gui

import org.bukkit.event.inventory.InventoryOpenEvent
import org.bukkit.plugin.Plugin
import xyz.janboerman.guilib.api.menu.MenuHolder
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

class NewMenu[P <: Plugin](implicit plugin: P) extends MenuHolder[P](plugin, getInventorySize(), "Pick a type") {

    //TODO how to do this? just open the editor for the chosen recipe type?

    override def onOpen(event: InventoryOpenEvent): Unit = {
        for ((i, recipeType) <- RecipeType.Values.zipWithIndex.take(getInventory.getSize)) {

            //TODO set the buttons.

        }
    }

}
