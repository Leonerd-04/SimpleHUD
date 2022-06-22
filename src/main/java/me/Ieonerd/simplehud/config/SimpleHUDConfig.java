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

// Handles config, including storage, for this mod
// I figured some of this code by looking at the implementation in Mod Menu
// Credit to TerraformersMC, though I didn't use their code verbatim
public class SimpleHUDConfig {
    private static CyclingOption<SimpleHUDDisplay.Clock> clockMode;
    private static CyclingOption<Boolean> indicateCanSleep;
    private static CyclingOption<Boolean> indicateLowFps;
    private static CyclingOption<Boolean> displayMinFps;
    private static CyclingOption<Boolean> respectReducedF3;

    // Using DoubleOption lets me use a slider
    private static DoubleOption coordsRounding;

    // An array with all the configs, specifically for rendering them in the settings screen
    public static ArrayList<Option> OPTIONS;

    // Hashmaps storing the config values
    private final static HashMap<String, String> STRING_MAP = new HashMap<>();
    private final static HashMap<String, Integer> INT_MAP = new HashMap<>();
    private final static HashMap<String, Boolean> BOOLEAN_MAP = new HashMap<>();

    private final static Type MAP_TYPE = new TypeToken<HashMap<String, Object>>() {}.getType();
    private final static Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static File file;
    private static final Logger LOGGER = LogManager.getLogger();

    // Checks if the config file has the correct path
    private static void prepareConfigFile(){
        if(file != null) return;
        file = new File(FabricLoader.getInstance().getConfigDir().toFile(), SimpleHUD.MOD_ID + ".json");
    }

    // The config stores different config types in three hashmaps and combines them into one for storage

    // Tries to load a config file; reverts to default values if not found
    public static void load(){
        OPTIONS = new ArrayList<>();

        // Default values in case loading fails
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
            HashMap<String, Object> map = GSON.fromJson(reader, MAP_TYPE);

            for(String key : map.keySet()){
                if(map.get(key) instanceof Double){
                    INT_MAP.put(key, ((Double) map.get(key)).intValue());
                    continue;
                }
                if(map.get(key) instanceof Boolean){
                    BOOLEAN_MAP.put(key, (Boolean) map.get(key));
                    continue;
                }

                STRING_MAP.put(key, (String) map.get(key));
            }

        } catch (FileNotFoundException | JsonSyntaxException e) {
            LOGGER.error("Config file failed to load; Reverting to default values");
            e.printStackTrace();
        }
    }

    // Tries to save the config file
    public static void save(){
        prepareConfigFile();

        HashMap<String, Object> map = new HashMap<>(STRING_MAP);
        map.putAll(BOOLEAN_MAP);
        map.putAll(INT_MAP);

        String json = GSON.toJson(map, MAP_TYPE);

        LOGGER.info("Saving config file");

        try (FileWriter fileWriter = new FileWriter(file)) {
            fileWriter.write(json);
        } catch (IOException e) {
            LOGGER.error("Failed to save config file");
            e.printStackTrace();
        }
    }


    public static boolean getBoolConfigValue(String key){
        return BOOLEAN_MAP.get(key);
    }

    public static int getIntConfigValue(String key){
        return INT_MAP.get(key);
    }

    public static String getConfigValue(String key){
        return STRING_MAP.get(key);
    }


    private static CyclingOption<Boolean> createBoolOption(String hashKey, String translationKey, boolean defaultVal){
        BOOLEAN_MAP.put(hashKey, defaultVal);

        CyclingOption<Boolean> option = CyclingOption.create(
                translationKey,
                ignored -> BOOLEAN_MAP.get(hashKey),
                (ignored, ignored2, newVal) -> BOOLEAN_MAP.put(hashKey, newVal)
        );

        OPTIONS.add(option);
        return option;
    }

    private static DoubleOption createIntOption(String hashKey, String translationKey, int defaultVal, int min, int max){
        INT_MAP.put(hashKey, defaultVal);

        DoubleOption option = new DoubleOption(translationKey, min, max, 1.0F,
                ignored -> (double) INT_MAP.get(hashKey),
                (ignored, newVal) -> INT_MAP.put(hashKey, newVal.intValue()),
                (ignored, ignored2) -> {
                    TranslatableText valueKey = new TranslatableText(String.format(translationKey + ".%d", INT_MAP.get(hashKey)));
                    return new TranslatableText(translationKey, valueKey);
                }
        );

        OPTIONS.add(option);
        return option;
    }

    private static <E extends Enum<E>> CyclingOption<E> createEnumOption(String hashKey, String translationKey, E defaultVal){
        STRING_MAP.put(hashKey, defaultVal.name());

        CyclingOption<E> option = CyclingOption.create(
                translationKey,
                defaultVal.getDeclaringClass().getEnumConstants(),
                value -> new TranslatableText(translationKey + "." + value.name().toLowerCase()),
                ignored -> Enum.valueOf(defaultVal.getDeclaringClass(), STRING_MAP.get(hashKey)),
                (ignored, ignored2, value) -> STRING_MAP.put(hashKey, value.name())
        );

        OPTIONS.add(option);
        return option;
    }
}
