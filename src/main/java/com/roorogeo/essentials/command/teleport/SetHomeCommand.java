package com.roorogeo.essentials.command.teleport;

import static net.minecraft.commands.Commands.argument;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import org.jspecify.annotations.Nullable;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.level.ServerPlayer;

import com.roorogeo.essentials.command.EssentialsCommand;
import com.roorogeo.essentials.config.ConfigManager;
import com.roorogeo.essentials.config.EssentialsConfig;
import com.roorogeo.essentials.data.Location;
import com.roorogeo.essentials.data.PlayerData;
import com.roorogeo.essentials.perm.PermissionNodes;
import com.roorogeo.essentials.service.HomeService;
import com.roorogeo.essentials.util.Names;

/**
 * {@code /sethome [name]} or {@code /sethome <player>:<name>}.
 *
 * <p>Overwriting an existing home never counts against the limit. New homes are refused once the
 * player has as many homes as their limit, including when the limit dropped below their count.
 */
public final class SetHomeCommand extends EssentialsCommand {
	public SetHomeCommand() {
		super("sethome", PermissionNodes.SETHOME);
	}

	@Override
	protected void build(LiteralArgumentBuilder<CommandSourceStack> root) {
		root.executes(this.run(ctx -> this.setHome(ctx.getSource(), null)))
				.then(argument("home", StringArgumentType.greedyString())
						.suggests(HomeTarget.suggestions(PermissionNodes.SETHOME_OTHERS))
						.executes(this.run(ctx -> this.setHome(ctx.getSource(), StringArgumentType.getString(ctx, "home")))));
	}

	private int setHome(CommandSourceStack source, @Nullable String raw) throws CommandSyntaxException {
		ServerPlayer player = player(source);
		EssentialsConfig.Homes config = ConfigManager.config().homes;
		HomeTarget target = HomeTarget.parse(source, player, raw, PermissionNodes.SETHOME_OTHERS);
		String name = Names.normalize(target.name() == null ? config.defaultHomeName : target.name(), config.namePattern);

		if (name == null) {
			throw error("home.invalid-name");
		}

		String dimension = Location.dimensionId(player.level());

		if (!config.allowedDimensions.isEmpty() && !config.allowedDimensions.contains(dimension)) {
			throw error("home.dimension-not-allowed");
		}

		PlayerData data = target.data();
		boolean exists = data.homes.containsKey(name);

		if (target.other()) {
			data.homes.put(name, Location.of(player));
			data.markDirty();
			send(source, "home.set-other", "home", name, "player", data.name);
			return 1;
		}

		int count = data.homes.size();
		int max = HomeService.maxHomes(player);

		if (!exists && count >= max) {
			throw error(count > max ? "home.over-limit" : "home.limit-reached", "count", count, "max", HomeService.describe(max));
		}

		data.homes.put(name, Location.of(player));
		data.markDirty();

		if (exists) {
			send(source, "home.updated", "home", name);
		} else {
			send(source, "home.set", "home", name, "count", count + 1, "max", HomeService.describe(max));
		}

		return 1;
	}
}
