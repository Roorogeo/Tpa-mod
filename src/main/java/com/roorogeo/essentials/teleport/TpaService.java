package com.roorogeo.essentials.teleport;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

import org.jspecify.annotations.Nullable;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

import com.roorogeo.essentials.config.ConfigManager;
import com.roorogeo.essentials.config.EssentialsConfig;
import com.roorogeo.essentials.data.Location;
import com.roorogeo.essentials.service.ChatService;
import com.roorogeo.essentials.text.DisplayNames;
import com.roorogeo.essentials.text.Messages;

/**
 * Pending /tpa and /tpahere requests.
 */
public final class TpaService {
	private static final List<Request> REQUESTS = new ArrayList<>();
	private static int tickCounter;

	/**
	 * A request.
	 *
	 * @param here false for /tpa (sender goes to target), true for /tpahere (target goes to sender)
	 */
	public record Request(UUID sender, String senderName, UUID target, String targetName, boolean here, long created, long expiresAt) {
		public boolean isExpired() {
			return System.currentTimeMillis() >= this.expiresAt;
		}
	}

	private TpaService() {
	}

	public static void send(ServerPlayer sender, ServerPlayer target, boolean here) {
		EssentialsConfig.Tpa config = ConfigManager.config().tpa;
		long now = System.currentTimeMillis();
		String senderName = DisplayNames.realName(sender);
		String targetName = DisplayNames.realName(target);

		// A new request to the same player replaces the old one.
		REQUESTS.removeIf(r -> r.sender().equals(sender.getUUID()) && r.target().equals(target.getUUID()));
		List<Request> outgoing = outgoing(sender.getUUID());

		while (outgoing.size() >= Math.max(1, config.maxOutgoing)) {
			Request oldest = outgoing.removeFirst();
			REQUESTS.remove(oldest);
			Messages.send(sender, "tpa.replaced", "player", oldest.targetName());
		}

		REQUESTS.add(new Request(sender.getUUID(), senderName, target.getUUID(), targetName, here, now, now + config.timeoutSeconds * 1000L));

		MutableComponent sent = Messages.get("tpa.sent", "player", DisplayNames.of(target), "seconds", config.timeoutSeconds);

		if (config.clickableButtons && Messages.isEnabled("tpa.button-cancel")) {
			sent.append(Messages.button("tpa.button-cancel", "tpa.button-cancel-hover", "/tpacancel " + targetName));
		}

		if (Messages.isEnabled("tpa.sent")) {
			sender.sendSystemMessage(sent);
		}

		// Ignored senders get the normal confirmation, but the target never hears about it.
		if (ChatService.ignores(target, sender)) {
			return;
		}

		Messages.send(target, here ? "tpahere.received" : "tpa.received", "player", DisplayNames.of(sender));

		if (config.clickableButtons) {
			Component accept = Messages.button("tpa.button-accept", "tpa.button-accept-hover", "/tpaccept " + senderName);
			Component deny = Messages.button("tpa.button-deny", "tpa.button-deny-hover", "/tpdeny " + senderName);
			Messages.send(target, "tpa.buttons", "accept", accept, "deny", deny, "seconds", config.timeoutSeconds);
		} else {
			Messages.send(target, "tpa.hint", "player", senderName);
		}
	}

	/** Live requests sent by {@code sender}, oldest first. */
	public static List<Request> outgoing(UUID sender) {
		List<Request> list = new ArrayList<>();

		for (Request request : REQUESTS) {
			if (request.sender().equals(sender) && !request.isExpired()) {
				list.add(request);
			}
		}

		list.sort(Comparator.comparingLong(Request::created));
		return list;
	}

	/** Live requests sent to {@code target}, newest first. */
	public static List<Request> incoming(UUID target) {
		List<Request> list = new ArrayList<>();

		for (Request request : REQUESTS) {
			if (request.target().equals(target) && !request.isExpired()) {
				list.add(request);
			}
		}

		list.sort(Comparator.comparingLong(Request::created).reversed());
		return list;
	}

	/** The request from {@code sender} to {@code target}, or the newest one to {@code target} if sender is null. */
	public static @Nullable Request find(UUID target, @Nullable UUID sender) {
		for (Request request : incoming(target)) {
			if (sender == null || request.sender().equals(sender)) {
				return request;
			}
		}

		return null;
	}

	public static void remove(Request request) {
		REQUESTS.remove(request);
	}

	/**
	 * Accepts a request. The moving player goes through the normal teleport flow (combat check,
	 * cooldown and warmup apply to them), and the destination follows the other player.
	 */
	public static void accept(ServerPlayer acceptor, Request request) {
		REQUESTS.remove(request);
		MinecraftServer server = acceptor.level().getServer();
		ServerPlayer sender = server.getPlayerList().getPlayer(request.sender());

		if (sender == null) {
			Messages.send(acceptor, "general.target-offline", "player", request.senderName());
			return;
		}

		Messages.send(acceptor, "tpa.accepted", "player", DisplayNames.of(sender));
		Messages.send(sender, "tpa.accepted-sender", "player", DisplayNames.of(acceptor));
		ServerPlayer mover = request.here() ? acceptor : sender;
		ServerPlayer destination = request.here() ? sender : acceptor;
		UUID destinationId = destination.getUUID();
		String command = request.here() ? "tpahere" : "tpa";

		TeleportService.start(mover, new TeleportRequest(command, DisplayNames.of(destination), () -> {
			ServerPlayer player = server.getPlayerList().getPlayer(destinationId);
			return player == null || !player.isAlive() ? null : Location.of(player);
		}, true));
	}

	public static void deny(ServerPlayer denier, Request request) {
		REQUESTS.remove(request);
		Messages.send(denier, "tpa.denied", "player", request.senderName());
		ServerPlayer sender = denier.level().getServer().getPlayerList().getPlayer(request.sender());

		if (sender != null) {
			Messages.send(sender, "tpa.denied-sender", "player", DisplayNames.of(denier));
		}
	}

	public static void cancel(ServerPlayer sender, Request request) {
		REQUESTS.remove(request);
		Messages.send(sender, "tpa.cancelled", "player", request.targetName());
		ServerPlayer target = sender.level().getServer().getPlayerList().getPlayer(request.target());

		if (target != null) {
			Messages.send(target, "tpa.cancelled-target", "player", DisplayNames.of(sender));
		}
	}

	public static void onLeave(ServerPlayer player) {
		UUID id = player.getUUID();
		MinecraftServer server = player.level().getServer();
		Iterator<Request> it = REQUESTS.iterator();

		while (it.hasNext()) {
			Request request = it.next();
			UUID other;
			String name;

			if (request.sender().equals(id)) {
				other = request.target();
				name = request.senderName();
			} else if (request.target().equals(id)) {
				other = request.sender();
				name = request.targetName();
			} else {
				continue;
			}

			it.remove();
			ServerPlayer otherPlayer = server.getPlayerList().getPlayer(other);

			if (otherPlayer != null) {
				Messages.send(otherPlayer, "tpa.player-left", "player", name);
			}
		}
	}

	public static void tick(MinecraftServer server) {
		if (++tickCounter < 20 || REQUESTS.isEmpty()) {
			return;
		}

		tickCounter = 0;
		Iterator<Request> it = REQUESTS.iterator();

		while (it.hasNext()) {
			Request request = it.next();

			if (!request.isExpired()) {
				continue;
			}

			it.remove();
			ServerPlayer sender = server.getPlayerList().getPlayer(request.sender());
			ServerPlayer target = server.getPlayerList().getPlayer(request.target());

			if (sender != null) {
				Messages.send(sender, "tpa.expired-sender", "player", request.targetName());
			}

			if (target != null) {
				Messages.send(target, "tpa.expired-target", "player", request.senderName());
			}
		}
	}

	public static void clear() {
		REQUESTS.clear();
	}
}
