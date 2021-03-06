package com.janboerman.recipes.v1_13_R2.recipe.bukkit

import java.util
import java.util.stream.Collectors

import org.bukkit.configuration.serialization.{ConfigurationSerializable, SerializableAs}
import org.bukkit.inventory.RecipeChoice.{ExactChoice, MaterialChoice}
import org.bukkit.{Material, NamespacedKey, World, inventory}
import org.bukkit.inventory.{FurnaceRecipe => _, ShapedRecipe => _, ShapelessRecipe => _, _}
import com.janboerman.recipes.api.recipe._
import com.janboerman.recipes.v1_13_R2.Extensions.{NmsInventory, ObcCraftingInventory}

import scala.collection.JavaConverters
import BukkitRecipeChoice._
import com.janboerman.recipes.api.persist.SerializableList
import com.janboerman.recipes.api.recipe.{CraftingIngredient, CraftingResult, FurnaceRecipe, ShapedRecipe, ShapelessRecipe}
import com.janboerman.recipes.api.persist.RecipeStorage._
import com.janboerman.recipes.api.persist.SerializableList

object BukkitShapedToJanny {
    def valueOf(map: util.Map[String, AnyRef]): BukkitShapedToJanny = {
        val key = map.get(KeyString).asInstanceOf[NamespacedRecipeKey].namespacedKey
        val result = map.get(ResultString).asInstanceOf[ItemStack]
        val ingredients = JavaConverters.mapAsScalaMap(map.get(IngredientsString).asInstanceOf[util.Map[String, BukkitRecipeChoiceToJannyCraftingIngredient]])
            .map {case (string: String, ingredient: BukkitRecipeChoiceToJannyCraftingIngredient) => (string(0), ingredient)}
        val pattern = JavaConverters.asScalaBuffer(map.get(PatternString).asInstanceOf[util.List[String]]).toIndexedSeq
        val group = map.getOrDefault(GroupString, "").asInstanceOf[String]
        val bukkitRecipe = new inventory.ShapedRecipe(key, result)
        bukkitRecipe.setGroup(group)
        bukkitRecipe.shape(pattern.toArray: _*)
        for ((c, ingr) <- ingredients) {
            bukkitRecipe.setIngredient(c, ingr.asInstanceOf[BukkitRecipeChoiceToJannyCraftingIngredient].bukkit)
        }
        BukkitShapedToJanny(bukkitRecipe)
    }
}
@SerializableAs("BukkitShaped")
case class BukkitShapedToJanny(bukkit: org.bukkit.inventory.ShapedRecipe) extends ShapedRecipe with ConfigurationSerializable {
    override def getResult(): ItemStack = bukkit.getResult

    override def getShape(): IndexedSeq[String] = bukkit.getShape

    override def getIngredients(): Map[Char, BukkitRecipeChoiceToJannyCraftingIngredient] = {
        val mutableBukkitMap = JavaConverters.mapAsScalaMap(bukkit.getChoiceMap)

        var map = Map[Char, BukkitRecipeChoiceToJannyCraftingIngredient]()
        mutableBukkitMap.foreach({case (character, recipeChoice) =>
            map += character.charValue() -> BukkitRecipeChoiceToJannyCraftingIngredient(recipeChoice)
        })
        map
    }

    override def tryCraft(craftingInventory: CraftingInventory, world: World): Option[CraftingResult] = {
        val (inventoryWidth, inventoryHeight) = craftingInventory match {
            case ObcCraftingInventory((matrix, result)) => (matrix.getWidth, matrix.getHeight)
            case _ => (3, 3)
        }

        val shape = getShape()
        val maxAddX = inventoryWidth - shape.head.length
        val maxAddY = inventoryHeight - shape.size

        for (addX <- 0 to maxAddX) {
            for (addY <- 0 to maxAddY) {
                for (mirrored <- Seq(false, true)) {
                    val successResult = matrixMatch(craftingInventory, inventoryWidth, inventoryHeight, addX, addY, mirrored) match {
                        //Scala can have protected methods in traits, unlike java interfaces :)
                        case Some(remainders) => Some(CraftingResult(getResult, remainders))
                        case _ => None
                    }

                    if (successResult.isDefined) return successResult
                }
            }
        }

        None
    }

    override def getKey: NamespacedKey = bukkit.getKey
    override def getGroup(): Option[String] = Option(bukkit.getGroup).filter(_.nonEmpty)

    override def serialize(): util.Map[String, AnyRef] = {
        val map = new util.HashMap[String, AnyRef]()
        map.put(KeyString, new NamespacedRecipeKey(getKey))
        map.put(PatternString, JavaConverters.bufferAsJavaList(getShape().toBuffer))
        map.put(IngredientsString, JavaConverters.mapAsJavaMap(getIngredients().map {case (c, ingr) => (String.valueOf(c), ingr)}))
        map.put(ResultString, getResult())
        map.put(GroupString, getGroup().getOrElse(""))
        map
    }
}

object BukkitShapelessToJanny {
    def valueOf(map: util.Map[String, AnyRef]): BukkitShapelessToJanny = {
        val key = map.get(KeyString).asInstanceOf[NamespacedRecipeKey].namespacedKey
        val ingredients = map.get(IngredientsString).asInstanceOf[SerializableList[BukkitRecipeChoiceToJannyCraftingIngredient]].list
        val result = map.get(ResultString).asInstanceOf[ItemStack]
        val group = map.getOrDefault(GroupString, "").asInstanceOf[String]
        val bukkitRecipe = new inventory.ShapelessRecipe(key, result)
        bukkitRecipe.setGroup(group)
        ingredients.map(_.bukkit).foreach(bukkitRecipe.addIngredient)
        BukkitShapelessToJanny(bukkitRecipe)
    }
}
@SerializableAs("BukkitShapeless")
case class BukkitShapelessToJanny(bukkit: org.bukkit.inventory.ShapelessRecipe) extends ShapelessRecipe with ConfigurationSerializable {
    override def getResult(): ItemStack = bukkit.getResult

    override def getIngredients(): List[BukkitRecipeChoiceToJannyCraftingIngredient] = {
        JavaConverters.asScalaBuffer(bukkit.getChoiceList)
            .map(BukkitRecipeChoiceToJannyCraftingIngredient(_))
            .toList
    }

    override def getKey: NamespacedKey = bukkit.getKey
    override def getGroup(): Option[String] = Option(bukkit.getGroup).filter(_.nonEmpty)

    override def serialize(): util.Map[String, AnyRef] = {
        val map = new util.HashMap[String, AnyRef]()
        map.put(KeyString, new NamespacedRecipeKey(getKey))
        map.put(IngredientsString, new SerializableList(getIngredients()))
        map.put(ResultString, getResult())
        map.put(GroupString, getGroup().getOrElse(""))
        map
    }
}

object BukkitFurnaceToJanny {
    def valueOf(map: util.Map[String, AnyRef]): BukkitFurnaceToJanny = {
        val key = map.get(KeyString).asInstanceOf[NamespacedRecipeKey].namespacedKey
        val ingredients = map.get(IngredientsString).asInstanceOf[BukkitRecipeChoiceToJannyFurnaceIngredient].bukkit
        val result = map.get(ResultString).asInstanceOf[ItemStack]
        val group = map.getOrDefault(GroupString, "").asInstanceOf[String]
        val experience = map.get(ExperienceString).toString.toFloat
        val cookingTime = map.get(CookingTimeString).toString.toInt
        val bukkitRecipe = new inventory.FurnaceRecipe(key, result, ingredients, experience, cookingTime)
        bukkitRecipe.setGroup(group)
        BukkitFurnaceToJanny(bukkitRecipe)
    }
}
@SerializableAs("BukkitFurnace")
case class BukkitFurnaceToJanny(bukkit: org.bukkit.inventory.FurnaceRecipe) extends FurnaceRecipe with ConfigurationSerializable { self =>
    override def getResult(): ItemStack = bukkit.getResult

    override def getIngredient(): BukkitRecipeChoiceToJannyFurnaceIngredient = new BukkitRecipeChoiceToJannyFurnaceIngredient(bukkit.getInputChoice) {
        override def getItemStack: ItemStack = self.bukkit.getInput
    }

    override def trySmelt(furnaceInventory: FurnaceInventory): Option[ItemStack] = {
        val acceptedByIngredient = getIngredient().apply(furnaceInventory.getSmelting)
        if (acceptedByIngredient) {
            Some(getResult())
        } else {
            None
        }
    }

    override def getKey: NamespacedKey = bukkit.getKey
    override def getGroup(): Option[String] = Option(bukkit.getGroup).filter(_.nonEmpty)
    override def getExperience(): Float = bukkit.getExperience
    override def getCookingTime(): Int = bukkit.getCookingTime

    override def serialize(): util.Map[String, AnyRef] = {
        val map = new util.HashMap[String, AnyRef]()
        map.put(KeyString, new NamespacedRecipeKey(getKey))
        map.put(IngredientsString, getIngredient())
        map.put(ResultString, getResult())
        map.put(GroupString, getGroup().getOrElse(""))
        map.put(ExperienceString, java.lang.Float.valueOf(getExperience()))
        map.put(CookingTimeString, java.lang.Integer.valueOf(getCookingTime()))
        map
    }
}

object BukkitRecipeChoiceToJannyCraftingIngredient {
    def valueOf(map: util.Map[String, AnyRef]): BukkitRecipeChoiceToJannyCraftingIngredient = {
        BukkitRecipeChoiceToJannyCraftingIngredient(deserializeRecipeChoice(map))
    }
}
@SerializableAs("BukkitCraftingIngredient")
case class BukkitRecipeChoiceToJannyCraftingIngredient(bukkit: RecipeChoice) extends CraftingIngredient
    with ConfigurationSerializable {
    override def getChoices(): List[_ <: ItemStack] = bukkit match {
        case materialChoice: MaterialChoice => JavaConverters.asScalaBuffer(materialChoice.getChoices)
                .toList
                .map(new ItemStack(_))
        case exactChoice: ExactChoice => List(exactChoice.getItemStack)
        case _ => List()
    }

    override def apply(itemStack: ItemStack): Boolean = bukkit.test(itemStack)

    override def clone(): BukkitRecipeChoiceToJannyCraftingIngredient = BukkitRecipeChoiceToJannyCraftingIngredient(cloneRecipeChoice(bukkit))

    override def serialize(): util.Map[String, AnyRef] = serializeRecipeChoice(bukkit)
}

object BukkitRecipeChoiceToJannyFurnaceIngredient {
    def valueOf(map: util.Map[String, AnyRef]): BukkitRecipeChoiceToJannyFurnaceIngredient = {
        BukkitRecipeChoiceToJannyFurnaceIngredient(deserializeRecipeChoice(map))
    }
}
@SerializableAs("BukkitFurnaceIngredient")
case class BukkitRecipeChoiceToJannyFurnaceIngredient(bukkit: RecipeChoice) extends FurnaceIngredient
    with ConfigurationSerializable {
    @Deprecated
    override def getItemStack(): ItemStack = bukkit match {
        case materialChoice: MaterialChoice =>
            val choices = materialChoice.getChoices()
            if (choices.isEmpty) {
                new ItemStack(Material.AIR)
            } else if (choices.size() == 1) {
                new ItemStack(choices.get(0))
            } else {
                val stack = new ItemStack(choices.get(0))
                stack.setDurability(Short.MaxValue)
                stack
            }
        case exactChoice: ExactChoice => exactChoice.getItemStack()
        case _ => new ItemStack(Material.AIR)
    }

    override def apply(itemStack: ItemStack): Boolean = bukkit.test(itemStack)

    override def clone(): BukkitRecipeChoiceToJannyFurnaceIngredient = BukkitRecipeChoiceToJannyFurnaceIngredient(cloneRecipeChoice(bukkit))

    override def serialize(): util.Map[String, AnyRef] = serializeRecipeChoice(bukkit)
}


object BukkitRecipeChoice {
    val IngredientType = "ingredient-type"
    val Choices = "choices"
    val MaterialChoice = "material-choice"
    val ExactChoice = "exact-choice"
    val Empty = ""

    def serializeRecipeChoice(recipeChoice: RecipeChoice): java.util.Map[String, AnyRef] = {
        val (ingredientType, choices) = recipeChoice match {
            case mc: MaterialChoice => (MaterialChoice, mc.getChoices.stream.map[ItemStack](new ItemStack(_)).collect(Collectors.toList[ItemStack]))
            case ec: ExactChoice    => (ExactChoice, util.Collections.singletonList[ItemStack](ec.getItemStack))
            case _                  => (Empty, util.Collections.emptyList())
        }
        val map = new util.HashMap[String, AnyRef]()
        map.put(IngredientType, ingredientType)
        map.put(Choices, choices)
        map
    }
    def deserializeRecipeChoice(map: util.Map[String, AnyRef]): RecipeChoice = {
        val ingredientType = map.get(IngredientType).asInstanceOf[String]
        val choices = map.get(Choices).asInstanceOf[util.List[ItemStack]]
        ingredientType match {
            case MaterialChoice => new MaterialChoice(choices.stream().map[Material](_.getType).collect(Collectors.toList[Material]))
            case ExactChoice    => new ExactChoice(choices.get(0))
            case _              => new RecipeChoice {
                override def getItemStack: ItemStack = null
                override def test(t: ItemStack): Boolean = false
                override def clone(): RecipeChoice = this
            }
        }
    }
}