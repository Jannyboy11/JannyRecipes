package com.janboerman.recipes.api.gui

import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.plugin.Plugin
import xyz.janboerman.guilib.api.menu.ToggleButton
import com.janboerman.recipes.api.recipe.Recipe
import com.janboerman.recipes.api.recipe.modify.RecipeModifier

class ModifierButton[P <: Plugin, R <: Recipe, RE <: RecipeEditor[P, R], MM <: ModifiersMenu[P, R, RE]]
    (icon: ItemStack, recipeModifier: RecipeModifier[R, _], recipeEditor: RE)
    extends ToggleButton[MM](icon) {

    override def afterToggle(menuHolder: MM, event: InventoryClickEvent): Unit = {
        if (isEnabled) {
            recipeEditor.addModifier(recipeModifier)
        } else {
            recipeEditor.removeModifier(recipeModifier)
        }
    }

}
