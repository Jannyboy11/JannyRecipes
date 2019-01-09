package xyz.janboerman.recipes.api.recipe

import org.bukkit.NamespacedKey
import org.bukkit.inventory.{FurnaceInventory, ItemStack}

object FurnaceRecipe {
    def unapply(arg: FurnaceRecipe): Option[(NamespacedKey, Option[String], FurnaceIngredient, ItemStack, Int, Float)] =
        Some(arg.getKey, arg.getGroup(), arg.getIngredient(), arg.getResult(), arg.getCookingTime(), arg.getExperience())

    def apply(key: NamespacedKey, ingredient: FurnaceIngredient, result: ItemStack): FurnaceRecipe =
        new SimpleFurnaceRecipe(key, ingredient, result)

    def apply(key: NamespacedKey, ingredient: FurnaceIngredient, result: ItemStack, cookingTime: Int, experience: Float): FurnaceRecipe =
        new SimpleFurnaceRecipe(key, ingredient, result, cookingTime, experience)

    def apply(key: NamespacedKey, group: Option[String], ingredient: FurnaceIngredient, result: ItemStack, cookingTime: Int, experience: Float): FurnaceRecipe =
        new SimpleFurnaceRecipe(key, group, ingredient, result, cookingTime, experience)

    def apply(key: NamespacedKey, group: Option[String], ingredient: FurnaceIngredient, result: ItemStack): FurnaceRecipe =
        apply(key, group, ingredient, result, 200, 0F)
}

trait FurnaceRecipe extends SmeltingRecipe
    with FixedIngredients
    with FixedResult {

    override def getResultStack(): ItemStack = getResult()
    override def getIngredientStacks: Iterable[ItemStack] = Seq(getIngredient().getItemStack())

    def getIngredient(): FurnaceIngredient

    override def trySmelt(furnaceInventory: FurnaceInventory): Option[ItemStack] = {
        val acceptedByIngredient = getIngredient().apply(furnaceInventory.getSmelting)
        if (acceptedByIngredient) {
            Some(getResult())
        } else {
            None
        }
    }
}

class SimpleFurnaceRecipe(private val namespacedKey: NamespacedKey,
                          private val group: Option[String],
                          private val furnaceIngredient: FurnaceIngredient,
                          private val result: ItemStack,
                          private val cookingTime: Int,
                          private val experience: Float)
    extends FurnaceRecipe {

    def this(namespacedKey: NamespacedKey, furnaceIngredient: FurnaceIngredient, result: ItemStack, cookingTme: Int, experience: Float) = this(namespacedKey, None, furnaceIngredient, result, cookingTme, experience)
    def this(namespacedKey: NamespacedKey, furnaceIngredient: FurnaceIngredient, result: ItemStack) = this(namespacedKey, furnaceIngredient, result, 200, 0F)
    def this(namespacedKey: NamespacedKey, furnaceIngredient: FurnaceIngredient, result: ItemStack, cookingTime: Int) = this(namespacedKey, furnaceIngredient, result, cookingTime, 0F)
    def this(namespacedKey: NamespacedKey, furnaceIngredient: FurnaceIngredient, result: ItemStack, experience: Float) = this(namespacedKey, furnaceIngredient, result, 200, 0F)

    override def getIngredient(): FurnaceIngredient = furnaceIngredient

    override def getResult(): ItemStack = if (result == null) null else result.clone()

    override def getKey: NamespacedKey = namespacedKey

    override def getGroup(): Option[String] = group

}