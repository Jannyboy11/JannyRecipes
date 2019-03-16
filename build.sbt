val Name = "JannyRecipes"
val Version = "3.0.0-ALPHA"

name := Name
version := Version
organization := "xyz.janboerman"

scalaVersion := "2.13.0-M5"
scalacOptions += "-language:implicitConversions"

packageOptions in (Compile, packageBin) +=
    Package.ManifestAttributes("Automatic-Module-Name" -> "xyz.janboerman.recipes")

//resolvers += "spigot-repo" at "https://hub.spigotmc.org/nexus/content/repositories/snapshots/"
resolvers += Resolver.jcenterRepo
resolvers += Resolver.mavenLocal
resolvers += "jitpack" at "https://jitpack.io"
resolvers += "wesjd-repo" at "https://nexus.wesjd.net/repository/thirdparty/"

libraryDependencies ++= Seq(
    //dependencies provided at runtime
    "org.bukkit" % "craftbukkit" % "1.13.2-R0.1-SNAPSHOT" % "provided",
    "com.github.Jannyboy11.ScalaPluginLoader" % "ScalaLoader" % "v0.10" % "provided",

    //dependencies that need to be bundled
    "com.github.Jannyboy11.GuiLib" % "GuiLib-API" % "v1.8.4",
    "net.wesjd" % "anvilgui" % "1.2.1-SNAPSHOT",
)

assemblyShadeRules in assembly := Seq(
    //should now work again because the assembly plugin now depends on jarjar 1.7.1 which should support the java 11 class file format
    ShadeRule.rename("xyz.janboerman.guilib.**" -> "xyz.janboerman.recipes.guilib.@1").inAll,
    ShadeRule.rename("net.wesjd.anvilgui.**" -> "xyz.janboerman.recipes.anvilgui.@1").inAll,
)

assemblyOption in assembly := (assemblyOption in assembly).value.copy(includeScala = false)
assemblyJarName in assembly := Name + "-" + Version + ".jar"