package xyz.janboerman.recipes.api.recipe

import java.util

import org.bukkit.Material
import org.bukkit.configuration.serialization.{ConfigurationSerializable, SerializableAs}
import org.bukkit.inventory.ItemStack
import xyz.janboerman.recipes.api.persist.RecipeStorage.SerializableList
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
    def apply(ingredient: ItemStack): CraftingIngredient = new SimpleCraftingIngredient(ingredient :: Nil)
    def apply(ingredient: Material): CraftingIngredient = new SimpleCraftingIngredient(new ItemStack(ingredient) :: Nil)

    def unapply(arg: CraftingIngredient): Option[List[_ <: ItemStack]] = Some(arg.getChoices())
}

trait CraftingIngredient extends Ingredient {

    def getChoices(): List[_ <: ItemStack]

    /**
      * Get the stack that remains on the spot of the ingredient's ItemStack.
      * This method can be overridden for ingredients that need to leave an item behind with a different material from the ingredient.
      * Vanilla minecraft uses this for lava buckets, water buckets, milk buckets and dragon breath potions.
      * Ingredients that want to use this vanilla behaviour shouldn't override this method.
      *
      * @param itemStack the ingredient stack.
      * @return None if the item that remains is the same as the ingredient,
      *         or Some ItemStack that remains in the inventory instead.
      */
    def getRemainingStack(itemStack: ItemStack): Option[_ <: ItemStack] = {
        if (itemStack == null) return None

        import xyz.janboerman.recipes.Extensions.ItemStackExtensions
        itemStack.getType match {
            case Material.LAVA_BUCKET | Material.WATER_BUCKET | Material.MILK_BUCKET =>
                Some(itemStack.clone()(Material.BUCKET))
            case Material.DRAGON_BREATH =>
                Some(itemStack.clone()(Material.GLASS_BOTTLE))

            case _ => Some(itemStack.clone())
        }
    }

    override def apply(itemStack: ItemStack): Boolean = if (itemStack == null) false else getChoices().exists(_.getType == itemStack.getType)

    override def clone(): CraftingIngredient = {
        val choicesClone = getChoices().map(itemStack => itemStack.clone())
        new CraftingIngredient {
            override def getChoices(): List[_ <: ItemStack] = choicesClone
        }
    }

}

object SimpleCraftingIngredient {
    def valueOf(map: util.Map[String, AnyRef]): SimpleCraftingIngredient = {
        val serializableList = map.get(ChoicesString).asInstanceOf[SerializableList[_ <: ItemStack]]
        new SimpleCraftingIngredient(serializableList.list)
    }
}

@SerializableAs("SimpleCraftingIngredient")
class SimpleCraftingIngredient(private val choices: List[_ <: ItemStack]) extends CraftingIngredient
    with ConfigurationSerializable {
    override def getChoices(): List[_ <: ItemStack] = choices
    override def clone(): SimpleCraftingIngredient = new SimpleCraftingIngredient(choices.map(stack => if (stack == null) null else stack.clone()))

    override def serialize(): util.Map[String, AnyRef] = {
        val map = new util.HashMap[String, AnyRef]()
        map.put(ChoicesString, new SerializableList(choices))
        map
    }
}
