package me.Ieonerd.simplehud.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import com.terraformersmc.modmenu.config.option.BooleanConfigOption;
import com.terraformersmc.modmenu.config.option.EnumConfigOption;
import me.Ieonerd.simplehud.SimpleHUD;
import me.Ieonerd.simplehud.gui.CondensedInfoHUD;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.DoubleOption;
import net.minecraft.client.option.Option;
import net.minecraft.text.TranslatableText;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.util.ArrayList;

//Handles config, including storage, for this mod
//I figured out a lot of this code by looking at the implementation in Mod Menu
//Credit to TerraformersMC, though I didn't use their code verbatim
public class SimpleHUDConfig {
    public int coordinateRounding = 3;
    public final EnumConfigOption<CondensedInfoHUD.Clock> clockMode = new EnumConfigOption<>("clock", CondensedInfoHUD.Clock.HR24);
    public final BooleanConfigOption indicateCanSleep = new BooleanConfigOption("sleep_indicator", true);
    public final BooleanConfigOption indicateLowFps = new BooleanConfigOption("low_fps", true);
    public final BooleanConfigOption displayMinFps = new BooleanConfigOption("fps_min", true);
    public final BooleanConfigOption respectReducedF3 = new BooleanConfigOption("respect_reduced_f3", false);

    //Using DoubleOption lets me use a slider
    public final DoubleOption coordsRounding = new DoubleOption("option.modmenu.coords", 0.0, 6.0, 1.0F,
            gameOptions -> (double) coordinateRounding, //getter for the coordinate rounding
            (gameOptions, rounding) -> coordinateRounding = (int) rounding.doubleValue(), //setter for the coordinate rounding
            (gameOptions, option) -> {
                TranslatableText valueKey = new TranslatableText(String.format("option.modmenu.coords.%d", coordinateRounding));
                return new TranslatableText("option.modmenu.coords", valueKey); //getter for the display text
            }
    );

    public final ArrayList<Option> options = new ArrayList<>();

    private final static Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static File file;
    private static final Logger LOGGER = LogManager.getLogger();

    //Returns a config with default values
    public SimpleHUDConfig(){
        options.add(clockMode.asOption());
        options.add(coordsRounding);
        options.add(indicateCanSleep.asOption());
        options.add(indicateLowFps.asOption());
        options.add(displayMinFps.asOption());
        options.add(respectReducedF3.asOption());
    }

    //Returns a config with values read from a json
    public SimpleHUDConfig(ConfigFileFormat format){
        this();
        if(format == null) {
            LOGGER.error("Config file not read properly; Reverting to default values");
            return;
        }

        clockMode.setValue(format.clockMode);
        coordsRounding.set(MinecraftClient.getInstance().options, format.coordRounding);
        indicateCanSleep.setValue(format.indicateCanSleep);
        indicateLowFps.setValue(format.indicateLowFps);
        displayMinFps.setValue(format.displayMinFps);
        respectReducedF3.setValue(format.respectReducedF3);
    }

    //Formats the game's config as another class that is easily stored
    private ConfigFileFormat formatForStorage(){
        return new ConfigFileFormat(clockMode.getValue(), (int) coordsRounding.get(MinecraftClient.getInstance().options),
                indicateCanSleep.getValue(), indicateLowFps.getValue(), displayMinFps.getValue(), respectReducedF3.getValue());
    }

    //Checks if the config file has the correct path
    private static void prepareConfigFile(){
        if(file != null) return;
        file = new File(FabricLoader.getInstance().getConfigDir().toFile(), SimpleHUD.MOD_ID + ".json");
    }

    //Tries to load a config file; reverts to default values if not found
    public static SimpleHUDConfig load(){
        LOGGER.info("Loading SimpleHUD configuration file");
        prepareConfigFile();
        ConfigFileFormat format;
        try{
            if(!file.exists()) {
                SimpleHUDConfigScreen.CONFIG = new SimpleHUDConfig();
                SimpleHUDConfigScreen.CONFIG.save();
            }
            FileReader reader = new FileReader(file);
            format = GSON.fromJson(reader, ConfigFileFormat.class);

        } catch (FileNotFoundException | JsonSyntaxException e) {
            LOGGER.error("Config file failed to load; Reverting to default values");
            e.printStackTrace();
            return new SimpleHUDConfig();
        }

        return new SimpleHUDConfig(format);
    }

    //Tries to save the config file
    public void save(){
        prepareConfigFile();
        String json = GSON.toJson(this.formatForStorage());

        LOGGER.info("Saving config file");

        try (FileWriter fileWriter = new FileWriter(file)) {
            fileWriter.write(json);
        } catch (IOException e) {
            LOGGER.error("Failed to save config file");
            e.printStackTrace();
        }
    }

    //Class that is used to store config as a json
    private static class ConfigFileFormat {
        CondensedInfoHUD.Clock clockMode;
        int coordRounding;
        boolean indicateCanSleep;
        boolean indicateLowFps;
        boolean displayMinFps;
        boolean respectReducedF3;

        private ConfigFileFormat(CondensedInfoHUD.Clock clockMode, int coordRounding,
                                 boolean indicateCanSleep, boolean indicateLowFps, boolean displayMinFps, boolean respectReducedF3){
            this.clockMode = clockMode;
            this.coordRounding = coordRounding;
            this.indicateCanSleep = indicateCanSleep;
            this.indicateLowFps = indicateLowFps;
            this.displayMinFps = displayMinFps;
            this.respectReducedF3 = respectReducedF3;
        }
    }

}
