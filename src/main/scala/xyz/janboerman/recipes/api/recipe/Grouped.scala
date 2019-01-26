package xyz.janboerman.recipes.api.recipe

trait Grouped {
    protected var group: String = null

    def getGroup(): Option[String] = Option(group).filter(_.nonEmpty)

}