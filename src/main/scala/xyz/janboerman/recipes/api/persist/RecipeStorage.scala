package xyz.janboerman.recipes.api.persist

import java.util

import org.bukkit.configuration.serialization.{ConfigurationSerializable, SerializableAs}
import xyz.janboerman.recipes.api.recipe.Recipe

import scala.collection.{JavaConverters, mutable}
import scala.util.Try

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

    object SerializableList {
        def valueOf[A](map: util.Map[String, AnyRef]): SerializableList[A] = {
            val javaList = map.get(ElementsString).asInstanceOf[util.List[A]]
            val scalaList = JavaConverters.asScalaBuffer(javaList)
            new SerializableList[A](scalaList.toList)
        }
    }

    @SerializableAs("SerializableList")
    implicit class SerializableList[+A](val list: List[A]) extends ConfigurationSerializable {
        override def serialize(): util.Map[String, AnyRef] = {
            val javaMap = new util.HashMap[String, AnyRef]()
            val scalaList = new mutable.ListBuffer[A]()
            scalaList.addAll(list)
            javaMap.put(ElementsString, JavaConverters.bufferAsJavaList(scalaList))
            javaMap
        }

        override def toString: String = list.toString()
        override def equals(obj: Any): Boolean = obj match {
            case sl: SerializableList[A] => sl.list == this.list
            case _ => false
        }
        override def hashCode(): Int = list.hashCode()
    }

    object SerializableMap {
        def valueOf[V](map: util.Map[String, AnyRef]): SerializableMap[V] = {
            val javaMap = map.get(ElementsString).asInstanceOf[util.Map[String, V]]
            val scalaMap = JavaConverters.mapAsScalaMap(javaMap)
            new SerializableMap[V](scalaMap.toMap)
        }
    }

    //TODO the ShapedRecipe (de)serializers should convert their characters to strings and vice versa.
    @SerializableAs("SeriazableMap")
    implicit class SerializableMap[+V](val map: Map[String, V]) extends ConfigurationSerializable {
        override def serialize(): util.Map[String, AnyRef] = {
            val javaMap = new util.HashMap[String, AnyRef]()
            val scalaMap = new mutable.HashMap[String, V]
            scalaMap.addAll(map)
            javaMap.put(ElementsString, JavaConverters.mapAsJavaMap(map))
            javaMap
        }

        override def toString: String = map.toString()
        override def equals(obj: Any): Boolean = obj match {
            case sm: SerializableMap[V] => sm.map == this.map
            case _ => false
        }
        override def hashCode(): Int = map.hashCode()
    }
}

trait RecipeStorage {

    def init(): Boolean

    def saveRecipe(recipe: Recipe with ConfigurationSerializable): Try[Unit]

    def loadRecipes(): Try[Iterator[_ <: Recipe with ConfigurationSerializable]]

    def deleteRecipe(recipe: Recipe with ConfigurationSerializable): Try[Unit]

}
