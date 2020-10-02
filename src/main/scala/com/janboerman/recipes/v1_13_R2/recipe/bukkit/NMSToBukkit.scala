package com.janboerman.recipes.v1_13_R2.recipe.bukkit

import java.util

import com.janboerman.recipes.v1_13_R2.recipe.{BetterFurnaceRecipe, BetterShapedRecipe, BetterShapelessRecipe}
import org.bukkit.NamespacedKey
import org.bukkit.craftbukkit.v1_13_R2.inventory.{CraftFurnaceRecipe, CraftShapedRecipe, CraftShapelessRecipe}
import org.bukkit.inventory.{ItemStack, RecipeChoice}
import com.janboerman.recipes.v1_13_R2
import com.janboerman.recipes.v1_13_R2.Conversions.{toBukkitKey, toBukkitStack}
import com.janboerman.recipes.v1_13_R2.recipe.{BetterFurnaceRecipe, BetterShapedRecipe, BetterShapelessRecipe}

case class BetterCraftShapedRecipe(mojang: BetterShapedRecipe) extends CraftShapedRecipe(toBukkitStack(mojang.getResult()), mojang) {
    override def addToCraftingManager(): Unit = v1_13_R2.getCraftingManager().recipes.putIfAbsent(mojang.getKey, mojang)

    override def getResult: ItemStack = toBukkitStack(mojang.getResult())

    @Deprecated
    override def getIngredientMap: util.Map[Character, ItemStack] = {
        val map = new util.HashMap[Character, ItemStack]()
        mojang.getShape().ingredientMap.foreach({case (key, ingredient) => map.put(Character.valueOf(key), {
            ingredient.buildChoices()
            if (ingredient.choices.isEmpty) {
                null
            } else if (ingredient.choices.length == 1) {
                toBukkitStack(ingredient.choices(0))
            } else {
                val cloneStack = ingredient.choices(0).cloneItemStack()
                val bukkitStack = toBukkitStack(cloneStack)
                bukkitStack.setDurability(Short.MaxValue)
                bukkitStack
            }
        })})
        map
    }

    override def getChoiceMap: util.Map[Character, RecipeChoice] = {
        val map = new util.HashMap[Character, RecipeChoice]()
        mojang.getShape().ingredientMap.foreach({case (key, ingredient) => map.put(Character.valueOf(key), BetterRecipeChoice(ingredient))})
        map
    }

    override def getShape: Array[String] = mojang.getShape().pattern.toArray

    override def getKey: NamespacedKey = toBukkitKey(mojang.getKey)

    override def getGroup: String = mojang.getGroup().getOrElse("")

}

case class BetterCraftShapelessRecipe(mojang: BetterShapelessRecipe) extends CraftShapelessRecipe(toBukkitStack(mojang.getResult()), mojang) {
    override def addToCraftingManager(): Unit = v1_13_R2.getCraftingManager().recipes.putIfAbsent(mojang.getKey, mojang)

    override def getResult: ItemStack = toBukkitStack(mojang.getResult())

    @Deprecated
    override def getIngredientList: util.List[ItemStack] = {
        val list = new util.ArrayList[ItemStack]()
        mojang.getIngredients().forEach(ingredient => list.add({ingredient.buildChoices()
            if (ingredient.choices.isEmpty) {
                null
            } else if (ingredient.choices.length == 1) {
                toBukkitStack(ingredient.choices(0))
            } else {
                val cloneStack = ingredient.choices(0).cloneItemStack()
                val bukkitStack = toBukkitStack(cloneStack)
                bukkitStack.setDurability(Short.MaxValue)
                bukkitStack
            }}))
        list
    }

    override def getChoiceList: util.List[RecipeChoice] = {
        val list = new util.ArrayList[RecipeChoice]()
        mojang.getIngredients().forEach(ingredient => list.add(BetterRecipeChoice(ingredient)))
        list
    }

    override def getKey: NamespacedKey = toBukkitKey(mojang.getKey)

    override def getGroup: String = mojang.getGroup().getOrElse("")
}

case class BetterCraftFurnaceRecipe(mojang: BetterFurnaceRecipe)
    extends CraftFurnaceRecipe(toBukkitKey(mojang.getKey),
        toBukkitStack(mojang.getResult),
        BetterRecipeChoice(mojang.getIngredient()),
        mojang.getExperience(),
        mojang.getCookingTime) {

    override def addToCraftingManager(): Unit = v1_13_R2.getCraftingManager().recipes.putIfAbsent(mojang.getKey, mojang)

    override def getResult: ItemStack = toBukkitStack(mojang.getResult())

    override def getInput: ItemStack = {
        val ingredient = mojang.getIngredient()
        ingredient.buildChoices()
        if (ingredient.choices.isEmpty) {
            null
        } else if (ingredient.choices.length == 1) {
            toBukkitStack(ingredient.choices(0))
        } else {
            val cloneStack = ingredient.choices(0).cloneItemStack()
            val bukkitStack = toBukkitStack(cloneStack)
            bukkitStack.setDurability(Short.MaxValue)
            bukkitStack
        }
    }

    override def getInputChoice: RecipeChoice = BetterRecipeChoice(mojang.getIngredient())

    override def getCookingTime: Int = mojang.getCookingTime()

    override def getExperience: Float = mojang.getExperience()

    override def getKey: NamespacedKey = toBukkitKey(mojang.getKey)

    override def getGroup: String = mojang.getGroup().getOrElse("")
}
