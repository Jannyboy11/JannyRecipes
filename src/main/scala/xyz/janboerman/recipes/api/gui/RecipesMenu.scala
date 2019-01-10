package xyz.janboerman.recipes.api.gui

import org.bukkit.event.inventory.{InventoryClickEvent, InventoryOpenEvent}
import org.bukkit.plugin.Plugin
import org.bukkit.scheduler.BukkitTask
import org.bukkit.{ChatColor, Keyed, Material}
import xyz.janboerman.guilib.api.ItemBuilder
import xyz.janboerman.guilib.api.menu.{ItemButton, MenuHolder, RedirectItemButton}
import xyz.janboerman.recipes.api.JannyRecipesAPI
import xyz.janboerman.recipes.api.recipe._

import scala.collection.mutable
import scala.collection.mutable.ArrayBuffer
import scala.util.Try

object RecipesMenu {
    val RecipesPerPage: Int = 5 * 9
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
    private var needsRefresh = true

    private var pageButton: ItemButton[RecipesMenu] = _

    def scheduleRefresh(): Unit = needsRefresh = true

    def setCurrentPage(newPageNr: Int): Unit = {
        this.currentPageNumber = newPageNr
        scheduleRefresh()
    }

    def addFilter(filterKey: String, recipeFilter: RecipeFilter): Unit = {
        scheduleRefresh()
        genericFilters.put(filterKey, recipeFilter)
    }
    def removeFilter(filterKey: String): Unit = {
        scheduleRefresh()
        genericFilters.remove(filterKey)
    }

    def addSearchFilter(searchProperty: SearchProperty, recipeFilter: RecipeFilter): Unit = {
        scheduleRefresh()
        searchFilters.put(searchProperty, recipeFilter)
    }

    def getSearch(): Option[String] = Option(this.search).filter(_.nonEmpty)
    def setSearch(query: String): Unit = {
        this.search = query

        searchFilters.clear()
        if (query != null && query.nonEmpty) {
            addSearchFilter(NamespaceSearchProperty, new ByNamespaceFilter(query))
            addSearchFilter(KeySearchProperty, new ByKeyFilter(query))
            addSearchFilter(IngredientSearchProperty, new ByIngredientFilter(query))
            addSearchFilter(ResultSearchProperty, new ByResultFilter(query))
        }
    }
    def clearSearch(): Unit = {
        this.search = null
        this.searchFilters.clear()
        scheduleRefresh()
    }

    override def onOpen(event: InventoryOpenEvent): Unit = {

        if (firstTimeOpen) {
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
            this.pageButton = new AnvilButton[RecipesMenu]({ case (holder: RecipesMenu, event: InventoryClickEvent, input) =>
                var newPageNr = Try { Integer.parseInt(input) }
                    .getOrElse({event.getWhoClicked().sendMessage(ChatColor.RED + "Please enter a positive integer number."); currentPageNumber}) - 1
                //TODO don't hardcode the ChatFormat

                if (newPageNr < 0) newPageNr = 0
                holder.setCurrentPage(newPageNr)
                getPlugin.getServer.getScheduler.runTask(getPlugin, (_: BukkitTask) => event.getWhoClicked.openInventory(holder.getInventory))
            }, "" + (currentPageNumber + 1), new ItemBuilder(Material.CLOCK).name(interactable("Page...")).build()) {
            }
            val searchButton = new AnvilButton[RecipesMenu]( { case (holder: RecipesMenu, event: InventoryClickEvent, input) =>
                holder.setSearch(input)
                getPlugin.getServer.getScheduler.runTask(getPlugin, (_: BukkitTask) => event.getWhoClicked.openInventory(holder.getInventory))
            }, "", new ItemBuilder(Material.COMPASS).name(interactable("Search...")).lore(lore("Right click to clear")).build()) {
                override def onClick(holder: RecipesMenu, event: InventoryClickEvent): Unit = {
                    if (event.isLeftClick) {
                        super.onClick(holder, event)
                    } else if (event.isRightClick) {
                        holder.clearSearch()
                        holder.refresh()
                        holder.fillPage()
                    }
                }
            }

            setButton(newButtonIndex, newButton)
            setButton(refreshButtonIndex, refreshButton)
            setButton(filtersButtonIndex, filtersButton)
            setButton(pageButtonIndex, pageButton)
            setButton(searchButtonIndex, searchButton)

            firstTimeOpen = false
        }

        if (needsRefresh) {
            refresh()
            fillPage()
        }
    }

    def refresh(): Unit = {
        recipeIterator = api.iterateRecipes()
        cachedRecipes.clear()
        lastReachedRecipe = -1

        var filter: RecipeFilter = (_: Recipe) => true
        if (typeFilters.nonEmpty) {
            filter = filter && typeFilters.iterator.foldLeft[RecipeFilter](_ => false)({case (p1, p2) => p1 || p2})
        }
        if (searchFilters.nonEmpty) {
            filter = filter && searchFilters.valuesIterator.foldLeft[RecipeFilter](_ => false)({case (p1, p2) => p1 || p2})
        }
        if (genericFilters.nonEmpty) {
            filter = filter && genericFilters.valuesIterator.foldLeft[RecipeFilter](_ => false)({case (p1, p2) => p1 || p2})
        }

        recipeIterator = recipeIterator.filter(filter)
    }

    def hasPreviousPage(): Boolean = currentPageNumber > 0

    def hasNextPage(): Boolean = recipeIterator.hasNext || currentPageNumber * RecipesPerPage < lastReachedRecipe - RecipesPerPage

    private def fillPage(): Unit = {
        var startIndex = currentPageNumber * RecipesPerPage //might overflow because currentPageNr is dependent on user input
        if (startIndex < 0) startIndex = 0

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

        if (pageButton != null) {
            pageButton.setIcon(new ItemBuilder(pageButton.getIcon).lore(lore("Current: " + (currentPageNumber + 1))).build())
        }

        needsRefresh = false
    }

}
