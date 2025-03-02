package net.Indyuce.mmoitems.api.crafting.condition;

import io.lumine.mythic.lib.api.MMOLineConfig;
import net.Indyuce.mmoitems.api.player.PlayerData;

import java.text.DecimalFormat;

public class StaminaCondition extends Condition {
    private final double amount;
    private final DecimalFormat format;

    public StaminaCondition(MMOLineConfig config) {
        super("stamina");

        config.validate("amount");
        amount = config.getDouble("amount");

        format = new DecimalFormat(config.getString("format", "0.#"));
    }

    @Override
    public boolean isMet(PlayerData data) {
        return data.getRPG().getStamina() >= amount;
    }

    @Override
    public String formatDisplay(String string) {
        return string.replace("#stamina#", format.format(amount));
    }

    @Override
    public void whenCrafting(PlayerData data) {
        data.getRPG().giveStamina(-amount);
    }
}
