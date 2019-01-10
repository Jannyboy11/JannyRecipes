package xyz.janboerman.recipes.api.recipe

import org.bukkit.Keyed
import org.bukkit.inventory.{FurnaceInventory, ItemStack}

trait SmeltingRecipe extends Recipe with Keyed {

    def getResult(): ItemStack

    //furnaces don't actually call craftItem on the recipe, they just call getResult
    //in the Janny implementation I can do some ugly hacks to circumvent this xD

    def trySmelt(furnaceInventory: FurnaceInventory): Option[ItemStack]

    //sadly, TileEntityFurnace doesn't ever call the method in IRecipe that returns the remaining items
    //in the future I might just override TileEntityFurnace with my own implementation

    def getGroup(): Option[String] = None

    def getCookingTime(): Int = 200
    def getExperience(): Float = 0F

    //TODO should the trySmelt method return a Tuple2<ItemStack, Float> instead?
    //TODO in the nms FurnaceRecipe#g() (which is .getExperience()) is called by the SlotFurnaceResult
    //TODO when is that called? when the player clicks the result slot.
    //TODO the tile entity furnace counts how many xp it got per recipe. so no.
    //TODO there is a lazy multiplication instead of a strict sum.

}
