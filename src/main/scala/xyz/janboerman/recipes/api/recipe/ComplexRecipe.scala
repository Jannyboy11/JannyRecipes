package xyz.janboerman.recipes.api.recipe

import org.bukkit.configuration.serialization.ConfigurationSerializable

trait ComplexRecipe extends CraftingRecipe {
    override def isHidden(): Boolean = true
}
//TODO implement FixedResult and FixedIngredients
trait ArmorDyeRecipe extends ComplexRecipe with ConfigurationSerializable
trait BannerAddPatternRecipe extends ComplexRecipe with ConfigurationSerializable
trait BannerDuplicateRecipe extends ComplexRecipe with ConfigurationSerializable
trait BookCloneRecipe extends ComplexRecipe with ConfigurationSerializable
trait FireworkRocketRecipe extends ComplexRecipe with ConfigurationSerializable
trait FireworkStarFadeRecipe extends ComplexRecipe with ConfigurationSerializable
trait FireworkStarRecipe extends ComplexRecipe with ConfigurationSerializable
trait MapCloneRecipe extends ComplexRecipe with ConfigurationSerializable
trait MapExtendRecipe extends ShapedRecipe with ComplexRecipe with ConfigurationSerializable
trait RepairItemRecipe extends ComplexRecipe with ConfigurationSerializable
trait ShieldDecorationRecipe extends ComplexRecipe with ConfigurationSerializable
trait ShulkerBoxColorRecipe extends ComplexRecipe with ConfigurationSerializable
trait TippedArrowRecipe extends ComplexRecipe with ConfigurationSerializable
