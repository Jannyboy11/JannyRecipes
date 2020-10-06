package com.janboerman.recipes.api.gui

import com.janboerman.recipes.RecipesPlugin
import org.bukkit.event.inventory.InventoryOpenEvent
import org.bukkit.plugin.Plugin
import xyz.janboerman.guilib.api.menu.MenuHolder
import com.janboerman.recipes.api.recipe.Recipe
import com.janboerman.recipes.api.recipe.modify.ModifierType

import scala.collection.mutable

class ModifiersMenu[P <: Plugin, R <: Recipe, RE <: RecipeEditor[P, R]](recipe: R, recipeEditor: RE)(implicit plugin: P)
    extends MenuHolder[P](plugin, 54, "Select Modifiers") {

    private val activeModifiers = mutable.HashMap[Int, Boolean]();

    private var firstTimeOpen = true

    def getRecipe(): R = recipe

    override def onOpen(event: InventoryOpenEvent): Unit = {
        if (firstTimeOpen) {
            setupButtons()
            firstTimeOpen = false
        }
    }

    private def setupButtons(): Unit = {
        var buttonSlot = 0
        for (modifierType <- ModifierType.values if modifierType.appliesTo(recipe) && buttonSlot < 45 /*TODO worry about paging later*/) {
            //assume modifiertype works for R since we tested modifierType.appliesTo(recipe)
            val button = modifierType.asInstanceOf[ModifierType[_, R, _]].newButton(recipe)
            setButton(buttonSlot, button)
            buttonSlot += 1
        }

        import recipeEditor.{recipesMenu, api}

        //Save & Exit Button.
        setButton(45, new SaveButton(() => recipeEditor.getInventory()))
        //TODO in the RecipeEditor, keep a list of active modifiers. (also populated when the editor is opened for the first time)
        //TODO the save button in this inventory then sets the modifiers list in the recipe editor
        //TODO the recipe editor serializes applies the modifiers when the recipe is saved (they are also serialized!)
    }

}
