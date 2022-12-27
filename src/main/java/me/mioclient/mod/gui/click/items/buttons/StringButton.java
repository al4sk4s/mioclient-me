package me.mioclient.mod.gui.click.items.buttons;

import com.mojang.realmsclient.gui.ChatFormatting;
import me.mioclient.api.managers.Managers;
import me.mioclient.api.util.render.RenderUtil;
import me.mioclient.mod.gui.screen.MioClickGui;
import me.mioclient.mod.modules.impl.client.ClickGui;
import me.mioclient.mod.modules.settings.Setting;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.init.SoundEvents;
import net.minecraft.util.ChatAllowedCharacters;

public class StringButton
        extends Button {
    private final Setting setting;
    public boolean isListening;
    private CurrentString currentString = new CurrentString("");

    public StringButton(Setting setting) {
        super(setting.getName());
        this.setting = setting;
        width = 15;
    }

    public static String removeLastChar(String str) {
        String output = "";
        if (str != null && str.length() > 0) {
            output = str.substring(0, str.length() - 1);
        }
        return output;
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        RenderUtil.drawRect(x, y, x + (float) width + 7.4f, y + (float) height - 0.5f, getState() ? (!isHovering(mouseX, mouseY) ? Managers.COLORS.getCurrentWithAlpha(120) : Managers.COLORS.getCurrentWithAlpha(200)) : (!isHovering(mouseX, mouseY) ? 0x11555555 : -2007673515));
        if (isListening) {
            Managers.TEXT.drawStringWithShadow(currentString.getString() + Managers.TEXT.getIdleSign(), x + 2.3f, y - 1.7f - (float) MioClickGui.INSTANCE.getTextOffset(), getState() ? -1 : -5592406);
        } else {
            Managers.TEXT.drawStringWithShadow((setting.getName().equals("Buttons") ? "Buttons " : (setting.getName().equals("Prefix") ? "Prefix  " + ChatFormatting.GRAY : "")) + setting.getValue(), x + 2.3f, y - 1.7f - (float) MioClickGui.INSTANCE.getTextOffset(), getState() ? -1 : -5592406);
        }
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
            switch (keyCode) {
                case 1: {
                    return;
                }
                case 28: {
                    enterString();
                }
                case 14: {
                    setString(removeLastChar(currentString.getString()));
                }
            }
            if (ChatAllowedCharacters.isAllowedCharacter(typedChar)) {
                setString(currentString.getString() + typedChar);
            }
        }
    }

    @Override
    public void update() {
        setHidden(!setting.isVisible());
    }

    private void enterString() {
        if (currentString.getString().isEmpty()) {
            setting.setValue(setting.getDefaultValue());
        } else {
            setting.setValue(currentString.getString());
        }
        setString("");
        onMouseClick();
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

    public void setString(String newString) {
        currentString = new CurrentString(newString);
    }

    public static class CurrentString {
        private final String string;

        public CurrentString(String string) {
            this.string = string;
        }

        public String getString() {
            return string;
        }
    }
}

