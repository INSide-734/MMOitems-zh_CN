package net.Indyuce.mmoitems.stat.data;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang.Validate;

import net.Indyuce.mmoitems.api.item.build.MMOItemBuilder;
import net.Indyuce.mmoitems.stat.data.random.RandomStatData;
import net.Indyuce.mmoitems.stat.data.type.Mergeable;
import net.Indyuce.mmoitems.stat.data.type.StatData;

public class CommandListData implements StatData, Mergeable, RandomStatData {
	private final Set<CommandData> commands;

	public CommandListData(Set<CommandData> commands) {
		this.commands = commands;
	}

	public CommandListData(CommandData... commands) {
		this(new HashSet<>());

		add(commands);
	}

	public void add(CommandData... commands) {
		this.commands.addAll(Arrays.asList(commands));
	}

	public Set<CommandData> getCommands() {
		return commands;
	}

	@Override
	public void merge(StatData data) {
		Validate.isTrue(data instanceof CommandListData, "Cannot merge two different stat data types");
		commands.addAll(((CommandListData) data).commands);
	}

	@Override
	public StatData randomize(MMOItemBuilder builder) {
		return new CommandListData(new HashSet<>(commands));
	}
}