package com.janboerman.recipes.api.recipe

trait Grouped { self: Recipe =>
    protected var group: String = null

    def getGroup(): Option[String] = Option(group).filter(_.nonEmpty)

}