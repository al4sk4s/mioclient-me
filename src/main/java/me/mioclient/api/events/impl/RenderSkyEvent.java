package me.mioclient.api.events.impl;

import me.mioclient.api.events.Event;
import net.minecraftforge.fml.common.eventhandler.Cancelable;

import java.awt.*;

@Cancelable
public class RenderSkyEvent extends Event {

    private Color color;

    public void setColor(Color color) {
        this.color = color;
    }

    public Color getColor() {
        return color;
    }
}