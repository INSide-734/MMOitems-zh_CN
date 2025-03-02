package net.Indyuce.mmoitems.api.crafting.output;

import io.lumine.mythic.lib.util.configobject.ConfigObject;
import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.api.Type;
import net.Indyuce.mmoitems.api.item.build.MMOItemBuilder;
import net.Indyuce.mmoitems.api.item.template.MMOItemTemplate;
import net.Indyuce.mmoitems.api.player.RPGPlayer;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public class MMOItemRecipeOutput extends RecipeOutput {
    private final MMOItemTemplate template;
    private final boolean ontoPlayer;

    public MMOItemRecipeOutput(ConfigObject config) {
        super(config);

        config.validateKeys("type", "id");
        Type type = MMOItems.plugin.getTypes().getOrThrow(config.getString("type").toUpperCase().replace("-", "_").replace(" ", "_"));
        template = MMOItems.plugin.getTemplates().getTemplateOrThrow(type, config.getString("id"));
        ontoPlayer = config.getBoolean("player", false);
    }

    @Override
    public ItemStack generateOutput(@NotNull RPGPlayer rpg) {
        MMOItemBuilder builder = ontoPlayer ? template.newBuilder(rpg) : template.newBuilder();
        return builder.build().newBuilder().build();
    }

    @Override
    public ItemStack getPreview() {
        return template.newBuilder().build().newBuilder().build();
    }
}
