package net.Indyuce.mmoitems.api.crafting.ingredient.inventory;

import dev.lone.itemsadder.api.CustomStack;
import io.lumine.mythic.lib.api.item.NBTItem;
import org.apache.commons.lang.Validate;

public class ItemsAdderPlayerIngredient extends PlayerIngredient {
    private final String id;

    public ItemsAdderPlayerIngredient(NBTItem item) {
        super(item);

        CustomStack stack = CustomStack.byItemStack(item.getItem());
        Validate.notNull(stack, "Not a custom item");
        id = stack.getId();
    }

    public String getId() {
        return id;
    }
}
