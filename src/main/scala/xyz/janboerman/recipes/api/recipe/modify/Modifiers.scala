package xyz.janboerman.recipes.api.recipe.modify

import java.util.UUID

import org.bukkit.entity.Player
import org.bukkit.{Bukkit, ChatColor, Material, NamespacedKey, World}
import org.bukkit.inventory.{CraftingInventory, ItemStack}
import org.bukkit.plugin.Plugin
import org.bukkit.scheduler.BukkitTask
import xyz.janboerman.guilib.api.ItemBuilder
import xyz.janboerman.guilib.api.menu.MenuButton
import xyz.janboerman.recipes.api.gui._
import xyz.janboerman.recipes.api.recipe._

import scala.collection.mutable

//TODO modifiers and ModifiedRecipes should be ConfigurationSerializable.

trait RecipeModifier[Base <: Recipe] {
    def apply(base: Base): ModifiedRecipe[Base]

    /**
      * The Icon of the modifier when it is not applied to a recipe.
      * @return an itemStack with a name indicating what kind of modifier it is
      */
    def getIcon(): ItemStack
}

object ModifiedRecipe {
    def unapply[Base <: Recipe](arg: ModifiedRecipe[Base]): Option[(Base, RecipeModifier[Base])] =
        Some((arg.getBaseRecipe(), arg.getModifier))
}

trait ModifiedRecipe[Base <: Recipe] /*does not extend Recipe because it is intended to be used using `with` */ {
    def getBaseRecipe(): Base
    def getModifier: RecipeModifier[Base]
}

class ModifiedRecipeType(val baseRecipeType: RecipeType, val modifierType: ModifierType[_, _, _]) extends RecipeType {
    override def getIcon(): ItemStack = baseRecipeType.getIcon() //TODO add to the lore? (e.g. for a world modifier "World: <world-name>") should the RecipeEditor do this?
    override def getName(): String = modifierType.getName() + " " + baseRecipeType.getName()
}

object ModifierType {
    val values = new mutable.HashSet[ModifierType[_, _, _]]()

    values.add(WorldModifier.modifierType[CraftingRecipe])
    values.add(PermissionModifier.modifierType[CraftingRecipe])
}

trait ModifierType[T, Base <: Recipe, M <: RecipeModifier[Base]] {
    def getIcon(): ItemStack
    def create(base: Base, from: T): Either[String, M]
    def getName(): String
    def appliesTo(recipe: Recipe): Boolean
    def newButton[P <: Plugin, RE <: RecipeEditor[P, Base]](recipe: Base)(implicit plugin: P): MenuButton[ModifiersMenu[P, Base, RE]]
}

object WorldModifier {
    implicit def modifierType[Base <: CraftingRecipe] = new ModifierType[String, Base, WorldModifier[Base]]() {
        override def getIcon(): ItemStack = new ItemBuilder(Material.FILLED_MAP).name(interactable("World")).build()
        override def create(base: Base, from: String): Either[String, WorldModifier[Base]] = {
            var world = Bukkit.getWorld(from)
            if (world == null) {
                try {
                    val uid = UUID.fromString(from)
                    world = Bukkit.getWorld(uid)
                    if (world != null) {
                        return Right(new WorldModifier(base, world))                   }
                } catch {
                    case _: IllegalArgumentException => () //string is not a world name or uuid - continue to `return Left(...)`
                }
            }

            return Left("No world exists with name or uid " + from)
        }
        override def getName(): String = "World-Specific"
        override def appliesTo(recipe: Recipe): Boolean = recipe.isInstanceOf[CraftingRecipe]

        override def newButton[P <: Plugin, RE <: RecipeEditor[P, Base]](recipe: Base)(implicit plugin: P): MenuButton[ModifiersMenu[P, Base, RE]] =
            //TODO instead of an anvil button, use a button that opens a new gui that lets you pick the world.

            new AnvilButton[ModifiersMenu[P, Base, RE]](icon = getIcon(), paperDisplayName = "", callback = (modifiersMenu, event, input) => {
                val eitherErrorWorldModifier = create(modifiersMenu.getRecipe(), input)

                //TODO apply modifier to recipe.
                //TODO re-open modifiers menu
            })
    }
}

class WorldModifier[Base <: CraftingRecipe](val base: Base, val world: World) extends RecipeModifier[Base] { self =>

    override def apply(base: Base): ModifiedRecipe[Base] =
        new CraftingRecipe with ModifiedRecipe[Base] {
            override def tryCraft(craftingInventory: CraftingInventory, world: World): Option[CraftingResult] = {
                craftingInventory.getHolder match {
                    case player: Player if player.getWorld.equals(world) => base.tryCraft(craftingInventory, world)
                    case _ => None
                }
            }

            override def getType(): RecipeType = new ModifiedRecipeType(base.getType(), WorldModifier.modifierType)

            override def getKey: NamespacedKey = base.getKey

            override def getBaseRecipe(): Base = base

            override def getModifier() = self
        }

    override def getIcon(): ItemStack = new ItemBuilder(Material.FILLED_MAP).name(interactable("World")).lore(lore(world.getName)).build()
}


object PermissionModifier {
    implicit def modifierType[Base <: CraftingRecipe] = new ModifierType[String, Base, PermissionModifier[Base]] {
        override def getIcon(): ItemStack = new ItemBuilder(Material.IRON_BARS).name(interactable("Permission")).build()
        override def create(base: Base, from: String): Either[String, PermissionModifier[Base]] = Right(new PermissionModifier(base, from))
        override def getName(): String = "Permission-Specific"
        override def appliesTo(recipe: Recipe): Boolean = recipe.isInstanceOf[CraftingRecipe]
        override def newButton[P <: Plugin, RE <: RecipeEditor[P, Base]](base: Base)(implicit plugin: P): MenuButton[ModifiersMenu[P, Base, RE]] =
            new AnvilButton[ModifiersMenu[P, Base, RE]](icon = getIcon(), paperDisplayName = "", callback = (modifiersMenu, event, input) => {
                val eitherErrorPermissionModifier = create(base, input)
                eitherErrorPermissionModifier match {
                    case Right(permissionModifier) =>
                    //TODO apply modifier to recipe.
                    //TODO set it to the RecipeEditor

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
}

class PermissionModifier[Base <: CraftingRecipe](val base: Base, val permission: String) extends RecipeModifier[Base] { self =>

    //TODO ideally i would want to return Base with ModifiedRecipeBase - use metaprogramming?!
    override def apply(base: Base): ModifiedRecipe[Base] = {
        new CraftingRecipe with ModifiedRecipe[Base] {
            override def tryCraft(craftingInventory: CraftingInventory, world: World): Option[CraftingResult] = {
                craftingInventory.getHolder match {
                    case player: Player if player.hasPermission(permission) => base.tryCraft(craftingInventory, world)
                    case _ => None
                }
            }

            override def getType(): RecipeType = new ModifiedRecipeType(base.getType(), PermissionModifier.modifierType)

            override def getKey: NamespacedKey = base.getKey

            override def getBaseRecipe(): Base = base

            override def getModifier() = self
        }
    }

    override def getIcon(): ItemStack = new ItemBuilder(Material.IRON_BARS).name(interactable("Permission")).lore(lore(permission)).build()

}

