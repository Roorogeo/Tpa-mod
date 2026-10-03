package com.roorogeo.essentials.command.admin;

import static net.minecraft.commands.Commands.literal;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.MinecraftServer;

import net.fabricmc.loader.api.FabricLoader;

import com.roorogeo.essentials.Essentials;
import com.roorogeo.essentials.command.CommandRegistry;
import com.roorogeo.essentials.command.EssentialsCommand;
import com.roorogeo.essentials.config.ConfigManager;
import com.roorogeo.essentials.data.KitStore;
import com.roorogeo.essentials.perm.PermissionNodes;
import com.roorogeo.essentials.perm.Perms;
import com.roorogeo.essentials.text.Messages;

/**
 * {@code /essentials} shows the version; {@code /essentials reload} re-reads config.json,
 * messages.json and kits.json in the background, then re-registers commands so enable toggles and
 * aliases apply immediately.
 */
public final class EssentialsAdminCommand extends EssentialsCommand {
	public EssentialsAdminCommand() {
		super("essentials", PermissionNodes.ESSENTIALS);
	}

	@Override
	protected void build(LiteralArgumentBuilder<CommandSourceStack> root) {
		root.executes(this.run(ctx -> {
			String version = FabricLoader.getInstance().getModContainer(Essentials.MOD_ID)
					.map(mod -> mod.getMetadata().getVersion().getFriendlyString())
					.orElse("?");
			send(ctx.getSource(), "general.version", "version", version);
			return 1;
		})).then(literal("reload")
				.requires(requires(PermissionNodes.RELOAD))
				.executes(this.run(ctx -> {
					CommandSourceStack source = ctx.getSource();
					MinecraftServer server = source.getServer();
					ConfigManager.reloadAsync(server).thenCompose(ignored -> KitStore.readAsync())
							.whenComplete((kits, error) -> server.execute(() -> {
								if (error != null) {
									Essentials.LOGGER.error("Reload failed", error);
									Messages.send(source, "general.reload-failed", "error", String.valueOf(error.getCause() != null ? error.getCause().getMessage() : error.getMessage()));
									return;
								}

								Perms.invalidate();
								KitStore.install(kits, server.registryAccess());
								CommandRegistry.reregister(server);
								Messages.send(source, "general.reloaded");
							}));
					return 1;
				})));
	}
}
