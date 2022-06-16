package me.Ieonerd.simplehud.mixin;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

//Used to display average FPS
@Environment(EnvType.CLIENT)
@Mixin(MinecraftClient.class)
public interface MinecraftClientAccessor {

    @Accessor("currentFps")
    static int getCurrentFPS() {
        throw new AssertionError();
    }
}
