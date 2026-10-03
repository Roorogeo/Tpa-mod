package com.roorogeo.essentials.command.teleport;

import static net.minecraft.commands.Commands.argument;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import org.jspecify.annotations.Nullable;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

import com.roorogeo.essentials.command.EssentialsCommand;
import com.roorogeo.essentials.config.ConfigManager;
import com.roorogeo.essentials.config.EssentialsConfig;
import com.roorogeo.essentials.data.PlayerData;
import com.roorogeo.essentials.perm.PermissionNodes;
import com.roorogeo.essentials.teleport.TeleportRequest;
import com.roorogeo.essentials.teleport.TeleportService;
import com.roorogeo.essentials.text.Messages;

/**
 * {@code /home [name]} or {@code /home <player>:<name>}: teleport to a home.
 *
 * <p>Without a name: the home called {@code homes.default-home-name}, or the only home, or (with
 * several homes and {@code homes.list-when-ambiguous}) the home list.
 */
public final class HomeCommand extends EssentialsCommand {
	public HomeCommand() {
		super("home", PermissionNodes.HOME);
	}

	@Override
	protected void build(LiteralArgumentBuilder<CommandSourceStack> root) {
		root.executes(this.run(ctx -> this.home(ctx.getSource(), null)))
				.then(argument("home", StringArgumentType.greedyString())
						.suggests(HomeTarget.suggestions(PermissionNodes.HOME_OTHERS))
						.executes(this.run(ctx -> this.home(ctx.getSource(), StringArgumentType.getString(ctx, "home")))));
	}

	private int home(CommandSourceStack source, @Nullable String raw) throws CommandSyntaxException {
		ServerPlayer player = player(source);
		HomeTarget target = HomeTarget.parse(source, player, raw, PermissionNodes.HOME_OTHERS);
		PlayerData data = target.data();
		EssentialsConfig.Homes config = ConfigManager.config().homes;
		String name = target.name();

		if (name == null) {
			if (data.homes.isEmpty()) {
				throw error("home.none");
			}

			if (data.homes.containsKey(config.defaultHomeName)) {
				name = config.defaultHomeName;
			} else if (data.homes.size() == 1) {
				name = data.homes.keySet().iterator().next();
			} else if (config.listWhenAmbiguous) {
				return HomesCommand.list(source, data, target.other());
			} else {
				name = config.defaultHomeName;
			}
		}

		if (!data.homes.containsKey(name)) {
			throw target.other() ? error("home.not-found-other", "player", data.name, "home", name) : error("home.not-found", "home", name);
		}

		String home = name;
		Component destination = target.other()
				? Messages.get("home.destination-other", "player", data.name, "home", home)
				: Messages.get("home.destination", "home", home);
		return TeleportService.start(player, new TeleportRequest("home", destination, () -> data.homes.get(home), true)) ? 1 : 0;
	}
}
