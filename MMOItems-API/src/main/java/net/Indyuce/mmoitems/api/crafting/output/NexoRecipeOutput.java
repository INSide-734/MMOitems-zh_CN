package net.Indyuce.mmoitems.api.crafting.output;

import com.nexomc.nexo.api.NexoItems;
import com.nexomc.nexo.items.ItemBuilder;
import io.lumine.mythic.lib.util.configobject.ConfigObject;
import net.Indyuce.mmoitems.api.player.RPGPlayer;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public class NexoRecipeOutput extends RecipeOutput {
    private final String id;

    public NexoRecipeOutput(ConfigObject config) {
        super(config);

        id = config.getString("id");
    }

    @Override
    public ItemStack generateOutput(@NotNull RPGPlayer rpg) {
        ItemBuilder builder = NexoItems.itemFromId(id);
        return builder.build();
    }

    @Override
    public ItemStack getPreview() {
        return NexoItems.itemFromId(id).build();
    }
}
