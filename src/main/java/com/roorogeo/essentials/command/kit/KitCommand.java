package com.roorogeo.essentials.command.kit;

import static net.minecraft.commands.Commands.argument;

import java.util.Locale;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.server.level.ServerPlayer;

import com.roorogeo.essentials.command.EssentialsCommand;
import com.roorogeo.essentials.data.KitStore;
import com.roorogeo.essentials.perm.PermissionNodes;
import com.roorogeo.essentials.perm.Perms;
import com.roorogeo.essentials.service.KitService;
import com.roorogeo.essentials.text.DisplayNames;
import com.roorogeo.essentials.text.Messages;
import com.roorogeo.essentials.util.Durations;
import com.roorogeo.essentials.util.PlayerLookup;

/**
 * {@code /kit [name] [player]}: claim a kit (needs {@code essentials.kit.<name>}); without a name it
 * lists kits. Giving a kit to someone else doesn't start their cooldown.
 */
public final class KitCommand extends EssentialsCommand {
	public KitCommand() {
		super("kit", PermissionNodes.KIT);
	}

	@Override
	protected void build(LiteralArgumentBuilder<CommandSourceStack> root) {
		root.executes(this.run(ctx -> KitsCommand.list(ctx.getSource())))
				.then(argument("kit", StringArgumentType.word())
						.suggests((ctx, builder) -> SharedSuggestionProvider.suggest(KitsCommand.usable(ctx.getSource()).stream().map(KitStore.Kit::name), builder))
						.executes(this.run(ctx -> claim(ctx.getSource(), StringArgumentType.getString(ctx, "kit"))))
						.then(argument("player", StringArgumentType.word())
								.requires(requires(PermissionNodes.KIT_OTHERS))
								.suggests(PlayerLookup.ONLINE)
								.executes(this.run(ctx -> {
									KitStore.Kit kit = find(ctx.getSource(), StringArgumentType.getString(ctx, "kit"));
									ServerPlayer target = onlinePlayer(ctx.getSource(), StringArgumentType.getString(ctx, "player"));

									if (KitService.give(target, kit, false)) {
										Messages.send(target, "kit.received", "kit", kit.name());
										send(ctx.getSource(), "kit.given", "kit", kit.name(), "player", DisplayNames.of(target));
									}

									return 1;
								}))));
	}

	private static KitStore.Kit find(CommandSourceStack source, String name) throws CommandSyntaxException {
		String lower = name.toLowerCase(Locale.ROOT);
		KitStore.Kit kit = KitStore.get(lower);

		if (kit == null) {
			throw error("kit.not-found", "kit", lower);
		}

		if (!Perms.check(source, Perms.named(PermissionNodes.KIT_NAMED, lower))) {
			throw error("kit.no-permission", "kit", lower);
		}

		return kit;
	}

	private static int claim(CommandSourceStack source, String name) throws CommandSyntaxException {
		ServerPlayer player = player(source);
		KitStore.Kit kit = find(source, name);
		long remaining = KitService.remaining(player, kit);

		if (remaining < 0) {
			throw error("kit.one-time", "kit", kit.name());
		}

		if (remaining > 0) {
			throw error("kit.cooldown", "kit", kit.name(), "time", Durations.format(remaining));
		}

		if (!KitService.give(player, kit, true)) {
			return 0;
		}

		send(source, "kit.received", "kit", kit.name());
		return 1;
	}
}
