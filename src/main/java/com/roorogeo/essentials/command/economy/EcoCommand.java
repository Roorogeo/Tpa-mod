package com.roorogeo.essentials.command.economy;

import static net.minecraft.commands.Commands.argument;
import static net.minecraft.commands.Commands.literal;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import net.minecraft.commands.CommandSourceStack;

import com.roorogeo.essentials.command.EssentialsCommand;
import com.roorogeo.essentials.config.ConfigManager;
import com.roorogeo.essentials.data.PlayerData;
import com.roorogeo.essentials.perm.PermissionNodes;
import com.roorogeo.essentials.service.EconomyService;
import com.roorogeo.essentials.util.PlayerLookup;

/**
 * {@code /eco give|take|set <player> <amount>} and {@code /eco reset <player>}. Works on offline players.
 */
public final class EcoCommand extends EssentialsCommand {
	public EcoCommand() {
		super("eco", PermissionNodes.ECO);
	}

	@Override
	protected void build(LiteralArgumentBuilder<CommandSourceStack> root) {
		root.then(literal("give").requires(requires(PermissionNodes.ECO_GIVE))
						.then(player().then(argument("amount", StringArgumentType.word()).executes(this.run(ctx -> give(ctx))))))
				.then(literal("take").requires(requires(PermissionNodes.ECO_TAKE))
						.then(player().then(argument("amount", StringArgumentType.word()).executes(this.run(ctx -> take(ctx))))))
				.then(literal("set").requires(requires(PermissionNodes.ECO_SET))
						.then(player().then(argument("amount", StringArgumentType.word()).executes(this.run(ctx -> set(ctx))))))
				.then(literal("reset").requires(requires(PermissionNodes.ECO_RESET))
						.then(player().executes(this.run(ctx -> {
							BalanceCommand.requireEconomy();
							PlayerData data = target(ctx);
							EconomyService.set(data, ConfigManager.config().economy.startingBalance);
							send(ctx.getSource(), "eco.reset", "player", data.name, "balance", EconomyService.format(data.balance));
							return 1;
						}))));
	}

	private static com.mojang.brigadier.builder.RequiredArgumentBuilder<CommandSourceStack, String> player() {
		return argument("player", StringArgumentType.word()).suggests(PlayerLookup.KNOWN);
	}

	private static PlayerData target(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
		return knownPlayer(ctx.getSource(), StringArgumentType.getString(ctx, "player"));
	}

	private static double amount(CommandContext<CommandSourceStack> ctx, boolean allowZero) throws CommandSyntaxException {
		String raw = StringArgumentType.getString(ctx, "amount");

		if (allowZero && raw.trim().matches("0+(\\.0+)?")) {
			return 0;
		}

		Double amount = EconomyService.parse(raw);

		if (amount == null) {
			throw error("pay.invalid-amount", "amount", raw);
		}

		return amount;
	}

	private static int give(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
		BalanceCommand.requireEconomy();
		PlayerData data = target(ctx);
		double amount = amount(ctx, false);

		if (!EconomyService.canHold(data, amount)) {
			throw error("eco.max-balance", "player", data.name, "max", EconomyService.format(ConfigManager.config().economy.maxBalance));
		}

		EconomyService.add(data, amount);
		send(ctx.getSource(), "eco.give", "amount", EconomyService.format(amount), "player", data.name, "balance", EconomyService.format(data.balance));
		return 1;
	}

	private static int take(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
		BalanceCommand.requireEconomy();
		PlayerData data = target(ctx);
		double amount = amount(ctx, false);

		if (data.balance < amount) {
			throw error("eco.insufficient", "player", data.name, "balance", EconomyService.format(data.balance));
		}

		EconomyService.add(data, -amount);
		send(ctx.getSource(), "eco.take", "amount", EconomyService.format(amount), "player", data.name, "balance", EconomyService.format(data.balance));
		return 1;
	}

	private static int set(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
		BalanceCommand.requireEconomy();
		PlayerData data = target(ctx);
		double amount = amount(ctx, true);

		if (amount > ConfigManager.config().economy.maxBalance) {
			throw error("eco.max-balance", "player", data.name, "max", EconomyService.format(ConfigManager.config().economy.maxBalance));
		}

		EconomyService.set(data, amount);
		send(ctx.getSource(), "eco.set", "player", data.name, "balance", EconomyService.format(data.balance));
		return 1;
	}
}
