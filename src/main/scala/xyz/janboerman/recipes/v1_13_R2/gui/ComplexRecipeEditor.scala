package xyz.janboerman.recipes.v1_13_R2.gui

import net.minecraft.server.v1_13_R2.{ChatComponentText, InventorySubcontainer}
import org.bukkit.craftbukkit.v1_13_R2.inventory.CraftInventory
import org.bukkit.inventory.InventoryHolder
import org.bukkit.plugin.Plugin
import xyz.janboerman.recipes.api.JannyRecipesAPI
import xyz.janboerman.recipes.api.gui.{ArmorDyeRecipeEditor, BannerAddPatternRecipeEditor, BannerDuplicateRecipeEditor, BookCloneRecipeEditor, FireworkRocketRecipeEditor, FireworkStarFadeRecipeEditor, FireworkStarRecipeEditor, MapCloneRecipeEditor, MapExtendRecipeEditor, RecipesMenu, RepairItemRecipeEditor, ShieldDecorationRecipeEditor, ShulkerBoxColorRecipeEditor, TippedArrowRecipeEditor}
import xyz.janboerman.recipes.api.recipe._

object ArmorDyeRecipeEditor {
    def apply[P <: Plugin](recipe: ArmorDyeRecipe)(implicit api: JannyRecipesAPI, plugin: P, recipesMenu: RecipesMenu[P]): ArmorDyeRecipeEditor[P] = {
        val inventory = new SimpleNMSInventory(54, "Toggle Armour Dye Recipe")
        val holder = new ArmorDyeRecipeEditor(recipe, new CraftInventory(inventory))
        inventory.setInventoryHolder(holder)
        holder
    }
}

object BannerAddPatternRecipeEditor {
    def apply[P <: Plugin](recipe: BannerAddPatternRecipe)(implicit api: JannyRecipesAPI, plugin: P, recipesMenu: RecipesMenu[P]): BannerAddPatternRecipeEditor[P] = {
        val inventory = new SimpleNMSInventory(54, "Toggle Banner Pattern Recipe")
        val holder = new BannerAddPatternRecipeEditor(recipe, new CraftInventory(inventory))
        inventory.setInventoryHolder(holder)
        holder
    }
}

object BannerDuplicateRecipeEditor {
    def apply[P <: Plugin](recipe: BannerDuplicateRecipe)(implicit api: JannyRecipesAPI, plugin: P, recipesMenu: RecipesMenu[P]): BannerDuplicateRecipeEditor[P] = {
        val inventory = new SimpleNMSInventory(54, "Toggle Banner Duplication Recipe")
        val holder = new BannerDuplicateRecipeEditor(recipe, new CraftInventory(inventory))
        inventory.setInventoryHolder(holder)
        holder
    }
}

object BookCloneRecipeEditor {
    def apply[P <: Plugin](recipe: BookCloneRecipe)(implicit api: JannyRecipesAPI, plugin: P, recipesMenu: RecipesMenu[P]): BookCloneRecipeEditor[P] = {
        val inventory = new SimpleNMSInventory(54, "Toggle Book Clone Recipe")
        val holder = new BookCloneRecipeEditor(recipe, new CraftInventory(inventory))
        inventory.setInventoryHolder(holder)
        holder
    }
}

object FireworkRocketRecipeEditor {
    def apply[P <: Plugin](recipe: FireworkRocketRecipe)(implicit api: JannyRecipesAPI, plugin: P, recipesMenu: RecipesMenu[P]): FireworkRocketRecipeEditor[P] = {
        val inventory = new SimpleNMSInventory(54, "Toggle Firework Rocket Recipe")
        val holder = new FireworkRocketRecipeEditor(recipe, new CraftInventory(inventory))
        inventory.setInventoryHolder(holder)
        holder
    }
}

object FireworkStarFadeRecipeEditor {
    def apply[P <: Plugin](recipe: FireworkStarFadeRecipe)(implicit api: JannyRecipesAPI, plugin: P, recipesMenu: RecipesMenu[P]): FireworkStarFadeRecipeEditor[P] = {
        val inventory = new SimpleNMSInventory(54, "Toggle Firework Star Fade Recipe")
        val holder = new FireworkStarFadeRecipeEditor(recipe, new CraftInventory(inventory))
        inventory.setInventoryHolder(holder)
        holder
    }
}

object FireworkStarRecipeEditor {
    def apply[P <: Plugin](recipe: FireworkStarRecipe)(implicit api: JannyRecipesAPI, Plugin: P, recipesMenu: RecipesMenu[P]): FireworkStarRecipeEditor[P] = {
        val inventory = new SimpleNMSInventory(54, "Toggle Firework Star Recipe")
        val holder = new FireworkStarRecipeEditor(recipe, new CraftInventory(inventory))
        inventory.setInventoryHolder(holder)
        holder
    }
}

object MapCloneRecipeEditor {
    def apply[P <: Plugin](recipe: MapCloneRecipe)(implicit api: JannyRecipesAPI, plugin: P, recipesMenu: RecipesMenu[P]): MapCloneRecipeEditor[P] = {
        val inventory = new SimpleNMSInventory(54, "Toggle Map Clone Recipe")
        val holder = new MapCloneRecipeEditor(recipe, new CraftInventory(inventory))
        inventory.setInventoryHolder(holder)
        holder
    }
}

object MapExtendRecipeEditor {
    def apply[P <: Plugin](recipe: MapExtendRecipe)(implicit api: JannyRecipesAPI, plugin: P, recipesMenu: RecipesMenu[P]): MapExtendRecipeEditor[P] = {
        val inventory = new SimpleNMSInventory(54, "Toggle Map Extend Recipe")
        val holder = new MapExtendRecipeEditor(recipe, new CraftInventory(inventory))
        inventory.setInventoryHolder(holder)
        holder
    }
}

object RepairItemRecipeEditor {
    def apply[P <: Plugin](recipe: RepairItemRecipe)(implicit api: JannyRecipesAPI, plugin: P, recipesMenu: RecipesMenu[P]): RepairItemRecipeEditor[P] = {
        val inventory = new SimpleNMSInventory(54, "Toggle Item Repair Recipe")
        val holder = new RepairItemRecipeEditor(recipe, new CraftInventory(inventory))
        inventory.setInventoryHolder(holder)
        holder
    }
}

object ShieldDecorationRecipeEditor {
    def apply[P <: Plugin](recipe: ShieldDecorationRecipe)(implicit api: JannyRecipesAPI, plugin: P, recipesMenu: RecipesMenu[P]): ShieldDecorationRecipeEditor[P] = {
        val inventory = new SimpleNMSInventory(54, "Toggle Shield Decoration Recipe")
        val holder = new ShieldDecorationRecipeEditor(recipe, new CraftInventory(inventory))
        inventory.setInventoryHolder(holder)
        holder
    }
}

object ShulkerBoxColorRecipeEditor {
    def apply[P <: Plugin](recipe: ShulkerBoxColorRecipe)(implicit api: JannyRecipesAPI, plugin: P, recipesMenu: RecipesMenu[P]): ShulkerBoxColorRecipeEditor[P] = {
        val inventory = new SimpleNMSInventory(54, "Toggle Shulker Box Colouring Recipe")
        val holder = new ShulkerBoxColorRecipeEditor(recipe, new CraftInventory(inventory))
        inventory.setInventoryHolder(holder)
        holder
    }
}

object TippedArrowRecipeEditor {
    def apply[P <: Plugin](recipe: TippedArrowRecipe)(implicit api: JannyRecipesAPI, plugin: P, recipesMenu: RecipesMenu[P]): TippedArrowRecipeEditor[P] = {
        val inventory = new SimpleNMSInventory(54, "Toggle Tipped Arrow Recipe")
        val holder = new TippedArrowRecipeEditor(recipe, new CraftInventory(inventory))
        inventory.setInventoryHolder(holder)
        holder
    }
}

class SimpleNMSInventory(size: Int, title: String) extends InventorySubcontainer(new ChatComponentText(title), size) {

    def setInventoryHolder(owner: InventoryHolder): Unit = this.bukkitOwner = owner

}
