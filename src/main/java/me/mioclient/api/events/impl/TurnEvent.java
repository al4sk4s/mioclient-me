package me.mioclient.api.events.impl;

import me.mioclient.api.events.Event;
import net.minecraftforge.fml.common.eventhandler.Cancelable;

@Cancelable
public class TurnEvent extends Event {

    private final float yaw;
    private final float pitch;

    public TurnEvent(float yaw, float pitch) {
        this.yaw = yaw;
        this.pitch = pitch;
    }

    public float getYaw() {
        return yaw;
    }

    public float getPitch() {
        return pitch;
    }
}