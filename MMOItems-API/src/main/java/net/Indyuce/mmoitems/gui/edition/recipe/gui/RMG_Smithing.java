package net.Indyuce.mmoitems.gui.edition.recipe.gui;

import io.lumine.mythic.lib.gui.Navigator;
import net.Indyuce.mmoitems.api.item.template.MMOItemTemplate;
import net.Indyuce.mmoitems.gui.edition.recipe.button.RBA_DropGems;
import net.Indyuce.mmoitems.gui.edition.recipe.button.RBA_InputOutput;
import net.Indyuce.mmoitems.gui.edition.recipe.button.RBA_SmithingEnchantments;
import net.Indyuce.mmoitems.gui.edition.recipe.button.RBA_SmithingUpgrades;
import net.Indyuce.mmoitems.gui.edition.recipe.interpreter.RMGRI_Smithing;
import net.Indyuce.mmoitems.gui.edition.recipe.interpreter.RMG_RecipeInterpreter;
import net.Indyuce.mmoitems.gui.edition.recipe.registry.RecipeRegistry;
import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;

/**
 * Edits smithing recipes, very nice.
 * <br> <br> <code>
 * - - - - - - - - -  <br>
 * - - - = - - - - -  <br>
 * 0 - 1 = 0 - 1 - R  <br>
 * - - - = - - - - -  </code>
 *
 * @author Gunging
 */
public class RMG_Smithing extends RecipeEditorGUI {

    @NotNull
    final HashMap<Integer, Integer> inputLinks = new HashMap<>();

    /**
     * An editor for a Shaped Recipe. Because the recipe is loaded from the YML when this is created,
     * concurrent modifications of the same recipe are unsupported.
     *
     * @param navigator Current UI navigator
     * @param template Template of which a recipe is being edited
     * @param recipeName Name of this recipe
     */
    public RMG_Smithing(@NotNull Navigator navigator, @NotNull MMOItemTemplate template, @NotNull String recipeName, @NotNull RecipeRegistry recipeRegistry) {
        super(navigator, template, recipeName, recipeRegistry);

        // Get section and build interpreter
        ConfigurationSection crafting = RecipeEditorGUI.getSection(getEditedSection(), "crafting");
        ConfigurationSection recipe = RecipeEditorGUI.getSection(crafting, getRecipeRegistry().getRecipeConfigPath());
        ConfigurationSection name = RecipeEditorGUI.getSection(recipe, getRecipeName());
        interpreter = new RMGRI_Smithing(name);

        // Bind inputs
        inputLinks.put(39, 0);
        inputLinks.put(41, 1);

        // Extra buttons
        addButton(new RBA_InputOutput(this));
        addButton(new RBA_SmithingUpgrades(this));
        addButton(new RBA_SmithingEnchantments(this));
        addButton(new RBA_DropGems(this));
    }


    @Override
    public void putRecipe() {

        // Fill inputs
        for (Integer s : inputLinks.keySet()) { inventory.setItem(s, getDisplay(isShowingInput(), inputLinks.get(s))); }
    }

    @Override
    int getInputSlot(int absolute) {

        // Not an input? Not our business
        @Nullable Integer found = inputLinks.get(absolute);

        // Found or negative
        return found != null ? found : -1;
    }
    @Override public int getButtonsRow() { return 1; }

    @NotNull final RMGRI_Smithing interpreter;
    @NotNull
    @Override
    public RMG_RecipeInterpreter getInterpreter() { return interpreter; }
}
