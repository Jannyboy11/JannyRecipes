package xyz.janboerman.recipes.api.recipe

import java.util

import org.bukkit.configuration.serialization.{ConfigurationSerializable, SerializableAs}
import org.bukkit.{NamespacedKey, World}
import org.bukkit.inventory.{CraftingInventory, Inventory, ItemStack}
import xyz.janboerman.recipes.api.persist.RecipeStorage._
import xyz.janboerman.recipes.api.persist.{SerializableList, SerializableMap}

import scala.collection.mutable.ListBuffer

object ShapedRecipe {
    def unapply(arg: ShapedRecipe): Option[(NamespacedKey, Option[String], IndexedSeq[String], Map[Char, _ <: CraftingIngredient], ItemStack)] =
        Some((arg.getKey, arg.getGroup(), arg.getShape(), arg.getIngredients(), arg.getResult()))

    def apply(namespacedKey: NamespacedKey, group: Option[String], shape: IndexedSeq[String], ingredients: Map[Char, _ <: CraftingIngredient], result: ItemStack): ShapedRecipe =
        new SimpleShapedRecipe(namespacedKey, group, shape, ingredients, result)

    def apply(namespacedKey: NamespacedKey, shape: IndexedSeq[String], ingredients: Map[Char, _ <: CraftingIngredient], result: ItemStack): ShapedRecipe =
        new SimpleShapedRecipe(namespacedKey, shape, ingredients, result)
}

trait ShapedRecipe extends CraftingRecipe
    with FixedIngredients
    with FixedResult {

    override def getResultStack(): ItemStack = getResult()
    override def getIngredientStacks: Iterable[ItemStack] = getIngredients().values.flatMap(_.getChoices())
    override def getType(): RecipeType = ShapedType

    def getResult(): ItemStack

    def getGroup(): Option[String] = None

    def getShape(): IndexedSeq[String]

    def getIngredients(): Map[Char, _ <: CraftingIngredient]

    override def tryCraft(craftingInventory: CraftingInventory, world: World): Option[CraftingResult] = {
        val (inventoryWidth, inventoryHeight) = (3, 3)

        val shape = getShape()
        val maxAddX = inventoryWidth - shape.head.length
        val maxAddY = inventoryHeight - shape.size

        for (addX <- 0 until maxAddX) {
            for (addY <- 0 until maxAddY) {
                for (mirrored <- Seq(false, true)) {
                    val successResult = matrixMatch(craftingInventory, inventoryWidth, inventoryHeight, addX, addY, mirrored)
                        .map(remainders => {
                            var resultStack = getResult()
                            if (resultStack != null) resultStack = resultStack.clone()
                            CraftingResult(resultStack, remainders)
                        })

                    if (successResult.isDefined) return successResult
                }
            }
        }

        None
    }

    protected def matrixMatch(craftingInventory: CraftingInventory, invWidth: Int, invHeight: Int, addX: Int, addY: Int, mirrored: Boolean): Option[List[Option[ItemStack]]] = {
        val shape = getShape()
        val ingredients = getIngredients()

        val shapeWidth = shape.head.length
        val shapeHeight = shape.size

        val remainders = new ListBuffer[Option[ItemStack]]()

        for (y <- 0 until shapeHeight) {
            for (x <- 0 until shapeWidth) {
                val gridX = if (mirrored) shapeWidth - 1 - x + addX else x + addX
                val gridY = y + addY

                val key = shape(y)(x)
                val ingredient = ingredients.getOrElse(key, CraftingIngredient.empty)

                val inputStack = getItemStackFromInventory(gridX, gridY, invWidth, invHeight, craftingInventory)
                if (!ingredient.apply(inputStack)) return None

                remainders.addOne(ingredient.getRemainingStack(inputStack))
            }
        }

        Some(remainders.toList)
    }

    private def getItemStackFromInventory(x: Int, y: Int, invWidth: Int, invHeight: Int, inventory: Inventory): ItemStack = {
        if (x < 0 || x >= invWidth) return null
        if (y < 0 || y >= invHeight) return null

        val index = y * invWidth + x
        inventory.getItem(index)
    }
}

object SimpleShapedRecipe {
    def valueOf(map: util.Map[String, AnyRef]): SimpleShapedRecipe = {
        val namespacedKey = map.get(KeyString).asInstanceOf[NamespacedRecipeKey].namespacedKey
        val group = Option(map.get(GroupString).asInstanceOf[String]).filter(_.nonEmpty)
        val shape = map.get(ShapeString).asInstanceOf[SerializableList].list.asInstanceOf[List[String]].toIndexedSeq
        val ingredients: Map[Char, _<: CraftingIngredient] = map.get(IngredientsString)
            .asInstanceOf[SerializableMap].map
            .map({case (string, ingredient: CraftingIngredient) => (string(0), ingredient)})
        val result = map.get(ItemStackString).asInstanceOf[ItemStack]
        new SimpleShapedRecipe(namespacedKey, group, shape, ingredients, result)
    }
}

@SerializableAs("SimpleShaped")
class SimpleShapedRecipe(private val namespacedKey: NamespacedKey,
                         private val group: Option[String],
                         private val shape: IndexedSeq[String],
                         private val ingredients: Map[Char, _ <: CraftingIngredient],
                         private val result: ItemStack) extends ShapedRecipe
    with ConfigurationSerializable {

    def this(namespacedKey: NamespacedKey, shape: IndexedSeq[String], ingredients: Map[Char, _ <: CraftingIngredient], result: ItemStack) =
        this(namespacedKey, None, shape, ingredients, result)

    override def getResult(): ItemStack = if (result == null) null else result.clone

    override def getShape(): IndexedSeq[String] = shape

    override def getIngredients(): Map[Char, _ <: CraftingIngredient] = ingredients

    override def getKey: NamespacedKey = namespacedKey

    override def getGroup(): Option[String] = group

    override def serialize(): util.Map[String, AnyRef] = {
        val map = new util.HashMap[String, AnyRef]()
        map.put(KeyString, new NamespacedRecipeKey(getKey))
        getGroup().foreach(map.put(GroupString, _))
        map.put(ShapeString, new SerializableList(getShape().toList))
        map.put(IngredientsString, new SerializableMap(getIngredients().map({case (character, ingredient) => (character.toString, ingredient)})))
        map.put(ResultString, getResult())
        map
    }
}