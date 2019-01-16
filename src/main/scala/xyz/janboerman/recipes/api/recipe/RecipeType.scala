package xyz.janboerman.recipes.api.recipe

import org.bukkit.Material._
import org.bukkit.block.banner.{Pattern, PatternType}
import org.bukkit.{Color, DyeColor, FireworkEffect, Material}
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta._
import org.bukkit.potion.{PotionData, PotionType}
import xyz.janboerman.guilib.api.ItemBuilder

import scala.collection.mutable

object RecipeType {
    val Values: mutable.Set[RecipeType] = new mutable.LinkedHashSet()
    Values.add(ShapedType)
    Values.add(ShapelessType)
    Values.add(FurnaceType)

    Values.add(ArmorDyeType)
    Values.add(BannerAddPatternType)
    Values.add(BannerDuplicateType)
    Values.add(BookCloneType)
    Values.add(FireworkRocketType)
    Values.add(FireworkStarFadeType)
    Values.add(FireworkStarType)
    Values.add(MapCloneType)
    Values.add(MapExtendType)
    Values.add(RepairItemType)
    Values.add(ShieldDecorationType)
    Values.add(ShulkerBoxColorType)
    Values.add(TippedArrowType)
}

trait RecipeType {
    def getName(): String
    def getIcon(): ItemStack
}

case object ShapedType extends RecipeType {
    override def getName() = "Shaped"
    override def getIcon() = new ItemStack(CRAFTING_TABLE)
}
case object ShapelessType extends RecipeType {
    override def getName() = "Shapeless"
    override def getIcon() = new ItemStack(CRAFTING_TABLE)
}
case object FurnaceType extends RecipeType {
    override def getName() = "Furnace"
    override def getIcon() = new ItemStack(FURNACE)
}
trait ComplexType extends RecipeType
case object ArmorDyeType extends ComplexType {
    private val icon = new ItemBuilder(LEATHER_CHESTPLATE)
        .changeMeta[LeatherArmorMeta](_.setColor(Color.WHITE))
        .build()

    override def getName() =  "ArmorDye"
    override def getIcon() = icon
}
case object BannerAddPatternType extends ComplexType {
    private val icon = new ItemBuilder(WHITE_BANNER)
        .changeMeta[BannerMeta](_.addPattern(new Pattern(DyeColor.RED, PatternType.CREEPER)))
        .build()

    override def getName() = "BannerAddPattern"
    override def getIcon() = icon
}
case object BannerDuplicateType extends ComplexType {
    private val icon = new ItemBuilder(WHITE_BANNER)
        .changeMeta[BannerMeta](_.addPattern(new Pattern(DyeColor.BLUE, PatternType.CREEPER)))
        .build()

    override def getName() = "BannerDuplicate"
    override def getIcon() = icon
}
case object BookCloneType extends ComplexType {
    private val icon = new ItemBuilder(WRITTEN_BOOK)
        .changeMeta[BookMeta](meta => {
        meta.setAuthor("Jannyboy11")
        meta.setTitle("The Art of Crafting")
        meta.setGeneration(BookMeta.Generation.ORIGINAL)
        meta.setPages(java.util.Arrays.asList("First you create a recipes plugin, inspect Mojang's code, and then you know all recipes!"))
    }).build()

    override def getName() = "BookClone"
    override def getIcon() = icon
}
case object FireworkRocketType extends ComplexType {
    private val icon = new ItemBuilder(FIREWORK_ROCKET)
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

    override def getName() = "FireworkRocket"
    override def getIcon() = icon
}
case object FireworkStarFadeType extends ComplexType {
    private val icon = new ItemBuilder(FIREWORK_STAR)
        .changeMeta[FireworkEffectMeta](meta => {
        meta.setEffect(FireworkEffect.builder
            .`with`(FireworkEffect.Type.STAR)
            .withColor(Color.YELLOW)
            .withFlicker()
            .withTrail()
            .build())
    }).build()

    override def getName() = "FireworkStarFade"
    override def getIcon() = icon
}
case object FireworkStarType extends ComplexType {
    private val icon = new ItemBuilder(FIREWORK_STAR)
        .changeMeta[FireworkEffectMeta](meta => {
        meta.setEffect(FireworkEffect.builder
            .`with`(FireworkEffect.Type.STAR)
            .withColor(Color.YELLOW)
            .withFlicker()
            .withTrail()
            .build())
    }).build()

    override def getName() = "FireworkStar"
    override def getIcon() = icon
}
case object MapCloneType extends ComplexType {
    private val icon = new ItemBuilder(FILLED_MAP)
        .changeMeta[MapMeta](meta => {
        meta.setScaling(false)
        meta.setColor(Color.WHITE)
        meta.setLocationName("Imagination Land")
        meta.setMapId(1337)
    }).build()

    override def getName() = "MapClone"
    override def getIcon() = icon
}
case object MapExtendType extends ComplexType {
    private val icon = new ItemBuilder(FILLED_MAP)
        .changeMeta[MapMeta](meta => {
        meta.setScaling(true)
        meta.setColor(Color.BLACK)
        meta.setLocationName("Imagination Land")
        meta.setMapId(9001)
    }).build()

    override def getName() = "MapExtend"
    override def getIcon() = icon
}
case object RepairItemType extends ComplexType {
    private val icon = new ItemBuilder(SHEARS)
        .changeMeta[ItemMeta with Damageable](meta => {
        meta.setDamage(1)
    }).build()

    override def getName() = "RepairItem"
    override def getIcon() = icon
}
case object ShieldDecorationType extends ComplexType {
    private val icon = new ItemStack(SHIELD)

    override def getName() = "ShieldDecoration"
    override def getIcon() = icon
}
case object ShulkerBoxColorType extends ComplexType {
    private val icon = new ItemStack(MAGENTA_SHULKER_BOX)

    override def getName() = "ShulkerBoxColor"
    override def getIcon() = icon
}
case object TippedArrowType extends ComplexType {
    private val icon = new ItemBuilder(TIPPED_ARROW)
        .changeMeta[PotionMeta](meta => {
        meta.setColor(Color.GREEN)
        meta.setBasePotionData(new PotionData(PotionType.POISON))
    }).build()

    override def getName() = "TippedArrow"
    override def getIcon() = icon
}



