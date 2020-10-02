package com.janboerman.recipes.v1_13_R2.recipe.janny

import java.util

import com.janboerman.recipes.api.recipe.{CraftingIngredient => ApiCraftingIngredient, FurnaceIngredient => ApiFurnaceIngredient}
import net.minecraft.server.v1_13_R2.RecipeItemStack.StackProvider
import net.minecraft.server.v1_13_R2.{ItemStack, RecipeItemStack}
import org.bukkit.configuration.serialization.{ConfigurationSerializable, SerializableAs}
import org.bukkit.craftbukkit.v1_13_R2.inventory.CraftItemStack
import com.janboerman.recipes.v1_13_R2.Conversions.{toBukkitStack, toNMSStack}
import com.janboerman.recipes.v1_13_R2.Extensions.NmsIngredient
import com.janboerman.recipes.v1_13_R2.recipe.{CraftingIngredient, FurnaceIngredient}
import com.janboerman.recipes.v1_13_R2.Storage.{deserializeRecipeItemStack => deserializeNMS, serializeRecipeItemStack => serializeNMS}

object JannyCraftingIngredient {
    def valueOf(map: util.Map[String, AnyRef]): JannyCraftingIngredient = JannyCraftingIngredient(deserializeNMS(map))
}
@SerializableAs("JannyCraftingIngredient")
case class JannyCraftingIngredient(nms: RecipeItemStack) extends CraftingIngredient
    with ApiCraftingIngredient
    with ConfigurationSerializable {

    //nms methods
    override def apply(itemStack: ItemStack): Boolean = nms.acceptsItem(itemStack)
    override def choices: List[ItemStack] = {
        nms.buildChoices()
        nms.choices.toList
    }

    //api methods
    override def getChoices(): List[CraftItemStack] = choices.map(toBukkitStack)
    override def apply(itemStack: org.bukkit.inventory.ItemStack) = apply(toNMSStack(itemStack))
    override def getRemainingStack(itemStack: org.bukkit.inventory.ItemStack) =
        Some(toBukkitStack(super.getRemainingItemStack(toNMSStack(itemStack))))
    override def clone(): JannyCraftingIngredient = JannyCraftingIngredient({
        nms.buildChoices()
        val choicesClone = nms.choices.map(_.cloneItemStack())
        new RecipeItemStack(java.util.Arrays.stream(choicesClone).map(new StackProvider(_)))
    })

    override def serialize(): util.Map[String, AnyRef] = serializeNMS(nms)
}

object JannyFurnaceIngredient {
    def valueOf(map: util.Map[String, AnyRef]): JannyFurnaceIngredient = JannyFurnaceIngredient(deserializeNMS(map))
}
@SerializableAs("JannyFurnaceIngredient")
case class JannyFurnaceIngredient(nms: RecipeItemStack) extends FurnaceIngredient
    with ApiFurnaceIngredient
    with ConfigurationSerializable {

    //nms methods
    override def apply(itemStack: ItemStack): Boolean = nms.acceptsItem(itemStack)
    override def itemStack: ItemStack = {
        nms.buildChoices()
        nms.choices(0)
    }

    //api methods
    override def apply(itemStack: org.bukkit.inventory.ItemStack) = apply(toNMSStack(itemStack))
    override def getItemStack(): org.bukkit.inventory.ItemStack = toBukkitStack(itemStack)
    override def clone(): JannyFurnaceIngredient = JannyFurnaceIngredient({
        nms.buildChoices()
        val choicesClone = nms.choices.map(_.cloneItemStack())
        new RecipeItemStack(java.util.Arrays.stream(choicesClone).map(new StackProvider(_)))
    })

    override def serialize(): util.Map[String, AnyRef] = serializeNMS(nms)
}
