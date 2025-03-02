package net.Indyuce.mmoitems.api.crafting.ingredient.inventory;

import io.lumine.mythic.lib.api.item.NBTItem;
import io.th0rgal.oraxen.api.OraxenItems;
import org.apache.commons.lang.Validate;

public class OraxenPlayerIngredient extends PlayerIngredient {
    private final String id;

    public OraxenPlayerIngredient(NBTItem item) {
        super(item);

        String id = OraxenItems.getIdByItem(item.getItem());
        Validate.notNull(id, "Not an Oraxen item");
        this.id = id;
    }

    public String getId() {
        return id;
    }
}
