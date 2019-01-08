package xyz.janboerman.recipes.v1_13_R2.recipe.bukkit

import org.bukkit.inventory.RecipeChoice.MaterialChoice
import org.bukkit.{Material, NamespacedKey, World}
import org.bukkit.inventory.{FurnaceRecipe => _, ShapedRecipe => _, ShapelessRecipe => _, _}
import xyz.janboerman.recipes.api.recipe._
import xyz.janboerman.recipes.v1_13_R2.Extensions.{NmsInventory, ObcCraftingInventory}

import scala.collection.JavaConverters
import scala.collection.mutable.ListBuffer

case class BukkitShapedToJanny(bukkit: org.bukkit.inventory.ShapedRecipe) extends ShapedRecipe {
    override def getResult(): ItemStack = bukkit.getResult

    override def getShape(): IndexedSeq[String] = bukkit.getShape

    override def getIngredients(): Map[Char, _ <: CraftingIngredient] = {
        val mutableBukkitMap = JavaConverters.mapAsScalaMap(bukkit.getChoiceMap)

        var map = Map[Char, CraftingIngredient]()
        mutableBukkitMap.foreach({case (character, recipeChoice) =>
            map += character.charValue() -> BukkitRecipeChoiceToJannyCraftingIngredient(recipeChoice)
        })
        map
    }

    override def tryCraft(craftingInventory: CraftingInventory, world: World): Option[CraftingResult] = {
        val (inventoryWidth, inventoryHeight) = craftingInventory match {
            case ObcCraftingInventory((matrix, result)) => (matrix.getWidth, matrix.getHeight)
            case _ => (3, 3)
        }

        val shape = getShape()
        val maxAddX = inventoryWidth - shape.head.length
        val maxAddY = inventoryHeight - shape.size

        for (addX <- 0 until maxAddX) {
            for (addY <- 0 until maxAddY) {
                for (mirrored <- Seq(false, true)) {
                    val successResult = matrixMatch(craftingInventory, inventoryWidth, inventoryHeight, addX, addY, mirrored) match {
                        case Some(remainders) => Some(CraftingResult(getResult, remainders))
                        case _ => None
                    }

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

    override def getKey: NamespacedKey = bukkit.getKey
}

case class BukkitShapelessToJanny(bukkit: org.bukkit.inventory.ShapelessRecipe) extends ShapelessRecipe {
    override def getResult(): ItemStack = bukkit.getResult

    override def getIngredients(): List[_ <: CraftingIngredient] = {
        JavaConverters.asScalaBuffer(bukkit.getChoiceList)
            .map(BukkitRecipeChoiceToJannyCraftingIngredient)
            .toList
    }

    override def tryCraft(craftingInventory: CraftingInventory, world: World): Option[CraftingResult] = {
        val ingredients = getIngredients()
        val inputItems = craftingInventory.getMatrix.filter(stack => stack != null)

        if (ingredients.size != inputItems.length) return None

        val lazyListIterator = LazyList(ingredients: _*).permutations
        val matchingIngredientList = lazyListIterator.find(_.zip(inputItems).forall(p => p._1.apply(p._2)))

        matchingIngredientList.map(ingredientList => {
            val remainders = ingredientList.zip(inputItems).map(pair => pair._1.getRemainingStack(pair._2)).toList
            CraftingResult(getResult(), remainders)
        })
    }

    override def getKey: NamespacedKey = bukkit.getKey
}

case class BukkitFurnaceToJanny(bukkit: org.bukkit.inventory.FurnaceRecipe) extends FurnaceRecipe { self =>
    override def getResult(): ItemStack = bukkit.getResult

    override def getIngredient(): FurnaceIngredient = new BukkitRecipeChoiceToJannyFurnaceIngredient(bukkit.getInputChoice) {
        override def getItemStack: ItemStack = self.bukkit.getInput
    }

    override def trySmelt(furnaceInventory: FurnaceInventory): Option[ItemStack] = {
        val acceptedByIngredient = getIngredient().apply(furnaceInventory.getSmelting)
        if (acceptedByIngredient) {
            Some(getResult())
        } else {
            None
        }
    }

    override def getKey: NamespacedKey = bukkit.getKey
}

case class BukkitRecipeChoiceToJannyCraftingIngredient(bukkit: RecipeChoice) extends CraftingIngredient {
    override def getChoices(): List[_ <: ItemStack] = bukkit match {
        case materialChoice: MaterialChoice => JavaConverters.asScalaBuffer(materialChoice.getChoices)
                .toList
                .map(new ItemStack(_))
        case _ => List()
    }

    override def apply(itemStack: ItemStack) = bukkit.test(itemStack)

    override def clone(): BukkitRecipeChoiceToJannyCraftingIngredient = BukkitRecipeChoiceToJannyCraftingIngredient(cloneRecipeChoice(bukkit))
}

case class BukkitRecipeChoiceToJannyFurnaceIngredient(bukkit: RecipeChoice) extends FurnaceIngredient {
    override def getItemStack(): ItemStack = bukkit match {
        case materialChoice: MaterialChoice =>
            val choices = materialChoice.getChoices()
            if (choices.isEmpty) {
                new ItemStack(Material.AIR)
            } else if (choices.size() == 1) {
                new ItemStack(choices.get(0))
            } else {
                val stack = new ItemStack(choices.get(0))
                stack.setDurability(Short.MaxValue)
                stack
            }
        case _ => new ItemStack(Material.AIR)
    }

    override def apply(itemStack: ItemStack) = bukkit.test(itemStack)

    override def clone(): BukkitRecipeChoiceToJannyFurnaceIngredient = BukkitRecipeChoiceToJannyFurnaceIngredient(cloneRecipeChoice(bukkit))
}