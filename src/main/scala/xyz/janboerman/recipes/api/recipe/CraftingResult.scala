package xyz.janboerman.recipes.api.recipe

import org.bukkit.inventory.ItemStack

case class CraftingResult(private val resultStack: ItemStack, private val remainingStacks: List[Option[_ <: ItemStack]])
    extends Product2[ItemStack, List[Option[_ <: ItemStack]]] {

    def getResultStack(): ItemStack = resultStack
    def getRemainders(): List[Option[_ <: ItemStack]] = remainingStacks

    final override def _1: ItemStack = getResultStack()
    final override def _2: List[Option[_ <: ItemStack]] = getRemainders()
}
