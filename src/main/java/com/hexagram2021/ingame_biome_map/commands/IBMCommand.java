package com.hexagram2021.ingame_biome_map.commands;

import com.hexagram2021.ingame_biome_map.IngameBiomeMap;
import com.hexagram2021.ingame_biome_map.utils.FileHelper;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

import javax.annotation.Nullable;
import java.io.IOException;

public class IBMCommand {
	private static final String RADIUS_ARGUMENT = "radius";
	private static final String SCALE_ARGUMENT = "scale";
	private static final String PLAYER_ARGUMENT = "player";
	private static final String STRUCTURE_ARGUMENT = "structure";
	
	public static LiteralArgumentBuilder<CommandSourceStack> register() {
		return Commands.literal("exportbiomemap")
				.requires(stack -> stack.hasPermission(2))
				.then(
						Commands.argument(RADIUS_ARGUMENT, IntegerArgumentType.integer())
								.then(
										Commands.argument(SCALE_ARGUMENT, IntegerArgumentType.integer())
												.executes(context -> exportBiomeMap(
														context,
														IntegerArgumentType.getInteger(context, RADIUS_ARGUMENT),
														IntegerArgumentType.getInteger(context, SCALE_ARGUMENT),
														context.getSource().getPlayer(), false
												))
												.then(
														Commands.argument(STRUCTURE_ARGUMENT, BoolArgumentType.bool())
																.executes(context -> exportBiomeMap(
																		context,
																		IntegerArgumentType.getInteger(context, RADIUS_ARGUMENT),
																		IntegerArgumentType.getInteger(context, SCALE_ARGUMENT),
																		context.getSource().getPlayer(),
																		BoolArgumentType.getBool(context, STRUCTURE_ARGUMENT)
																)).then(
																		Commands.argument(PLAYER_ARGUMENT, EntityArgument.player())
																				.executes(context -> exportBiomeMap(
																						context,
																						IntegerArgumentType.getInteger(context, RADIUS_ARGUMENT),
																						IntegerArgumentType.getInteger(context, SCALE_ARGUMENT),
																						EntityArgument.getPlayer(context, PLAYER_ARGUMENT),
																						BoolArgumentType.getBool(context, STRUCTURE_ARGUMENT)
																				))
																)
														
												)
								)
				);
	}

	private static final DynamicCommandExceptionType INVALID_RADIUS_PARAMETER = new DynamicCommandExceptionType(
			(radius) -> Component.translatable("commands.radius.invalid", radius)
	);
	private static final DynamicCommandExceptionType INVALID_SCALE_PARAMETER = new DynamicCommandExceptionType(
			(scale) -> Component.translatable("commands.scale.invalid", scale)
	);

	public static int exportBiomeMap(CommandContext<CommandSourceStack> context, int radius, int scale,
									 @Nullable ServerPlayer player, boolean structure) throws CommandSyntaxException {
		if(radius <= 0) {
			throw INVALID_RADIUS_PARAMETER.create(radius);
		}
		if(scale <= 0) {
			throw INVALID_SCALE_PARAMETER.create(scale);
		}
		try {
			if(player == null) {
				new FileHelper(context, new BlockPos(0, 160, 0), radius, scale, structure);
			} else {
				new FileHelper(player, player.getOnPos(), radius, scale, structure);
			}
		} catch (IOException e) {
			IngameBiomeMap.LOGGER.error("Error when export biome map: ", e);
		}
		return Command.SINGLE_SUCCESS;
	}
}
