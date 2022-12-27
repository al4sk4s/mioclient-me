package me.mioclient.api.events.impl;

import me.mioclient.api.events.Event;
import net.minecraft.entity.player.EntityPlayer;

public class DeathEvent extends Event {

    public EntityPlayer player;

    public DeathEvent(EntityPlayer player) {
        this.player = player;
    }
}

