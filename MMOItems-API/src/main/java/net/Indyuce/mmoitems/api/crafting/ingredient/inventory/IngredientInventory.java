package net.Indyuce.mmoitems.api.crafting.ingredient.inventory;

import io.lumine.mythic.lib.MythicLib;
import io.lumine.mythic.lib.api.item.NBTItem;
import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.api.crafting.ingredient.CheckedIngredient;
import net.Indyuce.mmoitems.api.crafting.ingredient.Ingredient;
import net.Indyuce.mmoitems.api.crafting.ingredient.IngredientType;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class IngredientInventory {
    private final Map<String, List<PlayerIngredient>> ingredients = new HashMap<>();

    /**
     * Loads all the possible crafting station ingredients from a player's inventory
     */
    public IngredientInventory(Player player) {
        this(player.getInventory());
    }

    /**
     * Loads all the possible crafting station ingredients from an inventory
     */
    public IngredientInventory(Inventory inv) {

        // Parse full inventory
        for (ItemStack item : inv.getContents()) {
            if (item == null || item.getType() == Material.AIR || item.getAmount() <= 0) continue;

            NBTItem nbt = MythicLib.plugin.getVersion().getWrapper().getNBTItem(item);
            IngredientType<?> type = matchIngredientType(nbt);
            addIngredient(nbt, type);
        }
    }

    @NotNull
    private IngredientType<?> matchIngredientType(NBTItem nbtItem) {
        for (IngredientType<?> ingredientType : MMOItems.plugin.getCrafting().getIngredients())
            if (ingredientType.check(nbtItem)) return ingredientType;
        throw new RuntimeException("No ingredient type matching");
    }

    /**
     * Registers an ingredient.
     *
     * @param item       The actual item in the inventory
     * @param ingredient The type of the ingredient added
     */
    public void addIngredient(NBTItem item, IngredientType<?> ingredient) {
        final String key = ingredient.getId();
        final List<PlayerIngredient> ingredients = this.ingredients.computeIfAbsent(key, ignored -> new ArrayList<>());
        ingredients.add(ingredient.readPlayerIngredient(item));
    }

    @NotNull
    public CheckedIngredient findMatching(@NotNull Ingredient<?> ingredient) {
        List<PlayerIngredient> found = new ArrayList<>();
        if (!ingredients.containsKey(ingredient.getPrefix()))
            return new CheckedIngredient(ingredient, found);

        for (PlayerIngredient checked : ingredients.get(ingredient.getPrefix()))
            if (((Ingredient) ingredient).matches(checked))
                found.add(checked);

        return new CheckedIngredient(ingredient, found);
    }

    /**
     * @deprecated First use {@link #findMatching(Ingredient)} and cache its
     *         result to use the isHad() method of returned class instead.
     */
    @Deprecated
    public boolean hasIngredient(Ingredient<?> ingredient) {
        return findMatching(ingredient).isHad();
    }
}
