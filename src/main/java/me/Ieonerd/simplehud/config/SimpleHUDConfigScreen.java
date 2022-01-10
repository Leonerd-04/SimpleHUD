package me.Ieonerd.simplehud.config;

import com.terraformersmc.modmenu.config.ModMenuConfigManager;
import com.terraformersmc.modmenu.config.option.OptionConvertable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ScreenTexts;
import net.minecraft.client.gui.screen.option.GameOptionsScreen;
import net.minecraft.client.gui.widget.ButtonListWidget;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

@Environment(EnvType.CLIENT)
public class SimpleHUDConfigScreen extends GameOptionsScreen {
    private ButtonListWidget buttonList;
    public static SimpleHUDConfig CONFIG;

    public SimpleHUDConfigScreen(Screen parent) {
        super(parent, MinecraftClient.getInstance().options, Text.of("SimpleHUD Options"));
    }

    protected void init(){
        buttonList = new ButtonListWidget(this.client, this.width, this.height, 32, this.height - 32, 25);
        for(OptionConvertable option : CONFIG.options) buttonList.addSingleOptionEntry(option.asOption());
        this.addDrawableChild(buttonList);

        this.addDrawableChild(new ButtonWidget(this.width / 2 - 100, this.height - 27, 200, 20, ScreenTexts.DONE, (button) -> {
            CONFIG.save();
            this.client.setScreen(this.parent);
        }));
    }

    public void removed(){
        CONFIG.save();
    }
}
