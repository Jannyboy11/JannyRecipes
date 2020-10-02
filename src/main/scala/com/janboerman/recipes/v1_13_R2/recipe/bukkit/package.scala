package com.janboerman.recipes.v1_13_R2.recipe

import org.bukkit.inventory.RecipeChoice.{ExactChoice, MaterialChoice}
import org.bukkit.inventory.{Inventory, ItemStack, RecipeChoice}

package object bukkit {
    private[bukkit] def getItemStackFromInventory(x: Int, y: Int, invWidth: Int, invHeight: Int, inventory: Inventory): ItemStack = {
        if (x < 0 || x >= invWidth) return null
        if (y < 0 || y >= invHeight) return null

        val index = y * invWidth + x
        inventory.getItem(index)
    }

    private[bukkit] def cloneRecipeChoice(recipeChoice: RecipeChoice): RecipeChoice = {
        recipeChoice match {
            case null => null
                //unfortunately necessary because the scala compiler is so dumb it does not know interface methods with access modifiers are public!
                //special-case known implementations to avoid reflection
            case brc: BetterRecipeChoice => brc.clone()
            case mat: MaterialChoice => mat.clone()
            case exact: ExactChoice => exact.clone()

            case x =>
                //invoke clone reflectively to work around the scala compiler issue *sigh*
                val method = x.getClass.getMethod("clone")
                val y = method.invoke(x)
                y.asInstanceOf[RecipeChoice]
        }
    }
}
