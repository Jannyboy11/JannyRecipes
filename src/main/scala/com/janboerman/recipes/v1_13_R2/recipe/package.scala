package com.janboerman.recipes.v1_13_R2

import net.minecraft.server.v1_13_R2._
import com.janboerman.recipes.v1_13_R2.Extensions.NmsKey

package object recipe {

    //TODO move to the customnms package?
    type Ingredient = ItemStack => Boolean

    val armorDye = new RecipeArmorDye(NmsKey("armor_dye"))
    val bannerAddPattern = new RecipeBannerAdd(NmsKey("banner_add_pattern"))
    val bannerDuplicate = new RecipeBannerDuplicate(NmsKey("banner_duplicate"))
    val bookClone = new RecipeBookClone(NmsKey("book_cloning"))
    val fireworkRocket = new RecipeFireworks(NmsKey("firework_rocket"))
    val fireworkStarFade = new RecipeFireworksFade(NmsKey("firework_star_fade"))
    val fireworkStar = new RecipeFireworksStar(NmsKey("firework_star"))
    val mapClone = new RecipeMapClone(NmsKey("map_cloning"))
    val mapExtending = new RecipeMapExtend(NmsKey("map_extending"))
    val repairItem = new RecipeRepair(NmsKey("repair_item"))
    val shieldDecoration = new RecipiesShield(NmsKey("shield_decoration"))
    val shulkerBoxColor = new RecipeShulkerBox(NmsKey("shulker_box_coloring"))
    val tippedArrow = new RecipeTippedArrow(NmsKey("tipped_arrow"))

}
