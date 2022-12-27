package me.mioclient.api.events.impl;

import me.mioclient.api.events.Event;
import net.minecraftforge.fml.common.eventhandler.Cancelable;

import java.awt.*;

@Cancelable
public class RenderFogColorEvent extends Event {

    private Color color;

    public Color getColor() {
        return color;
    }

    public void setColor(Color color) {
        this.color = color;
    }
}