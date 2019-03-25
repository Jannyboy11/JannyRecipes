package xyz.janboerman.recipes.v1_13_R2.gui

import xyz.janboerman.recipes.RecipesPlugin
import xyz.janboerman.recipes.api.gui.{RecipeEditor, RecipeGuiFactory, RecipesMenu, UnknownRecipeEditor}
import xyz.janboerman.recipes.api.recipe._


object GuiFactory extends RecipeGuiFactory[RecipesPlugin.type] {
    private implicit val plugin: RecipesPlugin.type = RecipesPlugin
    private type P = RecipesPlugin.type

    override def newRecipesMenu(): RecipesMenu[P] = new RecipesMenu[P]()

    override def newRecipeEditor(recipe: Recipe)
                                (implicit recipesMenu: RecipesMenu[P]): RecipeEditor[P, _] = {

        //TODO custom recipe types

        recipe match {
            //TODO modified recipe editors

            case armorDye: ArmorDyeRecipe => ArmorDyeRecipeEditor(armorDye)
            //TODO more complex recipe editors

            case shaped: ShapedRecipe => ShapedRecipeEditor(shaped)
            case shapeless: ShapelessRecipe => ShapelessRecipeEditor(shapeless)
            case furnace: FurnaceRecipe => FurnaceRecipeEditor(furnace)

            case _ => new UnknownRecipeEditor()
        }

    }

    //TODO add implicit parameter IsCreatable[R] or something.
    override def newRecipeEditor(recipeType: RecipeType)(implicit mainMenu: RecipesMenu[P]): RecipeEditor[P, _] = {
        //TODO custom recipes types

        recipeType match {
            //TODO modified type
            //TODO complex types

            case ShapedType => ShapedRecipeEditor(null)
            case ShapelessType => ShapelessRecipeEditor(null)
            case FurnaceType => FurnaceRecipeEditor(null)

            case _ => new UnknownRecipeEditor()
        }
    }
}
