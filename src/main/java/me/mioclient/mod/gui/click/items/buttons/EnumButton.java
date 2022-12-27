package me.mioclient.mod.gui.click.items.buttons;

import com.mojang.realmsclient.gui.ChatFormatting;
import me.mioclient.api.managers.Managers;
import me.mioclient.api.util.render.RenderUtil;
import me.mioclient.mod.gui.screen.MioClickGui;
import me.mioclient.mod.modules.impl.client.ClickGui;
import me.mioclient.mod.modules.settings.Setting;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.init.SoundEvents;

import java.util.Objects;

public class EnumButton
        extends Button {
    public Setting setting;

    public EnumButton(Setting setting) {
        super(setting.getName());
        this.setting = setting;
        width = 15;
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        boolean newStyle = ClickGui.INSTANCE.style.getValue() == ClickGui.Style.NEW || ClickGui.INSTANCE.style.getValue() == ClickGui.Style.DOTGOD;
        boolean future = ClickGui.INSTANCE.style.getValue() == ClickGui.Style.FUTURE;
        boolean dotgod = ClickGui.INSTANCE.style.getValue() == ClickGui.Style.DOTGOD;

        if (future) {
            RenderUtil.drawRect(x, y, x + (float) width + 7.4f, y + (float) height - 0.5f, getState() ? (!isHovering(mouseX, mouseY) ? Managers.COLORS.getCurrentWithAlpha(99) : Managers.COLORS.getCurrentWithAlpha(120)) : (!isHovering(mouseX, mouseY) ? Managers.COLORS.getCurrentWithAlpha(26) : Managers.COLORS.getCurrentWithAlpha(55)));

        } else if (dotgod) {
            RenderUtil.drawRect(x, y, x + (float) width + 7.4f, y + (float) height - 0.5f, getState() ? (!isHovering(mouseX, mouseY) ? Managers.COLORS.getCurrentWithAlpha(65) : Managers.COLORS.getCurrentWithAlpha(90)) : (!isHovering(mouseX, mouseY) ? Managers.COLORS.getCurrentWithAlpha(26) : Managers.COLORS.getCurrentWithAlpha(35)));

        } else {
            RenderUtil.drawRect(x, y, x + (float) width + 7.4f, y + (float) height - 0.5f, getState() ? (!isHovering(mouseX, mouseY) ? Managers.COLORS.getCurrentWithAlpha(120) : Managers.COLORS.getCurrentWithAlpha(200)) : (!isHovering(mouseX, mouseY) ? 0x11555555 : -2007673515));
        }
        Managers.TEXT.drawStringWithShadow((newStyle ? setting.getName().toLowerCase() + ":" : setting.getName()) + " " + ChatFormatting.GRAY + (setting.getCurrentEnumName().equalsIgnoreCase("ABC") ? "ABC" : setting.getCurrentEnumName()), x + 2.3f, y - 1.7f - (float) MioClickGui.INSTANCE.getTextOffset(), getState() ? -1 : -5592406);

        int y = (int) this.y;

        if (setting.open) {
            for (Object o : setting.getValue().getClass().getEnumConstants()) {

                y += 12;
                String s = !Objects.equals(o.toString(), "ABC") ? Character.toUpperCase(o.toString().charAt(0)) + o.toString().toLowerCase().substring(1) : o.toString();

                Managers.TEXT.drawStringWithShadow((setting.getCurrentEnumName().equals(s) ? ChatFormatting.WHITE : ChatFormatting.GRAY) + s, width / 2.0f - Managers.TEXT.getStringWidth(s) / 2.0f + 2.0f + x, y + (12 / 2f) - (mc.fontRenderer.FONT_HEIGHT / 2f) + 3.5f, -1);
            }
        }
    }

    @Override
    public void update() {
        setHidden(!setting.isVisible());
    }

    @Override
    public void mouseClicked(int mouseX, int mouseY, int mouseButton) {
        super.mouseClicked(mouseX, mouseY, mouseButton);
        if (isHovering(mouseX, mouseY)) {
            mc.getSoundHandler().playSound(PositionedSoundRecord.getMasterRecord(SoundEvents.UI_BUTTON_CLICK, 1.0f));
        }
        if (mouseButton == 1 && isHovering(mouseX, mouseY)) {
            setting.open = !setting.open;
            mc.getSoundHandler().playSound(PositionedSoundRecord.getMasterRecord(SoundEvents.UI_BUTTON_CLICK, 1.0f));
        }

        if (setting.open) {
            for (Object o : setting.getValue().getClass().getEnumConstants()) {
                y += 12;
                if (mouseX > x && mouseX < x + width && mouseY > y && mouseY < y + 12 + 3.5f && mouseButton == 0) {
                    setting.setEnumValue(String.valueOf(o));
                    mc.getSoundHandler().playSound(PositionedSoundRecord.getMasterRecord(SoundEvents.UI_BUTTON_CLICK, 1.0f));
                }
            }
        }
    }

    @Override
    public int getHeight() {
        return ClickGui.INSTANCE.getButtonHeight() - 1;
    }

    @Override
    public void toggle() {
        setting.increaseEnum();
    }

    @Override
    public boolean getState() {
        return true;
    }
}

