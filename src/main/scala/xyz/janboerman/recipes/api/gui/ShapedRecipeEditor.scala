package xyz.janboerman.recipes.api.gui

import org.bukkit.inventory.{Inventory, ItemStack}
import org.bukkit.plugin.Plugin
import xyz.janboerman.recipes.api.JannyRecipesAPI
import xyz.janboerman.recipes.api.recipe.ShapedRecipe
import CraftingRecipeEditor._
import org.bukkit.Material
import org.bukkit.enchantments.Enchantment
import xyz.janboerman.guilib.api.ItemBuilder
import xyz.janboerman.guilib.api.menu.ItemButton

class ShapedRecipeEditor(inventory: Inventory,
                         shapedRecipe: ShapedRecipe,
                         mainMenu: RecipesMenu,
                         api: JannyRecipesAPI,
                         plugin: Plugin)
    extends CraftingRecipeEditor[ShapedRecipe](inventory, shapedRecipe, mainMenu, api, plugin) {

    override def getIcon(): Option[ItemStack] = Option(recipe).map(_.getResult())

    def saveRecipe(): Boolean = {
        //TODO

        // make new ingredients and from itemstacks in the inventory
        // be sure to apply modifiers as necessary
        // if the namescape is 'minecraft' we should delete the original recipe and change the namespace to 'janny'

        // save to config


        false
    }

    def deleteRecipe(): Boolean = {
        //TODO

        // if the namespace is 'minecraft' we should save in a config that the vanilla recipe is deleted.

        false
    }

    override def layoutButtons(): Unit = {
        setButton(49, new ItemButton[ShapedRecipeEditor](new ItemBuilder(Material.CRAFTING_TABLE).name(TypeShaped).enchant(Enchantment.DURABILITY, 1).build()))
        super.layoutButtons()
    }

    def layoutRecipe(): Unit = {
        if (shapedRecipe != null) {

            val height = Math.min(MaxHeight, shapedRecipe.getShape().size)
            val width = Math.min(MaxWidth, shapedRecipe.getShape()(0).length)

            for (h <- 0 until height) {
                val y = CornerY + h + (if (height == 1) 1 else 0)
                val row = shapedRecipe.getShape()(h)

                for (w <- 0 until width) {
                    val x = CornerX + w + (if (width == 1) 1 else 0)
                    val char = row(w)

                    shapedRecipe.getIngredients().get(char) match {
                        case Some(craftingIngredient) =>
                            val invIndex = x + y * InventoryWidth

                            val iTriedToUseOrNullButThatDidntWork: Option[ItemStack] = craftingIngredient.getChoices().headOption

                            val ingredientIcon: ItemStack = iTriedToUseOrNullButThatDidntWork match {
                                case Some(itemStack) => itemStack
                                case None => null
                            }

                            inventory.setItem(invIndex, ingredientIcon)
                        case None => ()
                    }
                }
            }

            inventory.setItem(ResultSlot, shapedRecipe.getResult())
        }
    }

    //https://twitter.com/JanBoerman95/status/917791860487225351

    // use custom anvil guis for recipe namespaced keys and groups
}
