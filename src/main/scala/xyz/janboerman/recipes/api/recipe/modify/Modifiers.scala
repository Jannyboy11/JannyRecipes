package xyz.janboerman.recipes.api.recipe.modify

import java.util.UUID

import org.bukkit.entity.Player
import org.bukkit.{Bukkit, Material, NamespacedKey, World}
import org.bukkit.inventory.{CraftingInventory, ItemStack}
import org.bukkit.plugin.Plugin
import xyz.janboerman.guilib.api.ItemBuilder
import xyz.janboerman.guilib.api.menu.MenuButton
import xyz.janboerman.recipes.api.gui._

import scala.collection.mutable
import xyz.janboerman.recipes.api.recipe._
import xyz.janboerman.recipes.api.recipe.{ModifiedType, RecipeType}

//TODO modifiers and ModifiedRecipes should be ConfigurationSerializable.

trait RecipeModifier[R <: Recipe] extends (R => ModifiedRecipe[R]) {
    def getIcon(): ItemStack
}

trait ModifiedRecipe[R <: Recipe] { self: R =>
    def getBaseRecipe(): R
    def getModifier(): RecipeModifier[R]
}

trait CraftingRecipeModifier[R <: CraftingRecipe] extends RecipeModifier[R] {


}

trait SmeltingRecipeModifier[R <: SmeltingRecipe] extends RecipeModifier[R] {


}
//TODO? classes for shaped?, shapeless? and furnace?


object ModifierType {
    val values = new mutable.HashSet[ModifierType[_, _]]()

    values.add(PermissionModifier.modifierType)
    values.add(WorldModifier.modifierType)
}

trait ModifierType[T, M <: RecipeModifier[_]] {
    def getIcon(): ItemStack
    def create(from: T): Either[String, M]
    def getName(): String
    def appliesTo(recipe: Recipe): Boolean
    def newButton[R <: Recipe, P <: Plugin, RE <: RecipeEditor[P, R]](recipe: R)(implicit plugin: P): MenuButton[ModifiersMenu[P, R, RE]]
}

object WorldModifier {
    implicit val modifierType = new ModifierType[String, WorldModifier] {
        override def getIcon(): ItemStack = new ItemBuilder(Material.FILLED_MAP).name(interactable("World")).build()
        override def create(from: String): Either[String, WorldModifier] = {
            var world = Bukkit.getWorld(from)
            if (world == null) {
                try {
                    val uid = UUID.fromString(from)
                    world = Bukkit.getWorld(uid)
                    if (world != null) {
                        return Right(new WorldModifier(world))
                    }
                } catch {
                    case _: IllegalArgumentException => ()
                }
            }

            return Left("No world exists with name or uid " + from)
        }
        override def getName(): String = "World-Specific"
        override def appliesTo(recipe: Recipe): Boolean = recipe.isInstanceOf[CraftingRecipe]

        override def newButton[R <: Recipe, P <: Plugin, RE <: RecipeEditor[P, R]](recipe: R)(implicit plugin: P): MenuButton[ModifiersMenu[P, R, RE]] =
            new AnvilButton[ModifiersMenu[P, R, RE]](icon = getIcon(), paperDisplayName = "", callback = (modifiersMenu, event, input) => {
                val eitherErrorWorldModifier = create(input)

                //TODO apply modifier to recipe.
                //TODO re-open modifiers menu
            })
    }
}

class WorldModifier(val world: World) extends CraftingRecipeModifier[CraftingRecipe] { self =>

    override def apply(base: CraftingRecipe): CraftingRecipe with ModifiedRecipe[CraftingRecipe] =
        new CraftingRecipe with ModifiedRecipe[CraftingRecipe] { mix =>
            override def tryCraft(craftingInventory: CraftingInventory, world: World): Option[CraftingResult] = {
                craftingInventory.getHolder match {
                    case player: Player if player.getWorld.equals(world) => base.tryCraft(craftingInventory, world)
                    case _ => None
                }
            }

            override def getType(): RecipeType = {
                //TODO always return ModifiedType?
                base match {
                    case shaped: ShapedRecipe => ShapedType
                    case shapeless: ShapelessRecipe => ShapelessType
                    case _ => ModifiedType(base.getType(), WorldModifier.modifierType)
                }
            }

            override def getKey: NamespacedKey = base.getKey

            override def getBaseRecipe(): CraftingRecipe = base

            override def getModifier(): RecipeModifier[CraftingRecipe] = self
        }

    override def getIcon(): ItemStack = new ItemBuilder(Material.FILLED_MAP).name(interactable("World")).lore(lore(world.getName)).build()
}


object PermissionModifier {
    implicit val modifierType = new ModifierType[String, PermissionModifier] {
        override def getIcon(): ItemStack = new ItemBuilder(Material.IRON_BARS).name(interactable("Permission")).build()
        override def create(from: String): Either[String, PermissionModifier] = Right(new PermissionModifier(from))
        override def getName(): String = "Permission-Specific"
        override def appliesTo(recipe: Recipe): Boolean = recipe.isInstanceOf[CraftingRecipe]
        /*TODO override*/ def newButton[R <: Recipe, P <: Plugin, RE <: RecipeEditor[P, R]]
        (recipe: R, permissionModifier: PermissionModifier)
        (implicit plugin: P): MenuButton[ModifiersMenu[P, R, RE]] = {
            //TODO
            ???
        }
        override def newButton[R <: Recipe, P <: Plugin, RE <: RecipeEditor[P, R]](recipe: R)(implicit plugin: P): MenuButton[ModifiersMenu[P, R, RE]] =
            new AnvilButton[ModifiersMenu[P, R, RE]](icon = getIcon(), paperDisplayName = "", callback = (modifiersMenu, event, input) => {
                val eitherErrorPermissionModifier = create(input)

                //TODO apply modifier to recipe.
                //TODO re-open modifiers menu
            })
    }
}

class PermissionModifier(val permission: String) extends CraftingRecipeModifier[CraftingRecipe] { self =>

    override def apply(base: CraftingRecipe): CraftingRecipe with ModifiedRecipe[CraftingRecipe] = {
        new CraftingRecipe with ModifiedRecipe[CraftingRecipe] {
            override def tryCraft(craftingInventory: CraftingInventory, world: World): Option[CraftingResult] = {
                craftingInventory.getHolder match {
                    case player: Player if player.hasPermission(permission) => base.tryCraft(craftingInventory, world)
                    case _ => None
                }
            }

            override def getType(): RecipeType = base match {
                case shaped: ShapedRecipe => ShapedType
                case shapeless: ShapelessRecipe => ShapelessType
                case _ => UnknownType
            }

            override def getKey: NamespacedKey = base.getKey

            override def getBaseRecipe(): CraftingRecipe = base

            override def getModifier(): RecipeModifier[CraftingRecipe] = self
        }
    }

    override def getIcon(): ItemStack = new ItemBuilder(Material.IRON_BARS).name(interactable("Permission")).lore(lore(permission)).build()

}

//TODO
class ShapedIngredientModifier(ingredientKey: Char, ingredientModifier: CraftingIngredient => CraftingIngredient /*TODO create a specialized type for this?*/)
    extends CraftingRecipeModifier[ShapedRecipe] { self =>

    override def getIcon(): ItemStack = new ItemBuilder(Material.ITEM_FRAME).name(interactable("Ingredient")).build()

    override def apply(base: ShapedRecipe): ModifiedRecipe[ShapedRecipe] = {

        ???
    }
}

//TODO
class ShapelessIngredientModifier(ingredientKey: Int, ingredientModifier: CraftingIngredient => CraftingIngredient /*TODO idem*/)
    extends CraftingRecipeModifier[ShapelessRecipe] { self =>

    override def getIcon(): ItemStack = ???

    override def apply(recipe: ShapelessRecipe): ModifiedRecipe[ShapelessRecipe] = ???
}


//trait ModifiedCraftingRecipe[R <: CraftingRecipe] extends ModifiedRecipe[R] with CraftingRecipe
//
//case class SimpleModifiedCraftingRecipe[B <: CraftingRecipe](baseRecipe: B,
//                                                             resultModifier: (CraftingInventory, World, Option[CraftingResult]) => Option[CraftingResult],
//                                                             keyModifier: NamespacedKey => NamespacedKey = identity)
//    extends ModifiedCraftingRecipe[B] {
//
//    override def getBaseRecipe(): B = baseRecipe
//
//    override def tryCraft(inventory: CraftingInventory, world: World): Option[CraftingResult] = {
//        resultModifier.apply(inventory, world, getBaseRecipe().tryCraft(inventory, world))
//    }
//
//    override def getKey: NamespacedKey = keyModifier.apply(baseRecipe.getKey)
//}