package xyz.janboerman.recipes.api

import org.bukkit.{Bukkit, NamespacedKey}
import xyz.janboerman.recipes.api.gui.RecipeGuiFactory
import xyz.janboerman.recipes.api.recipe.Recipe

import scala.collection.JavaConverters

trait JannyRecipesAPI {

    def fromBukkitRecipe(bukkit: org.bukkit.inventory.Recipe): Recipe

    def toBukkitRecipe(janny: Recipe): org.bukkit.inventory.Recipe

    def getRecipe(key: NamespacedKey): Recipe

    def addRecipe(key: NamespacedKey, recipe: Recipe): Boolean

    def removeRecipe(recipe: Recipe): Boolean

    def isRegistered(recipe: Recipe): Boolean

    def iterateRecipes(): Iterator[_ <: Recipe] = {
        JavaConverters.asScalaIterator(Bukkit.recipeIterator()).map(fromBukkitRecipe)
    }

    def getGuiFactory(): RecipeGuiFactory

    //TODO def getPersistentStorage(): PersistentRecipeStorage
}
