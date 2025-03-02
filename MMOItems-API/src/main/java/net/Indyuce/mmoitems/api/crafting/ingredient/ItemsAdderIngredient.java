package net.Indyuce.mmoitems.api.crafting.ingredient;

import dev.lone.itemsadder.api.CustomStack;
import io.lumine.mythic.lib.UtilityMethods;
import io.lumine.mythic.lib.api.MMOLineConfig;
import io.lumine.mythic.lib.util.lang3.Validate;
import net.Indyuce.mmoitems.api.crafting.ingredient.inventory.ItemsAdderPlayerIngredient;
import net.Indyuce.mmoitems.api.player.RPGPlayer;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;

public class ItemsAdderIngredient extends Ingredient<ItemsAdderPlayerIngredient> {
    private final String id;
    private final String display;

    public ItemsAdderIngredient(MMOLineConfig config) {
        super("itemsadder", config);

        config.validate("id");
        id = config.getString("id");

        // Find the display name of the item
        display = config.contains("display") ? config.getString("display") : findName();
    }

    @Override
    public String formatDisplay(String s) {
        return s.replace("#item#", display).replace("#amount#", String.valueOf(getAmount()));
    }

    @Override
    public boolean matches(ItemsAdderPlayerIngredient playerIngredient) {
        return playerIngredient.getId().equals(id);
    }

    @NotNull
    @Override
    public ItemStack generateItemStack(@NotNull RPGPlayer player, boolean forDisplay) {
        CustomStack item = CustomStack.getInstance(id);
        Validate.notNull(item, String.format("Could not find item with ID '%s'", id));

        ItemStack generated = item.getItemStack();
        generated.setAmount(getAmount());
        return generated;
    }

    @Override
    public String toString() {
        return "ItemsAdderIngredient{" +
                "id='" + id + '\'' +
                '}';
    }

    private String findName() {

        // Try generating the item and getting the display name.
        CustomStack tryGenerate = CustomStack.getInstance(id);
        if (tryGenerate != null) {
            ItemStack asStack = tryGenerate.getItemStack();

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
