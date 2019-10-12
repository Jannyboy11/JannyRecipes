package xyz.janboerman.recipes.api.gui

import java.util.function.BiFunction

import net.wesjd.anvilgui.AnvilGUI
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.plugin.Plugin
import xyz.janboerman.guilib.api.menu.{ItemButton, MenuHolder}

/**
  * A button that opens an AnvilGui
  * @param callback the anvil gui's callback
  * @param paperDisplayName the display name of the item being renamed
  * @param icon the display icon
  * @tparam MH the menuholder's type
  */
class AnvilButton[MH <: MenuHolder[_]](private val callback: (MH, InventoryClickEvent, String) => Unit,
                                       private val paperDisplayName: String,
                                       icon: ItemStack)
    extends ItemButton[MH](icon) {

    override def onClick(holder: MH, event: InventoryClickEvent): Unit = {
        if (event.getWhoClicked.isInstanceOf[Player]) {
            val player = event.getWhoClicked.asInstanceOf[Player]
            val plugin = holder.getPlugin.asInstanceOf[Plugin]
            plugin.getServer.getScheduler.runTask(plugin, new Runnable() {
                override def run(): Unit = {
                    event.getView.close()

                    new AnvilGUI(plugin, player, paperDisplayName, new BiFunction[Player, String, String]() {
                        //same for BiFunction[Player, String, String] and ClickHandler.
                        override def apply(p: Player, userInput: String): String = {
                            callback(holder, event, userInput)
                            null //return null to close the anvilgui
                        }
                    })
                }
            })
        }
    }

}
