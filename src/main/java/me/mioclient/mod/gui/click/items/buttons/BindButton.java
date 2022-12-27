package me.mioclient.mod.gui.click.items.buttons;

import com.mojang.realmsclient.gui.ChatFormatting;
import me.mioclient.api.managers.Managers;
import me.mioclient.api.util.render.ColorUtil;
import me.mioclient.api.util.render.RenderUtil;
import me.mioclient.mod.gui.screen.MioClickGui;
import me.mioclient.mod.modules.impl.client.ClickGui;
import me.mioclient.mod.modules.settings.Bind;
import me.mioclient.mod.modules.settings.Setting;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.init.SoundEvents;

public class BindButton
        extends Button {
    private final Setting setting;
    public boolean isListening;

    public BindButton(Setting setting) {
        super(setting.getName());
        this.setting = setting;
        width = 15;
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        boolean newStyle = ClickGui.INSTANCE.style.getValue() == ClickGui.Style.NEW || ClickGui.INSTANCE.style.getValue() == ClickGui.Style.DOTGOD;

        int color = ColorUtil.toARGB(ClickGui.INSTANCE.color.getValue().getRed(), ClickGui.INSTANCE.color.getValue().getGreen(), ClickGui.INSTANCE.color.getValue().getBlue(), 255);
        RenderUtil.drawRect(x, y, x + (float) width + 7.4f, y + (float) height - 0.5f, getState() ? (!isHovering(mouseX, mouseY) ? 0x11555555 : -2007673515) : (!isHovering(mouseX, mouseY) ? Managers.COLORS.getCurrentGui(200) : Managers.COLORS.getCurrentGui(90)));
        if (isListening) {
            Managers.TEXT.drawStringWithShadow("Press a Key...", x + 2.3f, y - 1.7f - (float) MioClickGui.INSTANCE.getTextOffset(), -1);
        } else {
            Managers.TEXT.drawStringWithShadow((newStyle ? setting.getName().toLowerCase() : setting.getName()) + " " + ChatFormatting.GRAY + setting.getValue().toString().toUpperCase(), x + 2.3f, y - 1.7f - (float) MioClickGui.INSTANCE.getTextOffset(), getState() ? -1 : -5592406);
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
    }

    @Override
    public void onKeyTyped(char typedChar, int keyCode) {
        if (isListening) {
            Bind bind = new Bind(keyCode);
            if (bind.toString().equalsIgnoreCase("Escape")) {
                return;
            }
            if (bind.toString().equalsIgnoreCase("Delete")) {
                bind = new Bind(-1);
            }
            setting.setValue(bind);
            onMouseClick();
        }
    }

    @Override
    public int getHeight() {
        return ClickGui.INSTANCE.getButtonHeight() - 1;
    }

    @Override
    public void toggle() {
        isListening = !isListening;
    }

    @Override
    public boolean getState() {
        return !isListening;
    }
}

