package me.Ieonerd.simplehud.gui;

import com.google.common.base.Strings;
import me.Ieonerd.simplehud.config.SimpleHUDConfigScreen;
import me.Ieonerd.simplehud.mixin.MinecraftClientAccessor;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.Vec3d;

import java.util.Locale;
import java.util.Objects;

public class CondensedInfoHUD {
    MinecraftClient client;
    private static final int HUD_WHITE = 14737632; //Color of the F3 HUD text

    public CondensedInfoHUD(MinecraftClient client){
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
        int round = SimpleHUDConfigScreen.COORD_ROUNDING.getValue().getDigits();
        String placeDigits = String.format("XYZ: %%.%df, %%.%df, %%.%df", round, round, round);
        return String.format(Locale.ROOT, placeDigits, position.getX(), position.getY(), position.getZ());
    }

    private String getFPS(){
        return MinecraftClientAccessor.getCurrentFPS() + " fps";
    }

    private String getTime(){
        long time = this.client.world.getTimeOfDay();
        Clock setting = SimpleHUDConfigScreen.CLOCK_CONFIG.getValue();

        if(setting == Clock.TICK) return String.format(Locale.ROOT, "Time: %d",time % 24000); //Ticked clock, like /time query daytime

        int hr = (int) (time + 6000) / 1000 % 24;
        int min = (int) ((time % 1000) * 0.06);
        if(setting == Clock.HR24) return String.format(Locale.ROOT, "Time: %02d:%02d", hr, min); //24 hr clock

        String ampm = hr > 11 ? "PM" : "AM";
        hr = (hr) % 12;
        if(hr == 0) hr = 12;
        return String.format(Locale.ROOT, "Time: %2d:%02d %s",  hr, min, ampm); //12 hr AM PM clock
    }

    private String[] getText(){
        return new String[]{
                getFPS(),
                getCoords(),
                getTime()
        };
    }

    public enum Clock{
        TICK,
        HR24,
        HR12
    }

    public enum CoordRounding{
        INTEGER(0),
        ONE_DIGIT(1),
        TWO_DIGITS(2),
        THREE_DIGITS(3);

        private final int decimalDigits;

        CoordRounding(int decimalDigits){
            this.decimalDigits = decimalDigits;
        }

        public int getDigits(){
            return decimalDigits;
        }
    }
}
