package net.Indyuce.mmoitems.api.crafting;

import io.lumine.mythic.lib.util.annotation.BackwardsCompatibility;
import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.api.crafting.CraftingStatus.CraftingQueue.QueueItem;
import net.Indyuce.mmoitems.api.crafting.recipe.CraftingRecipe;
import net.Indyuce.mmoitems.api.crafting.recipe.Recipe;
import net.Indyuce.mmoitems.api.player.PlayerData;
import org.apache.commons.lang.Validate;
import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.logging.Level;

public class CraftingStatus {

    /**
     * Saves data about items being constructed in specific stations. players
     * must go back to the station GUI and claim their item once it's ready
     */
    private final Map<CraftingStation, CraftingQueue> queues = new HashMap<>();

    public void load(PlayerData data, ConfigurationSection config) {
        String name = data.isOnline() ? data.getPlayer().getName() : "Unknown Player";

        for (String stationId : config.getKeys(false)) {
            if (!MMOItems.plugin.getCrafting().hasStation(stationId)) {
                MMOItems.plugin.getLogger().log(Level.SEVERE,
                        "An error occurred while trying to load crafting station recipe data of '" + name + "': "
                                + "could not find crafting station with ID '" + stationId
                                + "', make sure you backup that player data file before the user logs off.");
                continue;
            }

            CraftingStation station = MMOItems.plugin.getCrafting().getStation(stationId);
            CraftingQueue queue = new CraftingQueue(station);
            queues.put(station, queue);

            @BackwardsCompatibility(version = "6.10") final Optional<String> legacyOpt = config.getConfigurationSection(stationId).getKeys(false).stream().findFirst();
            final boolean legacyLoading = legacyOpt.isPresent() && config.contains(stationId + "." + legacyOpt.get() + ".delay");

            for (String recipeConfigId : config.getConfigurationSection(stationId).getKeys(false)) {
                String recipeId = config.getString(stationId + "." + recipeConfigId + ".recipe");
                if (recipeId == null || !station.hasRecipe(recipeId)) {
                    MMOItems.plugin.getLogger().log(Level.SEVERE,
                            "An error occurred while trying to load crafting station recipe data of '" + name + "': "
                                    + "could not find recipe with ID '" + recipeId
                                    + "', make sure you backup that player data file before the user logs off.");
                    continue;
                }

                Recipe recipe = station.getRecipe(recipeId);
                if (!(recipe instanceof CraftingRecipe)) {
                    MMOItems.plugin.getLogger().log(Level.SEVERE, "An error occurred while trying to load crafting station recipe data of '"
                            + name + "': " + "recipe '" + recipe.getId() + "' is not a CRAFTING recipe.");
                    continue;
                }

                // Backwards compatibility config loading for MI <6.10
                if (legacyLoading) {
                    final long started = config.getLong(stationId + "." + recipeConfigId + ".started");
                    final long delay = config.getLong(stationId + "." + recipeConfigId + ".delay");

                    queue.add((CraftingRecipe) recipe,
                            started,
                            started + delay);
                    continue;
                }

                queue.add((CraftingRecipe) recipe,
                        config.getLong(stationId + "." + recipeConfigId + ".start"),
                        config.getLong(stationId + "." + recipeConfigId + ".completion"));
            }
        }
    }

    public void save(ConfigurationSection config) {
        queues.forEach((station, queue) -> {
            for (QueueItem craft : queue.getCrafts()) {
                config.set(station.getId() + ".recipe-" + craft.getUniqueId().toString() + ".recipe", craft.getRecipe().getId());
                config.set(station.getId() + ".recipe-" + craft.getUniqueId().toString() + ".start", craft.start);
                config.set(station.getId() + ".recipe-" + craft.getUniqueId().toString() + ".completion", craft.completion);
            }
        });
    }

    public CraftingQueue getQueue(@NotNull CraftingStation station) {
        return queues.computeIfAbsent(station, CraftingQueue::new);
    }

    public static class CraftingQueue {
        private final String station;
        private final List<QueueItem> crafts = new ArrayList<>();

        public CraftingQueue(CraftingStation station) {
            this.station = station.getId();
        }

        public List<QueueItem> getCrafts() {
            return crafts;
        }

        public boolean isFull(CraftingStation station) {
            return crafts.size() >= station.getMaxQueueSize();
        }

        public void remove(QueueItem item) {
            final int index = crafts.indexOf(item);
            Validate.isTrue(index >= 0, "Could not find item in queue");
            crafts.remove(index);

            // Remove time left from subsequent items
            final long gain = Math.min(item.getLeft(), item.getRecipe().getCraftingTime());
            for (int j = index; j < crafts.size(); j++)
                crafts.get(j).removeDelay(gain);
        }

        @Nullable
        public QueueItem getCraft(UUID uuid) {
            for (QueueItem craft : crafts)
                if (craft.getUniqueId().equals(uuid))
                    return craft;
            return null;
        }

        public void add(CraftingRecipe recipe) {
            final long highestCompletion = CraftingQueue.this.crafts.isEmpty() ? System.currentTimeMillis() :
                    Math.max(System.currentTimeMillis(), CraftingQueue.this.crafts.get(CraftingQueue.this.crafts.size() - 1).completion);
            final long itemCompletion = highestCompletion + recipe.getCraftingTime();

            add(recipe, System.currentTimeMillis(), itemCompletion);
        }

        private void add(CraftingRecipe recipe, long start, long completion) {
            crafts.add(new QueueItem(recipe, start, completion));
        }

        public CraftingStation getStation() {
            return MMOItems.plugin.getCrafting().getStation(station);
        }

        public class QueueItem {
            private final String recipe;
            private final UUID uuid = UUID.randomUUID();
            private final long start;

            /**
             * A crafting queue is composed of a series of queue items
             * that each wait on the previous item in the queue for completion.
             * It is much easier to work with the timestamp of completion for
             * any queued item, in order to avoid confusion.
             */
            private long completion;

            public QueueItem(@NotNull CraftingRecipe recipe, long start, long completion) {
                this.recipe = recipe.getId();
                this.start = start;
                this.completion = completion;
            }

            public UUID getUniqueId() {
                return uuid;
            }

            /**
             * /mi reload stations force MI to save the recipe
             * IDs instead of a direct reference to the crafting recipe
             */
            public CraftingRecipe getRecipe() {
                return (CraftingRecipe) getStation().getRecipe(recipe);
            }

            public boolean isReady() {
                return getLeft() == 0;
            }

            public void removeDelay(long amount) {
                this.completion -= amount;
            }

            public long getElapsed() {
                return Math.max(getRecipe().getCraftingTime(), System.currentTimeMillis() - start);
            }

            public long getLeft() {
                return Math.max(0, completion - System.currentTimeMillis());
            }

            @Override
            public boolean equals(Object o) {
                if (this == o) return true;
                if (o == null || getClass() != o.getClass()) return false;
                QueueItem that = (QueueItem) o;
                return Objects.equals(uuid, that.uuid);
            }

            @Override
            public int hashCode() {
                return Objects.hash(uuid);
            }
        }
    }
}
