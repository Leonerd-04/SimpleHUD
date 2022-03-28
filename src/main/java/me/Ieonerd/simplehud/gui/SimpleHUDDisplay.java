package me.Ieonerd.simplehud.gui;

import me.Ieonerd.simplehud.mixin.MinecraftClientAccessor;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.math.Vec3d;

import java.util.ArrayList;
import java.util.Locale;
import java.util.Objects;

import static me.Ieonerd.simplehud.config.SimpleHUDConfigScreen.CONFIG;

//Handles the rendering of SimpleHUD
public class SimpleHUDDisplay {
    MinecraftClient client;
    private static final int HUD_WHITE = 0xE0E0E0; //Color of the F3 HUD text
    private static final int HUD_BACKGROUND = 0x90505050; //Color of the F3 HUD background
    private static final int SLEEP_GREEN = 0x40D646;
    private static final int FPS_YELLOW = 0xFFFF45;
    private static final int FPS_RED = 0xF48282;

    public SimpleHUDDisplay(MinecraftClient client){
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
    //colors is allowed to be shorter than row.
    //If this happens, the last color in the array of colors will be used to render the rest of the row.
    private void renderRow(MatrixStack matrices, int height, String[] row, int[] colors){
        int position = 0; //Stores the position to render a particular string in the row
        String str;
        int color = colors[0];

        for(String string : row) position += this.client.textRenderer.getWidth(string);

        DrawableHelper.fill(matrices, 1, height - 1, 3 + position, height + 8, HUD_BACKGROUND);
        position = 0;

        for(int i = 0; i < row.length; i++){
            str = row[i];
            if(i < colors.length) color = colors[i];

            this.client.textRenderer.draw(matrices, str, 2.0F + position, (float)height, color);
            position += this.client.textRenderer.getWidth(str);
        }
    }

    //Gets the minimum fps over the last 240 frames by finding the largest frame time
    //Good indicator of stuttering at higher fps
    //It will be colored in the same way as average fps
    private int getMinFps(){
        long max = 0;
        long[] fpsData = this.client.getMetricsData().getSamples(); //Fps data is in nanoseconds per frame
        for(long fps : fpsData) max = Math.max(max, fps);
        return (int)(1000000000.0 / max);
    }

    //Changes the fps display's color
    //>60 fps -> white
    //30-59 fps -> yellow
    //<30 fps -> red
    private int getFPSColor(int fps){
        if(!CONFIG.indicateLowFps.getValue()) return HUD_WHITE;
        if(fps < 30) return FPS_RED;
        if(fps < 60) return FPS_YELLOW;
        return HUD_WHITE;
    }

    //Formats a string for coordinates
    private String getCoords(){
        Vec3d position = this.client.getCameraEntity().getPos();
        int round = (int) CONFIG.coordsRounding.get(client.options);

        //Formats the first coordinate string with the desired rounding numbers
        String placeDigits = String.format("XYZ: %%.%df, %%.%df, %%.%df", round, round, round);

        //Formats this formatted string again to insert the coordinates, with desired rounding
        return String.format(Locale.ROOT, placeDigits, position.getX(), position.getY(), position.getZ());
    }

    //Finds the direction the player is facing (North, East, South, West)
    private String getDirection(){
        float yaw = this.client.getCameraEntity().getYaw();
        float v = ((yaw + 45)/90);

        int mode = (int) v % 4;
        if(v < 0) mode += 3;

        return switch(mode){
            case 0 -> "S";
            case 1 -> "W";
            case 2 -> "N";
            case 3 -> "E";
            default -> "?"; //Shouldn't happen, used to identify unintended behavior
        };
    }

    //Formats a string for the time display
    private String getTime(){
        long time = this.client.world.getTimeOfDay(); //time = ticks since the world started
        Clock setting = CONFIG.clockMode.getValue();

        if(setting == Clock.TICK)
            return String.format(Locale.ROOT, "%d",time % 24000); //Ticked clock, like /time query daytime

        int hr = (int) (time + 6000) / 1000 % 24;
        int min = (int) ((time % 1000) * 0.06);

        if(setting == Clock.HR24)
            return String.format(Locale.ROOT, "%02d:%02d", hr, min); //24 hr clock

        String ampm = hr > 11 ? "PM" : "AM";
        hr = hr % 12;
        if(hr == 0) hr = 12;

        //System.out.println(new TranslatableText("simplehud.hud.time.hr12",  hr, min, ampm).getString());
        return String.format(Locale.ROOT, "%2d:%02d %s",  hr, min, ampm); //12 hr AM PM clock
    }

    //Changes the clock display's color to tell the player that they can sleep
    //It will only turn green whenever it's either thundering, or it's nighttime,
    //and ignores other things like nearby monsters.
    private int getTimeColor(){
        if(!CONFIG.indicateCanSleep.getValue()) return HUD_WHITE;

        long time = this.client.world.getTimeOfDay() % 24000; //time is equal to /time query daytime
        if(this.client.world.isThundering() || (time > 12541 && time < 23460)) return SLEEP_GREEN;

        return HUD_WHITE;
    }

    //Formats an ArrayList of String[] for the renderer
    private ArrayList<String[]> getText(int fps, int fpsMin){
        //Fps is displayed like in Optifine, with a slash between average and minimum fps
        String[] fpsRow = CONFIG.displayMinFps.getValue() ?
                new String[]{String.valueOf(fps), "/", String.valueOf(fpsMin), " fps"} :
                new String[]{String.valueOf(fps), " fps"};

        ArrayList<String[]> arr = new ArrayList<>();

        arr.add(fpsRow);

        //respectReducedF3 hides coordinates and time if the server has the gamerule toggled on
        if(CONFIG.respectReducedF3.getValue() && this.client.hasReducedDebugInfo()) return arr;


        arr.add(new String[]{getCoords(), " ", getDirection()});
        arr.add(new String[]{new TranslatableText("simplehud.hud.time").getString(), getTime()});
        return arr;
    }

    //Formats an ArrayList of int[] for the renderer's colors
    private ArrayList<int[]> getColors(int fps, int fpsMin){
        ArrayList<int[]> arr = new ArrayList<>();

        arr.add(new int[]{getFPSColor(fps), HUD_WHITE, getFPSColor(fpsMin), HUD_WHITE});
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
