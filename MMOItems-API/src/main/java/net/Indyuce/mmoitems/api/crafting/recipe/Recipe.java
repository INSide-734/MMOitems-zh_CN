package net.Indyuce.mmoitems.api.crafting.recipe;

import io.lumine.mythic.lib.api.MMOLineConfig;
import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.api.crafting.CraftingStation;
import net.Indyuce.mmoitems.api.crafting.condition.Condition;
import net.Indyuce.mmoitems.api.crafting.ingredient.Ingredient;
import net.Indyuce.mmoitems.api.crafting.ingredient.inventory.IngredientInventory;
import net.Indyuce.mmoitems.api.crafting.trigger.Trigger;
import net.Indyuce.mmoitems.api.player.PlayerData;
import org.apache.commons.lang.Validate;
import org.bukkit.configuration.ConfigurationSection;

import java.util.*;

@SuppressWarnings("unused")
public abstract class Recipe {
    private final String id;
    private final List<String> lore;
    private final Map<RecipeOption, Boolean> options = new HashMap<>();

    private final List<Ingredient> ingredients = new ArrayList<>();
    private final List<Condition> conditions = new ArrayList<>();

    /**
     * - <code>onUse</code> are called when USING the recipe (not claiming the item from the queue)
     * - <code>onClaim</code> are called when claiming the item in the crafting queue
     * - <code>onCancel</> are called when canceling the craft in the crafting queue
     * <p>
     * For backwards compatibility, <code>onClaim</code> corresponds to the trigger list
     * stored with the 'triggers' config key
     */
    private final List<Trigger> onUse = new ArrayList<>(), onClaim = new ArrayList<>(), onCancel = new ArrayList<>();

    /**
     * Stores the information about a specific recipe setup in a crafting
     * station. When a player opens a crafting station GUI, a CheckedRecipe
     * instance is created to evaluate the conditions which are not met/the
     * ingredients he is missing.
     *
     * @param config Config section to load data from
     */
    public Recipe(ConfigurationSection config) {
        this.id = Objects.requireNonNull(config.getName(), "Recipe ID cannot be null");

        // Load recipe options
        if (config.contains("options"))
            for (RecipeOption option : RecipeOption.values())
                if (config.getConfigurationSection("options").contains(option.getConfigPath()))
                    options.put(option, config.getBoolean("options." + option.getConfigPath()));

        lore = config.getStringList("lore");

        // Load ingredients
        for (String format : config.getStringList("ingredients"))
            try {
                Ingredient ingredient = MMOItems.plugin.getCrafting().getIngredient(new MMOLineConfig(format));
                ingredients.add(ingredient);
            } catch (IllegalArgumentException exception) {
                throw new IllegalArgumentException("Could not load ingredient '" + format + "': " + exception.getMessage());
            }

        // Load conditions
        for (String format : config.getStringList("conditions"))
            try {
                Condition condition = MMOItems.plugin.getCrafting().getCondition(new MMOLineConfig(format));
                conditions.add(condition);
            } catch (IllegalArgumentException exception) {
                throw new IllegalArgumentException("Could not load condition '" + format + "': " + exception.getMessage());
            }

        if (conditions.isEmpty() && ingredients.isEmpty()) {
            throw new IllegalArgumentException("No conditions or ingredients set.");
        }

        // Load triggers
        loadTriggerList(config.getStringList("on-use"), "on-use", onUse);
        loadTriggerList(config.getStringList("triggers"), "on-claim", onClaim);
        loadTriggerList(config.getStringList("on-cancel"), "on-cancel", onCancel);
    }

    private void loadTriggerList(List<String> stringList, String triggerType, List<Trigger> collection) {
        for (String format : stringList)
            try {
                Trigger trigger = MMOItems.plugin.getCrafting().getTrigger(new MMOLineConfig(format));
                Validate.notNull(trigger, "Could not match trigger");
                collection.add(trigger);
            } catch (IllegalArgumentException exception) {
                throw new IllegalArgumentException("Could not load " + triggerType + " trigger '" + format + "': " + exception.getMessage());
            }
    }

    public String getId() {
        return id;
    }

    public List<String> getLore() {
        return lore;
    }

    public List<Ingredient> getIngredients() {
        return ingredients;
    }

    public List<Condition> getConditions() {
        return conditions;
    }

    public List<Trigger> whenUsed() {
        return onUse;
    }

    public List<Trigger> whenClaimed() {
        return onClaim;
    }

    public List<Trigger> whenCanceled() {
        return onCancel;
    }

    public Condition getCondition(String format) {
        for (Condition condition : conditions)
            if (condition.getId().equals(format))
                return condition;
        return null;
    }

    public boolean hasOption(RecipeOption option) {
        return options.getOrDefault(option, option.getDefault());
    }

    public void addIngredient(Ingredient ingredient) {
        ingredients.add(ingredient);
    }

    public void registerCondition(Condition condition) {
        conditions.add(condition);
    }

    public void setOption(RecipeOption option, boolean value) {
        options.put(option, value);
    }

    /**
     * THE method that checks if a player can use a recipe or not. This checks for
     * conditions and ingredients and saves all the info needed to display everything
     * in the item lore.
     *
     * @param data Player trying to use the recipe
     * @param inv  The ingredients of the player
     * @return Class that knows if the player can use the recipe
     */
    public abstract CheckedRecipe evaluateRecipe(PlayerData data, IngredientInventory inv);

    /**
     * Called when all the recipe conditions are to true and when the player
     * eventually starts crafting OR when the player claims the item in the
     * crafting queue once the delay is over.
     * <p>
     * This however checks for the bukkit event which can be cancelled; if
     * this method returns false the ingredients shall not be consumed.
     *
     * @param data    Player crafting the item
     * @param inv     The player's ingredients
     * @param recipe  The recipe used to craft the item
     * @param station The station used to craft the item
     * @return If the crafting recipe was successfully used
     */
    public abstract boolean whenUsed(PlayerData data, IngredientInventory inv, CheckedRecipe recipe, CraftingStation station);

    /**
     * Applies extra conditions when a player has just clicked on a recipe item
     * in the GUI. This method is called after checking for the recipe
     * conditions and ingredients.
     *
     * @param data    The player crafting the item
     * @param inv     The player's ingredients
     * @param recipe  The recipe used to craft the item
     * @param station The station used to craft the item
     * @return If the player can use the recipe
     */
    public abstract boolean canUse(PlayerData data, IngredientInventory inv, CheckedRecipe recipe, CraftingStation station);

    @Override
    public boolean equals(Object obj) {
        return obj instanceof Recipe && ((Recipe) obj).id.equals(id);
    }

    public static enum RecipeOption {

        /**
         * Hide the crafting recipe when one of the condition is not met
         */
        HIDE_WHEN_LOCKED(false),

        /**
         * Hide the crafting recipe when the player does not have all the
         * ingredients
         */
        HIDE_WHEN_NO_INGREDIENTS(false),

        /**
         * If set to false (default is true), no output item will be given to
         * the player crafting the item. That option is made to have recipes
         * which entirely rely on triggers.
         */
        OUTPUT_ITEM(true),

        /**
         * Disables crafting sound
         */
        SILENT_CRAFT(false);

        private final boolean def;

        RecipeOption(boolean def) {
            this.def = def;
        }

        public boolean getDefault() {
            return def;
        }

        public String getConfigPath() {
            return name().toLowerCase().replace("_", "-");
        }
    }
}
