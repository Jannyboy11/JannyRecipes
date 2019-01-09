package xyz.janboerman.recipes.api.gui

import java.text.DecimalFormat

import org.bukkit.enchantments.Enchantment
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.{ChatColor, Material}
import org.bukkit.inventory.{Inventory, ItemStack}
import org.bukkit.plugin.Plugin
import xyz.janboerman.guilib.api.ItemBuilder
import xyz.janboerman.guilib.api.menu.{ItemButton, RedirectItemButton}
import xyz.janboerman.recipes.api.JannyRecipesAPI
import xyz.janboerman.recipes.api.recipe.FurnaceRecipe

object FurnaceRecipeEditor {
    val IngredientSlot = 1 * 9 + 2
    val ResultSlot = 1 * 9 + 6

    val InventoryWidth = 9
    val InventoryHeight = 4
    val HotbarSize = InventoryWidth
    val TopInventorySize = InventoryWidth * InventoryHeight
}
import FurnaceRecipeEditor._

class FurnaceRecipeEditor(inventory: Inventory,
                          furnaceRecipe: FurnaceRecipe,
                          mainMenu: RecipesMenu,
                          api: JannyRecipesAPI,
                          plugin: Plugin)
    extends RecipeEditor[FurnaceRecipe](furnaceRecipe, mainMenu, api, inventory, plugin) {

    override def getIcon(): Option[ItemStack] = Option(recipe).map(_.getResult())

    override protected def layoutBorder(): Unit = {
        val glassPaneStack = new ItemBuilder(Material.LIGHT_BLUE_STAINED_GLASS_PANE).name(Space).build()
        val inventory = getInventory

        for (i <- 0 until 9) inventory.setItem(i, glassPaneStack)
        for (x <- 0 until (18, 1)) {
            inventory.setItem(1 * InventoryWidth + x, glassPaneStack)
        }
        for (i <- 18 until 27) inventory.setItem(i, glassPaneStack)
    }

    override protected def layoutRecipe(): Unit = {
        if (recipe != null) {
            val inventory = getInventory

            inventory.setItem(10, recipe.getIngredient().getItemStack())
            inventory.setItem(12, recipe.getResult())
        }
    }

    def layoutButtons(): Unit = {
        val typeButton = new ItemButton[FurnaceRecipeEditor](new ItemBuilder(Material.FURNACE).name(TypeFurnace).enchant(Enchantment.DURABILITY, 1).build())

        if (recipe != null) {
            val cookingTimeButton = new ItemButton[FurnaceRecipeEditor](new ItemBuilder(Material.CLOCK)
                .name(interactable("Cooking time" + (if (recipe != null) ": " + recipe.getCookingTime() else "")))
                .build()) //TODO add functionality
            val experienceButton = new ItemButton[FurnaceRecipeEditor](new ItemBuilder(Material.EXPERIENCE_BOTTLE)
                .name(interactable("Experience" + (if (recipe != null) ": " + new DecimalFormat("#.##").format(recipe.getExperience()) else "")))
                .build()) //TODO add functionality

            setButton(14, cookingTimeButton)
            setButton(16, experienceButton)
        }

        val saveAndExitButton = new RedirectItemButton[FurnaceRecipeEditor](
            new ItemBuilder(Material.LIME_CONCRETE).name(SaveAndExit).build(),
            () => recipesMenu.getInventory()) {

            override def onClick(menuHolder: FurnaceRecipeEditor, event: InventoryClickEvent): Unit = {
                saveRecipe()
                super.onClick(menuHolder, event)
            }
        }
        val exitButton = new RedirectItemButton[FurnaceRecipeEditor](
            new ItemBuilder(Material.RED_CONCRETE).name(Exit).build(),
            () => recipesMenu.getInventory())


        val renameButton = new ItemButton[FurnaceRecipeEditor](new ItemBuilder(Material.NAME_TAG).name(Rename).build()) //TODO make this a RedirectItemButton as well
        val setGroupButton = new ItemButton[FurnaceRecipeEditor](new ItemBuilder(Material.CHEST).name(SetGroup).build()) //TODO Make this a RedirectItemButton as well
        val modifiersButton = new ItemButton[FurnaceRecipeEditor](new ItemBuilder(Material.HEART_OF_THE_SEA).name(Modifiers).build()) //TODO Make this a RedirectItemButton as well

        val deleteButton = new RedirectItemButton[FurnaceRecipeEditor](
            new ItemBuilder(Material.BARRIER).name(Delete).build(), //TODO use YesNoMenu from GuiLib
            () => recipesMenu.getInventory()) {

            override def onClick(menuHolder: FurnaceRecipeEditor, event: InventoryClickEvent): Unit = {
                deleteRecipe()
                super.onClick(menuHolder, event)
            }
        }

        setButton(27, saveAndExitButton)
        setButton(29, renameButton)
        setButton(30, setGroupButton)
        setButton(31, typeButton)
        setButton(32, modifiersButton)
        setButton(33, deleteButton)
        setButton(35, exitButton)
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
