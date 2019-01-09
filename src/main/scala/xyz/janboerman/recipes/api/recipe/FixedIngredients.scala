package xyz.janboerman.recipes.api.recipe

import org.bukkit.Material
import org.bukkit.inventory.ItemStack

trait FixedIngredients {

    def getIngredientStacks(): Iterable[ItemStack]
    def hasIngredient(itemStack: ItemStack): Boolean = getIngredientStacks.exists(_ == itemStack)
    def hasIngredient(material: Material): Boolean = getIngredientStacks.exists(is => if (is == null) false else is.getType `eq` material)

}
