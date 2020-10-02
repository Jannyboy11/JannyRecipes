package com.janboerman.recipes.api.recipe

import java.util

import com.janboerman.recipes.api.MaterialUtils
import org.bukkit.Material
import org.bukkit.Material._
import org.bukkit.configuration.serialization.ConfigurationSerializable
import org.bukkit.inventory.ItemStack
import com.janboerman.recipes.api.MaterialUtils

import scala.collection.JavaConverters

//TODO override getKey() as default methods?

trait ComplexRecipe extends CraftingRecipe {
    override def isHidden(): Boolean = true
}
trait ArmorDyeRecipe extends ComplexRecipe with ConfigurationSerializable with FixedResult with FixedIngredients {
    private val materials = util.EnumSet.noneOf(classOf[Material])
    private val leathers = Seq(LEATHER_HELMET, LEATHER_CHESTPLATE, LEATHER_LEGGINGS, LEATHER_BOOTS)
    for (material <- JavaConverters.asScalaIterator(MaterialUtils.dyesIngredient.iterator())) {
        materials.add(material)
    }
    for (material <- leathers) {
        materials.add(material)
    }

    override def getResultStack(): ItemStack = ArmorDyeType.getIcon()
    override def hasResultType(material: Material): Boolean = leathers.contains(material)

    override def hasIngredient(material: Material): Boolean = materials.contains(material)
    override def hasIngredient(itemStack: ItemStack): Boolean = itemStack != null && hasIngredient(itemStack.getType)
    override def getIngredientStacks(): Iterable[ItemStack] = new Iterable[ItemStack] {
        override def iterator: Iterator[ItemStack] = (JavaConverters.asScalaIterator[Material](materials.iterator()) ++ leathers.iterator).map(new ItemStack(_))
    }

    override def getType(): RecipeType = ArmorDyeType
}
trait BannerAddPatternRecipe extends ComplexRecipe with ConfigurationSerializable with FixedResult with FixedIngredients {
    private val materials = util.EnumSet.noneOf(classOf[Material])
    for (material <- JavaConverters.asScalaIterator(MaterialUtils.dyesIngredient.iterator()) ++ JavaConverters.asScalaIterator(MaterialUtils.bannerIngredient.iterator())) {
        materials.add(material)
    }

    override def getResultStack(): ItemStack = BannerAddPatternType.getIcon()
    override def hasResultType(material: Material): Boolean = MaterialUtils.bannerIngredient.contains(material)

    override def hasIngredient(material: Material): Boolean = materials.contains(material)
    override def hasIngredient(itemStack: ItemStack): Boolean = itemStack != null && hasIngredient(itemStack.getType)
    override def getIngredientStacks(): Iterable[ItemStack] = new Iterable[ItemStack](){
        override def iterator: Iterator[ItemStack] = JavaConverters.asScalaIterator(materials.iterator()).map(new ItemStack(_))
    }

    override def getType(): RecipeType = BannerAddPatternType
}
trait BannerDuplicateRecipe extends ComplexRecipe with ConfigurationSerializable with FixedResult with FixedIngredients {
    private val materials = util.EnumSet.noneOf(classOf[Material])
    for (material <- JavaConverters.asScalaIterator(MaterialUtils.bannerIngredient.iterator())) {
        materials.add(material)
    }

    override def getResultStack(): ItemStack = BannerDuplicateType.getIcon()
    override def hasResultType(material: Material): Boolean = MaterialUtils.bannerIngredient.contains(material)

    override def hasIngredient(material: Material): Boolean = materials.contains(material)
    override def hasIngredient(itemStack: ItemStack): Boolean = itemStack != null && hasIngredient(itemStack.getType)
    override def getIngredientStacks(): Iterable[ItemStack] = new Iterable[ItemStack](){
        override def iterator: Iterator[ItemStack] = JavaConverters.asScalaIterator(materials.iterator()).map(new ItemStack(_))
    }

    override def getType(): RecipeType = BannerDuplicateType
}
trait BookCloneRecipe extends ComplexRecipe with ConfigurationSerializable with FixedResult with FixedIngredients {
    private val materials = util.EnumSet.of(WRITTEN_BOOK, WRITABLE_BOOK)

    override def getResultStack(): ItemStack = BookCloneType.getIcon()
    override def hasResultType(material: Material): Boolean = material == WRITTEN_BOOK

    override def hasIngredient(itemStack: ItemStack): Boolean = itemStack != null && hasIngredient(itemStack.getType)
    override def hasIngredient(material: Material): Boolean = materials.contains(material)
    override def getIngredientStacks(): Iterable[ItemStack] = new Iterable[ItemStack]() {
        override def iterator: Iterator[ItemStack] = JavaConverters.asScalaIterator(materials.iterator()).map(new ItemStack(_))
    }

    override def getType(): RecipeType = BookCloneType
}
trait FireworkRocketRecipe extends ComplexRecipe with ConfigurationSerializable with FixedResult with FixedIngredients {
    private val materials = util.EnumSet.of(PAPER, GUNPOWDER, FIREWORK_STAR, AIR)

    override def getResultStack(): ItemStack = FireworkRocketType.getIcon()
    override def hasResultType(material: Material): Boolean = material == FIREWORK_ROCKET

    override def hasIngredient(itemStack: ItemStack): Boolean = itemStack != null && hasIngredient(itemStack.getType)
    override def hasIngredient(material: Material): Boolean = materials.contains(material)
    override def getIngredientStacks(): Iterable[ItemStack] = new Iterable[ItemStack]() {
        override def iterator: Iterator[ItemStack] = JavaConverters.asScalaIterator(materials.iterator()).map(new ItemStack(_))
    }

    override def getType(): RecipeType = FireworkRocketType
}
trait FireworkStarFadeRecipe extends ComplexRecipe with ConfigurationSerializable with FixedResult with FixedIngredients {
    private val materials = util.EnumSet.noneOf[Material](classOf[Material])
    for (material <- JavaConverters.asScalaIterator(MaterialUtils.dyesIngredient.iterator()) ++ Seq(FIREWORK_STAR, GUNPOWDER).iterator) {
        materials.add(material)
    }

    override def getResultStack(): ItemStack = FireworkStarFadeType.getIcon()
    override def hasResultType(material: Material): Boolean = material == FIREWORK_STAR

    override def hasIngredient(itemStack: ItemStack): Boolean = itemStack != null && hasIngredient(itemStack.getType)
    override def hasIngredient(material: Material): Boolean = materials.contains(material)
    override def getIngredientStacks(): Iterable[ItemStack] = new Iterable[ItemStack]() {
        override def iterator: Iterator[ItemStack] = JavaConverters.asScalaIterator(materials.iterator()).map(new ItemStack(_))
    }

    override def getType(): RecipeType = FireworkStarFadeType
}
trait FireworkStarRecipe extends ComplexRecipe with ConfigurationSerializable with FixedResult with FixedIngredients {
    private val materials = util.EnumSet.noneOf[Material](classOf[Material])
    for (material <- JavaConverters.asScalaIterator(MaterialUtils.dyesIngredient.iterator()) ++
        Seq(AIR, GUNPOWDER).iterator ++
        JavaConverters.asScalaIterator(MaterialUtils.fireworkShapesIngredient.iterator())) {
        materials.add(material)
    }

    override def getResultStack(): ItemStack = FireworkStarType.getIcon()
    override def hasResultType(material: Material): Boolean = material == FIREWORK_STAR

    override def hasIngredient(itemStack: ItemStack): Boolean = itemStack != null && hasIngredient(itemStack.getType)
    override def hasIngredient(material: Material): Boolean = materials.contains(material)
    override def getIngredientStacks(): Iterable[ItemStack] = new Iterable[ItemStack]() {
        override def iterator: Iterator[ItemStack] = JavaConverters.asScalaIterator(materials.iterator()).map(new ItemStack(_))
    }

    override def getType(): RecipeType = FireworkStarType
}
trait MapCloneRecipe extends ComplexRecipe with ConfigurationSerializable with FixedResult with FixedIngredients {
    private val materials = util.EnumSet.of[Material](FILLED_MAP, MAP)

    override def getResultStack(): ItemStack = MapCloneType.getIcon()
    override def hasResultType(material: Material): Boolean = material == FILLED_MAP

    override def hasIngredient(itemStack: ItemStack): Boolean = itemStack != null && hasIngredient(itemStack.getType)
    override def hasIngredient(material: Material): Boolean = materials.contains(material)
    override def getIngredientStacks(): Iterable[ItemStack] = new Iterable[ItemStack]() {
        override def iterator: Iterator[ItemStack] = JavaConverters.asScalaIterator(materials.iterator()).map(new ItemStack(_))
    }

    override def getType(): RecipeType = MapCloneType
}
trait MapExtendRecipe extends ShapedRecipe with ComplexRecipe with ConfigurationSerializable with FixedResult with FixedIngredients {
    private val materials = util.EnumSet.of[Material](FILLED_MAP, PAPER)

    override def getResultStack(): ItemStack = MapExtendType.getIcon()
    override def hasResultType(material: Material): Boolean = material == FILLED_MAP

    override def hasIngredient(itemStack: ItemStack): Boolean = itemStack != null && hasIngredient(itemStack.getType)
    override def hasIngredient(material: Material): Boolean = material == PAPER || material == FILLED_MAP
    override def getIngredientStacks(): Iterable[ItemStack] = new Iterable[ItemStack]() {
        override def iterator: Iterator[ItemStack] = JavaConverters.asScalaIterator(materials.iterator()).map(new ItemStack(_))
    }

    override def getType(): RecipeType = MapExtendType
}
trait RepairItemRecipe extends ComplexRecipe with ConfigurationSerializable with FixedResult with FixedIngredients {
    override def getResultStack(): ItemStack = RepairItemType.getIcon()
    override def hasResultType(material: Material): Boolean = material != null && material.getMaxDurability > 0

    override def hasIngredient(itemStack: ItemStack): Boolean = itemStack != null && hasIngredient(itemStack.getType)
    override def hasIngredient(material: Material): Boolean = material != null && material.getMaxDurability > 0
    override def getIngredientStacks(): Iterable[ItemStack] = new Iterable[ItemStack]() {
        override def iterator: Iterator[ItemStack] = JavaConverters.asScalaIterator(MaterialUtils.breakableItems.iterator()).map(new ItemStack(_))
    }

    override def getType(): RecipeType = RepairItemType
}
trait ShieldDecorationRecipe extends ComplexRecipe with ConfigurationSerializable with FixedResult with FixedIngredients {
    private val materials = util.EnumSet.copyOf[Material](MaterialUtils.bannerIngredient)

    override def getResultStack(): ItemStack = RepairItemType.getIcon()
    override def hasResultType(material: Material): Boolean = material == SHIELD

    override def hasIngredient(itemStack: ItemStack): Boolean = itemStack != null && hasIngredient(itemStack.getType)
    override def hasIngredient(material: Material): Boolean = materials.contains(material)
    override def getIngredientStacks(): Iterable[ItemStack] = new Iterable[ItemStack]() {
        override def iterator: Iterator[ItemStack] = JavaConverters.asScalaIterator(materials.iterator()).map(new ItemStack(_))
    }

    override def getType(): RecipeType = ShieldDecorationType
}
trait ShulkerBoxColorRecipe extends ComplexRecipe with ConfigurationSerializable with FixedResult with FixedIngredients {
    private val materials = util.EnumSet.noneOf[Material](classOf[Material])
    for (material <- JavaConverters.asScalaIterator(MaterialUtils.shulkerBoxIngredient.iterator()) ++
        JavaConverters.asScalaIterator(MaterialUtils.dyesIngredient.iterator())) {
        materials.add(material)
    }

    override def getResultStack(): ItemStack = ShulkerBoxColorType.getIcon()
    override def hasResultType(material: Material): Boolean = MaterialUtils.shulkerBoxIngredient.contains(material)

    override def hasIngredient(itemStack: ItemStack): Boolean = itemStack != null && hasIngredient(itemStack.getType)
    override def hasIngredient(material: Material): Boolean = materials.contains(material)
    override def getIngredientStacks(): Iterable[ItemStack] = new Iterable[ItemStack]() {
        override def iterator: Iterator[ItemStack] = JavaConverters.asScalaIterator(materials.iterator()).map(new ItemStack(_))
    }

    override def getType(): RecipeType = ShulkerBoxColorType
}
trait TippedArrowRecipe extends ComplexRecipe with ConfigurationSerializable with FixedResult with FixedIngredients {
    private val materials = util.EnumSet.of[Material](LINGERING_POTION, ARROW)

    override def getResultStack(): ItemStack = TippedArrowType.getIcon()
    override def hasResultType(material: Material): Boolean = material == TIPPED_ARROW

    override def hasIngredient(itemStack: ItemStack): Boolean = itemStack != null && hasIngredient(itemStack.getType)
    override def hasIngredient(material: Material): Boolean = materials.contains(material)
    override def getIngredientStacks(): Iterable[ItemStack] = new Iterable[ItemStack]() {
        override def iterator: Iterator[ItemStack] = JavaConverters.asScalaIterator(materials.iterator()).map(new ItemStack(_))
    }

    override def getType(): RecipeType = TippedArrowType
}
