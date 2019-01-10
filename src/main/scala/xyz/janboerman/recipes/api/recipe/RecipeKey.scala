package xyz.janboerman.recipes.api.recipe

import java.util
import java.util.UUID

import org.bukkit.NamespacedKey
import org.bukkit.configuration.serialization.{ConfigurationSerializable, SerializableAs}
import xyz.janboerman.recipes.api.persist.RecipeStorage._

object RecipeKey {
    implicit def toRecipeKey(namespacedKey: NamespacedKey): RecipeKey = new NamespacedRecipeKey(namespacedKey)
    implicit def toRecipeKey(stringKey: String): RecipeKey = new StringRecipeKey(stringKey)

    implicit def toNamespacedKey(namespacedRecipeKey: NamespacedRecipeKey) = namespacedRecipeKey.namespacedKey
    implicit def toString(stringRecipeKey: StringRecipeKey): String = stringRecipeKey.string
}

trait RecipeKey extends ConfigurationSerializable {
    def contains(string: String): Boolean
}

object NamespacedRecipeKey {
    def valueOf(map: util.Map[String, AnyRef]): NamespacedRecipeKey = {
        val namespace = map.get(NamespaceString).asInstanceOf[String]
        val key = map.get(KeyString).asInstanceOf[String]
        new NamespacedRecipeKey(new NamespacedKey(namespace, key))
    }
}

@SerializableAs("NamespacedRecipeKey")
final class NamespacedRecipeKey(val namespacedKey: NamespacedKey) extends RecipeKey {
    override def serialize(): util.Map[String, AnyRef] = {
        val map = new util.HashMap[String, AnyRef]()
        map.put(NamespaceString, namespacedKey.getNamespace)
        map.put(KeyString, namespacedKey.getKey)
        map
    }

    override def toString(): String = namespacedKey.toString
    override def hashCode(): Int = namespacedKey.hashCode()
    override def equals(obj: Any): Boolean = obj match {
        case namespacedRecipeKey: NamespacedRecipeKey => namespacedRecipeKey.namespacedKey == this.namespacedKey
        case _ => false
    }

    override def contains(string: String): Boolean = namespacedKey.getNamespace.contains(string)
}

object StringRecipeKey {
    def valueOf(map: util.Map[String, AnyRef]): StringRecipeKey = {
        val string = map.get(StringString).asInstanceOf[String]
        new StringRecipeKey(string)
    }
}

@SerializableAs("StringRecipeKey")
final class StringRecipeKey(val string: String) extends RecipeKey {
    override def serialize(): util.Map[String, AnyRef] = {
        val map = new util.HashMap[String, AnyRef]()
        map.put(StringString, string)
        map
    }

    override def toString(): String = string
    override def hashCode(): Int = string.hashCode()
    override def equals(obj: Any): Boolean = obj match {
        case stringRecipeKey: StringRecipeKey => stringRecipeKey.string == this.string
        case _ => false
    }

    override def contains(string: String): Boolean = this.string.contains(string)
}

object UUIDRecipeKey {
    def valueOf(map: util.Map[String, AnyRef]): UUIDRecipeKey = {
        val uuid = UUID.fromString(map.get(UUIDString).asInstanceOf[String])
        new UUIDRecipeKey(uuid)
    }
}

@SerializableAs("UUIDRecipeKey")
final class UUIDRecipeKey(val uuid: UUID) extends RecipeKey {
    override def serialize(): util.Map[String, AnyRef] = {
        val map = new util.HashMap[String, AnyRef]()
        map.put(UUIDString, uuid.toString)
        map
    }

    override def toString(): String = uuid.toString
    override def hashCode(): Int = uuid.hashCode()
    override def equals(obj: Any): Boolean = obj match {
        case uuidRecipeKey: UUIDRecipeKey => uuidRecipeKey.uuid == this.uuid
        case _ => false
    }

    override def contains(string: String): Boolean = uuid.toString.contains(string)
}