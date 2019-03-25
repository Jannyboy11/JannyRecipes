package xyz.janboerman.recipes.api.recipe.modify

import org.bukkit.entity.Player
import org.bukkit.{Material, NamespacedKey, World}
import org.bukkit.inventory.{CraftingInventory, ItemStack}
import xyz.janboerman.guilib.api.ItemBuilder
import xyz.janboerman.recipes.api.gui._

import scala.collection.mutable
import xyz.janboerman.recipes.api.recipe._

object RecipeModifier {
    val values = new mutable.HashSet[RecipeModifier[_]]()


}

trait RecipeModifier[R <: Recipe] extends (R => ModifiedRecipe[R]) {

    def appliesTo(recipeType: Recipe): Boolean

    def getIcon(): ItemStack
}


trait ModifiedRecipe[R <: Recipe] { self: R =>

    def getBaseRecipe(): R
    def getModifier(): RecipeModifier[R]

}

trait CraftingRecipeModifier[R <: CraftingRecipe] extends RecipeModifier[R] {


}

trait SmeltingRecipeModifier[R <: SmeltingRecipe] extends RecipeModifier[R] {


}

//TODO? classes for shaped?, shapeless? and furnace?



class PermissionModifier(val permission: String) extends CraftingRecipeModifier[CraftingRecipe] { self =>

    override def appliesTo(recipe: Recipe): Boolean = recipe.isInstanceOf[CraftingRecipe]

    override def apply(base: CraftingRecipe): CraftingRecipe with ModifiedRecipe[CraftingRecipe] = {
        new CraftingRecipe with ModifiedRecipe[CraftingRecipe] {
            override def tryCraft(craftingInventory: CraftingInventory, world: World): Option[CraftingResult] = {
                craftingInventory.getHolder match {
                    case player: Player if player.hasPermission(permission) => base.tryCraft(craftingInventory, world)
                    case _ => None
                }
            }

            override def getType(): RecipeType = base match {
                case shaped: ShapedRecipe => ShapedType
                case shapeless: ShapelessRecipe => ShapelessType
                case _ => UnknownType
            }

            override def getKey: NamespacedKey = base.getKey

            override def getBaseRecipe(): CraftingRecipe = base

            override def getModifier(): RecipeModifier[CraftingRecipe] = self
        }
    }

    override def getIcon(): ItemStack = new ItemBuilder(Material.IRON_BARS).name(interactable("Permission")).lore(lore(permission)).build() //TODO when (not) to include the permission in the lore?

}

//TODO
class ShapedIngredientModifier(ingredientKey: Char, ingredientModifier: CraftingIngredient => CraftingIngredient /*TODO create a specialized type for this?*/)
    extends CraftingRecipeModifier[ShapedRecipe] { self =>


    override def appliesTo(recipeType: Recipe): Boolean = ???

    override def getIcon(): ItemStack = ???

    override def apply(v1: ShapedRecipe): ModifiedRecipe[ShapedRecipe] = ???
}

//TODO
class ShapelessIngredientModifier(ingredientKey: Int, ingredientModifier: CraftingIngredient => CraftingIngredient /*TODO idem*/)
    extends CraftingRecipeModifier[ShapelessRecipe] { self =>


    override def appliesTo(recipeType: Recipe): Boolean = ???

    override def getIcon(): ItemStack = ???

    override def apply(v1: ShapelessRecipe): ModifiedRecipe[ShapelessRecipe] = ???
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