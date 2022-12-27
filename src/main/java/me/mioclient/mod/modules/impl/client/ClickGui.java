package me.mioclient.mod.modules.impl.client;

import com.mojang.realmsclient.gui.ChatFormatting;
import me.mioclient.api.events.impl.ClientEvent;
import me.mioclient.api.managers.Managers;
import me.mioclient.mod.commands.Command;
import me.mioclient.mod.gui.screen.MioClickGui;
import me.mioclient.mod.modules.Category;
import me.mioclient.mod.modules.Module;
import me.mioclient.mod.modules.settings.Setting;
import net.minecraft.client.gui.GuiChat;
import net.minecraft.client.gui.GuiMainMenu;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.lwjgl.input.Keyboard;

import java.awt.*;

public class ClickGui extends Module {

    public static ClickGui INSTANCE;

    //General settings

    public final Setting<String> prefix =
            add(new Setting<>("Prefix", ";"));

    public final Setting<Boolean> guiMove =
            add(new Setting<>("GuiMove", true));

    //Appearance

    public final Setting<Style> style =
            add(new Setting<>("Style", Style.NEW));

    public final Setting<Integer> height =
            add(new Setting<>("ButtonHeight", 4, 1, 5));

    public final Setting<Boolean> blur =
            add(new Setting<>("Blur", false));

    public final Setting<Boolean> line =
            add(new Setting<>("Line", true).setParent());
    public final Setting<Boolean> rollingLine =
            add(new Setting<>("RollingLine", true, v -> line.isOpen()));

    public final Setting<Boolean> rect =
            add(new Setting<>("Rect", true).setParent());
    public final Setting<Boolean> colorRect =
            add(new Setting<>("ColorRect", false, v -> rect.isOpen()));

    public final Setting<Boolean> gear =
            add(new Setting<>("Gear", true));

    //All guis things

    public final Setting<Boolean> particles =
            add(new Setting<>("Particles", true)
                    .setParent());
    public final Setting<Boolean> colorParticles =
            add(new Setting<>("ColorParticles", true, v -> particles.isOpen()));

    public final Setting<Boolean> background =
            add(new Setting<>("Background", true));

    public final Setting<Boolean> cleanGui =
            add(new Setting<>("CleanGui", false));

    //Colors

    public final Setting<Color> color =
            add(new Setting<>("Color", new Color(125, 125, 213)).hideAlpha());

    public final Setting<Boolean> rainbow =
            add(new Setting<>("Rainbow", false).setParent());
    public final Setting<Rainbow> rainbowMode =
            add(new Setting<>("Mode", Rainbow.NORMAL, v -> rainbow.isOpen()));
    public final Setting<Float> rainbowBrightness =
            add(new Setting<>("Brightness ", 150.0f, 1.0f, 255.0f, v -> rainbow.isOpen() && rainbowMode.getValue() == Rainbow.NORMAL));
    public final Setting<Float> rainbowSaturation =
            add(new Setting<>("Saturation", 150.0f, 1.0f, 255.0f, v -> rainbow.isOpen() && rainbowMode.getValue() == Rainbow.NORMAL));
    public final Setting<Color> secondColor =
            add(new Setting<>("SecondColor", new Color(255, 255, 255), v -> rainbow.isOpen() && rainbowMode.getValue() == Rainbow.DOUBLE).hideAlpha());
    public final Setting<HudRainbow> hudRainbow =
            add(new Setting<>("HUD", HudRainbow.STATIC, v -> rainbow.isOpen()));
    public final Setting<Integer> rainbowDelay =
            add(new Setting<>("Delay", 240, 0, 600, v -> rainbow.isOpen()));

    private final KeyBinding[] keys;

    public ClickGui() {
        super("ClickGui", "Opens the ClickGui.", Category.CLIENT, true);

        keys = new KeyBinding[] {
                        mc.gameSettings.keyBindForward,
                        mc.gameSettings.keyBindBack,
                        mc.gameSettings.keyBindLeft,
                        mc.gameSettings.keyBindRight,
                        mc.gameSettings.keyBindJump,
                        mc.gameSettings.keyBindSprint
        };

        INSTANCE = this;
    }

    public enum HudRainbow {
        STATIC,
        ROLLING
    }

    public enum Rainbow {
        NORMAL,
        PLAIN,
        DOUBLE
    }

    public enum Style {
        OLD,
        NEW,
        FUTURE,
        DOTGOD
    }

    @Override
    public void onEnable() {
        if (mc.world != null){
            mc.displayGuiScreen(MioClickGui.INSTANCE);
        }

        if (blur.getValue()) {
            mc.entityRenderer.loadShader(new ResourceLocation("shaders/post/blur.json"));
        }
    }

    @Override
    public void onLoad() {
        Managers.COLORS.setCurrent(color.getValue());
        Managers.COMMANDS.setPrefix(prefix.getValue());
    }

    @Override
    public void onTick() {
        if (!(mc.currentScreen instanceof MioClickGui) && !(mc.currentScreen instanceof GuiMainMenu)) {
            disable();
        }
    }

    @Override
    public void onUpdate() {
        if (guiMove.getValue()&& !(mc.currentScreen instanceof GuiChat)) {

            for (KeyBinding key : keys) {
                KeyBinding.setKeyBindState(key.getKeyCode(), Keyboard.isKeyDown(key.getKeyCode()));
            }

        } else {
            
            for (KeyBinding key : keys) {

                if (!Keyboard.isKeyDown(key.getKeyCode())) {
                    KeyBinding.setKeyBindState(key.getKeyCode(), false);
                }
            }
        }
    }

    @SubscribeEvent
    public void onSettingChange(ClientEvent event) {
        if (event.getStage() == 2 && event.getSetting().getMod().equals(this)) {

            if (event.getSetting().equals(prefix)) {
                Managers.COMMANDS.setPrefix(prefix.getPlannedValue());
                Command.sendMessage("Prefix set to " + ChatFormatting.DARK_GRAY + Managers.COMMANDS.getCommandPrefix());
            }

            Managers.COLORS.setCurrent(color.getValue());
        }
    }

    public int getButtonHeight() {
        return 11 + height.getValue();
    }
 }


