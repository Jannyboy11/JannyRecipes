package com.janboerman.recipes.api.gui

import org.bukkit.inventory.{Inventory, ItemStack}
import org.bukkit.plugin.Plugin
import com.janboerman.recipes.api.JannyRecipesAPI
import com.janboerman.recipes.api.recipe.{CraftingIngredient, ShapelessRecipe, SimpleCraftingIngredient, SimpleShapelessRecipe}
import CraftingRecipeEditor._
import org.bukkit.Material
import org.bukkit.enchantments.Enchantment
import xyz.janboerman.guilib.api.ItemBuilder
import xyz.janboerman.guilib.api.menu.ItemButton

class ShapelessRecipeEditor[P <: Plugin](inventory: Inventory,
                            shapelessRecipe: ShapelessRecipe)
                           (implicit override val recipesMenu: RecipesMenu[P],
                            implicit override val api: JannyRecipesAPI,
                            implicit override val plugin: P)
    extends CraftingRecipeEditor[P, ShapelessRecipe](inventory, shapelessRecipe) {

    protected var shouldUpdateIngredients = false
    protected var oldIngredientContents = (CornerY until CornerY + MaxHeight)
        .flatMap(y => (CornerX until CornerX + MaxWidth)
            .map(x => y * InventoryWidth + x)).map(getInventory.getItem(_))
    private var ingredients: List[_ <: CraftingIngredient] = null

    if (recipe != null) {
        this.ingredients = recipe.getIngredients()
    }

    protected def  hasIngredientContentsChanged(): Boolean = {
        ingredients == null || shouldUpdateIngredients || oldIngredientContents != (CornerY until CornerY + MaxHeight)
            .flatMap(y => (CornerX until CornerX + MaxWidth)
                .map(x => y * InventoryWidth + x)).map(getInventory.getItem(_))
    }

    def setNewIngredients(ingredients: List[_ <: CraftingIngredient]): Unit = {
        this.ingredients = ingredients
        for ((index, ingredient) <- IngredientIndices.zip(ingredients)) {
            getInventory.setItem(index, ingredient.getChoices().headOption.orNull)
        }

        shouldUpdateIngredients = false
    }

    override def getIcon(): Option[ItemStack] = Option(recipe).map(_.getResult())

    override def layoutButtons(): Unit = {
        setButton(49, new ItemButton[ShapedRecipeEditor[P]](new ItemBuilder(Material.CRAFTING_TABLE).name(TypeShapeless).enchant(Enchantment.DURABILITY, 1).build()))
        super.layoutButtons()
    }

    override protected def layoutRecipe(): Unit = {
        if (recipe != null) {
            val inventory = getInventory()

            val inventoryIndices = (CornerY until CornerY + MaxHeight)
                .flatMap(y => (CornerX until CornerX + MaxWidth)
                    .map(x => y * InventoryWidth + x))

            for ((index, ingredient) <- inventoryIndices.zip(recipe.getIngredients())) {
                inventory.setItem(index, ingredient.getChoices().headOption.orNull)
            }
            inventory.setItem(ResultSlot, recipe.getResult())
        }
    }

    override def makeRecipe(): Option[ShapelessRecipe] = {
        val ingredientStacks = (CornerY until CornerY + MaxHeight)
            .flatMap(y => (CornerX until CornerX + MaxWidth)
                .map(x => y * InventoryWidth + x)).map(getInventory.getItem(_))
            .filter(_ != null)

        if (ingredientStacks.nonEmpty) {
            //there are ingredients - let's  create a recipe!
            val result = getInventory.getItem(ResultSlot)
            val key = if (this.getKey() == null) generateId() else this.getKey()
            val group = Option(this.getGroup).filter(_.nonEmpty)
            val ingredients = if (hasIngredientContentsChanged()) {
                //TODO only 'replace' the ingredients of the slots where the itemstack actually changed?
                ingredientStacks.map(CraftingIngredient(_)).toList
            } else {
                this.ingredients
            }

            val recipe = new SimpleShapelessRecipe(key, group, ingredients, result)
            //TODO apply modifiers
            Some(recipe)
        } else {
            //no ingredients - too bad
            None
        }
    }
    
}
