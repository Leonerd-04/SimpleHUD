package me.Ieonerd.simplehud.gui;

import me.Ieonerd.simplehud.mixin.MinecraftClientAccessor;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.Vec3d;

import java.util.ArrayList;
import java.util.Locale;
import java.util.Objects;

import static me.Ieonerd.simplehud.config.SimpleHUDConfigScreen.CONFIG;

public class CondensedInfoHUD {
    MinecraftClient client;
    private static final int HUD_WHITE = 0xE0E0E0; //Color of the F3 HUD text
    private static final int SLEEP_GREEN = 0x40D646;
    private static final int FPS_YELLOW = 0xFFFF45;
    private static final int FPS_RED = 0xF48282;

    public CondensedInfoHUD(MinecraftClient client){
        this.client = client;
    }

    //Rendering algorithm based off of the one used to render the left side of the F3 screen
    //With the exception that instead of rendering one string as a row, this mod renders one array of strings as a row
    //To allow for individual sections of a given row to be different colors.
    public void render(MatrixStack matrices){
        int fps = MinecraftClientAccessor.getCurrentFPS();
        int minFps = getMinFps();
        ArrayList<String[]> text = this.getText(fps, minFps);
        ArrayList<int[]> colors = getColors(fps, minFps);
        String[] row;
        int[] rowColors;

        for(int i = 0; i < text.size(); i++){
            row = text.get(i);
            rowColors = colors.get(i);

            if(row == null) continue;
            Objects.requireNonNull(this.client.textRenderer);

            int height = 2 + 9 * i;
            renderRow(matrices, height, row, rowColors);
        }
    }

    //Renders an array of strings as a row in the HUD
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

    //Gets the minimum fps over the last 240 frames by finding the largest frame time
    //Good indicator of stuttering at higher fps
    private int getMinFps(){
        long max = 0;
        long[] fpsData = this.client.getMetricsData().getSamples(); //Fps data is in nanoseconds per frame
        for(long fps : fpsData) max = Math.max(max, fps);
        return (int)(1000000000.0 / max);
    }

    //Changes the fps display's color when fps goes below 60
    private int getFPSColor(int fps){
        if(!CONFIG.indicateLowFps.getValue()) return HUD_WHITE;
        if(fps < 30) return FPS_RED;
        if(fps < 60) return FPS_YELLOW;
        return HUD_WHITE;
    }

    private String getCoords(){
        Vec3d position = this.client.getCameraEntity().getPos();
        int round = (int) CONFIG.coordsRounding.get(client.options);

        //This String.format() will replace %% with % and %d with the number of digits to round to
        String placeDigits = String.format("XYZ: %%.%df, %%.%df, %%.%df", round, round, round);

        return String.format(Locale.ROOT, placeDigits, position.getX(), position.getY(), position.getZ());
    }

    private String getTime(){
        long time = this.client.world.getTimeOfDay(); //time = ticks since the world started
        Clock setting = CONFIG.clockMode.getValue();

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
        if(!CONFIG.indicateCanSleep.getValue()) return HUD_WHITE;

        long time = this.client.world.getTimeOfDay() % 24000; //time = ticks since the world started
        if(this.client.world.isThundering() || (time > 12541 && time < 23460)) return SLEEP_GREEN;

        return HUD_WHITE;
    }

    private ArrayList<String[]> getText(int fps, int fpsMin){
        String[] fpsRow = CONFIG.displayMinFps.getValue() ?
                new String[]{String.valueOf(fps), "/", String.valueOf(fpsMin), " fps"} :
                new String[]{String.valueOf(fps), " fps"};

        ArrayList<String[]> arr = new ArrayList<>();

        arr.add(fpsRow);

        if(CONFIG.respectReducedF3.getValue() && this.client.hasReducedDebugInfo()){
            arr.add(new String[]{"Time: ", getTime()});
            return arr;
        }

        arr.add(new String[]{getCoords()});
        arr.add(new String[]{"Time: ", getTime()});
        return arr;
    }

    private ArrayList<int[]> getColors(int fps, int fpsMin){
        ArrayList<int[]> arr = new ArrayList<>();

        arr.add(new int[]{getFPSColor(fps), HUD_WHITE, getFPSColor(fpsMin), HUD_WHITE});

        if(CONFIG.respectReducedF3.getValue() && this.client.hasReducedDebugInfo()){
            arr.add(new int[]{HUD_WHITE, getTimeColor()});
            return arr;
        }

        arr.add(new int[]{HUD_WHITE});
        arr.add(new int[]{HUD_WHITE, getTimeColor()});
        return arr;
    }

    public enum Clock{
        TICK,
        HR24,
        HR12
    }
}
