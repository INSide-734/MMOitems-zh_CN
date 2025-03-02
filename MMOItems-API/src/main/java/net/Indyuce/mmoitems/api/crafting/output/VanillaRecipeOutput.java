package net.Indyuce.mmoitems.api.crafting.output;

import io.lumine.mythic.lib.util.configobject.ConfigObject;
import net.Indyuce.mmoitems.api.player.RPGPlayer;
import net.Indyuce.mmoitems.util.MMOUtils;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public class VanillaRecipeOutput extends RecipeOutput {
    private final Material material;

    public VanillaRecipeOutput(ConfigObject config) {
        super(config);

        material = MMOUtils.friendlyValueOf(Material::valueOf, config.getString("type"), "Could not find item with type '%s'");
    }

    @Override
    public ItemStack generateOutput(@NotNull RPGPlayer rpg) {
        return new ItemStack(material);
    }

    @Override
    public ItemStack getPreview() {
        return new ItemStack(material);
    }
}
