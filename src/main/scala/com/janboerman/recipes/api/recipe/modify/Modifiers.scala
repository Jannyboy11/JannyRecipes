package com.janboerman.recipes.api.recipe.modify

import java.util.UUID

import com.janboerman.recipes.api.gui.{ModifiersMenu, RecipeEditor}
import com.janboerman.recipes.api.recipe.CraftingResult
import org.bukkit.entity.Player
import org.bukkit.{Bukkit, ChatColor, Material, NamespacedKey, World}
import org.bukkit.inventory.{CraftingInventory, ItemStack}
import org.bukkit.plugin.Plugin
import org.bukkit.scheduler.BukkitTask
import xyz.janboerman.guilib.api.ItemBuilder
import xyz.janboerman.guilib.api.menu.MenuButton
import com.janboerman.recipes.api.gui._
import com.janboerman.recipes.api.recipe._

import scala.collection.mutable

//TODO modifiers and ModifiedRecipes should be ConfigurationSerializable.

trait RecipeModifier[Base <: Recipe, Basic <: Recipe] {
    def apply(base: Base): ModifiedRecipe[Base, Basic] //do I need to add Basic as a second type argument? yes probably.

    /**m
      * The Icon of the modifier when it is not applied to a recipe.
      * @return an itemStack with a name indicating what kind of modifier it is
      */
    def getIcon(): ItemStack

    def getType(): ModifierType[_, Base, Basic /*Basic represents the 'minimal' recipe type that this modifier produces. */]
}

object ModifiedRecipe {
    def unapply[Base <: Recipe, Basic <: Recipe](arg: ModifiedRecipe[Base, Basic]): Option[(Base, RecipeModifier[Base, Basic])] =
        Some((arg.getBaseRecipe(), arg.getModifier()))
}

trait ModifiedRecipe[Base <: Recipe, Basic <: Recipe] { this: Recipe =>
    def getBaseRecipe(): Base
    def getModifier(): RecipeModifier[Base, Basic] /*not sure whether Basic must be a type parameter of the trait, or a type parameter of the method*/
}

class ModifiedRecipeType(val baseRecipeType: RecipeType, val modifierType: ModifierType[_, _, _]) extends RecipeType {
    override def getIcon(): ItemStack = baseRecipeType.getIcon() //TODO add to the lore? (e.g. for a world modifier "World: <world-name>") should the RecipeEditor do this?
    override def getName(): String = modifierType.getName() + " " + baseRecipeType.getName()
}

object ModifierType {
    val values = new mutable.HashSet[ModifierType[_, _, _]]()

    values.add(WorldModifierType)
    values.add(PermissionModifierType)
}

trait ModifierType[From, Base <: Recipe, Basic <: Recipe] {
    def getIcon(): ItemStack
    def create(base: Base, from: From): Either[String, RecipeModifier[Base, Basic]]
    def getName(): String
    def appliesTo(recipe: Recipe): Boolean
    def newButton[P <: Plugin, RE <: RecipeEditor[P, Base]](recipe: Base)(implicit plugin: P): MenuButton[ModifiersMenu[P, Base, RE]]
}

object WorldModifierType extends ModifierType[String, CraftingRecipe, CraftingRecipe] {
    override def getIcon(): ItemStack = new ItemBuilder(Material.FILLED_MAP).name(interactable("World")).build()
    override def create(base: CraftingRecipe, from: String): Either[String, WorldModifier] = {
        var world = Bukkit.getWorld(from)
        if (world == null) {
            try {
                val uid = UUID.fromString(from)
                world = Bukkit.getWorld(uid)
                if (world != null) {
                    return Right(new WorldModifier(base, world))
                }
            } catch {
                case _: IllegalArgumentException => () //string is not a world name or uuid - continue to `return Left(...)`
            }
        }

        return Left("No world exists with name or uid " + from)
    }
    override def getName(): String = "World-Specific"
    override def appliesTo(recipe: Recipe): Boolean = recipe.isInstanceOf[CraftingRecipe]

    override def newButton[P <: Plugin, RE <: RecipeEditor[P, CraftingRecipe]](recipe: CraftingRecipe)(implicit plugin: P): MenuButton[ModifiersMenu[P, CraftingRecipe, RE]] =
    //TODO instead of an anvil button, use a button that opens a new gui that lets you pick the world.

        new AnvilButton[ModifiersMenu[P, CraftingRecipe, RE]](icon = getIcon(), paperDisplayName = "", callback = (modifiersMenu, event, input) => {
            val eitherErrorWorldModifier = create(modifiersMenu.getRecipe(), input)

            //TODO apply modifier to recipe.
            //TODO re-open modifiers menu
        })
}

class WorldModifier(val base: CraftingRecipe, val world: World) extends RecipeModifier[CraftingRecipe, CraftingRecipe] { self =>

    override def apply(base: CraftingRecipe): ModifiedRecipe[CraftingRecipe, CraftingRecipe] = {
        //a type-class pattern would have fit nicely probably, because Ideally we would have `new Basic with ModifiedRecipe` but `new` can't be used for type arguments.
        new CraftingRecipe with ModifiedRecipe[CraftingRecipe, CraftingRecipe] {
            override def tryCraft(craftingInventory: CraftingInventory, world: World): Option[CraftingResult] = {
                craftingInventory.getHolder match {
                    case player: Player if player.getWorld.equals(world) => base.tryCraft(craftingInventory, world)
                    case _ => None
                }
            }

            override def getType(): RecipeType = new ModifiedRecipeType(base.getType(), WorldModifierType)

            override def getKey: NamespacedKey = base.getKey

            override def getBaseRecipe(): CraftingRecipe = base

            override def getModifier() = self
        }
    }

    override def getIcon(): ItemStack = new ItemBuilder(Material.FILLED_MAP).name(interactable("World")).lore(lore(world.getName)).build()

    override def getType() = WorldModifierType
}

object PermissionModifierType extends ModifierType[String, CraftingRecipe, CraftingRecipe] {
    override def getIcon(): ItemStack = new ItemBuilder(Material.IRON_BARS).name(interactable("Permission")).build()
    override def create(base: CraftingRecipe, from: String): Either[String, PermissionModifier] = Right(new PermissionModifier(base, from))
    override def getName(): String = "Permission-Specific"
    override def appliesTo(recipe: Recipe): Boolean = recipe.isInstanceOf[CraftingRecipe]
    override def newButton[P <: Plugin, RE <: RecipeEditor[P, CraftingRecipe]](base: CraftingRecipe)(implicit plugin: P): MenuButton[ModifiersMenu[P, CraftingRecipe, RE]] =
        new AnvilButton[ModifiersMenu[P, CraftingRecipe, RE]](icon = getIcon(), paperDisplayName = "", callback = (modifiersMenu, event, input) => {
            val eitherErrorPermissionModifier = create(base, input)
            eitherErrorPermissionModifier match {
                case Right(permissionModifier) =>
                //TODO apply modifier to recipe.
                //TODO set it to the RecipeEditor.
                //TODO make its icon glow using an enchantment



                case Left(message) =>
                    event.getWhoClicked.sendMessage(ChatColor.RED + message)
            }

            //re-open modifiers menu
            modifiersMenu.getPlugin.getServer.getScheduler.runTask(modifiersMenu.getPlugin, (_: BukkitTask) => {
                event.getView.close()
                event.getWhoClicked.openInventory(modifiersMenu.getInventory)
            })
        })
}

class PermissionModifier(val base: CraftingRecipe, val permission: String) extends RecipeModifier[CraftingRecipe, CraftingRecipe] { self =>

    //TODO ideally i would want to return Base with ModifiedRecipeBase - use metaprogramming?!
    //TODO can't I use Shapeless? That seems really far fetched. probably better to wait for Scala 3 and use Match Types.

    override def apply(base: CraftingRecipe): ModifiedRecipe[CraftingRecipe, CraftingRecipe] = {
        new CraftingRecipe with ModifiedRecipe[CraftingRecipe, CraftingRecipe] {
            override def tryCraft(craftingInventory: CraftingInventory, world: World): Option[CraftingResult] = {
                craftingInventory.getHolder match {
                    case player: Player if player.hasPermission(permission) => base.tryCraft(craftingInventory, world)
                    case _ => None
                }
            }

            override def getType(): RecipeType = new ModifiedRecipeType(base.getType(), PermissionModifierType)

            override def getKey: NamespacedKey = base.getKey

            override def getBaseRecipe(): CraftingRecipe = base

            override def getModifier() = self
        }
    }

    override def getIcon(): ItemStack = new ItemBuilder(Material.IRON_BARS).name(interactable("Permission")).lore(lore(permission)).build()

    override def getType() = PermissionModifierType
}

