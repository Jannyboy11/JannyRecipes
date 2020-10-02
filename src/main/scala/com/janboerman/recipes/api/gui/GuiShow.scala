package com.janboerman.recipes.api.gui

import org.bukkit.block.banner.{Pattern, PatternType}
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta._
import org.bukkit.potion.{PotionData, PotionType}
import org.bukkit.{Color, DyeColor, FireworkEffect, Material}
import xyz.janboerman.guilib.api.ItemBuilder
import com.janboerman.recipes.api.recipe._

//TODO do I want/need this? probabaly not. It seemed nice in theory though.
//TODO currently it's unused
object GuiShow {
    implicit val itemstackShow = new GuiShow[ItemStack] {
        override def getRepresentation(thing: ItemStack): ItemStack = thing
    }

    implicit val materialShow = new GuiShow[Material] {
        override def getRepresentation(thing: Material): ItemStack = new ItemStack(thing)
    }
}

trait GuiShow[T] {

    def getRepresentation(thing: T): ItemStack

}

object ComplexRecipeInstances {
    implicit val armorDyeShow = new GuiShow[ArmorDyeRecipe] {
        private lazy val repr = new ItemBuilder(Material.LEATHER_CHESTPLATE)
            .changeMeta[LeatherArmorMeta](_.setColor(Color.WHITE))
            .build()

        override def getRepresentation(thing: ArmorDyeRecipe): ItemStack = repr
    }

    implicit val bannerAddPatternShow = new GuiShow[BannerAddPatternRecipe] {
        private lazy val repr = new ItemBuilder(Material.WHITE_BANNER)
            .changeMeta[BannerMeta](_.addPattern(new Pattern(DyeColor.RED, PatternType.CREEPER)))
            .build()

        override def getRepresentation(thing: BannerAddPatternRecipe): ItemStack = repr
    }

    implicit val bannerDuplicateShow = new GuiShow[BannerDuplicateRecipe] {
        private lazy val repr = new ItemBuilder(Material.WHITE_BANNER)
            .changeMeta[BannerMeta](_.addPattern(new Pattern(DyeColor.BLUE, PatternType.CREEPER)))
            .build()

        override def getRepresentation(thing: BannerDuplicateRecipe): ItemStack = repr
    }

    implicit val bookCloneShow = new GuiShow[BookCloneRecipe] {
        private lazy val repr = new ItemBuilder(Material.WRITTEN_BOOK)
            .changeMeta[BookMeta](meta => {
            meta.setAuthor("Jannyboy11")
            meta.setTitle("The Art of Crafting")
            meta.setGeneration(BookMeta.Generation.ORIGINAL)
            meta.setPages(java.util.Arrays.asList("First you create a recipes plugin, inspect Mojang's code, and then you know all recipes!"))
        }).build()

        override def getRepresentation(thing: BookCloneRecipe): ItemStack = repr
    }

    implicit val fireworkRocketShow = new GuiShow[FireworkRocketRecipe] {
        private lazy val repr = new ItemBuilder(Material.FIREWORK_ROCKET)
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

        override def getRepresentation(thing: FireworkRocketRecipe): ItemStack = repr
    }

    implicit val fireworkStarFadeShow = new GuiShow[FireworkStarFadeRecipe] {
        private lazy val repr = new ItemBuilder(Material.FIREWORK_STAR)
            .changeMeta[FireworkEffectMeta](meta => {
            meta.setEffect(FireworkEffect.builder
                .`with`(FireworkEffect.Type.STAR)
                .withColor(Color.YELLOW)
                .withFade(Color.ORANGE)
                .withFlicker()
                .withTrail()
                .build())
        }).build()

        override def getRepresentation(thing: FireworkStarFadeRecipe): ItemStack = repr
    }

    implicit val fireworkStarShow = new GuiShow[FireworkStarRecipe] {
        private lazy val repr = new ItemBuilder(Material.FIREWORK_STAR)
            .changeMeta[FireworkEffectMeta](meta => {
            meta.setEffect(FireworkEffect.builder
                .`with`(FireworkEffect.Type.STAR)
                .withColor(Color.YELLOW)
                .withFlicker()
                .withTrail()
                .build())
        }).build()

        override def getRepresentation(thing: FireworkStarRecipe): ItemStack = repr
    }

    implicit val mapCloneShow = new GuiShow[MapCloneRecipe] {
        private lazy val repr = new ItemBuilder(Material.FILLED_MAP)
            .changeMeta[MapMeta](meta => {
            meta.setScaling(false)
            meta.setColor(Color.WHITE)
            meta.setLocationName("Imagination Land")
            meta.setMapId(1337)
        }).build()

        override def getRepresentation(thing: MapCloneRecipe): ItemStack = repr
    }

    implicit val mapExtendShow = new GuiShow[MapExtendRecipe] {
        private lazy val repr = new ItemBuilder(Material.FILLED_MAP)
            .changeMeta[MapMeta](meta => {
            meta.setScaling(true)
            meta.setColor(Color.BLACK)
            meta.setLocationName("Imagination Land")
            meta.setMapId(9001)
        }).build()

        override def getRepresentation(thing: MapExtendRecipe): ItemStack = repr
    }

    implicit val repairItemShow = new GuiShow[RepairItemRecipe] {
        private lazy val repr = new ItemBuilder(Material.ANVIL).build()

        override def getRepresentation(thing: RepairItemRecipe): ItemStack = repr
    }

    implicit val shieldDecorationShow = new GuiShow[ShieldDecorationRecipe] {
        private lazy val repr = new ItemBuilder(Material.SHIELD)
            //            .changeMeta[BlockStateMeta](meta => {
            //                meta.setBlockState(new Banner())
            //        }) Why doesn't SHIELD just use the BannerMeta, that would be much more helpful dear Bukkit, thanks.
            .build()

        override def getRepresentation(thing: ShieldDecorationRecipe): ItemStack = repr
    }

    implicit val shulkerBoxColorShow = new GuiShow[ShulkerBoxColorRecipe] {
        private lazy val repr = new ItemBuilder(Material.MAGENTA_SHULKER_BOX)
            .build()

        override def getRepresentation(thing: ShulkerBoxColorRecipe): ItemStack = repr
    }

    implicit val tippedArrowShow = new GuiShow[TippedArrowRecipe] {
        private lazy val repr = new ItemBuilder(Material.TIPPED_ARROW)
            .changeMeta[PotionMeta](meta => {
            meta.setColor(Color.GREEN)
            meta.setBasePotionData(new PotionData(PotionType.POISON))
        }).build()

        override def getRepresentation(thing: TippedArrowRecipe): ItemStack = repr
    }

}

object RegularRecipeInstances {
    implicit val shapedRecipeShow = new GuiShow[ShapedRecipe] {
        override def getRepresentation(thing: ShapedRecipe): ItemStack = thing.getResult()
    }

    implicit val shapelessRecipeShow = new GuiShow[ShapedRecipe] {
        override def getRepresentation(thing: ShapedRecipe): ItemStack = thing.getResult()
    }

    implicit val furnaceRecipeShow = new GuiShow[FurnaceRecipe] {
        override def getRepresentation(thing: FurnaceRecipe): ItemStack = thing.getResult()
    }

    //    implicit val unknownRecipeShow = new GuiShow[Recipe with Keyed] {
    //        override def getRepresentation(thing: Recipe): ItemStack = new ItemBuilder(Material.STRUCTURE_VOID)
    //              .name(thing.getKey().toString())
    //              .build()
    //    } //this should really be the fallback
}

