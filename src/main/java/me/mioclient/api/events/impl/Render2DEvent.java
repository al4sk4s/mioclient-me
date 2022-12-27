package me.mioclient.api.events.impl;

import me.mioclient.api.events.Event;
import net.minecraft.client.gui.ScaledResolution;

public class Render2DEvent extends Event {

    public float partialTicks;
    public ScaledResolution scaledResolution;

    public Render2DEvent(float partialTicks, ScaledResolution scaledResolution) {
        this.partialTicks = partialTicks;
        this.scaledResolution = scaledResolution;
    }

    public void setPartialTicks(float partialTicks) {
        this.partialTicks = partialTicks;
    }

    public void setScaledResolution(ScaledResolution scaledResolution) {
        this.scaledResolution = scaledResolution;
    }

    public double getScreenWidth() {
        return scaledResolution.getScaledWidth_double();
    }

    public double getScreenHeight() {
        return scaledResolution.getScaledHeight_double();
    }
}

