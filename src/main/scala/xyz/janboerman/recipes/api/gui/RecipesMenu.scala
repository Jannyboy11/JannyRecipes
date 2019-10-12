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

class RecipesMenu[P <: Plugin]()(implicit protected val api: JannyRecipesAPI, implicit protected val plugin: P)
    extends MenuHolder[Plugin](plugin, RecipesPerPage + 9, "Manage Recipes") { self =>

    private implicit val recipesMenu: RecipesMenu[_] = this
    private implicit val recipesMenuP: RecipesMenu[P] = this
    private implicit val mainMenu: RecipesMenu[Nothing] = this.asInstanceOf[RecipesMenu[Nothing]]

    private implicit val implicitGuiFactory: RecipeGuiFactory[P] = api.getGuiFactory()

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

    private var pageButton: ItemButton[RecipesMenu[P]] = _

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
            for (searchProperty <- FilterMenu.SearchProperties) {
                addSearchFilter(searchProperty, searchProperty.newSearchFilter(query))
            }
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

            val newButton = new RedirectItemButton[RecipesMenu[P]](new ItemBuilder(Material.NETHER_STAR)
                .name(interactable("New..."))
                .build(), () => new NewMenu[P]().getInventory)

            //TODO I shouldn't need this button, right?
            val refreshButton = new ItemButton[RecipesMenu[P]](new ItemBuilder(Material.TOTEM_OF_UNDYING).name(interactable("Refresh")).build()) {
                override def onClick(holder: RecipesMenu[P], event: InventoryClickEvent): Unit = {
                    self.refresh()
                    self.fillPage()
                }
            }
            val filtersButton = new RedirectItemButton[RecipesMenu[P]](new ItemBuilder(Material.COBWEB).name(interactable("Filters...")).build(), () =>
                new FilterMenu[P](getSearch().map(x => (x, searchFilters)), typeFilters).getInventory())

            this.pageButton = new AnvilButton[RecipesMenu[P]]({ case (holder: RecipesMenu[P], event: InventoryClickEvent, input) =>
                var newPageNr = Try { Integer.parseInt(input) }
                    .getOrElse({event.getWhoClicked().sendMessage(ChatColor.RED + "Please enter a positive integer number."); currentPageNumber}) - 1

                if (newPageNr < 0) newPageNr = 0
                holder.setCurrentPage(newPageNr)
                getPlugin.getServer.getScheduler.runTask(getPlugin, (_: BukkitTask) => event.getWhoClicked.openInventory(holder.getInventory))
            }, "" + (currentPageNumber + 1), new ItemBuilder(Material.CLOCK).name(interactable("Page...")).build())

            val searchButton = new AnvilButton[RecipesMenu[P]]( { case (holder: RecipesMenu[P], event: InventoryClickEvent, input) =>
                holder.setSearch(input)
                getPlugin.getServer.getScheduler.runTask(getPlugin, (_: BukkitTask) => event.getWhoClicked.openInventory(holder.getInventory))
            }, "", new ItemBuilder(Material.COMPASS).name(interactable("Search...")).lore(lore("Right click to clear")).build()) {
                override def onClick(holder: RecipesMenu[P], event: InventoryClickEvent): Unit = {
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
        //TODO can I generalize this so that it's usable for other plugins?
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
        var startIndex = currentPageNumber * RecipesPerPage //might overflow because currentPageNumber is dependent on user input
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

                val recipeEditor = guiFactory.newRecipeEditor(recipe)
                var icon = recipeEditor.getIcon().getOrElse(RecipeEditor.UnknownRecipeIcon)
                if (recipe.isInstanceOf[Keyed]) {
                   icon = new ItemBuilder(icon).name(interactable(recipe.asInstanceOf[Keyed].getKey.toString)).build()
                }
                setButton(slotIndex, new RedirectItemButton[RecipesMenu[P]](icon, () => recipeEditor.getInventory()))
            } else {
                unsetButton(slotIndex)
            }

            recipeIndex += 1
            slotIndex += 1
        }

        val previousNextBuilder = new ItemBuilder(Material.MAGENTA_GLAZED_TERRACOTTA)
        if (hasPreviousPage()) {
            setButton(RecipesPerPage, new ItemButton[RecipesMenu[P]](previousNextBuilder.name("Previous page").build()) {
                override def onClick(holder: RecipesMenu[P], event: InventoryClickEvent): Unit = {
                    assert(holder == self, "Holder of the previous-page button is not the same holder of the opened inventory.")
                    holder.currentPageNumber -= 1
                    holder.fillPage()
                }
            })
        } else {
            unsetButton(RecipesPerPage)
        }
        if (hasNextPage()) {
            setButton(RecipesPerPage + 8, new ItemButton[RecipesMenu[P]](previousNextBuilder.name("Next page").build()) {
                override def onClick(holder: RecipesMenu[P], event: InventoryClickEvent): Unit = {
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
