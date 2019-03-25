package xyz.janboerman.recipes.v1_13_R2

import Conversions.{toJannyRecipe, toNMSKey, toNMSRecipe}
import net.minecraft.server.v1_13_R2.{MinecraftKey, PotionBrewer, RecipeSerializer, RecipeSerializers}
import org.bukkit.craftbukkit.v1_13_R2.util.CraftMagicNumbers
import org.bukkit.plugin.Plugin
import org.bukkit.{Bukkit, Keyed, NamespacedKey}
import xyz.janboerman.recipes.{JannyImplementation, RecipesPlugin}
import xyz.janboerman.recipes.api.gui.RecipeGuiFactory
import xyz.janboerman.recipes.api.persist.RecipeStorage
import xyz.janboerman.recipes.api.recipe._
import xyz.janboerman.recipes.v1_13_R2.gui.GuiFactory
import xyz.janboerman.recipes.v1_13_R2.recipe._

import scala.collection.JavaConverters

object Impl extends JannyImplementation {

    private val supportedMappings = List("7dd4b3ec31629620c41553e5c142e454", "00ed8e5c39debc3ed194ad7c5645cc45")

    private var alreadyInitialized = false

    override def tryInitialize(): Boolean = {
        if (alreadyInitialized) return true

        if (!Bukkit.getServer.getClass.getName.equals("org.bukkit.craftbukkit.v1_13_R2.CraftServer"))
            return false

        val compileTimeMappingsVersion = supportedMappings.head

        val runTimeMappingsVersion: String = CraftMagicNumbers.INSTANCE.asInstanceOf[CraftMagicNumbers].getMappingsVersion
        if (!supportedMappings.contains(runTimeMappingsVersion)) {
            val logger = RecipesPlugin.getLogger

            logger.warning("")
            logger.warning("============================ WARNING ============================")
            logger.warning(s"Found possibly incompatible NMS mappings. ${RecipesPlugin.getName} may not work as expected.")
            logger.warning(s"Compile-time version: $compileTimeMappingsVersion" )
            logger.warning(s"Run-time version:     $runTimeMappingsVersion")
            logger.warning("============================ WARNING ============================")
            logger.warning("")

            //TODO record metrics on the NMS version?
            //TODO if 1.13.2 ever gets new mappings we should create a Map<MappingsVersion, Map<FieldSemanticId, FieldName>>

            //TODO I could think of all kinds of things that I want to collect statistics on :P
            //TODO such as Implementation name
        }

        try {

            val serializerMapField = classOf[RecipeSerializers].getDeclaredField("q")
            serializerMapField.setAccessible(true)
            val serializersMap = serializerMapField.get(null).asInstanceOf[java.util.Map[String, RecipeSerializer[_]]]

            //register custom serializers - improved nms recipes
            serializersMap.put(BetterFurnaceSerializer.getRecipeType(), BetterFurnaceSerializer)
            serializersMap.put(BetterShapedSerializer.getRecipeType(), BetterShapedSerializer)
            serializersMap.put(BetterShapelessSerializer.getRecipeType(), BetterShapelessSerializer)
            serializersMap.put(BetterMapExtendRecipe.serializer.a(), BetterMapExtendRecipe.serializer)
            serializersMap.put(BetterTippedArrowRecipe.serializer.a(), BetterTippedArrowRecipe.serializer)

            alreadyInitialized = true
            true //why is assignment not an expression in Scala?!
        } catch {
            case e: Throwable =>
                e.printStackTrace()
                false
        }
    }

    override def fromBukkitRecipe(bukkit: org.bukkit.inventory.Recipe): Recipe =
        Conversions.toJannyRecipe(bukkit)

    override def toBukkitRecipe(janny: Recipe): org.bukkit.inventory.Recipe =
        Conversions.toBukkitRecipe(janny)

    override def getRecipe(key: NamespacedKey): Recipe = {
        val iRecipe = getCraftingManager().recipes.get(toNMSKey(key))
        if (iRecipe == null) null else toJannyRecipe(iRecipe)
    }

    override def addRecipe(recipe: Recipe): Boolean = {
        if (recipe.isInstanceOf[Keyed]) {
            try {
                getCraftingManager().a(toNMSRecipe(recipe))
                true
            } catch {
                case e: IllegalStateException => false
            }
        } else {
            false
        }
    }

    def isRegistered(key: NamespacedKey): Boolean = {
        getCraftingManager().c().contains(toNMSKey(key))
    }

    override def removeRecipe(recipe: Recipe): Boolean = {
        recipe match {
            case keyedRecipe: Keyed => removeRecipe(keyedRecipe.getKey)
            case _ => false //I might implement brewing. eventually. somewhere in the far future. they are not keyed. but I can expose them as if they are?
        }
    }

    override def isRegistered(recipe: Recipe): Boolean = {
        recipe match {
            case keyedRecipe: Keyed => isRegistered(keyedRecipe.getKey)
            case _ => false //I might implement brewing recipes. eventually. somewhere in the far future. they are not keyed. but I can expose them as if they are?
        }
    }

    override def iterateRecipes(): Iterator[_ <: Recipe] = JavaConverters
        .asScalaIterator(getCraftingManager().recipes.values().iterator())
        .map(Conversions.toJannyRecipe)

    override def persist(): RecipeStorage = Storage

    def removeRecipe(namespacedKey: NamespacedKey): Boolean = removeRecipe(toNMSKey(namespacedKey))
    def removeRecipe(minecraftKey: MinecraftKey): Boolean = getCraftingManager().recipes.remove(minecraftKey) != null

    override def getGuiFactory[P <: Plugin](): RecipeGuiFactory[P] = GuiFactory.asInstanceOf[RecipeGuiFactory[P]] //TODO how to do this properly?
}
