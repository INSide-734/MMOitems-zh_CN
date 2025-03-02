package net.Indyuce.mmoitems.gui.edition.recipe.gui;

import io.lumine.mythic.lib.gui.Navigator;
import net.Indyuce.mmoitems.api.item.template.MMOItemTemplate;
import net.Indyuce.mmoitems.gui.edition.recipe.button.RBA_HideFromBook;
import net.Indyuce.mmoitems.gui.edition.recipe.button.RBA_InputOutput;
import net.Indyuce.mmoitems.gui.edition.recipe.interpreter.RMGRI_Shaped;
import net.Indyuce.mmoitems.gui.edition.recipe.interpreter.RMG_RecipeInterpreter;
import net.Indyuce.mmoitems.gui.edition.recipe.registry.RecipeRegistry;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;

/**
 * Edits shaped recipes, very nice.
 *
 * @author Gunging
 */
public class RMG_Shaped extends RecipeEditorGUI {

    @NotNull HashMap<Integer, Integer> inputLinks = new HashMap<>();

    /**
     * An editor for a Shaped Recipe. Because the recipe is loaded from the YML when this is created,
     * concurrent modifications of the same recipe are unsupported.
     *
     * @param navigator  Current UI navigator
     * @param template   Template of which a recipe is being edited
     * @param recipeName Name of this recipe
     */
    public RMG_Shaped(@NotNull Navigator navigator, @NotNull MMOItemTemplate template, @NotNull String recipeName, @NotNull RecipeRegistry recipeRegistry) {
        super(navigator, template, recipeName, recipeRegistry);
        addButton(new RBA_InputOutput(this));
        addButton(new RBA_HideFromBook(this));

        // Get section and build interpreter
        interpreter = new RMGRI_Shaped(getNameSection());

        // Bind inputs
        inputLinks.put(30, 0);
        inputLinks.put(31, 1);
        inputLinks.put(32, 2);

        inputLinks.put(39, 3);
        inputLinks.put(40, 4);
        inputLinks.put(41, 5);

        inputLinks.put(48, 6);
        inputLinks.put(49, 7);
        inputLinks.put(50, 8);
    }

    @Override
    public int getButtonsRow() {
        return 1;
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
    final RMGRI_Shaped interpreter;

    @NotNull
    @Override
    public RMG_RecipeInterpreter getInterpreter() {
        return interpreter;
    }
}
