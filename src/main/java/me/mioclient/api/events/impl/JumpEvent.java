package me.mioclient.api.events.impl;

import me.mioclient.api.events.Event;

public class JumpEvent extends Event {

    public double motionX;
    public double motionY;

    public JumpEvent(double motionX, double motionY) {
        super();
        this.motionX = motionX;
        this.motionY = motionY;
    }
}