package com.hexagram2021.ingame_biome_map.commands;

import com.hexagram2021.ingame_biome_map.IngameBiomeMap;
import com.hexagram2021.ingame_biome_map.utils.FileHelper;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.command.arguments.EntityArgument;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.text.TranslationTextComponent;

import java.io.IOException;

public class IBMCommand {
	public static LiteralArgumentBuilder<CommandSource> register() {
		return Commands.literal("exportbiomemap").requires((stack) -> stack.hasPermission(2))
				.then(Commands.argument("radius", IntegerArgumentType.integer())
						.then(Commands.argument("scale", IntegerArgumentType.integer())
								.then(Commands.argument("player", EntityArgument.player()).executes(IBMCommand::exportBiomeMap)))
				);
	}

	private static final DynamicCommandExceptionType INVALID_RADIUS_PARAMETER = new DynamicCommandExceptionType(
			(radius) -> new TranslationTextComponent("commands.radius.invalid", radius)
	);
	private static final DynamicCommandExceptionType INVALID_SCALE_PARAMETER = new DynamicCommandExceptionType(
			(scale) -> new TranslationTextComponent("commands.scale.invalid", scale)
	);

	public static int exportBiomeMap(CommandContext<CommandSource> context) throws CommandSyntaxException {
		Integer radius = context.getArgument("radius", Integer.class);
		Integer scale = context.getArgument("scale", Integer.class);
		if(radius <= 0) {
			throw INVALID_RADIUS_PARAMETER.create(radius);
		}
		if(scale <= 0) {
			throw INVALID_SCALE_PARAMETER.create(scale);
		}
		try {
			ServerPlayerEntity player = EntityArgument.getPlayer(context, "player");
			new FileHelper(player, player.getOnPos(), radius, scale);
		} catch (IOException | CommandSyntaxException e) {
			IngameBiomeMap.LOGGER.error(e.toString());
		}
		return 1;
	}
}
