package com.janboerman.recipes.api.gui

import com.janboerman.recipes.api.JannyRecipesAPI
import org.bukkit.{ChatColor, Keyed, Material, NamespacedKey}
import org.bukkit.event.inventory.{InventoryClickEvent, InventoryOpenEvent}
import org.bukkit.inventory.{Inventory, ItemStack}
import org.bukkit.plugin.Plugin

import scala.collection.mutable
import xyz.janboerman.guilib.api.ItemBuilder
import xyz.janboerman.guilib.api.menu.MenuHolder
import com.janboerman.recipes.api.recipe.modify.{ModifierType, RecipeModifier}
import com.janboerman.recipes.api.{JannyRecipesAPI, PlayerUtils}
import com.janboerman.recipes.api.recipe.{Grouped, Recipe}

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

    private lazy val modifiers = new mutable.LinkedHashSet[RecipeModifier[R, _]]()

    def getModifiers(): mutable.Iterable[RecipeModifier[R, _]] = modifiers

    def addModifier(modifier: RecipeModifier[R, _]): Unit = modifiers.add(modifier)

    def setModifiers(modifiers: IterableOnce[RecipeModifier[R, _]]): Unit = {
        this.modifiers.clear()
        this.modifiers.addAll(modifiers)
    }

    def removeModifier(modifier: RecipeModifier[R, _]): Unit = modifiers.remove(modifier)

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

        PlayerUtils.updateInventory(event.getWhoClicked, getPlugin) //needed to correct the client's shift-click predictions
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
    def makeRecipe(): Option[R] = { //TODO change return type to Option[Recipe]? the only thing that calls this is SaveButton and it does not care about the type.
        throw new NotImplementedError("The current editor (" + this + ") doesn't know how to make a new recipe from inventory contents (yet). It should override makeRecipe")
    }

    def getIcon(): Option[ItemStack] = None

}

trait RecipeEditorKeyOps { this: RecipeEditor[_, _] =>
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

trait RecipeEditorGroupOps { this: RecipeEditor[_, _] =>
    private var group: String = _

    def getGroup: String = group
    def setGroup(group: String): Unit = this.group = group
}


trait RecipeEditorModifiersOps[R <: Recipe] { this: RecipeEditor[_, R] =>
    private lazy val modifiers = new mutable.HashMap[ModifierType[_, R, _], RecipeModifier[R, _]]()

    def setModifier[RM <: RecipeModifier[R, _]](modifierType: ModifierType[_, R, _], modifier: RM): Unit = {
        modifiers.put(modifierType, modifier)
    }

    def getModifier[RM <: RecipeModifier[R, _]](modifierType: ModifierType[_, R, _]): Option[RM] = {
        modifiers.get(modifierType).asInstanceOf[Option[RM]]
    }

    private def getModifiers: Iterable[RecipeModifier[R, _]] = modifiers.values
}