package com.roorogeo.essentials.command.player;

import static net.minecraft.commands.Commands.argument;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.level.ServerPlayer;

import com.roorogeo.essentials.command.EssentialsCommand;
import com.roorogeo.essentials.data.PlayerData;
import com.roorogeo.essentials.data.PlayerDataStore;
import com.roorogeo.essentials.perm.PermissionNodes;
import com.roorogeo.essentials.text.DisplayNames;
import com.roorogeo.essentials.text.Messages;
import com.roorogeo.essentials.util.PlayerLookup;

/**
 * {@code /god [player]}: toggles invulnerability (and full hunger with {@code player.god-prevents-hunger}).
 */
public final class GodCommand extends EssentialsCommand {
	public GodCommand() {
		super("god", PermissionNodes.GOD);
	}

	@Override
	protected void build(LiteralArgumentBuilder<CommandSourceStack> root) {
		root.executes(this.run(ctx -> toggle(ctx.getSource(), player(ctx.getSource()))))
				.then(argument("player", StringArgumentType.word())
						.requires(requires(PermissionNodes.GOD_OTHERS))
						.suggests(PlayerLookup.ONLINE)
						.executes(this.run(ctx -> toggle(ctx.getSource(), onlinePlayer(ctx.getSource(), StringArgumentType.getString(ctx, "player"))))));
	}

	private static int toggle(CommandSourceStack source, ServerPlayer target) {
		PlayerData data = PlayerDataStore.get(target);
		data.god = !data.god;
		data.markDirty();
		Messages.send(target, data.god ? "god.enabled" : "god.disabled");

		if (source.getEntity() != target) {
			send(source, data.god ? "god.enabled-other" : "god.disabled-other", "player", DisplayNames.of(target));
		}

		return 1;
	}
}
