package com.janboerman.recipes.event

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import com.janboerman.recipes.JannyImplementation

object ImplementationEvent {
    private val handlers = new HandlerList()

    def getHandlerList(): HandlerList = handlers

    def apply(): ImplementationEvent = new ImplementationEvent()

    def unapply(arg: ImplementationEvent): Option[JannyImplementation] = Option(arg.implementation).filter(_.tryInitialize())
}

/**
  * The ImplementationEvent is called when the plugin couldn't provide a built-in implementation
  * that works on the server implementation it currently runs on.
  * This event can be used to set your own implementation that works for your server.
  */
class ImplementationEvent private(private var implementation: JannyImplementation) extends Event {
    def this() = this(null)

    def setImplementation(implementation: JannyImplementation): Unit =
        this.implementation = implementation

    def getImplementation(): JannyImplementation = this.implementation

    override def getHandlers(): HandlerList = ImplementationEvent.handlers

}
