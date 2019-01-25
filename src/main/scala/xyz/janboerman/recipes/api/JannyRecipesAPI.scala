package xyz.janboerman.recipes.api

import org.bukkit.plugin.Plugin
import org.bukkit.{Bukkit, NamespacedKey}
import xyz.janboerman.recipes.api.gui.RecipeGuiFactory
import xyz.janboerman.recipes.api.persist.RecipeStorage
import xyz.janboerman.recipes.api.recipe.Recipe

import scala.collection.JavaConverters

trait JannyRecipesAPI {

    //TODO overloads for shaped, shapeless and furnace recipes only instead of this type-unsafe method?
    def fromBukkitRecipe(bukkit: org.bukkit.inventory.Recipe): Recipe

    //TODO overloads for shaped, shapeless and furnace recipes only instead of this type-unsafe method?
    def toBukkitRecipe(janny: Recipe): org.bukkit.inventory.Recipe

    def getRecipe(key: NamespacedKey): Recipe

    //TODO overloads for crafting recipes / smelting recipes? (later brewing recipes?)
    def addRecipe(recipe: Recipe): Boolean

    def removeRecipe(recipe: Recipe): Boolean

    def isRegistered(recipe: Recipe): Boolean

    def iterateRecipes(): Iterator[_ <: Recipe] = {
        JavaConverters.asScalaIterator(Bukkit.recipeIterator()).map(fromBukkitRecipe)
    }

    def getGuiFactory[P <: Plugin](): RecipeGuiFactory[P]

    def persist(): RecipeStorage
}
