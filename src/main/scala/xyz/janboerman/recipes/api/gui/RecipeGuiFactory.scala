package xyz.janboerman.recipes.api.gui

import xyz.janboerman.recipes.api.recipe.Recipe

abstract class RecipeGuiFactory {

    def newRecipesMenu(): RecipesMenu

    def newRecipeEditor(recipe: Recipe, mainMenu: RecipesMenu): RecipeEditor[_]

    //TODO def newYesNoMenu(yesAction, noAction)



    //TODO add a method to register custom editors for custom recipe types for other plugins to use :)
    //TODO make this an abstract class which can be extended by server-version-specific implementations


}
