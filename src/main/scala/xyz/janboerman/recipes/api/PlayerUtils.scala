package xyz.janboerman.recipes.api

import org.bukkit.entity.{HumanEntity, Player}
import org.bukkit.plugin.Plugin
import org.bukkit.scheduler.BukkitTask

object PlayerUtils {

    def updateInventory(humanEntity: HumanEntity, plugin: Plugin): Unit = {
        plugin.getServer.getScheduler.runTask(plugin, (_: BukkitTask) => {
            humanEntity match {
                case player: Player => player.updateInventory()
                case _ => ()
            }
        })
    }

}
