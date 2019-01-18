package xyz.janboerman.recipes.v1_13_R2.recipe

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
            case brc: BetterRecipeChoice => brc.clone()
            case mat: MaterialChoice => mat.clone()
            case exact: ExactChoice => exact.clone()
            case x => x //cannot call x.clone() because scalac is dumb. it thinks interface methods can be package-protected. lol.
        }
    }
}
