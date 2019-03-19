package xyz.janboerman.recipes.api.gui

import org.bukkit.{Material, NamespacedKey}
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.inventory.Inventory
import org.bukkit.plugin.Plugin
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

    val IngredientIndices = (CornerY until CornerY + MaxHeight)
        .flatMap(y => (CornerX until CornerX + MaxWidth)
            .map(x => y * InventoryWidth + x))

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

    extends RecipeEditor[P, R](craftingRecipe, inventory)
    with KeyedRecipeEditorOps
    with GroupedRecipeEditorOps { self =>

    private implicit val storage = api.persist()

    if (recipe != null) {
        setKey(recipe.getKey)
        if (recipe.isInstanceOf[Grouped]) {
            val groupedRecipe = recipe.asInstanceOf[R with Grouped]
            setGroup(groupedRecipe.getGroup().getOrElse(""))
        }
    }

    override def getResultItemSlot(): Int = ResultSlot

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
        val saveAndExitIndex = 45
        val renameIndex = 47
        val setGroupIndex = 48
        val modifiersIndex = 50
        val deleteIndex = 51
        val exitIndex = 53

        val saveAndExitButton = new SaveButton[P, R, self.type](() => recipesMenu.getInventory)
        val exitButton = new RedirectItemButton[self.type](
            new ItemBuilder(Material.RED_CONCRETE).name(Exit).build(),
            () => recipesMenu.getInventory())

        val renameButton = new RenameButton(renameIndex, this)
        val setGroupButton = new SetGroupButton(setGroupIndex, this)
        val modifiersButton = new ItemButton[self.type](new ItemBuilder(Material.HEART_OF_THE_SEA).name(Modifiers).build()) //TODO Make this a RedirectItemButton as well
        val deleteButton = new DeleteButton[P, R, this.type]()

        setButton(saveAndExitIndex, saveAndExitButton)
        setButton(renameIndex, renameButton)
        setButton(setGroupIndex, setGroupButton)
        //49: type button
        setButton(modifiersIndex, modifiersButton)
        setButton(deleteIndex, deleteButton)
        setButton(exitIndex, exitButton)
    }

}
