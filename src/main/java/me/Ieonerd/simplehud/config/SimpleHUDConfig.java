package me.Ieonerd.simplehud.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.terraformersmc.modmenu.config.option.BooleanConfigOption;
import com.terraformersmc.modmenu.config.option.EnumConfigOption;
import me.Ieonerd.simplehud.SimpleHUD;
import me.Ieonerd.simplehud.gui.CondensedInfoHUD;
import net.fabricmc.loader.api.FabricLoader;

import java.io.*;

//Handles config, including storage, for the mod
//I figured out a lot of this code from Terraformers MC's implementation in ModMenu.
public class SimpleHUDConfig {
    public final EnumConfigOption<CondensedInfoHUD.Clock> clockMode = new EnumConfigOption<>("clock", CondensedInfoHUD.Clock.HR24);
    public final EnumConfigOption<CondensedInfoHUD.CoordRounding> coordRounding = new EnumConfigOption<>("coords", CondensedInfoHUD.CoordRounding.THREE_DIGITS);
    public final BooleanConfigOption indicateCanSleep = new BooleanConfigOption("sleep_indicator", true);

    private static Gson gson = new GsonBuilder().setPrettyPrinting().create();

    private static File file;

    //Returns a config with default values
    public SimpleHUDConfig(){
    }

    //Returns a config with values read from a json
    public SimpleHUDConfig(ConfigFileFormat format){
        if(format == null) {
            SimpleHUD.LOGGER.error("Config file not read properly; Reverting to default values");
            return;
        }
        clockMode.setValue(format.clockMode);
        coordRounding.setValue(format.coordRounding);
        indicateCanSleep.setValue(format.indicateCanSleep);
    }

    //Formats the game's config as another class that is easily stored
    private ConfigFileFormat formatForStorage(){
        return new ConfigFileFormat(clockMode.getValue(), coordRounding.getValue(), indicateCanSleep.getValue());
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
            format = gson.fromJson(reader, ConfigFileFormat.class);

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
        String json = gson.toJson(this.formatForStorage());

        try (FileWriter fileWriter = new FileWriter(file)) {
            fileWriter.write(json);
        } catch (IOException e) {
            SimpleHUD.LOGGER.error("Failed to save config file");
            e.printStackTrace();
        }
    }


    private class ConfigFileFormat {
        CondensedInfoHUD.Clock clockMode;
        CondensedInfoHUD.CoordRounding coordRounding;
        boolean indicateCanSleep;

        private ConfigFileFormat(CondensedInfoHUD.Clock clockMode, CondensedInfoHUD.CoordRounding coordRounding, boolean indicateCanSleep){
            this.clockMode = clockMode;
            this.coordRounding = coordRounding;
            this.indicateCanSleep = indicateCanSleep;
        }
    }

}
