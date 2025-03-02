package net.Indyuce.mmoitems.command.mmoitems;

import io.lumine.mythic.lib.api.item.NBTItem;
import io.lumine.mythic.lib.api.util.SmartGive;
import io.lumine.mythic.lib.command.api.CommandTreeNode;
import io.lumine.mythic.lib.command.api.Parameter;
import net.Indyuce.mmoitems.ItemStats;
import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.api.Type;
import net.Indyuce.mmoitems.api.item.mmoitem.MMOItem;
import net.Indyuce.mmoitems.api.item.template.MMOItemTemplate;
import net.Indyuce.mmoitems.api.player.PlayerData;
import net.Indyuce.mmoitems.api.util.RandomAmount;
import net.Indyuce.mmoitems.api.util.message.Message;
import net.Indyuce.mmoitems.command.MMOItemsCommandTreeRoot;
import net.Indyuce.mmoitems.stat.data.SoulboundData;
import net.Indyuce.mmoitems.util.MMOUtils;
import org.apache.commons.lang.Validate;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;
import java.util.Random;

public class GiveCommandTreeNode extends CommandTreeNode {
	private static final Random random = new Random();

	public GiveCommandTreeNode(CommandTreeNode parent) {
		super(parent, "give");

		addParameter(MMOItemsCommandTreeRoot.TYPE);
		addParameter(MMOItemsCommandTreeRoot.ID_2);
		addParameter(Parameter.PLAYER_OPTIONAL);
		addParameter(new Parameter("(min-max)", (explore, list) -> list.addAll(Arrays.asList("1-3", "1", "10", "32", "64"))));
		addParameter(new Parameter("(unidentify-chance)", (explore, list) -> list.add("(unidentify-chance)")));
		addParameter(new Parameter("(drop-chance)", (explore, list) -> list.add("(drop-chance)")));
		addParameter(new Parameter("(soulbound-chance)", (explore, list) -> list.add("(soulbound-chance)")));
		addParameter(new Parameter("(silent)", (explore, list) -> list.addAll(Arrays.asList("silent", "s"))));
	}

	@Override
	public CommandResult execute(CommandSender sender, String[] args) {
		if (args.length < 3)
			return CommandResult.THROW_USAGE;

		try {
			Validate.isTrue(args.length > 3 || sender instanceof Player, "Please specify a player.");

			// target
			Player target = args.length > 3 ? Bukkit.getPlayer(args[3]) : (Player) sender;
			Validate.notNull(target, "Could not find player called '" + args[args.length > 3 ? 3 : 2] + "'.");

			// item
			Type type = MMOItems.plugin.getTypes().getOrThrow(args[1].toUpperCase().replace("-", "_"));
			MMOItemTemplate template = MMOItems.plugin.getTemplates().getTemplateOrThrow(type, args[2].toUpperCase().replace("-", "_"));
			RandomAmount amount = args.length > 4 ? new RandomAmount(args[4]) : new RandomAmount(1, 1);
			double unidentify = args.length > 5 ? Double.parseDouble(args[5]) / 100 : 0;
			double drop = args.length > 6 ? Double.parseDouble(args[6]) / 100 : 1;
			double soulbound = args.length > 7 ? Double.parseDouble(args[7]) / 100 : 0;
			boolean silent = args.length > 8 && (args[8].equalsIgnoreCase("silent") || args[8].equalsIgnoreCase("s"));

			// roll drop chance
			if (random.nextDouble() > drop)
				return CommandResult.SUCCESS;

			// generate mmoitem
			MMOItem mmoitem = template.newBuilder(PlayerData.get(target).getRPG()).build();

			// roll soulbound
			if (random.nextDouble() < soulbound)
				mmoitem.setData(ItemStats.SOULBOUND, new SoulboundData(target, 1));

			// generate item
			ItemStack item = mmoitem.newBuilder().build();
			Validate.isTrue(item != null && item.getType() != Material.AIR,
					"Couldn't find/generate the item called '" + template.getId() + "'. Check your console for potential item generation issues.");

			// roll unidentification
			if (random.nextDouble() < unidentify)
				item = type.getUnidentifiedTemplate().newBuilder(NBTItem.get(item)).build();

            // set amount
            int amountComputed = amount.getRandomAmount();
            Validate.isTrue(amountComputed > 0, "Amount must be positive");
			item.setAmount(amountComputed);

            // message
            if (!silent) {
                Message.RECEIVED_ITEM.format(ChatColor.YELLOW, "#item#", MMOUtils.getDisplayName(item), "#amount#",
						(amountComputed > 1 ? " x" + amountComputed : "")).send(target);
				if (!sender.equals(target))
					sender.sendMessage(MMOItems.plugin.getPrefix() + ChatColor.YELLOW + "Successfully gave " + ChatColor.GOLD
							+ MMOUtils.getDisplayName(item) + (amountComputed > 1 ? " x" + amountComputed : "") + ChatColor.YELLOW + " to "
							+ ChatColor.GOLD + target.getName() + ChatColor.YELLOW + ".");
			}

			// item
			new SmartGive(target).give(item);
			return CommandResult.SUCCESS;

		} catch (IllegalArgumentException exception) {
			sender.sendMessage(MMOItems.plugin.getPrefix() + ChatColor.RED + exception.getMessage());
			return CommandResult.FAILURE;
		}
	}
}
