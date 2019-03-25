package xyz.janboerman.recipes.api.gui

import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.inventory.{Inventory, ItemStack}
import org.bukkit.inventory.meta.{BookMeta, LeatherArmorMeta}
import org.bukkit.{ChatColor, DyeColor, Material}
import org.bukkit.plugin.Plugin
import xyz.janboerman.guilib.api.ItemBuilder
import xyz.janboerman.guilib.api.menu.{ClaimButton, PermissionButton, RedirectItemButton}
import xyz.janboerman.recipes.api.JannyRecipesAPI
import xyz.janboerman.recipes.api.recipe._

object ComplexEditors {
    val BookPermission = "jannyrecipes.editor.book.claim"
    val BookAuthor = "Jannyboy11"
}
import ComplexEditors.BookPermission
import ComplexEditors.BookAuthor

abstract class ComplexRecipeEditor[P <: Plugin, R <: ComplexRecipe](recipe: R, inventory: Inventory)
                                                                   (implicit recipesMenu: RecipesMenu[P], api: JannyRecipesAPI, plugin: P)
    extends RecipeEditor[P, R] (recipe, inventory) {

    override def onClick(event: InventoryClickEvent): Unit = {
        super.onClick(event)
        event.setCancelled(true)
    }

    override def makeRecipe(): Option[R] = {
        Option(recipe)
    }

    protected def layoutBorder(): Unit = {
        val glassPaneStack = new ItemBuilder(Material.LIGHT_BLUE_STAINED_GLASS_PANE).name(Space).build()
        val inventory = getInventory

        for (i <- 0 until 9) inventory.setItem(i, glassPaneStack)
        for (i <- 36 until 45) inventory.setItem(i, glassPaneStack)
        for (y <- 1 until 4) {
            for (x <- Seq(0, 4, 5, 7, 8)) {
                inventory.setItem(y * 9 + x, glassPaneStack)
            }
        }
        inventory.setItem(15, glassPaneStack)
        inventory.setItem(33, glassPaneStack)
    }

    def getBook(): ItemStack

    override protected final def layoutButtons(): Unit = {
        val bookIndex = 49
        val deleteIndex = 51
        val exitIndex = 53

        val bookButton = new PermissionButton[ArmorDyeRecipeEditor[P]](BookPermission,
            new ClaimButton[ArmorDyeRecipeEditor[P]](getBook(), true))

        val deleteButton = new DeleteButton()

        val exitButton = new RedirectItemButton[ArmorDyeRecipeEditor[P]](
            new ItemBuilder(Material.RED_CONCRETE).name(Exit).build(),
            () => recipesMenu.getInventory())

        setButton(bookIndex, bookButton)
        setButton(deleteIndex, deleteButton)
        setButton(exitIndex, exitButton)
    }
}

class ArmorDyeRecipeEditor[P <: Plugin](armorDyeRecipe: ArmorDyeRecipe, inventory: Inventory)
                                       (implicit recipesMenu: RecipesMenu[P], api: JannyRecipesAPI, plugin: P)
    extends ComplexRecipeEditor[P, ArmorDyeRecipe](armorDyeRecipe, inventory) {

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

    def getBook(): ItemStack = new ItemBuilder(Material.WRITTEN_BOOK)
        .changeMeta[BookMeta](meta => {
        meta.setDisplayName(static("Type: Armour Dying"))
        meta.setAuthor(BookAuthor)
        meta.addPage(ChatColor.BOLD + "The Armour Dye Recipe\n\n" + ChatColor.RESET +
            "This recipe is the recipe that allows players to dye armour. " +
            "The ingredients are a leather armour piece, and one of the 16 dyes.")
        meta.addPage("The result is the same kind of leather armour piece, coloured by the dye. " +
            "The recipe acts a lot like a shapeless crafting recipe. " +
            "The only difference is that the result item is not fixed.")
    }).build()
}

class BannerAddPatternRecipeEditor[P <: Plugin](recipe: BannerAddPatternRecipe, inventory: Inventory)
                                               (implicit recipesMenu: RecipesMenu[P], api: JannyRecipesAPI, plugin: P)
    extends ComplexRecipeEditor[P, BannerAddPatternRecipe](recipe, inventory) {

    override def getIcon(): Option[ItemStack] = Some(BannerAddPatternType.getIcon())

    override def getBook(): ItemStack = new ItemBuilder(Material.WRITTEN_BOOK)
        .changeMeta[BookMeta](meta => {
        meta.setDisplayName(static("Type: Banner Add Pattern"))
        meta.setAuthor(BookAuthor)
        meta.addPage(ChatColor.BOLD + "The Banner Pattern Recipe\n\n" + ChatColor.RESET +
            "This recipe is the recipe that allows players to add patterns to their banners.")
        meta.addPage("The ingredients for this recipe are a banner, and pieces of dye of the same colour. " +
            "The pieces of dye need to be in a certain pattern in the inventory grid to create a certain pattern on the banner.")
        meta.addPage("Additionally, there are other special items that can be used to create patterns on banners in combination with dye. " +
            "These are: Golden Apple, Creeper Head, Wither Skeleton Skull, Flower, Vines, Bricks.")
        meta.addPage("For more information about pattern shapes, please consult the Minecraft wiki.")
        //TODO depend on the spigot api and use BookMeta.spigot() to add a page with a clickable url to the minecraft wiki explaining banner patterns?
        //TODO https://minecraft.gamepedia.com/Banner#Pattern_pattern
    }).build()

    override def layoutRecipe(): Unit = {
        val inventory = getInventory

        val resultSlot = 24
        val bannerSlot = 11
        val creeperHeadSlot = 20
        val dyeSlot = 29

        inventory.setItem(bannerSlot, new ItemStack(Material.WHITE_BANNER))
        inventory.setItem(creeperHeadSlot, new ItemStack(Material.CREEPER_HEAD))
        inventory.setItem(dyeSlot, new ItemStack(Material.ROSE_RED))
        inventory.setItem(resultSlot, BannerAddPatternType.getIcon())
    }

}

class BannerDuplicateRecipeEditor[P <: Plugin](recipe: BannerDuplicateRecipe, inventory: Inventory)
                                              (implicit recipesMenu: RecipesMenu[P], api: JannyRecipesAPI, plugin: P)
    extends ComplexRecipeEditor[P, BannerDuplicateRecipe](recipe, inventory) {

    override def getIcon(): Option[ItemStack] = Some(BannerDuplicateType.getIcon())

    override def getBook(): ItemStack = new ItemBuilder(Material.WRITTEN_BOOK)
        .changeMeta[BookMeta](meta => {
        meta.setDisplayName(static("Type: Banner Duplicate"))
        meta.setAuthor(BookAuthor)
        meta.addPage(ChatColor.BOLD + "The Banner Duplication Recipe\n\n" + ChatColor.RESET +
            "This recipe is the recipe that allows players copy banners.")
        meta.addPage("The ingredients are one banner with a pattern, and one banner with the same base colour without a pattern. " +
            "The 'empty' banner is then consumed to produce a new banner with the same pattern as the other banner.")
        meta.addPage("Please consult the Minecraft wiki for more information on how to create patterns on banners.")
        //TODO depend on the spigot api and use BookMeta.spigot() to add a page with a clickable url to the minecraft wiki explaining banner patterns?
        //TODO https://minecraft.gamepedia.com/Banner#Pattern_pattern
    }).build()

    override protected def layoutRecipe(): Unit = {
        val inventory = getInventory

        val resultSlot = 24
        val patternBannerSlot = 10
        val emptyBannerSlot = 11

        inventory.setItem(emptyBannerSlot, new ItemStack(Material.WHITE_BANNER))
        inventory.setItem(patternBannerSlot, BannerDuplicateType.getIcon())
        inventory.setItem(resultSlot, BannerDuplicateType.getIcon())
    }

}

class BookCloneRecipeEditor[P <: Plugin](recipe: BookCloneRecipe, inventory: Inventory)
                                        (implicit recipesMenu: RecipesMenu[P], api: JannyRecipesAPI, plugin: P)
    extends ComplexRecipeEditor[P, BookCloneRecipe](recipe, inventory) {

    override def getIcon(): Option[ItemStack] = Some(BookCloneType.getIcon())

    override def getBook(): ItemStack = new ItemBuilder(Material.WRITTEN_BOOK)
        .changeMeta[BookMeta](meta => {
        meta.setDisplayName(static("Type: Book Clone"))
        meta.setAuthor(BookAuthor)
        meta.addPage(ChatColor.BOLD + "The Book Clone Recipe\n\n" + ChatColor.RESET +
            "This recipe is the recipe that allows players to clone books.")
        meta.addPage("The inputs are a written book and a book and quill. " +
            "The book and quill are consumed by the recipe, the written book remains behind.")
    }).build()

    override protected def layoutRecipe(): Unit = {
        val inventory = getInventory

        val resultSlot = 24
        val writtenBookSlot = 10
        val bookAndQuillSlot = 21

        inventory.setItem(writtenBookSlot, BookCloneType.getIcon())
        inventory.setItem(bookAndQuillSlot, new ItemStack(Material.WRITABLE_BOOK))
        inventory.setItem(resultSlot, BookCloneType.getIcon())
    }
}

//TODO a class for every of the 13 complex recipe types. hurray! :D I'll probably put a book in there for admins to claim wich explains the functionality of each recipe.
