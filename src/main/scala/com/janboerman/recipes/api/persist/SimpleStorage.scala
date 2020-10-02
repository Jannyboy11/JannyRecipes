package com.janboerman.recipes.api.persist

import java.io.{File, IOException}
import java.util.logging.Level

import com.janboerman.recipes.api.JannyRecipesAPI
import com.janboerman.recipes.api.recipe.SimpleShapelessRecipe
import org.bukkit.{Keyed, NamespacedKey}
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.configuration.serialization.{ConfigurationSerializable, ConfigurationSerialization, SerializableAs}
import org.bukkit.plugin.Plugin
import com.janboerman.recipes.api.JannyRecipesAPI
import com.janboerman.recipes.api.recipe._

import scala.collection.{JavaConverters, mutable}
import scala.util.{Failure, Try}

//TODO why does this require the 'api'?
class SimpleStorage(val plugin: Plugin)(implicit api: JannyRecipesAPI) extends RecipeStorage {

    //used to check whether a recipe should be disabled or deleted
    private val ourRecipes: mutable.Set[NamespacedKey] = new mutable.HashSet[NamespacedKey]()


    private var recipesFolder: File = _
    private var disabledFolder: File = _

    def getRecipesFolder(): File = {
        recipesFolder
    }

    def getDisabledFolder(): File = {
        if (disabledFolder == null) {
            disabledFolder = new File(plugin.getDataFolder, "disabled")
            if (!disabledFolder.exists()) disabledFolder.mkdirs()
        }
        disabledFolder
    }

    def registerClass(clazz: Class[_ <: ConfigurationSerializable]): Unit = {
        val option = getConfigurationAlias(clazz)
        if (option.isDefined) {
            ConfigurationSerialization.registerClass(clazz, option.get)
        } else {
            ConfigurationSerialization.registerClass(clazz)
        }
    }

    private def getConfigurationAlias(clazz: Class[_ <: ConfigurationSerializable]): Option[String] = {
        val serializableAs: SerializableAs = clazz.getAnnotation(classOf[SerializableAs])
        Option(serializableAs).map(_.value())
    }


    override def init(): Boolean = {
        //setup recipes folder
        recipesFolder = new File(plugin.getDataFolder, "recipes")
        if (!recipesFolder.exists()) recipesFolder.mkdirs()

        //register classes for yaml serialization
        registerClass(classOf[NamespacedRecipeKey])
        //registerClass(classOf[StringRecipeKey])
        //registerClass(classOf[UUIDRecipeKey])

        registerClass(classOf[SerializableList[_]])
        registerClass(classOf[SerializableMap[_]])

        registerClass(classOf[SimpleCraftingIngredient])
        registerClass(classOf[ExactCraftingIngredient])
        registerClass(classOf[SimpleFurnaceIngredient])

        registerClass(classOf[SimpleShapedRecipe])
        registerClass(classOf[SimpleShapelessRecipe])
        registerClass(classOf[SimpleFurnaceRecipe])

        registerClass(classOf[DisableByKeyCondition])

        true
    }

    override def shutdown(): Boolean = {
        ourRecipes.clear()
        true
    }

    private def saveFileName(key: NamespacedKey): String = key.toString.replaceAll("\\W", "_") + ".yml"

    override def saveRecipe(recipe: Recipe with ConfigurationSerializable): Either[String, Unit] = {
        var newlyCreated = true

        if (recipe.isInstanceOf[Keyed]) {
            val key = recipe.asInstanceOf[Keyed].getKey
            val folder = getRecipesFolder()

            val fileName: String = saveFileName(key)
            val saveFile = new File(folder, fileName)

            val fileConfiguration = new YamlConfiguration()
            fileConfiguration.set("recipe", recipe)

            //un-disable the recipe, if it was disabled.
            if (disabledFolder != null && disabledFolder.exists()) {
                val disabledFile = new File(getDisabledFolder(), fileName)
                if (disabledFile.exists()) {
                    newlyCreated = false
                    disabledFile.delete()
                }
            }

            if (newlyCreated) {
                try {
                    if (!saveFile.exists()) saveFile.createNewFile()
                    fileConfiguration.save(saveFile)

                    //keep track of recipes that are created by ourselves.
                    ourRecipes.add(key)
                    return Right(())
                } catch {
                    case e: IOException =>
                        e.printStackTrace()
                        return Left("Error occured while saving a recipe to a file.")
                }
            } else {
                //we un-disabled a pre-existing recipe.
                Right(())
            }
        } else {
            Left("The recipe hasn't got a key.")
        }
    }

    override def loadRecipes(): Either[String, Iterator[Recipe with ConfigurationSerializable]] = {
        val disableAttempt = Try {
            JavaConverters.asScalaIterator[File](java.util.Arrays.asList(getDisabledFolder()
                .listFiles((file: File) => file.isFile && file.getName.endsWith(".yml")): _*).iterator())
                .map(YamlConfiguration.loadConfiguration)
                .map(_.get("condition").asInstanceOf[DisableCondition])
                .foreach(_.disableRecipes)
        }

        disableAttempt match {
            case Failure(ex) =>
                plugin.getLogger.log(Level.SEVERE, "Error whilst loading disabled conditions", ex)
                //don't return yet. we want to try and load the extra enabled recipes anyway.
            case _ => ()
        }

        val enableAttempt = Try {
            JavaConverters.asScalaIterator[File](java.util.Arrays.asList(recipesFolder
                .listFiles((file: File) => file.isFile && file.getName.endsWith(".yml")): _*).iterator())
                .map(YamlConfiguration.loadConfiguration)
                .map(_.get("recipe").asInstanceOf[Recipe with ConfigurationSerializable])
                .map(recipe => {
                    if (recipe.isInstanceOf[Keyed]) {
                        ourRecipes.add(recipe.asInstanceOf[Keyed].getKey)
                    }
                    recipe
                })
        }
        if (enableAttempt.isSuccess) return Right(enableAttempt.get)
        val throwable = enableAttempt.toEither.swap.getOrElse(null)
        throwable.printStackTrace()
        Left("Could not load recipes from disk.")
    }

    override def deleteRecipe(recipe: Recipe with ConfigurationSerializable): Either[String, Unit] = {
        if (recipe.isInstanceOf[Keyed]) {
            val r = recipe.asInstanceOf[Recipe with ConfigurationSerializable with Keyed]
            val key = r.getKey

            if (ourRecipes.contains(key)) {
                //delete the recipe file

                val saveFile = new File(recipesFolder, saveFileName(key))
                if (saveFile.exists()) {
                    if (saveFile.delete()) {
                        ourRecipes.remove(key)
                        Right(())
                    } else {
                        Left(s"Could not delete save file of recipe $recipe.")
                    }
                } else {
                    //saveFile doesn't exist. nothing to do here.
                    Right(())
                }

            } else {
                //save it as disabled.

                val saveFile = new File(getDisabledFolder(), saveFileName(key))
                val fileConfiguration = new YamlConfiguration()
                fileConfiguration.set("condition", new DisableByKeyCondition(new NamespacedRecipeKey(key)))

                try {
                    if (!saveFile.exists()) saveFile.createNewFile()
                    fileConfiguration.save(saveFile)
                    Right(())
                } catch {
                    case e: IOException =>
                        e.printStackTrace()
                        Left("Error occurred when trying to save the disabled-recipe file.")
                }
            }

        } else {
            Left("The recipe hasn't got a key.")
        }
    }

}
