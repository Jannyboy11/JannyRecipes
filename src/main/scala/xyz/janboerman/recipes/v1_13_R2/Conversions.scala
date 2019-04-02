package xyz.janboerman.recipes.v1_13_R2

import java.util
import java.util.stream.StreamSupport
import java.util.{Spliterator, Spliterators}

import com.google.gson.JsonObject
import net.minecraft.server.v1_13_R2
import net.minecraft.server.v1_13_R2._
import org.bukkit.craftbukkit.v1_13_R2.CraftWorld
import org.bukkit.{NamespacedKey, World, inventory}
import org.bukkit.craftbukkit.v1_13_R2.entity.CraftPlayer
import org.bukkit.craftbukkit.v1_13_R2.inventory._
import org.bukkit.entity.Player
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.RecipeChoice.MaterialChoice

import scala.collection.JavaConverters
import xyz.janboerman.recipes.Extensions.BukkitKey
import xyz.janboerman.recipes.RecipesPlugin
import xyz.janboerman.recipes.api.MaterialUtils._
import xyz.janboerman.recipes.api.recipe.{FurnaceRecipe, CraftingIngredient => _, FurnaceIngredient => _, _}
import xyz.janboerman.recipes.v1_13_R2.Extensions._
import xyz.janboerman.recipes.v1_13_R2.recipe._
import xyz.janboerman.recipes.v1_13_R2.recipe.bukkit._
import xyz.janboerman.recipes.v1_13_R2.recipe.janny._
import xyz.janboerman.recipes.v1_13_R2.recipe.wrapper._

object Conversions {

    // ======================= NMS <-> Bukkit ======================

    def toBukkitKey(minecraftKey: MinecraftKey): NamespacedKey = minecraftKey match {
        case NmsKey(namespace, key) => new NamespacedKey(namespace, key)
    }
    def toNMSKey(namespacedKey: NamespacedKey): MinecraftKey = namespacedKey match {
        case BukkitKey(namespace, key) => new MinecraftKey(namespace, key)
    }

    def toNMSPlayer(player: Player): EntityPlayer = player.asInstanceOf[CraftPlayer].getHandle
    def toBukkitPlayer(entityPlayer: EntityPlayer): CraftPlayer = entityPlayer.getBukkitEntity

    def toNMSWorld(world: World): WorldServer = world.asInstanceOf[CraftWorld].getHandle
    def toBukkitWorld(worldServer: WorldServer): World = worldServer.getWorld

    def toNMSStack(bukkit: org.bukkit.inventory.ItemStack): ItemStack = bukkit match {
        case craftItemStack: CraftItemStack => {
            val handleField = classOf[CraftItemStack].getDeclaredField("handle")
            handleField.setAccessible(true)
            handleField.get(craftItemStack).asInstanceOf[ItemStack]
        }
        case _ => CraftItemStack.asNMSCopy(bukkit)
    }
    def toBukkitStack(itemStack: ItemStack): CraftItemStack = CraftItemStack.asCraftMirror(itemStack)

    //crafting ingredient
    def toRecipeItemStack(craftingIngredient: CraftingIngredient): RecipeItemStack = craftingIngredient match {
        case JannyCraftingIngredient(nms) => nms
        case _ => //best effort - just a regular RecipeItemStack that uses the same choices list
            new RecipeItemStack(StreamSupport.stream(Spliterators
                .spliteratorUnknownSize(JavaConverters.asJavaIterator(craftingIngredient.choices.iterator), Spliterator.SIZED), false)
                .map(new RecipeItemStack.StackProvider(_)))
    }
    def toNMSIngredient(craftingIngredient: xyz.janboerman.recipes.api.recipe.CraftingIngredient): CraftingIngredient = craftingIngredient match {
        case janny: JannyCraftingIngredient => janny
        case _ => JannyToNMSCraftingIngredient(craftingIngredient)
    }
    //TODO implement conversions from and to RecipeChoice (MaterialChoice, ExactChoice) (BetterRecipeChoice, JannyCraftingIngredientToRecipeChoice, also janny nms versions: CraftingIngredient, FurnaceIngredient)

    //furnace ingredient
    def toRecipeItemStack(furnaceIngredient: FurnaceIngredient): RecipeItemStack = furnaceIngredient match {
        case JannyFurnaceIngredient(nms) => nms
        case _ => NmsIngredient(furnaceIngredient.itemStack.getItem)
    }
    def toNMSIngredient(furnaceIngredient: xyz.janboerman.recipes.api.recipe.FurnaceIngredient): FurnaceIngredient = furnaceIngredient match {
        case janny: JannyFurnaceIngredient => janny
        case _ => JannyToNMSFurnaceIngredient(furnaceIngredient);
    }


    def toNMSInventory(inventory: Inventory): IInventory = {
        inventory match {
            case craftInventory: CraftInventory => craftInventory.getInventory
            case pluginImplementation => new InventoryWrapper(pluginImplementation)
        }
    }
    def toBukkitInventory(iInventory: IInventory): Inventory = {
        iInventory match {
            case wrapper: InventoryWrapper => {
                val wrappedInventoryField = classOf[InventoryWrapper].getDeclaredField("inventory")
                wrappedInventoryField.setAccessible(true)
                wrappedInventoryField.get(wrapper).asInstanceOf[Inventory]
            }
            case furnace: TileEntityFurnace => new CraftInventoryFurnace(furnace)
            case crafting: InventoryCrafting => new CraftInventoryCrafting(crafting, crafting.resultInventory)
            case beacon: TileEntityBeacon => new CraftInventoryBeacon(beacon)
            case doubleChest: InventoryLargeChest => new CraftInventoryDoubleChest(doubleChest)
            case merchant: InventoryMerchant => new CraftInventoryMerchant(merchant)
            case player: PlayerInventory => new CraftInventoryPlayer(player)

            //fallback - try the open inventory (used for brewer/anvil/enchanting table)
            case openInventory if {
                val viewers = openInventory.getViewers
                if (viewers == null || viewers.isEmpty) {
                    false
                } else {
                    val viewer = openInventory.getViewers.get(0)
                    val view = viewer.getOpenInventory
                    view != null && view.getTopInventory != null
                }
            } => openInventory.getViewers.get(0).getOpenInventory.getTopInventory

            //we don't whether we need CraftInventoryHorse or CraftInventoryLlama since that is
            //determined by the entity (CraftHorse or CraftLlama)
            case horse: InventoryHorseChest => new CraftInventoryAbstractHorse(horse)

            //fallback - unopened unknown inventory
            case unknown => new CraftInventory(unknown)
        }
    }


    // ======================= NMS <-> Janny ======================

    def toJannyRecipe(nms: IRecipe): Recipe = {
        //TODO allow other plugins to register custom IRecipe type predicates?
        //TODO modified recipe? do i apply modifiers to iRecipe? probably should
        nms match {
            //janny wrappers //TODO make a common superclass JannyToNMS TODO figure out why I have not done this yet. xd
            case JannyShapedToNMS(janny) => janny
            case JannyShapelessToNMS(janny) => janny
            case JannyFurnaceToNMS(janny) => janny
            case JannySmeltingToNMS(janny) => janny

            //special cases
            case _: RecipeArmorDye                                  => new JannyArmorDye()
            case _: RecipeBannerAdd                                 => new JannyBannerAddPattern()
            case _: RecipeBannerDuplicate                           => new JannyBannerDuplicate()
            case _: RecipeBookClone                                 => new JannyBookClone()
            case _: RecipeFireworks                                 => new JannyFireworkRocket()
            case _: RecipeFireworksStar                             => new JannyFireworkStar()
            case _: RecipeFireworksFade                             => new JannyFireworkStarFade()
            case _: RecipeMapClone                                  => new JannyMapClone()
            case _: RecipeMapExtend | _: BetterMapExtendRecipe      => new JannyMapExtend()
            case _: RecipeRepair                                    => new JannyRepairItem()
            case _: RecipiesShield                                  => new JannyShieldDecoration()
            case _: RecipeShulkerBox                                => new JannyShulkerBoxColor()
            case _: RecipeTippedArrow | _: BetterTippedArrowRecipe  => new JannyTippedArrow()

            //normal cases
            case shaped: BetterShapedRecipe => JannyShaped(shaped)
            case shapeless: BetterShapelessRecipe => JannyShapeless(shapeless)
            case furnace: BetterFurnaceRecipe => JannyFurnace(furnace)

            //fallback //TODO allow plugins to register their own conversion? (using typeclasses :) ?)
            case iRecipe: IRecipe => JannyRecipe(iRecipe)
        }
    }

    def toNMSRecipe(janny: Recipe): IRecipe = {
        janny match {
            case nmsWrapper: JannyRecipe => nmsWrapper.nms
                //TODO modified recipes?

            //special cases
            case _: ArmorDyeRecipe => armorDye
            case _: BannerAddPatternRecipe => bannerAddPattern
            case _: BannerDuplicateRecipe => bannerDuplicate
            case _: BookCloneRecipe => bookClone
            case _: FireworkRocketRecipe => fireworkRocket
            case _: FireworkStarFadeRecipe => fireworkStarFade
            case _: FireworkStarRecipe => fireworkStar
            case _: MapCloneRecipe => mapClone
            case _: MapExtendRecipe => mapExtending
            case _: RepairItemRecipe => repairItem
            case _: ShieldDecorationRecipe => shieldDecoration
            case _: ShulkerBoxColorRecipe => shulkerBoxColor
            case _: TippedArrowRecipe => tippedArrow

            //normal cases
            case jannyShaped: ShapedRecipe => JannyShapedToNMS(jannyShaped)
            case jannyShapeless: ShapelessRecipe => JannyShapelessToNMS(jannyShapeless)
            case jannyFurnace: xyz.janboerman.recipes.api.recipe.FurnaceRecipe => JannyFurnaceToNMS(jannyFurnace)
            case jannySmelting: SmeltingRecipe => JannySmeltingToNMS(jannySmelting);

            //last attempt at preserving the 'craftingrecipe' information - this case can be triggered because of modifiers
            case jannyCrafting: CraftingRecipe => JannyUnknownCraftingToNMS(jannyCrafting)

            //fallback //TODO allow plugins to register their own conversion?
            case _ => DummyIRecipe
        }
    }

    object DummyIRecipe extends IRecipe {
        override def a(iInventory: IInventory, world: v1_13_R2.World): Boolean = false  //matches
        override def craftItem(iInventory: IInventory): ItemStack = NmsStack.empty      //craft
        override def d(): ItemStack = NmsStack.empty                                    //result
        override def getKey: MinecraftKey = NmsKey(RecipesPlugin.getName, "dummy")      //key
        override def toBukkitRecipe: inventory.Recipe = () => null                      //bukkit recipe
        override def c(): Boolean = true                                                //hidden
        override def a(): RecipeSerializer[_ <: IRecipe] = DummySerializer              //serializer
    }

    object DummySerializer extends RecipeSerializer[IRecipe] {
        override def a(minecraftKey: MinecraftKey, jsonObject: JsonObject): IRecipe = DummyIRecipe                      //deserializeFromJson
        override def a(minecraftKey: MinecraftKey, packetDataSerializer: PacketDataSerializer): IRecipe = DummyIRecipe  //deserializeFromPacket
        override def a(packetDataSerializer: PacketDataSerializer, t: IRecipe): Unit = ()                               //serializeToPacket
        override def a(): String = ""                                                                                   //serializerType
    }


    // ======================= Bukkit <-> Janny ======================

    def toJannyRecipe(bukkit: org.bukkit.inventory.Recipe): Recipe = {
        bukkit match {
            //janny wrappers
            case JannyComplexToBukkit(janny) => janny
            case JannyShapedToBukkit(janny) => janny
            case JannyShapelessToBukkit(janny) => janny
            case JannyFurnaceToBukkit(janny) => janny

            //mojang wrappers
            case BetterCraftShapedRecipe(nms) => JannyShaped(nms)
            case BetterCraftShapelessRecipe(nms) => JannyShapeless(nms)
            case BetterCraftFurnaceRecipe(nms) => JannyFurnace(nms)

            //regular bukkit
            case shapedRecipe: org.bukkit.inventory.ShapedRecipe => BukkitShapedToJanny(shapedRecipe)
            case shapelessRecipe: org.bukkit.inventory.ShapelessRecipe => BukkitShapelessToJanny(shapelessRecipe)
            case furnaceRecipe: org.bukkit.inventory.FurnaceRecipe => BukkitFurnaceToJanny(furnaceRecipe)

            //fallback //TODO throw an exception instead? or an empty shapeless recipe?
            case _ => new Recipe {
                override def isHidden(): Boolean = true
                override def getType(): RecipeType = UnknownType
            }
        }
    }

    def toBukkitRecipe(janny: Recipe): org.bukkit.inventory.Recipe = {
        janny match {
            //nms wrapper
            case JannyRecipe(nms) => nms.toBukkitRecipe

            //bukkit wrapper
            case BukkitShapedToJanny(bukkit) => bukkit
            case BukkitShapelessToJanny(bukkit) => bukkit
            case BukkitFurnaceToJanny(bukkit) => bukkit

            //complex recipe cases
            case janny: ArmorDyeRecipe =>
                val recipe = new inventory.ShapelessRecipe(janny.getKey, new org.bukkit.inventory.ItemStack(org.bukkit.Material.LEATHER_HELMET))
                recipe.addIngredient(new MaterialChoice(dyesIngredient))
                recipe
            case janny: BannerAddPatternRecipe =>
                val recipe = new inventory.ShapelessRecipe(janny.getKey, new inventory.ItemStack(org.bukkit.Material.WHITE_BANNER))
                recipe.addIngredient(new MaterialChoice(bannerIngredient))

                recipe.addIngredient(new MaterialChoice(dyesIngredient))
                recipe.addIngredient(new MaterialChoice(dyesIngredient))
                recipe.addIngredient(new MaterialChoice(dyesIngredient))

                val optionalDyesList = new util.ArrayList(dyesIngredient)
                optionalDyesList.add(org.bukkit.Material.AIR)
                recipe.addIngredient(new MaterialChoice(optionalDyesList))
                recipe.addIngredient(new MaterialChoice(optionalDyesList))
                recipe.addIngredient(new MaterialChoice(optionalDyesList))
                recipe.addIngredient(new MaterialChoice(optionalDyesList))
                recipe.addIngredient(new MaterialChoice(optionalDyesList))
                recipe
            case janny: BannerDuplicateRecipe =>
                val recipe = new inventory.ShapelessRecipe(janny.getKey, new inventory.ItemStack(org.bukkit.Material.WHITE_BANNER))
                recipe.addIngredient(new MaterialChoice(bannerIngredient))
                recipe.addIngredient(new MaterialChoice(bannerIngredient))
                recipe
            case janny: BookCloneRecipe =>
                val recipe = new inventory.ShapelessRecipe(janny.getKey, new inventory.ItemStack(org.bukkit.Material.WRITTEN_BOOK))
                recipe.addIngredient(org.bukkit.Material.WRITABLE_BOOK)
                recipe.addIngredient(org.bukkit.Material.WRITTEN_BOOK)
                recipe
            case janny: FireworkRocketRecipe =>
                val recipe = new inventory.ShapelessRecipe(janny.getKey, new inventory.ItemStack(org.bukkit.Material.FIREWORK_ROCKET))
                recipe.addIngredient(org.bukkit.Material.PAPER)
                recipe.addIngredient(org.bukkit.Material.GUNPOWDER)
                recipe.addIngredient(new MaterialChoice(util.Arrays.asList(org.bukkit.Material.AIR, org.bukkit.Material.FIREWORK_STAR)))
                recipe
            case janny: FireworkStarFadeRecipe =>
                val recipe = new inventory.ShapelessRecipe(janny.getKey, new inventory.ItemStack(org.bukkit.Material.FIREWORK_STAR))
                recipe.addIngredient(org.bukkit.Material.FIREWORK_STAR)
                recipe.addIngredient(new MaterialChoice(dyesIngredient))
                recipe
            case janny: FireworkStarRecipe =>
                val recipe = new inventory.ShapelessRecipe(janny.getKey, new inventory.ItemStack(org.bukkit.Material.FIREWORK_STAR))
                recipe.addIngredient(org.bukkit.Material.GUNPOWDER)

                val optionalDyesList = new util.ArrayList(dyesIngredient)
                optionalDyesList.add(org.bukkit.Material.AIR)
                recipe.addIngredient(new MaterialChoice(optionalDyesList))

                val optionalFireworkShapeList = new util.ArrayList(fireworkShapesIngredient)
                optionalFireworkShapeList.add(org.bukkit.Material.AIR)
                recipe.addIngredient(new MaterialChoice(optionalFireworkShapeList))

                recipe.addIngredient(new MaterialChoice(util.Arrays.asList(org.bukkit.Material.AIR, org.bukkit.Material.DIAMOND)))
                recipe.addIngredient(new MaterialChoice(util.Arrays.asList(org.bukkit.Material.AIR, org.bukkit.Material.GLOWSTONE_DUST)))

                recipe
            case janny: MapCloneRecipe =>
                val recipe = new inventory.ShapelessRecipe(janny.getKey, new inventory.ItemStack(org.bukkit.Material.FILLED_MAP, 2))
                recipe.addIngredient(org.bukkit.Material.MAP)
                recipe.addIngredient(org.bukkit.Material.FILLED_MAP)
                recipe
            case janny: MapExtendRecipe =>
                val recipe = new inventory.ShapedRecipe(janny.getKey, new inventory.ItemStack(org.bukkit.Material.FILLED_MAP))
                recipe.shape("PPP", "PMP", "PPP")
                recipe.setIngredient('P', org.bukkit.Material.PAPER)
                recipe.setIngredient('M',org.bukkit.Material.FILLED_MAP)
                recipe
            case janny: RepairItemRecipe =>
                val recipe = new inventory.ShapelessRecipe(janny.getKey, new inventory.ItemStack(org.bukkit.Material.LEATHER_HELMET))
                recipe.addIngredient(new MaterialChoice(breakableItems))
                recipe.addIngredient(new MaterialChoice(breakableItems))
                recipe
            case janny: ShieldDecorationRecipe =>
                val recipe = new inventory.ShapelessRecipe(janny.getKey, new inventory.ItemStack(org.bukkit.Material.SHIELD))
                recipe.addIngredient(org.bukkit.Material.SHIELD)
                recipe.addIngredient(new MaterialChoice(bannerIngredient))
                recipe
            case janny: ShulkerBoxColorRecipe =>
                val recipe = new inventory.ShapelessRecipe(janny.getKey, new inventory.ItemStack(org.bukkit.Material.WHITE_SHULKER_BOX))
                recipe.addIngredient(new MaterialChoice(shulkerBoxIngredient))
                recipe.addIngredient(new MaterialChoice(dyesIngredient))
                recipe
            case janny: TippedArrowRecipe =>
                val recipe = new inventory.ShapedRecipe(janny.getKey, new inventory.ItemStack(org.bukkit.Material.TIPPED_ARROW))
                recipe.shape("AAA", "ALA", "AAA")
                recipe.setIngredient('A', org.bukkit.Material.ARROW)
                recipe.setIngredient('L', org.bukkit.Material.LINGERING_POTION)
                recipe

            case janny: ComplexRecipe => JannyComplexToBukkit(janny)

            //regular recipe cases
            case janny: ShapedRecipe => JannyShapedToBukkit(janny)
            case janny: ShapelessRecipe => JannyShapelessToBukkit(janny)
            case janny: FurnaceRecipe => JannyFurnaceToBukkit(janny)

            case _ => () => null //fallback //TODO throw an exception instead? an empty shapeless recipe maybe?
        }
    }

}
