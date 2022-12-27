package me.mioclient.mod.modules.impl.client;

import me.mioclient.mod.gui.screen.MioAppearance;
import me.mioclient.mod.modules.Category;
import me.mioclient.mod.modules.Module;

public class Appearance extends Module {

    public Appearance() {
        super("Appearance", "Drag HUD elements all over your screen.", Category.CLIENT);
    }

    @Override
    public void onEnable() {
        mc.displayGuiScreen(MioAppearance.getClickGui());
    }

    @Override
    public void onTick() {
        if (!(mc.currentScreen instanceof MioAppearance)) {
            disable();
        }
    }
}
