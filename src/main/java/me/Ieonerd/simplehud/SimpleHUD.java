package me.Ieonerd.simplehud;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Environment(EnvType.CLIENT)
public class SimpleHUD implements ClientModInitializer {
	public static final Logger LOGGER = LogManager.getLogger("simplehud");

	@Override
	public void onInitializeClient() {

	}
}
