package xyz.janboerman.recipes

import net.minecraft.server.v1_13_R2.{CraftingManager, MinecraftServer}

package object v1_13_R2 {
    implicit def getMinecraftServer(): MinecraftServer = MinecraftServer.getServer;
    implicit def getCraftingManager(): CraftingManager = getMinecraftServer.getCraftingManager
}
