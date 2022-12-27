package me.mioclient.mod.modules.impl.render;

import me.mioclient.api.events.impl.Render3DEvent;
import me.mioclient.api.managers.Managers;
import me.mioclient.api.util.interact.BlockUtil;
import me.mioclient.api.util.render.RenderUtil;
import me.mioclient.mod.modules.Category;
import me.mioclient.mod.modules.Module;
import me.mioclient.mod.modules.settings.Setting;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3i;

import java.awt.*;

/**
 * @author t.me/asphyxia1337
 */

public class VoidESP extends Module {

    private final Setting<Integer> rangeX =
            add(new Setting<>("RangeX", 10, 0, 25));
    private final Setting<Integer> rangeY =
            add(new Setting<>("RangeY", 5, 0, 25));
    private final Setting<Mode> mode =
            add(new Setting<>("Mode", Mode.FULL));
    private final Setting<Integer> height =
            add(new Setting<>("Height", 1, 0, 4, v -> mode.getValue() == Mode.FULL));
    private final Setting<Boolean> fill =
            add(new Setting<>("Fill", true));
    private final Setting<Boolean> line =
            add(new Setting<>("Outline", true));
    private final Setting<Boolean> wireframe =
            add(new Setting<>("Wireframe", true));
    private final Setting<Color> color =
            add(new Setting<>("Color", new Color(0x64e80e00, true)));

    public VoidESP() {
        super("VoidESP", "Highlights void blocks", Category.RENDER);
    }

    private enum Mode {
        FLAT,
        SLAB,
        FULL
    }

    @Override
    public String getInfo() {
        return Managers.TEXT.normalizeCases(mode.getValue());
    }

    @Override
    public void onRender3D(Render3DEvent event) {
        if (!fullNullCheck()) {
            assert mc.renderViewEntity != null;

            Vec3i playerPos = new Vec3i(mc.renderViewEntity.posX, mc.renderViewEntity.posY, mc.renderViewEntity.posZ);

            BlockPos pos;

            for (int x = playerPos.getX() - rangeX.getValue(); x < playerPos.getX() + rangeX.getValue(); ++x) {
                for (int z = playerPos.getZ() - rangeX.getValue(); z < playerPos.getZ() + rangeX.getValue(); ++z) {
                    for (int y = playerPos.getY() + rangeY.getValue(); y > playerPos.getY() - rangeY.getValue(); --y) {
                        pos = new BlockPos(x, y, z);

                        double h = 0.0;

                        if (mode.getValue() == Mode.FLAT) {
                            h = -1.0;
                        } else if (mode.getValue() == Mode.SLAB) {
                            h = -0.8;
                        }


                        if (isVoid(pos)) {
                            if (mode.getValue() == Mode.FULL) {
                                if (height.getValue() == 1 || !isAir(pos.up())) {
                                    drawVoidESP(pos, color.getValue(), false, new Color(-1), 0.8f, line.getValue(), fill.getValue(), color.getValue().getAlpha(), true, 0.0, false, false, false, false, 0, wireframe.getValue(), true);

                                } else if (height.getValue() == 2 && isAir(pos.up())) {
                                    drawVoidESP(pos, color.getValue(), false, new Color(-1), 0.8f, line.getValue(), fill.getValue(), color.getValue().getAlpha(), true, 1.0, false, false, false, false, 0, wireframe.getValue(), true);

                                } else if (height.getValue() == 3 && isAir(pos.up()) && isAir(pos.up().up())) {
                                    drawVoidESP(pos, color.getValue(), false, new Color(-1), 0.8f, line.getValue(), fill.getValue(), color.getValue().getAlpha(), true, 2.0, false, false, false, false, 0, wireframe.getValue(), true);

                                } else if (height.getValue() == 4 && isAir(pos.up()) && isAir(pos.up().up()) && isAir(pos.up().up().up())) {
                                    drawVoidESP(pos, color.getValue(), false, new Color(-1), 0.8f, line.getValue(), fill.getValue(), color.getValue().getAlpha(), true, 3.0, false, false, false, false, 0, wireframe.getValue(), true);
                                }

                            } else {
                                drawVoidESP(pos, color.getValue(), false, new Color(-1), 0.8f, line.getValue(), fill.getValue(), color.getValue().getAlpha(), true, h, false, false, false, false, 0, wireframe.getValue(), true);
                            }
                        }
                    }
                }
            }
        }
    }

    private boolean isVoid(BlockPos pos) {
        if (pos.getY() != 0) return false;
        return !(BlockUtil.getBlock(pos) == Blocks.BEDROCK);
    }

    public static boolean isAir(BlockPos pos) {
        return BlockUtil.getBlock(pos) == Blocks.AIR;
    }

    private void drawVoidESP(BlockPos pos, Color color, boolean secondC, Color secondColor, float lineWidth, boolean outline, boolean box, int boxAlpha, boolean air, double height, boolean gradientBox, boolean gradientOutline, boolean invertGradientBox, boolean invertGradientOutline, int gradientAlpha, boolean cross, boolean flatCross) {
        if (box) {
            RenderUtil.drawBox(pos, new Color(color.getRed(), color.getGreen(), color.getBlue(), boxAlpha), height, gradientBox, invertGradientBox, gradientAlpha);
        }
        if (outline) {
            RenderUtil.drawBlockOutline(pos, secondC ? secondColor : color, lineWidth, air, height, gradientOutline, invertGradientOutline, gradientAlpha, false);
        }
        if (cross) {
            RenderUtil.drawBlockWireframe(pos, secondC ? secondColor : color, lineWidth, height, flatCross);
        }
    }
}
