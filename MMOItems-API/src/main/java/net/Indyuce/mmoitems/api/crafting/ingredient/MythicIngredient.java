package net.Indyuce.mmoitems.api.crafting.ingredient;

import io.lumine.mythic.bukkit.MythicBukkit;
import io.lumine.mythic.lib.UtilityMethods;
import io.lumine.mythic.lib.api.MMOLineConfig;
import net.Indyuce.mmoitems.api.crafting.ingredient.inventory.MythicPlayerIngredient;
import net.Indyuce.mmoitems.api.player.RPGPlayer;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;

public class MythicIngredient extends Ingredient<MythicPlayerIngredient> {
    private final String id;
    private final String display;

    public MythicIngredient(MMOLineConfig config) {
        super("mythic", config);

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
    public boolean matches(MythicPlayerIngredient playerIngredient) {
        return playerIngredient.getId().equals(id);
    }

    @NotNull
    @Override
    public ItemStack generateItemStack(@NotNull RPGPlayer player, boolean forDisplay) {
        ItemStack generated = MythicBukkit.inst().getItemManager().getItemStack(id);
        generated.setAmount(getAmount());
        return generated;
    }

    @Override
    public String toString() {
        return "MythicIngredient{" +
                "id='" + id + '\'' +
                '}';
    }

    private String findName() {

        // Try generating the item and getting the display name.
        ItemStack tryGenerate = MythicBukkit.inst().getItemManager().getItemStack(id);
        if (tryGenerate != null) {

            // Try to retrieve display name
            if (tryGenerate.hasItemMeta()) {
                ItemMeta meta = tryGenerate.getItemMeta();
                if (meta.hasDisplayName())
                    return meta.getDisplayName();
            }

            // Use material to generate name
            return UtilityMethods.caseOnWords(tryGenerate.getType().name().toLowerCase().replace("_", " "));
        }

        return "Unknown Item";
    }
}
