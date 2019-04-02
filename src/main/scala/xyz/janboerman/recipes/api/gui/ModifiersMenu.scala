package xyz.janboerman.recipes.api.gui

import org.bukkit.event.inventory.InventoryOpenEvent
import org.bukkit.plugin.Plugin
import xyz.janboerman.guilib.api.menu.MenuHolder
import xyz.janboerman.recipes.api.recipe.Recipe
import xyz.janboerman.recipes.api.recipe.modify.ModifierType

class ModifiersMenu[P <: Plugin, R <: Recipe, RE <: RecipeEditor[P, R]](recipe: R, recipeEditor: RE)(implicit plugin: P)
    extends MenuHolder[P](plugin, 54, "Select Modifiers") {

    var firstTimeOpen = true

    override def onOpen(event: InventoryOpenEvent): Unit = {
        if (firstTimeOpen) {
            setupButtons()
            firstTimeOpen = false
        }
    }

    private def setupButtons(): Unit = {
        var buttonSlot = 0
        for (modifierType <- ModifierType.values if modifierType.appliesTo(recipe) && buttonSlot < 45 /*TODO worry about paging later*/) {
            val button = modifierType.newButton(recipe)
            setButton(buttonSlot, button)
            buttonSlot += 1
        }

        //Save & Exit Button.
        //TODO in the RecipeEditor, keep a list of active modifiers. (also populated when the editor is opened for the first time)
        //TODO the save button in this inventory then sets the modifiers list in the recipe editor
        //TODO the recipe editor serializes applies the modifiers when the recipe is saved (they are also serialized!)
    }



}
