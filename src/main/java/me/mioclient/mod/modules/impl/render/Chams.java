package me.mioclient.mod.modules.impl.render;

import me.mioclient.mod.modules.Category;
import me.mioclient.mod.modules.Module;
import me.mioclient.mod.modules.settings.Setting;
import net.minecraftforge.client.event.RenderPlayerEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.awt.*;

/**
 * @author t.me/asphyxia1337
 */

public class Chams extends Module {

    public static Chams INSTANCE;

    private final Setting<Page> page =
            add(new Setting<>("Settings", Page.GLOBAL));

    //Global

    public final Setting<Boolean> fill =
            add(new Setting<>("Fill", true, v -> page.getValue() == Page.GLOBAL).setParent());
    public final Setting<Boolean> xqz =
            add(new Setting<>("XQZ", true, v -> page.getValue() == Page.GLOBAL && fill.isOpen()));
    public final Setting<Boolean> wireframe =
            add(new Setting<>("Wireframe", true, v -> page.getValue() == Page.GLOBAL));
    public final Setting<Model> model =
            add(new Setting<>("Model", Model.XQZ, v -> page.getValue() == Page.GLOBAL));
    public final Setting<Boolean> self =
            add(new Setting<>("Self", true, v -> page.getValue() == Page.GLOBAL));
    public final Setting<Boolean> noInterp =
            add(new Setting<>("NoInterp", false, v -> page.getValue() == Page.GLOBAL));
    public final Setting<Boolean> sneak =
            add(new Setting<>("Sneak", false, v -> page.getValue() == Page.GLOBAL));
    public final Setting<Boolean> glint =
            add(new Setting<>("Glint", false, v -> page.getValue() == Page.GLOBAL));

    //Colors

    public final Setting<Float> lineWidth =
            add(new Setting<>("LineWidth", 1.0f, 0.1f, 3.0f, v -> page.getValue() == Page.COLORS));
    public final Setting<Boolean> rainbow =
            add(new Setting<>("Rainbow", false, v -> page.getValue() == Page.COLORS));

    public final Setting<Color> color =
            add(new Setting<>("Color", new Color(132, 132, 241, 150), v -> page.getValue() == Page.COLORS));

    public final Setting<Color> lineColor =
            add(new Setting<>("LineColor", new Color(255, 255, 255), v -> page.getValue() == Page.COLORS).injectBoolean(false));

    public final Setting<Color> modelColor =
            add(new Setting<>("ModelColor", new Color(125, 125, 213, 150), v -> page.getValue() == Page.COLORS).injectBoolean(false));

    public Chams() {
        super("Chams", "Draws a pretty ESP around other players.", Category.RENDER, true);
        INSTANCE = this;
    }

    public enum Page {
        COLORS,
        GLOBAL
    }

    public enum Model {
        XQZ,
        VANILLA,
        OFF
    }

    @Override
    public String getInfo() {
        String info = null;

        if (fill.getValue()) {
            info = "Fill";

        } else if (wireframe.getValue()) {
            info = "Wireframe";
        }

        if (wireframe.getValue() && fill.getValue()) {
            info = "Both";
        }

        return info;
    }

    @SubscribeEvent
    public void onRenderPlayerEvent(RenderPlayerEvent.Pre event) {
        event.getEntityPlayer().hurtTime = 0;
    }
}

