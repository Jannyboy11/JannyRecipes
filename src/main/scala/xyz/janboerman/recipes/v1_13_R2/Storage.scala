package xyz.janboerman.recipes.v1_13_R2

import java.util.stream.Collectors
import java.util

import org.bukkit.configuration.serialization.{ConfigurationSerializable, ConfigurationSerialization}
import xyz.janboerman.recipes.RecipesPlugin
import xyz.janboerman.recipes.api.persist.RecipeStorage
import xyz.janboerman.recipes.api.persist.RecipeStorage._
import xyz.janboerman.recipes.api.recipe.Recipe
import net.minecraft.server.v1_13_R2.{ItemStack, MinecraftKey, NonNullList, RecipeItemStack}
import net.minecraft.server.v1_13_R2.RecipeItemStack.StackProvider
import xyz.janboerman.recipes.v1_13_R2.recipe.janny._
import Conversions.{toBukkitStack, toNMSStack}
import org.bukkit.craftbukkit.v1_13_R2.inventory.CraftItemStack
import xyz.janboerman.recipes.v1_13_R2.Extensions.{NmsIngredient, NmsKey, NmsNonNullList}
import xyz.janboerman.recipes.v1_13_R2.recipe.bukkit.{BukkitFurnaceToJanny, BukkitRecipeChoiceToJannyCraftingIngredient, BukkitShapedToJanny, BukkitShapelessToJanny}
import xyz.janboerman.recipes.v1_13_R2.recipe.{BetterFurnaceRecipe, BetterShapedRecipe, BetterShapelessRecipe, Shape}

import scala.collection.JavaConverters

object Storage extends RecipeStorage {

    private lazy val simpleStorage = RecipesPlugin.persistentStorage

    override def init(): Boolean = {
        simpleStorage.init()
        import simpleStorage.registerClass

        registerClass(classOf[JannyArmorDye])
        registerClass(classOf[JannyBannerAddPattern])
        registerClass(classOf[JannyBannerDuplicate])
        registerClass(classOf[JannyBookClone])
        registerClass(classOf[JannyFireworkRocket])
        registerClass(classOf[JannyFireworkStarFade])
        registerClass(classOf[JannyFireworkStar])
        registerClass(classOf[JannyMapClone])
        registerClass(classOf[JannyMapExtend])
        registerClass(classOf[JannyRepairItem])
        registerClass(classOf[JannyShieldDecoration])
        registerClass(classOf[JannyShulkerBoxColor])
        registerClass(classOf[JannyTippedArrow])

        registerClass(classOf[JannyCraftingIngredient])
        registerClass(classOf[JannyFurnaceIngredient])
        registerClass(classOf[JannyShaped])
        registerClass(classOf[JannyShapeless])
        registerClass(classOf[JannyFurnace])

        registerClass(classOf[BukkitRecipeChoiceToJannyCraftingIngredient])
        registerClass(classOf[BukkitRecipeChoiceToJannyCraftingIngredient])
        registerClass(classOf[BukkitShapedToJanny])
        registerClass(classOf[BukkitShapelessToJanny])
        registerClass(classOf[BukkitFurnaceToJanny])

        true
    }

    override def saveRecipe(recipe: Recipe with ConfigurationSerializable): Either[String, Unit] = {
        val simpleResult = simpleStorage.saveRecipe(recipe)

        if (simpleResult.isRight) {
            return simpleResult
        }

        //currently the there is nothing we can do that the simple implementation cannot do.
        //because all the nms types are configurationserializable. which is pretty dope.

        Left("Could not save recipe " + recipe)
    }

    override def loadRecipes(): Either[String, Iterator[Recipe with ConfigurationSerializable]] = {


        ??? //TODO
    }

    override def deleteRecipe(recipe: Recipe with ConfigurationSerializable): Either[String, Unit] = {


        ??? //TODO
    }


    def serializeRecipeItemStack(nms: RecipeItemStack): util.Map[String, AnyRef] = {
        if (nms == NmsIngredient.empty) return util.Map.of()
        val map = new util.HashMap[String, AnyRef]()
        nms.buildChoices()
        map.put(ChoicesString, util.Arrays.stream(nms.choices).map[CraftItemStack](toBukkitStack).collect(Collectors.toList[CraftItemStack]))
        map.put(ExactString, java.lang.Boolean.valueOf(nms.exact))
        map
    }
    def deserializeRecipeItemStack(map: util.Map[String, AnyRef]): RecipeItemStack = {
        if (map.isEmpty) return NmsIngredient.empty
        val choices = map.get(ChoicesString).asInstanceOf[util.List[org.bukkit.inventory.ItemStack]]
        val exact = java.lang.Boolean.getBoolean(map.get(ExactString).toString)
        val nmsIngredient = new RecipeItemStack(choices.stream().map[ItemStack](toNMSStack).map(new StackProvider(_)))
        nmsIngredient.exact = exact
        nmsIngredient
    }

    def serializeItemStack(nms: ItemStack): util.Map[String, AnyRef] = {
        toBukkitStack(nms).serialize()
    }
    def deserializeItemStack(map: util.Map[String, AnyRef]): ItemStack = {
        toNMSStack(ConfigurationSerialization
            .deserializeObject(map, classOf[org.bukkit.inventory.ItemStack])
            .asInstanceOf[org.bukkit.inventory.ItemStack])
    }

    def serializeMinecraftKey(nms: MinecraftKey): util.Map[String, AnyRef] = {
        val map = new util.HashMap[String, AnyRef]()
        map.put(NamespaceString, nms.getNamespace())
        map.put(KeyString, nms.getKey)
        map
    }
    def deserializeMinecraftKey(map: util.Map[String, AnyRef]): MinecraftKey = {
        val namespace = map.get(NamespaceString).asInstanceOf[String]
        val key = map.get(KeyString).asInstanceOf[String]
        NmsKey(namespace, key)
    }

    def serializeFurnaceRecipe(nms: BetterFurnaceRecipe): util.Map[String, AnyRef] = {
        val map = new util.HashMap[String, AnyRef]()
        map.put(KeyString, serializeMinecraftKey(nms.getKey))
        map.put(IngredientsString, serializeRecipeItemStack(nms.getIngredient()))
        map.put(ResultString, serializeItemStack(nms.getResult()))
        map.put(GroupString, nms.getGroup().getOrElse(""))
        map.put(ExperienceString, java.lang.Float.valueOf(nms.getExperience()))
        map.put(CookingTimeString, java.lang.Integer.valueOf(nms.getCookingTime()))
        map
    }
    def deserializeFurnaceRecipe(map: util.Map[String, AnyRef]): BetterFurnaceRecipe = {
        val key = deserializeMinecraftKey(map.get(KeyString).asInstanceOf[util.Map[String, AnyRef]])
        val ingredient = deserializeRecipeItemStack(map.get(IngredientsString).asInstanceOf[util.Map[String, AnyRef]])
        val result = deserializeItemStack(map.get(ResultString).asInstanceOf[util.Map[String, AnyRef]])
        val group = map.getOrDefault(GroupString, "").asInstanceOf[String]
        val experience = map.get(ExperienceString).toString.toFloat
        val cookingTime = map.get(CookingTimeString).toString.toInt
        BetterFurnaceRecipe(key, group, ingredient, result, experience, cookingTime)
    }

    def serializeIngredientMap(nms: Map[Char, RecipeItemStack]): util.Map[String, AnyRef] = {
        JavaConverters.mapAsJavaMap(nms.map({case (character, nmsIngredient) => (String.valueOf(character), serializeRecipeItemStack(nmsIngredient))}))
    }
    def deserializeIngredientMap(map: util.Map[String, AnyRef]): Map[Char, RecipeItemStack] = {
        JavaConverters.mapAsScalaMap(map).map { case (string: String, recipeItemStackMap: util.Map[String, AnyRef]) => (string(0), deserializeRecipeItemStack(recipeItemStackMap)) }.toMap
    }

    def serializeShape(nms: Shape): util.Map[String, AnyRef] = {
        val map = new util.HashMap[String, AnyRef]()
        map.put(PatternString, JavaConverters.bufferAsJavaList(nms.pattern.toBuffer))
        map.put(IngredientsString, serializeIngredientMap(nms.ingredientMap))
        map
    }
    def deserializeShape(map: util.Map[String, AnyRef]): Shape = {
        val pattern = JavaConverters.asScalaBuffer(map.get(PatternString).asInstanceOf[util.List[String]]).toIndexedSeq
        val ingredients = deserializeIngredientMap(map.get(IngredientsString).asInstanceOf[util.Map[String, AnyRef]])
        Shape(pattern, ingredients)
    }

    def serializeShapedRecipe(nms: BetterShapedRecipe): util.Map[String, AnyRef] = {
        val map = new util.HashMap[String, AnyRef]()
        map.put(KeyString, serializeMinecraftKey(nms.getKey))
        map.put(GroupString, nms.getGroup().getOrElse(""))
        map.put(ShapeString, serializeShape(nms.getShape()))
        map.put(ResultString, serializeItemStack(nms.getResult()))
        map
    }
    def deserializeShapedRecipe(map: util.Map[String, AnyRef]): BetterShapedRecipe = {
        val key = deserializeMinecraftKey(map.get(KeyString).asInstanceOf[util.Map[String, AnyRef]])
        val group = map.getOrDefault(GroupString, "").asInstanceOf[String]
        val shape = deserializeShape(map.get(ShapeString).asInstanceOf[util.Map[String, AnyRef]])
        val result = deserializeItemStack(map.get(ResultString).asInstanceOf[util.Map[String, AnyRef]])
        BetterShapedRecipe(key, group, shape, result)
    }

    def serializeShapelessRecipe(nms: BetterShapelessRecipe): util.Map[String, AnyRef] = {
        val map = new util.HashMap[String, AnyRef]()
        map.put(KeyString, serializeMinecraftKey(nms.getKey))
        map.put(GroupString, nms.getGroup().getOrElse(""))
        map.put(IngredientsString, nms.getIngredients().stream()
            .map[util.Map[String, AnyRef]](serializeRecipeItemStack)
            .collect(Collectors.toList[util.Map[String, AnyRef]]))
        map.put(ResultString, serializeItemStack(nms.getResult()))
        map
    }
    def deserializeShapelessRecipe(map: util.Map[String, AnyRef]): BetterShapelessRecipe = {
        val key = deserializeMinecraftKey(map.get(KeyString).asInstanceOf[util.Map[String, AnyRef]])
        val group = map.getOrDefault(GroupString, "").asInstanceOf[String]
        val ingredients: NonNullList[RecipeItemStack] = map.get(IngredientsString).asInstanceOf[util.List[util.Map[String, AnyRef]]].stream()
            .map[RecipeItemStack](deserializeRecipeItemStack)
            .collect(Collectors.toCollection[RecipeItemStack, NonNullList[RecipeItemStack]](() => NmsNonNullList.withEmpty[RecipeItemStack](NmsIngredient.empty)))
        val result = deserializeItemStack(map.get(ResultString).asInstanceOf[util.Map[String, AnyRef]])
        BetterShapelessRecipe(key, group, ingredients, result)
    }

}
