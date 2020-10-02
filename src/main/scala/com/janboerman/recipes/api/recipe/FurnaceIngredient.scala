package com.janboerman.recipes.api.recipe

import java.util
import com.janboerman.recipes.api.persist.RecipeStorage._
import org.bukkit.configuration.serialization.{ConfigurationSerializable, SerializableAs}
import org.bukkit.inventory.ItemStack

object FurnaceIngredient {
    def unapply(arg: FurnaceIngredient): Option[ItemStack] = Some(arg.getItemStack())

    def apply(matcher: ItemStack): FurnaceIngredient = new SimpleFurnaceIngredient(matcher)
}

trait FurnaceIngredient extends Ingredient with ItemIngredient {

    def getItemStack(): ItemStack

    override def apply(itemStack: ItemStack): Boolean = {
        val matcher = getItemStack()
        if (matcher == null) itemStack == null
        else itemStack != null && matcher.getType == itemStack.getType
    }

    override def clone(): FurnaceIngredient = {
        val cloneStack = getItemStack().clone()
        new FurnaceIngredient {
            override def getItemStack(): ItemStack = cloneStack
        }
    }

    override def firstItem(): ItemStack = getItemStack()

}

object SimpleFurnaceIngredient {
    def valueOf(map: util.Map[String, AnyRef]): SimpleFurnaceIngredient = {
        val matcher = map.get(ItemStackString).asInstanceOf[ItemStack]
        new SimpleFurnaceIngredient(matcher)
    }
}

@SerializableAs("SimpleFurnaceIngredient")
class SimpleFurnaceIngredient(private val matcher: ItemStack) extends FurnaceIngredient
    with ConfigurationSerializable {
    override def getItemStack(): ItemStack = if (matcher == null) null else matcher.clone()
    override def clone(): SimpleFurnaceIngredient = new SimpleFurnaceIngredient(if (matcher == null) null else matcher.clone)

    override def serialize(): util.Map[String, AnyRef] = {
        val map = new util.HashMap[String, AnyRef]()
        map.put(ItemStackString, getItemStack())
        map
    }
}