package xyz.janboerman.recipes.api.recipe.modify

import org.bukkit.Material
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.ItemMeta
import xyz.janboerman.recipes.api.recipe.{CraftingIngredient, Ingredient, Recipe}
import xyz.janboerman.guilib.api.ItemBuilder
import xyz.janboerman.recipes.api.gui.interactable

//TODO!

trait IngredientModifier[Base <: Ingredient] {

    def modify(base: Base): ModifiedIngredient[Base]
}

trait ModifiedIngredient[Base <: Ingredient] {
    def getBase(): Base
    def getModifier: IngredientModifier[Base]
}

class AmountModifier[Base <: CraftingIngredient](amount: Int) extends IngredientModifier[Base] { self =>
    override def modify(base: Base): ModifiedIngredient[Base] = new CraftingIngredient with ModifiedIngredient[Base] {
        override def apply(itemStack: ItemStack): Boolean = {
            if (itemStack == null) amount <= 0
            else if (itemStack.getAmount < amount) false
            else super.apply(itemStack)
        }

        override def getRemainingStack(itemStack: ItemStack): Option[_ <: ItemStack] = {
            super.getRemainingStack(itemStack)
                .map(is => {is.setAmount(is.getAmount - (amount - 1)); is})
                .filter(_.getAmount >= 0)
        }

        override def getBase(): Base = base

        override def getModifier: IngredientModifier[Base] = self

        override def getChoices(): List[_ <: ItemStack] = base.getChoices()
    }
}

class MetaModifier[Base <: Ingredient](val itemMeta: ItemMeta) extends IngredientModifier[Base] { self =>
    override def modify(base: Base): ModifiedIngredient[Base] = new Ingredient with ModifiedIngredient[Base] {
        override def apply(itemStack: ItemStack): Boolean = {
            if (itemStack == null) itemMeta == null
            else if (!itemStack.hasItemMeta) itemMeta == null
            else itemStack.getItemMeta.equals(itemMeta)
        }

        override def getBase(): Base = base

        override def getModifier: IngredientModifier[Base] = self
    }
}



class IngredientRecipeModifier[BaseIngredient <: Ingredient, L, BaseRecipe <: Recipe](ingredientLocator: (BaseRecipe, L) => BaseIngredient, baseRecipe: BaseRecipe) extends RecipeModifier[BaseRecipe] {
    //TODO
    override def apply(base: BaseRecipe): BaseRecipe with ModifiedRecipe[BaseRecipe] = {
        //TODO
        ???
    }

    /**
      * The Icon of the modifier when it is not applied to a recipe.
      *
      * @return an itemStack with a name indicating what kind of modifier it is
      */
    override def getIcon(): ItemStack = ??? //TODO
}

