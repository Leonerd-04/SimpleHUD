package me.Ieonerd.simplehud.mixin;

import me.Ieonerd.simplehud.gui.CondensedInfoHUD;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.util.math.MatrixStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Environment(EnvType.CLIENT)
@Mixin(InGameHud.class)
public class MixinInGameHud extends DrawableHelper {

    @Shadow @Final private MinecraftClient client;
    CondensedInfoHUD simpleHUD;

    @Inject(method = "<init>", at = @At("TAIL"))
    public void addSimpleHudToHUD(MinecraftClient client, CallbackInfo ci){
        simpleHUD = new CondensedInfoHUD(client);
    }

    @Inject(method = "render", at = @At("TAIL"))
    public void renderSimpleHud(MatrixStack matrices, float tickDelta, CallbackInfo ci){
        if(!this.client.options.debugEnabled) simpleHUD.render(matrices);
    }

}
