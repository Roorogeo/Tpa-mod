package com.roorogeo.essentials.command.economy;

import static net.minecraft.commands.Commands.argument;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import net.minecraft.commands.CommandSourceStack;

import com.roorogeo.essentials.command.EssentialsCommand;
import com.roorogeo.essentials.data.PlayerData;
import com.roorogeo.essentials.data.PlayerDataStore;
import com.roorogeo.essentials.perm.PermissionNodes;
import com.roorogeo.essentials.service.EconomyService;
import com.roorogeo.essentials.util.PlayerLookup;

/**
 * {@code /balance [player]}.
 */
public final class BalanceCommand extends EssentialsCommand {
	public BalanceCommand() {
		super("balance", PermissionNodes.BALANCE);
	}

	@Override
	protected void build(LiteralArgumentBuilder<CommandSourceStack> root) {
		root.executes(this.run(ctx -> {
			requireEconomy();
			PlayerData data = PlayerDataStore.get(player(ctx.getSource()));
			send(ctx.getSource(), "balance.self", "balance", EconomyService.format(data.balance));
			return 1;
		})).then(argument("player", StringArgumentType.word())
				.requires(requires(PermissionNodes.BALANCE_OTHERS))
				.suggests(PlayerLookup.KNOWN)
				.executes(this.run(ctx -> {
					requireEconomy();
					PlayerData data = knownPlayer(ctx.getSource(), StringArgumentType.getString(ctx, "player"));
					send(ctx.getSource(), "balance.other", "player", data.name, "balance", EconomyService.format(data.balance));
					return 1;
				})));
	}

	/** Fails when the economy is disabled in config.json. */
	static void requireEconomy() throws CommandSyntaxException {
		if (!EconomyService.isEnabled()) {
			throw error("economy.disabled");
		}
	}
}
