package xyz.janboerman.recipes.api.gui

import java.text.DecimalFormat

import org.bukkit.enchantments.Enchantment
import org.bukkit.{ChatColor, Material}
import org.bukkit.inventory.{Inventory, ItemStack}
import org.bukkit.plugin.Plugin
import org.bukkit.scheduler.BukkitTask
import xyz.janboerman.guilib.api.ItemBuilder
import xyz.janboerman.guilib.api.menu.{ItemButton, RedirectItemButton}
import xyz.janboerman.recipes.api.JannyRecipesAPI
import xyz.janboerman.recipes.api.recipe.{FurnaceIngredient, FurnaceRecipe, SimpleFurnaceRecipe}

import scala.util.Try

object FurnaceRecipeEditor {
    val IngredientSlot = 1 * 9 + 1
    val ResultSlot = 1 * 9 + 3

    val InventoryWidth = 9
    val InventoryHeight = 4
    val HotbarSize = InventoryWidth
    val TopInventorySize = InventoryWidth * InventoryHeight
}
import FurnaceRecipeEditor._

class FurnaceRecipeEditor[P <: Plugin](inventory: Inventory,
                                       furnaceRecipe: FurnaceRecipe)
                                      (implicit val mainMenu: RecipesMenu[P],
                                       implicit override val api: JannyRecipesAPI,
                                       implicit override val plugin: P)

    extends RecipeEditor[P, FurnaceRecipe](furnaceRecipe, inventory)
    with KeyedRecipeEditorOps
    with GroupedRecipeEditorOps { self =>

    private var cookingTime: Int = 200
    private var experience: Float = 0F

    protected var shouldUpdateIngredients = false
    protected var oldIngredientContent = getInventory.getItem(IngredientSlot)
    private var ingredient: FurnaceIngredient = _

    if (recipe != null) {
        this.ingredient = recipe.getIngredient()
        setKey(recipe.getKey)
        setGroup(recipe.getGroup().getOrElse(""))
        this.cookingTime = recipe.getCookingTime()
        this.experience = recipe.getExperience()
    }

    protected def hasIngredientContentsChanged(): Boolean = {
        ingredient == null || shouldUpdateIngredients || oldIngredientContent != getInventory.getItem(IngredientSlot)
    }

    override def makeRecipe(): Option[FurnaceRecipe] = {
        val ingredientStack = getInventory.getItem(IngredientSlot)

        if (ingredientStack != null) {
            val ingredient = if (hasIngredientContentsChanged()) {
                FurnaceIngredient(ingredientStack)
            } else {
                this.ingredient
            }
            val result = getInventory.getItem(ResultSlot)
            val key = if (this.getKey() == null) generateId() else this.getKey()
            val group = Option(this.getGroup).filter(_.nonEmpty)
            val cookingTime = this.cookingTime
            val experience = this.experience

            val recipe = new SimpleFurnaceRecipe(key, group, ingredient, result, cookingTime, experience)
            Some(recipe)
        } else {
            //no ingredient - too bad
            None
        }
    }

    override def getResultItemSlot(): Int = ResultSlot

    override def getIcon(): Option[ItemStack] = Option(recipe).map(_.getResult())

    override protected def layoutBorder(): Unit = {
        val glassPaneStack = new ItemBuilder(Material.LIGHT_BLUE_STAINED_GLASS_PANE).name(Space).build()
        val inventory = getInventory

        for (i <- 0 until 9) inventory.setItem(i, glassPaneStack)
        for (x <- 0 until (18, 2)) {
            inventory.setItem(1 * InventoryWidth + x, glassPaneStack)
        }
        for (i <- 18 until 27) inventory.setItem(i, glassPaneStack)
    }

    override protected def layoutRecipe(): Unit = {
        if (recipe != null) {
            val inventory = getInventory

            inventory.setItem(IngredientSlot, recipe.getIngredient().getItemStack())
            inventory.setItem(ResultSlot, recipe.getResult())
        }
    }

    def layoutButtons(): Unit = {

        val cookingTimeIndex = 14
        val experienceIndex = 16

        val saveAndExitIndex = 27
        val renameIndex = 29
        val setGroupIndex = 30
        val typeIndex = 31
        val modifiersIndex = 32
        val deleteIndex = 33
        val exitIndex = 35

        val typeButton = new ItemButton[FurnaceRecipeEditor[P]](new ItemBuilder(Material.FURNACE).name(TypeFurnace).enchant(Enchantment.DURABILITY, 1).build())

        val cookingTimeButton = new AnvilButton[self.type](callback = (menuHolder, event, input) => {
            val player = event.getWhoClicked

            Try { Integer.parseInt(input) } .toOption.filter(_ > 0) match {
                case Some(ck) =>
                    self.cookingTime = ck

                    //update the button's lore
                    val anvilButton = menuHolder.getButton(cookingTimeIndex).asInstanceOf[AnvilButton[_]]
                    anvilButton.setIcon(new ItemBuilder(anvilButton.getIcon).lore(lore(String.valueOf(self.cookingTime))).build())
                case None =>
                    player.sendMessage(ChatColor.RED + "Please enter a positive integer number.")
            }

            menuHolder.getPlugin.getServer.getScheduler.runTask(menuHolder.getPlugin, (_: BukkitTask) => player.openInventory(self.getInventory))
        },
            paperDisplayName = String.valueOf(if (recipe == null) self.cookingTime else recipe.getCookingTime()),
            icon = new ItemBuilder(Material.CLOCK)
                    .name(interactable("Cooking time"))
                    .lore(lore(String.valueOf(self.cookingTime)))
                    .build())

        val experienceButton = new AnvilButton[self.type](callback = (menuHolder, event, input) => {
            val player = event.getWhoClicked

            Try(java.lang.Double.parseDouble(input)).toOption.filter(_ >= 0) match {
                case Some(exp) =>
                    self.experience = exp.asInstanceOf[Float]

                    //update the button's lore
                    val anvilButton = menuHolder.getButton(experienceIndex).asInstanceOf[AnvilButton[_]]
                    anvilButton.setIcon(new ItemBuilder(anvilButton.getIcon).lore(lore(new DecimalFormat("#.##").format(self.experience))).build())
                case None =>
                    player.sendMessage(ChatColor.RED + "Please enter a non-negative number.")
            }

            menuHolder.getPlugin.getServer.getScheduler.runTask(menuHolder.getPlugin, (_: BukkitTask) => player.openInventory(menuHolder.getInventory))
        },
            paperDisplayName = if (self.recipe == null) "0" else new DecimalFormat("#.##").format(recipe.getExperience()),
            icon = new ItemBuilder(Material.EXPERIENCE_BOTTLE)
                    .name(interactable("Experience"))
                    .lore(lore(new DecimalFormat("#.##").format(self.experience)))
                    .build())

        setButton(cookingTimeIndex, cookingTimeButton)
        setButton(experienceIndex, experienceButton)

        val saveAndExitButton = new SaveButton[P, FurnaceRecipe, this.type](() => recipesMenu.getInventory)
        val exitButton = new RedirectItemButton[FurnaceRecipeEditor[P]](
            new ItemBuilder(Material.RED_CONCRETE).name(Exit).build(),
            () => recipesMenu.getInventory())

        val renameButton = new RenameButton(renameIndex, this)
        val setGroupButton = new SetGroupButton(setGroupIndex, this)
        val modifiersButton = new ItemButton[FurnaceRecipeEditor[P]](new ItemBuilder(Material.HEART_OF_THE_SEA).name(Modifiers).build()) //TODO Make this a RedirectItemButton as well
        val deleteButton = new DeleteButton[P, FurnaceRecipe, this.type]()

        setButton(saveAndExitIndex, saveAndExitButton)
        setButton(renameIndex, renameButton)
        setButton(setGroupIndex, setGroupButton)
        setButton(typeIndex, typeButton)
        setButton(modifiersIndex, modifiersButton)
        setButton(deleteIndex, deleteButton)
        setButton(exitIndex, exitButton)
    }

}
