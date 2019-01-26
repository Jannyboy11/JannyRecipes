package xyz.janboerman.recipes.api.recipe

import java.util

import org.bukkit.configuration.serialization.{ConfigurationSerializable, SerializableAs}
import org.bukkit.{NamespacedKey, World}
import org.bukkit.inventory.{CraftingInventory, ItemStack}
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

trait ShapedRecipe extends CraftingRecipe with Grouped
    with FixedIngredients
    with FixedResult {

    override def getResultStack(): ItemStack = getResult()
    override def getIngredientStacks: Iterable[ItemStack] = getIngredients().values.flatMap(_.getChoices())
    override def getType(): RecipeType = ShapedType

    def getResult(): ItemStack

    def getShape(): IndexedSeq[String]

    def getIngredients(): Map[Char, _ <: CraftingIngredient] //TODO return Map[Char, CraftingIngredient] now that I learned about Scala's (in/co/contra)variance

    override def tryCraft(craftingInventory: CraftingInventory, world: World): Option[CraftingResult] = {
        val (inventoryWidth, inventoryHeight) = (3, 3)

        val shape = getShape()
        val maxAddX = inventoryWidth - shape.head.length
        val maxAddY = inventoryHeight - shape.size

        for (addX <- 0 to maxAddX) {
            for (addY <- 0 to maxAddY) {
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

        val matrix = new Array[Array[Option[ItemStack]]](invHeight)
        for (y <- 0 until invHeight) {
            val row =  new Array[Option[ItemStack]](invWidth)
            for (x <- 0 until invWidth) {
                row(x) = None
            }
            matrix(y) = row
        }

        for (y <- 0 until invHeight) {
            for (x <- 0 until invWidth) {
                val gridX = if (mirrored) shapeWidth - 1 - x + addX else x + addX
                val gridY = y + addY

                val key = if (y < shapeHeight && x < shapeWidth) shape(y)(x) else ' '
                val ingredient = ingredients.getOrElse(key, CraftingIngredient.empty())

                val inputStack = getItemStackFromInventory(gridX, gridY, invWidth, invHeight, craftingInventory)
                if (!ingredient.apply(inputStack)) return None

                if (0 <= gridX && gridX < invWidth && 0 <= gridY && gridY < invHeight) {
                    matrix(gridY)(gridX) = ingredient.getRemainingStack(inputStack)
                } //else: none remains in the array
            }
        }

        Some(matrix.toList.flatten)
    }

    private def getItemStackFromInventory(x: Int, y: Int, invWidth: Int, invHeight: Int, inventory: CraftingInventory): ItemStack = {
        if (x < 0 || x >= invWidth) return null
        if (y < 0 || y >= invHeight) return null

        val index = y * invWidth + x
        inventory.getMatrix()(index)
    }
}

object SimpleShapedRecipe {
    def valueOf(map: util.Map[String, AnyRef]): SimpleShapedRecipe = {
        val namespacedKey = map.get(KeyString).asInstanceOf[NamespacedRecipeKey].namespacedKey
        val group = Option(map.get(GroupString).asInstanceOf[String]).filter(_.nonEmpty)
        val shape = map.get(ShapeString).asInstanceOf[SerializableList[String]].list.toIndexedSeq
        val ingredients: Map[Char, _<: CraftingIngredient] = map.get(IngredientsString)
            .asInstanceOf[SerializableMap[CraftingIngredient]].map
            .map({case (string, ingredient) => (string(0), ingredient)})
        val result = map.get(ItemStackString).asInstanceOf[ItemStack]
        new SimpleShapedRecipe(namespacedKey, group, shape, ingredients, result)
    }
}

@SerializableAs("SimpleShaped")
class SimpleShapedRecipe(private val namespacedKey: NamespacedKey,
                         optionGroup: Option[String],
                         private val shape: IndexedSeq[String],
                         private val ingredients: Map[Char, _ <: CraftingIngredient],
                         private val result: ItemStack)
    extends ShapedRecipe with ConfigurationSerializable {

    this.group = optionGroup.orNull

    def this(namespacedKey: NamespacedKey, shape: IndexedSeq[String], ingredients: Map[Char, _ <: CraftingIngredient], result: ItemStack) =
        this(namespacedKey, None, shape, ingredients, result)

    override def getResult(): ItemStack = if (result == null) null else result.clone

    override def getShape(): IndexedSeq[String] = shape

    override def getIngredients(): Map[Char, _ <: CraftingIngredient] = ingredients

    override def getKey: NamespacedKey = namespacedKey

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