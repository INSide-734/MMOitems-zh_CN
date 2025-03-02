package net.Indyuce.mmoitems.api.crafting.recipe;

import io.lumine.mythic.lib.api.crafting.uimanager.ProvidedUIFilter;
import io.lumine.mythic.lib.api.crafting.uimanager.UIFilterManager;
import io.lumine.mythic.lib.api.util.SmartGive;
import io.lumine.mythic.lib.api.util.ui.FriendlyFeedbackCategory;
import io.lumine.mythic.lib.api.util.ui.FriendlyFeedbackMessage;
import io.lumine.mythic.lib.api.util.ui.FriendlyFeedbackProvider;
import io.lumine.mythic.lib.api.util.ui.SilentNumbers;
import io.lumine.mythic.lib.version.Sounds;
import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.api.crafting.CraftingStation;
import net.Indyuce.mmoitems.api.crafting.CraftingStatus.CraftingQueue;
import net.Indyuce.mmoitems.api.crafting.ingredient.inventory.IngredientInventory;
import net.Indyuce.mmoitems.api.crafting.output.RecipeOutput;
import net.Indyuce.mmoitems.api.event.PlayerUseCraftingStationEvent;
import net.Indyuce.mmoitems.api.player.PlayerData;
import net.Indyuce.mmoitems.api.player.RPGPlayer;
import net.Indyuce.mmoitems.api.util.message.FFPMMOItems;
import net.Indyuce.mmoitems.api.util.message.Message;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public class CraftingRecipe extends Recipe {
    @Nullable
    private ProvidedUIFilter legacyUiFilter;
    private final RecipeOutput output;

    /**
     * There can't be any crafting time for upgrading recipes since there is no
     * way to save an MMOItem in the config file TODO save as ItemStack
     */
    private final long craftingTime;

    public CraftingRecipe(@NotNull ConfigurationSection config) throws IllegalArgumentException {
        super(config);

        craftingTime = (long) config.getDouble("crafting-time") * 1000;

        // [BACKWARDS COMPATIBLE] Legacy MythicLib UI Filters
        String uiFilter = config.getString("output.item");
        if (uiFilter != null) {
            // Generate filter
            FriendlyFeedbackProvider ffp = new FriendlyFeedbackProvider(FFPMMOItems.get());
            ProvidedUIFilter sweetOutput = UIFilterManager.getUIFilter(uiFilter, ffp);

            // Is it null?
            if (sweetOutput == null || !sweetOutput.isValid(ffp) || sweetOutput.getItemStack(null) == null) {

                // Throw message
                throw new IllegalArgumentException(SilentNumbers.collapseList(SilentNumbers.transcribeList(ffp.getFeedbackOf(FriendlyFeedbackCategory.ERROR), message -> {
                    if (message instanceof FriendlyFeedbackMessage) {
                        return ((FriendlyFeedbackMessage) message).forConsole(FFPMMOItems.get());
                    }
                    return "";
                }), ""));
            }

            // Accept
            legacyUiFilter = sweetOutput;
            output = null;
            return;
        }

        Object outputObject = Objects.requireNonNull(config.get("output"), "Could not find recipe output");
        this.output = MMOItems.plugin.getCrafting().getRecipeOutput(outputObject);
    }

    public long getCraftingTime() {
        return craftingTime;
    }

    public boolean isInstant() {
        return craftingTime <= 0;
    }

    /**
     * @return The output ItemStack from this
     */
    @SuppressWarnings("ConstantConditions")
    @NotNull
    public ItemStack getOutputItemStack(@Nullable RPGPlayer rpg) {

        // [Backwards Compatibility] UI Filters
        if (legacyUiFilter != null) {
            return legacyUiFilter.getItemStack(null);
        }

        return output.getOutput(rpg);
    }

    /**
     * @return The preview ItemStack from this
     */
    @NotNull
    public ItemStack getPreviewItemStack() {

        // [Backwards Compatibility] UI Filters
        if (legacyUiFilter != null) {
            // Generate from ProvidedUIFilter, guaranteed to not be null don't listen to the inspection.
            //return output.getParent().getDisplayStack(output.getArgument(), output.getData(), null);
            //return output.getDisplayStack(null);
            ItemStack gen = legacyUiFilter.getParent().getDisplayStack(legacyUiFilter.getArgument(), legacyUiFilter.getData(), null);
            gen.setAmount(legacyUiFilter.getAmount(1));
            ItemMeta itemMeta = gen.getItemMeta();
            if (itemMeta != null) {
                itemMeta.setDisplayName(SilentNumbers.getItemName(gen, false) + "\u00a7\u02ab");
                gen.setItemMeta(itemMeta);
            }
            return gen;
        }

        return output.getPreview();
    }

    public int getOutputAmount() {

        // [Backwards Compatibility] UI Filters
        if (legacyUiFilter != null) {
            return legacyUiFilter.getAmount(1);
        }

        return output.getAmount();
    }

    @Override
    public boolean whenUsed(PlayerData data, IngredientInventory inv, CheckedRecipe recipe, CraftingStation station) {
        if (!data.isOnline()) return false;

        /*
         * If the recipe is instant, take the ingredients off
         * and directly add the output to the player's inventory
         */
        if (isInstant()) {
            ItemStack result = hasOption(RecipeOption.OUTPUT_ITEM) ? getOutputItemStack(data.getRPG()) : null;
            PlayerUseCraftingStationEvent event = new PlayerUseCraftingStationEvent(data, station, recipe, result);
            Bukkit.getPluginManager().callEvent(event);
            if (event.isCancelled())
                return false;

            /*
             * Since instant recipes bypass the crafting queue MI still needs
             * to apply the trigger list when using an instant recipe
             */
            recipe.getRecipe().whenClaimed().forEach(trigger -> trigger.whenCrafting(data));

            if (result != null)
                new SmartGive(data.getPlayer()).give(result);

            // Play sound
            if (station.getEditableView().craftSound != null && !hasOption(RecipeOption.SILENT_CRAFT)) {
                data.getPlayer().playSound(data.getPlayer(), station.getEditableView().craftSound, 1, 1);
            }

            // Recipe was successfully used
            return true;

            /*
             * If the recipe is not instant, add the item to the crafting queue
             */
        }

        PlayerUseCraftingStationEvent called = new PlayerUseCraftingStationEvent(data, station, recipe);
        Bukkit.getPluginManager().callEvent(called);
        if (called.isCancelled())
            return false;

        // Play sound
        if (station.getEditableView().queueAddSound != null && !hasOption(RecipeOption.SILENT_CRAFT)) {
            data.getPlayer().playSound(data.getPlayer(), station.getEditableView().queueAddSound, 1, 1);
        }

        data.getCrafting().getQueue(station).add(this);

        // Recipe was successfully used
        return true;
    }

    @Override
    public boolean canUse(PlayerData data, IngredientInventory inv, CheckedRecipe recipe, CraftingStation station) {
        if (isInstant())
            return true;

        CraftingQueue queue = data.getCrafting().getQueue(station);
        if (queue.isFull(station)) {
            if (!data.isOnline())
                return false;

            Message.CRAFTING_QUEUE_FULL.format(ChatColor.RED).send(data.getPlayer());
            data.getPlayer().playSound(data.getPlayer().getLocation(), Sounds.ENTITY_VILLAGER_NO, 1, 1);
            return false;
        }
        return true;
    }

    @Override
    public CheckedRecipe evaluateRecipe(PlayerData data, IngredientInventory inv) {
        return new CheckedRecipe(this, data, inv);
    }
}
