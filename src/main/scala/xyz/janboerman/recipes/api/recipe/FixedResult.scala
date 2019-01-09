package xyz.janboerman.recipes.api.recipe

import org.bukkit.Material
import org.bukkit.inventory.ItemStack

trait FixedResult {

    def getResultStack(): ItemStack
    def hasResultType(material: Material): Boolean = {
        val resultStack = getResultStack()
        if (resultStack == null) return false
        resultStack.getType `eq` material
    }

}
