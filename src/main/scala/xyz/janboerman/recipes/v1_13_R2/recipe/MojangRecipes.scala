package xyz.janboerman.recipes.v1_13_R2.recipe

import java.util.stream.StreamSupport

import com.google.gson.{JsonArray, JsonElement, JsonObject, JsonSyntaxException}
import net.minecraft.server.v1_13_R2._
import xyz.janboerman.recipes.v1_13_R2.Extensions.{NmsIngredient, NmsKey, NmsNonNullList, NmsRecipe}
import xyz.janboerman.recipes.v1_13_R2.Extensions.NmsRecipe.Group
import xyz.janboerman.recipes.v1_13_R2.recipe.bukkit.{BetterCraftFurnaceRecipe, BetterCraftShapedRecipe, BetterCraftShapelessRecipe, BetterRecipeChoice}

import scala.collection.mutable.ArrayBuffer

case class BetterShapelessRecipe(key: MinecraftKey, group: Group, ingredients: NonNullList[RecipeItemStack], result: ItemStack)
    extends ShapelessRecipes(key, group, result, ingredients) {

    def getIngredients() = new NmsRecipe(this).getIngredients() //scalac y u so dumb? I should be able to call super.getIngredients()
    def getResult() = new NmsRecipe(this).getResult()  //scalac y u so dumb? I should be able to call super.getResult()
    def getGroup(): Option[Group] = Option(group).filter(_.nonEmpty)

    override def toBukkitRecipe: BetterCraftShapelessRecipe = BetterCraftShapelessRecipe(this)

    override def a(): RecipeSerializer[_] = BetterShapelessSerializer

}

object BetterShapelessSerializer extends ShapelessRecipes.a {

    // ========================== helper methods ==========================

    def readIngredients(json: JsonArray): NonNullList[RecipeItemStack] = {
        val nonNullList = NmsNonNullList[RecipeItemStack]()
        json.forEach(jsonElement => nonNullList.add(NmsIngredient(jsonElement)))
        nonNullList
    }

    // ========================== interface methods ==========================

    def readJson(minecraftKey: MinecraftKey, json: JsonObject): BetterShapelessRecipe = {
        val group = if (json.has("group")) json.get("group").getAsString else ""
        val ingredients = readIngredients(json.getAsJsonArray("ingredients"))
        val result = BetterShapedSerializer.readItemStack(json.getAsJsonObject("result"))
        BetterShapelessRecipe(minecraftKey, group, ingredients, result)
    }
    def read(minecraftKey: MinecraftKey, packetDataSerializer: PacketDataSerializer): BetterShapelessRecipe = {
        //I have a feeling this is only used on the client side?
        val group = packetDataSerializer.e(Short.MaxValue)
        val ingredientsListSize = packetDataSerializer.g()
        val ingredients = NmsNonNullList(ingredientsListSize, NmsIngredient.empty)
        for (index <- 0 until ingredientsListSize) ingredients.set(index, NmsIngredient(packetDataSerializer))
        val resultStack = packetDataSerializer.k()
        BetterShapelessRecipe(minecraftKey, group, ingredients, resultStack)
    }
    def write(packetDataSerializer: PacketDataSerializer, recipe: BetterShapelessRecipe): Unit = super.a(packetDataSerializer, recipe)
    def getRecipeType(): String = "crafting_shapeless"

    override def a(minecraftKey: MinecraftKey, jsonObject: JsonObject): BetterShapelessRecipe = readJson(minecraftKey, jsonObject)
    override def a(minecraftKey: MinecraftKey, packetDataSerializer: PacketDataSerializer): BetterShapelessRecipe = read(minecraftKey, packetDataSerializer)
    override def a(packetDataSerializer: PacketDataSerializer, betterShapelessRecipe: ShapelessRecipes): Unit = write(packetDataSerializer, betterShapelessRecipe.asInstanceOf[BetterShapelessRecipe])
    override def a(): String = getRecipeType()
}

case class BetterShapedRecipe(key: MinecraftKey, group: Group, shape: Shape, result: ItemStack)
    extends ShapedRecipes(key, group, shape.getWidth(), shape.getHeight(), shape.getIngredients(), result) {

    def getWidth() = shape.getWidth()
    def getHeight() = shape.getHeight()
    def getIngredients() = shape.getIngredients()
    def getShape() = shape
    def getGroup(): Option[Group] = Option(group).filter(_.nonEmpty)
    def getResult(): ItemStack = new NmsRecipe(this).getResult() //scalac y u so dumb? I should be able to call super.getResult()

    override def a(): RecipeSerializer[_] = BetterShapedSerializer

    override def toBukkitRecipe: BetterCraftShapedRecipe = BetterCraftShapedRecipe(this)
}

private final class FastVector[+T](a: Array[T]) extends IndexedSeq[T] {
    override def apply(idx: Int): T = a(idx)
    override def length: Int = a.length
}

object BetterShapedSerializer extends ShapedRecipes.a {

    // ========================== helper methods ==========================

    def readItemStack(json: JsonObject): ItemStack = {
        val item = IRegistry.ITEM.get(NmsKey(json.get("item").getAsString))
        if (item == null) throw new JsonSyntaxException("Unknown item: \"" + item + "\"")
        else if (json.has("data")) throw new JsonSyntaxException("Disallowed data tag found")
        else {
            if (json.has("count"))
                new ItemStack(item, json.get("count").getAsInt)
            else
                new ItemStack(item)
        }
    }

    def readIngredientMap(json: JsonObject): Map[Char, RecipeItemStack] = {
        var map = Map[Char, RecipeItemStack](' ' -> NmsIngredient.empty)

        val entrySetIterator = json.entrySet().iterator()
        while (entrySetIterator.hasNext) {
            val entry = entrySetIterator.next()

            if (entry.getKey.length() != 1) throw new JsonSyntaxException("Invalid key: \"" + entry.getKey + "\" (must be of length 1)")
            //allow recipe to override the space character #YOLO
            map += entry.getKey.charAt(0) -> NmsIngredient(entry.getValue)
        }

        map
    }

    def readPattern(json: JsonArray): IndexedSeq[String] = {
        //scalac why are you so dumb and do I need to provide you with type parameter information?
        val array = StreamSupport.stream[JsonElement](json.spliterator(), false)
            .map[String](row => row.getAsString)
            .toArray(size => new Array[String](size))
        new FastVector(array)
    }

    // ========================== interface methods ==========================

    def readJson(minecraftKey: MinecraftKey, json: JsonObject): BetterShapedRecipe = {
        val group = if (json.has("group")) json.get("group").getAsString else ""
        val pattern = readPattern(json.getAsJsonArray("pattern"))
        val ingredientMap = readIngredientMap(json.getAsJsonObject("key"))
        val result = readItemStack(json.getAsJsonObject("result"))
        val shape = Shape(pattern, ingredientMap)
        BetterShapedRecipe(minecraftKey, group, shape, result)
    }
    def read(minecraftKey: MinecraftKey, packetDataSerializer: PacketDataSerializer): BetterShapedRecipe = {
        //I have a feeling this is only used on the client side?
        val width = packetDataSerializer.g()
        val height = packetDataSerializer.g()
        val group = packetDataSerializer.e(Short.MaxValue)
        val ingredients = NmsNonNullList(width * height, NmsIngredient.empty)
        for (k <- 0 until ingredients.size()) {
            ingredients.set(k, NmsIngredient(packetDataSerializer))
        }
        val resultStack = packetDataSerializer.k()
        val shape = Shape(width, height, ingredients)
        BetterShapedRecipe(minecraftKey, group, shape, resultStack)
    }
    def write(packetDataSerializer: PacketDataSerializer, recipe: BetterShapedRecipe): Unit = super.a(packetDataSerializer, recipe)
    def getRecipeType(): String = "crafting_shaped"

    override def a(minecraftKey: MinecraftKey, jsonObject: JsonObject): BetterShapedRecipe = readJson(minecraftKey, jsonObject)
    override def a(minecraftKey: MinecraftKey, packetDataSerializer: PacketDataSerializer): BetterShapedRecipe = read(minecraftKey, packetDataSerializer)
    override def a(packetDataSerializer: PacketDataSerializer, betterShapedRecipe: ShapedRecipes): Unit = write(packetDataSerializer, betterShapedRecipe.asInstanceOf[BetterShapedRecipe])
    override def a(): String = getRecipeType()
}

object Shape {
    def apply(width: Int, height: Int, ingredients: NonNullList[RecipeItemStack]): Shape = {
        val size = width * height

        assume(size == ingredients.size(), "(width * height) must equal the size of the ingredients list")
        val patternBuilder = new ArrayBuffer[StringBuilder](height)
        var ingredientMap = Map(' ' -> NmsIngredient.empty)

        var index = 0
        var x = 'a'
        while (index < size) {
            val h = index / width
            val w = index % width
            if (w == 0) {
                //we arrived on a new row - add the StringBuilder to the ArrayBuffer!
                val currentRow = new StringBuilder(width)
                for (_ <- 0 until width) currentRow.append(' ')
                patternBuilder += currentRow
            }

            import scala.util.control.Breaks._
            breakable {
                val ingredient = ingredients.get(index)

                if (ingredient eq NmsIngredient.empty) break    //don't set the char for the empty ingredient
                x = (x.toInt + 1).toChar                        //calculate next char
                patternBuilder(h).setCharAt(w, x)               //put the character in the shape
                ingredientMap += x -> ingredient                //put mapping in the map
            }

            index += 1
        }

        val pattern: Array[String] = patternBuilder.map(_.toString).toArray
        Shape(new FastVector[String](pattern), ingredientMap)
    }

}

case class Shape(pattern: IndexedSeq[String], ingredientMap: Map[Char, RecipeItemStack]) {
    private val size = getWidth() * getHeight()

    def getWidth(): Int = pattern(0).length()
    def getHeight(): Int = pattern.length
    def getSize(): Int = size

    def getIngredients(): NonNullList[RecipeItemStack] = {
        val width = getWidth()
        val height = getHeight()

        val size = getSize()
        val ingredients = NmsNonNullList[RecipeItemStack](size, NmsIngredient.empty)
        for (h <- 0 until height) {
            for (w <- 0 until width) {
                val character = pattern(h).charAt(w)
                val index = h * width + w
                val ingredient = ingredientMap.getOrElse(character, NmsIngredient.empty)
                ingredients.set(index, ingredient)
            }
        }

        ingredients
    }
}

case class BetterFurnaceRecipe(key: MinecraftKey, group: Group, ingredient: RecipeItemStack, result: ItemStack, xp: Float, cookingTime: Int)
    extends FurnaceRecipe(key, group, ingredient, result, xp, cookingTime) {

    def getIngredients() = new NmsRecipe(this).getIngredients()
    def getResult() = new NmsRecipe(this).getResult()

    def getGroup(): Option[Group] = Option(group).filter(_.nonEmpty)
    def getIngredient(): RecipeItemStack = ingredient
    def getExperience(): Float = xp
    def getCookingTime(): Int = cookingTime

    //ugly hack! needed because the TileEntityFurnace doesn't call craftItem on the recipe
    private var lastFurnace: TileEntityFurnace = _

    override def a(inventory: IInventory, world: World): Boolean = {
        val works = super.a(inventory, world)
        if (works) {
            //part of the ugly hack... depends on the order the TileEntityFurnaces call us.
            //this only works because TileEntityFurnace uses a check-then-act pattern.
            lastFurnace = inventory.asInstanceOf[TileEntityFurnace]
        }
        works
    }

    override def craftItem(IInventory: IInventory): ItemStack = result.cloneItemStack()

    override def toBukkitRecipe: BetterCraftFurnaceRecipe = BetterCraftFurnaceRecipe(this)

    //ugly hack! called by the TileEntityFurnace
    override def d(): ItemStack = if (lastFurnace != null) craftItem(lastFurnace) else result.cloneItemStack()

    override def a(): RecipeSerializer[_] = BetterFurnaceSerializer
}

object BetterFurnaceSerializer extends FurnaceRecipe.a {

    // ========================== interface methods ==========================

    def readJson(minecraftKey: MinecraftKey, json: JsonObject): BetterFurnaceRecipe = {
        val group = if (json.has("group")) json.get("group").getAsString else ""
        val ingredient =
            if (ChatDeserializer.d(json, "ingredient")) NmsIngredient(ChatDeserializer.u(json, "ingredient"))   //json array case
            else                                        NmsIngredient(ChatDeserializer.t(json, "ingredient"))   //json object case
        val resultItemString = ChatDeserializer.h(json, "result")
        val resultItem = IRegistry.ITEM.get(new MinecraftKey(resultItemString))
        val result = new ItemStack(resultItem)
        val xp = if (json.has("experience")) json.get("experience").getAsFloat else 0F
        val cookingTime = if (json.has("cookingtime")) json.get("cookingtime").getAsInt else 200
        BetterFurnaceRecipe(minecraftKey, group, ingredient, result, xp, cookingTime)
    }
    def read(minecraftKey: MinecraftKey, packetDataSerializer: PacketDataSerializer): BetterFurnaceRecipe = {
        //I have a feeling this is only used on the client side?
        val group = packetDataSerializer.e(Short.MaxValue)
        val ingredient = NmsIngredient(packetDataSerializer)
        val result = packetDataSerializer.k()
        val xp = packetDataSerializer.readFloat()
        val cookingTime = packetDataSerializer.g()
        BetterFurnaceRecipe(minecraftKey, group, ingredient, result, xp, cookingTime)
    }
    def write(packetDataSerializer: PacketDataSerializer, recipe: BetterFurnaceRecipe): Unit = super.a(packetDataSerializer, recipe)
    def getRecipeType(): String = "smelting"

    override def a(minecraftKey: MinecraftKey, jsonObject: JsonObject): BetterFurnaceRecipe = readJson(minecraftKey, jsonObject)
    override def a(minecraftKey: MinecraftKey, packetDataSerializer: PacketDataSerializer): BetterFurnaceRecipe = read(minecraftKey, packetDataSerializer)
    override def a(packetDataSerializer: PacketDataSerializer, betterFurnaceRecipe: FurnaceRecipe): Unit = write(packetDataSerializer, betterFurnaceRecipe.asInstanceOf[BetterFurnaceRecipe])
    override def a(): String = getRecipeType()
}


// Complex recipes

import xyz.janboerman.recipes.v1_13_R2.Extensions.NmsShapedRecipe

object BetterMapExtendRecipe {
    private lazy val instance = new BetterMapExtendRecipe(mapExtending.getKey)
    def apply(): BetterMapExtendRecipe = instance

    val serializer = new RecipeSerializers.a("crafting_special_mapextending", key => new BetterMapExtendRecipe(key))
}
class BetterMapExtendRecipe(key: MinecraftKey)
    extends BetterShapedRecipe(key,
        "",
        Shape(mapExtending.getWidth, mapExtending.getHeight, mapExtending.getIngredients()),
        new ItemStack(Items.MAP)) {

    //override all overriden methods - delegate to vanilla

    override def a(inventory: IInventory, world: World) = mapExtending.a(inventory, world)
    override def craftItem(inventory: IInventory) = mapExtending.craftItem(inventory)
    override def c() = mapExtending.c()
    override def a() = BetterMapExtendRecipe.serializer
}

object BetterTippedArrowRecipe {
    private lazy val instance = new BetterTippedArrowRecipe(tippedArrow.getKey)
    def apply(): BetterTippedArrowRecipe = instance

    val serializer = new RecipeSerializers.a("crafting_special_tippedarrow", key => new BetterTippedArrowRecipe(key))
}
class BetterTippedArrowRecipe(key: MinecraftKey)
    extends BetterShapedRecipe(key,
        "",
        Shape(tippedArrow.getWidth(), tippedArrow.getHeight(), tippedArrow.getIngredients()),
        new ItemStack(Items.TIPPED_ARROW)) {
    //override all overriden methods - delegate to vanilla
    override def a(inventory: IInventory, world: World) = tippedArrow.a(inventory, world)
    override def craftItem(inventory: IInventory) = tippedArrow.craftItem(inventory)
    override def c() = tippedArrow.c()
    override def a() = BetterTippedArrowRecipe.serializer
}

//TODO all other types of complex recipes? Ideally I don't have any vanilla implementations hanging around
//TODO there may be other plugins that add recipes though (using nms) so this is not guaranteed