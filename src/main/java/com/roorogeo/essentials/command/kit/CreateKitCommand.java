package com.roorogeo.essentials.command.kit;

import static net.minecraft.commands.Commands.argument;

import java.util.ArrayList;
import java.util.List;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

import com.roorogeo.essentials.command.EssentialsCommand;
import com.roorogeo.essentials.config.ConfigManager;
import com.roorogeo.essentials.data.KitStore;
import com.roorogeo.essentials.perm.PermissionNodes;
import com.roorogeo.essentials.text.Messages;
import com.roorogeo.essentials.util.Durations;
import com.roorogeo.essentials.util.Names;

/**
 * {@code /createkit <name> [cooldown]}: saves your inventory (including armor and offhand) as a kit.
 * The cooldown is a duration like {@code 1d} or {@code 30m}, {@code 0} for none, or {@code once}
 * for a one-time kit. An existing kit with the same name is replaced.
 */
public final class CreateKitCommand extends EssentialsCommand {
	public CreateKitCommand() {
		super("createkit", PermissionNodes.CREATEKIT);
	}

	@Override
	protected void build(LiteralArgumentBuilder<CommandSourceStack> root) {
		root.then(argument("kit", StringArgumentType.word())
				.executes(this.run(ctx -> create(ctx, "0")))
				.then(argument("cooldown", StringArgumentType.word())
						.suggests((ctx, builder) -> {
							builder.suggest("0");
							builder.suggest("once");
							builder.suggest("1h");
							builder.suggest("1d");
							return builder.buildFuture();
						})
						.executes(this.run(ctx -> create(ctx, StringArgumentType.getString(ctx, "cooldown"))))));
	}

	private static int create(CommandContext<CommandSourceStack> ctx, String rawCooldown) throws CommandSyntaxException {
		ServerPlayer player = player(ctx.getSource());
		String name = Names.normalize(StringArgumentType.getString(ctx, "kit"), ConfigManager.config().kits.namePattern);

		if (name == null) {
			throw error("createkit.invalid-name");
		}

		if (ConfigManager.config().kits.reservedNames.contains(name)) {
			throw error("createkit.reserved", "kit", name);
		}

		long cooldownSeconds;

		if (rawCooldown.equalsIgnoreCase("once") || rawCooldown.equals("-1")) {
			cooldownSeconds = -1;
		} else {
			Long millis = Durations.parse(rawCooldown);

			if (millis == null || millis == Durations.PERMANENT) {
				throw error("general.invalid-duration", "input", rawCooldown);
			}

			cooldownSeconds = millis / 1000;
		}

		Inventory inventory = player.getInventory();
		List<ItemStack> items = new ArrayList<>();

		for (int slot = 0; slot < inventory.getContainerSize(); slot++) {
			ItemStack stack = inventory.getItem(slot);

			if (!stack.isEmpty()) {
				items.add(stack.copy());
			}
		}

		if (items.isEmpty()) {
			throw error("createkit.empty");
		}

		KitStore.put(new KitStore.Kit(name, cooldownSeconds, List.copyOf(items)), ctx.getSource().registryAccess());
		String cooldown = cooldownSeconds < 0 ? Messages.plain("kits.one-time") : Durations.formatSeconds(cooldownSeconds);
		send(ctx.getSource(), "createkit.created", "kit", name, "count", items.size(), "cooldown", cooldown);
		return 1;
	}
}
