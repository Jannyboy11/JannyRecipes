package xyz.janboerman.recipes.v1_13_R2.gui

import net.minecraft.server.v1_13_R2.{EntityHuman, IInventory, ItemStack, Slot}

class FixedSlot(inventory: IInventory, index: Int, xPosition: Int, yPosition: Int)
    extends Slot(inventory, index, xPosition, yPosition) {

    override def isAllowed(player: EntityHuman) = false

    override def isAllowed(input: ItemStack) = false

    override def set(item: ItemStack): Unit = {
        //we are not setting any item, this is a fixed slot!
        this.f()
    }

    override def getMaxStackSize = Int.MaxValue

    override def getMaxStackSize(itemStack: ItemStack): Int = Int.MaxValue
}
