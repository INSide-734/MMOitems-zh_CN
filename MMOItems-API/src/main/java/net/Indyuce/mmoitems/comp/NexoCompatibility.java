package net.Indyuce.mmoitems.comp;

import com.nexomc.nexo.api.NexoItems;
import io.lumine.mythic.lib.api.util.AltChar;
import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.api.crafting.ConditionalDisplay;
import net.Indyuce.mmoitems.api.crafting.ingredient.NexoIngredient;
import net.Indyuce.mmoitems.api.crafting.ingredient.inventory.NexoPlayerIngredient;
import net.Indyuce.mmoitems.api.crafting.output.NexoRecipeOutput;

public class NexoCompatibility {
    public NexoCompatibility() {
        MMOItems.plugin.getCrafting().registerIngredient("nexo",
                NexoIngredient::new,
                new ConditionalDisplay("&8" + AltChar.check + " &7#amount# #item#", "&c" + AltChar.cross + " &7#amount# #item#"),
                nbt -> NexoItems.exists(nbt.getItem()),
                NexoPlayerIngredient::new
        );
        MMOItems.plugin.getCrafting().registerOutputType("nexo", NexoRecipeOutput::new);
    }
}
