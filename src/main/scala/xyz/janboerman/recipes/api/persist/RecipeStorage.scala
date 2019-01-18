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
    //TODO WHY ARE WE NOT CALLED?!
    def valueOf(map: util.Map[String, AnyRef]): SerializableList = {
        val javaList = map.get(ElementsString).asInstanceOf[util.List[_]]
        val scalaList = JavaConverters.asScalaBuffer(javaList)
        println("\r\n DEBUG SerializableList#valueOf scalaList = " + scalaList + "\r\n")
        new SerializableList(scalaList.toList)
    }
}

@SerializableAs("SerializableList")
class SerializableList(val list: List[_] /*is appearantly null when deserialized using valueOf?*/) extends ConfigurationSerializable {
    override def serialize(): util.Map[String, AnyRef] = {
        val javaMap = new util.HashMap[String, AnyRef]()
        val scalaList = list.toBuffer
        javaMap.put(ElementsString, JavaConverters.bufferAsJavaList(scalaList))
        println("DEBUG SERIALIZING THAT SWEET SERIALIZABLE_LIST THING TO " + javaMap)
        javaMap
    }

    override def toString: String = list.toString()
    override def equals(obj: Any): Boolean = obj match {
        case sl: SerializableList => sl.list == this.list
        case _ => false
    }
    override def hashCode(): Int = list.hashCode()
}

object SerializableMap {
    def valueOf(map: util.Map[String, AnyRef]): SerializableMap = {
        val javaMap = map.get(ElementsString).asInstanceOf[util.Map[String, _]]
        val scalaMap = JavaConverters.mapAsScalaMap(javaMap)
        new SerializableMap(scalaMap.toMap)
    }
}

@SerializableAs("SerializableMap")
class SerializableMap(val map: Map[String, _]) extends ConfigurationSerializable {
    override def serialize(): util.Map[String, AnyRef] = {
        val map = new util.HashMap[String, AnyRef]()
        map.put(ElementsString, JavaConverters.mapAsJavaMap(this.map))
        map
    }

    override def toString: String = map.toString()
    override def equals(obj: Any): Boolean = obj match {
        case sm: SerializableMap => sm.map == this.map
        case _ => false
    }
    override def hashCode(): Int = map.hashCode()
}

trait RecipeStorage {

    def init(): Boolean

    def saveRecipe(recipe: Recipe with ConfigurationSerializable): Either[String, Unit]

    def loadRecipes(): Either[String, Iterator[Recipe with ConfigurationSerializable]]

    def deleteRecipe(recipe: Recipe with ConfigurationSerializable): Either[String, Unit]

}
