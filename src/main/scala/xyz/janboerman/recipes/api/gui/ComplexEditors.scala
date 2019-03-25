package xyz.janboerman.recipes.api.gui

import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.inventory.{Inventory, ItemStack}
import org.bukkit.inventory.meta.{BookMeta, LeatherArmorMeta}
import org.bukkit.{ChatColor, DyeColor, Material}
import org.bukkit.plugin.Plugin
import xyz.janboerman.guilib.api.ItemBuilder
import xyz.janboerman.guilib.api.menu.{ClaimButton, PermissionButton, RedirectItemButton}
import xyz.janboerman.recipes.api.JannyRecipesAPI
import xyz.janboerman.recipes.api.recipe.{ArmorDyeRecipe, ArmorDyeType}

object ComplexEditors {
    val BookPermission = "jannyrecipes.editor.book.claim"
    val BookAuthor = "Jannyboy11"
}
import ComplexEditors.BookPermission
import ComplexEditors.BookAuthor

class ArmorDyeRecipeEditor[P <: Plugin](armorDyeRecipe: ArmorDyeRecipe, inventory: Inventory)
                                               (implicit recipesMenu: RecipesMenu[P], api: JannyRecipesAPI, plugin: P)
extends RecipeEditor[P, ArmorDyeRecipe](armorDyeRecipe, inventory) {

    override def onClick(event: InventoryClickEvent): Unit = {
        super.onClick(event)
        event.setCancelled(true)
    }

    override def makeRecipe(): Option[ArmorDyeRecipe] = {
        Option(armorDyeRecipe)
    }

    override def getIcon(): Option[ItemStack] = Some(ArmorDyeType.getIcon())

    override protected def layoutBorder(): Unit = {
        val inventory = getInventory
        val glassPaneItem = new ItemBuilder(Material.LIGHT_BLUE_STAINED_GLASS_PANE).name(Space).build()

        for (i <- 0 until (4*9, 9)) {
            inventory.setItem(i+4, glassPaneItem)
        }
        for (i <- 4*9 until 5*9) {
            inventory.setItem(i, glassPaneItem)
        }
    }

    override protected def layoutRecipe(): Unit = {
        val inventory = getInventory

        val dyes = Array(Material.ROSE_RED, Material.ORANGE_DYE, Material.DANDELION_YELLOW, Material.LIME_DYE,
            Material.LAPIS_LAZULI, Material.CYAN_DYE, Material.LIGHT_BLUE_DYE, Material.CACTUS_GREEN,
            Material.PURPLE_DYE, Material.MAGENTA_DYE, Material.PINK_DYE, Material.COCOA_BEANS,
            Material.BONE_MEAL, Material.LIGHT_GRAY_DYE, Material.GRAY_DYE, Material.INK_SAC)

        val colours = Array(DyeColor.RED, DyeColor.ORANGE, DyeColor.YELLOW, DyeColor.LIME,
            DyeColor.BLUE, DyeColor.CYAN, DyeColor.LIGHT_BLUE, DyeColor.GREEN,
            DyeColor.PURPLE, DyeColor.MAGENTA, DyeColor.PINK, DyeColor.BROWN,
            DyeColor.WHITE, DyeColor.LIGHT_GRAY, DyeColor.GRAY, DyeColor.BLACK)

        for (i <- 0 until 16) {
            val x = i % 4
            val y = i / 4

            val slot = y * 9 + x

            val dyeColour = colours(i)
            val dye = dyes(i)

            inventory.setItem(slot, new ItemStack(dye))
            inventory.setItem(slot + 5, new ItemBuilder(x match {
                case 0 => Material.LEATHER_HELMET
                case 1 => Material.LEATHER_CHESTPLATE
                case 2 => Material.LEATHER_LEGGINGS
                case 3 => Material.LEATHER_BOOTS
            }).changeMeta[LeatherArmorMeta](_.setColor(dyeColour.getColor)).build())
        }
    }

    override protected def layoutButtons(): Unit = {
        val bookIndex = 49
        val deleteIndex = 51
        val exitIndex = 53

        val bookButton = new PermissionButton[ArmorDyeRecipeEditor[P]](BookPermission,
            new ClaimButton[ArmorDyeRecipeEditor[P]](new ItemBuilder(Material.WRITTEN_BOOK)
            .changeMeta[BookMeta](meta => {
                meta.setDisplayName(static("Type: Armour Dying"))
                meta.setAuthor(BookAuthor)
                meta.addPage(ChatColor.BOLD + "The Armour Dye Recipe\n\n" + ChatColor.RESET +
                    "This recipe is the recipe that allows players to dye armour." +
                    "The ingredients are a leather armour piece, and one of the 16 dyes.")
                meta.addPage(
                    "The result is the same kind of leather armour piece, coloured by the dye." +
                    "The recipe acts a lot like a shapeless crafting recipe." +
                    "The only difference is that the result item is not fixed.")
        }).build(), true))

        val deleteButton = new DeleteButton()

        val exitButton = new RedirectItemButton[ArmorDyeRecipeEditor[P]](
            new ItemBuilder(Material.RED_CONCRETE).name(Exit).build(),
            () => recipesMenu.getInventory())

        setButton(bookIndex, bookButton)
        setButton(deleteIndex, deleteButton)
        setButton(exitIndex, exitButton)
    }
}

//TODO a class for every of the 13 complex recipe types. hurray! :D I'll probably put a book in there for admins to claim wich explains the functionality of each recipe.
