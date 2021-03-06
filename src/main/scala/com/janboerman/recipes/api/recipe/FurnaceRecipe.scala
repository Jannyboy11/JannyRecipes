package com.janboerman.recipes.api.recipe

import java.util

import org.bukkit.NamespacedKey
import org.bukkit.configuration.serialization.{ConfigurationSerializable, SerializableAs}
import org.bukkit.inventory.{FurnaceInventory, ItemStack}
import com.janboerman.recipes.api.persist.RecipeStorage._

object FurnaceRecipe {
    def unapply(arg: FurnaceRecipe): Option[(NamespacedKey, Option[String], FurnaceIngredient, ItemStack, Int, Float)] =
        Some(arg.getKey, arg.getGroup(), arg.getIngredient(), arg.getResult(), arg.getCookingTime(), arg.getExperience())

    def apply(key: NamespacedKey, ingredient: FurnaceIngredient, result: ItemStack): FurnaceRecipe =
        new SimpleFurnaceRecipe(key, ingredient, result)

    def apply(key: NamespacedKey, ingredient: FurnaceIngredient, result: ItemStack, cookingTime: Int, experience: Float): FurnaceRecipe =
        new SimpleFurnaceRecipe(key, ingredient, result, cookingTime, experience)

    def apply(key: NamespacedKey, group: Option[String], ingredient: FurnaceIngredient, result: ItemStack, cookingTime: Int, experience: Float): FurnaceRecipe =
        new SimpleFurnaceRecipe(key, group, ingredient, result, cookingTime, experience)

    def apply(key: NamespacedKey, group: Option[String], ingredient: FurnaceIngredient, result: ItemStack): FurnaceRecipe =
        apply(key, group, ingredient, result, 200, 0F)
}

trait FurnaceRecipe extends SmeltingRecipe
    with FixedIngredients
    with FixedResult {

    override def getResultStack(): ItemStack = getResult()
    override def getIngredientStacks: Iterable[ItemStack] = Seq(getIngredient().getItemStack())
    override def getType(): RecipeType = FurnaceType

    def getIngredient(): FurnaceIngredient

    override def trySmelt(furnaceInventory: FurnaceInventory): Option[ItemStack] = {
        val acceptedByIngredient = getIngredient().apply(furnaceInventory.getSmelting)
        if (acceptedByIngredient) {
            Some(getResult())
        } else {
            None
        }
    }
}

object SimpleFurnaceRecipe {
    def valueOf(map: util.Map[String, AnyRef]): SimpleFurnaceRecipe = {
        val namespacedKey = map.get(KeyString).asInstanceOf[NamespacedRecipeKey].namespacedKey
        val group = Option(map.get(GroupString).asInstanceOf[String]).filter(_.nonEmpty)
        val furnaceIngredient = map.get(IngredientsString).asInstanceOf[FurnaceIngredient]
        val result = map.get(ResultString).asInstanceOf[ItemStack]
        val cookingTime = String.valueOf(map.get(CookingTimeString)).toInt
        val experience = String.valueOf(map.get(ExperienceString)).toFloat
        new SimpleFurnaceRecipe(namespacedKey, group, furnaceIngredient, result, cookingTime, experience)
    }
}

@SerializableAs("SimpleFurnace")
class SimpleFurnaceRecipe(private val namespacedKey: NamespacedKey,
                          optionGroup: Option[String],
                          private val furnaceIngredient: FurnaceIngredient,
                          private val result: ItemStack,
                          private val cookingTime: Int,
                          private val experience: Float)
    extends FurnaceRecipe with ConfigurationSerializable {

    this.group = optionGroup.orNull

    def this(namespacedKey: NamespacedKey, furnaceIngredient: FurnaceIngredient, result: ItemStack, cookingTme: Int, experience: Float) =
        this(namespacedKey, None, furnaceIngredient, result, cookingTme, experience)
    def this(namespacedKey: NamespacedKey, furnaceIngredient: FurnaceIngredient, result: ItemStack) =
        this(namespacedKey, furnaceIngredient, result, 200, 0F)
    def this(namespacedKey: NamespacedKey, furnaceIngredient: FurnaceIngredient, result: ItemStack, cookingTime: Int) =
        this(namespacedKey, furnaceIngredient, result, cookingTime, 0F)
    def this(namespacedKey: NamespacedKey, furnaceIngredient: FurnaceIngredient, result: ItemStack, experience: Float) =
        this(namespacedKey, furnaceIngredient, result, 200, experience)

    override def getIngredient(): FurnaceIngredient = furnaceIngredient

    override def getResult(): ItemStack = if (result == null) null else result.clone()

    override def getKey(): NamespacedKey = namespacedKey

    override def getCookingTime(): Int = cookingTime

    override def getExperience(): Float = experience

    override def serialize(): util.Map[String, AnyRef] = {
        val map = new util.HashMap[String, AnyRef]()
        map.put(KeyString, new NamespacedRecipeKey(getKey))
        getGroup().foreach(map.put(GroupString, _))
        map.put(IngredientsString, getIngredient())
        map.put(ResultString, getResult())
        map.put(CookingTimeString, java.lang.Integer.valueOf(getCookingTime())) //use explicit boxing cause we need java.lang.Integer instead of scala.RichInt
        map.put(ExperienceString, java.lang.Float.valueOf(getExperience()))     //same for floats
        map
    }

    override def toString(): String = s"SimpleFurnaceRecipe{key=${getKey()}," +
        s"group=${getGroup()}," +
        s"ingredient=${getIngredient()}," +
        s"result=${getResult()}," +
        s"cookingTime=${getCookingTime()}," +
        s"experience=${getExperience()}}"
}