package me.mioclient.api.events.impl;

import me.mioclient.api.events.Event;
import net.minecraft.entity.player.EntityPlayer;

import java.util.UUID;

public class ConnectionEvent extends Event {

    private final UUID uuid;
    private final EntityPlayer player;
    private final String name;

    public ConnectionEvent(int stage, UUID uuid, String name) {
        super(stage);
        this.uuid = uuid;
        this.name = name;
        player = null;
    }

    public ConnectionEvent(int stage, EntityPlayer player, UUID uuid, String name) {
        super(stage);
        this.player = player;
        this.uuid = uuid;
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public UUID getUuid() {
        return uuid;
    }

    public EntityPlayer getPlayer() {
        return player;
    }
}

