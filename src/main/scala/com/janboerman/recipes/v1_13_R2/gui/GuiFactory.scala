package com.janboerman.recipes.v1_13_R2.gui

import com.janboerman.recipes.RecipesPlugin
import com.janboerman.recipes.api.gui.{RecipeEditor, RecipeGuiFactory, RecipesMenu}
import com.janboerman.recipes.api.recipe.{ArmorDyeRecipe, ArmorDyeType, BannerAddPatternRecipe, BannerAddPatternType, BannerDuplicateRecipe, BannerDuplicateType, BookCloneRecipe, BookCloneType, FireworkRocketRecipe, FireworkRocketType, FireworkStarFadeRecipe, FireworkStarFadeType, FireworkStarRecipe, FireworkStarType, FurnaceRecipe, FurnaceType, MapCloneRecipe, MapCloneType, MapExtendRecipe, MapExtendType, Recipe, RecipeType, RepairItemRecipe, RepairItemType, ShapedRecipe, ShapedType, ShapelessRecipe, ShapelessType, ShieldDecorationRecipe, ShieldDecorationType, ShulkerBoxColorRecipe, ShulkerBoxColorType, TippedArrowRecipe, TippedArrowType}
import com.janboerman.recipes.api.recipe.modify.{ModifiedRecipeType, RecipeModifier}
import com.janboerman.recipes.RecipesPlugin
import com.janboerman.recipes.api.gui.{RecipeEditor, RecipeGuiFactory, RecipesMenu, SaveButton, UnknownRecipeEditor}
import com.janboerman.recipes.api.recipe._
import com.janboerman.recipes.api.recipe.modify.{ModifiedRecipe, ModifiedRecipeType, RecipeModifier}
import com.janboerman.recipes.v1_13_R2.recipe.janny.{JannyArmorDye, JannyBannerAddPattern, JannyBannerDuplicate, JannyBookClone, JannyFireworkRocket, JannyFireworkStar, JannyFireworkStarFade, JannyMapClone, JannyMapExtend, JannyRepairItem, JannyShieldDecoration, JannyShulkerBoxColor, JannyTippedArrow}


object GuiFactory extends RecipeGuiFactory[RecipesPlugin.type] {
    private implicit val plugin: RecipesPlugin.type = RecipesPlugin
    private type P = RecipesPlugin.type

    override def newRecipesMenu(): RecipesMenu[P] = new RecipesMenu[P]()

    private def newRecipeEditor(recipe: Recipe, modifiers: List[RecipeModifier[_, _]])
                               (implicit mainMenu: RecipesMenu[P]): RecipeEditor[P, _] = {

        recipe match {
            case ModifiedRecipe(base, modifier) =>
                newRecipeEditor(base, modifier :: modifiers)

            case armorDye: ArmorDyeRecipe =>                    ArmorDyeRecipeEditor(armorDye)
            case bannerAddPattern: BannerAddPatternRecipe =>    BannerAddPatternRecipeEditor(bannerAddPattern)
            case bannerDuplicate: BannerDuplicateRecipe =>      BannerDuplicateRecipeEditor(bannerDuplicate)
            case bookClone: BookCloneRecipe =>                  BookCloneRecipeEditor(bookClone)
            case fireworkRocket: FireworkRocketRecipe =>        FireworkRocketRecipeEditor(fireworkRocket)
            case fireworkStarFade: FireworkStarFadeRecipe =>    FireworkStarFadeRecipeEditor(fireworkStarFade)
            case fireworkStar: FireworkStarRecipe =>            FireworkStarRecipeEditor(fireworkStar)
            case mapClone: MapCloneRecipe =>                    MapCloneRecipeEditor(mapClone)
            case mapExtend: MapExtendRecipe =>                  MapExtendRecipeEditor(mapExtend)
            case repairItem: RepairItemRecipe =>                RepairItemRecipeEditor(repairItem)
            case shieldDecoration: ShieldDecorationRecipe =>    ShieldDecorationRecipeEditor(shieldDecoration)
            case shulkerBoxColor: ShulkerBoxColorRecipe =>      ShulkerBoxColorRecipeEditor(shulkerBoxColor)
            case tippedArrow: TippedArrowRecipe =>              TippedArrowRecipeEditor(tippedArrow)

            case shaped: ShapedRecipe =>        ShapedRecipeEditor(shaped)
            case shapeless: ShapelessRecipe =>  ShapelessRecipeEditor(shapeless)
            case furnace: FurnaceRecipe =>      FurnaceRecipeEditor(furnace)

            case _ => new UnknownRecipeEditor()
        }
    }

    override def newRecipeEditor(recipe: Recipe)
                                (implicit recipesMenu: RecipesMenu[P]): RecipeEditor[P, _] =
        newRecipeEditor(recipe, Nil)

    //TODO add implicit parameter IsCreatable[RecipeType] or something? so that this method can't ever be called with ModifiedRecipeType.
    override def newRecipeEditor(recipeType: RecipeType)
                                (implicit mainMenu: RecipesMenu[P]): RecipeEditor[P, _] = {

        //TODO custom recipes types (although that should not be necessary given there are modifiers..)

        recipeType match {
            case mt: ModifiedRecipeType =>
                //this is not realistic, because a modified recipe is never new!
                //it is always created from a `base` recipe
                throw new RuntimeException("Cannot create an editor for a new modified recipe, because modified recipes are never new!")

            case ArmorDyeType =>
                val editor = ArmorDyeRecipeEditor(JannyArmorDye())
                editor.setButton(45, new SaveButton(() => mainMenu.getInventory))
                editor
            case BannerAddPatternType =>
                val editor = BannerAddPatternRecipeEditor(JannyBannerAddPattern())
                editor.setButton(45, new SaveButton(() => mainMenu.getInventory))
                editor
            case BannerDuplicateType =>
                val editor = BannerDuplicateRecipeEditor(JannyBannerDuplicate())
                editor.setButton(45, new SaveButton(() => mainMenu.getInventory))
                editor
            case BookCloneType =>
                val editor = BookCloneRecipeEditor(JannyBookClone())
                editor.setButton(45, new SaveButton(() => mainMenu.getInventory))
                editor
            case FireworkRocketType =>
                val editor = FireworkRocketRecipeEditor(JannyFireworkRocket())
                editor.setButton(45, new SaveButton(() => mainMenu.getInventory))
                editor
            case FireworkStarType =>
                val editor = FireworkStarRecipeEditor(JannyFireworkStar())
                editor.setButton(45, new SaveButton(() => mainMenu.getInventory))
                editor
            case FireworkStarFadeType =>
                val editor = FireworkStarFadeRecipeEditor(JannyFireworkStarFade())
                editor.setButton(45, new SaveButton(() => mainMenu.getInventory))
                editor
            case MapCloneType =>
                val editor = MapCloneRecipeEditor(JannyMapClone())
                editor.setButton(45, new SaveButton(() => mainMenu.getInventory))
                editor
            case MapExtendType =>
                val editor = MapExtendRecipeEditor(JannyMapExtend())
                editor.setButton(45, new SaveButton(() => mainMenu.getInventory))
                editor
            case RepairItemType =>
                val editor = RepairItemRecipeEditor(JannyRepairItem())
                editor.setButton(45, new SaveButton(() => mainMenu.getInventory))
                editor
            case ShieldDecorationType =>
                val editor = ShieldDecorationRecipeEditor(JannyShieldDecoration())
                editor.setButton(45, new SaveButton(() => mainMenu.getInventory))
                editor
            case ShulkerBoxColorType =>
                val editor = ShulkerBoxColorRecipeEditor(JannyShulkerBoxColor())
                editor.setButton(45, new SaveButton(() => mainMenu.getInventory))
                editor
            case TippedArrowType =>
                val editor = TippedArrowRecipeEditor(JannyTippedArrow())
                editor.setButton(45, new SaveButton(() => mainMenu.getInventory))
                editor

            case ShapedType => ShapedRecipeEditor(null)
            case ShapelessType => ShapelessRecipeEditor(null)
            case FurnaceType => FurnaceRecipeEditor(null)

            case _ => new UnknownRecipeEditor()
        }
    }
}
