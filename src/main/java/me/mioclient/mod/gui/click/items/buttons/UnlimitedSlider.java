package me.mioclient.mod.gui.click.items.buttons;

import com.mojang.realmsclient.gui.ChatFormatting;
import me.mioclient.api.managers.Managers;
import me.mioclient.api.util.render.RenderUtil;
import me.mioclient.mod.gui.screen.MioClickGui;
import me.mioclient.mod.modules.impl.client.ClickGui;
import me.mioclient.mod.modules.settings.Setting;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.init.SoundEvents;

public class UnlimitedSlider
        extends Button {
    public Setting setting;

    public UnlimitedSlider(Setting setting) {
        super(setting.getName());
        this.setting = setting;
        width = 15;
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        boolean newStyle = ClickGui.INSTANCE.style.getValue() == ClickGui.Style.NEW;

        RenderUtil.drawRect(x, y, x + (float) width + 7.4f, y + (float) height - 0.5f, !isHovering(mouseX, mouseY) ? Managers.COLORS.getCurrentWithAlpha(120) : Managers.COLORS.getCurrentWithAlpha(200));
        RenderUtil.drawLine(x + 1, y, x + 1, y + (float) height - 0.5f, 0.9f, Managers.COLORS.getCurrentWithAlpha(255));
        Managers.TEXT.drawStringWithShadow(" - " + (newStyle ? setting.getName().toLowerCase() + ":" : setting.getName()) + " " + ChatFormatting.GRAY + setting.getValue() + ChatFormatting.WHITE + " +", x + 2.3f, y - 1.7f - (float) MioClickGui.INSTANCE.getTextOffset(), getState() ? -1 : -5592406);
    }

    @Override
    public void mouseClicked(int mouseX, int mouseY, int mouseButton) {
        super.mouseClicked(mouseX, mouseY, mouseButton);
        if (isHovering(mouseX, mouseY)) {
            mc.getSoundHandler().playSound(PositionedSoundRecord.getMasterRecord(SoundEvents.UI_BUTTON_CLICK, 1.0f));
            if (isRight(mouseX)) {
                if (setting.getValue() instanceof Double) {
                    setting.setValue((Double) setting.getValue() + 1.0);
                } else if (setting.getValue() instanceof Float) {
                    setting.setValue(Float.valueOf(((Float) setting.getValue()).floatValue() + 1.0f));
                } else if (setting.getValue() instanceof Integer) {
                    setting.setValue((Integer) setting.getValue() + 1);
                }
            } else if (setting.getValue() instanceof Double) {
                setting.setValue((Double) setting.getValue() - 1.0);
            } else if (setting.getValue() instanceof Float) {
                setting.setValue(Float.valueOf(((Float) setting.getValue()).floatValue() - 1.0f));
            } else if (setting.getValue() instanceof Integer) {
                setting.setValue((Integer) setting.getValue() - 1);
            }
        }
    }

    @Override
    public void update() {
        setHidden(!setting.isVisible());
    }

    @Override
    public int getHeight() {
        return 14;
    }

    @Override
    public void toggle() {
    }

    @Override
    public boolean getState() {
        return true;
    }

    public boolean isRight(int x) {
        return (float) x > this.x + ((float) width + 7.4f) / 2.0f;
    }
}

