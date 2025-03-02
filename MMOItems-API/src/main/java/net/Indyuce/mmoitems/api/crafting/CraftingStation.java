package net.Indyuce.mmoitems.api.crafting;

import io.lumine.mythic.lib.UtilityMethods;
import io.lumine.mythic.lib.util.PostLoadAction;
import io.lumine.mythic.lib.util.PreloadedObject;
import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.api.crafting.ingredient.inventory.IngredientInventory;
import net.Indyuce.mmoitems.api.crafting.recipe.CheckedRecipe;
import net.Indyuce.mmoitems.api.crafting.recipe.CraftingRecipe;
import net.Indyuce.mmoitems.api.crafting.recipe.Recipe;
import net.Indyuce.mmoitems.api.crafting.recipe.Recipe.RecipeOption;
import net.Indyuce.mmoitems.api.crafting.recipe.UpgradingRecipe;
import net.Indyuce.mmoitems.api.player.PlayerData;
import org.apache.commons.lang.Validate;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandMap;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.logging.Level;

public class CraftingStation implements PreloadedObject {
    private final String id, name;
    private final int maxQueueSize;
    private final EditableCraftingStationView editableView;
    private final EditableCraftingStationPreview editablePreview;

    /**
     * This map only contains recipes which are specific to that station,
     * it does not contain the recipes of its parent station (if it exists).
     */
    private final Map<String, Recipe> recipes = new LinkedHashMap<>();

    /**
     * A station inherits from the recipes of its parent station.
     */
    private CraftingStation parent;

    private final PostLoadAction postLoadAction = new PostLoadAction(config -> {
        if (!config.contains("parent")) return;

        String id = config.getString("parent").toLowerCase().replace(" ", "-").replace("_", "-");
        Validate.isTrue(!id.equals(CraftingStation.this.id), "Station cannot use itself as parent");
        Validate.isTrue(MMOItems.plugin.getCrafting().hasStation(id), "Could not find parent station with ID '" + id + "'");
        parent = MMOItems.plugin.getCrafting().getStation(id);
    });

    public CraftingStation(@NotNull String id, @NotNull FileConfiguration config) {
        postLoadAction.cacheConfig(config);

        this.id = id.toLowerCase().replace("_", "-").replace(" ", "-");
        this.name = config.getString("name", "A Station With No Name");
        this.editableView = new EditableCraftingStationView(this);
        this.editablePreview = new EditableCraftingStationPreview(this);

        // Setup command if required
        // A reload is required to flush older commands
        if (config.contains("command"))
            setupCommand(config.getConfigurationSection("command"));

        editableView.reload(MMOItems.plugin, config.getConfigurationSection("gui-layout"));
        if (config.isConfigurationSection("preview-gui-layout")) // Confirm GUI is now optional
            editablePreview.reload(MMOItems.plugin, config.getConfigurationSection("preview-gui-layout"));

        for (String key : config.getConfigurationSection("recipes").getKeys(false))
            try {
                registerRecipe(loadRecipe(config.getConfigurationSection("recipes." + key)));
            } catch (RuntimeException exception) {
                MMOItems.plugin.getLogger().log(Level.SEVERE,
                        "Could not load recipe '" + key + "' from crafting station '" + id + "': " + exception.getMessage());
            }

        maxQueueSize = Math.max(1, Math.min(config.getInt("max-queue-size"), 64));
    }

    public CraftingStation(String id, String name,
                           int maxQueueSize,
                           CraftingStation parent,
                           EditableCraftingStationView editableView,
                           EditableCraftingStationPreview editablePreview) {
        Validate.notNull(id, "Crafting station ID must not be null");
        Validate.notNull(name, "Crafting station name must not be null");

        this.id = id.toLowerCase().replace("_", "-").replace(" ", "-");
        this.name = name;
        this.maxQueueSize = maxQueueSize;
        this.parent = parent;
        this.editableView = editableView;
        this.editablePreview = editablePreview;
    }

    @NotNull
    @Override
    public PostLoadAction getPostLoadAction() {
        return postLoadAction;
    }

    public String getId() {
        return id;
    }

    private void setupCommand(ConfigurationSection config) {
        Validate.notNull(config, "Command config is null");

        CommandMap commandMap = Bukkit.getServer().getCommandMap();
        String name = Objects.requireNonNull(config.getString("name"), "Command name not found");

        // Command already exists
        if (commandMap.getCommand(name) != null) return;

        commandMap.register(MMOItems.plugin.getName(), new CraftingStationCommand(this, name, config));
    }

    public EditableCraftingStationView getEditableView() {
        return editableView;
    }

    public EditableCraftingStationPreview getEditablePreview() {
        return editablePreview;
    }

    public String getName() {
        return name;
    }

    @Nullable
    public CraftingStation getParent() {
        return parent;
    }

    /**
     * @return Recursively collects all recipes from that station and from
     *         its parent station.
     */
    @NotNull
    public Collection<Recipe> getRecipes() {
        if (parent == null)
            return recipes.values();

        // Collect recipes from station inheritance tree
        List<Recipe> collected = new ArrayList<>(recipes.values());
        CraftingStation next = parent;
        while (next != null) {
            collected.addAll(next.recipes.values());
            next = next.parent;
        }

        return collected;
    }

    /**
     * @param id Recipe identifier
     * @return Recursively checks if that station has the provided recipe.
     */
    public boolean hasRecipe(String id) {
        return recipes.containsKey(id) || (parent != null && parent.hasRecipe(id));
    }

    /**
     * @param id Recipe identifier
     * @return Recursively finds the corresponding recipe
     */
    @Nullable
    public Recipe getRecipe(String id) {
        Recipe found = recipes.get(id);
        return found == null && parent != null ? parent.getRecipe(id) : found;
    }

    public int getMaxQueueSize() {
        return maxQueueSize;
    }

    public List<CheckedRecipe> getAvailableRecipes(PlayerData data, IngredientInventory inv) {
        List<CheckedRecipe> infos = new ArrayList<>();

        for (Recipe recipe : getRecipes()) {
            CheckedRecipe info = recipe.evaluateRecipe(data, inv);
            if ((info.areConditionsMet() || !info.getRecipe().hasOption(RecipeOption.HIDE_WHEN_LOCKED))
                    && (info.allIngredientsHad() || !info.getRecipe().hasOption(RecipeOption.HIDE_WHEN_NO_INGREDIENTS)))
                infos.add(info);
        }

        return infos;
    }

    /**
     * Keep in mind this method also has the effect of register a recipe
     * inside any crafting station that has the current station as child.
     *
     * @param recipe Recipe being registered
     * @see #hasRecipe(String)
     */
    public void registerRecipe(Recipe recipe) {
        recipes.put(recipe.getId(), recipe);
    }

    @Deprecated
    public int getMaxPage() {
        int recipes = getRecipes().size();
        int perPage = Objects.requireNonNull(editableView.getByFunction("recipe"), "No item with function 'recipe'").getSlots().size();
        return UtilityMethods.getPageNumber(recipes, perPage);
    }

    /*
     * find type of crafting recipe based on section. there is no 'type' recipe
     * parameter because old files would be out of date, instead just looks for
     * a parameter of the crafting recipe which is 'output'
     */
    private Recipe loadRecipe(ConfigurationSection config) throws IllegalArgumentException {
        return config.contains("output") ? new CraftingRecipe(config) : new UpgradingRecipe(config);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CraftingStation that = (CraftingStation) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
