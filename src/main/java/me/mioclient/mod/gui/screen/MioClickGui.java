package me.mioclient.mod.gui.screen;

import me.mioclient.api.managers.Managers;
import me.mioclient.api.util.render.ColorUtil;
import me.mioclient.api.util.render.RenderUtil;
import me.mioclient.mod.Mod;
import me.mioclient.mod.gui.click.Component;
import me.mioclient.mod.gui.click.items.Item;
import me.mioclient.mod.gui.click.items.buttons.ModuleButton;
import me.mioclient.mod.gui.click.items.other.Particle;
import me.mioclient.mod.gui.click.items.other.Snow;
import me.mioclient.mod.modules.Category;
import me.mioclient.mod.modules.Module;
import me.mioclient.mod.modules.impl.client.ClickGui;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.ScaledResolution;
import org.lwjgl.input.Mouse;

import java.awt.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Random;

public class MioClickGui extends GuiScreen {

    public static MioClickGui INSTANCE;

    private final ArrayList<Snow> snow = new ArrayList<>();
    private final Particle.Util particles = new Particle.Util(300);

    Minecraft mc = Minecraft.getMinecraft();

    private final ArrayList<Component> components = new ArrayList<>();

    public MioClickGui() {
        onLoad();
    }

    private void onLoad() {

        INSTANCE = this;

        int x = -84;

        for (Category category : Managers.MODULES.getCategories()) {

            if (category != Category.HUD) {

                components.add(new Component(category.getName(), x += 90, 4, true) {

                    @Override
                    public void setupItems() {
                        counter1 = new int[]{1};
                        Managers.MODULES.getModulesByCategory(category).forEach(module -> addButton(new ModuleButton(module)));
                    }
                });
            }
        }

        components.forEach(components -> components.getItems().sort(Comparator.comparing(Mod::getName)));

        Random random = new Random();

        for (int i = 0; i < 100; ++i) {

            for (int y = 0; y < 3; ++y) {
                Snow snow = new Snow(25 * i, y * -50, random.nextInt(3) + 1, random.nextInt(2)+1);
                this.snow.add(snow);
            }
        }
    }

    public void updateModule(Module module) {
        for (Component component : components) {

            for (Item item : component.getItems()) {

                if (!(item instanceof ModuleButton)) continue;
                ModuleButton button = (ModuleButton) item;
                Module mod = button.getModule();
                if (module == null || !module.equals(mod)) continue;
                button.initSettings();
            }
        }
    }

    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        checkMouseWheel();

        if (mc.world != null) {
            drawDefaultBackground();
        } else {
            Gui.drawRect(0, 0, 1920, 1080, ColorUtil.injectAlpha(new Color(-1072689136), 150).getRGB());
        }

        if (ClickGui.INSTANCE.background.getValue() && mc.currentScreen instanceof MioClickGui && mc.world != null) {
            RenderUtil.drawVGradientRect(0, 0, (float) Managers.TEXT.scaledWidth, (float) Managers.TEXT.scaledHeight, new Color(0, 0, 0, 0).getRGB(), Managers.COLORS.getCurrentWithAlpha(60));
        }

        if (ClickGui.INSTANCE.particles.getValue()) {
            particles.drawParticles();
        }

        components.forEach(components ->  components.drawScreen(mouseX, mouseY, partialTicks));

        ScaledResolution res = new ScaledResolution(mc);

        int month = Calendar.getInstance().get(Calendar.MONTH) + 1;

        if (!snow.isEmpty() && (month == 12 || month == 1 || month == 2)) {
            snow.forEach(snow -> snow.drawSnow(res));
        }
    }

    public void mouseClicked(int mouseX, int mouseY, int clickedButton) {
        components.forEach(components -> components.mouseClicked(mouseX, mouseY, clickedButton));
    }

    public void mouseReleased(int mouseX, int mouseY, int releaseButton) {
        components.forEach(components -> components.mouseReleased(mouseX, mouseY, releaseButton));
    }

    public boolean doesGuiPauseGame() {
        return false;
    }

    public final ArrayList<Component> getComponents() {
        return components;
    }

    public void checkMouseWheel() {
        int dWheel = Mouse.getDWheel();

        if (dWheel < 0) {
            components.forEach(component -> component.setY(component.getY() - 10));

        } else if (dWheel > 0) {
            components.forEach(component -> component.setY(component.getY() + 10));
        }
    }

    public int getTextOffset() {
        return -6;
    }

    public void keyTyped(char typedChar, int keyCode) throws IOException {
        super.keyTyped(typedChar, keyCode);
        components.forEach(component -> component.onKeyTyped(typedChar, keyCode));
    }

    @Override
    public void onGuiClosed() {
        super.onGuiClosed();

        if (mc.entityRenderer.isShaderActive()) {
            mc.entityRenderer.getShaderGroup().deleteShaderGroup();
        }
    }
}

