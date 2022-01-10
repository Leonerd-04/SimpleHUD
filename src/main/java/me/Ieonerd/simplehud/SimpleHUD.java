package me.Ieonerd.simplehud;

import me.Ieonerd.simplehud.config.SimpleHUDConfig;
import me.Ieonerd.simplehud.config.SimpleHUDConfigScreen;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Environment(EnvType.CLIENT)
public class SimpleHUD implements ClientModInitializer {
	public static final String MOD_ID = "simplehud";
	public static final Logger LOGGER = LogManager.getLogger("SimpleHUD");

	@Override
	public void onInitializeClient() {
		SimpleHUDConfigScreen.CONFIG = SimpleHUDConfig.load();
	}
}
