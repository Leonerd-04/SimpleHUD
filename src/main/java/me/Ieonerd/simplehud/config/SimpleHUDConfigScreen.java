package me.Ieonerd.simplehud.config;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ScreenTexts;
import net.minecraft.client.gui.screen.option.GameOptionsScreen;
import net.minecraft.client.gui.widget.ButtonListWidget;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.option.Option;
import net.minecraft.text.Text;

import static me.Ieonerd.simplehud.config.SimpleHUDConfig.OPTIONS;

//Handles the screen that appears when using ModMenu
//I'd like to credit TerraformersMC again, as this code is from the Config in ModMenu
@Environment(EnvType.CLIENT)
public class SimpleHUDConfigScreen extends GameOptionsScreen {
    private ButtonListWidget buttonList;

    public SimpleHUDConfigScreen(Screen parent) {
        super(parent, MinecraftClient.getInstance().options, Text.of("SimpleHUD Options"));
    }

    protected void init(){
        buttonList = new ButtonListWidget(this.client, this.width, this.height, 32, this.height - 32, 25);
        for(Option option : OPTIONS) buttonList.addSingleOptionEntry(option);
        this.addDrawableChild(buttonList);

        this.addDrawableChild(new ButtonWidget(this.width / 2 - 100, this.height - 27, 200, 20, ScreenTexts.DONE, (button) -> {
            this.client.setScreen(this.parent);
        }));
    }

    public void removed(){
        SimpleHUDConfig.save();
    }
}
