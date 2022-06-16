package me.Ieonerd.simplehud.config;

import com.google.common.reflect.TypeToken;
import me.Ieonerd.simplehud.SimpleHUD;
import me.Ieonerd.simplehud.gui.SimpleHUDDisplay;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;

import net.fabricmc.loader.api.FabricLoader;

import net.minecraft.client.option.CyclingOption;
import net.minecraft.client.option.DoubleOption;
import net.minecraft.client.option.Option;
import net.minecraft.text.TranslatableText;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;

//Handles config, including storage, for this mod
//I figured out a lot of this code by looking at the implementation in Mod Menu
//Credit to TerraformersMC, though I didn't use their code verbatim
public class SimpleHUDConfig {
    private static CyclingOption<SimpleHUDDisplay.Clock> clockMode;
    private static CyclingOption<Boolean> indicateCanSleep;
    private static CyclingOption<Boolean> indicateLowFps;
    private static CyclingOption<Boolean> displayMinFps;
    private static CyclingOption<Boolean> respectReducedF3;

    //Using DoubleOption lets me use a slider
    private static DoubleOption coordsRounding;

    public static ArrayList<Option> OPTIONS;
    private static HashMap<String, String> MAP;

    private final static Type MAP_TYPE = new TypeToken<HashMap<String, String>>() {}.getType();
    private final static Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static File file;
    private static final Logger LOGGER = LogManager.getLogger();

    //Checks if the config file has the correct path
    private static void prepareConfigFile(){
        if(file != null) return;
        file = new File(FabricLoader.getInstance().getConfigDir().toFile(), SimpleHUD.MOD_ID + ".json");
    }

    //Tries to load a config file; reverts to default values if not found
    public static void load(){
        MAP = new HashMap<>();
        OPTIONS = new ArrayList<>();

        clockMode = createEnumOption("clockMode","simplehud.config.clock", SimpleHUDDisplay.Clock.HR24);
        indicateCanSleep = createBoolOption("indicateCanSleep", "simplehud.config.sleep_indicator", true);
        indicateLowFps = createBoolOption("indicateLowFps","simplehud.config.low_fps", true);
        displayMinFps = createBoolOption("displayMinFps", "simplehud.config.fps_min", true);
        respectReducedF3 = createBoolOption("respectReducedF3", "simplehud.config.respect_reduced_f3", false);
        coordsRounding = createIntOption("coordRounding", "simplehud.config.coords", 3, 0, 6);

        LOGGER.info("Loading SimpleHUD configuration file");
        prepareConfigFile();

        try{
            if(!file.exists()) {
                save();
            }
            FileReader reader = new FileReader(file);
            MAP = GSON.fromJson(reader, MAP_TYPE);
        } catch (FileNotFoundException | JsonSyntaxException e) {
            LOGGER.error("Config file failed to load; Reverting to default values");
            e.printStackTrace();
        }
    }

    //Tries to save the config file
    public static void save(){
        prepareConfigFile();
        String json = GSON.toJson(MAP, MAP_TYPE);

        LOGGER.info("Saving config file");

        try (FileWriter fileWriter = new FileWriter(file)) {
            fileWriter.write(json);
        } catch (IOException e) {
            LOGGER.error("Failed to save config file");
            e.printStackTrace();
        }
    }


    public static boolean getBoolConfigValue(String key){
        return Boolean.parseBoolean(MAP.get(key));
    }

    public static int getIntConfigValue(String key){
        return Integer.parseInt(MAP.get(key));
    }

    public static String getConfigValue(String key){
        return MAP.get(key);
    }


    private static CyclingOption<Boolean> createBoolOption(String hashKey, String translationKey, boolean defaultVal){
        MAP.put(hashKey, String.valueOf(defaultVal));

        CyclingOption<Boolean> option = CyclingOption.create(
                translationKey,
                ignored -> Boolean.parseBoolean(MAP.get(hashKey)),
                (ignored, ignored2, newVal) -> MAP.put(hashKey, String.valueOf(newVal))
        );

        OPTIONS.add(option);
        return option;
    }

    private static DoubleOption createIntOption(String hashKey, String translationKey, int defaultVal, int min, int max){
        MAP.put(hashKey, String.valueOf(defaultVal));

        DoubleOption option = new DoubleOption(translationKey, min, max, 1.0F,
                ignored -> (double) Integer.parseInt(MAP.get(hashKey)),
                (ignored, newVal) -> MAP.put(hashKey, String.valueOf(newVal.intValue())),
                (ignored, ignored2) -> {
                    TranslatableText valueKey = new TranslatableText(String.format(translationKey + ".%d", Integer.valueOf(MAP.get(hashKey))));
                    return new TranslatableText(translationKey, valueKey);
                }
        );

        OPTIONS.add(option);
        return option;
    }

    private static <E extends Enum<E>> CyclingOption<E> createEnumOption(String hashKey, String translationKey, E defaultVal){
        MAP.put(hashKey, defaultVal.name());

        CyclingOption<E> option = CyclingOption.create(
                translationKey,
                defaultVal.getDeclaringClass().getEnumConstants(),
                value -> new TranslatableText(translationKey + "." + value.name().toLowerCase()),
                ignored -> Enum.valueOf(defaultVal.getDeclaringClass(), MAP.get(hashKey)),
                (ignored, ignored2, value) -> MAP.put(hashKey, value.name())
        );

        OPTIONS.add(option);
        return option;
    }
}
