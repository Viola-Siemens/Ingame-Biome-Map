package com.hexagram2021.ingame_biome_map.commands;

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

import java.io.*;

public class ResourceExportCommand {
	public static final File filePath = new File("./ResourceExport/");
	
	private static final String BLOCK_ARGUMENT = "block";
	private static final String CHUNK_COUNT_ARGUMENT = "chunk";
	private static final String MIN_HEIGHT_ARGUMENT = "minHeightInclusive";
	private static final String MAX_HEIGHT_ARGUMENT = "maxHeightExclusive";
	private static final String BIOME_ARGUMENT = "biome";
	
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
										-63, 256
								))
								.then(
										Commands.argument(CHUNK_COUNT_ARGUMENT, IntegerArgumentType.integer(4, 65536))
												.executes(context -> exportResourceWithoutBiome(
														context,
														context.getSource().getLevel(),
														ResourceOrTagArgument.getResourceOrTag(context, BLOCK_ARGUMENT, Registries.BLOCK),
														IntegerArgumentType.getInteger(context, CHUNK_COUNT_ARGUMENT),
														-63, 256
												))
												.then(
														Commands.argument(MIN_HEIGHT_ARGUMENT, IntegerArgumentType.integer(-63, 320))
																.then(
																		Commands.argument(MAX_HEIGHT_ARGUMENT, IntegerArgumentType.integer(-63, 320))
																				.executes(context -> exportResourceWithoutBiome(
																						context,
																						context.getSource().getLevel(),
																						ResourceOrTagArgument.getResourceOrTag(context, BLOCK_ARGUMENT, Registries.BLOCK),
																						IntegerArgumentType.getInteger(context, CHUNK_COUNT_ARGUMENT),
																						IntegerArgumentType.getInteger(context, MIN_HEIGHT_ARGUMENT),
																						IntegerArgumentType.getInteger(context, MAX_HEIGHT_ARGUMENT)
																				))
																)
												)
								)
				);
	}
	
	private static final Dynamic2CommandExceptionType INVALID_HEIGHT_PARAMETER = new Dynamic2CommandExceptionType(
			(min, max) -> Component.translatable("commands.height.invalid", min, max)
	);
	
	public static int exportResourceWithoutBiome(CommandContext<CommandSourceStack> context, ServerLevel level, ResourceOrTagArgument.Result<Block> block,
												 int chunk, int minHeightInclusive, int maxHeightExclusive) throws CommandSyntaxException {
		if(maxHeightExclusive <= minHeightInclusive) {
			throw INVALID_HEIGHT_PARAMETER.create(minHeightInclusive, maxHeightExclusive);
		}
		
		File file = new File(filePath + "/" + block.asPrintable().replaceAll(":", "_") + "(" + chunk + ").png");
		try {
			if (!filePath.exists() && !filePath.mkdir()) {
				IngameBiomeMap.LOGGER.error("Could not mkdir " + filePath);
			} else if (!file.exists() && !file.createNewFile()) {
				IngameBiomeMap.LOGGER.error("Could not create new file " + file);
			} else {
				IngameBiomeMap.LOGGER.info("Exporting resource for " + block.asPrintable() + " in " + chunk + "chunks.");
				try(FileOutputStream out = new FileOutputStream(file)) {
					try(Writer writer = new OutputStreamWriter(out)) {
						if (IngameBiomeMap.config.isMultiThread()) {
							new Thread(() -> {
								try {
									threadTask(writer, level, block, chunk, minHeightInclusive, maxHeightExclusive);
									context.getSource().sendSuccess(() -> Component.translatable("info.resource.success", file.toString()), true);
								} catch (IOException e) {
									context.getSource().sendSuccess(() -> Component.translatable("info.resource.failure"), true);
								}
							}).start();
						} else {
							threadTask(writer, level, block, chunk, minHeightInclusive, maxHeightExclusive);
						}
					} catch (IOException e) {
						IngameBiomeMap.LOGGER.error(e.toString());
					}
				} catch (IOException e) {
					IngameBiomeMap.LOGGER.error(e.toString());
				}
			}
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		
		return Command.SINGLE_SUCCESS;
	}
	
	private static void threadTask(Writer writer, ServerLevel level, ResourceOrTagArgument.Result<Block> block,
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
		JsonObject json = new JsonObject();
		json.addProperty(BLOCK_ARGUMENT, block.asPrintable());
		json.addProperty("dimension", level.dimension().location().toString());
		json.addProperty(BIOME_ARGUMENT, defaultBiomeTag(level));
		json.addProperty("chunk_count", chunk);
		JsonObject info = new JsonObject();
		info.addProperty("seed", level.getSeed());
		info.addProperty("x", chunkX);
		info.addProperty("z", chunkZ);
		json.add("info", info);
		JsonObject values = new JsonObject();
		for(int y = minHeightInclusive; y < maxHeightExclusive; ++y) {
			values.addProperty(String.valueOf(y), result[y - minHeightInclusive]);
		}
		json.add("values", values);
		
		ConfigHelper.writeJsonToFile(writer, null, json, 0);
	}
	
	@SuppressWarnings("unused")
	public static int exportResourceWithBiome(ServerLevel level, ResourceOrTagArgument.Result<Block> block, ChunkPos from,
											  ResourceOrTagArgument.Result<Biome> biome, int minHeightInclusive, int maxHeightExclusive) {
		return Command.SINGLE_SUCCESS;
	}
}
