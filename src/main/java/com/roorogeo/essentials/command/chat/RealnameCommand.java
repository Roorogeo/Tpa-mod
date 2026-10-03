package com.roorogeo.essentials.command.chat;

import static net.minecraft.commands.Commands.argument;

import java.util.Locale;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.level.ServerPlayer;

import com.roorogeo.essentials.command.EssentialsCommand;
import com.roorogeo.essentials.data.PlayerData;
import com.roorogeo.essentials.data.PlayerDataStore;
import com.roorogeo.essentials.perm.PermissionNodes;
import com.roorogeo.essentials.service.VanishService;
import com.roorogeo.essentials.text.DisplayNames;
import com.roorogeo.essentials.text.TextFormatter;

/**
 * {@code /realname <nickname>}: shows which online players use a nickname (partial matches work).
 */
public final class RealnameCommand extends EssentialsCommand {
	public RealnameCommand() {
		super("realname", PermissionNodes.REALNAME);
	}

	@Override
	protected void build(LiteralArgumentBuilder<CommandSourceStack> root) {
		root.then(argument("nickname", StringArgumentType.greedyString())
				.executes(this.run(ctx -> {
					CommandSourceStack source = ctx.getSource();
					String search = TextFormatter.strip(StringArgumentType.getString(ctx, "nickname")).toLowerCase(Locale.ROOT);
					int found = 0;

					for (ServerPlayer player : source.getServer().getPlayerList().getPlayers()) {
						PlayerData data = PlayerDataStore.get(player);

						if (data.nickname == null || !VanishService.canSee(source, player)) {
							continue;
						}

						if (TextFormatter.strip(data.nickname).toLowerCase(Locale.ROOT).contains(search)) {
							send(source, "realname.result", "nick", DisplayNames.nickname(data.nickname), "player", DisplayNames.realName(player));
							found++;
						}
					}

					if (found == 0) {
						throw error("realname.not-found", "nick", search);
					}

					return found;
				})));
	}
}
