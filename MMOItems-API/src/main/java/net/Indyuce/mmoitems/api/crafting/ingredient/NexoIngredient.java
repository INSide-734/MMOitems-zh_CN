package net.Indyuce.mmoitems.api.crafting.ingredient;

import com.nexomc.nexo.api.NexoItems;
import com.nexomc.nexo.items.ItemBuilder;
import io.lumine.mythic.lib.UtilityMethods;
import io.lumine.mythic.lib.api.MMOLineConfig;
import io.lumine.mythic.lib.util.lang3.Validate;
import net.Indyuce.mmoitems.api.crafting.ingredient.inventory.NexoPlayerIngredient;
import net.Indyuce.mmoitems.api.player.RPGPlayer;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;

public class NexoIngredient extends Ingredient<NexoPlayerIngredient> {
    private final String id;
    private final String display;

    public NexoIngredient(MMOLineConfig config) {
        super("nexo", config);

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
    public boolean matches(NexoPlayerIngredient playerIngredient) {
        return playerIngredient.getId().equals(id);
    }

    @NotNull
    @Override
    public ItemStack generateItemStack(@NotNull RPGPlayer player, boolean forDisplay) {
        ItemBuilder builder = NexoItems.itemFromId(id);
        Validate.notNull(builder, String.format("Could find item with ID '%s'", id));

        ItemStack generated = builder.build();
        generated.setAmount(getAmount());
        return generated;
    }

    @Override
    public String toString() {
        return "NexoIngredient{" +
                "id='" + id + '\'' +
                '}';
    }

    private String findName() {

        // Try generating the item and getting the display name.
        ItemBuilder tryGenerate = NexoItems.itemFromId(id);
        if (tryGenerate != null) {
            ItemStack asStack = tryGenerate.build();

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
