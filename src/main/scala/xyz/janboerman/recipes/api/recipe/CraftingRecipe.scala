package xyz.janboerman.recipes.api.recipe

import org.bukkit.{Keyed, World}
import org.bukkit.inventory.{CraftingInventory, ItemStack}

/**
  * models a crafting recipe as a function: itemgrid -> (result, remainders)
  */
trait CraftingRecipe extends Recipe with Keyed {

    def tryCraft(craftingInventory: CraftingInventory, world: World): Option[CraftingResult]

}