package xyz.janboerman.recipes.api.gui

import org.bukkit.event.inventory.{InventoryClickEvent, InventoryOpenEvent}
import org.bukkit.plugin.Plugin
import org.bukkit.{Keyed, Material}
import xyz.janboerman.guilib.api.ItemBuilder
import xyz.janboerman.guilib.api.menu.{ItemButton, MenuHolder, RedirectItemButton}
import xyz.janboerman.recipes.RecipesPlugin
import xyz.janboerman.recipes.api.JannyRecipesAPI
import xyz.janboerman.recipes.api.recipe._

import scala.collection.mutable
import scala.collection.mutable.ArrayBuffer

object RecipesMenu {
    val RecipesPerPage: Int = 5 * 9

    type RecipeFilter = Recipe => Boolean

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

        override def hashCode(): Int = key.hashCode + 1

        override def apply(recipe: Recipe): Boolean = recipe match {
            case keyedRecipe: Keyed => keyedRecipe.getKey.getKey.equalsIgnoreCase(key)
            case _ => false
        }
    }

    class ByResultFilter(private val material: Material) extends RecipeFilter {
        override def equals(obj: Any): Boolean = {
            obj match {
                case byResultFilter: ByResultFilter => byResultFilter.material == this.material
                case _ => false
            }
        }

        override def hashCode(): Int = material.hashCode()

        override def apply(recipe: Recipe): Boolean = recipe match {
            case fixedResult: FixedResult => fixedResult.hasResultType(material)
            case _ => false
        }
    }

    class ByIngredientFilter(private val material: Material) extends RecipeFilter {
        override def equals(obj: Any): Boolean = {
            obj match {
                case byIngredientFilter: ByIngredientFilter => byIngredientFilter.material == this.material
                case _ => false
            }
        }

        override def hashCode(): Int = material.hashCode()

        override def apply(recipe: Recipe): Boolean = recipe match {
            case fixedIngredients: FixedIngredients => fixedIngredients.hasIngredient(material)
            case _ => false
        }
    }

    val ShapedFilter = new ByTypeFilter(classOf[ShapedRecipe])
    val ShapelessFilter = new ByTypeFilter(classOf[ShapelessRecipe])
    val FurnaceFilter = new ByTypeFilter(classOf[FurnaceRecipe])
    val ComplexFilter = new ByTypeFilter(classOf[ComplexRecipe])

    type SearchProperty = String
}
import RecipesMenu._

//TODO add P <: Plugin type paramter, extends MenuHolder[P] instead.
class RecipesMenu(private val api: JannyRecipesAPI, plugin: Plugin) extends MenuHolder[Plugin](plugin, RecipesPerPage + 9, "Manage Recipes") { self =>
    import RecipesMenu._

    private var recipeIterator: Iterator[_ <: Recipe] = api.iterateRecipes()
    private var currentPageNumber = 0
    private val cachedRecipes: mutable.Buffer[Recipe] = new ArrayBuffer[Recipe]()
    private var lastReachedRecipe = -1

    private var firstTimeOpen = true
    private val guiFactory = api.getGuiFactory()
    private var search: String = _

    private val typeFilters = new mutable.HashSet[ByTypeFilter]()
    private val searchFilters = new mutable.HashMap[SearchProperty, RecipeFilter]
    private val genericFilters = new mutable.HashMap[String, RecipeFilter]

    def addFilter(filterKey: String, recipeFilter: RecipeFilter): Unit = genericFilters.put(filterKey, recipeFilter)
    def removeFilter(filterKey: String): Unit = genericFilters.remove(filterKey)

    def addSearchFilter(searchProperty: SearchProperty, recipeFilter: RecipeFilter): Unit = {
        searchFilters.put(searchProperty, recipeFilter)
    }

    def getSearch(): Option[String] = Option(this.search).filter(_.nonEmpty)
    def setSearch(query: String): Unit = {
        this.search = query

        searchFilters.clear()
        if (query != null && query.nonEmpty) {
            addSearchFilter("Namespace", new ByNamespaceFilter(query))
            addSearchFilter("Key", new ByKeyFilter(query))

            val material = Material.matchMaterial(query)
            if (material != null) {
                addSearchFilter("Ingredient", new ByIngredientFilter(material))
                addSearchFilter("Result", new ByResultFilter(material))
            }
        }
    }

    override def onOpen(event: InventoryOpenEvent): Unit = {
        refresh()

        if (firstTimeOpen) {
            fillPage()
            firstTimeOpen = false
        }

        //TODO initialise the buttons in the middle
        //New...
        //Refresh
        //Filters...
        //Page...
        //Search...

        val newButtonIndex = RecipesPerPage + 2
        val refreshButtonIndex = newButtonIndex + 1
        val filtersButtonIndex = refreshButtonIndex + 1
        val pageButtonIndex = filtersButtonIndex + 1
        val searchButtonIndex = pageButtonIndex + 1

        val newButton = new ItemButton[RecipesMenu](new ItemBuilder(Material.NETHER_STAR).name(interactable("New...")).build()) //TODO
        val refreshButton = new ItemButton[RecipesMenu](new ItemBuilder(Material.TOTEM_OF_UNDYING).name(interactable("Refresh")).build()) //TODO
        val filtersButton = new RedirectItemButton[RecipesMenu](new ItemBuilder(Material.COBWEB).name(interactable("Filters...")).build(), () => {
            new FilterMenu[Plugin](this, getPlugin, getSearch().map(x => (x, searchFilters)), typeFilters).getInventory()
        })
        val pageButton = new ItemButton[RecipesMenu](new ItemBuilder(Material.CLOCK).name(interactable("Page...")).build())
        val searchButton = new AnvilButton[RecipesMenu]( { case (holder: RecipesMenu, event: InventoryClickEvent, input) =>
            holder.setSearch(input)
            event.getWhoClicked.openInventory(holder.getInventory)
        }, interactable("Click me to complete the search term!"),
            "What do you seek?", Material.COMPASS)

        setButton(newButtonIndex, newButton)
        setButton(refreshButtonIndex, refreshButton)
        setButton(filtersButtonIndex, filtersButton)
        setButton(pageButtonIndex, pageButton)
        setButton(searchButtonIndex, searchButton)


    }

    def refresh(): Unit = {
        recipeIterator = api.iterateRecipes()
        cachedRecipes.clear()
        lastReachedRecipe = -1

        val recipeFilters = typeFilters ++ searchFilters ++ genericFilters
        if (recipeFilters.nonEmpty) {
            val filter: Recipe => Boolean = recipeFilters.foldLeft[Recipe => Boolean](_ => false)({case (p1: RecipeFilter, p2: RecipeFilter) => (x: Recipe) => p1(x) || p2(x)})

            recipeIterator = recipeIterator.filter(filter)
        }
    }

    def hasPreviousPage(): Boolean = currentPageNumber > 0

    def hasNextPage(): Boolean = recipeIterator.hasNext || currentPageNumber * RecipesPerPage < lastReachedRecipe - RecipesPerPage

    private def fillPage(): Unit = {
        var startIndex = currentPageNumber * RecipesPerPage

        //if the recipes for this page aren't 'loaded' yet, load them all unit here.
        while (lastReachedRecipe < startIndex + RecipesPerPage && recipeIterator.hasNext) {
            val recipe: Recipe = recipeIterator.next()
            cachedRecipes.addOne(recipe)
            lastReachedRecipe += 1
        }

        //if we still didn't reach the startIndex, well then.. just lower the startIndex and currentPageNumber :)
        if (startIndex > lastReachedRecipe) {
            startIndex = Math.max(0, lastReachedRecipe - RecipesPerPage)
            currentPageNumber = startIndex / RecipesPerPage
        }

        //set the buttons in the inventory
        var slotIndex = 0
        var recipeIndex = startIndex
        while (recipeIndex < startIndex + RecipesPerPage) {
            if (recipeIndex < cachedRecipes.size) {
                val recipe = cachedRecipes(recipeIndex)

                val recipeEditor = guiFactory.newRecipeEditor(recipe, this)
                setButton(slotIndex, new RedirectItemButton[RecipesMenu](recipeEditor.getIcon().getOrElse(RecipeEditor.UnknownRecipeIcon), () => recipeEditor.getInventory()))
            } else {
                unsetButton(slotIndex)
            }

            recipeIndex += 1
            slotIndex += 1
        }

        val previousNextBuilder = new ItemBuilder(Material.MAGENTA_GLAZED_TERRACOTTA)
        if (hasPreviousPage()) {
            setButton(RecipesPerPage, new ItemButton[RecipesMenu](previousNextBuilder.name("Previous page").build()) {
                override def onClick(holder: RecipesMenu, event: InventoryClickEvent): Unit = {
                    assert(holder == self, "Holder of the previous-page button is not the same holder of the opened inventory.")
                    holder.currentPageNumber -= 1
                    holder.fillPage()
                }
            })
        } else {
            unsetButton(RecipesPerPage)
        }
        if (hasNextPage()) {
            setButton(RecipesPerPage + 8, new ItemButton[RecipesMenu](previousNextBuilder.name("Next page").build()) {
                override def onClick(holder: RecipesMenu, event: InventoryClickEvent): Unit = {
                    assert(holder == self, "Holder of the next-page button is not the same holder of the opened inventory.")
                    holder.currentPageNumber += 1
                    holder.fillPage()
                }
            })
        } else {
            unsetButton(RecipesPerPage + 8)
        }
    }

}
