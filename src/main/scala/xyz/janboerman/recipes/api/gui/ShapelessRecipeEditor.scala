package xyz.janboerman.recipes.api.gui

import org.bukkit.inventory.{Inventory, ItemStack}
import org.bukkit.plugin.Plugin
import xyz.janboerman.recipes.api.JannyRecipesAPI
import xyz.janboerman.recipes.api.recipe.ShapelessRecipe
import CraftingRecipeEditor._
import org.bukkit.Material
import org.bukkit.enchantments.Enchantment
import xyz.janboerman.guilib.api.ItemBuilder
import xyz.janboerman.guilib.api.menu.ItemButton

class ShapelessRecipeEditor(inventory: Inventory,
                            shapelessRecipe: ShapelessRecipe,
                            mainMenu: RecipesMenu,
                            api: JannyRecipesAPI,
                            plugin: Plugin)
    extends CraftingRecipeEditor[ShapelessRecipe](inventory, shapelessRecipe, mainMenu, api, plugin) {

    override def getIcon(): Option[ItemStack] = Option(recipe).map(_.getResult())

    override def layoutButtons(): Unit = {
        setButton(49, new ItemButton[ShapedRecipeEditor](new ItemBuilder(Material.CRAFTING_TABLE).name(TypeShapeless).enchant(Enchantment.DURABILITY, 1).build()))
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

    override def saveRecipe(): Boolean = {
        //TODO

        false
    }

    override def deleteRecipe(): Boolean = {
        //TODO

        false
    }
}
