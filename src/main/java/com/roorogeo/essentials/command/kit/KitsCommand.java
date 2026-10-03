package com.roorogeo.essentials.command.kit;

import java.util.ArrayList;
import java.util.List;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerPlayer;

import com.roorogeo.essentials.command.EssentialsCommand;
import com.roorogeo.essentials.data.KitStore;
import com.roorogeo.essentials.perm.PermissionNodes;
import com.roorogeo.essentials.perm.Perms;
import com.roorogeo.essentials.service.KitService;
import com.roorogeo.essentials.text.Messages;
import com.roorogeo.essentials.util.Durations;

/**
 * {@code /kits}: lists the kits you may use; kits on cooldown show the time left.
 */
public final class KitsCommand extends EssentialsCommand {
	public KitsCommand() {
		super("kits", PermissionNodes.KITS_LIST);
	}

	@Override
	protected void build(LiteralArgumentBuilder<CommandSourceStack> root) {
		root.executes(this.run(ctx -> list(ctx.getSource())));
	}

	static List<KitStore.Kit> usable(CommandSourceStack source) {
		List<KitStore.Kit> kits = new ArrayList<>();

		for (KitStore.Kit kit : KitStore.all().values()) {
			if (Perms.check(source, Perms.named(PermissionNodes.KIT_NAMED, kit.name()))) {
				kits.add(kit);
			}
		}

		return kits;
	}

	static int list(CommandSourceStack source) {
		List<KitStore.Kit> kits = usable(source);

		if (kits.isEmpty()) {
			send(source, "kits.none");
			return 0;
		}

		ServerPlayer player = source.getPlayer();
		MutableComponent message = Messages.get("kits.header");

		for (int i = 0; i < kits.size(); i++) {
			KitStore.Kit kit = kits.get(i);

			if (i > 0) {
				message.append(Messages.get("kits.separator"));
			}

			long remaining = player == null ? 0 : KitService.remaining(player, kit);

			if (remaining != 0) {
				String time = remaining < 0 ? Messages.plain("kits.one-time") : Durations.format(remaining);
				message.append(Messages.get("kits.entry-cooldown", "kit", kit.name(), "time", time));
			} else {
				String cooldown = kit.oneTime() ? Messages.plain("kits.one-time") : Durations.formatSeconds(kit.cooldownSeconds());
				message.append(Messages.button("kits.entry", "kits.entry-hover", "/kit " + kit.name(),
						"kit", kit.name(), "count", kit.items().size(), "cooldown", cooldown));
			}
		}

		source.sendSystemMessage(message);
		return kits.size();
	}
}
