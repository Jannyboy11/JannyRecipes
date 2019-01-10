package xyz.janboerman.recipes.api.recipe

import java.util

import org.bukkit.configuration.serialization.{ConfigurationSerializable, SerializableAs}
import org.bukkit.{NamespacedKey, World}
import org.bukkit.inventory.{CraftingInventory, ItemStack}
import xyz.janboerman.recipes.api.persist.RecipeStorage._

object ShapelessRecipe {
    def unapply(arg: ShapelessRecipe): Option[(NamespacedKey, Option[String], List[_ <: CraftingIngredient], ItemStack)] =
        Some((arg.getKey, arg.getGroup(), arg.getIngredients(), arg.getResult()))

    def apply(namespacedKey: NamespacedKey, group: Option[String], ingredients: List[_ <: CraftingIngredient], result: ItemStack): ShapelessRecipe =
        new SimpleShapelessRecipe(namespacedKey, group, ingredients, result)

    def apply(namespacedKey: NamespacedKey, ingredients: List[_ <: CraftingIngredient], result: ItemStack): ShapelessRecipe =
        new SimpleShapelessRecipe(namespacedKey, ingredients, result)
}

trait ShapelessRecipe extends CraftingRecipe
    with FixedIngredients
    with FixedResult {

    override def getResultStack(): ItemStack = getResult()
    override def getIngredientStacks: Iterable[ItemStack] = getIngredients().flatMap(_.getChoices())

    def getResult(): ItemStack

    def getGroup(): Option[String] = None

    def getIngredients(): List[_ <: CraftingIngredient]

    override def tryCraft(craftingInventory: CraftingInventory, world: World): Option[CraftingResult] = {
        val ingredients = getIngredients()
        val inputItems = craftingInventory.getMatrix.filter(_ != null)

        if (ingredients.size != inputItems.length) return None

        val lazyListIterator = LazyList(ingredients: _*).permutations
        val matchingIngredientList = lazyListIterator.find(_.zip(inputItems).forall(p => p._1.apply(p._2)))

        matchingIngredientList.map(ingredientList => {
            val remainders = ingredientList.zip(inputItems).map(pair => pair._1.getRemainingStack(pair._2)).toList
            CraftingResult(getResult(), remainders)
        })
    }
}

object SimpleShapelessRecipe {
    def valueOf(map: util.Map[String, AnyRef]): SimpleShapelessRecipe = {
        val namescapedKey = map.get(KeyString).asInstanceOf[NamespacedRecipeKey].namespacedKey
        val group = Option(map.get(GroupString).asInstanceOf[String]).filter(_.nonEmpty)
        val ingredients = map.get(IngredientsString).asInstanceOf[SerializableList[_ <: CraftingIngredient]].list
        val result = map.get(ResultString).asInstanceOf[ItemStack]
        new SimpleShapelessRecipe(namescapedKey, group, ingredients, result)
    }

}

@SerializableAs("SimpleShaped")
class SimpleShapelessRecipe(private val namespacedKey: NamespacedKey,
                            private val group: Option[String],
                            private val ingredients: List[_ <: CraftingIngredient],
                            private val result: ItemStack)
    extends ShapelessRecipe with ConfigurationSerializable {

    def this(namespacedKey: NamespacedKey, ingredients: List[_ <: CraftingIngredient], result: ItemStack) = this(namespacedKey, None, ingredients, result)

    override def getResult(): ItemStack = if (result == null) null else result.clone

    override def getIngredients(): List[_ <: CraftingIngredient] = ingredients

    override def getKey: NamespacedKey = namespacedKey

    override def serialize(): util.Map[String, AnyRef] = {
        val map = new util.HashMap[String, AnyRef]()
        map.put(KeyString, new NamespacedRecipeKey(getKey))
        getGroup().foreach(map.put(GroupString, _))
        map.put(IngredientsString, new SerializableList[CraftingIngredient](getIngredients()))
        map.put(ResultString, getResult())
        map
    }
}
