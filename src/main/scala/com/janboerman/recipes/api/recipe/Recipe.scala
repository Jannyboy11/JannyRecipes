package com.janboerman.recipes.api.recipe

//TODO in a future release, create create api for BrewingRecipe

trait Recipe {
    //this is one out of 2 things recipes have in common.
    //the other thing is the acceptsInput - but we don't put that here, we use the tryCraft and trySmelt instead.

    //TODO override this method in Simple Shaped/Shapeless/Furnace implementations. Be sure to take the value into account when (de)serializing
    //TODO can it also work for the janny implementations? should be possible because of the Better implementations they use^^
    def isHidden(): Boolean = false
    //if we ever do brewing recipes then we move this method to CraftingRecipe and SmeltingRecipe
    //or should I remove this super type completely after all?

    //getKey: RecipeKey or NamespacedKey TODO do I want this? nah probably not I think. let's not think about it now when I'm not doing brewing recipes (yet).

    def getType(): RecipeType
}
