package net.Indyuce.mmoitems.api.crafting.output;

import io.lumine.mythic.lib.util.configobject.ConfigObject;
import io.th0rgal.oraxen.api.OraxenItems;
import io.th0rgal.oraxen.items.ItemBuilder;
import net.Indyuce.mmoitems.api.player.RPGPlayer;
import org.apache.commons.lang3.Validate;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public class OraxenRecipeOutput extends RecipeOutput {
    private final String id;

    public OraxenRecipeOutput(ConfigObject config) {
        super(config);

        id = config.getString("id");
    }

    @Override
    public ItemStack generateOutput(@NotNull RPGPlayer rpg) {
        ItemBuilder builder = OraxenItems.getItemById(id);
        Validate.notNull(builder, String.format("No Oraxen item with ID '%s'", id));
        return builder.build();
    }

    @Override
    public ItemStack getPreview() {
        ItemBuilder builder = OraxenItems.getItemById(id);
        Validate.notNull(builder, String.format("No Oraxen item with ID '%s'", id));
        return builder.build();
    }
}
