package com.roorogeo.essentials.command.player;

import static net.minecraft.commands.Commands.argument;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.world.level.GameType;

import com.roorogeo.essentials.command.EssentialsCommand;
import com.roorogeo.essentials.perm.PermissionNodes;
import com.roorogeo.essentials.util.PlayerLookup;

/**
 * {@code /gmc}, {@code /gms}, {@code /gma}, {@code /gmsp} {@code [player]}: shortcuts for /gamemode.
 * They need {@code essentials.gamemode} plus the node of their mode.
 */
public final class GamemodeShortcutCommand extends EssentialsCommand {
	private final GameType mode;

	public GamemodeShortcutCommand(String name, GameType mode) {
		super(name, PermissionNodes.GAMEMODE);
		this.mode = mode;
	}

	@Override
	protected void build(LiteralArgumentBuilder<CommandSourceStack> root) {
		PermissionNodes.Node modeNode = GamemodeCommand.modeNode(this.mode);
		root.requires(source -> has(source, PermissionNodes.GAMEMODE) && has(source, modeNode))
				.executes(this.run(ctx -> GamemodeCommand.set(ctx.getSource(), player(ctx.getSource()), this.mode)))
				.then(argument("player", StringArgumentType.word())
						.requires(requires(PermissionNodes.GAMEMODE_OTHERS))
						.suggests(PlayerLookup.ONLINE)
						.executes(this.run(ctx -> GamemodeCommand.set(ctx.getSource(), onlinePlayer(ctx.getSource(), StringArgumentType.getString(ctx, "player")), this.mode))));
	}
}
