package net.Indyuce.mmoitems.api.crafting.ingredient.inventory;

import io.lumine.mythic.bukkit.MythicBukkit;
import io.lumine.mythic.lib.api.item.NBTItem;

public class MythicPlayerIngredient extends PlayerIngredient {
    private final String id;

    public MythicPlayerIngredient(NBTItem item) {
        super(item);

        this.id = MythicBukkit.inst().getItemManager().getMythicTypeFromItem(item.getItem());
    }

    public String getId() {
        return id;
    }
}
