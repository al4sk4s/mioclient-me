package me.mioclient.mod;

import me.mioclient.api.util.Wrapper;
import me.mioclient.mod.gui.screen.MioClickGui;
import me.mioclient.mod.modules.Module;
import me.mioclient.mod.modules.settings.Setting;

import java.util.ArrayList;
import java.util.List;

public class Mod implements Wrapper {

    public List<Setting> settings = new ArrayList<>();
    private String name;

    public Mod(String name) {
        this.name = name;
    }

    public Mod() {

    }

    //Settings

    public Setting add(Setting setting) {
        setting.setMod(this);
        settings.add(setting);

        if (this instanceof Module && mc.currentScreen instanceof MioClickGui) {
            MioClickGui.INSTANCE.updateModule((Module) this);
        }

        return setting;
    }

    public Setting getSettingByName(String name) {

        for (Setting setting : settings) {
            if (!setting.getName().equalsIgnoreCase(name)) continue;

            return setting;
        }

        return null;
    }

    public void resetSettings() {
        settings = new ArrayList<>();
    }

    //Getters

    public String getName() {
        return name;
    }

    public List<Setting> getSettings() {
        return settings;
    }

    //Checks

    public static boolean nullCheck() {
        return mc.player == null;
    }

    public static boolean fullNullCheck() {
        return mc.player == null || mc.world == null;
    }

    public static boolean spawnCheck() {
        return (mc.player.ticksExisted > 15);
    }
}

