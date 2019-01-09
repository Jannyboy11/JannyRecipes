package xyz.janboerman.recipes.api.gui

import org.bukkit.{Keyed, Material}
import xyz.janboerman.recipes.api.gui.RecipeFilter.RecipeFilter
import xyz.janboerman.recipes.api.recipe._

trait SearchProperty {
    def getName(): String
    def newSearchFilter(searchTerm: String): RecipeFilter
}

//TODO use MetaProgramming?
object IngredientSearchProperty extends SearchProperty {
    override def getName(): String = "Ingredient"
    override def newSearchFilter(searchTerm: String): RecipeFilter = new ByIngredientFilter(searchTerm)
}
object ResultSearchProperty extends SearchProperty {
    override def getName(): String = "Result"
    override def newSearchFilter(searchTerm: String): RecipeFilter = new ByResultFilter(searchTerm)
}
object NamespaceSearchProperty extends SearchProperty {
    override def getName(): String = "Namespace"
    override def newSearchFilter(searchTerm: String): RecipeFilter = new ByNamespaceFilter(searchTerm)
}
object KeySearchProperty extends SearchProperty {
    override def getName(): String = "Key"
    override def newSearchFilter(searchTerm: String): RecipeFilter = new ByKeyFilter(searchTerm)
}


object RecipeFilter {
    type RecipeFilter = Recipe => Boolean

    val ShapedFilter = new ByTypeFilter(classOf[ShapedRecipe])
    val ShapelessFilter = new ByTypeFilter(classOf[ShapelessRecipe])
    val FurnaceFilter = new ByTypeFilter(classOf[FurnaceRecipe])
    val ComplexFilter = new ByTypeFilter(classOf[ComplexRecipe])

}
import RecipeFilter._


class ByTypeFilter(private val recipeClass: Class[_ <: Recipe]) extends RecipeFilter {
    override def equals(obj: Any): Boolean = {
        obj match {
            case recipeTypeFilter: ByTypeFilter => recipeTypeFilter.recipeClass == this.recipeClass
            case _ => false
        }
    }

    override def hashCode(): Int = recipeClass.hashCode()

    override def apply(recipe: Recipe): Boolean = recipeClass.isInstance(recipe)
}

class ByNamespaceFilter(private val namespace: String) extends RecipeFilter {
    override def equals(obj: Any): Boolean = {
        obj match {
            case byNamespaceFilter: ByNamespaceFilter => byNamespaceFilter.namespace == this.namespace
            case _ => false
        }
    }

    override def hashCode(): Int = namespace.hashCode()

    override def apply(recipe: Recipe): Boolean = recipe match {
        case keyedRecipe: Keyed => keyedRecipe.getKey.getNamespace.equalsIgnoreCase(namespace)
        case _ => false
    }
}

class ByKeyFilter(private val key: String) extends RecipeFilter {
    override def equals(obj: Any): Boolean = {
        obj match {
            case byKeyFilter: ByKeyFilter => byKeyFilter.key == this.key
            case _ => false
        }
    }

    override def hashCode(): Int = key.hashCode

    override def apply(recipe: Recipe): Boolean = recipe match {
        case keyedRecipe: Keyed => keyedRecipe.getKey.getKey.equalsIgnoreCase(key)
        case _ => false
    }
}

class ByResultFilter(private val searchTerm: String) extends RecipeFilter {
    override def equals(obj: Any): Boolean = {
        obj match {
            case byResultFilter: ByResultFilter => byResultFilter.searchTerm == this.searchTerm
            case _ => false
        }
    }

    override def hashCode(): Int = searchTerm.hashCode()

    override def apply(recipe: Recipe): Boolean = {
        recipe match {
            case fixedResult: FixedResult => fixedResult.hasResultType(Material.matchMaterial(searchTerm)) ||
                Option(fixedResult.getResultMaterial())
                    .map(_.toString.toLowerCase)
                    .exists(_.contains(searchTerm.toLowerCase))

            case _ => false
        }
    }
}

class ByIngredientFilter(private val searchTerm: String) extends RecipeFilter {
    override def equals(obj: Any): Boolean = {
        obj match {
            case byIngredientFilter: ByIngredientFilter => byIngredientFilter.searchTerm == this.searchTerm
            case _ => false
        }
    }

    override def hashCode(): Int = searchTerm.hashCode()

    override def apply(recipe: Recipe): Boolean = recipe match {
        case fixedIngredients: FixedIngredients => fixedIngredients.hasIngredient(Material.matchMaterial(searchTerm)) ||
            Option(fixedIngredients.getIngredientStacks())
                .map(_.toString.toLowerCase)
                .exists(_.contains(searchTerm.toLowerCase))
        case _ => false
    }
}
