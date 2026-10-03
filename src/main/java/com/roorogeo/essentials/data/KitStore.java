package com.roorogeo.essentials.data;

import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.serialization.JsonOps;
import org.jspecify.annotations.Nullable;

import net.minecraft.core.HolderLookup;
import net.minecraft.resources.RegistryOps;
import net.minecraft.world.item.ItemStack;

import com.roorogeo.essentials.Essentials;
import com.roorogeo.essentials.config.ConfigManager;
import com.roorogeo.essentials.storage.AsyncFileWriter;

/**
 * Kits, stored in {@code config/essentials/kits.json}:
 * <pre>{@code
 * {
 *   "starter": {
 *     "cooldown-seconds": 86400,
 *     "items": [ { "id": "minecraft:stone_sword", "count": 1 } ]
 *   }
 * }
 * }</pre>
 * A cooldown of {@code -1} means the kit can only be claimed once; {@code 0} means no cooldown.
 * Items use the vanilla item stack format, so components (enchantments, names, ...) are kept.
 */
public final class KitStore {
	private static final Path PATH = ConfigManager.DIRECTORY.resolve("kits.json");
	private static final Map<String, Kit> KITS = new TreeMap<>();
	private static JsonObject raw = new JsonObject();

	/** A kit. {@code cooldownSeconds} is -1 for one-time kits. */
	public record Kit(String name, long cooldownSeconds, List<ItemStack> items) {
		public boolean oneTime() {
			return this.cooldownSeconds < 0;
		}
	}

	private KitStore() {
	}

	/** Reads kits.json on the I/O thread. */
	public static CompletableFuture<JsonObject> readAsync() {
		return CompletableFuture.supplyAsync(() -> {
			if (!Files.exists(PATH)) {
				return defaultKits();
			}

			try (Reader reader = Files.newBufferedReader(PATH, StandardCharsets.UTF_8)) {
				JsonElement element = JsonParser.parseReader(reader);
				return element.isJsonObject() ? element.getAsJsonObject() : new JsonObject();
			} catch (Exception e) {
				throw new CompletionException(e);
			}
		}, AsyncFileWriter.executor());
	}

	/** An example kit written on first start so the format is easy to copy. */
	private static JsonObject defaultKits() {
		JsonObject root = new JsonObject();
		JsonObject starter = new JsonObject();
		starter.addProperty("cooldown-seconds", 86400);
		JsonArray items = new JsonArray();
		items.add(item("minecraft:stone_sword", 1));
		items.add(item("minecraft:stone_pickaxe", 1));
		items.add(item("minecraft:bread", 16));
		starter.add("items", items);
		root.add("starter", starter);
		return root;
	}

	private static JsonObject item(String id, int count) {
		JsonObject item = new JsonObject();
		item.addProperty("id", id);
		item.addProperty("count", count);
		return item;
	}

	/** Decodes the loaded JSON into item stacks (server thread, needs registries). */
	public static void install(JsonObject json, HolderLookup.Provider registries) {
		KITS.clear();
		raw = json;
		RegistryOps<JsonElement> ops = RegistryOps.create(JsonOps.INSTANCE, registries);

		for (Map.Entry<String, JsonElement> entry : json.entrySet()) {
			if (!entry.getValue().isJsonObject()) {
				continue;
			}

			JsonObject kit = entry.getValue().getAsJsonObject();
			long cooldown = kit.has("cooldown-seconds") ? kit.get("cooldown-seconds").getAsLong() : 0;
			List<ItemStack> items = new ArrayList<>();

			if (kit.has("items") && kit.get("items").isJsonArray()) {
				for (JsonElement itemJson : kit.getAsJsonArray("items")) {
					ItemStack.CODEC.parse(ops, itemJson)
							.resultOrPartial(error -> Essentials.LOGGER.error("Invalid item in kit {}: {}", entry.getKey(), error))
							.ifPresent(items::add);
				}
			}

			KITS.put(entry.getKey(), new Kit(entry.getKey(), cooldown, List.copyOf(items)));
		}

		Essentials.LOGGER.info("Loaded {} kits", KITS.size());
		save();
	}

	public static Map<String, Kit> all() {
		return Collections.unmodifiableMap(KITS);
	}

	public static @Nullable Kit get(String name) {
		return KITS.get(name);
	}

	public static void put(Kit kit, HolderLookup.Provider registries) {
		RegistryOps<JsonElement> ops = RegistryOps.create(JsonOps.INSTANCE, registries);
		JsonObject json = new JsonObject();
		json.addProperty("cooldown-seconds", kit.cooldownSeconds());
		JsonArray items = new JsonArray();

		for (ItemStack stack : kit.items()) {
			items.add(ItemStack.CODEC.encodeStart(ops, stack).getOrThrow());
		}

		json.add("items", items);
		raw.add(kit.name(), json);
		KITS.put(kit.name(), kit);
		save();
	}

	public static boolean remove(String name) {
		if (KITS.remove(name) == null) {
			return false;
		}

		raw.remove(name);
		save();
		return true;
	}

	public static void clear() {
		KITS.clear();
		raw = new JsonObject();
	}

	private static void save() {
		AsyncFileWriter.write(PATH, ConfigManager.GSON.toJson(raw));
	}
}
