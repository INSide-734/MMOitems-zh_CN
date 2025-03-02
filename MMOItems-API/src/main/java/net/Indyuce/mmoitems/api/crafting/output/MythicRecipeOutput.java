package net.Indyuce.mmoitems.api.crafting.output;

import io.lumine.mythic.bukkit.MythicBukkit;
import io.lumine.mythic.lib.util.configobject.ConfigObject;
import net.Indyuce.mmoitems.api.player.RPGPlayer;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public class MythicRecipeOutput extends RecipeOutput {
    private final String id;

    public MythicRecipeOutput(ConfigObject config) {
        super(config);

        id = config.getString("id");
    }

    @Override
    public ItemStack generateOutput(@NotNull RPGPlayer rpg) {
        return MythicBukkit.inst().getItemManager().getItemStack(id);
    }

    @Override
    public ItemStack getPreview() {
        return MythicBukkit.inst().getItemManager().getItemStack(id);
    }
}
