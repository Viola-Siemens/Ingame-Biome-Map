package com.hexagram2021.ingame_biome_map.commands;

import com.hexagram2021.ingame_biome_map.IngameBiomeMap;
import com.hexagram2021.ingame_biome_map.utils.FileHelper;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

import java.io.IOException;

public class IBMCommand {
	public static LiteralArgumentBuilder<CommandSourceStack> register() {
		return Commands.literal("exportbiomemap").requires((stack) -> stack.hasPermission(2))
				.then(Commands.argument("radius", IntegerArgumentType.integer())
						.then(Commands.argument("scale", IntegerArgumentType.integer())
								.then(Commands.argument("player", EntityArgument.player()).executes(IBMCommand::exportBiomeMap)))
				);
	}

	private static final DynamicCommandExceptionType INVALID_RADIUS_PARAMETER = new DynamicCommandExceptionType(
			(radius) -> Component.translatable("commands.radius.invalid", radius)
	);
	private static final DynamicCommandExceptionType INVALID_SCALE_PARAMETER = new DynamicCommandExceptionType(
			(scale) -> Component.translatable("commands.scale.invalid", scale)
	);

	public static int exportBiomeMap(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
		Integer radius = context.getArgument("radius", Integer.class);
		Integer scale = context.getArgument("scale", Integer.class);
		if(radius <= 0) {
			throw INVALID_RADIUS_PARAMETER.create(radius);
		}
		if(scale <= 0) {
			throw INVALID_SCALE_PARAMETER.create(scale);
		}
		try {
			ServerPlayer player = EntityArgument.getPlayer(context, "player");
			new FileHelper(player, player.getOnPos(), radius, scale);
		} catch (IOException | CommandSyntaxException e) {
			IngameBiomeMap.LOGGER.error("Error when export biome map: ", e);
		}
		return 1;
	}
}
