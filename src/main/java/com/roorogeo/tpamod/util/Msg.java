package com.roorogeo.tpamod.util;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;

/**
 * Small helpers for consistently styled chat messages.
 */
public final class Msg {
	private Msg() {
	}

	public static MutableComponent info(String text) {
		return Component.literal(text).withStyle(ChatFormatting.GOLD);
	}

	public static MutableComponent success(String text) {
		return Component.literal(text).withStyle(ChatFormatting.GREEN);
	}

	public static MutableComponent error(String text) {
		return Component.literal(text).withStyle(ChatFormatting.RED);
	}

	public static MutableComponent highlight(String text) {
		return Component.literal(text).withStyle(ChatFormatting.YELLOW);
	}

	/** A clickable chat button that runs {@code command} when clicked. */
	public static MutableComponent button(String label, ChatFormatting color, String command, String hover) {
		return Component.literal("[" + label + "]").withStyle(style -> style
				.withColor(color)
				.withBold(true)
				.withClickEvent(new ClickEvent.RunCommand(command))
				.withHoverEvent(new HoverEvent.ShowText(Component.literal(hover))));
	}

	/** A clickable, non-bold list entry. */
	public static MutableComponent link(String label, String command, String hover) {
		return Component.literal(label).withStyle(style -> style
				.withColor(ChatFormatting.AQUA)
				.withClickEvent(new ClickEvent.RunCommand(command))
				.withHoverEvent(new HoverEvent.ShowText(Component.literal(hover))));
	}
}
