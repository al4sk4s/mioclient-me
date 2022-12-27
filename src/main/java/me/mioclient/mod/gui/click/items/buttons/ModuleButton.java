package me.mioclient.mod.gui.click.items.buttons;

import com.mojang.realmsclient.gui.ChatFormatting;
import me.mioclient.api.managers.Managers;
import me.mioclient.api.util.render.ColorUtil;
import me.mioclient.api.util.render.RenderUtil;
import me.mioclient.mod.gui.click.Component;
import me.mioclient.mod.gui.click.items.Item;
import me.mioclient.mod.gui.screen.MioClickGui;
import me.mioclient.mod.modules.Module;
import me.mioclient.mod.modules.impl.client.ClickGui;
import me.mioclient.mod.modules.impl.client.FontMod;
import me.mioclient.mod.modules.settings.Bind;
import me.mioclient.mod.modules.settings.Setting;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.init.SoundEvents;
import net.minecraft.util.ResourceLocation;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

import static me.mioclient.mod.gui.click.Component.calculateRotation;

public class ModuleButton extends Button {

    private final Module module;
    private List<Item> items = new ArrayList<Item>();
    private boolean subOpen;
    private int progress;

    public ModuleButton(Module module) {
        super(module.getName());
        progress = 0;
        this.module = module;
        initSettings();
    }

    public void initSettings() {
        ArrayList<Item> newItems = new ArrayList<Item>();
        if (!module.getSettings().isEmpty()) {
            for (Setting setting : module.getSettings()) {
                if (setting.getValue() instanceof Boolean && !setting.getName().equals("Enabled")) {
                    newItems.add(new BooleanButton(setting));
                }
                if (setting.getValue() instanceof Bind && !setting.getName().equalsIgnoreCase("Keybind") && !module.getName().equalsIgnoreCase("Hud")) {
                    newItems.add(new BindButton(setting));
                }
                if ((setting.getValue() instanceof String || setting.getValue() instanceof Character) && !setting.getName().equalsIgnoreCase("displayName")) {
                    newItems.add(new StringButton(setting));
                }
                if (setting.getValue() instanceof Color) {
                    newItems.add(new PickerButton(setting));
                }
                if (setting.isNumberSetting() && setting.hasRestriction()) {
                    newItems.add(new Slider(setting));
                    continue;
                }
                if (!setting.isEnumSetting()) continue;
                newItems.add(new EnumButton(setting));
            }
        }
        newItems.add(new BindButton(module.getSettingByName("Keybind")));
        items = newItems;
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        super.drawScreen(mouseX, mouseY, partialTicks);
        if (!items.isEmpty()) {

            drawGear();

            if (subOpen) {

                ++progress;

                float height = 1.0f;
                for (Item item : items) {
                    Component.counter1[0] = Component.counter1[0] + 1;
                    if (!item.isHidden()) {
                        item.setLocation(x + 1.0f, y + (height += ClickGui.INSTANCE.getButtonHeight()));
                        item.setHeight(ClickGui.INSTANCE.getButtonHeight());
                        item.setWidth(width - 9);
                        item.drawScreen(mouseX, mouseY, partialTicks);

                        if (item instanceof PickerButton && ((PickerButton)item).setting.open) {
                            height += 110.0f;
                        }

                        if (item instanceof EnumButton && ((EnumButton)item).setting.open) {
                            height += ((EnumButton) item).setting.getValue().getClass().getEnumConstants().length * 12;
                        }
                    }
                    item.update();
                }
            }
        }

        if (isHovering(mouseX, mouseY) && ClickGui.INSTANCE.isOn()) {

            String description = ChatFormatting.GRAY + module.getDescription();

            Gui.drawRect(0, mc.currentScreen.height - 11, Managers.TEXT.getStringWidth(description) + 2, mc.currentScreen.height, ColorUtil.injectAlpha(new Color(-1072689136), 200).getRGB());

            assert mc.currentScreen != null;
            Managers.TEXT.drawStringWithShadow(description, 2, mc.currentScreen.height - 10, -1);
        }
    }

    public void drawGear() {
        boolean newStyle = ClickGui.INSTANCE.style.getValue() == ClickGui.Style.NEW;
        boolean future = ClickGui.INSTANCE.style.getValue() == ClickGui.Style.FUTURE;

        if (ClickGui.INSTANCE.gear.getValue()) {

            if (future) {
                GlStateManager.pushMatrix();
                GlStateManager.enableBlend();
                mc.getTextureManager().bindTexture(new ResourceLocation("textures/mio/gear.png"));
                GlStateManager.translate(getX() + getWidth() - 6.7F, getY() + 7.7F - 0.3F, 0.0F);
                GlStateManager.rotate(calculateRotation((float) progress), 0.0F, 0.0F, 1.0F);
                RenderUtil.drawModalRect(-5, -5, 0.0F, 0.0F, 10, 10, 10, 10, 10.0F, 10.0F);
                GlStateManager.disableBlend();
                GlStateManager.popMatrix();
            } else {

                String color = (module.isOn() || newStyle) ? "" : "" + ChatFormatting.GRAY;
                String gear = subOpen ? "-" : "+";
                float x = this.x - 1.5f + (float) width - 7.4f;

                Managers.TEXT.drawStringWithShadow(color + gear,
                        x + ((FontMod.INSTANCE.isOn() && gear.equals("-")) ? 1.0f : 0.0f),
                        y - 2.2f - (float) MioClickGui.INSTANCE.getTextOffset()
                        , -1);
            }
        }
    }

    @Override
    public void mouseClicked(int mouseX, int mouseY, int mouseButton) {
        super.mouseClicked(mouseX, mouseY, mouseButton);
        if (!items.isEmpty()) {
            if (mouseButton == 1 && isHovering(mouseX, mouseY)) {
                subOpen = !subOpen;
                mc.getSoundHandler().playSound(PositionedSoundRecord.getMasterRecord(SoundEvents.UI_BUTTON_CLICK, 1.0f));
            }
            if (subOpen) {
                for (Item item : items) {
                    if (item.isHidden()) continue;
                    item.mouseClicked(mouseX, mouseY, mouseButton);
                }
            }
        }
    }

    @Override
    public void onKeyTyped(char typedChar, int keyCode) {
        super.onKeyTyped(typedChar, keyCode);
        if (!items.isEmpty() && subOpen) {
            for (Item item : items) {
                if (item.isHidden()) continue;
                item.onKeyTyped(typedChar, keyCode);
            }
        }
    }

    @Override
    public int getHeight() {
        if (subOpen) {
            int height = ClickGui.INSTANCE.getButtonHeight() - 1;
            for (Item item : items) {
                if (item.isHidden()) continue;
                height += item.getHeight() + 1;

                if (item instanceof PickerButton && ((PickerButton)item).setting.open) {
                    height += 110;
                }
                if (item instanceof EnumButton && ((EnumButton)item).setting.open) {
                    height += ((EnumButton) item).setting.getValue().getClass().getEnumConstants().length * 12;
                }
            }
            return height + 2;
        }
        return ClickGui.INSTANCE.getButtonHeight() - 1;
    }

    public Module getModule() {
        return module;
    }

    @Override
    public void toggle() {
        module.toggle();
    }

    @Override
    public boolean getState() {
        return module.isOn();
    }
}

