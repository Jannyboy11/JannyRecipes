package xyz.janboerman.recipes.v1_13_R2

import org.bukkit.configuration.serialization.ConfigurationSerializable
import xyz.janboerman.recipes.RecipesPlugin
import xyz.janboerman.recipes.api.persist.RecipeStorage
import xyz.janboerman.recipes.api.recipe.Recipe
import RecipesPlugin.registerClass
import xyz.janboerman.recipes.v1_13_R2.recipe.janny._

import scala.util.Try

object Storage_1_13_R2 extends RecipeStorage {

    private lazy val simpleStorage = RecipesPlugin.persistentStorage

    override def init(): Boolean = {
        simpleStorage.init()

        //TODO JannyShaped, JannyShapeless, JannyFurnace

        registerClass(classOf[JannyArmorDye])
        registerClass(classOf[JannyBannerAddPattern])
        registerClass(classOf[JannyBannerDuplicate])
        registerClass(classOf[JannyBookClone])
        registerClass(classOf[JannyFireworkRocket])
        registerClass(classOf[JannyFireworkStarFade])
        registerClass(classOf[JannyFireworkStar])
        registerClass(classOf[JannyMapClone])
        registerClass(classOf[JannyMapExtend])
        registerClass(classOf[JannyRepairItem])
        registerClass(classOf[JannyShieldDecoration])
        registerClass(classOf[JannyShulkerBoxColor])
        registerClass(classOf[JannyTippedArrow])

        //TODO do dome files / folders setup?

        true
    }

    override def saveRecipe(recipe: Recipe with ConfigurationSerializable): Try[Unit] = ???

    override def loadRecipes(): Try[Iterator[_ <: Recipe with ConfigurationSerializable]] = ???

    override def deleteRecipe(recipe: Recipe with ConfigurationSerializable): Try[Unit] = ???
}
