package xyz.janboerman.recipes.api.gui

import java.util.Collections

import org.bukkit.{ChatColor, Material, NamespacedKey}
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.inventory.Inventory
import org.bukkit.plugin.Plugin
import org.bukkit.scheduler.BukkitTask
import xyz.janboerman.guilib.api.ItemBuilder
import xyz.janboerman.guilib.api.menu.{ItemButton, RedirectItemButton}
import xyz.janboerman.recipes.api.JannyRecipesAPI
import xyz.janboerman.recipes.api.recipe.{CraftingRecipe, Grouped}

object CraftingRecipeEditor {
    val CornerX = 1
    val CornerY = 1
    val ResultSlot = 2 * 9 + 6

    val MaxWidth = 3
    val MaxHeight = 3

    val InventoryWidth = 9
    val InventoryHeight = 6
    val HotbarSize = InventoryWidth
    val TopInventorySize = InventoryWidth * InventoryHeight
}
import CraftingRecipeEditor._

abstract class CraftingRecipeEditor[P <: Plugin, R <: CraftingRecipe](inventory: Inventory, craftingRecipe: R)(
    protected implicit override val recipesMenu: RecipesMenu[P],
    protected implicit override val api: JannyRecipesAPI,
    protected implicit override val plugin: P)
    extends RecipeEditor[P, R](craftingRecipe, inventory) { self =>

    private implicit val storage = api.persist()

    protected var group: String = ""
    protected var key: NamespacedKey = null

    if (recipe != null) {
        key = recipe.getKey
        if (recipe.isInstanceOf[Grouped]) {
            val groupedRecipe = recipe.asInstanceOf[R with Grouped]
            this.group = groupedRecipe.getGroup().getOrElse("")
        }
    }


    //TODO duplicate code (it's also in FurnaceRecipeEditor)
    protected def generateId(): NamespacedKey = {
        val resultStack = getInventory.getItem(ResultSlot)

        var uniqueString = java.util.UUID.randomUUID().toString

        if (resultStack != null) {
            uniqueString = resultStack.getType.name() + "_" + uniqueString
        }

        new NamespacedKey(plugin, uniqueString)
    }

    def layoutBorder(): Unit = {
        val glassPaneStack = new ItemBuilder(Material.LIGHT_BLUE_STAINED_GLASS_PANE).name(Space).build()
        val inventory = getInventory

        for (i <- 0 until 9) inventory.setItem(i, glassPaneStack)
        for (i <- 36 until 45) inventory.setItem(i, glassPaneStack)
        for (y <- 1 until 4) {
            for (x <- 0::4::5::7::8::Nil) {
                inventory.setItem(y * InventoryWidth + x, glassPaneStack)
            }
        }
        inventory.setItem(15, glassPaneStack)
        inventory.setItem(33, glassPaneStack)
    }

    def layoutButtons(): Unit = {
        val saveAndExitButton = new SaveButton[P, R, self.type](() => recipesMenu.getInventory)
        val exitButton = new RedirectItemButton[self.type](
            new ItemBuilder(Material.RED_CONCRETE).name(Exit).build(),
            () => recipesMenu.getInventory())

        val renameButton = new AnvilButton[self.type]({case (menuHolder, event, input) =>
            val player = event.getWhoClicked
            val split = input.split(":")
            if (split.exists(s => s.isEmpty || !s.matches("[a-z0-9._-]+"))) {
                player.sendMessage(ChatColor.RED + "Recipe names can only contain lowercase alphanumeric characters, underscores and dashes.")
            } else {
                this.key = if (split.length > 1) {
                    val namespace = split(0)
                    val rest = String.join("_", split.tail: _*)
                    new NamespacedKey(namespace, rest)
                } else {
                    new NamespacedKey(menuHolder.getPlugin, split(0))
                }
            }
            menuHolder.getPlugin.getServer.getScheduler.runTask(menuHolder.getPlugin, (_: BukkitTask) => player.openInventory(menuHolder.getInventory))
        }, if (this.key == null) "" else this.key.toString, new ItemBuilder(Material.NAME_TAG).name(Rename).lore({
            if (this.key == null)
                Collections.emptyList[String]()
            else
                Collections.singletonList(lore(key.toString))
        }).build())

        val setGroupButton = new AnvilButton[self.type]({case (menuHolder, event, input) =>
            val player = event.getWhoClicked
            this.group = input

            menuHolder.getPlugin.getServer.getScheduler.runTask(menuHolder.getPlugin, (_: BukkitTask) => player.openInventory(menuHolder.getInventory))
        }, if (this.group == null) "" else this.group, new ItemBuilder(Material.CHEST).name(SetGroup).lore({
            if (this.group == null || this.group.isEmpty) {
                Collections.emptyList[String]()
            } else {
                Collections.singletonList(lore(group))
            }
        }).build())

        val modifiersButton = new ItemButton[self.type](new ItemBuilder(Material.HEART_OF_THE_SEA).name(Modifiers).build()) //TODO Make this a RedirectItemButton as well

        //TODO
        val deleteButton = new RedirectItemButton[self.type](
            new ItemBuilder(Material.BARRIER).name(Delete).build(), //TODO use YesNoMenu from GuiLib
            () => recipesMenu.getInventory()) {

            override def onClick(menuHolder: self.type, event: InventoryClickEvent): Unit = {
                //deleteRecipe() //TODO
                super.onClick(menuHolder, event)
            }
        }

        setButton(45, saveAndExitButton)
        setButton(47, renameButton)
        setButton(48, setGroupButton)
        //49: type button
        setButton(50, modifiersButton)
        setButton(51, deleteButton)
        setButton(53, exitButton)
    }

}
