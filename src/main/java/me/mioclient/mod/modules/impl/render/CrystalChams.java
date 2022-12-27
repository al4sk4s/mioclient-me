package me.mioclient.mod.modules.impl.render;

import me.mioclient.mod.modules.Category;
import me.mioclient.mod.modules.Module;
import me.mioclient.mod.modules.settings.Setting;

import java.awt.*;

/**
 * @author t.me/asphyxia1337
 */

public class CrystalChams extends Module {

    public static CrystalChams INSTANCE;

    private final Setting<Page> page =
            add(new Setting<>("Settings", Page.GLOBAL));

    //Global

    public Setting<Boolean> fill =
            add(new Setting<>("Fill", true, v -> page.getValue() == Page.GLOBAL).setParent());
    public Setting<Boolean> xqz =
            add(new Setting<>("XQZ", true, v -> page.getValue() == Page.GLOBAL && fill.isOpen()));
    public Setting<Boolean> wireframe =
            add(new Setting<>("Wireframe", true, v -> page.getValue() == Page.GLOBAL));
    public Setting<Model> model =
            add(new Setting<>("Model", Model.XQZ, v -> page.getValue() == Page.GLOBAL));
    public Setting<Boolean> glint =
            add(new Setting<>("Glint", false, v -> page.getValue() == Page.GLOBAL));
    public Setting<Float> scale =
            add(new Setting<>("Scale", 1.0f, 0.1f, 1.0f, v -> page.getValue() == Page.GLOBAL));
    public Setting<Boolean> changeSpeed =
            add(new Setting<>("ChangeSpeed", false, v -> page.getValue() == Page.GLOBAL).setParent());
    public Setting<Float> spinSpeed =
            add(new Setting<>("SpinSpeed", 1.0f, 0.0f, 10.0f, v -> page.getValue() == Page.GLOBAL && changeSpeed.isOpen()));
    public Setting<Float> floatFactor =
            add(new Setting<>("FloatFactor", 1.0f, 0.0f, 1.0f, v -> page.getValue() == Page.GLOBAL && changeSpeed.isOpen()));

    //Colors

    public Setting<Float> lineWidth =
            add(new Setting<>("LineWidth", 1.0f, 0.1f, 3.0f, v -> page.getValue() == Page.COLORS));
    public Setting<Boolean> rainbow =
            add(new Setting<>("Rainbow", false, v -> page.getValue() == Page.COLORS));

    public Setting<Color> color =
            add(new Setting<>("Color", new Color(132, 132, 241, 150), v -> page.getValue() == Page.COLORS));

    public Setting<Color> lineColor =
            add(new Setting<>("LineColor", new Color(255, 255, 255), v -> page.getValue() == Page.COLORS).injectBoolean(false));

    public Setting<Color> modelColor =
            add(new Setting<>("ModelColor", new Color(125, 125, 213, 150), v -> page.getValue() == Page.COLORS).injectBoolean(false));

    public CrystalChams() {
        super("CrystalChams", "Draws a pretty ESP around end crystals.", Category.RENDER);
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
}
