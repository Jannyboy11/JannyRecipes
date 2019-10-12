val Name = "JannyRecipes"
val Version = "3.0.0-ALPHA"

name := Name
version := Version
organization := "xyz.janboerman"

scalaVersion := "2.13.1"
scalacOptions += "-language:implicitConversions"

packageOptions in (Compile, packageBin) +=
    Package.ManifestAttributes("Automatic-Module-Name" -> "xyz.janboerman.recipes")

//resolvers += "spigot-repo" at "https://hub.spigotmc.org/nexus/content/repositories/snapshots/"
resolvers += Resolver.mavenLocal
resolvers += "jitpack" at "https://jitpack.io"
resolvers += "wesjd-repo" at "https://nexus.wesjd.net/repository/thirdparty/"

libraryDependencies ++= Seq(
    //dependencies provided at runtime
    "org.bukkit" % "craftbukkit" % "1.13.2-R0.1-SNAPSHOT" % "provided",
    "com.github.Jannyboy11.ScalaPluginLoader" % "ScalaLoader" % "v0.11.1" % "provided",

    //dependencies that need to be bundled
    "com.github.Jannyboy11.GuiLib" % "GuiLib-API" % "v1.9.2",
    "net.wesjd" % "anvilgui" % "1.2.2-SNAPSHOT",
)

assemblyShadeRules in assembly := Seq(
    ShadeRule.rename("xyz.janboerman.guilib.**" -> "xyz.janboerman.recipes.guilib.@1").inAll,
    ShadeRule.rename("net.wesjd.anvilgui.**" -> "xyz.janboerman.recipes.anvilgui.@1").inAll,
)

assemblyOption in assembly := (assemblyOption in assembly).value.copy(includeScala = false)
assemblyJarName in assembly := Name + "-" + Version + ".jar"