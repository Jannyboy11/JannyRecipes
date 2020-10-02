package com.janboerman.recipes.api.recipe

import java.util

import com.janboerman.recipes.api.persist.SerializableList
import org.bukkit.configuration.serialization.{ConfigurationSerializable, SerializableAs}
import org.bukkit.{NamespacedKey, World}
import org.bukkit.inventory.{CraftingInventory, ItemStack}
import com.janboerman.recipes.api.persist.RecipeStorage._
import com.janboerman.recipes.api.persist.SerializableList

object ShapelessRecipe {
    def unapply(arg: ShapelessRecipe): Option[(NamespacedKey, Option[String], List[_ <: CraftingIngredient], ItemStack)] =
        Some((arg.getKey, arg.getGroup(), arg.getIngredients(), arg.getResult()))

    def apply(namespacedKey: NamespacedKey, group: Option[String], ingredients: List[_ <: CraftingIngredient], result: ItemStack): ShapelessRecipe =
        new SimpleShapelessRecipe(namespacedKey, group, ingredients, result)

    def apply(namespacedKey: NamespacedKey, ingredients: List[_ <: CraftingIngredient], result: ItemStack): ShapelessRecipe =
        new SimpleShapelessRecipe(namespacedKey, ingredients, result)
}

trait ShapelessRecipe extends CraftingRecipe with Grouped
    with FixedIngredients
    with FixedResult {

    override def getResultStack(): ItemStack = getResult()
    override def getIngredientStacks: Iterable[ItemStack] = getIngredients().flatMap(_.getChoices())
    override def getType(): RecipeType = ShapelessType

    def getResult(): ItemStack

    def getIngredients(): List[_ <: CraftingIngredient] //TODO return List[CraftingIngredient] now that I learned about Scala's (in/co/contra)variance

    override def tryCraft(craftingInventory: CraftingInventory, world: World): Option[CraftingResult] = {
        val ingredients = getIngredients()
        val inputItems = craftingInventory.getMatrix.filter(_ != null)

        if (ingredients.size != inputItems.length) return None

        val lazyListIterator = LazyList(ingredients: _*).permutations
        val matchingIngredients: Option[(LazyList[CraftingIngredient], LazyList[ItemStack])] = lazyListIterator
            .map((_, LazyList(inputItems: _*)))
            .find({ case (ingredients, inputs) =>
                ingredients.zip(inputs).forall { case (ingredient, input) => ingredient.apply(input) }
        })

        matchingIngredients match {
            case Some((matches, theItemStacks)) =>
                val matrix = craftingInventory.getMatrix
                val ingrArray = matches.toArray

                var remainders: List[Option[_ <: ItemStack]] = Nil
                var ingrIndex = ingrArray.length - 1

                for (i <- (matrix.length - 1) to (0, -1)) {
                    val inputItem = matrix(i)
                    val remainder = if (inputItem == null) None else {
                        val rem = ingrArray(ingrIndex).getRemainingStack(matrix(i))
                        ingrIndex -= 1
                        rem
                    }
                    remainders = remainder :: remainders
                }

                Some(CraftingResult(getResult(), remainders))
            case None => None
        }
    }
}

object SimpleShapelessRecipe {
    def valueOf(map: util.Map[String, AnyRef]): SimpleShapelessRecipe = {
        val namescapedKey = map.get(KeyString).asInstanceOf[NamespacedRecipeKey].namespacedKey
        val group = Option(map.get(GroupString).asInstanceOf[String]).filter(_.nonEmpty)
        val ingredients = map.get(IngredientsString).asInstanceOf[SerializableList[CraftingIngredient]].list
        val result = map.get(ResultString).asInstanceOf[ItemStack]
        new SimpleShapelessRecipe(namescapedKey, group, ingredients, result)
    }

}

@SerializableAs("SimpleShapeless")
class SimpleShapelessRecipe(private val namespacedKey: NamespacedKey,
                            optionGroup: Option[String],
                            private val ingredients: List[_ <: CraftingIngredient],
                            private val result: ItemStack)
    extends ShapelessRecipe with ConfigurationSerializable {

    this.group = optionGroup.orNull

    def this(namespacedKey: NamespacedKey, ingredients: List[_ <: CraftingIngredient], result: ItemStack) = this(namespacedKey, None, ingredients, result)

    override def getResult(): ItemStack = if (result == null) null else result.clone

    override def getIngredients(): List[_ <: CraftingIngredient] = ingredients

    override def getKey(): NamespacedKey = namespacedKey

    override def serialize(): util.Map[String, AnyRef] = {
        val map = new util.HashMap[String, AnyRef]()
        map.put(KeyString, new NamespacedRecipeKey(getKey))
        getGroup().foreach(map.put(GroupString, _))
        map.put(IngredientsString, new SerializableList(getIngredients()))
        map.put(ResultString, getResult())
        map
    }

    override def toString(): String = s"SimpleShapelessRecipe{key=${getKey()},group=${getGroup()},ingredients=${getIngredients()},result=${getResult()}}"
}
