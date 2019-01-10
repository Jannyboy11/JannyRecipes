package xyz.janboerman.recipes.api

import java.util
import java.util.stream.Collectors

import org.bukkit.Material._

object MaterialUtils {

    lazy val dyesIngredient = util.List.of(
        INK_SAC,
        ROSE_RED,
        CACTUS_GREEN,
        COCOA_BEANS,
        LAPIS_LAZULI,
        PURPLE_DYE,
        CYAN_DYE,
        LIGHT_GRAY_DYE,
        GRAY_DYE,
        PINK_DYE,
        LIME_DYE,
        DANDELION_YELLOW,
        LIGHT_BLUE_DYE,
        MAGENTA_DYE,
        ORANGE_DYE,
        BONE_MEAL)

    lazy val bannerIngredient = util.List.of(
        WHITE_BANNER,
        ORANGE_BANNER,
        MAGENTA_BANNER,
        LIGHT_BLUE_BANNER,
        YELLOW_BANNER,
        LIME_BANNER,
        PINK_BANNER,
        GRAY_BANNER,
        LIGHT_GRAY_BANNER,
        CYAN_BANNER,
        PURPLE_BANNER,
        BLUE_BANNER,
        BROWN_BANNER,
        GREEN_BANNER,
        RED_BANNER,
        BLACK_BANNER
    )

    lazy val fireworkShapesIngredient = util.List.of(
        FIRE_CHARGE,
        FEATHER,
        GOLD_NUGGET,
        SKELETON_SKULL,
        WITHER_SKELETON_SKULL,
        CREEPER_HEAD,
        PLAYER_HEAD,
        DRAGON_HEAD,
        ZOMBIE_HEAD,
    )

    lazy val shulkerBoxIngredient = util.List.of(
        SHULKER_BOX,
        BLACK_SHULKER_BOX,
        BLUE_SHULKER_BOX,
        BROWN_SHULKER_BOX,
        CYAN_SHULKER_BOX,
        GRAY_SHULKER_BOX,
        GREEN_SHULKER_BOX,
        LIGHT_BLUE_SHULKER_BOX,
        LIGHT_GRAY_SHULKER_BOX,
        LIME_SHULKER_BOX,
        MAGENTA_SHULKER_BOX,
        ORANGE_SHULKER_BOX,
        PINK_SHULKER_BOX,
        PURPLE_SHULKER_BOX,
        RED_SHULKER_BOX,
        WHITE_SHULKER_BOX,
        YELLOW_SHULKER_BOX
    )

    private def isMadeOfToolMaterial(material: org.bukkit.Material): Boolean = {
        val name = material.name()

        (name.startsWith("WOODEN")
            || name.startsWith("STONE")
            || name.startsWith("IRON")
            || name.startsWith("GOLDEN")
            || name.startsWith("DIAMOND"))
    }

    private def isTool(material: org.bukkit.Material): Boolean = {
        val name = material.name()

        (isMadeOfToolMaterial(material) &&
            (name.endsWith("AXE") //includes PICKAXE
                || name.endsWith("SHOVEL")
                || name.endsWith("HOE"))) ||
            material == SHEARS ||
            material == FISHING_ROD ||
            material == FLINT_AND_STEEL
    }

    private def isWeapon(material: org.bukkit.Material): Boolean = {
        val name = material.name()

        (isMadeOfToolMaterial(material) &&  name.endsWith("SWORD")) ||
            name.endsWith("BOW") ||
            material == TRIDENT
            //TODO crossbow
    }

    private def isArmour(material: org.bukkit.Material): Boolean = {
        val name = material.name()
        //don't count horse armour because it is unbreakable
        material == SHIELD ||
        material == TURTLE_HELMET ||
        material == LEATHER_HELMET ||
        material == LEATHER_CHESTPLATE ||
        material == LEATHER_LEGGINGS ||
        material == LEATHER_BOOTS ||
        (isMadeOfToolMaterial(material) &&
            (name.endsWith("HELMET")
                || name.endsWith("CHESTPLATE")
                || name.endsWith("LEGGINGS")
                || name.endsWith("BOOTS")))
    }

    lazy val breakableItems = util.Arrays.stream(org.bukkit.Material.values())
        .filter(m => isTool(m)
            || isWeapon(m)
            || isArmour(m))
        .collect(Collectors.toList())


}
