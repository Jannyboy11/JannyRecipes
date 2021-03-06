package com.janboerman.recipes.v1_13_R2.recipe.bukkit

import java.util
import java.util.Spliterators
import java.util.stream.StreamSupport

import com.janboerman.recipes.api.recipe
import com.janboerman.recipes.api.recipe.{ComplexRecipe, CraftingIngredient, ExactCraftingIngredient, SimpleCraftingIngredient}
import com.janboerman.recipes.v1_13_R2.Conversions
import net.minecraft.server.v1_13_R2.RecipeItemStack
import net.minecraft.server.v1_13_R2.RecipeItemStack.StackProvider
import org.bukkit.{Material, NamespacedKey}
import org.bukkit.craftbukkit.v1_13_R2.inventory.CraftRecipe
import org.bukkit.inventory.RecipeChoice.MaterialChoice
import org.bukkit.inventory._
import com.janboerman.recipes.api.recipe.{ComplexRecipe, ExactCraftingIngredient, SimpleCraftingIngredient, CraftingIngredient => ApiCraftingIngredient, FurnaceRecipe => ApiFurnaceRecipe, ShapedRecipe => ApiShapedRecipe, ShapelessRecipe => ApiShapelessRecipe}
import com.janboerman.recipes.v1_13_R2.recipe.janny.JannyCraftingIngredient
import com.janboerman.recipes.v1_13_R2.{Conversions, Impl}

import scala.collection.JavaConverters

//classes that wrap janny api types and 'implement' the bukkit api

case class JannyFurnaceToBukkit(janny: recipe.FurnaceRecipe) extends FurnaceRecipe (
    janny.getKey,
    janny.getResult,
    janny.getIngredient.getItemStack.getType,
    janny.getExperience,
    janny.getCookingTime) with CraftRecipe {

    override def getKey(): NamespacedKey = janny.getKey
    override def getInput: ItemStack = janny.getIngredient.getItemStack
    override def getResult: ItemStack = janny.getResult
    override def getGroup: String = janny.getGroup.getOrElse("")
    override def getExperience(): Float = janny.getExperience
    override def getCookingTime: Int = janny.getCookingTime

    //TODO override setters?

    override def addToCraftingManager(): Unit = Impl.addRecipe(janny)
}

case class JannyShapedToBukkit(janny: recipe.ShapedRecipe) extends ShapedRecipe(janny.getKey, janny.getResult()) with CraftRecipe {
    shape(janny.getShape: _*)
    for ((key, ingredient) <- janny.getIngredients()) {
        setIngredient(key, JannyCraftingIngredientToRecipeChoice(ingredient))
    }

    override def getKey: NamespacedKey = janny.getKey
    override def getGroup: String = janny.getGroup.getOrElse("")
    override def getResult: ItemStack = janny.getResult
    override def getShape: Array[String] = janny.getShape.toArray
    override def getChoiceMap: util.Map[Character, RecipeChoice] = JavaConverters.mapAsJavaMap(janny.getIngredients().map({case (k,v) => (k, JannyCraftingIngredientToRecipeChoice(v))}))
    @Deprecated
    override def getIngredientMap: util.Map[Character, ItemStack] = JavaConverters.mapAsJavaMap(janny.getIngredients().map({case (k, v) => {
        val choices = v.getChoices()
        val itemStack: ItemStack = if (choices.isEmpty) null else if (choices.length == 1) choices(0) else {
            val clone: ItemStack = choices(0).clone()
            clone.setDurability(Short.MaxValue)
            clone
        }
        (k, itemStack)
    }}))

    //TODO override setters?

    override def addToCraftingManager(): Unit = Impl.addRecipe(janny)
    override def toNMS(bukkit: RecipeChoice, requireNonEmpty: Boolean): RecipeItemStack = bukkit match {
        case bukkitToJanny: JannyCraftingIngredientToRecipeChoice => bukkitToJanny.toRecipeItemStack
        case _ => super.toNMS(bukkit, requireNonEmpty)
    }
}

case class JannyShapelessToBukkit(janny: recipe.ShapelessRecipe) extends ShapelessRecipe(janny.getKey, janny.getResult()) with CraftRecipe {
    for (ingredient <- janny.getIngredients()) {
        addIngredient(JannyCraftingIngredientToRecipeChoice(ingredient))
    }

    override def getKey: NamespacedKey = janny.getKey
    override def getGroup: String = janny.getGroup.getOrElse("")
    override def getResult(): ItemStack = janny.getResult
    override def getChoiceList(): util.List[RecipeChoice] = JavaConverters.bufferAsJavaList(janny.getIngredients().map(JannyCraftingIngredientToRecipeChoice).toBuffer)
    @Deprecated
    override def getIngredientList: util.List[ItemStack] = JavaConverters.bufferAsJavaList(janny.getIngredients().map(craftingIngredient => {
        val choices = craftingIngredient.getChoices()
        val itemStack: ItemStack = if (choices.isEmpty) null else if (choices.length == 1) choices(0) else {
            val clone: ItemStack = choices(0).clone()
            clone.setDurability(Short.MaxValue)
            clone
        }
        itemStack
    }).toBuffer)

    //TODO override setters?

    override def addToCraftingManager(): Unit = Impl.addRecipe(janny)
    override def toNMS(bukkit: RecipeChoice, requireNonEmpty: Boolean): RecipeItemStack = bukkit match {
        case bukkitToJanny: JannyCraftingIngredientToRecipeChoice => bukkitToJanny.toRecipeItemStack
        case _ => super.toNMS(bukkit, requireNonEmpty)
    }
}

//used for pattern matching only - only matched if there I missed a complex recipe type
case class JannyComplexToBukkit(janny: ComplexRecipe) extends ShapelessRecipe(janny.getKey, new ItemStack(Material.AIR) /*cannot use null unfortunately.*/)

case class JannyCraftingIngredientToRecipeChoice(janny: CraftingIngredient) extends MaterialChoice({
    val bukkitChoices: util.List[org.bukkit.Material] = new util.ArrayList[Material]()
    for (jannyChoice <- janny.getChoices()) bukkitChoices.add(jannyChoice.getType)
    bukkitChoices
}) {
    @Deprecated
    override def getItemStack: ItemStack = {
        val choices = janny.getChoices()
        if (choices.isEmpty) null else if (choices.length == 1) choices(0) else {
            val clone: ItemStack = choices(0).clone()
            clone.setDurability(Short.MaxValue)
            clone
        }
    }

    override def clone(): MaterialChoice = {
        janny match {
            case simple: SimpleCraftingIngredient => JannyCraftingIngredientToRecipeChoice(simple.clone())
            case exact: ExactCraftingIngredient => JannyCraftingIngredientToRecipeChoice(exact.clone())
            case _ => super.clone()
        }
    }

    override def test(t: ItemStack): Boolean = janny.apply(t)

    def toRecipeItemStack(): RecipeItemStack = {
        janny match {
            case JannyCraftingIngredient(recipeItemStack) => recipeItemStack
            case exactIngredient: ExactCraftingIngredient => //TODO this is really just a best effort
                val javaIterator: java.util.Iterator[ItemStack] = JavaConverters.asJavaIterator(exactIngredient.getChoices().iterator)
                val javaStream: java.util.stream.Stream[ItemStack] = StreamSupport.stream(Spliterators.spliteratorUnknownSize(javaIterator, 0), false)
                val nmsStackStream: java.util.stream.Stream[net.minecraft.server.v1_13_R2.ItemStack] = javaStream.map(Conversions.toNMSStack)
                val recipeItemStack = new RecipeItemStack(nmsStackStream.map(new StackProvider(_)))
                recipeItemStack.exact = true
                recipeItemStack
            case ingredient => //TODO unfortunately RecipeItemStack is final so we cannot extend it and override its behaviour like we could in 1.12.
                val javaIterator: java.util.Iterator[ItemStack] = JavaConverters.asJavaIterator(ingredient.getChoices().iterator)
                val javaStream: java.util.stream.Stream[ItemStack] = StreamSupport.stream(Spliterators.spliteratorUnknownSize(javaIterator, 0), false)
                val nmsStackStream: java.util.stream.Stream[net.minecraft.server.v1_13_R2.ItemStack] = javaStream.map(Conversions.toNMSStack)
                new RecipeItemStack(nmsStackStream.map(new StackProvider(_)))
        }
    }
}