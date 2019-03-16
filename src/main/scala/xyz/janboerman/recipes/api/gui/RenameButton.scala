package xyz.janboerman.recipes.api.gui

import java.util.Collections

import org.bukkit.plugin.Plugin
import org.bukkit.scheduler.BukkitTask
import org.bukkit.{ChatColor, Keyed, Material, NamespacedKey}
import xyz.janboerman.guilib.api.ItemBuilder
import xyz.janboerman.recipes.api.recipe.Recipe

class RenameButton(private val index: Int, private val recipeEditor: RecipeEditor[_ <: Plugin, _ <: Recipe] with KeyedRecipeEditor)
    extends AnvilButton[RecipeEditor[Plugin, Recipe with Keyed]]({case (menuHolder, event, input) =>

    val player = event.getWhoClicked
    val split = input.split(":")
    if (split.exists(s => s.isEmpty() || !s.matches("[a-z0-9._-]+"))) {
        player.sendMessage(ChatColor.RED + "Recipe names can only contain lowercase alphanumeric characters, underscores and dashes.")
    } else {
        recipeEditor.setKey(if (split.length > 1) {
            val namespace = split(0)
            val rest = String.join("_", split.tail: _*)
            new NamespacedKey(namespace, rest)
        } else {
            new NamespacedKey(menuHolder.getPlugin, split(0))
        })

        val anvilButton = menuHolder.getButton(index).asInstanceOf[AnvilButton[_]]
        anvilButton.setIcon(new ItemBuilder(anvilButton.getIcon).lore(lore(recipeEditor.getKey().toString)).build())
    }
    menuHolder.getPlugin.getServer.getScheduler.runTask(menuHolder.getPlugin, (_: BukkitTask) => player.openInventory(menuHolder.getInventory))
}, if (recipeEditor.getKey == null) "" else recipeEditor.getKey().toString,
    new ItemBuilder(Material.NAME_TAG).name(Rename).lore({
        if (recipeEditor.getKey() == null)
            Collections.emptyList[String]()
        else
            Collections.singletonList(lore(recipeEditor.getKey().toString))
}).build())
