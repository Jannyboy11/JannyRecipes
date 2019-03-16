package xyz.janboerman.recipes.api.gui

import java.util.Collections

import org.bukkit.Material
import org.bukkit.plugin.Plugin
import org.bukkit.scheduler.BukkitTask
import xyz.janboerman.guilib.api.ItemBuilder
import xyz.janboerman.recipes.api.recipe.{Grouped, Recipe}

class SetGroupButton(private val setGroupIndex: Int, private val recipeEditor: RecipeEditor[_ <: Plugin, _ <: Recipe] with GroupedRecipeEditor)
    extends AnvilButton[RecipeEditor[Plugin, Recipe with Grouped]]({case (menuHolder, event, input) =>

        val player = event.getWhoClicked
        recipeEditor.setGroup(input)
        val anvilButton = menuHolder.getButton(setGroupIndex).asInstanceOf[AnvilButton[_]]
        anvilButton.setIcon(new ItemBuilder(anvilButton.getIcon).lore(lore(recipeEditor.getGroup)).build())

        menuHolder.getPlugin.getServer.getScheduler.runTask(menuHolder.getPlugin, (_: BukkitTask) => player.openInventory(menuHolder.getInventory))
    },
    if (recipeEditor.getGroup == null) "" else recipeEditor.getGroup,
        new ItemBuilder(Material.CHEST).name(SetGroup).lore({
            if (recipeEditor.getGroup == null || recipeEditor.getGroup.isEmpty)
                Collections.emptyList[String]()
            else
                Collections.singletonList[String](lore(recipeEditor.getGroup))
    }).build())
