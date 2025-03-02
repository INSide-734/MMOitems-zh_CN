package net.Indyuce.mmoitems.command.mmoitems;

import io.lumine.mythic.lib.command.api.CommandTreeNode;
import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.api.Type;
import net.Indyuce.mmoitems.command.MMOItemsCommandTreeRoot;
import net.Indyuce.mmoitems.gui.edition.ItemEdition;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class EditCommandTreeNode extends CommandTreeNode {
	public EditCommandTreeNode(CommandTreeNode parent) {
		super(parent, "edit");

		addParameter(MMOItemsCommandTreeRoot.TYPE);
		addParameter(MMOItemsCommandTreeRoot.ID_2);
	}

	@Override
	public CommandResult execute(CommandSender sender, String[] args) {
		if (args.length < 3)
			return CommandResult.THROW_USAGE;

		if (!(sender instanceof Player)) {
			sender.sendMessage(ChatColor.RED + "This command is only for players.");
			return CommandResult.FAILURE;
		}

		if (!Type.isValid(args[1])) {
			sender.sendMessage(
					MMOItems.plugin.getPrefix() + ChatColor.RED + "There is no item type called " + args[1].toUpperCase().replace("-", "_") + ".");
			sender.sendMessage(MMOItems.plugin.getPrefix() + ChatColor.RED + "Type " + ChatColor.GREEN + "/mi list type" + ChatColor.RED
					+ " to see all the available item types.");
			return CommandResult.FAILURE;
		}

		Type type = Type.get(args[1]);
		String id = args[2].toUpperCase().replace("-", "_");
		if (!MMOItems.plugin.getTemplates().hasTemplate(type, id)) {
			sender.sendMessage(MMOItems.plugin.getPrefix() + ChatColor.RED + "Could not find a template called '" + id + "'.");
			return CommandResult.FAILURE;
		}

		ItemEdition.of((Player) sender, MMOItems.plugin.getTemplates().getTemplate(type, id)).open();
		return CommandResult.SUCCESS;
	}
}
