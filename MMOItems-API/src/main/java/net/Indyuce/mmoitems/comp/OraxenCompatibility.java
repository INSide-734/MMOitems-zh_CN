package net.Indyuce.mmoitems.comp;

import io.lumine.mythic.lib.api.util.AltChar;
import io.th0rgal.oraxen.api.OraxenItems;
import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.api.crafting.ConditionalDisplay;
import net.Indyuce.mmoitems.api.crafting.ingredient.OraxenIngredient;
import net.Indyuce.mmoitems.api.crafting.ingredient.inventory.OraxenPlayerIngredient;
import net.Indyuce.mmoitems.api.crafting.output.OraxenRecipeOutput;

public class OraxenCompatibility {
    public OraxenCompatibility() {
        MMOItems.plugin.getCrafting().registerIngredient("oraxen",
                OraxenIngredient::new,
                new ConditionalDisplay("&a" + AltChar.check + " &7#amount# #item#", "&c" + AltChar.cross + " &7#amount# #item#"),
                nbt -> OraxenItems.getIdByItem(nbt.getItem()) != null,
                OraxenPlayerIngredient::new
        );
        MMOItems.plugin.getCrafting().registerOutputType("oraxen", OraxenRecipeOutput::new);
    }
}
