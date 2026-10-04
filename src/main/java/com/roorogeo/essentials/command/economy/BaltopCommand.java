package com.roorogeo.essentials.command.economy;

import static net.minecraft.commands.Commands.argument;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import net.minecraft.commands.CommandSourceStack;

import com.roorogeo.essentials.command.EssentialsCommand;
import com.roorogeo.essentials.config.ConfigManager;
import com.roorogeo.essentials.data.PlayerData;
import com.roorogeo.essentials.data.PlayerDataStore;
import com.roorogeo.essentials.perm.PermissionNodes;
import com.roorogeo.essentials.service.EconomyService;

/**
 * {@code /baltop [page]}: richest players.
 */
public final class BaltopCommand extends EssentialsCommand {
	public BaltopCommand() {
		super("baltop", PermissionNodes.BALTOP);
	}

	@Override
	protected void build(LiteralArgumentBuilder<CommandSourceStack> root) {
		root.executes(this.run(ctx -> show(ctx.getSource(), 1)))
				.then(argument("page", IntegerArgumentType.integer(1))
						.executes(this.run(ctx -> show(ctx.getSource(), IntegerArgumentType.getInteger(ctx, "page")))));
	}

	private static int show(CommandSourceStack source, int page) throws CommandSyntaxException {
		BalanceCommand.requireEconomy();
		List<PlayerData> players = new ArrayList<>(PlayerDataStore.all());
		// Players whose name isn't known yet (homes imported before they joined) can't be listed by name.
		players.removeIf(data -> data.name.isEmpty());
		players.sort(Comparator.comparingDouble((PlayerData data) -> data.balance).reversed());
		int pageSize = Math.max(1, ConfigManager.config().economy.baltopPageSize);
		int pages = Math.max(1, (players.size() + pageSize - 1) / pageSize);
		int current = Math.min(page, pages);
		double total = 0;

		for (PlayerData data : players) {
			total += data.balance;
		}

		send(source, "baltop.header", "page", current, "pages", pages);

		for (int i = (current - 1) * pageSize; i < Math.min(players.size(), current * pageSize); i++) {
			PlayerData data = players.get(i);
			send(source, "baltop.entry", "rank", i + 1, "player", data.name, "balance", EconomyService.format(data.balance));
		}

		send(source, "baltop.total", "total", EconomyService.format(total));
		return 1;
	}
}
