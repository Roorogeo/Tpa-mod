package com.roorogeo.essentials.command.teleport;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;

import com.roorogeo.essentials.text.Messages;

/**
 * Failures shared by the home commands' argument parsing.
 */
final class HomeErrors {
	private HomeErrors() {
	}

	static CommandSyntaxException noPermission() {
		return new SimpleCommandExceptionType(Messages.get("general.no-permission")).create();
	}

	static CommandSyntaxException neverJoined(String name) {
		return new SimpleCommandExceptionType(Messages.get("general.player-never-joined", "player", name)).create();
	}
}
