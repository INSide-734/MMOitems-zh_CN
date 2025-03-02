package net.Indyuce.mmoitems.command.mmoitems.item;

import io.lumine.mythic.lib.command.api.CommandTreeNode;
import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.api.interaction.util.DurabilityItem;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class RepairCommandTreeNode extends CommandTreeNode {
    public RepairCommandTreeNode(CommandTreeNode parent) {
        super(parent, "repair");
    }

    @Override
    public CommandResult execute(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "This command is only for players.");
            return CommandResult.FAILURE;
        }

        Player player = (Player) sender;

        // Mainhand priority
        ItemStack stack = player.getInventory().getItemInMainHand();
        // Try offhand if mainhand is empty
        if (stack == null || stack.getType() == Material.AIR) stack = player.getInventory().getItemInOffHand();

        DurabilityItem durItem = DurabilityItem.from(player, stack);
        if (durItem == null) {
            sender.sendMessage(MMOItems.plugin.getPrefix() + "The item you are holding can't be repaired.");
            return CommandResult.FAILURE;
        }

        durItem.addDurability(durItem.getMaxDurability());
        durItem.updateInInventory();

        sender.sendMessage(MMOItems.plugin.getPrefix() + "Successfully repaired the item you are holding.");
        return CommandResult.SUCCESS;
    }
}
