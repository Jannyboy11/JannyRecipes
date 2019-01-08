package xyz.janboerman.recipes

import org.bukkit.NamespacedKey
import org.bukkit.permissions.PermissionDefault
import org.bukkit.plugin.PluginLoadOrder
import xyz.janboerman.guilib.api.GuiListener
import xyz.janboerman.recipes.api.JannyRecipesAPI
import xyz.janboerman.recipes.api.gui.RecipeGuiFactory
import xyz.janboerman.recipes.api.recipe.Recipe
import xyz.janboerman.recipes.command.{ReEditCommandExecutor, ReOpenCommandExecutor, RecipesCommandExecutor}
import xyz.janboerman.recipes.event.ImplementationEvent
import xyz.janboerman.recipes.listeners.GuiInventoryHolderListener
import xyz.janboerman.scalaloader.plugin.ScalaPluginDescription.{Command, Permission}
import xyz.janboerman.scalaloader.plugin.description.{Api, ApiVersion, Scala, ScalaVersion}
import xyz.janboerman.scalaloader.plugin.{ScalaPlugin, ScalaPluginDescription}

@Api(value = ApiVersion.v1_13)
@Scala(version = ScalaVersion.v2_13_0_M5)
object RecipesPlugin
    extends ScalaPlugin(new ScalaPluginDescription("JannyRecipes", "3.0.0-SNAPSHOT")
        .authors("Jannyboy11")
        .website("https://www.github.com/Jannyboy11/JannyRecipes")
        .prefix("JR")
        .description("Plugin and library for managing crafting and smelting recipes")
        .loadOrder(PluginLoadOrder.STARTUP)
        .addCommand(new Command("recipes").description("Open the recipes manager").permission("jannyrecipes.command.recipes"))
        .addCommand(new Command("reopen").description("Reopens the GUI").permission("jannyrecipes.command.reopen").aliases("re-open"))
        .addCommand(new Command("reedit").description("Reopens the last editor GUI").permission("jannyrecipes.command.reedit").aliases("re-edit"))
        .addPermission(new Permission("jannyrecipes.command.recipes").description("Allows access to /recipes").permissionDefault(PermissionDefault.OP))
        .addPermission(new Permission("jannyrecipes.command.reopen").description("Allows access to /reopen").permissionDefault(PermissionDefault.OP))
        .addPermission(new Permission("jannyrecipes.command.reedit").description("Allows access to /reedit").permissionDefault(PermissionDefault.OP)))
    with JannyRecipesAPI {

    private var implementation: JannyImplementation = null
    private var guiListener: GuiListener = null

    implicit def getGuiListener: GuiListener = guiListener

    override def onLoad(): Unit = {
        if (implementation == null) {
            //try to load a working builtin implementation
            for {
                implementation <- JannyImplementation(getServer)
            } yield setImplementation(implementation)
        }
    }

    override def onEnable(): Unit = {
        val pluginManager = getServer.getPluginManager
        guiListener = GuiListener.getInstance()
        pluginManager.registerEvents(guiListener, this)
        pluginManager.registerEvents(GuiInventoryHolderListener, this)

        getCommand("recipes").setExecutor(RecipesCommandExecutor)
        getCommand("reopen").setExecutor(ReOpenCommandExecutor)
        getCommand("reedit").setExecutor(ReEditCommandExecutor)

        if (implementation == null) { //we couldn't find the right implementation ourselves.
            //let another plugin try it (it should have JannyRecipes in its load-before option in the plugin.yml!
            val event = ImplementationEvent()
            pluginManager.callEvent(event)
            event match {
                case ImplementationEvent(workingImplementation) => setImplementation(workingImplementation)
                case _ =>
                    getLogger.severe("Failed to acquire a working recipe implementation. Disabling...")
                    pluginManager.disablePlugin(this)
            }
        }
    }

    def setImplementation(jannyImplementation: JannyImplementation): Boolean = {
        if (implementation != null) throw new IllegalStateException("Cannot set implementation twice. Use the ImplementationEvent instead. Be sure to make your plugin load before " + getName + ".")
        if (jannyImplementation == null) throw new NullPointerException("implementation cannot be null.")
        if (!jannyImplementation.tryInitialize()) {
            getLogger.info("Implementation " + implementation + " could not be initialized.")
            return false
        }
        this.implementation = jannyImplementation
        getLogger.info("Using recipes implementation " + jannyImplementation)
        true
    }

    override def fromBukkitRecipe(bukkit: org.bukkit.inventory.Recipe): Recipe = implementation.fromBukkitRecipe(bukkit)

    override def toBukkitRecipe(janny: Recipe): org.bukkit.inventory.Recipe = implementation.toBukkitRecipe(janny)

    override def getRecipe(key: NamespacedKey): Recipe = implementation.getRecipe(key)

    override def addRecipe(key: NamespacedKey, recipe: Recipe): Boolean = implementation.addRecipe(key, recipe)

    override def removeRecipe(recipe: Recipe): Boolean = implementation.removeRecipe(recipe)

    override def isRegistered(recipe: Recipe): Boolean = implementation.isRegistered(recipe)

    override def iterateRecipes(): Iterator[_ <: Recipe] = implementation.iterateRecipes()

    override def getGuiFactory(): RecipeGuiFactory = implementation.getGuiFactory()
}