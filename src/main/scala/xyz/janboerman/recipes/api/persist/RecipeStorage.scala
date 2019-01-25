package xyz.janboerman.recipes.api.persist

import java.util

import org.bukkit.configuration.serialization.{ConfigurationSerializable, SerializableAs}
import xyz.janboerman.recipes.api.persist.RecipeStorage.ElementsString
import xyz.janboerman.recipes.api.recipe.Recipe

import scala.collection.JavaConverters

object RecipeStorage {
    val StringString = "string"
    val NamespaceString = "namespace"
    val KeyString = "key"
    val UUIDString = "uuid"
    val ResultString = "result"
    val ShapeString = "shape"
    val IngredientsString = "ingredients"
    val ItemStackString = "itemstack"
    val PatternString = "pattern"
    val GroupString = "group"
    val ChoicesString = "choices"
    val ExactString = "exact"
    val ElementsString = "elements"
    val CookingTimeString = "cooking-time"
    val ExperienceString = "experience"
}

object SerializableList {
    def valueOf[A](map: util.Map[String, AnyRef]): SerializableList[A] = {
        val javaList = map.get(ElementsString).asInstanceOf[util.List[A]]
        val scalaList = JavaConverters.asScalaBuffer(javaList)
        new SerializableList(scalaList.toList)
    }
}

@SerializableAs("SerializableList")
class SerializableList[+A](val list: List[A] /*is appearantly null when deserialized using valueOf?*/) extends ConfigurationSerializable {
    override def serialize(): util.Map[String, AnyRef] = {
        val javaMap = new util.HashMap[String, AnyRef]()
        val scalaList = list.toBuffer
        javaMap.put(ElementsString, JavaConverters.bufferAsJavaList(scalaList))
        javaMap
    }

    override def toString: String = list.toString()
    override def equals(obj: Any): Boolean = obj match {
        case sl: SerializableList[_] => sl.list == this.list
        case _ => false
    }
    override def hashCode(): Int = list.hashCode()
}

object SerializableMap {
    def valueOf[A](map: util.Map[String, AnyRef]): SerializableMap[A] = {
        val javaMap = map.get(ElementsString).asInstanceOf[util.Map[String, A]]
        val scalaMap = JavaConverters.mapAsScalaMap(javaMap)
        new SerializableMap(scalaMap.toMap)
    }
}

@SerializableAs("SerializableMap")
class SerializableMap[+A](val map: Map[String, A]) extends ConfigurationSerializable {
    override def serialize(): util.Map[String, AnyRef] = {
        val map = new util.HashMap[String, AnyRef]()
        map.put(ElementsString, JavaConverters.mapAsJavaMap(this.map))
        map
    }

    override def toString: String = map.toString()
    override def equals(obj: Any): Boolean = obj match {
        case sm: SerializableMap[_] => sm.map == this.map
        case _ => false
    }
    override def hashCode(): Int = map.hashCode()
}

trait RecipeStorage {

    def init(): Boolean

    def saveRecipe(recipe: Recipe with ConfigurationSerializable): Either[String, Unit] //TODO Either[String, CompletableFuture[Unit]]? Future[Either[String, Unit]]?

    def loadRecipes(): Either[String, Iterator[Recipe with ConfigurationSerializable]]

    def deleteRecipe(recipe: Recipe with ConfigurationSerializable): Either[String, Unit] //TODO Either[String, CompletableFuture[Unit]]? Future[Either[String, Unit]]?

}
