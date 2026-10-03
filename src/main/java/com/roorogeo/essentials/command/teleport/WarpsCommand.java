package com.roorogeo.essentials.command.teleport;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.MutableComponent;

import com.roorogeo.essentials.command.EssentialsCommand;
import com.roorogeo.essentials.config.ConfigManager;
import com.roorogeo.essentials.data.Location;
import com.roorogeo.essentials.data.NamedLocations;
import com.roorogeo.essentials.perm.PermissionNodes;
import com.roorogeo.essentials.perm.Perms;
import com.roorogeo.essentials.text.Messages;

/**
 * {@code /warps}: lists warps as clickable entries.
 */
public final class WarpsCommand extends EssentialsCommand {
	public WarpsCommand() {
		super("warps", PermissionNodes.WARPS);
	}

	@Override
	protected void build(LiteralArgumentBuilder<CommandSourceStack> root) {
		root.executes(this.run(ctx -> list(ctx.getSource())));
	}

	/** Warps the source may use (all warps if per-warp permissions or list filtering are off). */
	static List<String> usable(CommandSourceStack source) {
		List<String> names = new ArrayList<>();
		boolean filter = ConfigManager.config().warps.perWarpPermissions && ConfigManager.config().warps.listOnlyUsable;

		for (String name : NamedLocations.WARPS.all().keySet()) {
			if (!filter || Perms.check(source, Perms.named(PermissionNodes.WARP_NAMED, name))) {
				names.add(name);
			}
		}

		return names;
	}

	static int list(CommandSourceStack source) {
		List<String> names = usable(source);

		if (names.isEmpty()) {
			send(source, "warps.none");
			return 0;
		}

		Map<String, Location> all = NamedLocations.WARPS.all();
		MutableComponent message = Messages.get("warps.header", "count", names.size());

		for (int i = 0; i < names.size(); i++) {
			if (i > 0) {
				message.append(Messages.get("warps.separator"));
			}

			String name = names.get(i);
			message.append(Messages.button("warps.entry", "warps.entry-hover", "/warp " + name, "warp", name, "location", all.get(name).describe()));
		}

		source.sendSystemMessage(message);
		return names.size();
	}
}
