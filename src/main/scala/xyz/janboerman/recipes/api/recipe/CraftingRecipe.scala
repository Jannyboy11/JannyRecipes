package xyz.janboerman.recipes.api.recipe

import org.bukkit.{Keyed, World}
import org.bukkit.inventory.{CraftingInventory, ItemStack}

/**
  * models a crafting recipe as a function: itemgrid -> (result, remainders)
  */
trait CraftingRecipe extends Recipe with Keyed {

    def tryCraft(craftingInventory: CraftingInventory, world: World): Option[CraftingResult]

}

trait Grouped {
    protected var group: String = null

    def getGroup(): Option[String] = Option(group).filter(_.nonEmpty)

}