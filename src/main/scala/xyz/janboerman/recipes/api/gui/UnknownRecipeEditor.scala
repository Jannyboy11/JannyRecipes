package xyz.janboerman.recipes.api.gui

import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.{ChatColor, Material}
import org.bukkit.plugin.Plugin
import xyz.janboerman.guilib.api.ItemBuilder
import xyz.janboerman.guilib.api.menu.{ItemButton, RedirectItemButton}
import xyz.janboerman.recipes.api.JannyRecipesAPI
import xyz.janboerman.recipes.api.recipe.Recipe

class UnknownRecipeEditor(recipe: Recipe, mainMenu: RecipesMenu, api: JannyRecipesAPI, plugin: Plugin)
    extends RecipeEditor[Recipe](recipe, mainMenu, api, plugin.getServer.createInventory(null, 54, "Edit Unknown Recipe"), plugin) {
    override protected def layoutBorder(): Unit = {}

    override def onClick(event: InventoryClickEvent): Unit = {
        super.onClick(event)
        event.setCancelled(true)
    }

    override protected def layoutRecipe(): Unit = {
        val glassPaneButton = new ItemButton(new ItemBuilder(Material.RED_STAINED_GLASS_PANE).name(ChatColor.RESET + " ").build())

        for (i <- 2::3::4::5::6::11::15::22::23::24::31::49::Nil) {
            setButton(i, glassPaneButton)
        }
    }

    override protected def layoutButtons(): Unit = {
        val exitButton = new RedirectItemButton[UnknownRecipeEditor](new ItemBuilder(Material.RED_CONCRETE).name(Exit).build(), () => mainMenu.getInventory)

        setButton(53, exitButton)

        val deleteButton = new ItemButton[UnknownRecipeEditor](
            new ItemBuilder(Material.BARRIER).name(Delete).build()) //TODO use RedirectItemButton, get the YesNoMenu from the guiFactory
    }

    override def saveRecipe(): Boolean = {
        //TODO cannot really do this.

        false
    }

    override def deleteRecipe(): Boolean = {
        //TODO can do this if the recipe is keyed.

        false
    }

    override def getIcon(): Option[ItemStack] = Some(RecipeEditor.UnknownRecipeIcon)
}
