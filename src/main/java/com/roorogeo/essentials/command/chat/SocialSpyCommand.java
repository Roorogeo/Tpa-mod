package com.roorogeo.essentials.command.chat;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;

import net.minecraft.commands.CommandSourceStack;

import com.roorogeo.essentials.command.EssentialsCommand;
import com.roorogeo.essentials.data.PlayerData;
import com.roorogeo.essentials.data.PlayerDataStore;
import com.roorogeo.essentials.perm.PermissionNodes;

/**
 * {@code /socialspy}: toggles seeing other players' private messages and mail.
 */
public final class SocialSpyCommand extends EssentialsCommand {
	public SocialSpyCommand() {
		super("socialspy", PermissionNodes.SOCIALSPY);
	}

	@Override
	protected void build(LiteralArgumentBuilder<CommandSourceStack> root) {
		root.executes(this.run(ctx -> {
			PlayerData data = PlayerDataStore.get(player(ctx.getSource()));
			data.socialSpy = !data.socialSpy;
			data.markDirty();
			send(ctx.getSource(), data.socialSpy ? "socialspy.enabled" : "socialspy.disabled");
			return 1;
		}));
	}
}
