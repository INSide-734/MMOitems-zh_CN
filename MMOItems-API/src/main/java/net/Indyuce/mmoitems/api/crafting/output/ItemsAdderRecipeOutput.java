package net.Indyuce.mmoitems.api.crafting.output;

import dev.lone.itemsadder.api.CustomStack;
import io.lumine.mythic.lib.util.configobject.ConfigObject;
import net.Indyuce.mmoitems.api.player.RPGPlayer;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public class ItemsAdderRecipeOutput extends RecipeOutput {
    private final String id;

    public ItemsAdderRecipeOutput(ConfigObject config) {
        super(config);

        id = config.getString("id");
    }

    @Override
    public ItemStack generateOutput(@NotNull RPGPlayer rpg) {
        return CustomStack.getInstance(id).getItemStack();
    }

    @Override
    public ItemStack getPreview() {
        return CustomStack.getInstance(id).getItemStack();
    }
}
