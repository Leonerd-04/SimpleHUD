package me.Ieonerd.simplehud.config;

import com.terraformersmc.modmenu.config.ModMenuConfigManager;
import com.terraformersmc.modmenu.config.option.EnumConfigOption;
import me.Ieonerd.simplehud.gui.CondensedInfoHUD;
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
    public static final EnumConfigOption<CondensedInfoHUD.Clock> CLOCK_CONFIG = new EnumConfigOption<>("clock", CondensedInfoHUD.Clock.HR24);
    public static final EnumConfigOption<CondensedInfoHUD.CoordRounding> COORD_ROUNDING = new EnumConfigOption<>("coords", CondensedInfoHUD.CoordRounding.THREE_DIGITS);
    private ButtonListWidget buttonList;

    public SimpleHUDConfigScreen(Screen parent) {
        super(parent, MinecraftClient.getInstance().options, Text.of("SimpleHUD Options"));
    }

    protected void init(){
        buttonList = new ButtonListWidget(this.client, this.width, this.height, 32, this.height - 32, 25);
        buttonList.addSingleOptionEntry(CLOCK_CONFIG.asOption());
        buttonList.addSingleOptionEntry(COORD_ROUNDING.asOption());
        this.addDrawableChild(buttonList);
        this.addDrawableChild(new ButtonWidget(this.width / 2 - 100, this.height - 27, 200, 20, ScreenTexts.DONE, (button) -> {
            ModMenuConfigManager.save();
            this.client.setScreen(this.parent);
        }));
    }
}
