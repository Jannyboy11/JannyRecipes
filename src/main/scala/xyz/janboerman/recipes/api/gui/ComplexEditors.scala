package xyz.janboerman.recipes.api.gui

import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.inventory.{Inventory, ItemStack}
import org.bukkit.inventory.meta._
import org.bukkit._
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
        val bookAndQuillSlot = 11

        inventory.setItem(writtenBookSlot, BookCloneType.getIcon())
        inventory.setItem(bookAndQuillSlot, new ItemStack(Material.WRITABLE_BOOK))
        inventory.setItem(resultSlot, BookCloneType.getIcon())
    }
}

class FireworkRocketRecipeEditor[P <: Plugin](recipe: FireworkRocketRecipe, inventory: Inventory)
                                             (implicit recipesMenu: RecipesMenu[P], api: JannyRecipesAPI, plugin: P)
    extends ComplexRecipeEditor[P, FireworkRocketRecipe](recipe, inventory) {

    override def getIcon(): Option[ItemStack] = Some(FireworkRocketType.getIcon())

    override def getBook(): ItemStack = new ItemBuilder(Material.WRITTEN_BOOK)
        .changeMeta[BookMeta](meta => {
        meta.setDisplayName(static("Type: Firework Rocket"))
        meta.setAuthor(BookAuthor)
        meta.addPage(ChatColor.BOLD + "The Firework Rocket Recipe\n\n" + ChatColor.RESET +
            "This recipe allows players to create firework rockets. " +
            "The ingredients are paper, a firework star and gunpowder.")
        meta.addPage("The flight duration of the rocket depends on the amount of gunpowder. " +
            "Per gunpowder, the recipe gains 1 flight duration, with a maximum of 3.")
    }).build()

    override protected def layoutRecipe(): Unit = {
        val inventory = getInventory

        val resultSlot = 24
        val paperSlot = 10
        val starSlot = 11
        val gunpowderSlot1 = 12
        val gunpowderSlot2 = 19

        inventory.setItem(resultSlot, new ItemBuilder(Material.FIREWORK_ROCKET)
            .changeMeta[FireworkMeta](_.setPower(2))
            .build())
        inventory.setItem(paperSlot, new ItemStack(Material.PAPER))
        inventory.setItem(starSlot, new ItemStack(Material.FIREWORK_STAR))
        inventory.setItem(gunpowderSlot1, new ItemStack(Material.GUNPOWDER))
        inventory.setItem(gunpowderSlot2, new ItemStack(Material.GUNPOWDER))
    }

}

class FireworkStarFadeRecipeEditor[P <: Plugin](recipe: FireworkStarFadeRecipe, inventory: Inventory)
                                               (implicit recipesMenu: RecipesMenu[P], api: JannyRecipesAPI, plugin: P)
    extends ComplexRecipeEditor[P, FireworkStarFadeRecipe](recipe, inventory) {

    override def getIcon(): Option[ItemStack] = Some(FireworkStarFadeType.getIcon())

    override def getBook(): ItemStack = new ItemBuilder(Material.WRITTEN_BOOK)
        .changeMeta[BookMeta](meta => {
        meta.setDisplayName(static("Type: Firework Star Fade"))
        meta.setAuthor(BookAuthor)
        meta.addPage(ChatColor.BOLD + "The Firework Star Fade Recipe\n\n" + ChatColor.RESET +
            "This recipe allows players to customise the colours to which firework explosions use when they fade. " +
            "The inputs are a firework star and a bit of dye.")
    }).build()

    override protected def layoutRecipe(): Unit = {
        val inventory = getInventory

        val resultSlot = 24
        val starSlot = 10
        val dyeSlot = 11

        //TODO use icon from FireworkStarFadeType
        inventory.setItem(resultSlot, new ItemBuilder(Material.FIREWORK_STAR)
            .changeMeta[FireworkEffectMeta](_.setEffect(FireworkEffect.builder().withColor(Color.WHITE).withFade(Color.WHITE).build()))
            .build())
        inventory.setItem(starSlot, new ItemStack(Material.FIREWORK_STAR))
        inventory.setItem(dyeSlot, new ItemStack(Material.BONE_MEAL))
    }
}

class FireworkStarRecipeEditor[P <: Plugin](recipe: FireworkStarRecipe, inventory: Inventory)
                                     (implicit recipesMenu: RecipesMenu[P], api: JannyRecipesAPI, plugin: P)
    extends ComplexRecipeEditor[P, FireworkStarRecipe](recipe, inventory) {

    override def getIcon(): Option[ItemStack] = Some(FireworkStarType.getIcon())

    override def getBook(): ItemStack = new ItemBuilder(Material.WRITTEN_BOOK)
        .changeMeta[BookMeta](meta => {
        meta.setDisplayName(static("Type: Firework Star"))
        meta.setAuthor(BookAuthor)
        meta.addPage(ChatColor.BOLD + "The Firework star Recipe\n\n" + ChatColor.RESET +
            "This recipe allows players to customise the colours and patterns fireworks use when they explode.")
        meta.addPage("The base ingredient for this recipe is gungpowder. To specify the colour, use any combination of dyes.")
        meta.addPage("The pattern can be specified as follows:\n"
            + "Fire Charge -> Large Ball\n"
            + "Feather -> Burst\n"
            + "Gold Nugget -> Star\n"
            + "Any type of skull -> Creeper face\n")
        meta.addPage("A diamond gives the firework star its fade effect. Glowstone dust adds a twinkling effect.")

    }).build()

    override protected def layoutRecipe(): Unit = {
        val inventory = getInventory

        val resultSlot = 24
        val gunpowderSlot = 10
        val glowstoneDustSlot = 11
        val diamondSlot = 12
        val dyeOneSlot = 19
        val dyeTwoSlot = 20

        inventory.setItem(resultSlot, FireworkRocketType.getIcon())
        inventory.setItem(gunpowderSlot, new ItemStack(Material.GUNPOWDER))
        inventory.setItem(glowstoneDustSlot, new ItemStack(Material.GLOWSTONE_DUST))
        inventory.setItem(diamondSlot, new ItemStack(Material.DIAMOND))
        inventory.setItem(dyeOneSlot, new ItemStack(Material.DANDELION_YELLOW))
        inventory.setItem(dyeTwoSlot, new ItemStack(Material.ORANGE_DYE))
    }
}

class MapCloneRecipeEditor[P <: Plugin](recipe: MapCloneRecipe, inventory: Inventory)
                                       (implicit recipesMenu: RecipesMenu[P], api: JannyRecipesAPI, plugin: P)
    extends ComplexRecipeEditor[P, MapCloneRecipe](recipe, inventory) {

    override def getIcon(): Option[ItemStack] = Some(MapCloneType.getIcon())

    override def getBook(): ItemStack = new ItemBuilder(Material.WRITTEN_BOOK)
        .changeMeta[BookMeta](meta => {
        meta.setDisplayName(static("Type: Map Clone"))
        meta.setAuthor(BookAuthor)
        meta.addPage(ChatColor.BOLD + "The Map Cloning Recipe\n\n" + ChatColor.RESET +
            "This recipe allows player to duplicate maps. The inputs are a filled map and an empty map.")
        meta.addPage("The result is a copy of the filled map, and only the empty map is consumed by this recipe.")
    }).build()


    override def layoutRecipe(): Unit = {
        val inventory = getInventory

        val resultSlot = 24
        val filledMapSlot = 10
        val emptyMapSlot = 11

        inventory.setItem(resultSlot, MapCloneType.getIcon())
        inventory.setItem(filledMapSlot, MapCloneType.getIcon())
        inventory.setItem(emptyMapSlot, new ItemStack(Material.MAP))
    }
}

class MapExtendRecipeEditor[P <: Plugin](recipe: MapExtendRecipe, inventory: Inventory)
                                        (implicit recipesMenu: RecipesMenu[P], api: JannyRecipesAPI, plugin: P)
    extends ComplexRecipeEditor[P, MapExtendRecipe](recipe, inventory) {

    override def getIcon(): Option[ItemStack] = Some(MapExtendType.getIcon())

    override def getBook(): ItemStack = new ItemBuilder(Material.WRITTEN_BOOK)
        .changeMeta[BookMeta](meta => {
        meta.setDisplayName(static("Type: Map Extend"))
        meta.setAuthor(BookAuthor)
        meta.addPage(ChatColor.BOLD + "The Map Extension Recipe\n\n" + ChatColor.RESET +
            "This recipe allows players to extend their maps such that they cover a larger area.")
        meta.addPage("This recipe requires a filled map in the middle of crafting grid, surrounded with paper.")
        meta.addPage("The output is a new map with the explored in the middle. Maps can be extended up to 4 times.")
    }).build()

    override protected def layoutRecipe(): Unit = {
        val inventory = getInventory

        val paper = new ItemStack(Material.PAPER)
        val enlargedMap = MapExtendType.getIcon()
        val filledMap = new ItemBuilder(enlargedMap).changeMeta[MapMeta](_.setScaling(false)).build()
        inventory.setItem(20, filledMap)
        inventory.setItem(10, paper)
        inventory.setItem(11, paper)
        inventory.setItem(12, paper)
        inventory.setItem(19, paper)
        inventory.setItem(21, paper)
        inventory.setItem(28, paper)
        inventory.setItem(29, paper)
        inventory.setItem(30, paper)
    }
}

class RepairItemRecipeEditor[P <: Plugin](recipe: RepairItemRecipe, inventory: Inventory)
                                         (implicit recipesMenu: RecipesMenu[P], api: JannyRecipesAPI, plugin: P)
    extends ComplexRecipeEditor[P, RepairItemRecipe](recipe, inventory) {

    override def getIcon(): Option[ItemStack] = Some(RepairItemType.getIcon())

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

    override def getBook(): ItemStack = new ItemBuilder(Material.WRITTEN_BOOK)
        .changeMeta[BookMeta](meta => {
        meta.setDisplayName(static("Type: Repair Item"))
        meta.setAuthor(BookAuthor)
        meta.addPage(ChatColor.BOLD + "The Repair Item Recipe\n\n" + ChatColor.RESET +
            "This recipe allows players to repair items in a crafting table. The ingredients are two (damaged) items. " +
            "Together they will produce a new item with more durability.")
        meta.addPage("Note that enchantments and other metadata will be lost from the repaired item.")
    }).build()

    override protected def layoutRecipe(): Unit = {
        val repairableItems = List(Material.TURTLE_HELMET, Material.BOW, Material.LEATHER_LEGGINGS, Material.IRON_SWORD,
            Material.GOLDEN_HOE, Material.TRIDENT, Material.FLINT_AND_STEEL, Material.SHIELD)

        val inventory = getInventory

        var i: Int = 0
        for (material <- repairableItems) {
            val x = (i * 2) % 4
            val y = i / 2

            val slot1 = x + 9 * y
            val slot2 = slot1 + 1
            val repairedSlot = slot1 + 5

            val item = new ItemStack(material)
            val damagedItem = new ItemBuilder(material)
                .changeMeta[ItemMeta](_.asInstanceOf[Damageable].setDamage(material.getMaxDurability / 2))
                .build()

            inventory.setItem(slot1, damagedItem)
            inventory.setItem(slot2, damagedItem)
            inventory.setItem(repairedSlot, item)

            i += 1
        }
    }

}
//TODO ShieldDecoration
//TODO ShulkerBoxColor
//TODO TippedArrow
