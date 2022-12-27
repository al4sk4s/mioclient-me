package me.mioclient.api.events.impl;

import me.mioclient.api.events.Event;

public class UpdateWalkingPlayerEvent extends Event {

    public UpdateWalkingPlayerEvent(int stage) {
        super(stage);
    }
}

