package me.mioclient.mod.modules.impl.misc;

import me.mioclient.mod.modules.Category;
import me.mioclient.mod.modules.Module;
import me.mioclient.mod.modules.settings.Setting;

public class UnfocusedCPU extends Module {

    public static UnfocusedCPU INSTANCE;

    public Setting<Integer> unfocusedFps =
            add(new Setting<>("UnfocusedFPS", 5, 1, 30));

    public UnfocusedCPU() {
        super("UnfocusedCPU", "Decreases your framerate when minecraft is unfocused.", Category.MISC);
        INSTANCE = this;
    }
}
