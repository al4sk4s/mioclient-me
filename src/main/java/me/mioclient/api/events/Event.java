package me.mioclient.api.events;

import me.mioclient.Mio;

public class Event extends net.minecraftforge.fml.common.eventhandler.Event {

    private int stage;

    public Event() {
    }

    public Event(int stage) {
        this.stage = stage;
    }

    public int getStage() {
        return stage;
    }

    public void setStage(int stage) {
        this.stage = stage;
    }

    public void cancel() {
        try {
            setCanceled(true);

        } catch (Exception e) {
            Mio.LOGGER.info(getClass().toString() + " Isn't cancellable!");
        }
    }
}

