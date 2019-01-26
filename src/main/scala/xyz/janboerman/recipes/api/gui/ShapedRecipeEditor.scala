package xyz.janboerman.recipes.api.gui

import org.bukkit.inventory.{Inventory, ItemStack}
import org.bukkit.plugin.Plugin
import xyz.janboerman.recipes.api.JannyRecipesAPI
import xyz.janboerman.recipes.api.recipe.{CraftingIngredient, ShapedRecipe, SimpleCraftingIngredient, SimpleShapedRecipe}
import CraftingRecipeEditor._
import org.bukkit.Material
import org.bukkit.enchantments.Enchantment
import xyz.janboerman.guilib.api.ItemBuilder
import xyz.janboerman.guilib.api.menu.ItemButton

import scala.collection.mutable

class ShapedRecipeEditor[P <: Plugin](inventory: Inventory,
                                      shapedRecipe: ShapedRecipe)
                                     (implicit override val recipesMenu: RecipesMenu[P],
                                      implicit override val api: JannyRecipesAPI,
                                      implicit override val plugin: P)
    extends CraftingRecipeEditor[P, ShapedRecipe](inventory, shapedRecipe) {

    protected var shouldUpdateIngredients = false
    protected var oldIngredientContents: Map[Int, ItemStack] = (CornerY until CornerY + MaxHeight)
        .flatMap(y => (CornerX until CornerX + MaxWidth)
            .map(x => y * InventoryWidth + x))
        .map(i => (i, getInventory.getItem(i)))
        .toMap

    private var shape: IndexedSeq[String] = null
    private var ingredients: Map[Char, _ <: CraftingIngredient] = null

    if (recipe != null) {
        this.ingredients = recipe.getIngredients()
        this.shape = recipe.getShape()
    }

    protected def hasIngredientContentsChanged(): Boolean = {
        ingredients == null || shouldUpdateIngredients || oldIngredientContents != (CornerY until CornerY + MaxHeight)
            .flatMap(y => (CornerX until CornerX + MaxWidth)
                .map(x => y * InventoryWidth + x))
            .map(i => (i, getInventory.getItem(i)))
            .toMap
    }

    def setNewIngredients(shape: IndexedSeq[String], ingredients: Map[Char, _ <: CraftingIngredient]): Unit = {
        this.shape = shape
        this.ingredients = ingredients

        for (index <- IngredientIndices) {
            val y = index / InventoryWidth - CornerY
            val x = index % InventoryWidth - CornerX

            val s: String = shape.applyOrElse(y, (_: Int) => (0 until MaxWidth).map(_ => ' ').mkString(""))
            val c: Char = s.applyOrElse(x, (_: Int) => ' ')

            val ingredient: Option[CraftingIngredient] = ingredients.get(c)
            val stack: ItemStack = ingredient.flatMap(_.getChoices().headOption).orNull

            getInventory.setItem(index, stack)
        }

        this.shouldUpdateIngredients = false
    }

    override def makeRecipe(): Option[ShapedRecipe] = {
        val ingredientStacks = (CornerY until CornerY + MaxHeight)
            .flatMap(y => (CornerX until CornerX + MaxWidth)
                .map(x => y * InventoryWidth + x)).map(getInventory.getItem(_))
            .filter(_ != null)

        if (ingredientStacks.nonEmpty) {
            //there are ingredients - let's  create a recipe!
            val result = getInventory.getItem(ResultSlot)
            val key = if (this.key == null) generateId() else this.key
            val group = this.group

            val ingredientContentsChanged = hasIngredientContentsChanged()

            val characters: Map[ItemStack, Char] = ingredientStacks.toSet.zipWithIndex
                .map[(Char, ItemStack)]({ case (itemStack: ItemStack, index: Int) => (('A' + index).toChar, itemStack) })
                .map(_.swap)
                .toMap

            val shape: IndexedSeq[String] = if (ingredientContentsChanged) {

                val coordinates = new mutable.ListBuffer[(Int, mutable.ListBuffer[Int])]()
                for (h <- 0 until MaxHeight) {
                    val row = new mutable.ListBuffer[Int]()
                    for (w <- 0 until MaxWidth) {
                        row.addOne(w)
                    }
                    coordinates.addOne((h, row))
                }

                val isLineEmpty: Seq[Int] => Boolean = _.forall(getInventory.getItem(_) == null)
                val toIndex: (Int, Int) => Int = (y, x) => y * InventoryWidth + x
                val coord: (Int, Int) => Int = (ry, rx) => toIndex(CornerY + ry, CornerX + rx)

                val removeCoordinates: Seq[(Int, Int)] => Unit = _.foreach { case (y: Int, x: Int) =>
                    val tup: (Int, mutable.ListBuffer[Int]) = coordinates.applyOrElse(y, (uwot: Int) => (uwot, new mutable.ListBuffer[Int]()))
                    val row = tup._2
                    row -= x
                    if (row.isEmpty) {
                        coordinates.-=((y, row))
                    }
                }

                //TODO don't hardcode all these coordinates? it can be done with 4 loops (after one another, not nested)
                if (isLineEmpty(Seq(coord(0, 1), coord(1, 1), coord(2, 1))) &&
                    (isLineEmpty(Seq(coord(0, 0), coord(1, 0), coord(2, 0))) ||
                        isLineEmpty(Seq(coord(0, 2), coord(1, 2), coord(2, 2))))) {
                    removeCoordinates(Seq((0, 1), (1, 1), (2, 1)))
                }
                if (isLineEmpty(Seq(coord(0, 2), coord(1, 2), coord(2, 2)))) {
                    removeCoordinates(Seq((0, 2), (1, 2), (2, 2)))
                }
                if (isLineEmpty(Seq(coord(0, 0), coord(1, 0), coord(2, 0)))) {
                    removeCoordinates(Seq((0, 0), (1, 0), (2, 0)))
                }
                if (isLineEmpty(Seq(coord(1, 0), coord(1, 1), coord(1, 2))) &&
                    (isLineEmpty(Seq(coord(0, 0), coord(0, 1), coord(0, 2))) ||
                        isLineEmpty(Seq(coord(2, 0), coord(2, 1), coord(2, 2))))) {
                    removeCoordinates(Seq((1, 0), (1, 1), (1, 2)))
                }
                if (isLineEmpty(Seq(coord(0, 0), coord(0, 1), coord(0, 2)))) {
                    removeCoordinates(Seq((0, 0), (0, 1), (0, 2)))
                }
                if (isLineEmpty(Seq(coord(2, 0), coord(2, 1), coord(2, 2)))) {
                    removeCoordinates(Seq((2, 0), (2, 1), (2, 2)))
                }

                coordinates.map { case (y, row) => {
                    row.map(x => {
                        val i = coord(y, x)
                        val itemStack = getInventory.getItem(i)
                        val character = characters.getOrElse(itemStack, ' ')
                        character
                    }).mkString
                }}.toIndexedSeq
            } else {
                this.shape
            }

            val ingredients: Map[Char, _ <: CraftingIngredient] = if (ingredientContentsChanged) {
                //TODO only 'replace' the ingredients of the slots where the itemstack actually changed?
                characters.map({case (itemStack, c) => (c, CraftingIngredient(itemStack))})
            } else {
                this.ingredients
            }

            val recipe = new SimpleShapedRecipe(key, Option(group), shape, ingredients, result)
            Some(recipe)
        } else {
            //no ingredients - too bad
            None
        }
    }


    override def getIcon(): Option[ItemStack] = Option(recipe).map(_.getResult())

    override def layoutButtons(): Unit = {
        setButton(49, new ItemButton[ShapedRecipeEditor[P]](new ItemBuilder(Material.CRAFTING_TABLE).name(TypeShaped).enchant(Enchantment.DURABILITY, 1).build()))
        super.layoutButtons()
    }



    def layoutRecipe(): Unit = {
        if (shapedRecipe != null) {

            val height = Math.min(MaxHeight, shapedRecipe.getShape().size)
            val width = Math.min(MaxWidth, shapedRecipe.getShape()(0).length)

            for (h <- 0 until height) {
                val y = CornerY + h + (if (height < 3) 1 else 0)
                val row = shapedRecipe.getShape()(h)

                for (w <- 0 until width) {
                    val x = CornerX + w + (if (width == 1) 1 else 0)
                    val char = row(w)

                    shapedRecipe.getIngredients().get(char) match {
                        case Some(craftingIngredient) =>
                            val invIndex = x + y * InventoryWidth

                            val iTriedToUseOrNullButThatDidntWork: Option[ItemStack] = craftingIngredient.getChoices().headOption

                            val ingredientIcon: ItemStack = iTriedToUseOrNullButThatDidntWork match {
                                case Some(itemStack) => itemStack
                                case None => null
                            }

                            inventory.setItem(invIndex, ingredientIcon)
                        case None => ()
                    }
                }
            }

            inventory.setItem(ResultSlot, shapedRecipe.getResult())
        }
    }

}
