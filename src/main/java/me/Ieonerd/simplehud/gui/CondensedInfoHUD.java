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
    private static final int GREEN = 4249158;

    public CondensedInfoHUD(MinecraftClient client){
        this.client = client;
    }

    //Rendering algorithm based off of the one used to render the left side of the F3 screen
    public void render(MatrixStack matrices){
        String[][] text = this.getText();
        int[][] colors = getColors();
        String[] row;
        int[] rowColors;

        for(int i = 0; i < text.length; i++){
            row = text[i];
            rowColors = colors[i];

            if(Strings.isNullOrEmpty(row[0])) continue;
            Objects.requireNonNull(this.client.textRenderer);

            int height = 2 + 9 * i;
            renderRow(matrices, height, row, rowColors);
        }
    }

    private void renderRow(MatrixStack matrices, int height, String[] row, int[] colors){
        int position = 0;
        String str;
        int color = colors[0];
        for(int i = 0; i < row.length; i++){
            str = row[i];
            if(i < colors.length) color = colors[i];

            this.client.textRenderer.draw(matrices, str, 2.0F + position, (float)height, color);
            position += this.client.textRenderer.getWidth(str);
        }
    }


    private String getCoords(){
        Vec3d position = this.client.getCameraEntity().getPos();
        int round = SimpleHUDConfigScreen.COORD_ROUNDING.getValue().getDigits();

        //The String.format() will replace %% with % and %d with a number
        String placeDigits = String.format("XYZ: %%.%df, %%.%df, %%.%df", round, round, round);

        return String.format(Locale.ROOT, placeDigits, position.getX(), position.getY(), position.getZ());
    }

    private String getFPS(){
        return MinecraftClientAccessor.getCurrentFPS() + " fps";
    }

    private String getTime(){
        long time = this.client.world.getTimeOfDay(); //time = ticks since the world started
        Clock setting = SimpleHUDConfigScreen.CLOCK_CONFIG.getValue();

        if(setting == Clock.TICK) return String.format(Locale.ROOT, "%d",time % 24000); //Ticked clock, like /time query daytime

        int hr = (int) (time + 6000) / 1000 % 24;
        int min = (int) ((time % 1000) * 0.06);
        if(setting == Clock.HR24) return String.format(Locale.ROOT, "%02d:%02d", hr, min); //24 hr clock

        String ampm = hr > 11 ? "PM" : "AM";
        hr = hr % 12;
        if(hr == 0) hr = 12;
        return String.format(Locale.ROOT, "%2d:%02d %s",  hr, min, ampm); //12 hr AM PM clock
    }

    //Changes the clock display's color to tell the player they can sleep
    private int getTimeColor(){
        if(!SimpleHUDConfigScreen.INDICATE_SLEEP.getValue()) return HUD_WHITE;

        long time = this.client.world.getTimeOfDay() % 24000; //time = ticks since the world started
        if(this.client.world.isThundering() || (time > 12541 && time < 23460)) return GREEN;

        return HUD_WHITE;
    }

    private String[][] getText(){
        return new String[][]{
                new String[]{getFPS()},
                new String[]{getCoords()} ,
                new String[]{"Time: ", getTime()}
        };
    }

    private int[][] getColors(){
        return new int[][]{
                new int[]{HUD_WHITE},
                new int[]{HUD_WHITE},
                new int[]{HUD_WHITE, getTimeColor()}
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
