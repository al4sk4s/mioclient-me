package me.mioclient.mod.modules.impl.player;

import me.mioclient.mod.modules.Category;
import me.mioclient.mod.modules.Module;
import me.mioclient.mod.modules.settings.Setting;

public class TpsSync extends Module {

    public static TpsSync INSTANCE;

    public Setting<Boolean> attack =
            add(new Setting<>("Attack", false));
    public Setting<Boolean> mining =
            add(new Setting<>("Mine", true));

    public TpsSync() {
        super("TpsSync", "Syncs your client with the TPS.", Category.PLAYER);
        INSTANCE = this;
    }
}

