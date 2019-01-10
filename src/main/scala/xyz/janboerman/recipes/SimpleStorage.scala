package xyz.janboerman.recipes

import org.bukkit.configuration.serialization.ConfigurationSerializable
import org.bukkit.plugin.Plugin
import xyz.janboerman.recipes.api.persist.RecipeStorage
import xyz.janboerman.recipes.api.recipe.Recipe

import scala.util.Try

class SimpleStorage(private val plugin: Plugin) extends RecipeStorage {

    override def init(): Boolean = {
        //TODO

        false
    }

    override def saveRecipe(recipe: Recipe with ConfigurationSerializable): Try[Unit] = ???

    override def loadRecipes(): Try[Iterator[_ <: Recipe with ConfigurationSerializable]] = ???

    override def deleteRecipe(recipe: Recipe with ConfigurationSerializable): Try[Unit] = ???

}
