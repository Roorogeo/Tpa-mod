package com.roorogeo.essentials.command.player;

import static net.minecraft.commands.Commands.argument;
import static net.minecraft.commands.Commands.literal;

import java.util.List;
import java.util.Map;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.GameType;

import com.roorogeo.essentials.command.EssentialsCommand;
import com.roorogeo.essentials.perm.PermissionNodes;
import com.roorogeo.essentials.text.DisplayNames;
import com.roorogeo.essentials.text.Messages;
import com.roorogeo.essentials.util.PlayerLookup;

/**
 * {@code /gamemode <mode> [player]}. Modes: survival/s/0, creative/c/1, adventure/a/2, spectator/sp/3;
 * each needs {@code essentials.gamemode.<mode>}. Replaces vanilla /gamemode.
 */
public final class GamemodeCommand extends EssentialsCommand {
	private static final Map<GameType, List<String>> NAMES = Map.of(
			GameType.SURVIVAL, List.of("survival", "s", "0"),
			GameType.CREATIVE, List.of("creative", "c", "1"),
			GameType.ADVENTURE, List.of("adventure", "a", "2"),
			GameType.SPECTATOR, List.of("spectator", "sp", "3"));

	public GamemodeCommand() {
		super("gamemode", PermissionNodes.GAMEMODE);
	}

	@Override
	protected void build(LiteralArgumentBuilder<CommandSourceStack> root) {
		for (Map.Entry<GameType, List<String>> entry : NAMES.entrySet()) {
			GameType mode = entry.getKey();

			for (String name : entry.getValue()) {
				root.then(literal(name)
						.requires(requires(modeNode(mode)))
						.executes(this.run(ctx -> set(ctx.getSource(), player(ctx.getSource()), mode)))
						.then(argument("player", StringArgumentType.word())
								.requires(requires(PermissionNodes.GAMEMODE_OTHERS))
								.suggests(PlayerLookup.ONLINE)
								.executes(this.run(ctx -> set(ctx.getSource(), onlinePlayer(ctx.getSource(), StringArgumentType.getString(ctx, "player")), mode)))));
			}
		}
	}

	static PermissionNodes.Node modeNode(GameType mode) {
		return switch (mode) {
			case SURVIVAL -> PermissionNodes.GAMEMODE_SURVIVAL;
			case CREATIVE -> PermissionNodes.GAMEMODE_CREATIVE;
			case ADVENTURE -> PermissionNodes.GAMEMODE_ADVENTURE;
			case SPECTATOR -> PermissionNodes.GAMEMODE_SPECTATOR;
		};
	}

	/** Changes the game mode and tells both sides. */
	static int set(CommandSourceStack source, ServerPlayer target, GameType mode) throws CommandSyntaxException {
		if (!has(source, modeNode(mode))) {
			throw error("gamemode.no-permission-mode", "mode", Messages.plain("gamemode.name." + mode.getName()));
		}

		target.setGameMode(mode);
		String name = Messages.plain("gamemode.name." + mode.getName());
		Messages.send(target, "gamemode.set", "mode", name);

		if (source.getEntity() != target) {
			send(source, "gamemode.set-other", "player", DisplayNames.of(target), "mode", name);
		}

		return 1;
	}
}
