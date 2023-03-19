package com.hexagram2021.ingame_biome_map.utils;

import com.google.gson.*;
import com.hexagram2021.ingame_biome_map.IngameBiomeMap;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.biome.Biome;

import java.awt.*;
import java.io.*;
import java.util.*;
import java.util.List;

public class ConfigHelper {
	public final File filePath = new File("./config/");
	private final File file;

	public static class CustomBiome {
		final ResourceLocation id;
		final int r, g, b, a;

		public CustomBiome(String id, int r, int g, int b, int a) {
			this.id = ResourceLocation.tryParse(id);
			this.r = r & 255;
			this.g = g & 255;
			this.b = b & 255;
			this.a = a & 255;
		}

		Color getColor() {
			return new Color(this.r, this.g, this.b, this.a);
		}
	}

	private Integer port = null;
	private List<CustomBiome> customBiomes = null;

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
	}

	private void loadFromJson(JsonObject json) {
		if(json.has("port")) {
			this.port = json.get("port").getAsInt();
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
		if(this.customBiomes == null) {
			this.customBiomes = new ArrayList<>();
		}
		this.saveConfig();
	}

	private void saveConfig() {
		try {
			FileOutputStream out = new FileOutputStream(this.file);
			JsonObject json = new JsonObject();
			json.addProperty("port", this.port);
			JsonArray array = new JsonArray();
			for(CustomBiome customBiome: this.customBiomes) {
				JsonObject biomeObject = new JsonObject();
				biomeObject.addProperty("id", customBiome.id.toString());
				biomeObject.addProperty("r", customBiome.r);
				biomeObject.addProperty("g", customBiome.g);
				biomeObject.addProperty("b", customBiome.b);
				biomeObject.addProperty("a", customBiome.a);
				array.add(biomeObject);
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

	public static void writeJsonToFile(Writer writer, String key, JsonElement json, int tab) throws IOException {
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

	public Color getColorByBiome(Holder<Biome> target) {
		List<CustomBiome> matchedList = this.customBiomes.stream().filter((customBiome) -> target.is(customBiome.id)).toList();
		if(matchedList.size() == 0) return null;
		return matchedList.get(0).getColor();
	}
}
