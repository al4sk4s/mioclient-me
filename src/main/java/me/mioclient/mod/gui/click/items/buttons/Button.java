package me.mioclient.mod.gui.click.items.buttons;

import me.mioclient.api.managers.Managers;
import me.mioclient.api.util.render.RenderUtil;
import me.mioclient.mod.gui.click.Component;
import me.mioclient.mod.gui.click.items.Item;
import me.mioclient.mod.gui.screen.MioClickGui;
import me.mioclient.mod.modules.impl.client.ClickGui;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.init.SoundEvents;

public class Button extends Item {

    private boolean state;

    public Button(String name) {
        super(name);
        height = ClickGui.INSTANCE.getButtonHeight();
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        boolean newStyle = ClickGui.INSTANCE.style.getValue() == ClickGui.Style.NEW;
        boolean future = ClickGui.INSTANCE.style.getValue() == ClickGui.Style.FUTURE;
        boolean dotgod = ClickGui.INSTANCE.style.getValue() == ClickGui.Style.DOTGOD;

        if (newStyle) {
            RenderUtil.drawRect(x, y, x + (float) width, y + (float) height - 0.5f, (!isHovering(mouseX, mouseY) ? 0x11555555 : -2007673515));

            Managers.TEXT.drawStringWithShadow(getName(),
                    x + 2.3f, y - 2.0f - (float) MioClickGui.INSTANCE.getTextOffset(),
                    getState() ? Managers.COLORS.getCurrentGui(240) : -1);

        } else if (dotgod) {
            RenderUtil.drawRect(x, y, x + (float) width, y + (float) height - 0.5f, getState() ? (!isHovering(mouseX, mouseY) ? Managers.COLORS.getCurrentWithAlpha(65) : Managers.COLORS.getCurrentWithAlpha(90)) : (!isHovering(mouseX, mouseY) ? Managers.COLORS.getCurrentWithAlpha(26) : Managers.COLORS.getCurrentWithAlpha(35)));

            Managers.TEXT.drawStringWithShadow(getName(),
                    x + 2.3f, y - 2.0f - (float) MioClickGui.INSTANCE.getTextOffset(),
                    getState() ? Managers.COLORS.getCurrentGui(240) : 0xB0B0B0);

        } else if (future) {
            RenderUtil.drawRect(x, y, x + (float) width, y + (float) height - 0.5f, getState() ? (!isHovering(mouseX, mouseY) ? Managers.COLORS.getCurrentWithAlpha(99) : Managers.COLORS.getCurrentWithAlpha(120)) : (!isHovering(mouseX, mouseY) ? Managers.COLORS.getCurrentWithAlpha(26) : Managers.COLORS.getCurrentWithAlpha(55)));

            Managers.TEXT.drawStringWithShadow(getName(),
                    x + 2.3f,
                    y - 2.0f - (float) MioClickGui.INSTANCE.getTextOffset(),
                    getState() ? -1 : -5592406);

        } else {
            RenderUtil.drawRect(x, y, x + (float) width, y + (float) height - 0.5f, getState() ? (!isHovering(mouseX, mouseY) ? Managers.COLORS.getCurrentWithAlpha(120) : Managers.COLORS.getCurrentWithAlpha(200)) : (!isHovering(mouseX, mouseY) ? 0x11555555 : -2007673515));

            Managers.TEXT.drawStringWithShadow(getName(),
                    x + 2.3f,
                    y - 2.0f - (float) MioClickGui.INSTANCE.getTextOffset(),
                    getState() ? -1 : -5592406);
        }
    }

    @Override
    public void mouseClicked(int mouseX, int mouseY, int mouseButton) {
        if (mouseButton == 0 && isHovering(mouseX, mouseY)) {
            onMouseClick();
        }
    }

    public void onMouseClick() {
        state = !state;
        toggle();
        mc.getSoundHandler().playSound(PositionedSoundRecord.getMasterRecord(SoundEvents.UI_BUTTON_CLICK, 1.0f));
    }

    public void toggle() {
    }

    public boolean getState() {
        return state;
    }

    @Override
    public int getHeight() {
        return ClickGui.INSTANCE.getButtonHeight() - 1;
    }

    public boolean isHovering(int mouseX, int mouseY) {
        for (Component component : MioClickGui.INSTANCE.getComponents()) {
            if (!component.drag) continue;
            return false;
        }
        return (float) mouseX >= getX() && (float) mouseX <= getX() + (float) getWidth() && (float) mouseY >= getY() && (float) mouseY <= getY() + (float) height;
    }
}

