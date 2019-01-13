package xyz.janboerman.recipes.v1_13_R2.gui

import xyz.janboerman.recipes.RecipesPlugin
import xyz.janboerman.recipes.api.recipe.{FurnaceRecipe, Recipe, ShapedRecipe, ShapelessRecipe}
import xyz.janboerman.recipes.api.gui.{RecipeEditor, RecipeGuiFactory, RecipesMenu, UnknownRecipeEditor}
import xyz.janboerman.recipes.v1_13_R2.Impl

object GuiFactory extends RecipeGuiFactory {
    override def newRecipesMenu(): RecipesMenu = new RecipesMenu(Impl, RecipesPlugin)

    override def newRecipeEditor(recipe: Recipe, mainMenu: RecipesMenu): RecipeEditor[_] = {
        //TODO custom recipe types

        recipe match {
            //TODO modified recipes
            //TODO complex recipes

            case shaped: ShapedRecipe => ShapedRecipeEditor(mainMenu, shaped)
            case shapeless: ShapelessRecipe => ShapelessRecipeEditor(mainMenu, shapeless)
            case furnace: FurnaceRecipe => FurnaceRecipeEditor(mainMenu, furnace)

            case _ => new UnknownRecipeEditor(recipe, mainMenu, RecipesPlugin, RecipesPlugin)
        }

    }

}
