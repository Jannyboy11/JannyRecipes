package com.janboerman.recipes.api

import com.janboerman.recipes.api.gui.RecipeGuiFactory
import com.janboerman.recipes.api.persist.RecipeStorage
import com.janboerman.recipes.api.recipe.Recipe
import org.bukkit.plugin.Plugin
import org.bukkit.{Bukkit, NamespacedKey}

import scala.jdk.CollectionConverters._

trait JannyRecipesAPI /*TODO plugin type parameter? so that I don't have to parameterize getGuiFactory*/ {

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
        Bukkit.recipeIterator().asScala.map(fromBukkitRecipe)
    }

    def getGuiFactory[P <: Plugin](): RecipeGuiFactory[P]

    def persist(): RecipeStorage
}
