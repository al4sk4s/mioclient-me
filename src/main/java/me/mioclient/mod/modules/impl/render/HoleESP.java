package me.mioclient.mod.modules.impl.render;

import me.mioclient.api.events.impl.Render3DEvent;
import me.mioclient.api.managers.Managers;
import me.mioclient.api.util.interact.BlockUtil;
import me.mioclient.api.util.render.ColorUtil;
import me.mioclient.api.util.render.RenderUtil;
import me.mioclient.mod.modules.Category;
import me.mioclient.mod.modules.Module;
import me.mioclient.mod.modules.settings.Setting;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3i;

import java.awt.*;

public class HoleESP extends Module {
    
    private final Setting<Page> page =
            add(new Setting<>("Settings", Page.GLOBAL));

    //Fov

    private final Setting<Boolean> renderOwn =
            add(new Setting<>("RenderOwn", true, v -> page.getValue() == Page.GLOBAL));
    private final Setting<Boolean> fov =
            add(new Setting<>("FovOnly", true, v -> page.getValue() == Page.GLOBAL));
    private final Setting<Integer> range =
            add(new Setting<>("Range", 5, 0, 25, v -> page.getValue() == Page.GLOBAL));

    //Global

    private final Setting<Boolean> box =
            add(new Setting<>("Box", true, v -> page.getValue() == Page.GLOBAL).setParent());
    private final Setting<Boolean> gradientBox =
            add(new Setting<>("FadeBox", false, v -> box.isOpen() && page.getValue() == Page.GLOBAL));
    private final Setting<Boolean> invertGradientBox =
            add(new Setting<>("InvertBoxFade", false, v -> box.isOpen() && page.getValue() == Page.GLOBAL));

    private final Setting<Boolean> outline =
            add(new Setting<>("Outline", true, v -> page.getValue() == Page.GLOBAL).setParent());
    private final Setting<Boolean> gradientOutline =
            add(new Setting<>("FadeLine", false, v -> outline.isOpen() && page.getValue() == Page.GLOBAL));
    private final Setting<Boolean> invertGradientOutline =
            add(new Setting<>("InvertLineFade", false, v -> outline.isOpen() && page.getValue() == Page.GLOBAL));
    private final Setting<Boolean> separateHeight =
            add(new Setting<>("SeparateHeight", false, v -> outline.isOpen() && page.getValue() == Page.GLOBAL));
    private final Setting<Double> lineHeight =
            add(new Setting<>("LineHeight", -1.1, -2.0, 2.0, v -> outline.isOpen() && page.getValue() == Page.GLOBAL && separateHeight.getValue()));

    private final Setting<Boolean> wireframe =
            add(new Setting<>("Wireframe", true, v -> page.getValue() == Page.GLOBAL).setParent());
    private final Setting<WireframeMode> wireframeMode =
            add(new Setting<>("Mode", WireframeMode.FULL, v -> wireframe.isOpen() && page.getValue() == Page.GLOBAL));

    private final Setting<Double> height =
            add(new Setting<>("Height", -1.1, -2.0, 2.0, v -> page.getValue() == Page.GLOBAL));
    private final Setting<Integer> boxAlpha =
            add(new Setting<>("BoxAlpha", 80, 0, 255, v -> box.getValue() && page.getValue() == Page.GLOBAL));
    private final Setting<Float> lineWidth =
            add(new Setting<>("LineWidth", 0.5f, 0.1f, 5.0f, v -> (outline.getValue() || wireframe.getValue()) && page.getValue() == Page.GLOBAL));

    //Colors

    private final Setting<Boolean> rainbow =
            add(new Setting<>("Rainbow", false, v -> page.getValue() == Page.COLORS));

    private final Setting<Color> obbyColor =
            add(new Setting<>("Obby", new Color(0xC21D1D), v -> page.getValue() == Page.COLORS));
    private final Setting<Color> brockColor =
            add(new Setting<>("Bedrock", new Color(0x279A4B), v -> page.getValue() == Page.COLORS));

    private final Setting<Boolean> customOutline =
            add(new Setting<>("LineColor", false, v -> page.getValue() == Page.COLORS).setParent());
    private final Setting<Color> obbyLineColor =
            add(new Setting<>("ObbyLine", new Color(-1), v -> customOutline.isOpen() && page.getValue() == Page.COLORS));
    private final Setting<Color> brockLineColor =
            add(new Setting<>("BedrockLine", new Color(-1), v -> customOutline.isOpen() && page.getValue() == Page.COLORS));

    public HoleESP() {
        super("HoleESP", "Shows safe spots near you.", Category.RENDER);
    }

    private enum Page {
        COLORS,
        GLOBAL
    }

    private enum WireframeMode {
        FLAT,
        FULL
    }

    @Override
    public void onRender3D(Render3DEvent event) {

        assert (mc.renderViewEntity != null);

        Vec3i playerPos = new Vec3i(
                mc.renderViewEntity.posX,
                mc.renderViewEntity.posY,
                mc.renderViewEntity.posZ);

        for (int x = playerPos.getX() - range.getValue(); x < playerPos.getX() + range.getValue(); ++x) {

            for (int z = playerPos.getZ() - range.getValue(); z < playerPos.getZ() + range.getValue(); ++z) {

                int rangeY = 5;

                for (int y = playerPos.getY() + rangeY; y > playerPos.getY() - rangeY; --y) {

                    BlockPos pos = new BlockPos(x, y, z);

                    Color safeColor = rainbow.getValue() ? Managers.COLORS.getRainbow() : brockColor.getValue();
                    Color color = rainbow.getValue() ? Managers.COLORS.getRainbow() : obbyColor.getValue();
                    Color safecColor = brockLineColor.getValue();
                    Color cColor = obbyLineColor.getValue();

                    if (!mc.world.getBlockState(pos).getBlock().equals(Blocks.AIR) || !mc.world.getBlockState(pos.add(0, 1, 0)).getBlock().equals(Blocks.AIR) || !mc.world.getBlockState(pos.add(0, 2, 0)).getBlock().equals(Blocks.AIR) || pos.equals(new BlockPos(mc.player.posX, mc.player.posY, mc.player.posZ)) && !renderOwn.getValue() || !Managers.ROTATIONS.isInFov(pos) && fov.getValue()) continue;

                    if (mc.world.getBlockState(pos.north()).getBlock() == Blocks.AIR && mc.world.getBlockState(pos.north().up()).getBlock() == Blocks.AIR && mc.world.getBlockState(pos.north().down()).getBlock() == Blocks.BEDROCK && mc.world.getBlockState(pos.north(2)).getBlock() == Blocks.BEDROCK && mc.world.getBlockState(pos.east()).getBlock() == Blocks.BEDROCK && mc.world.getBlockState(pos.north().east()).getBlock() == Blocks.BEDROCK && mc.world.getBlockState(pos.west()).getBlock() == Blocks.BEDROCK && mc.world.getBlockState(pos.north().west()).getBlock() == Blocks.BEDROCK && mc.world.getBlockState(pos.south()).getBlock() == Blocks.BEDROCK && mc.world.getBlockState(pos.down()).getBlock() == Blocks.BEDROCK) {
                        drawDoubles(true, pos, safeColor, customOutline.getValue(), safecColor, lineWidth.getValue(), outline.getValue(), box.getValue(), boxAlpha.getValue(), true, height.getValue(), separateHeight.getValue() ? lineHeight.getValue() : height.getValue(), gradientBox.getValue(), gradientOutline.getValue(), invertGradientBox.getValue(), invertGradientOutline.getValue(), 0, wireframe.getValue(), wireframeMode.getValue() == WireframeMode.FLAT);

                    } else if (!(mc.world.getBlockState(pos.north()).getBlock() != Blocks.AIR || mc.world.getBlockState(pos.north().up()).getBlock() != Blocks.AIR || mc.world.getBlockState(pos.north().down()).getBlock() != Blocks.OBSIDIAN && mc.world.getBlockState(pos.north().down()).getBlock() != Blocks.BEDROCK || mc.world.getBlockState(pos.north(2)).getBlock() != Blocks.OBSIDIAN && mc.world.getBlockState(pos.north(2)).getBlock() != Blocks.BEDROCK || mc.world.getBlockState(pos.east()).getBlock() != Blocks.OBSIDIAN && mc.world.getBlockState(pos.east()).getBlock() != Blocks.BEDROCK || mc.world.getBlockState(pos.north().east()).getBlock() != Blocks.OBSIDIAN && mc.world.getBlockState(pos.north().east()).getBlock() != Blocks.BEDROCK || mc.world.getBlockState(pos.west()).getBlock() != Blocks.OBSIDIAN && mc.world.getBlockState(pos.west()).getBlock() != Blocks.BEDROCK || mc.world.getBlockState(pos.north().west()).getBlock() != Blocks.OBSIDIAN && mc.world.getBlockState(pos.north().west()).getBlock() != Blocks.BEDROCK || mc.world.getBlockState(pos.south()).getBlock() != Blocks.OBSIDIAN && mc.world.getBlockState(pos.south()).getBlock() != Blocks.BEDROCK || mc.world.getBlockState(pos.down()).getBlock() != Blocks.OBSIDIAN && mc.world.getBlockState(pos.down()).getBlock() != Blocks.BEDROCK)) {
                        drawDoubles(true, pos, color, customOutline.getValue(), cColor, lineWidth.getValue(), outline.getValue(), box.getValue(), boxAlpha.getValue(), true, height.getValue(), separateHeight.getValue() ? lineHeight.getValue() : height.getValue(), gradientBox.getValue(), gradientOutline.getValue(), invertGradientBox.getValue(), invertGradientOutline.getValue(), 0, wireframe.getValue(), wireframeMode.getValue() == WireframeMode.FLAT);
                    }

                    if (mc.world.getBlockState(pos.east()).getBlock() == Blocks.AIR && mc.world.getBlockState(pos.east().up()).getBlock() == Blocks.AIR && mc.world.getBlockState(pos.east().down()).getBlock() == Blocks.BEDROCK && mc.world.getBlockState(pos.east(2)).getBlock() == Blocks.BEDROCK && mc.world.getBlockState(pos.east(2).down()).getBlock() == Blocks.BEDROCK && mc.world.getBlockState(pos.north()).getBlock() == Blocks.BEDROCK && mc.world.getBlockState(pos.east().north()).getBlock() == Blocks.BEDROCK && mc.world.getBlockState(pos.west()).getBlock() == Blocks.BEDROCK && mc.world.getBlockState(pos.east().south()).getBlock() == Blocks.BEDROCK && mc.world.getBlockState(pos.south()).getBlock() == Blocks.BEDROCK && mc.world.getBlockState(pos.down()).getBlock() == Blocks.BEDROCK) {
                        drawDoubles(false, pos, safeColor, customOutline.getValue(), safecColor, lineWidth.getValue(), outline.getValue(), box.getValue(), boxAlpha.getValue(), true, height.getValue(), separateHeight.getValue() ? lineHeight.getValue() : height.getValue(), gradientBox.getValue(), gradientOutline.getValue(), invertGradientBox.getValue(), invertGradientOutline.getValue(), 0, wireframe.getValue(), wireframeMode.getValue() == WireframeMode.FLAT);

                    } else if (!(mc.world.getBlockState(pos.east()).getBlock() != Blocks.AIR || mc.world.getBlockState(pos.east().up()).getBlock() != Blocks.AIR || mc.world.getBlockState(pos.east().down()).getBlock() != Blocks.BEDROCK && mc.world.getBlockState(pos.east().down()).getBlock() != Blocks.OBSIDIAN || mc.world.getBlockState(pos.east(2)).getBlock() != Blocks.BEDROCK && mc.world.getBlockState(pos.east(2)).getBlock() != Blocks.OBSIDIAN || mc.world.getBlockState(pos.north()).getBlock() != Blocks.BEDROCK && mc.world.getBlockState(pos.north()).getBlock() != Blocks.OBSIDIAN || mc.world.getBlockState(pos.east().north()).getBlock() != Blocks.BEDROCK && mc.world.getBlockState(pos.east().north()).getBlock() != Blocks.OBSIDIAN || mc.world.getBlockState(pos.west()).getBlock() != Blocks.BEDROCK && mc.world.getBlockState(pos.west()).getBlock() != Blocks.OBSIDIAN || mc.world.getBlockState(pos.east().south()).getBlock() != Blocks.BEDROCK && mc.world.getBlockState(pos.east().south()).getBlock() != Blocks.OBSIDIAN || mc.world.getBlockState(pos.south()).getBlock() != Blocks.BEDROCK && mc.world.getBlockState(pos.south()).getBlock() != Blocks.OBSIDIAN || mc.world.getBlockState(pos.down()).getBlock() != Blocks.BEDROCK && mc.world.getBlockState(pos.down()).getBlock() != Blocks.OBSIDIAN)) {
                        drawDoubles(false, pos, color, customOutline.getValue(), cColor, lineWidth.getValue(), outline.getValue(), box.getValue(), boxAlpha.getValue(), true, height.getValue(), separateHeight.getValue() ? lineHeight.getValue() : height.getValue(), gradientBox.getValue(), gradientOutline.getValue(), invertGradientBox.getValue(), invertGradientOutline.getValue(), 0, wireframe.getValue(), wireframeMode.getValue() == WireframeMode.FLAT);
                    }

                    if (mc.world.getBlockState(pos.north()).getBlock() == Blocks.BEDROCK && mc.world.getBlockState(pos.east()).getBlock() == Blocks.BEDROCK && mc.world.getBlockState(pos.west()).getBlock() == Blocks.BEDROCK && mc.world.getBlockState(pos.south()).getBlock() == Blocks.BEDROCK && mc.world.getBlockState(pos.down()).getBlock() == Blocks.BEDROCK) {
                        drawHoleESP(pos, safeColor, customOutline.getValue(), safecColor, lineWidth.getValue(), outline.getValue(), box.getValue(), boxAlpha.getValue(), true, height.getValue(), separateHeight.getValue() ? lineHeight.getValue() : height.getValue(), gradientBox.getValue(), gradientOutline.getValue(), invertGradientBox.getValue(), invertGradientOutline.getValue(), 0, wireframe.getValue(), wireframeMode.getValue() == WireframeMode.FLAT);
                        continue;
                    }

                    if (!BlockUtil.isUnsafe(mc.world.getBlockState(pos.down()).getBlock()) || !BlockUtil.isUnsafe(mc.world.getBlockState(pos.east()).getBlock()) || !BlockUtil.isUnsafe(mc.world.getBlockState(pos.west()).getBlock()) || !BlockUtil.isUnsafe(mc.world.getBlockState(pos.south()).getBlock()) || !BlockUtil.isUnsafe(mc.world.getBlockState(pos.north()).getBlock())) continue;

                    drawHoleESP(pos, color, customOutline.getValue(), cColor, lineWidth.getValue(), outline.getValue(), box.getValue(), boxAlpha.getValue(), true, height.getValue(), separateHeight.getValue() ? lineHeight.getValue() : height.getValue(), gradientBox.getValue(), gradientOutline.getValue(), invertGradientBox.getValue(), invertGradientOutline.getValue(), 0, wireframe.getValue(), wireframeMode.getValue() == WireframeMode.FLAT);
                }
            }
        }
    }

    public void drawDoubles(boolean faceNorth, BlockPos pos, Color color, boolean secondC, Color secondColor, float lineWidth, boolean outline, boolean box, int boxAlpha, boolean air, double height, double lineHeight, boolean gradientBox, boolean gradientOutline, boolean invertGradientBox, boolean invertGradientOutline, int gradientAlpha, boolean cross, boolean flatCross) {
        drawHoleESP(pos, color, secondC, secondColor, lineWidth, outline, box, boxAlpha, air, height, lineHeight, gradientBox, gradientOutline, invertGradientBox, invertGradientOutline, gradientAlpha, cross, flatCross);
        drawHoleESP(faceNorth ? pos.north() : pos.east(), color, secondC, secondColor, lineWidth, outline, box, boxAlpha, air, height, lineHeight, gradientBox, gradientOutline, invertGradientBox, invertGradientOutline, gradientAlpha, cross, flatCross);
    }

    public void drawHoleESP(BlockPos pos, Color color, boolean secondC, Color secondColor, float lineWidth, boolean outline, boolean box, int boxAlpha, boolean air, double height, double lineHeight, boolean gradientBox, boolean gradientOutline, boolean invertGradientBox, boolean invertGradientOutline, int gradientAlpha, boolean cross, boolean flatCross) {
        if (box) {
            RenderUtil.drawBox(pos, ColorUtil.injectAlpha(color, boxAlpha), height, gradientBox, invertGradientBox, gradientAlpha);
        }
        if (outline) {
            RenderUtil.drawBlockOutline(pos, secondC ? secondColor : color, lineWidth, air, lineHeight, gradientOutline, invertGradientOutline, gradientAlpha, false);
        }
        if (cross) {
            RenderUtil.drawBlockWireframe(pos, secondC ? secondColor : color, lineWidth, height, flatCross);
        }
    }
}
