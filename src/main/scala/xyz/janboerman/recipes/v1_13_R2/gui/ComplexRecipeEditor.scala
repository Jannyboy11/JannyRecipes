package xyz.janboerman.recipes.v1_13_R2.gui

import net.minecraft.server.v1_13_R2.{ChatComponentText, InventorySubcontainer}
import org.bukkit.craftbukkit.v1_13_R2.inventory.CraftInventory
import org.bukkit.inventory.InventoryHolder
import org.bukkit.plugin.Plugin
import xyz.janboerman.recipes.api.JannyRecipesAPI
import xyz.janboerman.recipes.api.gui.{ArmorDyeRecipeEditor, RecipesMenu}
import xyz.janboerman.recipes.api.recipe.ArmorDyeRecipe

class ComplexRecipeEditor {

}

object ArmorDyeRecipeEditor {
    def apply[P <: Plugin](recipe: ArmorDyeRecipe)(implicit api: JannyRecipesAPI, plugin: P, recipesMenu: RecipesMenu[P]): ArmorDyeRecipeEditor[P] = {
        val inventory = new SimpleNMSInventory(54, "Edit Armor Dye Recipe")
        val holder = new ArmorDyeRecipeEditor(recipe, new CraftInventory(inventory))
        inventory.setInventoryHolder(holder)
        holder
    }
}

class SimpleNMSInventory(size: Int, title: String) extends InventorySubcontainer(new ChatComponentText(title), size) {

    def setInventoryHolder(owner: InventoryHolder): Unit = this.bukkitOwner = owner

}
