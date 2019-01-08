package xyz.janboerman.recipes.v1_13_R2.recipe.wrapper

import net.minecraft.server.v1_13_R2.ItemStack
import xyz.janboerman.recipes.v1_13_R2.Conversions.{toBukkitStack, toNMSStack}
import xyz.janboerman.recipes.v1_13_R2.recipe.{CraftingIngredient, FurnaceIngredient}

case class JannyToNMSCraftingIngredient(janny: xyz.janboerman.recipes.api.recipe.CraftingIngredient) extends CraftingIngredient {
    override def choices: List[ItemStack] = janny.getChoices().map(toNMSStack)
    override def apply(nmsStack: ItemStack): Boolean = janny.apply(toBukkitStack(nmsStack))
    override def getRemainingItemStack(ingredientStack: ItemStack): ItemStack = {
        val jannyRemaining = janny.getRemainingStack(toBukkitStack(ingredientStack))
        if (jannyRemaining.isDefined) {
            ingredientStack.setCount(0)
            toNMSStack(jannyRemaining.get)
        } else {
            ingredientStack
        }
    }
}

case class JannyToNMSFurnaceIngredient(janny: xyz.janboerman.recipes.api.recipe.FurnaceIngredient) extends FurnaceIngredient {
    override def apply(nmsStack: ItemStack): Boolean = janny.apply(toBukkitStack(nmsStack))
    override def itemStack: ItemStack = toNMSStack(janny.getItemStack())
}
