package xyz.janboerman.recipes.api.gui

import org.bukkit.event.inventory.InventoryOpenEvent
import org.bukkit.plugin.Plugin
import xyz.janboerman.guilib.api.menu.MenuHolder
import xyz.janboerman.recipes.api.recipe.Recipe

class ModifiersMenu[P <: Plugin, R <: Recipe](plugin: P, recipeEditor: RecipeEditor[P, R])
    extends MenuHolder[P](plugin, 54, "Select Modifiers") {

    private var firstTimeOpen = true

    override def onOpen(event: InventoryOpenEvent): Unit = {
        if (firstTimeOpen) {
            resetButtons()

            firstTimeOpen = false
        }
    }

    def resetButtons(): Unit = {
        //init buttons based on what's already on the recipe or not.

        //TODO

    }



}
