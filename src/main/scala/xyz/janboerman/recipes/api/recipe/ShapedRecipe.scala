package xyz.janboerman.recipes.api.recipe

import org.bukkit.{NamespacedKey, World}
import org.bukkit.inventory.{CraftingInventory, Inventory, ItemStack}

import scala.collection.mutable.ListBuffer

object ShapedRecipe {
    def unapply(arg: ShapedRecipe): Option[(NamespacedKey, Option[String], IndexedSeq[String], Map[Char, _ <: CraftingIngredient], ItemStack)] =
        Some((arg.getKey, arg.getGroup(), arg.getShape(), arg.getIngredients(), arg.getResult()))

    def apply(namespacedKey: NamespacedKey, group: Option[String], shape: IndexedSeq[String], ingredients: Map[Char, _ <: CraftingIngredient], result: ItemStack): ShapedRecipe =
        new SimpleShapedRecipe(namespacedKey, group, shape, ingredients, result)

    def apply(namespacedKey: NamespacedKey, shape: IndexedSeq[String], ingredients: Map[Char, _ <: CraftingIngredient], result: ItemStack): ShapedRecipe =
        new SimpleShapedRecipe(namespacedKey, shape, ingredients, result)
}

trait ShapedRecipe extends CraftingRecipe {
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

    private def matrixMatch(craftingInventory: CraftingInventory, invWidth: Int, invHeight: Int, addX: Int, addY: Int, mirrored: Boolean): Option[List[Option[ItemStack]]] = {
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

class SimpleShapedRecipe(val namespacedKey: NamespacedKey,
                         val group: Option[String],
                         val shape: IndexedSeq[String],
                         val ingredients: Map[Char, _ <: CraftingIngredient],
                         val result: ItemStack) extends ShapedRecipe {

    def this(namespacedKey: NamespacedKey, shape: IndexedSeq[String], ingredients: Map[Char, _ <: CraftingIngredient], result: ItemStack) =
        this(namespacedKey, None, shape, ingredients, result)

    override def getResult(): ItemStack = if (result == null) null else result.clone

    override def getShape(): IndexedSeq[String] = shape

    override def getIngredients(): Map[Char, _ <: CraftingIngredient] = ingredients

    override def getKey: NamespacedKey = namespacedKey
}