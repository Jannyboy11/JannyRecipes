package xyz.janboerman.recipes.api.gui

import org.bukkit.inventory.ItemStack
import org.bukkit.{Keyed, Material}
import xyz.janboerman.guilib.api.ItemBuilder
import xyz.janboerman.recipes.api.recipe._

import scala.collection.mutable

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
object LocalizedNameSearchProperty extends SearchProperty {
    override def getName(): String = "Localized Name"
    override def newSearchFilter(searchTerm: String): RecipeFilter = new ByLocalizedNameFilter(searchTerm)
}


object RecipeFilter {

    val ShapedFilter = new ByTypeFilter(classOf[ShapedRecipe]) with SingleFilter {
        override def getIcon(): ItemStack = new ItemBuilder(Material.CRAFTING_TABLE).name(interactable("Type: Shaped")).build()
    }
    val ShapelessFilter = new ByTypeFilter(classOf[ShapelessRecipe]) with SingleFilter {
        override def getIcon(): ItemStack = new ItemBuilder(Material.CRAFTING_TABLE).name(interactable("Type: Shapeless")).build()
    }
    val FurnaceFilter = new ByTypeFilter(classOf[FurnaceRecipe]) with SingleFilter {
        override def getIcon(): ItemStack = new ItemBuilder(Material.FURNACE).name(interactable("Type: Furnace")).build()
    }
    val ComplexFilter = new ByTypeFilter(classOf[ComplexRecipe]) with SingleFilter {
        override def getIcon(): ItemStack = new ItemBuilder(Material.CRAFTING_TABLE).name(interactable("Type: Complex")).build()
    }

    //TODO use RecipeType.Values

    val TypeFilters = new mutable.HashSet[ByTypeFilter]
    TypeFilters.addAll(Seq(ShapedFilter, ShapelessFilter, FurnaceFilter, ComplexFilter))
}

//TODO do I want this? so that search filters can declare their own representation in the FiltersMenu gui
//TODO Yes. Probably. But it can wait for now.
trait SingleFilter {
    def getIcon(): ItemStack
}

/*TODO trait SearchFilter {
    def getSearchQuery(): String
}*/

trait RecipeFilter extends Function[Recipe, Boolean] {
    def ||(that: RecipeFilter): RecipeFilter = recipe => this(recipe) || that(recipe)
    def &&(that: RecipeFilter): RecipeFilter = recipe => this(recipe) && that(recipe)
}

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

    //TODO merge with ByNamespaceFilter?
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

class ByLocalizedNameFilter(private val searchTerm: String) extends RecipeFilter {
    override def equals(obj: Any): Boolean = {
        obj match {
            case byLocalizedNameFilter: ByLocalizedNameFilter => byLocalizedNameFilter.searchTerm == this.searchTerm
            case _ => false
        }
    }

    override def hashCode(): Int = searchTerm.hashCode()

    private def hasLocalizedName(itemStack: ItemStack): Boolean = {
        if (itemStack == null) return false
        val itemMeta = itemStack.getItemMeta
        if (itemMeta == null) return false
        if (!itemMeta.hasLocalizedName) return false

        itemMeta.getLocalizedName.toLowerCase.contains(searchTerm.toLowerCase)
    }

    override def apply(recipe: Recipe): Boolean = recipe match {
        case fixedIngredients: FixedIngredients => fixedIngredients.getIngredientStacks().exists(hasLocalizedName)
        case fixedResult: FixedResult => hasLocalizedName(fixedResult.getResultStack())
        case _ => false
    }
}