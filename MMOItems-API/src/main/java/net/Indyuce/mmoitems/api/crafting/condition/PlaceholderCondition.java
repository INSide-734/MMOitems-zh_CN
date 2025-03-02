package net.Indyuce.mmoitems.api.crafting.condition;

import io.lumine.mythic.lib.api.MMOLineConfig;
import me.clip.placeholderapi.PlaceholderAPI;
import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.api.player.PlayerData;
import org.apache.commons.lang.Validate;

import java.util.logging.Level;

public class PlaceholderCondition extends GenericCondition {
    private final String expression1, comparator, expression2;

    public PlaceholderCondition(MMOLineConfig config) {
        super("placeholder", config);

        config.validateKeys("placeholder");
        String[] array = config.getString("placeholder").split("~");
        Validate.isTrue(array.length == 3, "Please use exactly twice ~");
        expression1 = array[0];
        comparator = array[1];
        expression2 = array[2];
    }

    private static final double EQUALITY_THRESHOLD = Math.pow(10, -5);

    @Override
    public boolean isMet(PlayerData data) {
        final String unparsed1 = PlaceholderAPI.setPlaceholders(data.getPlayer(), expression1);
        final String unparsed2 = PlaceholderAPI.setPlaceholders(data.getPlayer(), expression2);
        try {
            switch (comparator) {
                case "<":
                    return Double.parseDouble(unparsed1) < Double.parseDouble(unparsed2);
                case "<=":
                    return Double.parseDouble(unparsed1) <= Double.parseDouble(unparsed2);
                case ">":
                    return Double.parseDouble(unparsed1) > Double.parseDouble(unparsed2);
                case ">=":
                    return Double.parseDouble(unparsed1) >= Double.parseDouble(unparsed2);
                case "==":
                case "=":
                    return Math.abs(Double.parseDouble(unparsed1) - Double.parseDouble(unparsed2)) <= EQUALITY_THRESHOLD;
                case "!=":
                    return Math.abs(Double.parseDouble(unparsed1) - Double.parseDouble(unparsed2)) > EQUALITY_THRESHOLD;
                case "equals":
                case "eq":
                    return unparsed1.equals(unparsed2);
                case "neq":
                    return !unparsed1.equals(unparsed2);
                default:
                    throw new RuntimeException("Comparator not recognized");
            }
        } catch (RuntimeException exception) {
            MMOItems.plugin.getLogger().log(Level.SEVERE, "Could not evaluate placeholder condition expression: " + exception.getMessage());
            return false;
        }
    }

    @Override
    public void whenCrafting(PlayerData data) {
    }
}
