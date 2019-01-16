package xyz.janboerman.recipes.api.persist

import java.io.{File, IOException}

import org.bukkit.Keyed
import org.bukkit.configuration.file.{FileConfiguration, YamlConfiguration}
import org.bukkit.configuration.serialization.{ConfigurationSerializable, ConfigurationSerialization, SerializableAs}
import org.bukkit.plugin.Plugin
import xyz.janboerman.recipes.api.persist.RecipeStorage.{SerializableList, SerializableMap}
import xyz.janboerman.recipes.api.recipe._

class SimpleStorage(val plugin: Plugin) extends RecipeStorage {

    private var recipesFolder: File = _

    def getRecipesFolder(): File = {
        recipesFolder
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

        true
    }

    override def saveRecipe(recipe: Recipe with ConfigurationSerializable): Either[String, Unit] = {
        if (recipe.isInstanceOf[Keyed]) {
            val key = recipe.asInstanceOf[Keyed].getKey
            val folder = getRecipesFolder()

            val fileName = key.toString.replaceAll("\\W", "_") + ".yml"

            val fileConfiguration = new YamlConfiguration()
            fileConfiguration.set("recipe", recipe)

            try {
                val saveFile = new File(folder, fileName)
                if (!saveFile.exists()) saveFile.createNewFile()
                fileConfiguration.save(saveFile)
                return Right(())
            } catch {
                case e: IOException =>
                    e.printStackTrace()
                    return Left("Error occured while saving a recipe to a file.")
            }
        }

        Left("The recipe hasn't got a key.")
    }

    override def loadRecipes(): Either[String, Iterator[Recipe with ConfigurationSerializable]] = {

        ???
    }

    override def deleteRecipe(recipe: Recipe with ConfigurationSerializable): Either[String, Unit] = {

        ???
    }

}
