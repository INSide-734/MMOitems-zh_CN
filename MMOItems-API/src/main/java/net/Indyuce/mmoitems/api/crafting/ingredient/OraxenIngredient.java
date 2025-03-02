package net.Indyuce.mmoitems.api.crafting.ingredient;

import io.lumine.mythic.lib.UtilityMethods;
import io.lumine.mythic.lib.api.MMOLineConfig;
import io.lumine.mythic.lib.util.Lazy;
import io.th0rgal.oraxen.api.OraxenItems;
import io.th0rgal.oraxen.items.ItemBuilder;
import net.Indyuce.mmoitems.api.crafting.ingredient.inventory.OraxenPlayerIngredient;
import net.Indyuce.mmoitems.api.player.RPGPlayer;
import org.apache.commons.lang3.Validate;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;

public class OraxenIngredient extends Ingredient<OraxenPlayerIngredient> {
    private final String id;

    /**
     * Using a Lazy because Oraxen loads after MMOItems.
     */
    private final Lazy<String> display;

    public OraxenIngredient(MMOLineConfig config) {
        super("oraxen", config);

        config.validate("id");
        id = config.getString("id");

        // Find the display name of the item
        display = config.contains("display") ? Lazy.of(config.getString("display")) : Lazy.of(() -> findName());
    }

    @Override
    public String formatDisplay(String s) {
        return s.replace("#item#", display.get()).replace("#amount#", String.valueOf(getAmount()));
    }

    @Override
    public boolean matches(OraxenPlayerIngredient playerIngredient) {
        return playerIngredient.getId().equals(id);
    }

    @NotNull
    @Override
    public ItemStack generateItemStack(@NotNull RPGPlayer player, boolean forDisplay) {
        ItemBuilder builder = OraxenItems.getItemById(id);
        Validate.notNull(builder, String.format("No Oraxen item found with ID '%s'", id));
        ItemStack stack = builder.build();
        stack.setAmount(getAmount());
        return stack;
    }

    @Override
    public String toString() {
        return "OraxenIngredient{" +
                "id='" + id + '\'' +
                '}';
    }

    private String findName() {

        // Try generating the item and getting the display name.
        ItemBuilder builder = OraxenItems.getItemById(id);
        if (builder != null) {
            ItemStack asStack = builder.build();

            // Try to retrieve display name
            if (asStack.hasItemMeta()) {
                ItemMeta meta = asStack.getItemMeta();
                if (meta.hasDisplayName())
                    return meta.getDisplayName();
            }

            // Use material to generate name
            return UtilityMethods.caseOnWords(asStack.getType().name().toLowerCase().replace("_", " "));
        }

        return "Unknown Item";
    }
}
