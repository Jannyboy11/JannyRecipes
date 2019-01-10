package xyz.janboerman.recipes.api.recipe

//TODO in a future release, create create api for BrewingRecipe

trait Recipe {
    //this is one out of 2 things recipes have in common.
    //the other thing is the acceptsInput - but we don't put that here, we use the tryCraft and trySmelt instead.
    def isHidden(): Boolean = false
    //if we ever do brewing recipes then we move this method to CraftingRecipe and SmeltingRecipe
    //or should I remove this super type completely after all?

    //TODO getKey: RecipeKey
}
