package xyz.janboerman.recipes.v1_13_R2

import com.google.gson.{JsonElement, JsonObject}
import net.minecraft.server.v1_13_R2._
import org.bukkit.craftbukkit.v1_13_R2.inventory.{CraftInventoryCrafting, CraftInventoryFurnace}
import org.bukkit.inventory.{CraftingInventory, FurnaceInventory, InventoryHolder}

object Extensions {
    object NmsKey {
        type Namespace = String
        type Key = String
        def unapply(arg: MinecraftKey): Option[(Namespace, Key)] = Some(arg.getNamespace(), arg.getKey())
        def apply(namespace: Namespace, key: Key): MinecraftKey = new MinecraftKey(namespace, key)
        def apply(key: String): MinecraftKey = new MinecraftKey(key)
    }

    implicit class NmsKey(nms: MinecraftKey) {
        import NmsKey.{Namespace, Key}
        def getNamespace(): Namespace = nms.b()
        def getKey(): Key = nms.getKey()
    }

    implicit class NmsItem(nms: Item) {
        def hasCraftingResult(): Boolean = nms.p()
        def getCraftingResult(): Item = nms.o()
    }

    object NmsStack {
        type Count = Int
        def unapply(arg: ItemStack): Option[(Item, Count, NBTTagCompound)] =
            Some(arg.getItem(), arg.getCount(), arg.getTag())
        def empty: ItemStack = ItemStack.a

        def haveSameItem(one: ItemStack, two: ItemStack) = ItemStack.c(one, two)
        def haveSameNbt(one: ItemStack, two: ItemStack) = ItemStack.equals(one, two)
        def equals(one: ItemStack, two: ItemStack) = ItemStack.matches(one, two)
    }

    implicit class NmsStack(val nms: ItemStack) {
        def hasSameNbtAs(that: ItemStack) = NmsStack.haveSameNbt(nms, that)
        def hasSameItemAs(that: ItemStack) = NmsStack.haveSameItem(nms, that)

        override def equals(obj: scala.Any): Boolean = obj match {
            case that: ItemStack    => NmsStack.equals(nms, that)
            case that: NmsStack     => NmsStack.equals(nms, that.nms)
            case _                  => false
        }

        override def hashCode(): Int = nms.hashCode()
    }

    object ObcFurnaceInventory {
        def unapply(arg: FurnaceInventory): Option[TileEntityFurnace] = {
            arg match {
                case obc: CraftInventoryFurnace => Some(obc.getInventory.asInstanceOf[TileEntityFurnace])
                case _ => None
            }
        }
    }

    object ObcCraftingInventory {
        type Matrix = IInventory
        type Result = IInventory
        def unapply(arg: CraftingInventory): Option[(Matrix, Result)] = {
            arg match {
                case obc: CraftInventoryCrafting => Some((obc.getMatrixInventory, obc.getResultInventory))
                    //could be a CraftInventoryCustom or sth?
                case _ => None
            }
        }
    }

    object NmsInventory {
        def unapply(arg: IInventory): Option[(java.util.List[ItemStack], Option[InventoryHolder])] =
            Some((arg.getContents, Option(arg.getOwner)))
    }

    implicit class NmsInventory(iinventory: IInventory) {
        def isEmpty(): Boolean = iinventory.P_()
        def getNumberOfProperties(): Int = iinventory.h()
        def getWidth(): Int = iinventory.U_()
        def getHeight(): Int = iinventory.n()
    }

    implicit class NmsContainer(container: Container) {
        def viewItemStacks(): NonNullList[ItemStack] = container.a()
        def isViewedBy(entityHuman: EntityHuman): Boolean = container.c(entityHuman)
    }

    implicit class NmsSlot(slot: Slot) {
        def isThis(inventory: IInventory, index: Int): Boolean = slot.a(inventory, index)
        def updateInventory(): Unit = slot.f()
    }

    object NmsRecipe {
        type Group = String
    }

    implicit class NmsRecipe(iRecipe: IRecipe) {
        def acceptsInput(inputs: IInventory, world: World): Boolean = iRecipe.a(inputs, world)
        def getResult(): ItemStack = iRecipe.d()
        def getRemainingStacks(matrix: IInventory): NonNullList[ItemStack] = iRecipe.b(matrix)
        def getIngredients(): NonNullList[RecipeItemStack] = iRecipe.e()
        def isHidden(): Boolean = iRecipe.c()
        def getSerializer(): RecipeSerializer[_ <: IRecipe] = iRecipe.a()
    }

    implicit class NmsShapedRecipe(shapedRecipes: ShapedRecipes) {
        def getWidth(): Int = shapedRecipes.g()
        def getHeight(): Int = shapedRecipes.h()
    }

    object NmsNonNullList {
        def apply[T](): NonNullList[T] = NonNullList.a()
        def apply[T](singleElement: T): NonNullList[T] = {
            val list: NonNullList[T] = NonNullList.a()
            list.add(singleElement)
            list
        }
        def apply[T](size: Int, emptyElement: T): NonNullList[T] = NonNullList.a(size, emptyElement)
        def apply[T](emptyElement: T, elements: Seq[T]): NonNullList[T] = {
            val list: NonNullList[T] = NonNullList.a(emptyElement)
            elements.foreach(list.add)
            list
        }
    }

    object NmsIngredient {
        //the next two apply methods are more like deserializers. do I want to name them deserialize() explicitly?
        def apply(json: JsonElement): RecipeItemStack = RecipeItemStack.a(json)
        def apply(packetDataSerializer: PacketDataSerializer): RecipeItemStack = RecipeItemStack.b(packetDataSerializer)
        def apply(iMaterial: IMaterial): RecipeItemStack = RecipeItemStack.a(iMaterial)
        def empty: RecipeItemStack = RecipeItemStack.a
    }

    implicit class NmsIngredient(recipeItemStack: RecipeItemStack) {
        def isEmpty(): Boolean = recipeItemStack.d()
        def nonEmpty(): Boolean = !isEmpty()
        def acceptsItem(itemStack: ItemStack): Boolean = recipeItemStack.test(itemStack)
    }

    implicit class NmsRecipeSerializer[T <: IRecipe](serializer: RecipeSerializer[T]) {
        def readJson(minecraftKey: MinecraftKey, json: JsonObject): T = serializer.a(minecraftKey, json)
        def read(minecraftKey: MinecraftKey, packetDataSerializer: PacketDataSerializer): T = serializer.a(minecraftKey, packetDataSerializer)
        def write(packetDataSerializer: PacketDataSerializer, recipe: T): Unit = serializer.a(packetDataSerializer, recipe)
        def getRecipeType(): String = serializer.a()
    }
}
