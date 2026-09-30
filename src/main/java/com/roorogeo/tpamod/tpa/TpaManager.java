package com.roorogeo.tpamod.tpa;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.jspecify.annotations.Nullable;

import net.minecraft.ChatFormatting;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

import com.roorogeo.tpamod.config.ModConfig;
import com.roorogeo.tpamod.data.Location;
import com.roorogeo.tpamod.teleport.TeleportManager;
import com.roorogeo.tpamod.util.Msg;

/**
 * Keeps track of pending /tpa and /tpahere requests. Each player can have one outgoing request.
 */
public final class TpaManager {
	/** Keyed by the sender, since each sender has at most one outgoing request. */
	private static final Map<UUID, TpaRequest> REQUESTS = new HashMap<>();
	private static int tickCounter;

	private TpaManager() {
	}

	public static void send(ServerPlayer sender, ServerPlayer target, TpaRequest.Type type) {
		long expiresAt = System.currentTimeMillis() + ModConfig.get().requestTimeoutSeconds * 1000L;
		TpaRequest request = new TpaRequest(sender.getUUID(), sender.getGameProfile().name(), target.getUUID(), target.getGameProfile().name(), type, expiresAt);
		TpaRequest previous = REQUESTS.put(sender.getUUID(), request);

		if (previous != null && !previous.target().equals(target.getUUID())) {
			sender.sendSystemMessage(Msg.info("Your request to " + previous.targetName() + " was cancelled."));
			ServerPlayer oldTarget = sender.level().getServer().getPlayerList().getPlayer(previous.target());

			if (oldTarget != null) {
				oldTarget.sendSystemMessage(Msg.info(sender.getGameProfile().name() + " cancelled their teleport request."));
			}
		}

		String senderName = sender.getGameProfile().name();
		String text = type == TpaRequest.Type.TO
				? " wants to teleport to you."
				: " wants you to teleport to them.";

		target.sendSystemMessage(Msg.highlight(senderName).append(Msg.info(text)));
		target.sendSystemMessage(Msg.button("Accept", ChatFormatting.GREEN, "/tpaccept " + senderName, "Click to accept")
				.append(Msg.info(" "))
				.append(Msg.button("Deny", ChatFormatting.RED, "/tpdeny " + senderName, "Click to deny"))
				.append(Msg.info(" Expires in " + ModConfig.get().requestTimeoutSeconds + "s.")));

		sender.sendSystemMessage(Msg.info("Request sent to ")
				.append(Msg.highlight(target.getGameProfile().name()))
				.append(Msg.info(". "))
				.append(Msg.button("Cancel", ChatFormatting.RED, "/tpcancel", "Click to cancel")));
	}

	/** Requests sent to {@code target}, newest first. */
	public static List<TpaRequest> incoming(UUID target) {
		List<TpaRequest> list = new ArrayList<>();

		for (TpaRequest request : REQUESTS.values()) {
			if (request.target().equals(target) && !request.isExpired()) {
				list.add(request);
			}
		}

		list.sort((a, b) -> Long.compare(b.expiresAt(), a.expiresAt()));
		return list;
	}

	/**
	 * Finds a request sent to {@code target}; from {@code sender} if given, otherwise the newest one.
	 */
	public static @Nullable TpaRequest find(UUID target, @Nullable UUID sender) {
		if (sender != null) {
			TpaRequest request = REQUESTS.get(sender);
			return request != null && request.target().equals(target) && !request.isExpired() ? request : null;
		}

		List<TpaRequest> list = incoming(target);
		return list.isEmpty() ? null : list.getFirst();
	}

	public static @Nullable TpaRequest outgoing(UUID sender) {
		TpaRequest request = REQUESTS.get(sender);
		return request != null && !request.isExpired() ? request : null;
	}

	/** Accepts a request. {@code acceptor} is the target of the request. */
	public static void accept(ServerPlayer acceptor, TpaRequest request) {
		REQUESTS.remove(request.sender());
		MinecraftServer server = acceptor.level().getServer();
		ServerPlayer sender = server.getPlayerList().getPlayer(request.sender());

		if (sender == null) {
			acceptor.sendSystemMessage(Msg.error(request.senderName() + " is no longer online."));
			return;
		}

		acceptor.sendSystemMessage(Msg.success("Accepted the request from " + request.senderName() + "."));
		sender.sendSystemMessage(Msg.success(request.targetName() + " accepted your teleport request."));

		// The player who moves and the player they move to
		ServerPlayer mover = request.type() == TpaRequest.Type.TO ? sender : acceptor;
		ServerPlayer destination = request.type() == TpaRequest.Type.TO ? acceptor : sender;
		UUID destinationId = destination.getUUID();

		TeleportManager.start(mover, destination.getGameProfile().name(), () -> {
			ServerPlayer player = server.getPlayerList().getPlayer(destinationId);
			return player == null || !player.isAlive() ? null : Location.of(player);
		});
	}

	public static void deny(ServerPlayer denier, TpaRequest request) {
		REQUESTS.remove(request.sender());
		denier.sendSystemMessage(Msg.info("Denied the request from " + request.senderName() + "."));
		ServerPlayer sender = denier.level().getServer().getPlayerList().getPlayer(request.sender());

		if (sender != null) {
			sender.sendSystemMessage(Msg.error(request.targetName() + " denied your teleport request."));
		}
	}

	public static void cancel(ServerPlayer sender, TpaRequest request) {
		REQUESTS.remove(request.sender());
		sender.sendSystemMessage(Msg.info("Cancelled your request to " + request.targetName() + "."));
		ServerPlayer target = sender.level().getServer().getPlayerList().getPlayer(request.target());

		if (target != null) {
			target.sendSystemMessage(Msg.info(request.senderName() + " cancelled their teleport request."));
		}
	}

	public static void onLeave(ServerPlayer player) {
		UUID id = player.getUUID();
		MinecraftServer server = player.level().getServer();
		Iterator<TpaRequest> it = REQUESTS.values().iterator();

		while (it.hasNext()) {
			TpaRequest request = it.next();

			if (request.sender().equals(id)) {
				it.remove();
				ServerPlayer target = server.getPlayerList().getPlayer(request.target());

				if (target != null) {
					target.sendSystemMessage(Msg.info("The teleport request from " + request.senderName() + " expired, they left."));
				}
			} else if (request.target().equals(id)) {
				it.remove();
				ServerPlayer sender = server.getPlayerList().getPlayer(request.sender());

				if (sender != null) {
					sender.sendSystemMessage(Msg.info("Your teleport request to " + request.targetName() + " expired, they left."));
				}
			}
		}
	}

	public static void clear() {
		REQUESTS.clear();
	}

	public static void tick(MinecraftServer server) {
		if (++tickCounter < 20 || REQUESTS.isEmpty()) {
			return;
		}

		tickCounter = 0;
		Iterator<TpaRequest> it = REQUESTS.values().iterator();

		while (it.hasNext()) {
			TpaRequest request = it.next();

			if (!request.isExpired()) {
				continue;
			}

			it.remove();
			ServerPlayer sender = server.getPlayerList().getPlayer(request.sender());
			ServerPlayer target = server.getPlayerList().getPlayer(request.target());

			if (sender != null) {
				sender.sendSystemMessage(Msg.info("Your teleport request to " + request.targetName() + " expired."));
			}

			if (target != null) {
				target.sendSystemMessage(Msg.info("The teleport request from " + request.senderName() + " expired."));
			}
		}
	}
}
