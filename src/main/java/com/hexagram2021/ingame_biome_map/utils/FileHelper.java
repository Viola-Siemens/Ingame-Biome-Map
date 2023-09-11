package com.hexagram2021.ingame_biome_map.utils;

import com.hexagram2021.ingame_biome_map.IngameBiomeMap;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Tuple;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.chunk.ChunkStatus;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraftforge.registries.ForgeRegistries;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.*;

public class FileHelper {
	public final File filePath = new File("./BiomeMap/");
	private final File file;
	private final int RADIUS;
	private final int SCALE;

	public FileHelper(ServerPlayer player, BlockPos standPos, Integer radius, Integer scale) throws IOException {
		this.RADIUS = radius;
		this.SCALE = scale;
		this.file = new File(this.filePath + "/[" + player.getDisplayName().getString() + "]" + LocalDateTime.now().toString().replaceAll(":", "_") + "(" + radius + ").png");
		if (!this.filePath.exists() && !this.filePath.mkdir()) {
			IngameBiomeMap.LOGGER.error("Could not mkdir " + this.filePath);
		} else if (!this.file.exists() && !this.file.createNewFile()) {
			IngameBiomeMap.LOGGER.error("Could not create new file " + this.file);
		} else {
			IngameBiomeMap.LOGGER.info("Exporting map with radius of " + this.RADIUS);
			this.writeToFile(player.serverLevel(), standPos);
			IngameBiomeMap.LOGGER.info("Successfully exported map with radius of " + this.RADIUS);
			player.sendSystemMessage(Component.translatable("info.export.success", this.file.toString()));
		}
	}

	private void writeToFile(ServerLevel level, BlockPos blockPos) throws IOException {
		int range = this.RADIUS / this.SCALE;
		BufferedImage image = new BufferedImage(range * 2 + 1, range * 2 + 1, BufferedImage.TYPE_4BYTE_ABGR);

		List<Tuple<Tuple<Integer, Integer>, ResourceLocation>> features = new ArrayList<>();
		Registry<Structure> structureRegistry = level.registryAccess().registryOrThrow(Registries.STRUCTURE);

		Set<String> duplicateUnknown = new HashSet<>();
		for(int i = -range; i <= range; ++i) {
			for(int j = -range; j <= range; ++j) {
				int finalX = range + i;
				int finalY = range + j;
				BlockPos current = blockPos.offset(i * this.SCALE, 0, j * this.SCALE);
				
				if(IngameBiomeMap.config.drawStructures()) {
					if (this.SCALE < 16) {
						int inChunkX = current.getX() & 15;
						int inChunkZ = current.getZ() & 15;
						if (Math.abs(8 - inChunkX) < Math.abs(8 - inChunkX - this.SCALE) &&
								Math.abs(8 - inChunkX) <= Math.abs(8 - inChunkX + this.SCALE) &&
								Math.abs(8 - inChunkZ) < Math.abs(8 - inChunkZ - this.SCALE) &&
								Math.abs(8 - inChunkZ) <= Math.abs(8 - inChunkZ + this.SCALE)) {
							chunkStructures(level, structureRegistry, new ChunkPos(current)).forEach(rl -> features.add(new Tuple<>(new Tuple<>(finalX, finalY), rl)));
						}
					} else {
						int preChunkX = (current.getX() - this.SCALE) >> 4;
						int preChunkZ = (current.getZ() - this.SCALE) >> 4;
						int chunkX = current.getX() >> 4;
						int chunkZ = current.getZ() >> 4;
						int nextChunkX = (current.getX() + this.SCALE) >> 4;
						int nextChunkZ = (current.getZ() + this.SCALE) >> 4;
						for (int di = ((preChunkX + chunkX) >> 1) + 1; di <= (chunkX + nextChunkX) >> 1; ++di) {
							for (int dj = ((preChunkZ + chunkZ) >> 1) + 1; dj <= (chunkZ + nextChunkZ) >> 1; ++dj) {
								chunkStructures(level, structureRegistry, new ChunkPos(di, dj)).forEach(rl -> features.add(new Tuple<>(new Tuple<>(finalX, finalY), rl)));
							}
						}
					}
				}
				
				Holder<Biome> biome = level.getBiome(current);
				image.setRGB(finalX, finalY, getColor(biome, duplicateUnknown).getRGB());
			}
		}
		
		if(IngameBiomeMap.config.drawStructures()) {
			IngameBiomeMap.LOGGER.info("Found " + features.size() + " structures.");
			features.forEach(feature -> {
				Tuple<Integer, Integer> xy = feature.getA();
				placeFeatureToImage(xy.getA(), xy.getB(), feature.getB(), image, duplicateUnknown);
			});
		}

		ImageIO.write(image, "png", this.file);
	}

	// TODO: place structure icons on the map.
	private List<ResourceLocation> chunkStructures(ServerLevel level, Registry<Structure> structureRegistry, ChunkPos chunkPos) {
		return level.getChunk(chunkPos.x, chunkPos.z, ChunkStatus.STRUCTURE_STARTS).getAllStarts().keySet().stream()
				.map(structureRegistry::getKey).filter(Objects::nonNull).toList();
	}

	private static void placeFeatureToImage(int x, int y, ResourceLocation feature, BufferedImage image, Set<String> duplicateUnknown) {
		BufferedImage source = IngameBiomeMap.iconManager.getImageByStructureId(feature);
		if(source == null) {
			String featureName = feature.toString();
			if (!duplicateUnknown.contains(featureName)) {
				IngameBiomeMap.LOGGER.info("Failed to match " + featureName);
				duplicateUnknown.add(featureName);
			}
			return;
		}
		placeFeatureIcon(x, y, source, image);
	}
	private static void placeFeatureIcon(int x, int y, BufferedImage icon, BufferedImage image) {
		int wid2 = icon.getWidth() / 2;
		int hgt2 = icon.getHeight() / 2;
		int maxi = Math.min(image.getWidth(), x + icon.getWidth() - wid2);
		int maxj = Math.min(image.getHeight(), y + icon.getHeight() - hgt2);
		for(int i = Math.max(0, x - wid2); i < maxi; ++i) {
			for(int j = Math.max(0, y - hgt2); j < maxj; ++j) {
				int rgba = icon.getRGB(i - x + wid2, j - y + hgt2);
				int rgb = rgba & 0xffffff;
				int a = (rgba >> 24) & 0xff;
				if(a >= 100) {
					image.setRGB(i, j, rgb);
				}
			}
		}
	}

	private static Color getColor(Holder<Biome> biome, Set<String> duplicateUnknown) {
		ResourceLocation biomeId = biome.unwrap().map(ResourceKey::location, b -> Objects.requireNonNull(ForgeRegistries.BIOMES.getKey(b)));
		Color customColor = IngameBiomeMap.config.getColorByBiome(biomeId);
		if (customColor != null) {
			return customColor;
		}
		String biomeName = biomeId.toString();
		if (!duplicateUnknown.contains(biomeName)) {
			IngameBiomeMap.LOGGER.info("Failed to match " + biomeName);
			duplicateUnknown.add(biomeName);
		}

		return new Color(0, 0, 0, 0);
	}
}
