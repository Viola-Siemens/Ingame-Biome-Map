package com.hexagram2021.ingame_biome_map.utils;

import com.hexagram2021.ingame_biome_map.IngameBiomeMap;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Util;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.Biomes;
import net.minecraft.world.server.ServerWorld;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

public class FileHelper {
	public final File filePath = new File("./BiomeMap/");
	private final File file;
	private final int RADIUS;
	private final int SCALE;

	public FileHelper(ServerPlayerEntity player, BlockPos standPos, Integer radius, Integer scale) throws IOException {
		this.RADIUS = radius;
		this.SCALE = scale;
		this.file = new File(this.filePath + "/[" + player.getDisplayName().getString() + "]" + LocalDateTime.now().toString().replaceAll(":", "_") + "(" + radius + ").png");
		if (!this.filePath.exists() && !this.filePath.mkdir()) {
			IngameBiomeMap.LOGGER.error("Could not mkdir " + this.filePath);
		} else if (!this.file.exists() && !this.file.createNewFile()) {
			IngameBiomeMap.LOGGER.error("Could not create new file " + this.file);
		} else {
			IngameBiomeMap.LOGGER.info("Exporting map with radius of " + this.RADIUS);
			this.writeToFile(player.getLevel(), standPos);
			IngameBiomeMap.LOGGER.info("Successfully exported map with radius of " + this.RADIUS);
			player.sendMessage(new TranslationTextComponent("info.export.success", this.file.toString()), Util.NIL_UUID);
		}
	}

	private void writeToFile(ServerWorld level, BlockPos blockPos) throws IOException {
		int range = this.RADIUS / this.SCALE;
		BufferedImage image = new BufferedImage(range * 2 + 1, range * 2 + 1, BufferedImage.TYPE_4BYTE_ABGR);

	//	List<Tuple<Tuple<Integer, Integer>, ResourceLocation>> features = new ArrayList<>();

		Set<String> duplicateUnknown = new HashSet<>();
		for(int i = -range; i <= range; ++i) {
			for(int j = -range; j <= range; ++j) {
				int finalX = range + i;
				int finalY = range + j;
				BlockPos current = blockPos.offset(i * this.SCALE, 0, j * this.SCALE);
			//	int inChunkX = current.getX() & 15;
			//	int inChunkZ = current.getZ() & 15;
			//	if(Math.abs(8 - inChunkX) < Math.abs(8 - inChunkX - this.SCALE) &&
			//			Math.abs(8 - inChunkX) <= Math.abs(8 - inChunkX + this.SCALE) &&
			//			Math.abs(8 - inChunkZ) < Math.abs(8 - inChunkZ - this.SCALE) &&
			//			Math.abs(8 - inChunkZ) <= Math.abs(8 - inChunkZ + this.SCALE)) {
			//		BuiltinRegistries.STRUCTURE_SETS.stream().forEach((set) -> {
			//			ResourceLocation featureKey = this.hasFeature(set, level, current);
			//			if(featureKey != null) {
			//				features.add(new Tuple<>(new Tuple<>(finalX, finalY), featureKey));
			//			}
			//		});
			//	}
				Biome biome = level.getBiome(current);
				image.setRGB(finalX, finalY, getColor(biome, duplicateUnknown).getRGB());
			}
		}

	//	features.forEach((feature) -> {
	//		Tuple<Integer, Integer> xy = feature.getA();
	//		placeFeatureToImage(xy.getA(), xy.getB(), feature.getB(), image);
	//	});

		ImageIO.write(image, "png", this.file);
	}

	private static Color getColor(Biome biome, Set<String> duplicateUnknown) {
		ResourceLocation biomeRegistryName = biome.getRegistryName();

		if(biomeRegistryName == null) {
			IngameBiomeMap.LOGGER.info("Biome " + biome + " has no registry name!");
			return new Color(0, 0, 0, 0);
		}

		String biomeId = biomeRegistryName.toString();
		Color customColor = IngameBiomeMap.config.getColorById(biomeId);
		if(customColor != null) {
			return customColor;
		}

		if(Biomes.PLAINS.location().equals(biomeRegistryName)) {
			return new Color(180, 240, 28);
		}
		if(Biomes.MOUNTAINS.location().equals(biomeRegistryName)) {
			return new Color(126, 156, 32);
		}
		if(Biomes.MOUNTAIN_EDGE.location().equals(biomeRegistryName)) {
			return new Color(162, 188, 92);
		}
		if(Biomes.SUNFLOWER_PLAINS.location().equals(biomeRegistryName)) {
			return new Color(216, 242, 56);
		}
		if(Biomes.SNOWY_TUNDRA.location().equals(biomeRegistryName)) {
			return new Color(250, 250, 250);
		}
		if(Biomes.SNOWY_MOUNTAINS.location().equals(biomeRegistryName)) {
			return new Color(148, 148, 148);
		}
		if(Biomes.ICE_SPIKES.location().equals(biomeRegistryName)) {
			return new Color(204, 204, 254);
		}
		if(Biomes.DESERT.location().equals(biomeRegistryName)) {
			return new Color(244, 224, 170);
		}
		if(Biomes.DESERT_LAKES.location().equals(biomeRegistryName)) {
			return new Color(182, 184, 220);
		}
		if(Biomes.DESERT_HILLS.location().equals(biomeRegistryName)) {
			return new Color(206, 190, 148);
		}
		if(Biomes.SWAMP.location().equals(biomeRegistryName)) {
			return new Color(114, 254, 234);
		}
		if(Biomes.SWAMP_HILLS.location().equals(biomeRegistryName)) {
			return new Color(118, 208, 194);
		}
		if(Biomes.FOREST.location().equals(biomeRegistryName)) {
			return new Color(156, 122, 96);
		}
		if(Biomes.WOODED_HILLS.location().equals(biomeRegistryName)) {
			return new Color(128, 104, 88);
		}
		if(Biomes.FLOWER_FOREST.location().equals(biomeRegistryName)) {
			return new Color(212, 164, 142);
		}
		if(Biomes.BIRCH_FOREST.location().equals(biomeRegistryName)) {
			return new Color(252, 240, 220);
		}
		if(Biomes.BIRCH_FOREST_HILLS.location().equals(biomeRegistryName)) {
			return new Color(212, 204, 192);
		}
		if(Biomes.DARK_FOREST.location().equals(biomeRegistryName)) {
			return new Color(124, 116, 96);
		}
		if(Biomes.DARK_FOREST_HILLS.location().equals(biomeRegistryName)) {
			return new Color(104, 100, 92);
		}
		if(Biomes.TALL_BIRCH_FOREST.location().equals(biomeRegistryName)) {
			return new Color(224, 208, 184);
		}
		if(Biomes.TALL_BIRCH_HILLS.location().equals(biomeRegistryName)) {
			return new Color(188, 178, 162);
		}
		if(Biomes.GIANT_TREE_TAIGA.location().equals(biomeRegistryName)) {
			return new Color(44, 98, 32);
		}
		if(Biomes.GIANT_TREE_TAIGA_HILLS.location().equals(biomeRegistryName)) {
			return new Color(50, 100, 42);
		}
		if(Biomes.GIANT_SPRUCE_TAIGA.location().equals(biomeRegistryName)) {
			return new Color(46, 84, 38);
		}
		if(Biomes.GIANT_SPRUCE_TAIGA_HILLS.location().equals(biomeRegistryName)) {
			return new Color(34, 72, 26);
		}
		if(Biomes.TAIGA.location().equals(biomeRegistryName)) {
			return new Color(66, 120, 54);
		}
		if(Biomes.TAIGA_HILLS.location().equals(biomeRegistryName)) {
			return new Color(56, 94, 48);
		}
		if(Biomes.TAIGA_MOUNTAINS.location().equals(biomeRegistryName)) {
			return new Color(54, 86, 46);
		}
		if(Biomes.SNOWY_TAIGA.location().equals(biomeRegistryName)) {
			return new Color(122, 156, 118);
		}
		if(Biomes.SNOWY_TAIGA_HILLS.location().equals(biomeRegistryName)) {
			return new Color(108, 132, 106);
		}
		if(Biomes.SNOWY_TAIGA_MOUNTAINS.location().equals(biomeRegistryName)) {
			return new Color(80, 104, 78);
		}
		if(Biomes.SAVANNA.location().equals(biomeRegistryName)) {
			return new Color(254, 172, 124);
		}
		if(Biomes.SAVANNA_PLATEAU.location().equals(biomeRegistryName)) {
			return new Color(150, 98, 74);
		}
		if(Biomes.SHATTERED_SAVANNA.location().equals(biomeRegistryName)) {
			return new Color(212, 156, 102);
		}
		if(Biomes.SHATTERED_SAVANNA_PLATEAU.location().equals(biomeRegistryName)) {
			return new Color(144, 112, 72);
		}
		if(Biomes.GRAVELLY_MOUNTAINS.location().equals(biomeRegistryName)) {
			return new Color(192, 192, 192);
		}
		if(Biomes.MODIFIED_GRAVELLY_MOUNTAINS.location().equals(biomeRegistryName)) {
			return new Color(204, 198, 198);
		}
		if(Biomes.WOODED_MOUNTAINS.location().equals(biomeRegistryName)) {
			return new Color(160, 144, 98);
		}
		if(Biomes.JUNGLE.location().equals(biomeRegistryName)) {
			return new Color(64, 248, 34);
		}
		if(Biomes.JUNGLE_HILLS.location().equals(biomeRegistryName)) {
			return new Color(96, 224, 42);
		}
		if(Biomes.JUNGLE_EDGE.location().equals(biomeRegistryName)) {
			return new Color(72, 198, 54);
		}
		if(Biomes.BAMBOO_JUNGLE.location().equals(biomeRegistryName)) {
			return new Color(128, 238, 114);
		}
		if(Biomes.BAMBOO_JUNGLE_HILLS.location().equals(biomeRegistryName)) {
			return new Color(124, 204, 110);
		}
		if(Biomes.MODIFIED_JUNGLE.location().equals(biomeRegistryName)) {
			return new Color(128, 196, 30);
		}
		if(Biomes.MODIFIED_JUNGLE_EDGE.location().equals(biomeRegistryName)) {
			return new Color(108, 158, 38);
		}
		if(Biomes.BADLANDS.location().equals(biomeRegistryName)) {
			return new Color(252, 184, 56);
		}
		if(Biomes.BADLANDS_PLATEAU.location().equals(biomeRegistryName)) {
			return new Color(216, 170, 82);
		}
		if(Biomes.MODIFIED_BADLANDS_PLATEAU.location().equals(biomeRegistryName)) {
			return new Color(194, 148, 62);
		}
		if(Biomes.ERODED_BADLANDS.location().equals(biomeRegistryName)) {
			return new Color(222, 128, 12);
		}
		if(Biomes.WOODED_BADLANDS_PLATEAU.location().equals(biomeRegistryName)) {
			return new Color(182, 128, 64);
		}
		if(Biomes.MODIFIED_WOODED_BADLANDS_PLATEAU.location().equals(biomeRegistryName)) {
			return new Color(156, 108, 48);
		}
		if(Biomes.RIVER.location().equals(biomeRegistryName)) {
			return new Color(172, 172, 222);
		}
		if(Biomes.FROZEN_RIVER.location().equals(biomeRegistryName)) {
			return new Color(176, 188, 208);
		}
		if(Biomes.BEACH.location().equals(biomeRegistryName)) {
			return new Color(254, 248, 226);
		}
		if(Biomes.SNOWY_BEACH.location().equals(biomeRegistryName)) {
			return new Color(214, 208, 202);
		}
		if(Biomes.STONE_SHORE.location().equals(biomeRegistryName)) {
			return new Color(180, 178, 176);
		}
		if(Biomes.WARM_OCEAN.location().equals(biomeRegistryName)) {
			return new Color(67, 213, 238);
		}
		if(Biomes.DEEP_WARM_OCEAN.location().equals(biomeRegistryName)) {
			return new Color(45, 220, 250);
		}
		if(Biomes.LUKEWARM_OCEAN.location().equals(biomeRegistryName)) {
			return new Color(60, 151, 211);
		}
		if(Biomes.DEEP_LUKEWARM_OCEAN.location().equals(biomeRegistryName)) {
			return new Color(69, 173, 242);
		}
		if(Biomes.OCEAN.location().equals(biomeRegistryName)) {
			return new Color(55, 103, 199);
		}
		if(Biomes.DEEP_OCEAN.location().equals(biomeRegistryName)) {
			return new Color(63, 118, 228);
		}
		if(Biomes.COLD_OCEAN.location().equals(biomeRegistryName)) {
			return new Color(53, 76, 187);
		}
		if(Biomes.DEEP_COLD_OCEAN.location().equals(biomeRegistryName)) {
			return new Color(61, 87, 214);
		}
		if(Biomes.FROZEN_OCEAN.location().equals(biomeRegistryName)) {
			return new Color(49, 49, 175);
		}
		if(Biomes.DEEP_FROZEN_OCEAN.location().equals(biomeRegistryName)) {
			return new Color(57, 56, 201);
		}
		if(Biomes.MUSHROOM_FIELDS.location().equals(biomeRegistryName)) {
			return new Color(202, 102, 252);
		}
		if(Biomes.MUSHROOM_FIELD_SHORE.location().equals(biomeRegistryName)) {
			return new Color(156, 116, 190);
		}
		if(Biomes.NETHER_WASTES.location().equals(biomeRegistryName)) {
			return new Color(246, 140, 114);
		}
		if(Biomes.WARPED_FOREST.location().equals(biomeRegistryName)) {
			return new Color(54, 254, 238);
		}
		if(Biomes.CRIMSON_FOREST.location().equals(biomeRegistryName)) {
			return new Color(254, 54, 94);
		}
		if(Biomes.SOUL_SAND_VALLEY.location().equals(biomeRegistryName)) {
			return new Color(126, 86, 22);
		}
		if(Biomes.BASALT_DELTAS.location().equals(biomeRegistryName)) {
			return new Color(80, 84, 84);
		}
		if(Biomes.THE_END.location().equals(biomeRegistryName)) {
			return new Color(232, 226, 174);
		}
		if(Biomes.THE_VOID.location().equals(biomeRegistryName)) {
			return new Color(16, 16, 16);
		}
		if(Biomes.END_HIGHLANDS.location().equals(biomeRegistryName)) {
			return new Color(250, 242, 178);
		}
		if(Biomes.END_MIDLANDS.location().equals(biomeRegistryName)) {
			return new Color(242, 204, 120);
		}
		if(Biomes.SMALL_END_ISLANDS.location().equals(biomeRegistryName)) {
			return new Color(238, 154, 138);
		}
		if(Biomes.END_BARRENS.location().equals(biomeRegistryName)) {
			return new Color(200, 166, 240);
		}
		if(!duplicateUnknown.contains(biomeId)) {
			IngameBiomeMap.LOGGER.info("Failed to match " + biomeId);
			duplicateUnknown.add(biomeId);
		}
		return new Color(0, 0, 0, 0);
	}
}
