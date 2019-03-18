package xyz.janboerman.recipes

import org.bukkit.{Keyed, NamespacedKey}
import org.bukkit.permissions.PermissionDefault
import org.bukkit.plugin.{Plugin, PluginLoadOrder}
import org.bukkit.scheduler.BukkitTask
import xyz.janboerman.guilib.api.GuiListener
import xyz.janboerman.recipes.api.JannyRecipesAPI
import xyz.janboerman.recipes.api.gui.RecipeGuiFactory
import xyz.janboerman.recipes.api.persist.{RecipeStorage, SimpleStorage}
import xyz.janboerman.recipes.api.recipe._
import xyz.janboerman.recipes.command._
import xyz.janboerman.recipes.event.ImplementationEvent
import xyz.janboerman.recipes.listeners.GuiInventoryHolderListener
import xyz.janboerman.scalaloader.plugin.ScalaPluginDescription.{Command, Permission}
import xyz.janboerman.scalaloader.plugin.description.{Api, ApiVersion, Scala, ScalaVersion}
import xyz.janboerman.scalaloader.plugin.{ScalaPlugin, ScalaPluginDescription}

@Api(value = ApiVersion.v1_13)
@Scala(version = ScalaVersion.v2_13_0_M5)
object RecipesPlugin
    extends ScalaPlugin(new ScalaPluginDescription("JannyRecipes", "3.0.0-ALPHA")
        .authors("Jannyboy11")
        .website("https://www.github.com/Jannyboy11/JannyRecipes")
        .prefix("CustomRecipes")
        .description("Plugin and library for managing crafting and smelting recipes")
        .loadOrder(PluginLoadOrder.STARTUP)
        .addCommand(new Command("recipes").description("Open the recipes manager").permission("jannyrecipes.command.recipes"))
        .addCommand(new Command("addrecipe").description("Shortcut command to open the new-recipe wizard").permission("jannyrecipes.command.recipes").aliases("newrecipe"))
        .addCommand(new Command("editrecipe").description("Edit a recipe that uses the item in your hand").permission("jannyrecipes.command.recipes"))
        .addCommand(new Command("reopen").description("Reopens the GUI").permission("jannyrecipes.command.reopen").aliases("re-open"))
        .addCommand(new Command("reedit").description("Reopens the last editor GUI").permission("jannyrecipes.command.reedit").aliases("re-edit"))
        .addCommand(new Command("reloadrecipes").description("Reloads the recipes from the config files").permission("jannyrecipes.reload").aliases("rr", "reloadr", "rrecipes"))
        .addPermission(new Permission("jannyrecipes.command.recipes").description("Allows access to /recipes").permissionDefault(PermissionDefault.OP))
        .addPermission(new Permission("jannyrecipes.command.reopen").description("Allows access to /reopen").permissionDefault(PermissionDefault.OP))
        .addPermission(new Permission("jannyrecipes.command.reedit").description("Allows access to /reedit").permissionDefault(PermissionDefault.OP))
        .addPermission(new Permission("jannyrecipes.command.reloadrecipes").description("Allows access to /reloadrecipes").permissionDefault(PermissionDefault.OP)))
    with JannyRecipesAPI {

    implicit def getAPI(): JannyRecipesAPI = this
    private implicit val api: JannyRecipesAPI = this

    private var implementation: JannyImplementation = null
    private var guiListener: GuiListener = null

    val persistentStorage: SimpleStorage = new SimpleStorage(this)

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
        getCommand("reloadrecipes").setExecutor(ReloadRecipesCommandExecutor)
        getCommand("addrecipe").setExecutor(AddRecipeCommandExecutor)
        getCommand("editrecipe").setExecutor(EditRecipeCommandExecutor)

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

        val persistentStorage = persist()
        if (!persistentStorage.init()) {
            getLogger.warning("Persistent storage layer failed to initialize correctly.")
        }
        getServer().getScheduler.runTask(this, (_: BukkitTask) => {
            //in a bukkit task because minecraft's datapack clear the recipes on startup. lol.
            //TODO find out how to 'implement' a datapack? can I wrap my 'Impl' or 'JannyRecipesAPI' class?
            persistentStorage.loadRecipes() match {
                case Right(iterator) =>
                    var successes = 0
                    var errors = 0
                    for (recipe <- iterator) {
                        val successfullyAdded = addRecipe(recipe)
                        if (!successfullyAdded) {
                            getLogger.warning("Could not register recipe: " + recipe)
                            if (recipe.isInstanceOf[Keyed]) {
                                val key = recipe.asInstanceOf[Keyed].getKey
                                getLogger.warning("It's key is: " + key + ". Is that key already registered?")
                            }
                            errors += 1
                        } else {
                            successes += 1
                        }
                    }
                    if (successes > 0) getLogger.info(s"Loaded $successes recipes.")
                    if (errors > 0) getLogger.severe(s"$errors recipes failed to load.")
                case Left(errorMessage) => getLogger.severe(errorMessage)
            }
        })
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

    override def addRecipe(recipe: Recipe): Boolean = implementation.addRecipe(recipe)

    override def removeRecipe(recipe: Recipe): Boolean = implementation.removeRecipe(recipe)

    override def isRegistered(recipe: Recipe): Boolean = implementation.isRegistered(recipe)

    override def iterateRecipes(): Iterator[_ <: Recipe] = implementation.iterateRecipes()

    override def getGuiFactory[P <: Plugin](): RecipeGuiFactory[P] = implementation.getGuiFactory()

    override def persist(): RecipeStorage = implementation.persist()

}
