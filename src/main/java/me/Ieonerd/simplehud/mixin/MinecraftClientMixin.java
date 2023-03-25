package me.Ieonerd.simplehud.mixin;

import me.Ieonerd.simplehud.gui.SimpleHUDDisplay;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.util.MetricsData;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MinecraftClient.class)
public abstract class MinecraftClientMixin {

	private final static Logger LOGGER = LogManager.getLogger();

	//Triggers whenever the current fps is set, so the average & minimum fps as well as the ping update simultaneously
	@Inject(method = "render", at = @At(value = "INVOKE", target = "Ljava/lang/String;format(Ljava/util/Locale;Ljava/lang/String;[Ljava/lang/Object;)Ljava/lang/String;"))
	public void updateDisplay(boolean tick, CallbackInfo ci){
		SimpleHUDDisplay.setMinFps(calculateMinFps());

		try {
			SimpleHUDDisplay.setPing(getServerPing());
		} catch(Throwable throwable){
			LOGGER.error("setPing failed ", throwable);
		}
	}

	private int getServerPing(){
		ClientPlayNetworkHandler handler = ((MinecraftClient)(Object) this).getNetworkHandler();

		//Second condition checks whether the server is integrated
		if(handler == null || ((MinecraftClient)(Object) this).getServer() != null){
			return -1;
		}

		return handler.getPlayerListEntry(handler.getProfile().getId()).getLatency();
	}

	//Gets the minimum fps over the last second by finding the largest frame time
	//Good indicator of stuttering at higher fps
	//It will be colored in the same way as average fps when displayed
	private int calculateMinFps(){
		long max = 0;
		long total = 0;
		MetricsData data = ((MinecraftClient)(Object) this).getMetricsData();
		for(int i = data.getCurrentIndex() + 240; i > data.getCurrentIndex(); i--){
			max = Math.max(max, data.getSamples()[i % 240]);
			total += data.getSamples()[i % 240];

			if(total > 1000000000) break;
		}
		return (int)(1000000000.0 / max);
	}
}
