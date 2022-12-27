package me.mioclient.api.util.entity;

import me.mioclient.api.util.render.entity.StaticModelPlayer;
import net.minecraft.entity.player.EntityPlayer;

public class CopyOfPlayer {

    private final EntityPlayer player;
    private final StaticModelPlayer model;

    private final long time;
    private final double x;
    private final double y;
    private final double z;

    public CopyOfPlayer(EntityPlayer player, long time, double x, double y, double z, boolean slim) {
        this.player = player;
        this.time = time;
        this.x = x;
        this.y = y - (player.isSneaking() ? 0.125 : 0);
        this.z = z;
        model = new StaticModelPlayer(player, slim, 0);
        model.disableArmorLayers();
    }

    public EntityPlayer getPlayer() {
        return player;
    }

    public long getTime() {
        return time;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public double getZ() {
        return z;
    }

    public StaticModelPlayer getModel() {
        return model;
    }
}
