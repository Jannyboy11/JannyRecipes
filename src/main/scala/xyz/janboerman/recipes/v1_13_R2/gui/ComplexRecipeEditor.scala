package xyz.janboerman.recipes.v1_13_R2.gui

import net.minecraft.server.v1_13_R2.{ChatComponentText, InventorySubcontainer}
import org.bukkit.craftbukkit.v1_13_R2.inventory.CraftInventory
import org.bukkit.inventory.InventoryHolder
import org.bukkit.plugin.Plugin
import xyz.janboerman.recipes.api.JannyRecipesAPI
import xyz.janboerman.recipes.api.gui.{ArmorDyeRecipeEditor, BannerAddPatternRecipeEditor, BannerDuplicateRecipeEditor, BookCloneRecipeEditor, RecipesMenu}
import xyz.janboerman.recipes.api.recipe.{ArmorDyeRecipe, BannerAddPatternRecipe, BannerDuplicateRecipe, BookCloneRecipe}

object ArmorDyeRecipeEditor {
    def apply[P <: Plugin](recipe: ArmorDyeRecipe)(implicit api: JannyRecipesAPI, plugin: P, recipesMenu: RecipesMenu[P]): ArmorDyeRecipeEditor[P] = {
        val inventory = new SimpleNMSInventory(54, "Edit Armour Dye Recipe")
        val holder = new ArmorDyeRecipeEditor(recipe, new CraftInventory(inventory))
        inventory.setInventoryHolder(holder)
        holder
    }
}

object BannerAddPatternRecipeEditor {
    def apply[P <: Plugin](recipe: BannerAddPatternRecipe)(implicit api: JannyRecipesAPI, plugin: P, recipesMenu: RecipesMenu[P]): BannerAddPatternRecipeEditor[P] = {
        val inventory = new SimpleNMSInventory(54, "Edit Banner Pattern Recipe")
        val holder = new BannerAddPatternRecipeEditor(recipe, new CraftInventory(inventory))
        inventory.setInventoryHolder(holder)
        holder
    }
}

object BannerDuplicateRecipeEditor {
    def apply[P <: Plugin](recipe: BannerDuplicateRecipe)(implicit api: JannyRecipesAPI, plugin: P, recipesMenu: RecipesMenu[P]): BannerDuplicateRecipeEditor[P] = {
        val inventory = new SimpleNMSInventory(54, "Edit Banner Duplication Recipe")
        val holder = new BannerDuplicateRecipeEditor(recipe, new CraftInventory(inventory))
        inventory.setInventoryHolder(holder)
        holder
    }
}

object BookCloneRecipeEditor {
    def apply[P <: Plugin](recipe: BookCloneRecipe)(implicit api: JannyRecipesAPI, plugin: P, recipesMenu: RecipesMenu[P]): BookCloneRecipeEditor[P] = {
        val inventory = new SimpleNMSInventory(54, "Edit Book Clone Recipe")
        val holder = new BookCloneRecipeEditor(recipe, new CraftInventory(inventory))
        inventory.setInventoryHolder(holder)
        holder
    }
}

class SimpleNMSInventory(size: Int, title: String) extends InventorySubcontainer(new ChatComponentText(title), size) {

    def setInventoryHolder(owner: InventoryHolder): Unit = this.bukkitOwner = owner

}
