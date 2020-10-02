package com.janboerman.recipes.api.persist

import java.util

import com.janboerman.recipes.api.JannyRecipesAPI
import com.janboerman.recipes.api.recipe.Recipe
import org.bukkit.Keyed
import org.bukkit.configuration.serialization.{ConfigurationSerializable, SerializableAs}
import com.janboerman.recipes.api.JannyRecipesAPI
import com.janboerman.recipes.api.recipe.{NamespacedRecipeKey, Recipe, RecipeKey}

trait DisableCondition extends ConfigurationSerializable {
    /** Check whether the recipe matches this condition */
    def matches(recipe: Recipe): Boolean
    /** Actively disable recipes that satisfy this condition */
    def disableRecipes()(implicit api: JannyRecipesAPI): Boolean
}

object DisableByKeyCondition {
    def valueOf(map: java.util.Map[String, AnyRef]): DisableByKeyCondition =
        DisableByKeyCondition(map.get("key").asInstanceOf[RecipeKey])
}

@SerializableAs("ByKeyCondition")
case class DisableByKeyCondition(key: RecipeKey) extends DisableCondition {
    override def matches(recipe: Recipe): Boolean = recipe match {
        case keyedRecipe: Keyed => key == new NamespacedRecipeKey(keyedRecipe.getKey)
        case _ => false
    }

    override def serialize(): util.Map[String, AnyRef] = {
        return util.Map.of("key", key)
    }

    override def disableRecipes()(implicit api: JannyRecipesAPI): Boolean = {
        key match {
            case ns: NamespacedRecipeKey =>
                val recipe = api.getRecipe(ns.namespacedKey)
                api.removeRecipe(recipe)
            case _ => false
        }
    }
}
