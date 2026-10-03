package com.roorogeo.essentials.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

import org.jspecify.annotations.Nullable;

import com.roorogeo.essentials.config.ConfigManager;
import com.roorogeo.essentials.config.EssentialsConfig;
import com.roorogeo.essentials.data.PlayerData;

/**
 * Balances are stored in each player's data file and rounded to {@code economy.decimal-places}.
 */
public final class EconomyService {
	private EconomyService() {
	}

	public static boolean isEnabled() {
		return ConfigManager.config().economy.enabled;
	}

	/** Rounds to the configured number of decimal places. */
	public static double round(double amount) {
		return BigDecimal.valueOf(amount).setScale(Math.max(0, ConfigManager.config().economy.decimalPlaces), RoundingMode.HALF_UP).doubleValue();
	}

	/** Parses a positive amount like "12.5", or returns null. */
	public static @Nullable Double parse(String text) {
		try {
			double value = round(new BigDecimal(text.trim()).doubleValue());
			return value > 0 && Double.isFinite(value) ? value : null;
		} catch (NumberFormatException e) {
			return null;
		}
	}

	/** True if {@code data} may receive {@code amount} without going over the maximum balance. */
	public static boolean canHold(PlayerData data, double amount) {
		return data.balance + amount <= ConfigManager.config().economy.maxBalance;
	}

	public static void set(PlayerData data, double amount) {
		data.balance = round(Math.max(0, Math.min(amount, ConfigManager.config().economy.maxBalance)));
		data.markDirty();
	}

	public static void add(PlayerData data, double amount) {
		set(data, data.balance + amount);
	}

	/** Formats an amount with {@code economy.format}, e.g. "$1,234.50". */
	public static String format(double amount) {
		EssentialsConfig.Economy config = ConfigManager.config().economy;
		int decimals = Math.max(0, config.decimalPlaces);
		StringBuilder pattern = new StringBuilder(config.groupThousands ? "#,##0" : "0");

		if (decimals > 0) {
			pattern.append('.').append("0".repeat(decimals));
		}

		DecimalFormat format = new DecimalFormat(pattern.toString(), DecimalFormatSymbols.getInstance(Locale.ROOT));
		String number = format.format(amount);
		String name = amount == 1.0 ? config.currencyNameSingular : config.currencyNamePlural;
		return config.format.replace("{symbol}", config.currencySymbol).replace("{amount}", number).replace("{name}", name);
	}
}
