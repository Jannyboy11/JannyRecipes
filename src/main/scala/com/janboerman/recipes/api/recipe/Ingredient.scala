package com.janboerman.recipes.api.recipe

import org.bukkit.inventory.ItemStack

trait Ingredient {
    def apply(itemStack: ItemStack): Boolean
}

//TODO use a type-class pattern?, so that modified ingredients can 'implement' the type class if the base ingredient also implements it?
//TODO or modifiers can just take that into account in their implementation and change their return type accordingly.
//TODO first approach does not enable runtime-querying of whether an ingredient implements this trait.
trait ItemIngredient { self: Ingredient =>
    def firstItem(): ItemStack
}

