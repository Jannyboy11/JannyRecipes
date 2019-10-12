package xyz.janboerman.recipes

import org.bukkit.Server
import xyz.janboerman.recipes.api.JannyRecipesAPI

import scala.collection.mutable

object JannyImplementation {
    private val implementations = new mutable.ListBuffer[() => JannyImplementation]()
    registerImplementation(() => v1_13_R2.Impl)

    def registerImplementation(implementationFactory: () => JannyImplementation): Unit = {
        implementations += implementationFactory
    }

    def apply(server: Server): Option[JannyImplementation] = {
        for (implementationSupplier <- implementations) {
            val jannyImpl = implementationSupplier()
            if (jannyImpl != null && jannyImpl.tryInitialize()) return Some(jannyImpl)
        }

        None
    }
}

trait JannyImplementation extends JannyRecipesAPI /*TODO plugin type parameter*/ {

    /**
      * Detects the server runtime and checks whether this implementation works on the server runtime.
      * This method may be called multiple times, implementations should cache their results.
      * @return true if this implementation was registered and works, otherwise false
      */
    def tryInitialize(): Boolean

}
