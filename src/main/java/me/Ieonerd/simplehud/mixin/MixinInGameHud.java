package me.Ieonerd.simplehud.mixin;

import me.Ieonerd.simplehud.gui.SimpleHUDDisplay;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.util.math.MatrixStack;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Environment(EnvType.CLIENT)
@Mixin(InGameHud.class)
public class MixinInGameHud {

    @Shadow @Final private MinecraftClient client;
    SimpleHUDDisplay simpleHUD;

    //Adds a CondensedInfoHUD object to the InGameHud object used by the client
    @Inject(method = "<init>", at = @At("TAIL"))
    public void addSimpleHudToHUD(MinecraftClient client, ItemRenderer itemRenderer, CallbackInfo ci){
        simpleHUD = new SimpleHUDDisplay(client);
    }

    //Renders the mod's HUD whenever the render() method for the vanilla InGameHud is called
    //It is rendered last, so it would appear above anything that the vanilla HUD would render.
    //It only renders when the F3 menu is closed.
    @Inject(method = "render", at = @At("TAIL"))
    public void renderSimpleHud(DrawContext context, float tickDelta, CallbackInfo ci){
        if(!this.client.options.debugEnabled) simpleHUD.render(context);
    }

}
