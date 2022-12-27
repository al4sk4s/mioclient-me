package me.mioclient.api.events.impl;

import me.mioclient.api.events.Event;
import me.mioclient.mod.Mod;
import me.mioclient.mod.modules.settings.Setting;
import net.minecraftforge.fml.common.eventhandler.Cancelable;

@Cancelable
public class ClientEvent extends Event {

    private Mod mod;
    private Setting setting;

    public ClientEvent(int stage, Mod mod) {
        super(stage);
        this.mod = mod;
    }

    public ClientEvent(Setting setting) {
        super(2);
        this.setting = setting;
    }

    public Mod getMod() {
        return mod;
    }

    public Setting getSetting() {
        return setting;
    }
}

