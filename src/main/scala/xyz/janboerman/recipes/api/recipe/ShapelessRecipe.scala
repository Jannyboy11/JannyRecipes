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
    override def getType(): RecipeType = ShapelessType

    def getResult(): ItemStack

    def getGroup(): Option[String] = None

    def getIngredients(): List[_ <: CraftingIngredient]

    override def tryCraft(craftingInventory: CraftingInventory, world: World): Option[CraftingResult] = {
        val ingredients = getIngredients()
        val inputItems = craftingInventory.getMatrix.filter(_ != null)

        if (ingredients.size != inputItems.length) return None

        val lazyListIterator = LazyList(ingredients: _*).permutations
        //val matchingIngredientList: Option[LazyList[CraftingIngredient]] = lazyListIterator.find(_.zip(inputItems).forall(p => p._1.apply(p._2)))
        val matchingIngredients: Option[(LazyList[CraftingIngredient], LazyList[ItemStack])] = lazyListIterator.map((_, LazyList(inputItems: _*))).find({case (ingredients, inputs) => {
            var ingrs = ingredients
            var ins = inputs
            var goingStrong = true
            while (goingStrong && ingrs.nonEmpty && inputs.nonEmpty) {
                val ingr = ingrs.head
                val in = ins.head
                if (!ingr(in)) {
                    goingStrong = false
                }
                ingrs = ingrs.tail
                ins = ins.tail
            }
            goingStrong && ingrs.isEmpty && ins.isEmpty
        }})


        matchingIngredients match {
            case Some((matches, theItemStacks)) =>
                val matrix = craftingInventory.getMatrix
                val ingrArray = matches.toArray

                var remainders: List[Option[_ <: ItemStack]] = Nil
                var ingrIndex = ingrArray.length - 1

                for (i <- (matrix.length - 1) to (0, -1)) {
                    val inputItem = matrix(i)
                    val remainder = if (inputItem == null) None else {
                        println(s"DEBUG input item = ${matrix(i)}")
                        println(s"DEBUG ingredient = ${ingrArray(ingrIndex)}")

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
