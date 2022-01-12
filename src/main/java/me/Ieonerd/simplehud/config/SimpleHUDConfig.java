package me.Ieonerd.simplehud.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.terraformersmc.modmenu.config.option.BooleanConfigOption;
import com.terraformersmc.modmenu.config.option.EnumConfigOption;
import com.terraformersmc.modmenu.config.option.OptionConvertable;
import me.Ieonerd.simplehud.SimpleHUD;
import me.Ieonerd.simplehud.gui.CondensedInfoHUD;
import net.fabricmc.loader.api.FabricLoader;

import java.io.*;
import java.util.ArrayList;

//Handles config, including storage, for the mod
//I figured out a lot of this code by looking at the implementation in Mod Menu.
//Credit to TerraformersMC, though I didn't use their code verbatim
public class SimpleHUDConfig {
    public final EnumConfigOption<CondensedInfoHUD.Clock> clockMode = new EnumConfigOption<>("clock", CondensedInfoHUD.Clock.HR24);
    public final EnumConfigOption<CondensedInfoHUD.CoordRounding> coordRounding = new EnumConfigOption<>("coords", CondensedInfoHUD.CoordRounding.THREE_DIGITS);
    public final BooleanConfigOption indicateCanSleep = new BooleanConfigOption("sleep_indicator", true);
    public final BooleanConfigOption indicateLowFps = new BooleanConfigOption("low_fps", true);
    public final BooleanConfigOption displayMinFps = new BooleanConfigOption("fps_min", true);
    public final BooleanConfigOption respectReducedF3 = new BooleanConfigOption("respect_reduced_f3", false);
    public final ArrayList<OptionConvertable> options = new ArrayList<>();

    private final static Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    private static File file;

    //Returns a config with default values
    public SimpleHUDConfig(){
        options.add(clockMode);
        options.add(coordRounding);
        options.add(indicateCanSleep);
        options.add(indicateLowFps);
        options.add(displayMinFps);
        options.add(respectReducedF3);
    }

    //Returns a config with values read from a json
    public SimpleHUDConfig(ConfigFileFormat format){
        this();
        if(format == null) {
            SimpleHUD.LOGGER.error("Config file not read properly; Reverting to default values");
            return;
        }

        clockMode.setValue(format.clockMode);
        coordRounding.setValue(format.coordRounding);
        indicateCanSleep.setValue(format.indicateCanSleep);
        indicateLowFps.setValue(format.indicateLowFps);
        displayMinFps.setValue(format.displayMinFps);
    }

    //Formats the game's config as another class that is easily stored
    private ConfigFileFormat formatForStorage(){
        return new ConfigFileFormat(clockMode.getValue(), coordRounding.getValue(),
                indicateCanSleep.getValue(), indicateLowFps.getValue(), displayMinFps.getValue(), respectReducedF3.getValue());
    }

    //Checks if the config file has the correct path
    private static void prepareConfigFile(){
        if(file != null) return;
        file = new File(FabricLoader.getInstance().getConfigDir().toFile(), SimpleHUD.MOD_ID + ".json");
    }

    //Tries to load a config file; reverts to default values if not found
    public static SimpleHUDConfig load(){
        SimpleHUD.LOGGER.info("Loading SimpleHUD configuration file");
        prepareConfigFile();
        ConfigFileFormat format;
        try{
            if(!file.exists()) {
                SimpleHUDConfigScreen.CONFIG = new SimpleHUDConfig();
                SimpleHUDConfigScreen.CONFIG.save();
            }
            FileReader reader = new FileReader(file);
            format = GSON.fromJson(reader, ConfigFileFormat.class);

        } catch (FileNotFoundException e) {
            SimpleHUD.LOGGER.error("Config file failed to load; Reverting to default values");
            e.printStackTrace();
            return new SimpleHUDConfig();
        }

        return new SimpleHUDConfig(format);
    }

    //Tries to save the config file
    public void save(){
        prepareConfigFile();
        String json = GSON.toJson(this.formatForStorage());

        try (FileWriter fileWriter = new FileWriter(file)) {
            fileWriter.write(json);
        } catch (IOException e) {
            SimpleHUD.LOGGER.error("Failed to save config file");
            e.printStackTrace();
        }
    }


    private static class ConfigFileFormat {
        CondensedInfoHUD.Clock clockMode;
        CondensedInfoHUD.CoordRounding coordRounding;
        boolean indicateCanSleep;
        boolean indicateLowFps;
        boolean displayMinFps;
        boolean respectReducedF3;

        private ConfigFileFormat(CondensedInfoHUD.Clock clockMode, CondensedInfoHUD.CoordRounding coordRounding,
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
