package me.mioclient.mod.gui.click.items.buttons;

import com.mojang.realmsclient.gui.ChatFormatting;
import me.mioclient.api.managers.Managers;
import me.mioclient.api.util.render.ColorUtil;
import me.mioclient.api.util.render.RenderUtil;
import me.mioclient.mod.gui.click.Component;
import me.mioclient.mod.gui.screen.MioClickGui;
import me.mioclient.mod.modules.impl.client.ClickGui;
import me.mioclient.mod.modules.settings.Setting;
import org.lwjgl.input.Mouse;

import java.awt.*;

public class Slider extends Button {

    private final Number min;
    private final Number max;
    private final int difference;
    public Setting setting;
    private float renderWidth;
    private float prevRenderWidth;

    public Slider(Setting setting) {
        super(setting.getName());
        this.setting = setting;
        min = (Number) setting.getMinValue();
        max = (Number) setting.getMaxValue();
        difference = max.intValue() - min.intValue();
        width = 15;
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        boolean newStyle = ClickGui.INSTANCE.style.getValue() == ClickGui.Style.NEW;
        boolean future = ClickGui.INSTANCE.style.getValue() == ClickGui.Style.FUTURE;
        boolean dotgod = ClickGui.INSTANCE.style.getValue() == ClickGui.Style.DOTGOD;

        dragSetting(mouseX, mouseY);
        setRenderWidth(x + ((float) width + 7.4f) * partialMultiplier());
        RenderUtil.drawRect(x, y, x + (float) width + 7.4f, y + (float) height - 0.5f, !isHovering(mouseX, mouseY) ? 0x11555555 : -2007673515);

        if (future) {
            RenderUtil.drawRect(x, y, ((Number) setting.getValue()).floatValue() <= min.floatValue() ? x : getRenderWidth(), y + (float) height - 0.5f, (!isHovering(mouseX, mouseY) ? Managers.COLORS.getCurrentWithAlpha(99) : Managers.COLORS.getCurrentWithAlpha(120)));

        } else if (dotgod) {
            RenderUtil.drawRect(x, y, ((Number) setting.getValue()).floatValue() <= min.floatValue() ? x : getRenderWidth(), y + (float) height - 0.5f, (!isHovering(mouseX, mouseY) ? Managers.COLORS.getCurrentWithAlpha(65) : Managers.COLORS.getCurrentWithAlpha(90)));

        } else {
            if (isHovering(mouseX, mouseY) && Mouse.isButtonDown(0)) {
                RenderUtil.drawHGradientRect(x, y, ((Number) setting.getValue()).floatValue() <= min.floatValue() ? x : getRenderWidth(), y + (float) height - 0.5f, ColorUtil.pulseColor(new Color(ClickGui.INSTANCE.color.getValue().getRed(), ClickGui.INSTANCE.color.getValue().getGreen(), ClickGui.INSTANCE.color.getValue().getBlue(), 200), 50, 1).getRGB(), ColorUtil.pulseColor(new Color(ClickGui.INSTANCE.color.getValue().getRed(), ClickGui.INSTANCE.color.getValue().getGreen(), ClickGui.INSTANCE.color.getValue().getBlue(), 200), 50, 1000).getRGB());

            } else {
                RenderUtil.drawRect(x, y, ((Number) setting.getValue()).floatValue() <= min.floatValue() ? x : getRenderWidth(), y + (float) height - 0.5f, !isHovering(mouseX, mouseY) ? Managers.COLORS.getCurrentWithAlpha(120) : Managers.COLORS.getCurrentWithAlpha(200));
            }
            RenderUtil.drawLine(x + 1, y, x + 1, y + (float) height - 0.5f, 0.9f, Managers.COLORS.getCurrentWithAlpha(255));
        }

        if (dotgod) {
            Managers.TEXT.drawStringWithShadow((getName().toLowerCase() + ":") + " " + ChatFormatting.GRAY + (setting.getValue() instanceof Float ? setting.getValue() : Double.valueOf(((Number) setting.getValue()).doubleValue())), x + 2.3f, y - 1.7f - (float) MioClickGui.INSTANCE.getTextOffset(), Managers.COLORS.getCurrentGui(240));

        } else {
            Managers.TEXT.drawStringWithShadow((newStyle ? getName().toLowerCase() + ":" : getName()) + " " + ChatFormatting.GRAY + (setting.getValue() instanceof Float ? setting.getValue() : Double.valueOf(((Number) setting.getValue()).doubleValue())), x + 2.3f, y - 1.7f - (float) MioClickGui.INSTANCE.getTextOffset(), -1);
        }
    }

    @Override
    public void mouseClicked(int mouseX, int mouseY, int mouseButton) {
        super.mouseClicked(mouseX, mouseY, mouseButton);
        if (isHovering(mouseX, mouseY)) {
            setSettingFromX(mouseX);
        }
    }

    @Override
    public boolean isHovering(int mouseX, int mouseY) {
        for (Component component : MioClickGui.INSTANCE.getComponents()) {
            if (!component.drag) continue;
            return false;
        }
        return (float) mouseX >= getX() && (float) mouseX <= getX() + (float) getWidth() + 8.0f && (float) mouseY >= getY() && (float) mouseY <= getY() + (float) height;
    }

    @Override
    public void update() {
        setHidden(!setting.isVisible());
    }

    private void dragSetting(int mouseX, int mouseY) {
        if (isHovering(mouseX, mouseY) && Mouse.isButtonDown(0)) {
            setSettingFromX(mouseX);
        }
    }

    @Override
    public int getHeight() {
        return ClickGui.INSTANCE.getButtonHeight() - 1;
    }

    private void setSettingFromX(int mouseX) {
        float percent = ((float) mouseX - x) / ((float) width + 7.4f);
        if (setting.getValue() instanceof Double) {
            double result = (Double) setting.getMinValue() + (double) ((float) difference * percent);
            setting.setValue((double) Math.round(10.0 * result) / 10.0);
        } else if (setting.getValue() instanceof Float) {
            float result = ((Float) setting.getMinValue()).floatValue() + (float) difference * percent;
            setting.setValue(Float.valueOf((float) Math.round(10.0f * result) / 10.0f));
        } else if (setting.getValue() instanceof Integer) {
            setting.setValue((Integer) setting.getMinValue() + (int) ((float) difference * percent));
        }
    }

    private float middle() {
        return max.floatValue() - min.floatValue();
    }

    private float part() {
        return ((Number) setting.getValue()).floatValue() - min.floatValue();
    }

    private float partialMultiplier() {
        return part() / middle();
    }

    /**
     * @credit cattyn
     */

    public void setRenderWidth(float renderWidth) {
        if (this.renderWidth == renderWidth) return;
        prevRenderWidth = this.renderWidth;
        this.renderWidth = renderWidth;
    }

    public float getRenderWidth() {
        if (Managers.FPS.getFPS() < 20) {
            return renderWidth;
        }
        renderWidth = prevRenderWidth + (renderWidth - prevRenderWidth) * mc.getRenderPartialTicks() / (8 * (Math.min(240, Managers.FPS.getFPS()) / 240f));
        return renderWidth;
    }
}

