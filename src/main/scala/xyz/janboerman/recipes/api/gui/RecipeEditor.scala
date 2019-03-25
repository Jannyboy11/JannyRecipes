package xyz.janboerman.recipes.api.gui

import org.bukkit.{ChatColor, Material, NamespacedKey}
import org.bukkit.event.inventory.{InventoryClickEvent, InventoryOpenEvent, InventoryType}
import org.bukkit.inventory.{Inventory, ItemStack}
import org.bukkit.plugin.Plugin
import xyz.janboerman.guilib.api.ItemBuilder
import xyz.janboerman.guilib.api.menu.MenuHolder
import xyz.janboerman.recipes.api.{JannyRecipesAPI, PlayerUtils}
import xyz.janboerman.recipes.api.recipe.{Recipe, ShapedRecipe}

object RecipeEditor {

    //TODO create some kind of Map<Class<? extends Recipe>, Supplier<Option<ItemStack, ? extends RecipeEditor>>> to which other plugins can register their custom recipe types
    //TODO I might be able to use the type-class pattern here? yes but no cause I also want an UnkownRecipeEditor that just supports the delete operation for unknown recipes.

    lazy val UnknownRecipeIcon: ItemStack = new ItemBuilder(Material.STRUCTURE_VOID).name(ChatColor.RED + "Unknown recipe").build()


}

abstract class RecipeEditor[P <: Plugin, R <: Recipe](protected var recipe: R,
                                                      private val inventory: Inventory)
                                                     (implicit protected val recipesMenu: RecipesMenu[P],
                                                      implicit protected val api: JannyRecipesAPI,
                                                      implicit protected val plugin: P)
    extends MenuHolder[P](plugin, inventory) {
    // Scala I wish you allowed us to be able to call different super constructors in different constructor overloads. dammit.
    // The following doesn't work since 'this' cannot be used in the body of an auxiliary constructor. Why!?
//    def this(recipe: R, size: Int, title: String)
//            (implicit recipesMenu: RecipesMenu[P], api: JannyRecipesAPI, plugin: P) = {
//        this(recipe, plugin.getServer.createInventory(this, size, title))
//    }
//
//    def this(recipe: R, inventoryType: InventoryType, title: String)
//            (implicit recipesMenu: RecipesMenu[P], api: JannyRecipesAPI, plugin: P) = {
//        this(recipe, plugin.getServer.createInventory(this, inventoryType, title))
//    }

    private var firstTimeOpen = true

    override def onOpen(event: InventoryOpenEvent): Unit = {
        if (firstTimeOpen) {
            layoutBorder()
            layoutRecipe()
            layoutButtons()

            firstTimeOpen = false
        }

        super.onOpen(event)
    }

    override def onClick(event: InventoryClickEvent): Unit = {
        event.setCancelled(false)
        super.onClick(event)

        PlayerUtils.updateInventory(event.getWhoClicked, getPlugin) //needed to update the client's bad shift-click predictions
    }

    def getCachedRecipe(): Option[R] = {
        Option(recipe)
    }

    protected def layoutBorder(): Unit
    protected def layoutRecipe(): Unit
    protected def layoutButtons(): Unit

    /**
      * Constructs a new recipe based on the items in the inventory, and on other fields in the subclasses.
      * @return Some(recipe) if a new recipe could be constructed, otherwise None
      */
    def makeRecipe(): Option[R] = {
        throw new NotImplementedError("The current editor (" + this + ") doesn't know how to make a new recipe from inventory contents (yet). It should override makeRecipe")
    }

    def getIcon(): Option[ItemStack] = None

}

trait KeyedRecipeEditorOps { this: RecipeEditor[_, _/*TODO require that the Recipe implements Keyed?*/] =>
    private var key: NamespacedKey = _

    def getKey(): NamespacedKey = key
    def setKey(key: NamespacedKey): Unit = this.key = key

    def getResultItemSlot(): Int //In Scala 3, I would use a parameterized trait to pass this number.

    protected def generateId(): NamespacedKey = {
        val resultStack = getInventory.getItem(getResultItemSlot())

        //theoretically not unique, but I'm not going to bother.
        var uniqueString = java.util.UUID.randomUUID().toString

        if (resultStack != null) {
            uniqueString = resultStack.getType.name() + "_" + uniqueString
        }

        new NamespacedKey(this.getPlugin.asInstanceOf[Plugin], uniqueString)
    }
}

trait GroupedRecipeEditorOps { this: RecipeEditor[_, _/*TODO require that the Recipe implements Grouped?*/] =>
    private var group: String = _

    def getGroup: String = group
    def setGroup(group: String): Unit = this.group = group
}
