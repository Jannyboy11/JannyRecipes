package xyz.janboerman.recipes.api.gui

import org.bukkit.Material
import org.bukkit.configuration.serialization.ConfigurationSerializable
import org.bukkit.inventory.{Inventory, ItemStack}
import org.bukkit.plugin.Plugin
import xyz.janboerman.guilib.api.ItemBuilder
import xyz.janboerman.guilib.api.menu.RedirectItemButton
import xyz.janboerman.recipes.api.persist.RecipeStorage
import xyz.janboerman.recipes.api.recipe.Recipe

class SaveButton[P <: Plugin, R <: Recipe, MH <: RecipeEditor[P, R]](icon: ItemStack, redirect: () => Inventory)(implicit recipeStorage: RecipeStorage)
    extends RedirectItemButton[MH](
    icon, () => redirect()) {

    def this(redirect: () => Inventory)(implicit recipeStorage: RecipeStorage) = this(new ItemBuilder(Material.LIME_CONCRETE)
        .name(interactable(SaveAndExit))
        .build(), redirect)

    def save(recipe: Recipe with ConfigurationSerializable): Either[String, Unit] = {
        if (recipe != null) {
            recipeStorage.saveRecipe(recipe)
        } else {
            Left("The inputs do not make a valid recipe.")
        }
    }



}
