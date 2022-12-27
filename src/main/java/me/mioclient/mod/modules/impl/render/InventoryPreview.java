package me.mioclient.mod.modules.impl.render;

import me.mioclient.api.events.impl.Render2DEvent;
import me.mioclient.api.managers.Managers;
import me.mioclient.api.util.render.RenderUtil;
import me.mioclient.mod.modules.Category;
import me.mioclient.mod.modules.Module;
import me.mioclient.mod.modules.settings.Setting;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;

import java.awt.*;

public class InventoryPreview extends Module {

    public Setting<XOffset> xOffset =
            add(new Setting<>("XOffset", XOffset.CUSTOM));
    public Setting<Integer> x =
            add(new Setting<>("X", 500, 0, 1000, v -> xOffset.getValue() == XOffset.CUSTOM));

    public Setting<YOffset> yOffset =
            add(new Setting<>("YOffset", YOffset.CUSTOM));
    public Setting<Integer> y =
            add(new Setting<>("Y", 2, 0, 1000, v -> yOffset.getValue() == YOffset.CUSTOM));

    public Setting<Boolean> outline =
            add(new Setting<>("Outline", true).setParent());
    public Setting<Color> lineColor =
            add(new Setting<>("LineColor", new Color(10, 10, 10, 100), v -> outline.isOpen()));
    public Setting<Color> secondColor =
            add(new Setting<>("SecondColor", new Color(30, 30, 30, 100), v -> outline.isOpen()).injectBoolean(true));

    public Setting<Boolean> rect =
            add(new Setting<>("Rect", true).setParent());
    public Setting<Color> rectColor =
            add(new Setting<>("RectColor", new Color(10, 10, 10, 50), v -> rect.isOpen()));

    public InventoryPreview() {
        super("InventoryPreview", "Allows you to see your own inventory without opening it.", Category.RENDER, true);
    }

    private enum XOffset {
        CUSTOM,
        LEFT,
        RIGHT
    }

    private enum YOffset {
        CUSTOM,
        TOP,
        BOTTOM
    }

    @Override
    public void onRender2D(Render2DEvent event) {
        if (fullNullCheck()) return;

        GlStateManager.enableTexture2D();
        GlStateManager.disableLighting();
        GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);

        GlStateManager.disableDepth();

        int x = xOffset.getValue() == XOffset.CUSTOM ? this.x.getValue() : (xOffset.getValue() == XOffset.LEFT ? 0 : Managers.TEXT.scaledWidth - 172);
        int y = yOffset.getValue() == YOffset.CUSTOM ? this.y.getValue() : (yOffset.getValue() == YOffset.TOP ? 0 : Managers.TEXT.scaledHeight - 74);

        if (outline.getValue()) {
            RenderUtil.drawNameTagOutline(
                    x + 6.5f,
                    y + 16.5f,
                    x + 171.5f,
                    y + 73.5f,
                    1.0f,
                    lineColor.getValue().getRGB(),
                    secondColor.booleanValue ? secondColor.getValue().getRGB() : lineColor.getValue().getRGB(),
                    false);
        }

        if (rect.getValue()) {
            RenderUtil.drawRect(
                    x + 7,
                    y + 17,
                    x + 171,
                    y + 73,
                    rectColor.getValue().getRGB());
        }

        GlStateManager.enableDepth();

        RenderHelper.enableGUIStandardItemLighting();
        GlStateManager.enableRescaleNormal();
        GlStateManager.enableColorMaterial();

        GlStateManager.enableLighting();

        NonNullList<ItemStack> items = mc.player.inventory.mainInventory;

        for (int i = 0; i < items.size() - 9; i++) {

            int iX = x + i % 9 * 18 + 8;
            int iY = y + i / 9 * 18 + 18;

            ItemStack stack = items.get(i + 9);

            mc.getItemRenderer().itemRenderer.zLevel = 501.0f;

            RenderUtil.itemRender.renderItemAndEffectIntoGUI(stack, iX, iY);
            RenderUtil.itemRender.renderItemOverlayIntoGUI(mc.fontRenderer, stack, iX, iY, null);

            mc.getItemRenderer().itemRenderer.zLevel = 0.0f;
        }

        GlStateManager.disableLighting();
        GlStateManager.disableBlend();
        GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
    }
}
