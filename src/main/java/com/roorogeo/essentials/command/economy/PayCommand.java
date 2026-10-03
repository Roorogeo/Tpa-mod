package com.roorogeo.essentials.command.economy;

import static net.minecraft.commands.Commands.argument;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.level.ServerPlayer;

import com.roorogeo.essentials.command.EssentialsCommand;
import com.roorogeo.essentials.config.ConfigManager;
import com.roorogeo.essentials.data.PlayerData;
import com.roorogeo.essentials.data.PlayerDataStore;
import com.roorogeo.essentials.perm.PermissionNodes;
import com.roorogeo.essentials.service.EconomyService;
import com.roorogeo.essentials.text.DisplayNames;
import com.roorogeo.essentials.text.Messages;
import com.roorogeo.essentials.util.PlayerLookup;

/**
 * {@code /pay <player> <amount>}. Offline players can be paid when {@code economy.pay-offline} is on.
 */
public final class PayCommand extends EssentialsCommand {
	public PayCommand() {
		super("pay", PermissionNodes.PAY);
	}

	@Override
	protected void build(LiteralArgumentBuilder<CommandSourceStack> root) {
		root.then(argument("player", StringArgumentType.word())
				.suggests(PlayerLookup.KNOWN)
				.then(argument("amount", StringArgumentType.word())
						.executes(this.run(ctx -> {
							BalanceCommand.requireEconomy();
							CommandSourceStack source = ctx.getSource();
							ServerPlayer sender = player(source);
							String rawAmount = StringArgumentType.getString(ctx, "amount");
							Double amount = EconomyService.parse(rawAmount);

							if (amount == null) {
								throw error("pay.invalid-amount", "amount", rawAmount);
							}

							double minimum = ConfigManager.config().economy.minPayment;

							if (amount < minimum) {
								throw error("pay.minimum", "amount", EconomyService.format(minimum));
							}

							PlayerData target = knownPlayer(source, StringArgumentType.getString(ctx, "player"));

							if (target.uuid.equals(sender.getUUID())) {
								throw error("pay.self");
							}

							ServerPlayer online = online(source, target);

							if (online == null && !ConfigManager.config().economy.payOffline) {
								throw error("pay.offline-disabled");
							}

							PlayerData data = PlayerDataStore.get(sender);

							if (data.balance < amount) {
								throw error("pay.insufficient", "balance", EconomyService.format(data.balance));
							}

							if (!EconomyService.canHold(target, amount)) {
								throw error("pay.max-balance", "player", target.name);
							}

							EconomyService.add(data, -amount);
							EconomyService.add(target, amount);
							String formatted = EconomyService.format(amount);
							send(source, "pay.sent", "amount", formatted, "player", target.name);

							if (online != null) {
								Messages.send(online, "pay.received", "amount", formatted, "player", DisplayNames.of(sender));
							}

							return 1;
						}))));
	}
}
