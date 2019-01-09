package xyz.janboerman.recipes.api.gui

import org.bukkit.Material
import org.bukkit.plugin.Plugin
import xyz.janboerman.guilib.api.ItemBuilder
import xyz.janboerman.guilib.api.menu.{ItemButton, MenuHolder, RedirectItemButton}
import org.bukkit.event.inventory.{InventoryClickEvent, InventoryCloseEvent, InventoryOpenEvent}
import org.bukkit.inventory.ItemStack
import xyz.janboerman.recipes.api.gui.RecipeFilter._

import scala.collection.mutable

object FilterMenu {
    val InventorySize = 5 * 9

    val SearchProperties = new mutable.HashSet[SearchProperty]
    SearchProperties.addAll(Seq(IngredientSearchProperty, ResultSearchProperty, NamespaceSearchProperty, KeySearchProperty))
}
import FilterMenu._

class FilterMenu[P <: Plugin](private val mainMenu: RecipesMenu,
                              plugin: P,
                              private val searchFilters: Option[(String, mutable.Map[SearchProperty, RecipeFilter])],
                              private val typeFilters: mutable.Set[ByTypeFilter])
    extends MenuHolder[P](plugin, InventorySize, "Select filters") {

    private val statusses: mutable.Map[RecipeFilter, Boolean] = new mutable.HashMap[RecipeFilter, Boolean]()
    for (searchFilter <- searchFilters.iterator.flatMap(_._2.values)) {
        statusses.put(searchFilter, true)
    }
    for (typeFilter <- typeFilters) {
        statusses.put(typeFilter, true)
    }

    override def onOpen(event: InventoryOpenEvent): Unit = {
        val ShapedStack = new ItemBuilder(Material.CRAFTING_TABLE).name(interactable("Type: Shaped")).build()
        val ShapelessStack = new ItemBuilder(Material.CRAFTING_TABLE).name(interactable("Type: Shapeless")).build()
        val FurnaceStack = new ItemBuilder(Material.FURNACE).name(interactable("Type: Furnace")).build()
        val ComplexStack = new ItemBuilder(Material.CRAFTING_TABLE).name(interactable("Type: Complex")).build()

        //TODO use a for loop or something similar
        val shapedIndex = if (typeFilters.contains(ShapedFilter)) 0 else 27
        val shapedButton = new StatusSwitchingButton(ShapedFilter, 0, 27, ShapedStack)
        val shapelessIndex = if (typeFilters.contains(ShapelessFilter)) 1 else 28
        val shapelessButton = new StatusSwitchingButton(ShapelessFilter, 1, 28, ShapelessStack)
        val furnaceIndex = if (typeFilters.contains(FurnaceFilter)) 2 else 29
        val furnaceButton = new StatusSwitchingButton(FurnaceFilter, 2, 29, FurnaceStack)
        val complexIndex = if (typeFilters.contains(ComplexFilter)) 3 else 30
        val complexButton = new StatusSwitchingButton(ComplexFilter, 3, 30, ComplexStack)

        setButton(shapedIndex, shapedButton)
        setButton(shapelessIndex, shapelessButton)
        setButton(furnaceIndex, furnaceButton)
        setButton(complexIndex, complexButton)

        searchFilters.foreach({
            case (searchTerm, searchFilters) => {
                var searchButtonIndex = 9
                for (searchProperty <- SearchProperties.take(9)) {
                    val upperIndex = searchButtonIndex
                    val lowerIndex = searchButtonIndex + 27
                    val itemStack = new ItemBuilder(Material.COMPASS).name(interactable(searchProperty.getName() + ": " + searchTerm)).build()

                    val option = searchFilters.get(searchProperty)
                    if (option.isDefined) {
                        setButton(upperIndex, new StatusSwitchingButton(option.get, upperIndex, lowerIndex, itemStack))
                    } else {
                        setButton(lowerIndex, new StatusSwitchingButton(searchProperty.newSearchFilter(searchTerm), upperIndex, lowerIndex, itemStack))
                    }

                    searchButtonIndex += 1
                }
            }
        })


        val inventory = getInventory
        val glassPaneStack = new ItemBuilder(Material.LIGHT_BLUE_STAINED_GLASS_PANE).name(Space).build()
        for (i <- 18 until 22) inventory.setItem(i, glassPaneStack)
        for (i <- 23 until 27) inventory.setItem(i, glassPaneStack)

        setButton(22, new RedirectItemButton[FilterMenu[P]](
            new ItemBuilder(Material.RED_CONCRETE).name(interactable(Exit)).build(),
            () => mainMenu.getInventory()))
    }

    override def onClose(event: InventoryCloseEvent): Unit = {
        searchFilters.foreach(sf => sf._2.filterInPlace({case (_: SearchProperty, recipeFilter: RecipeFilter) => statusses.getOrElse(recipeFilter, false)}))

        typeFilters.clear()
        typeFilters.addAll(statusses.filter(_._2).keys.filter(_.isInstanceOf[ByTypeFilter]).map(_.asInstanceOf[ByTypeFilter]))
    }


    class StatusSwitchingButton(val recipeFilter: RecipeFilter, val upper: Int, val lower: Int, icon: ItemStack)
        extends ItemButton[FilterMenu[P]](icon) {

        def switchStatus(): Boolean = {
            val newStatus = !statusses.getOrElse(recipeFilter, false)
            statusses.put(recipeFilter, newStatus)
            newStatus
        }

        override def onClick(holder: FilterMenu[P], event: InventoryClickEvent): Unit = {
            if (switchStatus()) {
                holder.setButton(upper, this)
                holder.unsetButton(lower)
            } else {
                holder.setButton(lower, this)
                holder.unsetButton(upper)
            }
        }
    }
}
