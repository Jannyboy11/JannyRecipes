package xyz.janboerman.recipes.api.recipe

import org.bukkit.inventory.ItemStack

object FurnaceIngredient {
    def unapply(arg: FurnaceIngredient): Option[ItemStack] = Some(arg.getItemStack())

    def apply(matcher: ItemStack): FurnaceIngredient = new SimpleFurnaceIngredient(matcher)
}

trait FurnaceIngredient extends Ingredient { outer =>

    def getItemStack(): ItemStack

    override def apply(itemStack: ItemStack): Boolean = {
        val matcher = getItemStack()
        if (matcher == null) itemStack == null
        else itemStack != null && matcher.getType == itemStack.getType
    }

    override def clone(): FurnaceIngredient = {
        val cloneStack = getItemStack().clone()
        new FurnaceIngredient {
            override def getItemStack(): ItemStack = cloneStack
        }
    }

}

class SimpleFurnaceIngredient(matcher: ItemStack) extends FurnaceIngredient {
    override def getItemStack(): ItemStack = if (matcher == null) null else matcher.clone()
    override def clone(): SimpleFurnaceIngredient = new SimpleFurnaceIngredient(if (matcher == null) null else matcher.clone)
}