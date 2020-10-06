package com.janboerman.recipes.api.recipe.modify

import java.util

import com.janboerman.recipes.api.recipe.{CraftingIngredient, Ingredient, ItemIngredient, Recipe}
import org.bukkit.Material
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.ItemMeta
import xyz.janboerman.guilib.api.ItemBuilder
import com.janboerman.recipes.api.gui.{ModifiersMenu, RecipeEditor, interactable}
import org.bukkit.plugin.Plugin
import xyz.janboerman.guilib.api.menu.MenuButton

//TODO!

trait IngredientModifier[Base <: Ingredient] {

    def modify(base: Base): ModifiedIngredient[Base]
}

trait ModifiedIngredient[Base <: Ingredient] {
    def getBase(): Base
    def getModifier: IngredientModifier[Base]
}

class AmountModifier[Base <: CraftingIngredient](amount: Int) extends IngredientModifier[Base] { self =>
    override def modify(base: Base): ModifiedIngredient[Base] =
        new CraftingIngredient with ModifiedIngredient[Base] {
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
    override def modify(base: Base): ModifiedIngredient[Base] = {
        val core = new Ingredient with ModifiedIngredient[Base] {
            override def apply(itemStack: ItemStack): Boolean = {
                if (itemStack == null) itemMeta == null
                else if (!itemStack.hasItemMeta) itemMeta == null
                else itemStack.getItemMeta.equals(itemMeta)
            }

            override def getBase(): Base = base

            override def getModifier: IngredientModifier[Base] = self
        }

        if (base.isInstanceOf[ItemIngredient]) {
            val itemIngredient = base.asInstanceOf[ItemIngredient]
            val firstItemMatch = itemIngredient.firstItem().getItemMeta == itemMeta

            if (firstItemMatch) {
                return new Ingredient with ModifiedIngredient[Base] with ItemIngredient {
                    override def apply(itemStack: ItemStack): Boolean = core.apply(itemStack)
                    override def getBase(): Base = core.getBase()
                    override def getModifier: IngredientModifier[Base] = core.getModifier
                    override def firstItem(): ItemStack = itemIngredient.firstItem()
                }
            }
        }

        core
    }
}

case class IngredientLocator[IngredientLocation, BaseRecipe <: Recipe, BaseIngredient <: Ingredient](location: IngredientLocation, locator: (BaseRecipe, IngredientLocation) => BaseIngredient) {
    def locate(recipe: BaseRecipe): BaseIngredient = locator(recipe, location)
}

//TODO not sure whether I even want this class..
class IngredientRecipeModifier[BaseIngredient <: Ingredient, IngredientLocation, BaseRecipe <: Recipe](
    ingredientLocator: IngredientLocator[IngredientLocation, BaseRecipe, BaseIngredient],
    baseRecipe: BaseRecipe, ingredientLocation: IngredientLocation)

    extends RecipeModifier[BaseRecipe, BaseRecipe] { self =>

    //TODO i feel like this should have been a separate class. oh well :)
    lazy val modifierType: ModifierType[IngredientLocator[IngredientLocation, BaseRecipe, BaseIngredient], BaseRecipe, BaseRecipe] = new ModifierType[IngredientLocator[IngredientLocation, BaseRecipe, BaseIngredient], BaseRecipe, BaseRecipe] {
        lazy val icon: ItemStack = {
            val baseIngredient = ingredientLocator.locate(baseRecipe)
            val stack = if (baseIngredient.isInstanceOf[ItemIngredient]) {
                baseIngredient.asInstanceOf[ItemIngredient].firstItem()
            } else {
                new ItemStack(Material.STRUCTURE_VOID)
            }

            new ItemBuilder(stack)
                .name(interactable("Ingredient" /*TODO what convention whas I using again?*/))
                //TODO add some lore?
                .build()
        }

        override def getIcon(): ItemStack = icon

        override def create(base: BaseRecipe, from: IngredientLocator[IngredientLocation, BaseRecipe, BaseIngredient]): Either[String, RecipeModifier[BaseRecipe, BaseRecipe]] = {
            Right(self) //TODO this seems incorrect design
        }

        override def getName(): String = "with modified ingredient"

        override def appliesTo(recipe: Recipe): Boolean = baseRecipe.getClass.isInstance(recipe)    //TODO is this correct?

        override def newButton[P <: Plugin, RE <: RecipeEditor[P, BaseRecipe]](recipe: BaseRecipe)(implicit plugin: P): MenuButton[ModifiersMenu[P, BaseRecipe, RE]] = {
            //TODO
            ???
        }
    }

    //TODO
    override def apply(base: BaseRecipe): BaseRecipe with ModifiedRecipe[BaseRecipe, BaseRecipe /*use base as basic becase we are only modifying the ingredient!*/] = {
        //TODO
        ???
    }

    /**
      * The Icon of the modifier when it is not applied to a recipe.
      *
      * @return an itemStack with a name indicating what kind of modifier it is
      */
    override def getIcon(): ItemStack = ??? //TODO

    override def getType(): ModifierType[IngredientLocator[IngredientLocation, BaseRecipe, BaseIngredient], BaseRecipe, BaseRecipe] = modifierType;

    override def serialize(): util.Map[String, AnyRef] = ??? //TODO (don't forget valueOf in companion object)
}


