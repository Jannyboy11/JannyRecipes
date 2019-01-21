package xyz.janboerman.recipes.api.recipe.modify

import xyz.janboerman.recipes.api.recipe.Recipe

//TODO can I use typeclasses/implicits here?
trait ModifiedRecipe[R <: Recipe] extends Recipe {
    type T <: Recipe



    def getBaseRecipe(): R

}

//trait ModifiedCraftingRecipe[R <: CraftingRecipe] extends ModifiedRecipe[R] with CraftingRecipe
//
//case class SimpleModifiedCraftingRecipe[B <: CraftingRecipe](baseRecipe: B,
//                                                             resultModifier: (CraftingInventory, World, Option[CraftingResult]) => Option[CraftingResult],
//                                                             keyModifier: NamespacedKey => NamespacedKey = identity)
//    extends ModifiedCraftingRecipe[B] {
//
//    override def getBaseRecipe(): B = baseRecipe
//
//    override def tryCraft(inventory: CraftingInventory, world: World): Option[CraftingResult] = {
//        resultModifier.apply(inventory, world, getBaseRecipe().tryCraft(inventory, world))
//    }
//
//    override def getKey: NamespacedKey = keyModifier.apply(baseRecipe.getKey)
//
//    override def getType(): RecipeType = ??? //TODO ModifiedType
//}