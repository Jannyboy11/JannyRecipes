package xyz.janboerman.recipes.api.recipe

import org.bukkit.Material
import org.bukkit.inventory.ItemStack

import scala.collection.mutable

object RecipeType {
    val Values = new mutable.HashSet[RecipeType]()
    Values.add(ShapedType)
    Values.add(ShapelessType)
    Values.add(FurnaceType)
}

trait RecipeType {
    def getName(): String
    def getIcon(): ItemStack
}

object ShapedType extends RecipeType {
    override def getName() = "Shaped"
    override def getIcon() = new ItemStack(Material.CRAFTING_TABLE)
}
object ShapelessType extends RecipeType {
    override def getName() = "Shapeless"
    override def getIcon() = new ItemStack(Material.CRAFTING_TABLE)
}
object FurnaceType extends RecipeType {
    override def getName() = "Furnace"
    override def getIcon(): ItemStack = new ItemStack(Material.FURNACE)
}
//TODO complex recipes