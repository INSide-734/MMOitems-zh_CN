package net.Indyuce.mmoitems.api.crafting.ingredient;

import net.Indyuce.mmoitems.api.crafting.ingredient.inventory.PlayerIngredient;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.Iterator;
import java.util.List;

public class CheckedIngredient {
    @NotNull
    private final Ingredient<?> ingredient;
    @NotNull
    private final List<PlayerIngredient> found;
    private final boolean isHad;

    /**
     * Instantiated everytime an ingredient is evaluated for a player when a
     * CheckedRecipe is being created (when a player is opening a crafting
     * station). This helps greatly reducing ingredient checkups by caching
     * the items the plugin will need to take off the player's ingredient
     *
     * @param ingredient The ingredient being evaluated
     * @param found      The corresponding ingredient found in the player's
     *                   ingredient
     */
    public CheckedIngredient(@NotNull Ingredient<?> ingredient, @NotNull List<PlayerIngredient> found) {
        this.ingredient = ingredient;
        this.found = found;
        this.isHad = getTotalAmount() >= ingredient.getAmount();
    }

    /**
     * @return If the player has enough of the specific item or not
     */
    public boolean isHad() {
        return isHad;
    }

    public int getTotalAmount() {
        int total = 0;
        for (PlayerIngredient ing : this.found)
            total += ing.getAmount();
        return total;
    }

    /**
     * Takes off the required amount of ingredients from a player's inventory.
     */
    public void takeAway() {
        reduceItem(ingredient.getAmount());
    }

    /**
     * Takes off a specific amount of ingredients from a player's inventory.
     *
     * @param amount Amount to take off the player's inventory.
     *               It most likely matches ingredient.getAmount()
     */
    public void reduceItem(int amount) {

        Iterator<PlayerIngredient> iterator = found.iterator();
        while (iterator.hasNext() && amount > 0) {
            ItemStack item = iterator.next().getItem();

            // Remove itemStack from list if amount <= 0
            if (item.getAmount() <= 0) {
                iterator.remove();
                continue;
            }

            // Amount of items it can take from this particular ItemStack
            int take = Math.min(item.getAmount(), amount);

            amount -= take;
            item.setAmount(item.getAmount() - take);
        }
    }

    @NotNull
    public Ingredient<?> getIngredient() {
        return ingredient;
    }

    @NotNull
    public List<PlayerIngredient> getFound() {
        return found;
    }

    @NotNull
    public String format() {
        return ingredient.formatDisplay(ingredient.getDisplay().format(isHad));
    }
}