package xyz.janboerman.recipes.api.recipe

import java.util

import org.bukkit.{Color, DyeColor, FireworkEffect, Material}
import org.bukkit.Material._
import org.bukkit.block.banner.{Pattern, PatternType}
import org.bukkit.configuration.serialization.ConfigurationSerializable
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta._
import org.bukkit.potion.{PotionData, PotionType}
import xyz.janboerman.guilib.api.ItemBuilder
import xyz.janboerman.recipes.api.MaterialUtils

import scala.collection.JavaConverters

//TODO implement FixedResult and FixedIngredients

trait ComplexRecipe extends CraftingRecipe {
    override def isHidden(): Boolean = true
}
trait ArmorDyeRecipe extends ComplexRecipe with ConfigurationSerializable with FixedResult with FixedIngredients {
    private val materials = util.EnumSet.noneOf(classOf[Material])
    private val leathers = Seq(LEATHER_HELMET, LEATHER_CHESTPLATE, LEATHER_LEGGINGS, LEATHER_BOOTS)
    private val resultStack = new ItemBuilder(Material.LEATHER_CHESTPLATE)
        .changeMeta[LeatherArmorMeta](_.setColor(Color.WHITE))
        .build()
    for (material <- JavaConverters.asScalaIterator(MaterialUtils.dyesIngredient.iterator())) {
        materials.add(material)
    }
    for (material <- leathers) {
        materials.add(material)
    }

    override def getResultStack(): ItemStack = resultStack
    override def hasResultType(material: Material): Boolean = leathers.contains(material)

    override def hasIngredient(material: Material): Boolean = materials.contains(material)
    override def hasIngredient(itemStack: ItemStack): Boolean = itemStack != null && hasIngredient(itemStack.getType)
    override def getIngredientStacks(): Iterable[ItemStack] = new Iterable[ItemStack] {
        override def iterator: Iterator[ItemStack] = (JavaConverters.asScalaIterator[Material](materials.iterator()) ++ leathers.iterator).map(new ItemStack(_))
    }
}
trait BannerAddPatternRecipe extends ComplexRecipe with ConfigurationSerializable with FixedResult with FixedIngredients {
    private val materials = util.EnumSet.noneOf(classOf[Material])
    private val resultStack = new ItemBuilder(Material.WHITE_BANNER)
        .changeMeta[BannerMeta](_.addPattern(new Pattern(DyeColor.RED, PatternType.CREEPER)))
        .build()
    for (material <- JavaConverters.asScalaIterator(MaterialUtils.dyesIngredient.iterator()) ++ JavaConverters.asScalaIterator(MaterialUtils.bannerIngredient.iterator())) {
        materials.add(material)
    }

    override def getResultStack(): ItemStack = resultStack
    override def hasResultType(material: Material): Boolean = MaterialUtils.bannerIngredient.contains(material)

    override def hasIngredient(material: Material): Boolean = materials.contains(material)
    override def hasIngredient(itemStack: ItemStack): Boolean = itemStack != null && hasIngredient(itemStack.getType)
    override def getIngredientStacks(): Iterable[ItemStack] = new Iterable[ItemStack](){
        override def iterator: Iterator[ItemStack] = JavaConverters.asScalaIterator(materials.iterator()).map(new ItemStack(_))
    }
}
trait BannerDuplicateRecipe extends ComplexRecipe with ConfigurationSerializable with FixedResult with FixedIngredients {
    private val materials = util.EnumSet.noneOf(classOf[Material])
    private val resultStack = new ItemBuilder(Material.WHITE_BANNER)
        .changeMeta[BannerMeta](_.addPattern(new Pattern(DyeColor.BLUE, PatternType.CREEPER)))
        .build()
    for (material <- JavaConverters.asScalaIterator(MaterialUtils.bannerIngredient.iterator())) {
        materials.add(material)
    }

    override def getResultStack(): ItemStack = resultStack
    override def hasResultType(material: Material): Boolean = MaterialUtils.bannerIngredient.contains(material)

    override def hasIngredient(material: Material): Boolean = materials.contains(material)
    override def hasIngredient(itemStack: ItemStack): Boolean = itemStack != null && hasIngredient(itemStack.getType)
    override def getIngredientStacks(): Iterable[ItemStack] = new Iterable[ItemStack](){
        override def iterator: Iterator[ItemStack] = JavaConverters.asScalaIterator(materials.iterator()).map(new ItemStack(_))
    }
}
trait BookCloneRecipe extends ComplexRecipe with ConfigurationSerializable with FixedResult with FixedIngredients {
    private val materials = util.EnumSet.of(WRITTEN_BOOK, WRITABLE_BOOK)
    private val resultStack = new ItemBuilder(Material.WRITTEN_BOOK)
        .changeMeta[BookMeta](meta => {
        meta.setAuthor("Jannyboy11")
        meta.setTitle("The Art of Crafting")
        meta.setGeneration(BookMeta.Generation.ORIGINAL)
        meta.setPages(java.util.Arrays.asList("First you create a recipes plugin, inspect Mojang's code, and then you know all recipes!"))
    }).build()

    override def getResultStack(): ItemStack = resultStack
    override def hasResultType(material: Material): Boolean = material == WRITTEN_BOOK

    override def hasIngredient(itemStack: ItemStack): Boolean = itemStack != null && hasIngredient(itemStack.getType)
    override def hasIngredient(material: Material): Boolean = materials.contains(material)
    override def getIngredientStacks(): Iterable[ItemStack] = new Iterable[ItemStack]() {
        override def iterator: Iterator[ItemStack] = JavaConverters.asScalaIterator(materials.iterator()).map(new ItemStack(_))
    }
}
trait FireworkRocketRecipe extends ComplexRecipe with ConfigurationSerializable with FixedResult with FixedIngredients {
    private val materials = util.EnumSet.of(PAPER, GUNPOWDER, FIREWORK_STAR, AIR)
    private val resultStack = new ItemBuilder(Material.FIREWORK_ROCKET)
        .changeMeta[FireworkMeta](meta => {
        meta.addEffect(FireworkEffect.builder
            .`with`(FireworkEffect.Type.STAR)
            .withColor(Color.YELLOW)
            .withFade(Color.ORANGE)
            .withFlicker()
            .withTrail()
            .build())
        meta.setPower(32)
    }).build()

    override def getResultStack(): ItemStack = resultStack
    override def hasResultType(material: Material): Boolean = material == FIREWORK_ROCKET

    override def hasIngredient(itemStack: ItemStack): Boolean = itemStack != null && hasIngredient(itemStack.getType)
    override def hasIngredient(material: Material): Boolean = materials.contains(material)
    override def getIngredientStacks(): Iterable[ItemStack] = new Iterable[ItemStack]() {
        override def iterator: Iterator[ItemStack] = JavaConverters.asScalaIterator(materials.iterator()).map(new ItemStack(_))
    }
}
trait FireworkStarFadeRecipe extends ComplexRecipe with ConfigurationSerializable with FixedResult with FixedIngredients {
    private val materials = util.EnumSet.noneOf[Material](classOf[Material])
    for (material <- JavaConverters.asScalaIterator(MaterialUtils.dyesIngredient.iterator()) ++ Seq(FIREWORK_STAR, GUNPOWDER).iterator) {
        materials.add(material)
    }
    private val resultStack = new ItemBuilder(Material.FIREWORK_STAR)
        .changeMeta[FireworkEffectMeta](meta => {
        meta.setEffect(FireworkEffect.builder
            .`with`(FireworkEffect.Type.STAR)
            .withColor(Color.YELLOW)
            .withFlicker()
            .withTrail()
            .build())
    }).build()

    override def getResultStack(): ItemStack = resultStack
    override def hasResultType(material: Material): Boolean = material == FIREWORK_STAR

    override def hasIngredient(itemStack: ItemStack): Boolean = itemStack != null && hasIngredient(itemStack.getType)
    override def hasIngredient(material: Material): Boolean = materials.contains(material)
    override def getIngredientStacks(): Iterable[ItemStack] = new Iterable[ItemStack]() {
        override def iterator: Iterator[ItemStack] = JavaConverters.asScalaIterator(materials.iterator()).map(new ItemStack(_))
    }
}
trait FireworkStarRecipe extends ComplexRecipe with ConfigurationSerializable with FixedResult with FixedIngredients {
    private val materials = util.EnumSet.noneOf[Material](classOf[Material])
    for (material <- JavaConverters.asScalaIterator(MaterialUtils.dyesIngredient.iterator()) ++
        Seq(AIR, GUNPOWDER).iterator ++
        JavaConverters.asScalaIterator(MaterialUtils.fireworkShapesIngredient.iterator())) {
        materials.add(material)
    }
    private val resultStack = new ItemBuilder(Material.FIREWORK_STAR)
        .changeMeta[FireworkEffectMeta](meta => {
        meta.setEffect(FireworkEffect.builder
            .`with`(FireworkEffect.Type.STAR)
            .withColor(Color.YELLOW)
            .withFlicker()
            .withTrail()
            .build())
    }).build()

    override def getResultStack(): ItemStack = resultStack
    override def hasResultType(material: Material): Boolean = material == FIREWORK_STAR

    override def hasIngredient(itemStack: ItemStack): Boolean = itemStack != null && hasIngredient(itemStack.getType)
    override def hasIngredient(material: Material): Boolean = materials.contains(material)
    override def getIngredientStacks(): Iterable[ItemStack] = new Iterable[ItemStack]() {
        override def iterator: Iterator[ItemStack] = JavaConverters.asScalaIterator(materials.iterator()).map(new ItemStack(_))
    }
}
trait MapCloneRecipe extends ComplexRecipe with ConfigurationSerializable with FixedResult with FixedIngredients {
    private val materials = util.EnumSet.of[Material](FILLED_MAP, MAP)
    private val resultStack = new ItemBuilder(Material.FILLED_MAP)
        .changeMeta[MapMeta](meta => {
        meta.setScaling(false)
        meta.setColor(Color.WHITE)
        meta.setLocationName("Imagination Land")
        meta.setMapId(1337)
    }).build()

    override def getResultStack(): ItemStack = resultStack
    override def hasResultType(material: Material): Boolean = material == FILLED_MAP

    override def hasIngredient(itemStack: ItemStack): Boolean = itemStack != null && hasIngredient(itemStack.getType)
    override def hasIngredient(material: Material): Boolean = materials.contains(material)
    override def getIngredientStacks(): Iterable[ItemStack] = new Iterable[ItemStack]() {
        override def iterator: Iterator[ItemStack] = JavaConverters.asScalaIterator(materials.iterator()).map(new ItemStack(_))
    }
}
trait MapExtendRecipe extends ShapedRecipe with ComplexRecipe with ConfigurationSerializable with FixedResult with FixedIngredients {
    private val materials = util.EnumSet.of[Material](FILLED_MAP, PAPER)
    private val resultStack = new ItemBuilder(Material.FILLED_MAP)
        .changeMeta[MapMeta](meta => {
        meta.setScaling(true)
        meta.setColor(Color.BLACK)
        meta.setLocationName("Imagination Land")
        meta.setMapId(9001)
    }).build()

    override def getResultStack(): ItemStack = resultStack
    override def hasResultType(material: Material): Boolean = material == FILLED_MAP

    override def hasIngredient(itemStack: ItemStack): Boolean = itemStack != null && hasIngredient(itemStack.getType)
    override def hasIngredient(material: Material): Boolean = material == PAPER || material == FILLED_MAP
    override def getIngredientStacks(): Iterable[ItemStack] = new Iterable[ItemStack]() {
        override def iterator: Iterator[ItemStack] = JavaConverters.asScalaIterator(materials.iterator()).map(new ItemStack(_))
    }
}
trait RepairItemRecipe extends ComplexRecipe with ConfigurationSerializable with FixedResult with FixedIngredients {
    private val resultStack = new ItemBuilder(SHEARS)
        .changeMeta[ItemMeta with Damageable](meta => {
        meta.setDamage(1)
    }).build()

    override def getResultStack(): ItemStack = resultStack
    override def hasResultType(material: Material): Boolean = material != null && material.getMaxDurability > 0

    override def hasIngredient(itemStack: ItemStack): Boolean = itemStack != null && hasIngredient(itemStack.getType)
    override def hasIngredient(material: Material): Boolean = material != null && material.getMaxDurability > 0
    override def getIngredientStacks(): Iterable[ItemStack] = new Iterable[ItemStack]() {
        override def iterator: Iterator[ItemStack] = JavaConverters.asScalaIterator(MaterialUtils.breakableItems.iterator()).map(new ItemStack(_))
    }
}
trait ShieldDecorationRecipe extends ComplexRecipe with ConfigurationSerializable with FixedResult with FixedIngredients {
    private val materials = util.EnumSet.copyOf[Material](MaterialUtils.bannerIngredient)
    materials.add(SHIELD)
    private val resultStack = new ItemStack(Material.SHIELD)

    override def getResultStack(): ItemStack = resultStack
    override def hasResultType(material: Material): Boolean = material == SHIELD

    override def hasIngredient(itemStack: ItemStack): Boolean = itemStack != null && hasIngredient(itemStack.getType)
    override def hasIngredient(material: Material): Boolean = materials.contains(material)
    override def getIngredientStacks(): Iterable[ItemStack] = new Iterable[ItemStack]() {
        override def iterator: Iterator[ItemStack] = JavaConverters.asScalaIterator(materials.iterator()).map(new ItemStack(_))
    }
}
trait ShulkerBoxColorRecipe extends ComplexRecipe with ConfigurationSerializable with FixedResult with FixedIngredients {
    private val materials = util.EnumSet.noneOf[Material](classOf[Material])
    for (material <- JavaConverters.asScalaIterator(MaterialUtils.shulkerBoxIngredient.iterator()) ++
        JavaConverters.asScalaIterator(MaterialUtils.dyesIngredient.iterator())) {
        materials.add(material)
    }
    private val resultStack = new ItemStack(MAGENTA_SHULKER_BOX)

    override def getResultStack(): ItemStack = resultStack
    override def hasResultType(material: Material): Boolean = MaterialUtils.shulkerBoxIngredient.contains(material)

    override def hasIngredient(itemStack: ItemStack): Boolean = itemStack != null && hasIngredient(itemStack.getType)
    override def hasIngredient(material: Material): Boolean = materials.contains(material)
    override def getIngredientStacks(): Iterable[ItemStack] = new Iterable[ItemStack]() {
        override def iterator: Iterator[ItemStack] = JavaConverters.asScalaIterator(materials.iterator()).map(new ItemStack(_))
    }
}
trait TippedArrowRecipe extends ComplexRecipe with ConfigurationSerializable with FixedResult with FixedIngredients {
    private val materials = util.EnumSet.of[Material](LINGERING_POTION, ARROW)
    private val resultStack = new ItemBuilder(Material.TIPPED_ARROW)
        .changeMeta[PotionMeta](meta => {
        meta.setColor(Color.GREEN)
        meta.setBasePotionData(new PotionData(PotionType.POISON))
    }).build()

    override def getResultStack(): ItemStack = resultStack
    override def hasResultType(material: Material): Boolean = material == TIPPED_ARROW

    override def hasIngredient(itemStack: ItemStack): Boolean = itemStack != null && hasIngredient(itemStack.getType)
    override def hasIngredient(material: Material): Boolean = materials.contains(material)
    override def getIngredientStacks(): Iterable[ItemStack] = new Iterable[ItemStack]() {
        override def iterator: Iterator[ItemStack] = JavaConverters.asScalaIterator(materials.iterator()).map(new ItemStack(_))
    }
}
