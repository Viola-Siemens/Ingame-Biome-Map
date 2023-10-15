package com.hexagram2021.ingame_biome_map.commands;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.hexagram2021.ingame_biome_map.IngameBiomeMap;
import com.hexagram2021.ingame_biome_map.utils.ConfigHelper;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.Dynamic2CommandExceptionType;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.ResourceOrTagArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.fml.loading.FMLLoader;

import java.io.*;

public class ResourceExportCommand {
	public static final File filePath = new File("./ResourceExport/");
	
	private static final String BLOCK_ARGUMENT = "block";
	private static final String CHUNK_COUNT_ARGUMENT = "chunk";
	private static final String MIN_HEIGHT_ARGUMENT = "minHeightInclusive";
	private static final String MAX_HEIGHT_ARGUMENT = "maxHeightExclusive";
	private static final String BIOME_ARGUMENT = "biome";
	private static final String REPEAT_ARGUMENT = "multiProcessor";
	
	private static int taskId = 0;
	
	private static String defaultBiomeTag(ServerLevel level) {
		ResourceLocation id = level.dimension().location();
		return id.getNamespace() + ":is_" + id.getPath();
	}
	
	public static LiteralArgumentBuilder<CommandSourceStack> register(CommandBuildContext buildContext) {
		return Commands.literal("exportresource")
				.requires(stack -> stack.hasPermission(4))
				.then(
						Commands.argument(BLOCK_ARGUMENT, ResourceOrTagArgument.resourceOrTag(buildContext, Registries.BLOCK))
								.executes(context -> exportResourceWithoutBiome(
										context,
										context.getSource().getLevel(),
										ResourceOrTagArgument.getResourceOrTag(context, BLOCK_ARGUMENT, Registries.BLOCK),
										1024,
										-63, 64, 1
								))
								.then(
										Commands.argument(CHUNK_COUNT_ARGUMENT, IntegerArgumentType.integer(4, 65536))
												.executes(context -> exportResourceWithoutBiome(
														context,
														context.getSource().getLevel(),
														ResourceOrTagArgument.getResourceOrTag(context, BLOCK_ARGUMENT, Registries.BLOCK),
														IntegerArgumentType.getInteger(context, CHUNK_COUNT_ARGUMENT),
														-63, 64, 1
												))
												.then(
														Commands.argument(MIN_HEIGHT_ARGUMENT, IntegerArgumentType.integer(-64, 320))
																.then(
																		Commands.argument(MAX_HEIGHT_ARGUMENT, IntegerArgumentType.integer(-64, 320))
																				.executes(context -> exportResourceWithoutBiome(
																						context,
																						context.getSource().getLevel(),
																						ResourceOrTagArgument.getResourceOrTag(context, BLOCK_ARGUMENT, Registries.BLOCK),
																						IntegerArgumentType.getInteger(context, CHUNK_COUNT_ARGUMENT),
																						IntegerArgumentType.getInteger(context, MIN_HEIGHT_ARGUMENT),
																						IntegerArgumentType.getInteger(context, MAX_HEIGHT_ARGUMENT),
																						1
																				))
																				.then(
																						Commands.argument(REPEAT_ARGUMENT, IntegerArgumentType.integer(1, 64))
																								.executes(context -> exportResourceWithoutBiome(
																										context,
																										context.getSource().getLevel(),
																										ResourceOrTagArgument.getResourceOrTag(context, BLOCK_ARGUMENT, Registries.BLOCK),
																										IntegerArgumentType.getInteger(context, CHUNK_COUNT_ARGUMENT),
																										IntegerArgumentType.getInteger(context, MIN_HEIGHT_ARGUMENT),
																										IntegerArgumentType.getInteger(context, MAX_HEIGHT_ARGUMENT),
																										IntegerArgumentType.getInteger(context, REPEAT_ARGUMENT)
																								))
																				)
																)
												)
								)
				);
	}
	
	private static final Dynamic2CommandExceptionType INVALID_HEIGHT_PARAMETER = new Dynamic2CommandExceptionType(
			(min, max) -> Component.translatable("commands.height.invalid", min, max)
	);
	
	public static int exportResourceWithoutBiome(CommandContext<CommandSourceStack> context, ServerLevel level, ResourceOrTagArgument.Result<Block> block,
												 int chunk, int minHeightInclusive, int maxHeightExclusive, int repeat) throws CommandSyntaxException {
		if(maxHeightExclusive <= minHeightInclusive) {
			throw INVALID_HEIGHT_PARAMETER.create(minHeightInclusive, maxHeightExclusive);
		}
		if (!filePath.exists() && !filePath.mkdir()) {
			IngameBiomeMap.LOGGER.error("Could not mkdir " + filePath);
			context.getSource().sendSuccess(() -> Component.translatable("info.ibm.resource.failure"), true);
			return Command.SINGLE_SUCCESS;
		}
		
		File file = new File(filePath + "/" + block.asPrintable().replaceAll("/", "_").replaceAll(":", "_") + "(" + chunk + ")-" + taskId + ".json");
		taskId += 1;
		try {
			if (!file.exists() && !file.createNewFile()) {
				IngameBiomeMap.LOGGER.error("Could not create new file " + file);
			} else {
				IngameBiomeMap.LOGGER.info("Exporting resource for " + block.asPrintable() + " in " + chunk + " chunks.");
				context.getSource().sendSuccess(() -> Component.translatable("info.ibm.exporting"), true);
				JsonObject[] results = new JsonObject[repeat];
				if (IngameBiomeMap.config.isMultiThread()) {
					new Thread(() -> {
						Thread[] threads = new Thread[repeat];
						for(int r = 0; r < repeat; ++r) {
							int finalR = r;
							threads[r] = new Thread(() -> {
								results[finalR] = new JsonObject();
								try {
									threadTask(results[finalR], level, block, chunk, minHeightInclusive, maxHeightExclusive);
								} catch (Exception e) {
									context.getSource().sendSuccess(() -> Component.translatable("info.ibm.resource.failure"), true);
									IngameBiomeMap.LOGGER.error("Error exporting resource.", e);
								}
							});
							threads[r].start();
						}
						for(int r = 0; r < repeat; ++r) {
							try {
								threads[r].join();
							} catch (InterruptedException e) {
								context.getSource().sendSuccess(() -> Component.translatable("info.ibm.resource.failure"), true);
								IngameBiomeMap.LOGGER.error("Error exporting resource.", e);
							}
						}
						output(
								context, file, block.asPrintable(),
								level.dimension().location().toString(), defaultBiomeTag(level), level.getSeed(),
								minHeightInclusive, maxHeightExclusive,
								results
						);
					}).start();
				} else {
					for(int r = 0; r < repeat; ++r) {
						results[r] = new JsonObject();
						threadTask(results[r], level, block, chunk, minHeightInclusive, maxHeightExclusive);
					}
					output(
							context, file, block.asPrintable(),
							level.dimension().location().toString(), defaultBiomeTag(level), level.getSeed(),
							minHeightInclusive, maxHeightExclusive,
							results
					);
				}
			}
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		
		return Command.SINGLE_SUCCESS;
	}
	
	private static void threadTask(JsonObject json, ServerLevel level, ResourceOrTagArgument.Result<Block> block,
								   int chunk, int minHeightInclusive, int maxHeightExclusive) throws IOException {
		int chunkX = level.getRandom().nextInt(65536) - 32768;
		int chunkZ = level.getRandom().nextInt(65536) - 32768;
		int a = (int)Math.sqrt(chunk) + 1;
		int[] result = new int[maxHeightExclusive - minHeightInclusive];
		for(int y = minHeightInclusive; y < maxHeightExclusive; ++y) {
			result[y - minHeightInclusive] = 0;
		}
		for(int c = 0; c < chunk; ++c) {
			int x = chunkX + c % a;
			int z = chunkZ + c / a;
			for(int y = minHeightInclusive; y < maxHeightExclusive; ++y) {
				for (int i = 0; i < 16; ++i) {
					for (int j = 0; j < 16; ++j) {
						if(block.test(level.getChunk(x, z).getBlockState(new BlockPos(i, y, j)).getBlockHolder())) {
							result[y - minHeightInclusive] += 1;
						}
					}
				}
			}
		}
		
		json.addProperty("chunk", chunk);
		json.addProperty("x", chunkX);
		json.addProperty("z", chunkZ);
		JsonObject values = new JsonObject();
		for(int y = minHeightInclusive; y < maxHeightExclusive; ++y) {
			values.addProperty(String.valueOf(y), result[y - minHeightInclusive]);
		}
		json.add("values", values);
	}
	
	private static void output(CommandContext<CommandSourceStack> context, File file, String block, String dimension, String biome, long seed,
							   int minHeightInclusive, int maxHeightExclusive, JsonObject[] results) {
		JsonObject combined = new JsonObject();
		combined.addProperty("version", FMLLoader.versionInfo().mcVersion());
		combined.addProperty(BLOCK_ARGUMENT, block);
		combined.addProperty("dimension", dimension);
		combined.addProperty(BIOME_ARGUMENT, biome);
		JsonObject info = new JsonObject();
		info.addProperty("seed", seed);
		JsonArray coordinates = new JsonArray();
		int[] combineResult = new int[maxHeightExclusive - minHeightInclusive];
		JsonObject values = new JsonObject();
		
		int chunk = 0;
		for(JsonObject result: results) {
			JsonObject coordinate = new JsonObject();
			int count = result.get("chunk").getAsInt();
			chunk += count;
			//Make the result reproducible.
			coordinate.addProperty("chunk", count);
			coordinate.addProperty("x", result.get("x").getAsInt());
			coordinate.addProperty("z", result.get("z").getAsInt());
			coordinates.add(coordinate);
			
			JsonObject ys = result.get("values").getAsJsonObject();
			for(int y = minHeightInclusive; y < maxHeightExclusive; ++y) {
				combineResult[y - minHeightInclusive] += ys.get(String.valueOf(y)).getAsInt();
			}
		}
		for(int y = minHeightInclusive; y < maxHeightExclusive; ++y) {
			values.addProperty(String.valueOf(y), combineResult[y - minHeightInclusive]);
		}
		combined.addProperty("chunk_count", chunk);
		info.add("coordinates", coordinates);
		combined.add("info", info);
		combined.add("values", values);
		
		try(FileOutputStream out = new FileOutputStream(file)) {
			try(Writer writer = new OutputStreamWriter(out)) {
				ConfigHelper.writeJsonToFile(writer, null, combined, 0);
				context.getSource().sendSuccess(() -> Component.translatable("info.ibm.resource.success", file.toString()), true);
			} catch (IOException e) {
				context.getSource().sendSuccess(() -> Component.translatable("info.ibm.resource.failure"), true);
				IngameBiomeMap.LOGGER.error(e.toString());
			}
		} catch (IOException e) {
			context.getSource().sendSuccess(() -> Component.translatable("info.ibm.resource.failure"), true);
			IngameBiomeMap.LOGGER.error(e.toString());
		}
	}
	
	@SuppressWarnings("unused")
	public static int exportResourceWithBiome(ServerLevel level, ResourceOrTagArgument.Result<Block> block, ChunkPos from,
											  ResourceOrTagArgument.Result<Biome> biome, int minHeightInclusive, int maxHeightExclusive) {
		return Command.SINGLE_SUCCESS;
	}
}
