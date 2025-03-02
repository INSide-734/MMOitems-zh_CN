package net.Indyuce.mmoitems.comp;

import dev.lone.itemsadder.api.CustomStack;
import io.lumine.mythic.lib.api.util.AltChar;
import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.api.crafting.ConditionalDisplay;
import net.Indyuce.mmoitems.api.crafting.ingredient.ItemsAdderIngredient;
import net.Indyuce.mmoitems.api.crafting.ingredient.inventory.ItemsAdderPlayerIngredient;
import net.Indyuce.mmoitems.api.crafting.output.ItemsAdderRecipeOutput;

public class ItemsAdderCompatibility {
    public ItemsAdderCompatibility() {
        MMOItems.plugin.getCrafting().registerIngredient(
                "itemsadder",
                ItemsAdderIngredient::new,
                new ConditionalDisplay("&a" + AltChar.check + " &7#amount# #item#", "&c" + AltChar.cross + " &7#amount# #item#"),
                // TODO improve performance
                nbt -> CustomStack.byItemStack(nbt.getItem()) != null,
                ItemsAdderPlayerIngredient::new);
        MMOItems.plugin.getCrafting().registerOutputType("itemsadder", ItemsAdderRecipeOutput::new, "ia", "itemadder", "iadder");
    }
}
