package me.Ieonerd.simplehud.mixin;

import me.Ieonerd.simplehud.gui.SimpleHUDDisplay;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.MetricsData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MinecraftClient.class)
public class MinecraftClientMixin {

	//Triggers whenever the current fps is set, so the average and minimum fps update simultaneously
	@Inject(method = "render", at = @At(value = "INVOKE", target = "Ljava/lang/String;format(Ljava/lang/String;[Ljava/lang/Object;)Ljava/lang/String;"))
	public void updateDisplay(boolean tick, CallbackInfo ci){
		SimpleHUDDisplay.setMinFps(calculateMinFps());
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
