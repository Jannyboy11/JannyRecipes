package xyz.janboerman.recipes.api.gui

import org.bukkit.plugin.Plugin
import xyz.janboerman.recipes.api.recipe.{Recipe, RecipeType}

trait RecipeGuiFactory[P <: Plugin] {

    def newRecipesMenu(): RecipesMenu[P]

    def newRecipeEditor(recipe: Recipe)(implicit mainMenu: RecipesMenu[P]): RecipeEditor[P, _]

    def newRecipeEditor(recipeType: RecipeType)(implicit mainMenu: RecipesMenu[P]): RecipeEditor[P, _]


    //TODO add a method to register custom editors for custom recipe types for other plugins to use :)
    //TODO make this an abstract class which can be extended by server-version-specific implementations


}
