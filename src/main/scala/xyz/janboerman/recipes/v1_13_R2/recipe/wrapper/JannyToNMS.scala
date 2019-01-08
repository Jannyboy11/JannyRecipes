package xyz.janboerman.recipes.v1_13_R2.recipe.wrapper

import com.google.gson.JsonObject
import net.minecraft.server.v1_13_R2._
import org.bukkit.craftbukkit.v1_13_R2.inventory.{CraftInventoryCrafting, CraftInventoryFurnace}
import org.bukkit.inventory.Recipe
import xyz.janboerman.recipes.api.recipe._
import xyz.janboerman.recipes.v1_13_R2.Conversions._
import xyz.janboerman.recipes.v1_13_R2.Extensions.NmsRecipe.Group
import xyz.janboerman.recipes.v1_13_R2.Extensions.{NmsNonNullList, NmsStack}
import xyz.janboerman.recipes.v1_13_R2.recipe._
import xyz.janboerman.recipes.v1_13_R2.recipe.CraftingIngredient
import xyz.janboerman.recipes.v1_13_R2.recipe.bukkit.{JannyFurnaceToBukkit, JannyShapedToBukkit, JannyShapelessToBukkit}

//abstract class to factor out some common code
abstract class JannyCraftingToNMS(janny: CraftingRecipe) extends IRecipe {

    private var lastCheck: Option[CraftingResult] = None

    override def a(iInventory: IInventory, world: World): Boolean = {
        iInventory match {
            case inventoryCrafting: InventoryCrafting =>
                val obcInventoryCrafting = new CraftInventoryCrafting(inventoryCrafting, inventoryCrafting.resultInventory)
                val bukkitWorld = toBukkitWorld(world.asInstanceOf[WorldServer])

                lastCheck = janny.tryCraft(obcInventoryCrafting, bukkitWorld)
                lastCheck.isDefined
            case _ => false
        }
    }

    override def b(iinventory: IInventory): NonNullList[ItemStack] = {
        var jannyRemainders: List[Option[_ <: org.bukkit.inventory.ItemStack]] = lastCheck.get.getRemainders()
        val remainders: NonNullList[ItemStack] = NmsNonNullList.apply(iinventory.getSize, NmsStack.empty)

        var index = 0
        while (jannyRemainders.nonEmpty) {
            val jannyRemainder: Option[_ <: org.bukkit.inventory.ItemStack] = jannyRemainders.head
            remainders.set(index, jannyRemainder.map(bukkitStack => toNMSStack(bukkitStack)).getOrElse(NmsStack.empty))

            //forces minecraft to recompute the remaining item see SlotResult#a(EntityHuman, ItemStack)
            if (jannyRemainder.isDefined) {
                val ingredientStack = iinventory.getItem(index)
                ingredientStack.setCount(0)
                iinventory.setItem(index, ingredientStack)
            }

            jannyRemainders = jannyRemainders.tail
            index += 1
        }

        remainders
    }

    override def craftItem(iInventory: IInventory): ItemStack = toNMSStack(lastCheck.get.getResultStack())
    override def getKey: MinecraftKey = toNMSKey(janny.getKey)
    override def c(): Boolean = janny.isHidden()

}

//actual implementations
case class JannyShapedToNMS(janny: ShapedRecipe) extends JannyCraftingToNMS(janny) {

    override def d(): ItemStack = toNMSStack(janny.getResult())
    override def a(): RecipeSerializer[_ <: IRecipe] = JannyShapedSerializer
    override def toBukkitRecipe: Recipe = JannyShapedToBukkit(janny)

    def getGroup: Option[Group] = janny.getGroup()
    def getShape: IndexedSeq[String] = janny.getShape()
    def getIngredients: Map[Char, _ <: CraftingIngredient] = janny.getIngredients().map({case (k,v) => (k, toNMSIngredient(v))})
    def getResult: ItemStack = toNMSStack(janny.getResult())
}

case class JannyShapelessToNMS(janny: ShapelessRecipe) extends JannyCraftingToNMS(janny) {

    override def d(): ItemStack = toNMSStack(janny.getResult)
    override def a(): RecipeSerializer[_ <: IRecipe] = JannyShapelessSerializer
    override def toBukkitRecipe: Recipe = JannyShapelessToBukkit(janny)

    def getGroup: Option[Group] = janny.getGroup()
    def getIngredients: List[_ <: CraftingIngredient] = janny.getIngredients().map(toNMSIngredient)
    def getResult: ItemStack = toNMSStack(janny.getResult())
}

object JannyFurnaceToNMS {
    def apply(janny: xyz.janboerman.recipes.api.recipe.FurnaceRecipe): JannyFurnaceToNMS = new JannyFurnaceToNMS(janny)
    def unapply(jannyWrapped: JannyFurnaceToNMS): Option[xyz.janboerman.recipes.api.recipe.FurnaceRecipe] = Some(jannyWrapped.janny)
}

class JannyFurnaceToNMS(override val janny: xyz.janboerman.recipes.api.recipe.FurnaceRecipe) extends JannySmeltingToNMS(janny) {
    override def e(): NonNullList[RecipeItemStack] = NmsNonNullList(toRecipeItemStack(toNMSIngredient(janny.getIngredient())))
    override def a(): RecipeSerializer[_ <: IRecipe] = JannyFurnaceSerializer
    override def toBukkitRecipe: Recipe = JannyFurnaceToBukkit(janny)
}

case class JannySmeltingToNMS(janny: SmeltingRecipe) extends IRecipe {
    type BukkitResult = org.bukkit.inventory.ItemStack
    private var lastCheck: Option[BukkitResult] = None

    override def a(iInventory: IInventory, world: World): Boolean = {
        iInventory match {
            case inventoryFurnace: TileEntityFurnace =>
                val obcFurnaceInventory = new CraftInventoryFurnace(inventoryFurnace)

                lastCheck = janny.trySmelt(obcFurnaceInventory)
                lastCheck.isDefined
            case _ => false
        }
    }
    override def craftItem(iInventory: IInventory): ItemStack = toNMSStack(lastCheck.get)
    //the getRemainingItems is never called by TileEntityFurnace so we don't override it.
    //that it why it is not present in the janny result
    override def d(): ItemStack = toNMSStack(janny.getResult)
    override def getKey: MinecraftKey = toNMSKey(janny.getKey)
    override def a(): RecipeSerializer[_ <: IRecipe] = DummySerializer
    override def toBukkitRecipe: Recipe = toNMSRecipe(janny).toBukkitRecipe //TODO? how to convert directly from janny to bukkit?
    override def c(): Boolean = janny.isHidden()

    def getCookingTime(): Int = janny.getCookingTime()
    def getExperience(): Float = janny.getExperience()
    def getGroup(): Option[Group] = janny.getGroup()
}

//serializers delegate to the vanilla version. cuz that's probably the easiest way to migrate this code to 1.14
object JannyShapedSerializer extends RecipeSerializer[JannyShapedToNMS] {
    private val delegateSerializer = BetterShapedSerializer
    private def toBetterShaped(janny: JannyShapedToNMS): BetterShapedRecipe =
        BetterShapedRecipe(janny.getKey,
            janny.getGroup.getOrElse(""),
            Shape(janny.getShape, janny.getIngredients.map({ case (k, v) => (k, toRecipeItemStack(v))})),
            //Shape(janny.getShape, janny.getIngredients.mapValues(toRecipeItemStack)),
            janny.getResult)

    override def a(minecraftKey: MinecraftKey, jsonObject: JsonObject): JannyShapedToNMS = ???
    override def a(minecraftKey: MinecraftKey, packetDataSerializer: PacketDataSerializer): JannyShapedToNMS = ???

    override def a(packetDataSerializer: PacketDataSerializer, t: JannyShapedToNMS): Unit =
        delegateSerializer.a(packetDataSerializer, toBetterShaped(t))
    override def a(): String = delegateSerializer.a()
}

object JannyShapelessSerializer extends RecipeSerializer[JannyShapelessToNMS] {
    private val delegateSerializer = BetterShapelessSerializer
    private def toBetterShapeless(janny: JannyShapelessToNMS): BetterShapelessRecipe =
        BetterShapelessRecipe(janny.getKey,
            janny.getGroup.getOrElse(""),
            {
                val ingredients: NonNullList[RecipeItemStack] = NmsNonNullList()
                janny.getIngredients.map(toRecipeItemStack).foreach(ingredients.add)
                ingredients
            },
            janny.getResult)

    override def a(minecraftKey: MinecraftKey, jsonObject: JsonObject): JannyShapelessToNMS = ???
    override def a(minecraftKey: MinecraftKey, packetDataSerializer: PacketDataSerializer): JannyShapelessToNMS = ???

    override def a(packetDataSerializer: PacketDataSerializer, t: JannyShapelessToNMS): Unit =
        delegateSerializer.a(packetDataSerializer, toBetterShapeless(t))
    override def a(): String = delegateSerializer.a()
}

object JannyFurnaceSerializer extends RecipeSerializer[JannyFurnaceToNMS] {
    private val delegateSerializer = BetterFurnaceSerializer
    import xyz.janboerman.recipes.v1_13_R2.Extensions.NmsRecipe
    private def toBetterFurnace(janny: JannyFurnaceToNMS): BetterFurnaceRecipe =
        BetterFurnaceRecipe(janny.getKey,
            janny.getGroup().getOrElse(""),
            janny.getIngredients().get(0),
            janny.getResult(),
            janny.getExperience(),
            janny.getCookingTime())

    override def a(minecraftKey: MinecraftKey, jsonObject: JsonObject): JannyFurnaceToNMS = ???
    override def a(minecraftKey: MinecraftKey, packetDataSerializer: PacketDataSerializer): JannyFurnaceToNMS = ???

    override def a(packetDataSerializer: PacketDataSerializer, t: JannyFurnaceToNMS): Unit =
        delegateSerializer.a(packetDataSerializer, toBetterFurnace(t))
    override def a(): String = delegateSerializer.a()
}