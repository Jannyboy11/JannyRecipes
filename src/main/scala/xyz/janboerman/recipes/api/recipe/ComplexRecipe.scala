package xyz.janboerman.recipes.api.recipe

trait ComplexRecipe extends CraftingRecipe {
    override def isHidden(): Boolean = true
}
trait ArmorDyeRecipe extends ComplexRecipe
trait BannerAddPatternRecipe extends ComplexRecipe
trait BannerDuplicateRecipe extends ComplexRecipe
trait BookCloneRecipe extends ComplexRecipe
trait FireworkRocketRecipe extends ComplexRecipe
trait FireworkStarFadeRecipe extends ComplexRecipe
trait FireworkStarRecipe extends ComplexRecipe
trait MapCloneRecipe extends ComplexRecipe
trait MapExtendRecipe extends ShapedRecipe with ComplexRecipe
trait RepairItemRecipe extends ComplexRecipe
trait ShieldDecorationRecipe extends ComplexRecipe
trait ShulkerBoxColorRecipe extends ComplexRecipe
trait TippedArrowRecipe extends ComplexRecipe
