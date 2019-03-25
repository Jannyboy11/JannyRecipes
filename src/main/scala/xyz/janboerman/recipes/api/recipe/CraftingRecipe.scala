package xyz.janboerman.recipes.api.recipe

import org.bukkit.{Keyed, World}
import org.bukkit.inventory.CraftingInventory

/**
  * Models a crafting recipe as a function: itemgrid -> (result, remainders)
  */
trait CraftingRecipe extends Recipe with Keyed /*no 'with Grouped' because complex recipes don't have groups*/ {

    def tryCraft(craftingInventory: CraftingInventory, world: World): Option[CraftingResult]

}