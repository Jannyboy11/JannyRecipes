package com.janboerman.recipes.v1_13_R2.recipe.bukkit

import java.util
import java.util.{Arrays, Collections}
import java.util.stream.Collectors

import com.janboerman.recipes.v1_13_R2.Conversions
import com.janboerman.recipes.v1_13_R2.Extensions.NmsIngredient
import net.minecraft.server.v1_13_R2.RecipeItemStack.StackProvider
import net.minecraft.server.v1_13_R2.{ItemStack, RecipeItemStack}
import org.bukkit.Material
import org.bukkit.craftbukkit.v1_13_R2.inventory.CraftItemStack
import org.bukkit.inventory.RecipeChoice.MaterialChoice
import com.janboerman.recipes.v1_13_R2.Extensions.NmsIngredient
import com.janboerman.recipes.v1_13_R2.Conversions

case class BetterRecipeChoice(recipeItemStack: RecipeItemStack) extends MaterialChoice({
    recipeItemStack.buildChoices()
    if (recipeItemStack.choices.isEmpty) {
        //needed because MaterialChoice can't have an empty list.
        Collections.singletonList(Material.AIR)
    } else {
        Arrays.stream(recipeItemStack.choices)
            .map[ItemStack](itemStack => itemStack.cloneItemStack())
            .map[CraftItemStack](itemStack => Conversions.toBukkitStack(itemStack))
            .map[Material](itemStack => itemStack.getType())
            .collect(Collectors.toList())
    }
}) {
    //we can assume the choices array was already built, because we built it in our constructor call :)

    @Deprecated
    override def getItemStack: org.bukkit.inventory.ItemStack = {
        if (recipeItemStack.choices.isEmpty) {
            new org.bukkit.inventory.ItemStack(Material.AIR)
        } else if (recipeItemStack.choices.length == 0) {
            Conversions.toBukkitStack(recipeItemStack.choices(0))
        } else {
            val cloneStack = recipeItemStack.choices(0).cloneItemStack()
            val bukkitStack = Conversions.toBukkitStack(cloneStack)
            bukkitStack.setDurability(Short.MaxValue)
            bukkitStack
        }
    }

    override def test(bukkitStack: org.bukkit.inventory.ItemStack): Boolean =
        recipeItemStack.acceptsItem(Conversions.toNMSStack(bukkitStack))

    override def getChoices: util.List[Material] = Arrays.stream(recipeItemStack.choices)
        .map[ItemStack](itemStack => itemStack.cloneItemStack())
        .map[CraftItemStack](itemStack => Conversions.toBukkitStack(itemStack))
        .map[Material](itemStack => itemStack.getType())
        .collect(Collectors.toList())

    override def clone(): BetterRecipeChoice = {
        BetterRecipeChoice({
            if (recipeItemStack.choices.isEmpty){
                NmsIngredient.empty
            } else {
                new RecipeItemStack(Arrays.stream(recipeItemStack.choices)
                    .map[ItemStack](_.cloneItemStack())
                    .map[StackProvider](new StackProvider(_)))
            }
        })
    }

}
