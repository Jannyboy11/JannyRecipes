package com.janboerman.recipes.api.recipe

import org.bukkit.Material
import org.bukkit.inventory.ItemStack

trait FixedResult {

    def getResultStack(): ItemStack
    def getResultMaterial(): Material = {
        val resultStack = getResultStack()
        if (resultStack == null) null else resultStack.getType
    }
    def hasResultType(material: Material): Boolean = getResultMaterial() `eq` material

}
