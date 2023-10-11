package com.hexagram2021.ingame_biome_map.utils;

import com.google.common.collect.ImmutableMap;
import com.google.gson.*;
import com.hexagram2021.ingame_biome_map.IngameBiomeMap;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.biome.Biomes;

import javax.annotation.Nullable;
import java.awt.*;
import java.io.*;
import java.util.List;
import java.util.*;

public class ConfigHelper {
	public final File filePath = new File("./config/");
	private final File file;

	public static class CustomBiome {
		final ResourceLocation id;
		final int r, g, b, a;

		public CustomBiome(String id, int r, int g, int b, int a) {
			this.id = Objects.requireNonNull(ResourceLocation.tryParse(id));
			this.r = r & 255;
			this.g = g & 255;
			this.b = b & 255;
			this.a = a & 255;
		}

		Color getColor() {
			return new Color(this.r, this.g, this.b, this.a);
		}
	}

	@Nullable
	private Integer port = null;
	@Nullable
	private Boolean multiThread = null;
	@Nullable
	private List<CustomBiome> customBiomes = null;
	
	private final Map<ResourceLocation, Color> biomeColors;

	public ConfigHelper(String filename) {
		this.file = new File(this.filePath + "/" + filename);

		try {
			if (!this.filePath.exists() && !this.filePath.mkdir()) {
				IngameBiomeMap.LOGGER.error("Could not mkdir " + this.filePath);
			} else if (!this.file.exists()) {
				if(this.file.createNewFile()) {
					this.fillEmpty();
				} else {
					IngameBiomeMap.LOGGER.error("Could not create new file " + this.file);
				}
			} else {
				FileInputStream in = new FileInputStream(this.file);
				Reader reader = new InputStreamReader(in);
				JsonElement json = JsonParser.parseReader(reader);
				reader.close();
				in.close();
				this.loadFromJson(json.getAsJsonObject());
				this.fillEmpty();
			}
		} catch(IOException e) {
			IngameBiomeMap.LOGGER.error(e.toString());
		}
		ImmutableMap.Builder<ResourceLocation, Color> builder = ImmutableMap.builder();
		this.customBiomes.forEach(b -> builder.put(b.id, b.getColor()));
		this.biomeColors = builder.build();
	}

	private void loadFromJson(JsonObject json) {
		if(json.has("port")) {
			this.port = json.get("port").getAsInt();
		}
		if(json.has("multiThread")) {
			this.multiThread = json.get("multiThread").getAsBoolean();
		}
		if(json.has("CustomBiomes")) {
			JsonArray array = json.get("CustomBiomes").getAsJsonArray();
			this.customBiomes = new ArrayList<>();
			Random random = new Random();
			for(int i = 0; i < array.size(); ++i) {
				JsonObject customBiome = array.get(i).getAsJsonObject();
				if(customBiome.has("id")) {
					int r = customBiome.has("r") ? customBiome.get("r").getAsInt() : random.nextInt(256);
					int g = customBiome.has("g") ? customBiome.get("g").getAsInt() : random.nextInt(256);
					int b = customBiome.has("b") ? customBiome.get("b").getAsInt() : random.nextInt(256);
					int a = customBiome.has("a") ? customBiome.get("a").getAsInt() : 255;
					this.customBiomes.add(new CustomBiome(customBiome.get("id").getAsString(), r, g, b, a));
				}
			}
		}
	}

	private void fillEmpty() {
		if(this.port == null) {
			this.port = 1949;
		}
		if(this.multiThread == null) {
			this.multiThread = true;
		}
		if(this.customBiomes == null) {
			this.customBiomes = new ArrayList<>();
			this.customBiomes.add(new CustomBiome(Biomes.PLAINS.location().toString(), 180, 240, 28, 255));
			this.customBiomes.add(new CustomBiome(Biomes.SUNFLOWER_PLAINS.location().toString(), 216, 240, 56, 255));
			this.customBiomes.add(new CustomBiome(Biomes.SNOWY_PLAINS.location().toString(), 250, 250, 250, 255));
			this.customBiomes.add(new CustomBiome(Biomes.ICE_SPIKES.location().toString(), 204, 204, 254, 255));
			this.customBiomes.add(new CustomBiome(Biomes.DESERT.location().toString(), 244, 224, 170, 255));
			this.customBiomes.add(new CustomBiome(Biomes.SWAMP.location().toString(), 114, 254, 234, 255));
			this.customBiomes.add(new CustomBiome(Biomes.MANGROVE_SWAMP.location().toString(), 252, 208, 242, 255));
			this.customBiomes.add(new CustomBiome(Biomes.FOREST.location().toString(), 156, 122, 96, 255));
			this.customBiomes.add(new CustomBiome(Biomes.FLOWER_FOREST.location().toString(), 212, 164, 142, 255));
			this.customBiomes.add(new CustomBiome(Biomes.BIRCH_FOREST.location().toString(), 252, 240, 220, 255));
			this.customBiomes.add(new CustomBiome(Biomes.DARK_FOREST.location().toString(), 124, 116, 96, 255));
			this.customBiomes.add(new CustomBiome(Biomes.OLD_GROWTH_BIRCH_FOREST.location().toString(), 222, 208, 184, 255));
			this.customBiomes.add(new CustomBiome(Biomes.OLD_GROWTH_PINE_TAIGA.location().toString(), 44, 98, 32, 255));
			this.customBiomes.add(new CustomBiome(Biomes.OLD_GROWTH_SPRUCE_TAIGA.location().toString(), 46, 84, 38, 255));
			this.customBiomes.add(new CustomBiome(Biomes.TAIGA.location().toString(), 66, 120, 54, 255));
			this.customBiomes.add(new CustomBiome(Biomes.SNOWY_TAIGA.location().toString(), 122, 156, 118, 255));
			this.customBiomes.add(new CustomBiome(Biomes.SAVANNA.location().toString(), 254, 172, 124, 255));
			this.customBiomes.add(new CustomBiome(Biomes.SAVANNA_PLATEAU.location().toString(), 194, 138, 104, 255));
			this.customBiomes.add(new CustomBiome(Biomes.WINDSWEPT_HILLS.location().toString(), 122, 102, 172, 255));
			this.customBiomes.add(new CustomBiome(Biomes.WINDSWEPT_GRAVELLY_HILLS.location().toString(), 192, 192, 192, 255));
			this.customBiomes.add(new CustomBiome(Biomes.WINDSWEPT_FOREST.location().toString(), 160, 144, 98, 255));
			this.customBiomes.add(new CustomBiome(Biomes.WINDSWEPT_SAVANNA.location().toString(), 212, 156, 102, 255));
			this.customBiomes.add(new CustomBiome(Biomes.JUNGLE.location().toString(), 64, 248, 34, 255));
			this.customBiomes.add(new CustomBiome(Biomes.SPARSE_JUNGLE.location().toString(), 72, 198, 54, 255));
			this.customBiomes.add(new CustomBiome(Biomes.BAMBOO_JUNGLE.location().toString(), 128, 238, 114, 255));
			this.customBiomes.add(new CustomBiome(Biomes.BADLANDS.location().toString(), 252, 184, 56, 255));
			this.customBiomes.add(new CustomBiome(Biomes.ERODED_BADLANDS.location().toString(), 222, 128, 12, 255));
			this.customBiomes.add(new CustomBiome(Biomes.WOODED_BADLANDS.location().toString(), 182, 128, 64, 255));
			this.customBiomes.add(new CustomBiome(Biomes.MEADOW.location().toString(), 150, 252, 164, 255));
			this.customBiomes.add(new CustomBiome(Biomes.CHERRY_GROVE.location().toString(), 224, 186, 232, 255));
			this.customBiomes.add(new CustomBiome(Biomes.GROVE.location().toString(), 33, 150, 48, 255));
			this.customBiomes.add(new CustomBiome(Biomes.SNOWY_SLOPES.location().toString(), 222, 238, 250, 255));
			this.customBiomes.add(new CustomBiome(Biomes.FROZEN_PEAKS.location().toString(), 170, 178, 188, 255));
			this.customBiomes.add(new CustomBiome(Biomes.JAGGED_PEAKS.location().toString(), 208, 228, 254, 255));
			this.customBiomes.add(new CustomBiome(Biomes.STONY_PEAKS.location().toString(), 128, 128, 132, 255));
			this.customBiomes.add(new CustomBiome(Biomes.RIVER.location().toString(), 172, 172, 222, 255));
			this.customBiomes.add(new CustomBiome(Biomes.FROZEN_RIVER.location().toString(), 176, 188, 208, 255));
			this.customBiomes.add(new CustomBiome(Biomes.BEACH.location().toString(), 254, 248, 226, 255));
			this.customBiomes.add(new CustomBiome(Biomes.SNOWY_BEACH.location().toString(), 214, 208, 202, 255));
			this.customBiomes.add(new CustomBiome(Biomes.STONY_SHORE.location().toString(), 180, 178, 176, 255));
			this.customBiomes.add(new CustomBiome(Biomes.WARM_OCEAN.location().toString(), 67, 213, 238, 255));
			this.customBiomes.add(new CustomBiome(Biomes.LUKEWARM_OCEAN.location().toString(), 60, 151, 211, 255));
			this.customBiomes.add(new CustomBiome(Biomes.DEEP_LUKEWARM_OCEAN.location().toString(), 69, 173, 242, 255));
			this.customBiomes.add(new CustomBiome(Biomes.OCEAN.location().toString(), 55, 103, 199, 255));
			this.customBiomes.add(new CustomBiome(Biomes.DEEP_OCEAN.location().toString(), 63, 118, 228, 255));
			this.customBiomes.add(new CustomBiome(Biomes.COLD_OCEAN.location().toString(), 53, 76, 187, 255));
			this.customBiomes.add(new CustomBiome(Biomes.DEEP_COLD_OCEAN.location().toString(), 61, 87, 214, 255));
			this.customBiomes.add(new CustomBiome(Biomes.FROZEN_OCEAN.location().toString(), 49, 49, 175, 255));
			this.customBiomes.add(new CustomBiome(Biomes.DEEP_FROZEN_OCEAN.location().toString(), 57, 56, 201, 255));
			this.customBiomes.add(new CustomBiome(Biomes.MUSHROOM_FIELDS.location().toString(), 202, 102, 252, 255));
			this.customBiomes.add(new CustomBiome(Biomes.DRIPSTONE_CAVES.location().toString(), 70, 56, 8, 255));
			this.customBiomes.add(new CustomBiome(Biomes.LUSH_CAVES.location().toString(), 246, 114, 172, 255));
			this.customBiomes.add(new CustomBiome(Biomes.DEEP_DARK.location().toString(), 6, 56, 72, 255));
			this.customBiomes.add(new CustomBiome(Biomes.NETHER_WASTES.location().toString(), 246, 140, 114, 255));
			this.customBiomes.add(new CustomBiome(Biomes.WARPED_FOREST.location().toString(), 54, 254, 238, 255));
			this.customBiomes.add(new CustomBiome(Biomes.CRIMSON_FOREST.location().toString(), 254, 54, 94, 255));
			this.customBiomes.add(new CustomBiome(Biomes.SOUL_SAND_VALLEY.location().toString(), 126, 86, 22, 255));
			this.customBiomes.add(new CustomBiome(Biomes.BASALT_DELTAS.location().toString(), 80, 84, 84, 255));
			this.customBiomes.add(new CustomBiome(Biomes.THE_END.location().toString(), 16, 16, 16, 255));
			this.customBiomes.add(new CustomBiome(Biomes.END_HIGHLANDS.location().toString(), 250, 242, 178, 255));
			this.customBiomes.add(new CustomBiome(Biomes.END_MIDLANDS.location().toString(), 242, 204, 120, 255));
			this.customBiomes.add(new CustomBiome(Biomes.SMALL_END_ISLANDS.location().toString(), 238, 154, 138, 255));
			this.customBiomes.add(new CustomBiome(Biomes.END_BARRENS.location().toString(), 200, 166, 240, 255));
			this.customBiomes.add(new CustomBiome("emeraldcraft:azure_desert", 128, 154, 198, 255));
			this.customBiomes.add(new CustomBiome("emeraldcraft:jadeite_desert", 130, 200, 128, 255));
			this.customBiomes.add(new CustomBiome("emeraldcraft:ginkgo_forest", 210, 226, 92, 255));
			this.customBiomes.add(new CustomBiome("emeraldcraft:karst_hills", 104, 122, 132, 255));
			this.customBiomes.add(new CustomBiome("emeraldcraft:petunia_plains", 54, 210, 98, 255));
			this.customBiomes.add(new CustomBiome("emeraldcraft:xanadu", 12, 246, 40, 255));
			this.customBiomes.add(new CustomBiome("emeraldcraft:palm_beach", 134, 108, 72, 255));
			this.customBiomes.add(new CustomBiome("emeraldcraft:golden_beach", 148, 144, 62, 255));
			this.customBiomes.add(new CustomBiome("emeraldcraft:dead_crimson_ocean", 116, 32, 252, 255));
			this.customBiomes.add(new CustomBiome("emeraldcraft:dead_warped_ocean", 48, 236, 212, 255));
			this.customBiomes.add(new CustomBiome("emeraldcraft:deep_dead_crimson_ocean", 95, 11, 210, 255));
			this.customBiomes.add(new CustomBiome("emeraldcraft:deep_dead_warped_ocean", 33, 213, 188, 255));
			this.customBiomes.add(new CustomBiome("emeraldcraft:quartz_desert", 236, 234, 234, 255));
			this.customBiomes.add(new CustomBiome("emeraldcraft:emery_desert", 32, 32, 34, 255));
			this.customBiomes.add(new CustomBiome("emeraldcraft:purpuraceus_swamp", 154, 108, 180, 255));
		}
		this.saveConfig();
	}

	private void saveConfig() {
		try {
			FileOutputStream out = new FileOutputStream(this.file);
			JsonObject json = new JsonObject();
			json.addProperty("port", this.port);
			json.addProperty("multiThread", this.multiThread);
			JsonArray array = new JsonArray();
			if(this.customBiomes != null) {
				for (CustomBiome customBiome : this.customBiomes) {
					JsonObject biomeObject = new JsonObject();
					biomeObject.addProperty("id", customBiome.id.toString());
					biomeObject.addProperty("r", customBiome.r);
					biomeObject.addProperty("g", customBiome.g);
					biomeObject.addProperty("b", customBiome.b);
					biomeObject.addProperty("a", customBiome.a);
					array.add(biomeObject);
				}
			}
			json.add("CustomBiomes", array);
			Writer writer = new OutputStreamWriter(out);
			writeJsonToFile(writer, null, json, 0);
			writer.close();
			out.close();
		} catch (IOException e) {
			IngameBiomeMap.LOGGER.error(e.toString());
		}
	}

	public static void writeJsonToFile(Writer writer, @Nullable String key, JsonElement json, int tab) throws IOException {
		writer.write("\t".repeat(tab));
		if(key != null) {
			writer.write("\"" + key + "\": ");
		}
		if(json.isJsonObject()) {
			writer.write("{\n");
			boolean first = true;
			for(Map.Entry<String, JsonElement> entry: json.getAsJsonObject().entrySet()) {
				if(first) {
					first = false;
				} else {
					writer.write(",\n");
				}
				writeJsonToFile(writer, entry.getKey(), entry.getValue(), tab + 1);
			}
			writer.write("\n" + "\t".repeat(tab) + "}");
		} else if(json.isJsonArray()) {
			writer.write("[\n");
			boolean first = true;
			for (JsonElement element : json.getAsJsonArray()) {
				if (first) {
					first = false;
				} else {
					writer.write(",\n");
				}
				writeJsonToFile(writer, null, element, tab + 1);
			}
			writer.write("\n" + "\t".repeat(tab) + "]");
		} else if(json.isJsonPrimitive()) {
			JsonPrimitive jsonPrimitive = json.getAsJsonPrimitive();
			if(jsonPrimitive.isBoolean()) {
				writer.write(String.valueOf(jsonPrimitive.getAsBoolean()));
			} else if(jsonPrimitive.isNumber()) {
				writer.write(String.valueOf(jsonPrimitive.getAsNumber().intValue()));
			} else if(jsonPrimitive.isString()) {
				writer.write('\"' + jsonPrimitive.getAsString() + '\"');
			}
		}
	}

	@Nullable
	public Color getColorByBiome(ResourceLocation target) {
		if(this.customBiomes == null) {
			return null;
		}
		return this.biomeColors.get(target);
	}
	
	public boolean isMultiThread() {
		return Boolean.TRUE.equals(this.multiThread);
	}
}
