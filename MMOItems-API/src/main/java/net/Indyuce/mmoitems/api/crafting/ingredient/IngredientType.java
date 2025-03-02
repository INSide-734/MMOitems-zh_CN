package net.Indyuce.mmoitems.api.crafting.ingredient;

import io.lumine.mythic.lib.api.MMOLineConfig;
import io.lumine.mythic.lib.api.item.NBTItem;
import net.Indyuce.mmoitems.api.crafting.ConditionalDisplay;
import net.Indyuce.mmoitems.api.crafting.LoadedCraftingObject;
import net.Indyuce.mmoitems.api.crafting.ingredient.inventory.PlayerIngredient;

import java.util.function.Function;
import java.util.function.Predicate;

/**
 * A type of ingredient, like a vanilla ingredient or a MMOItems ingredient.
 * <p>
 * See {@link PlayerIngredient} for more information.
 */
public class IngredientType<P extends PlayerIngredient> extends LoadedCraftingObject<Ingredient<P>> {
    private final Predicate<NBTItem> check;
    private final Function<NBTItem, P> readIngredient;

    public IngredientType(String id, Function<MMOLineConfig, Ingredient<P>> function, ConditionalDisplay display, Predicate<NBTItem> check, Function<NBTItem, P> readIngredient) {
        super(id, function, display);

        this.check = check;
        this.readIngredient = readIngredient;
    }

    /**
     * @return If the checked item can be handled by this ingredient
     */
    public boolean check(NBTItem item) {
        return check.test(item);
    }

    /**
     * Reads the ingredient from an NBTItem, called after checking
     * that this ingredient type can handle this NBTItem
     *
     * @return
     */
    public P readPlayerIngredient(NBTItem item) {
        return readIngredient.apply(item);
    }
}