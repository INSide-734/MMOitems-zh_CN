package net.Indyuce.mmoitems.gui.edition.recipe.gui;

import io.lumine.mythic.lib.gui.Navigator;
import net.Indyuce.mmoitems.api.item.template.MMOItemTemplate;
import net.Indyuce.mmoitems.gui.edition.recipe.button.RBA_CookingTime;
import net.Indyuce.mmoitems.gui.edition.recipe.button.RBA_Experience;
import net.Indyuce.mmoitems.gui.edition.recipe.button.RBA_HideFromBook;
import net.Indyuce.mmoitems.gui.edition.recipe.interpreter.RMGRI_LegacyBurning;
import net.Indyuce.mmoitems.gui.edition.recipe.interpreter.RMG_RecipeInterpreter;
import net.Indyuce.mmoitems.gui.edition.recipe.registry.RecipeRegistry;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;


/**
 * The legacy recipes that are not supported by MythicLib that all happen to have to do
 * with burning stuff - furnaces, campfires, the other furnaces...
 *
 * @author Gunging
 */
public class RMG_BurningLegacy extends RecipeEditorGUI {

    @NotNull
    HashMap<Integer, Integer> inputLinks = new HashMap<>();

    /**
     * An editor for a Shaped Recipe. Because the recipe is loaded from the YML when this is created,
     * concurrent modifications of the same recipe are unsupported.
     *
     * @param navigator  Current UI navigator
     * @param template   Template of which a recipe is being edited
     * @param recipeName Name of this recipe
     */
    public RMG_BurningLegacy(@NotNull Navigator navigator, @NotNull MMOItemTemplate template, @NotNull String recipeName, @NotNull RecipeRegistry recipeRegistry) {
        super(navigator, template, recipeName, recipeRegistry);
        addButton(new RBA_HideFromBook(this));
        addButton(new RBA_Experience(this));
        addButton(new RBA_CookingTime(this));

        // NO OUTPUT
        if (!isShowingInput()) {
            switchInput();
        }

        // Get section and build interpreter
        interpreter = new RMGRI_LegacyBurning(getNameSection());

        // Bind inputs - Furnace only has which item to smelt
        inputLinks.put(40, 0);
    }

    @Override
    public int getButtonsRow() {
        return 2;
    }

    @Override
    public void putRecipe() {

        // Fill inputs
        for (Integer s : inputLinks.keySet()) {
            inventory.setItem(s, getDisplay(isShowingInput(), inputLinks.get(s)));
        }
    }

    @Override
    int getInputSlot(int absolute) {

        // Not an input? Not our business
        @Nullable Integer found = inputLinks.get(absolute);

        // Found or negative
        return found != null ? found : -1;
    }

    @NotNull
    final RMGRI_LegacyBurning interpreter;

    @NotNull
    @Override
    public RMG_RecipeInterpreter getInterpreter() {
        return interpreter;
    }
}
