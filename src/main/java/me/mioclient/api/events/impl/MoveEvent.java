package me.mioclient.api.events.impl;

import me.mioclient.api.events.Event;
import net.minecraftforge.fml.common.eventhandler.Cancelable;

@Cancelable
public class MoveEvent extends Event {

    public double motionX, motionY, motionZ;

    public MoveEvent(double motionX, double motionY, double motionZ) {
        this.motionX = motionX;
        this.motionY = motionY;
        this.motionZ = motionZ;
    }
}