package xyz.janboerman.recipes.v1_13_R2.recipe.janny

import java.util.Objects

import net.minecraft.server.v1_13_R2._
import org.bukkit.NamespacedKey
import org.bukkit.craftbukkit.v1_13_R2.inventory.CraftItemStack
import org.bukkit.inventory.{CraftingInventory, FurnaceInventory, ItemStack}
import xyz.janboerman.recipes.api.recipe.{CraftingIngredient, FurnaceIngredient, FurnaceRecipe, _}
import xyz.janboerman.recipes.v1_13_R2.Conversions._
import xyz.janboerman.recipes.v1_13_R2.Extensions.{NmsInventory, NmsRecipe, ObcCraftingInventory, ObcFurnaceInventory}
import xyz.janboerman.recipes.v1_13_R2.recipe._

import scala.collection.JavaConverters

//NMS wrappers that implement the janny recipe api. similar to craftbukkit

object JannyRecipe {
    def apply(nms: IRecipe): JannyRecipe            = new JannyRecipe(nms)
    def unapply(arg: JannyRecipe): Option[IRecipe]  = Some(arg.nms)
}
class JannyRecipe(val nms: IRecipe) extends Recipe {
    override def equals(obj: scala.Any): Boolean = {
        if (obj == this) return true
        obj match {
            case that: JannyRecipe => Objects.equals(this.nms, that.nms)
            case _ => false
        }
    }
    override def hashCode(): Int = Objects.hashCode(nms)
    override def isHidden(): Boolean = nms.isHidden()
}

object JannyCrafting {
    def apply(iRecipe: IRecipe): JannyCrafting          = new JannyCrafting(iRecipe)
    def unapply(arg: JannyCrafting): Option[IRecipe]    = Some(arg.nms)
}
class JannyCrafting(iRecipe: IRecipe) extends JannyRecipe(iRecipe) with CraftingRecipe {
    lazy val key = toBukkitKey(nms.getKey)

    override def tryCraft(craftingInventory: CraftingInventory, world: org.bukkit.World): Option[CraftingResult] = {
        craftingInventory match {
            case ObcCraftingInventory(matrix @ NmsInventory(items, Some(player: EntityHuman)), resultInventory)
                if iRecipe.acceptsInput(matrix, player.getWorld) =>

                val nonnullistRemainingStacks: NonNullList[net.minecraft.server.v1_13_R2.ItemStack] = iRecipe.getRemainingStacks(matrix)
                var remaining = List[Option[CraftItemStack]]()
                for (idx <- nonnullistRemainingStacks.size() - 1 to(0, -1)) {
                    remaining = Option(toBukkitStack(nonnullistRemainingStacks.get(idx))) :: remaining
                }

                val resultStack = toBukkitStack(iRecipe.craftItem(matrix))

                Some(CraftingResult(resultStack, remaining))
            case _ => None
        }
    }

    override def getKey: NamespacedKey = key
}

object JannyFurnace {
    def apply(furnaceRecipe: BetterFurnaceRecipe): JannyFurnace  = new JannyFurnace(furnaceRecipe)
    def unapply(arg: JannyFurnace): Option[BetterFurnaceRecipe]  = Some(arg.nms.asInstanceOf[BetterFurnaceRecipe])
}
class JannyFurnace(furnaceRecipe: BetterFurnaceRecipe) extends JannyRecipe(furnaceRecipe) with FurnaceRecipe {
    lazy val key = toBukkitKey(nms.getKey)

    override def trySmelt(furnaceInventory: FurnaceInventory): Option[ItemStack] = {
        furnaceInventory match {
            case ObcFurnaceInventory(tileEntityFurnace @ NmsInventory(items, Some(player: EntityHuman)))
                if furnaceRecipe.acceptsInput(tileEntityFurnace, player.getWorld) =>
                Some(toBukkitStack(furnaceRecipe.craftItem(tileEntityFurnace)))
            case _ => None
        }
    }

    def getKey: NamespacedKey = key

    override def getResult(): ItemStack = toBukkitStack(furnaceRecipe.getResult())

    override def getIngredient(): FurnaceIngredient = JannyFurnaceIngredient(furnaceRecipe.getIngredient())
}

object JannyShaped {
    def apply(shapedRecipes: BetterShapedRecipe): JannyShaped    = new JannyShaped(shapedRecipes)
    def unapply(arg: JannyShaped): Option[BetterShapedRecipe]    = Some(arg.nms.asInstanceOf[BetterShapedRecipe])
}
object JannyShapeless {
    def apply(shapelessRecipes: BetterShapelessRecipe): JannyShapeless   = new JannyShapeless(shapelessRecipes)
    def unapply(arg: JannyShapeless): Option[BetterShapelessRecipe]      = Some(arg.nms.asInstanceOf[BetterShapelessRecipe])
}
class JannyShaped(shapedRecipes: BetterShapedRecipe)            extends JannyCrafting(shapedRecipes)    with ShapedRecipe {
    override def getGroup = shapedRecipes.getGroup()
    override def getResult() = toBukkitStack(shapedRecipes.getResult())
    override def getShape(): IndexedSeq[String] = shapedRecipes.shape.pattern
    override def getIngredients(): Map[Char, _ <: CraftingIngredient] = shapedRecipes.shape.ingredientMap.map({ case (k, v) => (k, JannyCraftingIngredient(v))})
}
class JannyShapeless(shapelessRecipes: BetterShapelessRecipe)   extends JannyCrafting(shapelessRecipes) with ShapelessRecipe {
    override def getGroup = shapelessRecipes.getGroup()
    override def getResult() = toBukkitStack(shapelessRecipes.getResult())
    override def getIngredients(): List[JannyCraftingIngredient] =
        JavaConverters.collectionAsScalaIterable(shapelessRecipes.getIngredients()).map(JannyCraftingIngredient(_)).toList
}

abstract class JannyComplex(complexRecipe: IRecipe) extends JannyCrafting(complexRecipe) with ComplexRecipe
case object JannyArmorDye           extends JannyComplex(armorDye)                  with ArmorDyeRecipe
case object JannyBannerAddPattern   extends JannyComplex(bannerAddPattern)          with BannerAddPatternRecipe
case object JannyBannerDuplicate    extends JannyComplex(bannerDuplicate)           with BannerDuplicateRecipe
case object JannyBookClone          extends JannyComplex(bannerDuplicate)           with BookCloneRecipe
case object JannyFireworkRocket     extends JannyComplex(fireworkRocket)            with FireworkRocketRecipe
case object JannyFireworkStar       extends JannyComplex(fireworkStar)              with FireworkStarRecipe
case object JannyFireworkStarFade   extends JannyComplex(fireworkStarFade)          with FireworkStarFadeRecipe
case object JannyMapClone           extends JannyComplex(mapClone)                  with MapCloneRecipe
case object JannyMapExtend          extends JannyShaped(BetterMapExtendRecipe())    with MapExtendRecipe
case object JannyRepairItem         extends JannyComplex(repairItem)                with RepairItemRecipe
case object JannyShieldDecoration   extends JannyComplex(shieldDecoration)          with ShieldDecorationRecipe
case object JannyShulkerBoxColor    extends JannyComplex(shulkerBoxColor)           with ShulkerBoxColorRecipe
case object JannyTippedArrow        extends JannyShaped(BetterTippedArrowRecipe())  with TippedArrowRecipe