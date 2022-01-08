package me.Ieonerd.simplehud.gui;

import com.google.common.base.Strings;
import me.Ieonerd.simplehud.mixin.MinecraftClientAccessor;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.Vec3d;

import java.util.Locale;
import java.util.Objects;

public class SimpleHUD {
    MinecraftClient client;
    private static int HUD_WHITE = 14737632; //Color of the F3 HUD

    public SimpleHUD(MinecraftClient client){
        this.client = client;
    }

    public void render(MatrixStack matrices){
        String[] text = this.getText();
        String string;

        for(int i = 0; i < text.length; i++){
            string = text[i];

            if(Strings.isNullOrEmpty(string)) continue;
            Objects.requireNonNull(this.client.textRenderer);

            int height = 2 + 9 * i;
            this.client.textRenderer.draw(matrices, string, 2.0F, (float)height, HUD_WHITE);
        }
    }

    private String getCoords(){
        Vec3d position = this.client.getCameraEntity().getPos();
        return String.format(Locale.ROOT, "XYZ: %.2f, %.2f, %.2f", position.getX(), position.getY(), position.getZ());
    }

    private String getFPS(){
        return MinecraftClientAccessor.getCurrentFPS() + " fps";
    }

    private String getTime(){
        long time = this.client.world.getTimeOfDay();
        int hr = (int) (time + 6000) / 1000 % 24;
        int min = (int) ((time % 1000) * 0.06);
        return String.format(Locale.ROOT, "%02d:%02d", hr, min); //24 hr clock
    }

    private String[] getText(){
        return new String[]{
                getFPS(),
                getCoords(),
                getTime()
        };
    }
}
