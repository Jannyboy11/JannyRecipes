package com.janboerman.recipes.listeners

import com.janboerman.recipes.api.gui.{FilterMenu, NewMenu, RecipeEditor, RecipesMenu}
import org.bukkit.entity.Player
import org.bukkit.event.{EventHandler, Listener}
import org.bukkit.event.inventory.{InventoryCloseEvent, InventoryOpenEvent}
import org.bukkit.event.player.PlayerQuitEvent
import xyz.janboerman.guilib.api.GuiInventoryHolder
import com.janboerman.recipes.RecipesPlugin
import com.janboerman.recipes.api.gui.{FilterMenu, NewMenu, RecipeEditor, RecipesMenu}
import com.janboerman.recipes.api.recipe.Recipe

import scala.collection.mutable

object GuiInventoryHolderListener extends Listener {

    private val guiListener = RecipesPlugin.getGuiListener
    private val lastOpenedGuis = new mutable.WeakHashMap[Player, GuiInventoryHolder[_]]
    private val lastOpenedEditors = new mutable.WeakHashMap[Player, RecipeEditor[_, _ <: Recipe]]

    def getLastOpenedGui(player: Player): Option[GuiInventoryHolder[_]] = lastOpenedGuis.get(player)
    def getLastOpenedEditor(player: Player): Option[RecipeEditor[_, _ <: Recipe]] = lastOpenedEditors.get(player)

    @EventHandler
    def onClose(event: InventoryCloseEvent): Unit = {
        if (!event.getPlayer.isInstanceOf[Player]) return

        val player = event.getPlayer.asInstanceOf[Player]
        var guiInventoryHolder = guiListener.getHolder(event.getInventory)

        guiInventoryHolder = guiInventoryHolder match {
            case recipesMenu: RecipesMenu[_] => recipesMenu
            case recipeEditor: RecipeEditor[_, _] => recipeEditor
            case filterMenu: FilterMenu[_] => filterMenu
            case newMenu: NewMenu[_] => newMenu
            case _ => null
        }

        if (guiInventoryHolder != null && guiInventoryHolder.getPlugin == RecipesPlugin) {
            lastOpenedGuis.put(player, guiInventoryHolder)

            if (guiInventoryHolder.isInstanceOf[RecipeEditor[_, _ <: Recipe]]) {
                lastOpenedEditors.put(player, guiInventoryHolder.asInstanceOf[RecipeEditor[_, _ <: Recipe]])
            }
        }
    }

    @EventHandler
    def onQuit(event: PlayerQuitEvent): Unit = {
        lastOpenedGuis.remove(event.getPlayer)
        lastOpenedEditors.remove(event.getPlayer)
    }

}
