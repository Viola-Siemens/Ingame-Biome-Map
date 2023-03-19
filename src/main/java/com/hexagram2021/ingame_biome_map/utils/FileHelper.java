package com.hexagram2021.ingame_biome_map.utils;

import com.hexagram2021.ingame_biome_map.IngameBiomeMap;
import com.mojang.datafixers.util.Pair;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.data.BuiltinRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureSet;
import net.minecraftforge.registries.ForgeRegistries;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

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
			this.writeToFile(player.getLevel(), standPos);
			IngameBiomeMap.LOGGER.info("Successfully exported map with radius of " + this.RADIUS);
			player.sendSystemMessage(Component.translatable("info.export.success", this.file.toString()));
		}
	}

	private void writeToFile(ServerLevel level, BlockPos blockPos) throws IOException {
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
				Holder<Biome> biome = level.getBiome(current);
				image.setRGB(finalX, finalY, getColor(biome, duplicateUnknown).getRGB());
			}
		}

	//	features.forEach((feature) -> {
	//		Tuple<Integer, Integer> xy = feature.getA();
	//		placeFeatureToImage(xy.getA(), xy.getB(), feature.getB(), image);
	//	});

		ImageIO.write(image, "png", this.file);
	}

	// TODO: place structure icons on the map.
	private ResourceLocation hasFeature(StructureSet set, ServerLevel level, BlockPos blockPos) {
		int findRadius = (this.SCALE - 1) >> 4;
		HolderSet<Structure> holderSet = HolderSet.direct(
				set.structures().stream().map(StructureSet.StructureSelectionEntry::structure).toList()
		);
		Pair<BlockPos, Holder<Structure>> pair =
				level.getChunkSource().getGenerator().findNearestMapStructure(level, holderSet, blockPos, findRadius, false);
		if(pair == null){
			return null;
		}
		IngameBiomeMap.LOGGER.info(pair.getFirst().toString());
		return BuiltinRegistries.STRUCTURES.getKey(pair.getSecond().value());
	}

	private static void placeFeatureToImage(int x, int y, ResourceLocation feature, BufferedImage image) {
		if(feature == null) {
			return;
		}
		try {
			ResourceLocation icon = new ResourceLocation(feature.getNamespace(), "icons/" + feature.getPath() + ".png");
			Optional<Resource> icon_resource = Minecraft.getInstance().getResourceManager().getResource(icon);
			if(icon_resource.isPresent()) {
				BufferedImage source = ImageIO.read(icon_resource.get().open());
				placeFeatureIcon(x, y, source, image);
			}
		} catch (IOException e) {
			IngameBiomeMap.LOGGER.error(e.toString());
		}
	}
	private static void placeFeatureIcon(int x, int y, BufferedImage icon, BufferedImage image) {
		int wid2 = icon.getWidth() / 2;
		int hgt2 = icon.getHeight() / 2;
		for(int i = Math.max(0, x - wid2); i < Math.min(image.getWidth(), x + icon.getWidth() - wid2); ++i) {
			for(int j = Math.max(0, y - hgt2); j < Math.min(image.getHeight(), y + icon.getHeight() - hgt2); ++j) {
				int rgb = icon.getRGB(i + wid2, j + hgt2);
				if(rgb != 0) {
					image.setRGB(i, j, rgb);
				}
			}
		}
	}

	private static Color getColor(Holder<Biome> biome, Set<String> duplicateUnknown) {
		if(biome.is(Biomes.PLAINS)) {
			return new Color(180, 240, 28);
		}
		if(biome.is(Biomes.SUNFLOWER_PLAINS)) {
			return new Color(216, 240, 56);
		}
		if(biome.is(Biomes.SNOWY_PLAINS)) {
			return new Color(250, 250, 250);
		}
		if(biome.is(Biomes.ICE_SPIKES)) {
			return new Color(204, 204, 254);
		}
		if(biome.is(Biomes.DESERT)) {
			return new Color(244, 224, 170);
		}
		if(biome.is(Biomes.SWAMP)) {
			return new Color(114, 254, 234);
		}
		if(biome.is(Biomes.MANGROVE_SWAMP)) {
			return new Color(252, 208, 242);
		}
		if(biome.is(Biomes.FOREST)) {
			return new Color(156, 122, 96);
		}
		if(biome.is(Biomes.FLOWER_FOREST)) {
			return new Color(212, 164, 142);
		}
		if(biome.is(Biomes.BIRCH_FOREST)) {
			return new Color(252, 240, 220);
		}
		if(biome.is(Biomes.DARK_FOREST)) {
			return new Color(124, 116, 96);
		}
		if(biome.is(Biomes.OLD_GROWTH_BIRCH_FOREST)) {
			return new Color(222, 208, 184);
		}
		if(biome.is(Biomes.OLD_GROWTH_PINE_TAIGA)) {
			return new Color(44, 98, 32);
		}
		if(biome.is(Biomes.OLD_GROWTH_SPRUCE_TAIGA)) {
			return new Color(46, 84, 38);
		}
		if(biome.is(Biomes.TAIGA)) {
			return new Color(66, 120, 54);
		}
		if(biome.is(Biomes.SNOWY_TAIGA)) {
			return new Color(122, 156, 118);
		}
		if(biome.is(Biomes.SAVANNA)) {
			return new Color(254, 172, 124);
		}
		if(biome.is(Biomes.SAVANNA_PLATEAU)) {
			return new Color(194, 138, 104);
		}
		if(biome.is(Biomes.SAVANNA_PLATEAU)) {
			return new Color(150, 98, 74);
		}
		if(biome.is(Biomes.WINDSWEPT_HILLS)) {
			return new Color(122, 102, 172);
		}
		if(biome.is(Biomes.WINDSWEPT_GRAVELLY_HILLS)) {
			return new Color(192, 192, 192);
		}
		if(biome.is(Biomes.WINDSWEPT_FOREST)) {
			return new Color(160, 144, 98);
		}
		if(biome.is(Biomes.WINDSWEPT_SAVANNA)) {
			return new Color(212, 156, 102);
		}
		if(biome.is(Biomes.JUNGLE)) {
			return new Color(64, 248, 34);
		}
		if(biome.is(Biomes.SPARSE_JUNGLE)) {
			return new Color(72, 198, 54);
		}
		if(biome.is(Biomes.BAMBOO_JUNGLE)) {
			return new Color(128, 238, 114);
		}
		if(biome.is(Biomes.BADLANDS)) {
			return new Color(252, 184, 56);
		}
		if(biome.is(Biomes.ERODED_BADLANDS)) {
			return new Color(222, 128, 12);
		}
		if(biome.is(Biomes.WOODED_BADLANDS)) {
			return new Color(182, 128, 64);
		}
		if(biome.is(Biomes.MEADOW)) {
			return new Color(150, 252, 164);
		}
		if(biome.is(Biomes.GROVE)) {
			return new Color(33, 150, 48);
		}
		if(biome.is(Biomes.SNOWY_SLOPES)) {
			return new Color(222, 238, 250);
		}
		if(biome.is(Biomes.FROZEN_PEAKS)) {
			return new Color(170, 178, 188);
		}
		if(biome.is(Biomes.STONY_PEAKS)) {
			return new Color(128, 128, 132);
		}
		if(biome.is(Biomes.RIVER)) {
			return new Color(172, 172, 222);
		}
		if(biome.is(Biomes.FROZEN_RIVER)) {
			return new Color(176, 188, 208);
		}
		if(biome.is(Biomes.BEACH)) {
			return new Color(254, 248, 226);
		}
		if(biome.is(Biomes.SNOWY_BEACH)) {
			return new Color(214, 208, 202);
		}
		if(biome.is(Biomes.STONY_SHORE)) {
			return new Color(180, 178, 176);
		}
		if(biome.is(Biomes.WARM_OCEAN)) {
			return new Color(67, 213, 238);
		}
		if(biome.is(Biomes.LUKEWARM_OCEAN)) {
			return new Color(60, 151, 211);
		}
		if(biome.is(Biomes.DEEP_LUKEWARM_OCEAN)) {
			return new Color(69, 173, 242);
		}
		if(biome.is(Biomes.OCEAN)) {
			return new Color(55, 103, 199);
		}
		if(biome.is(Biomes.DEEP_OCEAN)) {
			return new Color(63, 118, 228);
		}
		if(biome.is(Biomes.COLD_OCEAN)) {
			return new Color(53, 76, 187);
		}
		if(biome.is(Biomes.DEEP_COLD_OCEAN)) {
			return new Color(61, 87, 214);
		}
		if(biome.is(Biomes.FROZEN_OCEAN)) {
			return new Color(49, 49, 175);
		}
		if(biome.is(Biomes.DEEP_FROZEN_OCEAN)) {
			return new Color(57, 56, 201);
		}
		if(biome.is(Biomes.MUSHROOM_FIELDS)) {
			return new Color(202, 102, 252);
		}
		if(biome.is(Biomes.DRIPSTONE_CAVES)) {
			return new Color(70, 56, 8);
		}
		if(biome.is(Biomes.LUSH_CAVES)) {
			return new Color(246, 114, 172);
		}
		if(biome.is(Biomes.DEEP_DARK)) {
			return new Color(6, 56, 72);
		}
		if(biome.is(Biomes.NETHER_WASTES)) {
			return new Color(246, 140, 114);
		}
		if(biome.is(Biomes.WARPED_FOREST)) {
			return new Color(54, 254, 238);
		}
		if(biome.is(Biomes.CRIMSON_FOREST)) {
			return new Color(254, 54, 94);
		}
		if(biome.is(Biomes.SOUL_SAND_VALLEY)) {
			return new Color(126, 86, 22);
		}
		if(biome.is(Biomes.BASALT_DELTAS)) {
			return new Color(80, 84, 84);
		}
		if(biome.is(Biomes.THE_END)) {
			return new Color(16, 16, 16);
		}
		if(biome.is(Biomes.END_HIGHLANDS)) {
			return new Color(250, 242, 178);
		}
		if(biome.is(Biomes.END_MIDLANDS)) {
			return new Color(242, 204, 120);
		}
		if(biome.is(Biomes.SMALL_END_ISLANDS)) {
			return new Color(238, 154, 138);
		}
		if(biome.is(Biomes.END_BARRENS)) {
			return new Color(200, 166, 240);
		}
		Color customColor = IngameBiomeMap.config.getColorByBiome(biome);
		if (customColor != null) {
			return customColor;
		}
		String biomeName = biome.unwrap().map(ResourceKey::toString, b -> Objects.requireNonNull(ForgeRegistries.BIOMES.getKey(b)).toString());
		if (!duplicateUnknown.contains(biomeName)) {
			IngameBiomeMap.LOGGER.info("Failed to match " + biomeName);
			duplicateUnknown.add(biomeName);
		}

		return new Color(0, 0, 0, 0);
	}
}
