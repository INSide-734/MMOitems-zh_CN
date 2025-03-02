package net.Indyuce.mmoitems.gui.edition.recipe.gui;

import io.lumine.mythic.lib.gui.Navigator;
import net.Indyuce.mmoitems.api.item.template.MMOItemTemplate;
import net.Indyuce.mmoitems.gui.edition.recipe.button.RBA_InputOutput;
import net.Indyuce.mmoitems.gui.edition.recipe.interpreter.RMGRI_MegaShaped;
import net.Indyuce.mmoitems.gui.edition.recipe.interpreter.RMG_RecipeInterpreter;
import net.Indyuce.mmoitems.gui.edition.recipe.registry.RecipeRegistry;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;

/**
 * Edits super shaped recipes, very nice.
 *
 * @author Gunging
 */
public class RMG_MegaShaped extends RecipeEditorGUI {

    @NotNull
    HashMap<Integer, Integer> inputLinks = new HashMap<>();

    /**
     * An editor for a Super Shaped Recipe. Because the recipe is loaded from the YML when this is created,
     * concurrent modifications of the same recipe are unsupported.
     *
     * @param navigator  Current UI navigator
     * @param template   Template of which a recipe is being edited
     * @param recipeName Name of this recipe
     */
    public RMG_MegaShaped(@NotNull Navigator navigator, @NotNull MMOItemTemplate template, @NotNull String recipeName, @NotNull RecipeRegistry recipeRegistry) {
        super(navigator, template, recipeName, recipeRegistry);
        addButton(new RBA_InputOutput(this));

        // Get section and build interpreter
        interpreter = new RMGRI_MegaShaped(getNameSection());

        // Bind inputs
        inputLinks.put(1, 0);
        inputLinks.put(2, 1);
        inputLinks.put(3, 2);
        inputLinks.put(4, 3);
        inputLinks.put(5, 4);
        inputLinks.put(6, 5);

        inputLinks.put(10, 6);
        inputLinks.put(11, 7);
        inputLinks.put(12, 8);
        inputLinks.put(13, 9);
        inputLinks.put(14, 10);
        inputLinks.put(15, 11);

        inputLinks.put(19, 12);
        inputLinks.put(20, 13);
        inputLinks.put(21, 14);
        inputLinks.put(22, 15);
        inputLinks.put(23, 16);
        inputLinks.put(24, 17);

        inputLinks.put(28, 18);
        inputLinks.put(29, 19);
        inputLinks.put(30, 20);
        inputLinks.put(31, 21);
        inputLinks.put(32, 22);
        inputLinks.put(33, 23);

        inputLinks.put(37, 24);
        inputLinks.put(38, 25);
        inputLinks.put(39, 26);
        inputLinks.put(40, 27);
        inputLinks.put(41, 28);
        inputLinks.put(42, 29);

        inputLinks.put(46, 30);
        inputLinks.put(47, 31);
        inputLinks.put(48, 32);
        inputLinks.put(49, 33);
        inputLinks.put(50, 34);
        inputLinks.put(51, 35);
    }

    @Override
    public int getButtonsRow() {
        return -1;
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
    final RMGRI_MegaShaped interpreter;

    @NotNull
    @Override
    public RMG_RecipeInterpreter getInterpreter() {
        return interpreter;
    }
}
