package xyz.janboerman.recipes.v1_13_R2.gui

import org.bukkit.craftbukkit.v1_13_R2.entity.CraftHumanEntity
import org.bukkit.craftbukkit.v1_13_R2.inventory.{CraftInventory, CraftInventoryView}
import org.bukkit.inventory.{InventoryHolder, InventoryView}
import xyz.janboerman.recipes.RecipesPlugin
import xyz.janboerman.recipes.api.recipe.{ShapedRecipe, ShapelessRecipe}
import net.minecraft.server.v1_13_R2._
import net.minecraft.server.v1_13_R2.{ItemStack => NMSStack}
import xyz.janboerman.recipes.api.gui.{RecipesMenu, ShapedRecipeEditor, ShapelessRecipeEditor}
import xyz.janboerman.recipes.api.gui.CraftingRecipeEditor._

import scala.collection.mutable

object ShapedRecipeEditor {
    def apply(recipesMenu: RecipesMenu, shapedRecipe: ShapedRecipe): ShapedRecipeEditor = {
        val nmsInventory = new CraftingRecipeNMSInventory("Edit Shaped Recipe")
        val editor = new ShapedRecipeEditor(nmsInventory.bukkitInventory, shapedRecipe, recipesMenu, RecipesPlugin, RecipesPlugin)
        nmsInventory.setInventoryHolder(editor)
        editor
    }
}

object ShapelessRecipeEditor {
    def apply(recipesMenu: RecipesMenu, shapelessRecipe: ShapelessRecipe): ShapelessRecipeEditor = {
        val nmsInventory = new CraftingRecipeNMSInventory("Edit Shapeless Recipe")
        val editor = new ShapelessRecipeEditor(nmsInventory.bukkitInventory, shapelessRecipe, recipesMenu, RecipesPlugin, RecipesPlugin)
        nmsInventory.setInventoryHolder(editor)
        editor
    }
}


class CraftingRecipeNMSInventory(inventoryTitle: String)
    extends InventorySubcontainer(new ChatComponentText(inventoryTitle), TopInventorySize)
    with ITileEntityContainer {

    lazy val bukkitInventory = new CraftInventory(this)

    override def createContainer(playerInventory: PlayerInventory, entityHuman: EntityHuman): Container = {
        entityHuman match {
            case player: EntityPlayer => new CraftingRecipeContainer(player.nextContainerCounter(), player.getBukkitEntity, bukkitInventory)
            case _ =>
                RecipesPlugin.getLogger().severe("Non-EntityPlayer EntityHuman tried to open the ShapedRecipeEditor.")
                RecipesPlugin.getLogger().severe("Its type is " + entityHuman.getClass + "!")
                throw new RuntimeException("Should never occur.")
        }
    }

    override def getContainerName: String = "minecraft:container"

    def setInventoryHolder(inventoryHolder: InventoryHolder): Unit = {
        this.bukkitOwner = inventoryHolder
    }
}

object CraftingRecipeContainer {
    val TopShiftClickableSlots = new mutable.LinkedHashSet[Int]
    for (y <- CornerY until CornerY + MaxHeight) {
        for (x <- CornerX until CornerX + MaxWidth) {
            TopShiftClickableSlots += y * 9 + x //ingredient slots
        }
    }
    TopShiftClickableSlots += ResultSlot

    def isTopShiftClickSlot(index: Int): Boolean = TopShiftClickableSlots.contains(index)
}
import CraftingRecipeContainer._

//TODO de-duplicate this code
class CraftingRecipeContainer(id: Int,
                              private val bukkitPlayer: CraftHumanEntity,
                              private val bukkitInventory: CraftInventory)
    extends Container {
    import xyz.janboerman.recipes.v1_13_R2.Extensions.{NmsStack, NmsSlot}

    //initialize fields
    this.windowId = id
    private lazy val bukkitView = new CraftInventoryView(bukkitPlayer, bukkitInventory, this)
    private val playerInventory: PlayerInventory = bukkitPlayer.getHandle.inventory

    //setup slots (inspired by ContainerChest)
    //upper top container slots
    for (y <- 0 until InventoryHeight) {
        for (x <- 0 until InventoryWidth) {
            val index = x + y * InventoryWidth
            val xPosition = 8 + x * 18      //I don't understand this. at all.
            val yPosition = 18 + y * 18     //I don't understand this. at all.

            a(if (isTopShiftClickSlot(index)) {
                new Slot(bukkitInventory.getInventory, index, xPosition, yPosition)
            } else {
                new FixedSlot(bukkitInventory.getInventory, index, xPosition, yPosition)
            })
        }
    }
    //bottom (player)inventory slots
    val i = (InventoryHeight - 4) * 18  //(6-4)*18 = 36
    for (y <- 0 until 3) {
        for (x <- 0 until InventoryWidth) {
            val index = x + y * InventoryWidth + HotbarSize //skips the hotbar slots for now, we add those slots later
            val xPosition = 8 + x * 18          //I don't understand this. at all.
            val yPosition = 103 + y * 18 + i    //I don't understand this. at all.
            a(new Slot(playerInventory, index, xPosition, yPosition))
        }
    }
    //hotbar (player)inventory slots
    for (x <- 0 until HotbarSize) {
        a(new Slot(playerInventory, x /*index*/, x * 18 /*xPos*/, 161 + i /*yPos*/))
    }


    //methods
    override def getBukkitView: InventoryView = bukkitView

    override def canUse(entityHuman: EntityHuman): Boolean = true

    override def shiftClick(entityhuman: EntityHuman, rawSlot: Int): NMSStack = {
        val clickedSlot: Slot = slots.get(rawSlot)
        if (clickedSlot == null || !clickedSlot.hasItem) return NmsStack.empty

        val clickedStack: NMSStack = clickedSlot.getItem
        val clickedClone: NMSStack = clickedStack.cloneItemStack
        if (rawSlot < TopInventorySize) {
            //top inventory was clicked, shift to bottom
            //can only shift-click on the ingredient slots and result slot
            if (!isTopShiftClickSlot(rawSlot)) return NmsStack.empty

            //the indexes this item can merge into are 54 - 89 (bottom inventory)
            if (!this.a(clickedStack, TopInventorySize, this.slots.size, true)) return NmsStack.empty

        } else {
            //bottom inventory was clicked, shift to top
            /*
             * Algorithm description:
             *
             * 1. fill ingredient slots first, then the result slot
             * 2. empty slots should take priority and fill up with count 1, after that try to merge with other stacks
             *
             */

            var somethingTransferred: Boolean = false
            //iterator is ordered, because of LinkedHashSet
            var topSlotIterator = TopShiftClickableSlots.iterator
            while (topSlotIterator.hasNext && !clickedStack.isEmpty) {
                val topSlotIndex: Int = topSlotIterator.next
                val topSlot: Slot = this.slots.get(topSlotIndex)
                //empty slots first
                if (!topSlot.hasItem) { //no need to check whether the item is allowed, any item is allowed in the slot.
                    somethingTransferred = true
                    //clones the clicked stack and subtracts the argument (1)
                    //cloneAndSubtract returns a clone with the count equal to the argument (1)
                    topSlot.set(clickedStack.cloneAndSubtract(1))
                    topSlot.updateInventory()
                }
            }

            topSlotIterator = TopShiftClickableSlots.iterator

            while (topSlotIterator.hasNext && !clickedStack.isEmpty) {
                val topSlotIndex: Int = topSlotIterator.next
                val topSlot: Slot = this.slots.get(topSlotIndex)
                //merge with this slot
                //top slot has an item, guaranteed, because all empty slots are filled and our stack is still not empty
                val topSlotStack: NMSStack = topSlot.getItem
                var canMerge: Boolean = topSlotStack.getCount < topSlot.getMaxStackSize(topSlotStack)
                canMerge &= (clickedStack.getItem == topSlotStack.getItem)
                canMerge &= (clickedStack.getTag == topSlotStack.getTag)
                if (canMerge) {
                    somethingTransferred = true
                    val amountTransfer: Int = Math.min(clickedStack.getCount, topSlot.getMaxStackSize(clickedStack) - topSlotStack.getCount)
                    topSlotStack.add(amountTransfer)
                    clickedStack.subtract(amountTransfer)
                    topSlot.updateInventory()
                }
            }
            if (!somethingTransferred) return NmsStack.empty
        }

        if (clickedSlot.hasItem) {
            clickedSlot.updateInventory()
        } else {
            clickedSlot.set(NmsStack.empty)
        }

        clickedClone
    }

}

