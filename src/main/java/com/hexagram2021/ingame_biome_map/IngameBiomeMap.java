package com.hexagram2021.ingame_biome_map;

import com.hexagram2021.ingame_biome_map.commands.IBMCommand;
import com.hexagram2021.ingame_biome_map.utils.ConfigHelper;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.logging.LogUtils;
import net.minecraft.commands.CommandSourceStack;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.slf4j.Logger;

import static com.hexagram2021.ingame_biome_map.IngameBiomeMap.MODID;

@Mod(MODID)
public class IngameBiomeMap {
	public static final Logger LOGGER = LogUtils.getLogger();

	public static final ConfigHelper config = new ConfigHelper("IBMConfig.json");

	public static final String MODID = "ingame_biome_map";
	public static final String MODNAME = "Ingame Biome Map";
	public static final String VERSION = "${version}";

	public IngameBiomeMap() {
		MinecraftForge.EVENT_BUS.addListener(this::registerCommands);
		MinecraftForge.EVENT_BUS.register(this);
	}

	@SubscribeEvent
	public void registerCommands(RegisterCommandsEvent event) {
		final CommandDispatcher<CommandSourceStack> dispatcher = event.getDispatcher();
		dispatcher.register(IBMCommand.register());
	}
}
