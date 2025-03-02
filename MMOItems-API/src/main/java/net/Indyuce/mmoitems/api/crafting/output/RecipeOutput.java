package net.Indyuce.mmoitems.api.crafting.output;

import io.lumine.mythic.lib.util.configobject.ConfigObject;
import net.Indyuce.mmoitems.api.player.RPGPlayer;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

/**
 * TODO merge with ingredients or item types within MythicLib..... not a fan
 *
 * @author jules
 */
public abstract class RecipeOutput {
    private final int amount;

    public RecipeOutput(ConfigObject object) {
        amount = object.contains("amount") ? object.getInt("amount") : 1;
    }

    public ItemStack getOutput(@NotNull RPGPlayer rpg) {
        ItemStack stack = generateOutput(rpg);
        stack.setAmount(amount);
        return stack;
    }

    public int getAmount() {
        return amount;
    }

    protected abstract ItemStack generateOutput(@NotNull RPGPlayer rpg);

    public abstract ItemStack getPreview();
}
