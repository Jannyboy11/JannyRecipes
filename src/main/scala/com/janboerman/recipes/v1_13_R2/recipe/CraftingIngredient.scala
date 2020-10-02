package com.janboerman.recipes.v1_13_R2.recipe

import net.minecraft.server.v1_13_R2.ItemStack

//why not rename to BetterRecipeItemStack? because we don't extend it. we replace it! :)
//TODO move tho the customnms package? rename to CustomCraftingIngredient?
trait CraftingIngredient extends Ingredient {

    def choices: List[ItemStack]
    def getRemainingItemStack(ingredientStack: ItemStack): ItemStack = {
        import com.janboerman.recipes.v1_13_R2.Extensions.NmsItem

        if (ingredientStack.getItem.hasCraftingResult()) {
            val customResultStack = ingredientStack.cloneItemStack()
            customResultStack.setItem(ingredientStack.getItem.getCraftingResult())
            customResultStack
        } else {
            ingredientStack
        }
    }
}

//TODO CustomFurnaceIngredient?
trait FurnaceIngredient extends Ingredient {
    def itemStack: ItemStack
}
