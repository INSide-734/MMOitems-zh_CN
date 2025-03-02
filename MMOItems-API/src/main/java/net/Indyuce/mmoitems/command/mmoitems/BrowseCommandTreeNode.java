package net.Indyuce.mmoitems.command.mmoitems;

import net.Indyuce.mmoitems.command.MMOItemsCommandTreeRoot;
import net.Indyuce.mmoitems.gui.TypeBrowser;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.api.Type;
import net.Indyuce.mmoitems.gui.ItemBrowser;
import io.lumine.mythic.lib.command.api.CommandTreeNode;

public class BrowseCommandTreeNode extends CommandTreeNode {
	public BrowseCommandTreeNode(CommandTreeNode parent) {
		super(parent, "browse");

		addParameter(MMOItemsCommandTreeRoot.TYPE);
	}

	@Override
	public CommandResult execute(CommandSender sender, String[] args) {
		if (!(sender instanceof Player)) {
			sender.sendMessage(ChatColor.RED + "This command is only for players.");
			return CommandResult.FAILURE;
		}

		if (args.length < 2) {
			TypeBrowser.of((Player) sender).open();
			return CommandResult.SUCCESS;
		}

		if (!Type.isValid(args[1])) {
			sender.sendMessage(MMOItems.plugin.getPrefix() + ChatColor.RED + "Please specify a valid item type.");
			return CommandResult.FAILURE;
		}

		ItemBrowser.of((Player) sender, Type.get(args[1])).open();
		return CommandResult.SUCCESS;
	}
}
