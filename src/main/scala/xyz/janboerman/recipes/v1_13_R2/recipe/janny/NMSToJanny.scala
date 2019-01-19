package xyz.janboerman.recipes.v1_13_R2.recipe.janny

import java.util
import java.util.Objects

import net.minecraft.server.v1_13_R2._
import org.bukkit.NamespacedKey
import org.bukkit.configuration.serialization.{ConfigurationSerializable, SerializableAs}
import org.bukkit.craftbukkit.v1_13_R2.inventory.CraftItemStack
import org.bukkit.inventory.{CraftingInventory, FurnaceInventory, ItemStack}
import xyz.janboerman.recipes.api.recipe.{CraftingIngredient, FurnaceIngredient, FurnaceRecipe, _}
import xyz.janboerman.recipes.v1_13_R2.Conversions._
import xyz.janboerman.recipes.v1_13_R2.Extensions.{NmsInventory, NmsRecipe, ObcCraftingInventory, ObcFurnaceInventory}
import xyz.janboerman.recipes.v1_13_R2.Storage
import xyz.janboerman.recipes.v1_13_R2.recipe._

import scala.collection.JavaConverters

//NMS wrappers that implement the janny recipe api. similar to craftbukkit

object JannyRecipe {
    def apply(nms: IRecipe): JannyRecipe            = new JannyRecipe(nms) {
        override def getType(): RecipeType = UnknownType
    }
    def unapply(arg: JannyRecipe): Option[IRecipe]  = Some(arg.nms)
}
abstract class JannyRecipe(val nms: IRecipe) extends Recipe {
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
    def apply(iRecipe: IRecipe): JannyCrafting          = new JannyCrafting(iRecipe) {
        override def getType(): RecipeType = UnknownType
    }
    def unapply(arg: JannyCrafting): Option[IRecipe]    = Some(arg.nms)
}
abstract class JannyCrafting(iRecipe: IRecipe) extends JannyRecipe(iRecipe) with CraftingRecipe {
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
    def valueOf(map: util.Map[String, AnyRef]): JannyFurnace = {
        new JannyFurnace(Storage.deserializeFurnaceRecipe(map))
    }
}
@SerializableAs("JannyFurnace")
class JannyFurnace(furnaceRecipe: BetterFurnaceRecipe) extends JannyRecipe(furnaceRecipe)
    with FurnaceRecipe
    with ConfigurationSerializable {
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

    override def getExperience(): Float = furnaceRecipe.getExperience()

    override def getCookingTime(): Int = furnaceRecipe.getCookingTime()

    override def getGroup(): Option[String] = furnaceRecipe.getGroup()

    override def serialize(): util.Map[String, AnyRef] = {
        Storage.serializeFurnaceRecipe(furnaceRecipe)
    }
}

object JannyShaped {
    def apply(shapedRecipes: BetterShapedRecipe): JannyShaped   = new JannyShaped(shapedRecipes)
    def unapply(arg: JannyShaped): Option[BetterShapedRecipe]   = Some(arg.nms.asInstanceOf[BetterShapedRecipe])
    def valueOf(map: util.Map[String, AnyRef]): JannyShaped     = new JannyShaped(Storage.deserializeShapedRecipe(map))
}
object JannyShapeless {
    def apply(shapelessRecipes: BetterShapelessRecipe): JannyShapeless  = new JannyShapeless(shapelessRecipes)
    def unapply(arg: JannyShapeless): Option[BetterShapelessRecipe]     = Some(arg.nms.asInstanceOf[BetterShapelessRecipe])
    def valueOf(map: util.Map[String, AnyRef]): JannyShapeless          = new JannyShapeless(Storage.deserializeShapelessRecipe(map))
}
@SerializableAs("JannyShaped")
class JannyShaped(shapedRecipes: BetterShapedRecipe) extends JannyCrafting(shapedRecipes)
    with ShapedRecipe
    with ConfigurationSerializable {

    override def getGroup = shapedRecipes.getGroup()
    override def getResult() = toBukkitStack(shapedRecipes.getResult())
    override def getShape(): IndexedSeq[String] = shapedRecipes.shape.pattern
    override def getIngredients(): Map[Char, _ <: CraftingIngredient] = shapedRecipes.shape.ingredientMap.map({ case (k, v) => (k, JannyCraftingIngredient(v))})

    override def serialize(): util.Map[String, AnyRef] = Storage.serializeShapedRecipe(shapedRecipes)
}
@SerializableAs("JannyShapeless")
class JannyShapeless(shapelessRecipes: BetterShapelessRecipe) extends JannyCrafting(shapelessRecipes)
    with ShapelessRecipe
    with ConfigurationSerializable {
    override def getGroup = shapelessRecipes.getGroup()
    override def getResult() = toBukkitStack(shapelessRecipes.getResult())
    override def getIngredients(): List[JannyCraftingIngredient] =
        JavaConverters.collectionAsScalaIterable(shapelessRecipes.getIngredients()).map(JannyCraftingIngredient(_)).toList

    override def serialize(): util.Map[String, AnyRef] = Storage.serializeShapelessRecipe(shapelessRecipes)
}

abstract class JannyComplex(complexRecipe: IRecipe) extends JannyCrafting(complexRecipe) with ComplexRecipe

//TODO metaprogramming?
object JannyArmorDye {
    def valueOf(map: util.Map[String, AnyRef]): JannyArmorDye = new JannyArmorDye
}
@SerializableAs("JannyArmorDye")
case class JannyArmorDye()           extends JannyComplex(armorDye)                  with ArmorDyeRecipe {
    override def serialize(): util.Map[String, AnyRef] = util.Collections.emptyMap()
}


object JannyBannerAddPattern {
    def valueOf(map: util.Map[String, AnyRef]): JannyBannerAddPattern = new JannyBannerAddPattern()
}
@SerializableAs("JannyBannerAddPattern")
case class JannyBannerAddPattern()  extends JannyComplex(bannerAddPattern)          with BannerAddPatternRecipe {
    override def serialize(): util.Map[String, AnyRef] = util.Collections.emptyMap()
}


object JannyBannerDuplicate {
   def valueOf(map: util.Map[String, AnyRef]): JannyBannerDuplicate = new JannyBannerDuplicate()
}
@SerializableAs("JannyBannerDuplicate")
case class JannyBannerDuplicate()   extends JannyComplex(bannerDuplicate)           with BannerDuplicateRecipe {
    override def serialize(): util.Map[String, AnyRef] = util.Collections.emptyMap()
}


object JannyBookClone {
    def valueOf(map: util.Map[String, AnyRef]): JannyBookClone = new JannyBookClone()
}
@SerializableAs("JannyBookClone")
case class JannyBookClone()         extends JannyComplex(bookClone)           with BookCloneRecipe {
    override def serialize(): util.Map[String, AnyRef] = util.Collections.emptyMap()
}


object JannyFireworkRocket {
    def valueOf(map: util.Map[String, AnyRef]): JannyFireworkRocket = new JannyFireworkRocket()
}
@SerializableAs("JannyFireworkRocket")
case class JannyFireworkRocket()    extends JannyComplex(fireworkRocket)            with FireworkRocketRecipe {
    override def serialize(): util.Map[String, AnyRef] = util.Collections.emptyMap()
}


object JannyFireworkStar {
    def valueOf(map: util.Map[String, AnyRef]): JannyFireworkStar = new JannyFireworkStar()
}
@SerializableAs("JannyFireworkStar")
case class JannyFireworkStar()      extends JannyComplex(fireworkStar)              with FireworkStarRecipe {
    override def serialize(): util.Map[String, AnyRef] = util.Collections.emptyMap()
}


object JannyFireworkStarFade {
    def valueOf(map: util.Map[String, AnyRef]): JannyFireworkStarFade = new JannyFireworkStarFade()
}
@SerializableAs("JannyFireworkStarFade")
case class JannyFireworkStarFade()  extends JannyComplex(fireworkStarFade)          with FireworkStarFadeRecipe {
    override def serialize(): util.Map[String, AnyRef] = util.Collections.emptyMap()
}


object JannyMapClone {
    def valueOf(map: util.Map[String, AnyRef]): JannyMapClone = new JannyMapClone()
}
@SerializableAs("JannyMapClone")
case class JannyMapClone()          extends JannyComplex(mapClone)                  with MapCloneRecipe {
    override def serialize(): util.Map[String, AnyRef] = util.Collections.emptyMap()
}


object JannyMapExtend {
    def valueOf(map: util.Map[String, AnyRef]): JannyMapExtend = new JannyMapExtend()
}
@SerializableAs("JannyMapExtend")
case class JannyMapExtend()         extends JannyShaped(BetterMapExtendRecipe())    with MapExtendRecipe {
    override def serialize(): util.Map[String, AnyRef] = util.Collections.emptyMap()
}


object JannyRepairItem {
    def valueOf(map: util.Map[String, AnyRef]): JannyRepairItem = new JannyRepairItem()
}
@SerializableAs("JannyRepairItem")
case class JannyRepairItem()        extends JannyComplex(repairItem)                with RepairItemRecipe {
    override def serialize(): util.Map[String, AnyRef] = util.Collections.emptyMap()
}


object JannyShieldDecoration {
    def valueOf(map: util.Map[String, AnyRef]): JannyShieldDecoration = new JannyShieldDecoration()
}
@SerializableAs("JannyShieldDecoration")
case class JannyShieldDecoration()  extends JannyComplex(shieldDecoration)          with ShieldDecorationRecipe {
    override def serialize(): util.Map[String, AnyRef] = util.Collections.emptyMap()
}


object JannyShulkerBoxColor {
    def valueOf(map: util.Map[String, AnyRef]): JannyShulkerBoxColor = new JannyShulkerBoxColor()
}
@SerializableAs("JannyShulkerBoxColor")
case class JannyShulkerBoxColor()   extends JannyComplex(shulkerBoxColor)           with ShulkerBoxColorRecipe {
    override def serialize(): util.Map[String, AnyRef] = util.Collections.emptyMap()
}


object JannyTippedArrow {
    def valueOf(map: util.Map[String, AnyRef]): JannyTippedArrow = new JannyTippedArrow()
}
@SerializableAs("JannyTippedArrow")
case class JannyTippedArrow()       extends JannyShaped(BetterTippedArrowRecipe())  with TippedArrowRecipe {
    override def serialize(): util.Map[String, AnyRef] = util.Collections.emptyMap()
}