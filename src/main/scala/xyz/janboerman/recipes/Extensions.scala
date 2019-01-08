package xyz.janboerman.recipes

import org.bukkit.inventory.meta.ItemMeta
import org.bukkit.{Material, NamespacedKey}
import org.bukkit.inventory.{Inventory, ItemStack}
import org.bukkit.plugin.Plugin

object Extensions {

    object BukkitKey {
        def unapply(arg: NamespacedKey): Option[(String, String)] =
            Some(arg.getNamespace(), arg.getKey())
        def apply(key: String): NamespacedKey = {
            val colonIndex = key.indexOf(':')

            if (colonIndex != -1) {
                val split = key.split(":")
                new NamespacedKey(split(0), split(1))
            } else {
                NamespacedKey.minecraft(key)
            }
        }
        def apply(namespace: String, key: String): NamespacedKey = new NamespacedKey(namespace, key)
        def apply(plugin: Plugin, key: String): NamespacedKey = new NamespacedKey(plugin, key)
    }

    implicit class InventoryExtensions(inventory: Inventory) extends IndexedSeq[ItemStack] {
        override def length: Int = inventory.getSize
        override def apply(idx: Int): ItemStack = inventory.getItem(idx)
    }

    implicit class ItemStackExtensions(itemStack: ItemStack) {
        def +(addAmount: Int): ItemStack = itemStack.clone() += addAmount
        def -(subtractAmount: Int): ItemStack = itemStack.clone() -= subtractAmount
        def +=(addAmount: Int): ItemStack = {itemStack.setAmount(itemStack.getAmount + addAmount); itemStack}
        def -=(subtractAmount: Int): ItemStack = {itemStack.setAmount(itemStack.getAmount - subtractAmount); itemStack}

        def apply(material: Material): ItemStack = {itemStack.setType(material); itemStack}
        def apply(amount: Int): ItemStack = {itemStack.setAmount(amount); itemStack}
        def apply(itemMeta: ItemMeta): ItemStack = {itemStack.setItemMeta(itemMeta); itemStack}
    }

    object BukkitStack {
        type Amount = Int
        def unapply(itemStack: ItemStack): Option[(Material, Amount, ItemMeta)] =
            if (itemStack == null) None
            else Some((itemStack.getType, itemStack.getAmount, itemStack.getItemMeta))
    }
}
