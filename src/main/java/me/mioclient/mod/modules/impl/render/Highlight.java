package me.mioclient.mod.modules.impl.render;

import me.mioclient.api.events.impl.Render3DEvent;
import me.mioclient.api.managers.Managers;
import me.mioclient.api.util.render.RenderUtil;
import me.mioclient.mod.modules.Category;
import me.mioclient.mod.modules.Module;
import me.mioclient.mod.modules.settings.Setting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;

import java.awt.*;

public class Highlight extends Module {

    private final Setting<Boolean> line =
            add(new Setting<>("Line", true));
    private final Setting<Boolean> box =
            add(new Setting<>("Box", false));
    private final Setting<Boolean> depth =
            add(new Setting<>("Depth", true));
    private final Setting<Float> lineWidth =
            add(new Setting<>("LineWidth", 1.0f, 0.1f, 3.0f));

    private final Setting<Color> color =
            add(new Setting<>("Color", new Color(125, 125, 213, 150)));
    private final Setting<Boolean> rainbow =
            add(new Setting<>("Rainbow", false));

    public Highlight() {
        super("Highlight", "Highlights the block u look at.", Category.RENDER);
    }

    @Override
    public void onRender3D(Render3DEvent event) {
        RayTraceResult ray = mc.objectMouseOver;

        if (ray != null && ray.typeOfHit == RayTraceResult.Type.BLOCK) {
            BlockPos pos = ray.getBlockPos();

            RenderUtil.drawSelectionBoxESP(
                    pos,
                    rainbow.getValue() ? Managers.COLORS.getRainbow() : color.getValue(),
                    false,
                    new Color(-1),
                    lineWidth.getValue(),
                    line.getValue(),
                    box.getValue(),
                    color.getValue().getAlpha(),
                    depth.getValue());
        }
    }
}

