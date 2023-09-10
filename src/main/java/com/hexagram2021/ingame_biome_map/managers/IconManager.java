package com.hexagram2021.ingame_biome_map.managers;

import com.google.common.collect.ImmutableMap;
import com.google.gson.*;
import com.hexagram2021.ingame_biome_map.IngameBiomeMap;
import net.minecraft.ResourceLocationException;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.profiling.ProfilerFiller;

import javax.annotation.Nullable;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Base64;
import java.util.Map;

public class IconManager extends SimpleJsonResourceReloadListener {
	private static final Gson GSON = (new GsonBuilder()).create();
	
	private static final Base64.Decoder base64Decoder = Base64.getDecoder();
	
	private Map<ResourceLocation, BufferedImage> icons = ImmutableMap.of();
	
	public IconManager() {
		super(GSON, "ibm/map_icons");
	}
	
	@Override
	protected void apply(Map<ResourceLocation, JsonElement> missions, ResourceManager resourceManager, ProfilerFiller profilerFiller) {
		ImmutableMap.Builder<ResourceLocation, BufferedImage> builder = ImmutableMap.builder();
		for(Map.Entry<ResourceLocation, JsonElement> entry: missions.entrySet()) {
			ResourceLocation id = entry.getKey();
			if (id.getPath().startsWith("_")) {
				continue;
			}
			
			try {
				JsonObject jsonObject = GsonHelper.convertToJsonObject(entry.getValue(), "top element");
				ResourceLocation iconId = new ResourceLocation(GsonHelper.getAsString(jsonObject, "id"));
				byte[] base64 = base64Decoder.decode(GsonHelper.getAsString(jsonObject, "base64"));
				ByteArrayInputStream inputStream = new ByteArrayInputStream(base64);
				builder.put(iconId, ImageIO.read(inputStream));
			} catch (IllegalArgumentException | JsonParseException exception) {
				IngameBiomeMap.LOGGER.error("Parsing error loading icon %s.".formatted(id), exception);
			} catch (ResourceLocationException exception) {
				IngameBiomeMap.LOGGER.error("Invalid resource location when parsing %s.".formatted(id), exception);
			} catch (IOException exception) {
				IngameBiomeMap.LOGGER.error("IO Error when parsing %s.".formatted(id), exception);
			}
		}
		this.icons = builder.build();
		IngameBiomeMap.LOGGER.info("Loaded {} icons", this.icons.size());
	}
	
	@Nullable
	public BufferedImage getImageByStructureId(ResourceLocation id) {
		return this.icons.get(id);
	}
}
