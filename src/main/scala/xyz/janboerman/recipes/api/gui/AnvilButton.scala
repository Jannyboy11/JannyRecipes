package xyz.janboerman.recipes.api.gui

import java.util.function.BiFunction

import net.wesjd.anvilgui.AnvilGUI
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.plugin.Plugin
import xyz.janboerman.guilib.api.ItemBuilder
import xyz.janboerman.guilib.api.menu.{ItemButton, MenuHolder}

class AnvilButton[P <: Plugin](val callback: String => Unit,
                               val paperDisplayName: String,
                               displayName: String,
                               material: Material)
    extends ItemButton[MenuHolder[P]](new ItemBuilder(material).name(displayName).build()) {

    def this(callback: String => Unit, paperDisplayName: String, displayName: String) =
        this(callback, paperDisplayName, displayName, Material.NAME_TAG)

    def this(callback: String => Unit, displayName: String) =
        this(callback, "Please enter a new name..", displayName)

    override def onClick(holder: MenuHolder[P], event: InventoryClickEvent): Unit = {
        if (event.getWhoClicked.isInstanceOf[Player]) {
            val player = event.getWhoClicked.asInstanceOf[Player]
            val plugin = holder.getPlugin
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
                            callback(userInput)
                            null
                        }
                    })
                }
            })
        }
    }

}
