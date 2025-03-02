package net.Indyuce.mmoitems.gui.edition.recipe.gui;

import io.lumine.mythic.lib.gui.Navigator;
import net.Indyuce.mmoitems.api.item.template.MMOItemTemplate;
import net.Indyuce.mmoitems.gui.edition.recipe.interpreter.RMGRI_SuperShaped;
import net.Indyuce.mmoitems.gui.edition.recipe.interpreter.RMG_RecipeInterpreter;
import net.Indyuce.mmoitems.gui.edition.recipe.button.RBA_InputOutput;
import net.Indyuce.mmoitems.gui.edition.recipe.registry.RecipeRegistry;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;

/**
 * Edits super shaped recipes, very nice.
 *
 * @author Gunging
 */
public class RMG_SuperShaped extends RecipeEditorGUI {

    @NotNull HashMap<Integer, Integer> inputLinks = new HashMap<>();

    /**
     * An editor for a Super Shaped Recipe. Because the recipe is loaded from the YML when this is created,
     * concurrent modifications of the same recipe are unsupported.
     *
     * @param navigator  Current UI navigator
     * @param template   Template of which a recipe is being edited
     * @param recipeName Name of this recipe
     */
    public RMG_SuperShaped(@NotNull Navigator navigator, @NotNull MMOItemTemplate template, @NotNull String recipeName, @NotNull RecipeRegistry recipeRegistry) {
        super(navigator, template, recipeName, recipeRegistry);
        addButton(new RBA_InputOutput(this));

        // Get section and build interpreter
        interpreter = new RMGRI_SuperShaped(getNameSection());

        // Bind inputs
        inputLinks.put(11, 0);
        inputLinks.put(12, 1);
        inputLinks.put(13, 2);
        inputLinks.put(14, 3);
        inputLinks.put(15, 4);

        inputLinks.put(20, 5);
        inputLinks.put(21, 6);
        inputLinks.put(22, 7);
        inputLinks.put(23, 8);
        inputLinks.put(24, 9);

        inputLinks.put(29, 10);
        inputLinks.put(30, 11);
        inputLinks.put(31, 12);
        inputLinks.put(32, 13);
        inputLinks.put(33, 14);

        inputLinks.put(38, 15);
        inputLinks.put(39, 16);
        inputLinks.put(40, 17);
        inputLinks.put(41, 18);
        inputLinks.put(42, 19);

        inputLinks.put(47, 20);
        inputLinks.put(48, 21);
        inputLinks.put(49, 22);
        inputLinks.put(50, 23);
        inputLinks.put(51, 24);
    }

    @Override public int getButtonsRow() { return 0; }

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

    @NotNull final RMGRI_SuperShaped interpreter;
    @NotNull
    @Override
    public RMG_RecipeInterpreter getInterpreter() { return interpreter; }
}
