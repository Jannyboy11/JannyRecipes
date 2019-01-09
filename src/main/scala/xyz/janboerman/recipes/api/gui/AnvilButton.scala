package xyz.janboerman.recipes.api.gui

import java.util.function.BiFunction

import net.wesjd.anvilgui.AnvilGUI
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.plugin.Plugin
import xyz.janboerman.guilib.api.ItemBuilder
import xyz.janboerman.guilib.api.menu.{ItemButton, MenuHolder}

/**
  * A button that opens an AnvilGui
  * @param callback the anvil gui's callback
  * @param paperDisplayName the display name of the item being renamed
  * @param displayName the display name of the button itself
  * @param material the material type
  * @tparam MH the menuholder's type
  */
class AnvilButton[MH <: MenuHolder[_]](private val callback: (MH, InventoryClickEvent, String) => Unit,
                                       private val paperDisplayName: String,
                                       displayName: String,
                                       material: Material)
    extends ItemButton[MH](new ItemBuilder(material).name(displayName).build()) {

    override def onClick(holder: MH, event: InventoryClickEvent): Unit = {
        if (event.getWhoClicked.isInstanceOf[Player]) {
            val player = event.getWhoClicked.asInstanceOf[Player]
            val plugin = holder.getPlugin.asInstanceOf[Plugin]
            plugin.getServer.getScheduler.runTask(plugin, new Runnable {
                //Can't use the lambda expression version because scalac is dumb.
                //It doesn't know that it should prioritize the functional interface over the abstract class.
                //Runnable should be preferred over BukkitRunnable.
                //At least it's better than in scala 2.12 - where the lambda was compiled to an instance of the abstract class
                //instead of to an instance of the functional interface
                override def run(): Unit = {
                    event.getView.close()
                    new AnvilGUI(plugin, player, paperDisplayName, new BiFunction[Player, String, String] {
                        //same for BiFunction[Player, String, String] and ClickHandler.
                        override def apply(p: Player, userInput: String): String = {
                            callback(holder, event, userInput)
                            null
                        }
                    })
                }
            })
        }
    }

}
