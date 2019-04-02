package xyz.janboerman.recipes.api.gui

import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.{ChatColor, Material}
import org.bukkit.plugin.Plugin
import xyz.janboerman.guilib.api.ItemBuilder
import xyz.janboerman.guilib.api.menu.{ItemButton, RedirectItemButton}
import xyz.janboerman.recipes.api.JannyRecipesAPI
import xyz.janboerman.recipes.api.recipe.Recipe

class UnknownRecipeEditor[P <: Plugin]()(implicit override val recipesMenu: RecipesMenu[P],
                                         implicit override val api: JannyRecipesAPI,
                                         implicit override val plugin: P)
    extends RecipeEditor[P, Recipe](null, plugin.getServer.createInventory(null, 54, "Toggle Unknown Recipe")) {
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
        val exitButton = new RedirectItemButton[UnknownRecipeEditor[P]](new ItemBuilder(Material.RED_CONCRETE).name(Exit).build(), () => recipesMenu.getInventory)

        setButton(53, exitButton)
        //TODO set the deletebutton here?
    }

    override def getIcon(): Option[ItemStack] = Some(RecipeEditor.UnknownRecipeIcon)
}
