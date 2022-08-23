package me.Ieonerd.simplehud.config;

import com.mojang.serialization.Codec;
import me.Ieonerd.simplehud.SimpleHUD;
import me.Ieonerd.simplehud.gui.SimpleHUDDisplay;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import com.google.common.reflect.TypeToken;

import net.fabricmc.loader.api.FabricLoader;

import net.minecraft.text.Text;
import net.minecraft.client.option.SimpleOption;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

// Handles config, including storage, for this mod
// I figured some of this code by looking at the implementation in Mod Menu
// Credit to TerraformersMC, though I didn't use their code verbatim
public class SimpleHUDConfig {
    public static SimpleOption<SimpleHUDDisplay.Clock> clockMode;
    public static SimpleOption<SimpleHUDDisplay.Compass> compassMode;
    public static SimpleOption<Boolean> indicateCanSleep;
    public static SimpleOption<Boolean> indicateLowFps;
    public static SimpleOption<Boolean> displayMinFps;
    public static SimpleOption<Boolean> respectReducedF3;

    // Using DoubleOption lets me use a slider
    public static SimpleOption<Integer> coordsRounding;

    // An array with all the configs, specifically for rendering them in the settings screen
    public static ArrayList<SimpleOption<?>> OPTIONS;

    // Hashmaps storing the config values
    private final static HashMap<String, SimpleOption<Enum<?>>> ENUM_MAP = new HashMap<>();
    private final static HashMap<String, SimpleOption<Integer>> INT_MAP = new HashMap<>();
    private final static HashMap<String, SimpleOption<Boolean>> BOOLEAN_MAP = new HashMap<>();

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
        compassMode = createEnumOption("compassMode","simplehud.config.compass", SimpleHUDDisplay.Compass.INITIALS_ONLY);
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
                    INT_MAP.get(key).setValue(((Double) map.get(key)).intValue());
                    continue;
                }
                if(map.get(key) instanceof Boolean){
                    BOOLEAN_MAP.get(key).setValue((Boolean) map.get(key));
                    continue;
                }
                if(map.get(key) instanceof Enum<?>){
                    ENUM_MAP.get(key).setValue((Enum<?>) map.get(key));
                }
            }

        } catch (FileNotFoundException | JsonSyntaxException e) {
            LOGGER.error("Config file failed to load; Reverting to default values");
            e.printStackTrace();
        }
    }

    // Tries to save the config file
    public static void save(){
        prepareConfigFile();

        HashMap<String, Object> map = new HashMap<>(getValues(ENUM_MAP));
        map.putAll(getValues(BOOLEAN_MAP));
        map.putAll(getValues(INT_MAP));

        String json = GSON.toJson(map, MAP_TYPE);

        LOGGER.info("Saving config file");

        try (FileWriter fileWriter = new FileWriter(file)) {
            fileWriter.write(json);
        } catch (IOException e) {
            LOGGER.error("Failed to save config file");
            e.printStackTrace();
        }
    }

    //Maps a hashmap of SimpleOptions to a hashmap of values
    private static <T> HashMap<String, T> getValues(HashMap<String, SimpleOption<T>> optionMap){
        HashMap<String, T> valMap = new HashMap<>();
        for(String key : optionMap.keySet()){
            valMap.put(key, optionMap.get(key).getValue());
        }

        return valMap;
    }


    public static boolean getBoolConfigValue(String key){
        return BOOLEAN_MAP.get(key).getValue();
    }

    public static int getIntConfigValue(String key){
        return INT_MAP.get(key).getValue();
    }

    public static String getConfigValue(String key){
        return ENUM_MAP.get(key).getValue().toString();
    }


    private static SimpleOption<Boolean> createBoolOption(String hashKey, String translationKey, boolean defaultVal){
        SimpleOption<Boolean> option = SimpleOption.ofBoolean(translationKey, defaultVal);

        BOOLEAN_MAP.put(hashKey, option);
        OPTIONS.add(option);
        return option;
    }

    private static SimpleOption<Integer> createIntOption(String hashKey, String translationKey, int defaultVal, int min, int max){
        SimpleOption<Integer> option = new SimpleOption<>(
                translationKey,
                SimpleOption.emptyTooltip(),
                (optionText, value) -> {
                    Text valueKey = Text.translatable(String.format(translationKey + ".%d", value));
                    return Text.translatable(translationKey, valueKey);
                },
                new SimpleOption.ValidatingIntSliderCallbacks(min, max),
                defaultVal,
                value -> {}
        );

        INT_MAP.put(hashKey, option);
        OPTIONS.add(option);
        return option;
    }

    private static <E extends Enum<E>> SimpleOption<E> createEnumOption(String hashKey, String translationKey, E defaultVal){
        List<E> values = Arrays.asList(defaultVal.getDeclaringClass().getEnumConstants());

        SimpleOption<E> option = new SimpleOption<>(
                translationKey,
                SimpleOption.emptyTooltip(),
                (optionText, value) -> Text.translatable(translationKey + "." + value.name().toLowerCase()),
                new SimpleOption.PotentialValuesBasedCallbacks<>(values,
                        Codec.INT.xmap(values::get, values::indexOf)),
                defaultVal,
                value -> {}
        );

        ENUM_MAP.put(hashKey, (SimpleOption<Enum<?>>) option);
        OPTIONS.add(option);
        return option;
    }
}
