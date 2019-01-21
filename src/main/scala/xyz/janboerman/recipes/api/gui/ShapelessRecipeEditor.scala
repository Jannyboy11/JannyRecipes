package xyz.janboerman.recipes.api.gui

import org.bukkit.inventory.{Inventory, ItemStack}
import org.bukkit.plugin.Plugin
import xyz.janboerman.recipes.api.JannyRecipesAPI
import xyz.janboerman.recipes.api.recipe.{CraftingIngredient, ShapelessRecipe, SimpleCraftingIngredient, SimpleShapelessRecipe}
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
        //TODO because ingredients can have multiple materials (itemstacks actualy) called 'choices'
        //TODO cache CraftingIngredients in the CraftingRecipeEditor field.

        //TODO use that cache here.
        //TODO for now just use the itemstacks that are in the editor
        val ingredientStacks = (CornerY until CornerY + MaxHeight)
            .flatMap(y => (CornerX until CornerX + MaxWidth)
                .map(x => y * InventoryWidth + x)).map(getInventory.getItem(_))
            .filter(_ != null)

        if (ingredientStacks.nonEmpty) {
            //there are ingredients - let's  create a recipe!
            val result = getInventory.getItem(ResultSlot)
            val key = if (this.key == null) generateId() else this.key
            val group = this.group //TODO does not seem to work? might be due to the other bug //TODO test this again.
            println("DEBUG simple group = " + group)
            val ingredients = ingredientStacks.map(is => new SimpleCraftingIngredient(List(is))).toList //TODO use cached ingredients.

            val recipe = new SimpleShapelessRecipe(key, Option(group), ingredients, result)
            Some(recipe)
        } else {
            //no ingredients - too bad
            None
        }
    }
    
}
