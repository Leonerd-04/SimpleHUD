package me.Ieonerd.simplehud;

import me.Ieonerd.simplehud.config.SimpleHUDConfigScreen;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public class SimpleHUDModMenu implements ModMenuApi {
    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return SimpleHUDConfigScreen::new;
    }
}
