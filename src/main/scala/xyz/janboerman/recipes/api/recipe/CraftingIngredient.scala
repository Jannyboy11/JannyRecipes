package xyz.janboerman.recipes.api.recipe

import java.util

import org.bukkit.Material
import org.bukkit.configuration.serialization.{ConfigurationSerializable, SerializableAs}
import org.bukkit.inventory.ItemStack
import xyz.janboerman.recipes.api.persist.SerializableList
import xyz.janboerman.recipes.api.persist.RecipeStorage._

object CraftingIngredient {
    private lazy val EmptyIngredient = new CraftingIngredient {
        override def getChoices(): List[_ <: ItemStack] = Nil
        override def apply(input: ItemStack): Boolean = input == null
        override def clone(): CraftingIngredient = this
    }

    def empty(): CraftingIngredient = EmptyIngredient

    def apply(firstChoice: ItemStack, choices: ItemStack*): CraftingIngredient = if (choices.isEmpty) apply(firstChoice) else new SimpleCraftingIngredient(firstChoice :: choices.toList)
    def apply(firstChoice: Material, choices: Material*): CraftingIngredient = if (choices.isEmpty) apply(firstChoice) else new SimpleCraftingIngredient((firstChoice :: choices.toList).map(new ItemStack(_)))
    def apply(ingredient: ItemStack): CraftingIngredient = if (ingredient == null) EmptyIngredient else new SimpleCraftingIngredient(ingredient :: Nil)
    def apply(ingredient: Material): CraftingIngredient = if (ingredient == null) EmptyIngredient else new SimpleCraftingIngredient(new ItemStack(ingredient) :: Nil)

    def unapply(arg: CraftingIngredient): Option[List[_ <: ItemStack]] = Some(arg.getChoices())
}

trait CraftingIngredient extends Ingredient {

    def getChoices(): List[_ <: ItemStack]

    /**
      * Get the stack that remains on the spot of the ingredient's ItemStack.
      * This method can be overridden for ingredients that need to leave an item behind with a different material from the ingredient.
      * Vanilla Minecraft uses this for lava buckets, water buckets, milk buckets and dragon breath potions.
      * Ingredients that want to use this vanilla behaviour shouldn't override this method.
      *
      * @param itemStack the ingredient stack.
      * @return None if the item that remains is the same as the ingredient,
      *         or Some ItemStack that new item remains in the inventory instead.
      */
    def getRemainingStack(itemStack: ItemStack): Option[_ <: ItemStack] = {
        if (itemStack == null) return None

        itemStack.getType match {
            case Material.LAVA_BUCKET | Material.WATER_BUCKET | Material.MILK_BUCKET =>
                Some({val clone = itemStack.clone(); clone.setType(Material.BUCKET); clone})
            case Material.DRAGON_BREATH =>
                Some({val clone = itemStack.clone(); clone.setType(Material.GLASS_BOTTLE); clone})

            case _ => Some({val clone = itemStack.clone(); clone.setAmount(clone.getAmount - 1); clone})
        }
    }

    override def apply(itemStack: ItemStack): Boolean = {
        getChoices().exists(choice =>
            if (itemStack == null) choice == null
            else choice != null && choice.getType == itemStack.getType)
    }

    override def clone(): CraftingIngredient = {
        val choicesClone = getChoices().map(itemStack => itemStack.clone())
        new CraftingIngredient {
            override def getChoices(): List[_ <: ItemStack] = choicesClone
        }
    }

}

object SimpleCraftingIngredient {
    def valueOf(map: util.Map[String, AnyRef]): SimpleCraftingIngredient = {
        val serializableList = map.get(ChoicesString).asInstanceOf[SerializableList[ItemStack]]
        new SimpleCraftingIngredient(serializableList.list)
    }
}

@SerializableAs("SimpleCraftingIngredient")
case class SimpleCraftingIngredient(private val choices: List[_ <: ItemStack]) extends CraftingIngredient
    with ConfigurationSerializable {

    override def getChoices(): List[_ <: ItemStack] = choices
    override def clone(): SimpleCraftingIngredient = new SimpleCraftingIngredient(choices.map(stack => if (stack == null) null else stack.clone()))
    override def serialize(): util.Map[String, AnyRef] = {
        val map = new util.HashMap[String, AnyRef]()
        map.put(ChoicesString, new SerializableList(getChoices()))
        map
    }

    override def toString(): String = s"SimpleCraftingIngredient{choices=$choices}"
    override def hashCode(): Int = java.util.Objects.hashCode(choices)
    override def equals(obj: Any): Boolean = obj match {
        case SimpleCraftingIngredient(thatChoices) => this.choices == thatChoices
        case _ => false
    }
}

object ExactCraftingIngredient {
    def valueOf(map: util.Map[String, AnyRef]): ExactCraftingIngredient = {
        val choices = map.get(ChoicesString).asInstanceOf[SerializableList[ItemStack]]
        new ExactCraftingIngredient(choices.list.asInstanceOf[List[ItemStack]])
    }
}

@SerializableAs("ExactCraftingIngredient")
case class ExactCraftingIngredient(private val choices: List[_ <: ItemStack]) extends CraftingIngredient
    with ConfigurationSerializable {

    override def getChoices(): List[_ <: ItemStack] = choices
    override def clone(): ExactCraftingIngredient = new ExactCraftingIngredient(choices.map(stack => if (stack == null) null else stack.clone()))
    override def serialize(): util.Map[String, AnyRef] = {
        val map = new util.HashMap[String, AnyRef]()
        map.put(ChoicesString, new SerializableList[ItemStack](getChoices()))
        map
    }
    override def apply(itemStack: ItemStack): Boolean = choices.contains(itemStack)

    override def toString(): String = s"ExactCraftingIngredient{choices=$choices}"
    override def hashCode(): Int = java.util.Objects.hashCode(choices)
    override def equals(obj: Any): Boolean = obj match {
        case ExactCraftingIngredient(thatChoices) => this.choices == thatChoices
        case _ => false
    }
}