package me.mioclient.mod.gui.click.items.buttons;

import com.mojang.realmsclient.gui.ChatFormatting;
import me.mioclient.api.managers.Managers;
import me.mioclient.api.util.render.RenderUtil;
import me.mioclient.mod.gui.screen.MioClickGui;
import me.mioclient.mod.modules.impl.client.ClickGui;
import me.mioclient.mod.modules.settings.Setting;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.init.SoundEvents;
import net.minecraft.util.ResourceLocation;

import static me.mioclient.mod.gui.click.Component.calculateRotation;

public class BooleanButton extends Button {

    private final Setting setting;
    private int progress;

    public BooleanButton(Setting setting) {
        super(setting.getName());
        progress = 0;
        this.setting = setting;
        width = 15;
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        boolean newStyle = ClickGui.INSTANCE.style.getValue() == ClickGui.Style.NEW;
        boolean future = ClickGui.INSTANCE.style.getValue() == ClickGui.Style.FUTURE;
        boolean dotgod = ClickGui.INSTANCE.style.getValue() == ClickGui.Style.DOTGOD;

        if (future) {
            RenderUtil.drawRect(x, y, x + (float) width + 7.4f, y + (float) height - 0.5f, getState() ? (!isHovering(mouseX, mouseY) ? Managers.COLORS.getCurrentWithAlpha(99) : Managers.COLORS.getCurrentWithAlpha(120)) : (!isHovering(mouseX, mouseY) ? Managers.COLORS.getCurrentWithAlpha(26) : Managers.COLORS.getCurrentWithAlpha(55)));
            Managers.TEXT.drawStringWithShadow((newStyle ? getName().toLowerCase() : getName()), x + 2.3f, y - 1.7f - (float) MioClickGui.INSTANCE.getTextOffset(), getState() ? -1 : -5592406);

        } else if (dotgod) {
            RenderUtil.drawRect(x, y, x + (float) width + 7.4f, y + (float) height - 0.5f, getState() ? (!isHovering(mouseX, mouseY) ? Managers.COLORS.getCurrentWithAlpha(65) : Managers.COLORS.getCurrentWithAlpha(90)) : (!isHovering(mouseX, mouseY) ? Managers.COLORS.getCurrentWithAlpha(26) : Managers.COLORS.getCurrentWithAlpha(55)));
            Managers.TEXT.drawStringWithShadow((getName().toLowerCase()), x + 2.3f, y - 1.7f - (float) MioClickGui.INSTANCE.getTextOffset(), getState() ? Managers.COLORS.getCurrentGui(240) : 0xB0B0B0);

        } else {
            RenderUtil.drawRect(x, y, x + (float) width + 7.4f, y + (float) height - 0.5f, getState() ? (!isHovering(mouseX, mouseY) ? Managers.COLORS.getCurrentWithAlpha(120) : Managers.COLORS.getCurrentWithAlpha(200)) : (!isHovering(mouseX, mouseY) ? 0x11555555 : -2007673515));
            Managers.TEXT.drawStringWithShadow((newStyle ? getName().toLowerCase() : getName()), x + 2.3f, y - 1.7f - (float) MioClickGui.INSTANCE.getTextOffset(), getState() ? -1 : -5592406);
        }

        if (setting.parent) {

            if (setting.open) {
                ++progress;
            }

            if (future) {
                GlStateManager.pushMatrix();
                GlStateManager.enableBlend();
                mc.getTextureManager().bindTexture(new ResourceLocation("textures/mio/gear.png"));
                GlStateManager.translate(getX() + getWidth() - 6.7F + 8.0f, getY() + 7.7F - 0.3F, 0.0F);
                GlStateManager.rotate(calculateRotation((float) progress), 0.0F, 0.0F, 1.0F);
                RenderUtil.drawModalRect(-5, -5, 0.0F, 0.0F, 10, 10, 10, 10, 10.0F, 10.0F);
                GlStateManager.disableBlend();
                GlStateManager.popMatrix();
            } else {
                String color = (getState() || newStyle) ? "" : "" + ChatFormatting.GRAY;
                String gear = setting.open ? "-" : "+";

                Managers.TEXT.drawStringWithShadow(color + gear,
                        x - 1.5f + (float) width - 7.4f + 8.0f,
                        y - 2.2f - (float) MioClickGui.INSTANCE.getTextOffset(), -1);
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
    }

    @Override
    public int getHeight() {
        return ClickGui.INSTANCE.getButtonHeight() - 1;
    }

    @Override
    public void toggle() {
        setting.setValue(!((Boolean) setting.getValue()));
    }

    @Override
    public boolean getState() {
        return (Boolean) setting.getValue();
    }
}

