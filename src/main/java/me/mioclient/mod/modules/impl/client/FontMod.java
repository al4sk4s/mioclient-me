package me.mioclient.mod.modules.impl.client;

import com.mojang.realmsclient.gui.ChatFormatting;
import me.mioclient.api.events.impl.ClientEvent;
import me.mioclient.api.managers.Managers;
import me.mioclient.mod.commands.Command;
import me.mioclient.mod.modules.Category;
import me.mioclient.mod.modules.Module;
import me.mioclient.mod.modules.settings.Setting;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.awt.*;

public class FontMod extends Module {

    public static FontMod INSTANCE;

    public final Setting<String> font =
            add(new Setting<>("Font", "Verdana"));
    public final Setting<Boolean> antiAlias =
            add(new Setting<>("AntiAlias", true));
    public final Setting<Boolean> metrics =
            add(new Setting<>("Metrics", true));
    public final Setting<Boolean> global =
            add(new Setting<>("Global", false));
    public final Setting<Integer> size =
            add(new Setting<>("Size", 17, 12, 30));
    public final Setting<Style> style =
            add(new Setting<>("Style", Style.PLAIN));

    private boolean reload;

    public FontMod() {
        super("Fonts", "Custom font for all of the clients text. Use the font command.", Category.CLIENT, true);
        INSTANCE = this;
    }

    private enum Style {
        PLAIN,
        BOLD,
        ITALIC,
        BOLDITALIC
    }

    @Override
    public String getInfo() {
        return font.getValue();
    }

    @Override
    public void onTick() {
        if (reload) {
            Managers.TEXT.init();
            reload = false;
        }
    }

    @SubscribeEvent
    public void onSettingChange(ClientEvent event) {
        Setting setting;

        if (event.getStage() == 2 && (setting = event.getSetting()) != null && setting.getMod().equals(this)) {

            if (setting.getName().equals("Font") && !checkFont(setting.getPlannedValue().toString())) {

                Command.sendMessage(ChatFormatting.RED + "That font doesn't exist.");

                event.cancel();
                return;
            }
            reload = true;
        }
    }

    private boolean checkFont(String font) {

        for (String s : GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames()) {

            if (s.equals(font)) {
                return true;
            }

        }
        return false;
    }

    public int getFont() {

        switch (style.getValue()) {

            case BOLD:
                return 1;

            case ITALIC:
                return 2;

            case BOLDITALIC:
                return 3;

            default:
                return 0;
        }
    }
}

