package com.janboerman.recipes.api

import org.bukkit.ChatColor

package object gui {

    val Space = ChatColor.RESET + " "
    val SaveAndExit = interactable("Save & Exit")
    val Exit = interactable("Exit")
    val Modifiers = interactable("Modifiers...")
    val Rename = interactable("Rename...")
    val SetGroup = interactable("Set group...")
    val Delete = interactable("Delete...")

    val TypeShaped = static("Type: Shaped")
    val TypeShapeless = static("Type: Shapeless")
    val TypeFurnace = static("Type: Furnace")

    def error(message: String): String = ChatColor.RESET + "" + ChatColor.RED + message
    def lore(lore: String): String = ChatColor.RESET + "" + ChatColor.AQUA + lore
    def interactable(text: String): String = ChatColor.RESET + "" + ChatColor.ITALIC + text
    def static(text: String): String = ChatColor.RESET + "" + ChatColor.AQUA + text

}
