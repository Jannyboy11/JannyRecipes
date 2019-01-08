package xyz.janboerman.recipes.api.recipe

import org.bukkit.inventory.ItemStack

trait Ingredient {
    def apply(itemStack: ItemStack): Boolean
}
