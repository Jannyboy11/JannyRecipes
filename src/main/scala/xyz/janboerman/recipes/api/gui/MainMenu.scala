package xyz.janboerman.recipes.api.gui

import org.bukkit.Material
import org.bukkit.event.inventory.InventoryType
import org.bukkit.inventory.ItemStack
import xyz.janboerman.guilib.api.menu.{ItemButton, MenuHolder}
import xyz.janboerman.recipes.RecipesPlugin

object MainMenu extends MenuHolder(RecipesPlugin, InventoryType.HOPPER, "Manage Recipes") {

    setButton(0, new ItemButton(new ItemStack(Material.CRAFTING_TABLE))) //TODO actually add callbacks
    setButton(1, new ItemButton(new ItemStack(Material.FURNACE)))        //TODO actually add callbacks

}
