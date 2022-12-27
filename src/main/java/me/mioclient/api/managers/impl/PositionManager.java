package me.mioclient.api.managers.impl;

import me.mioclient.mod.Mod;
import net.minecraft.network.play.client.CPacketPlayer;

public class PositionManager extends Mod {

    private double x;
    private double y;
    private double z;
    private boolean onGround;

    public void updatePosition() {
        x = mc.player.posX;
        y = mc.player.posY;
        z = mc.player.posZ;
        onGround = mc.player.onGround;
    }

    public void restorePosition() {
        mc.player.posX = x;
        mc.player.posY = y;
        mc.player.posZ = z;
        mc.player.onGround = onGround;
    }

    public void setPlayerPosition(double x, double y, double z) {
        mc.player.posX = x;
        mc.player.posY = y;
        mc.player.posZ = z;
    }

    public void setPlayerPosition(double x, double y, double z, boolean onGround) {
        mc.player.posX = x;
        mc.player.posY = y;
        mc.player.posZ = z;
        mc.player.onGround = onGround;
    }

    public void setPositionPacket(double x, double y, double z, boolean onGround, boolean setPos, boolean noLagBack) {
        mc.player.connection.sendPacket(new CPacketPlayer.Position(x, y, z, onGround));

        if (setPos) {
            mc.player.setPosition(x, y, z);
            if (noLagBack) {
                updatePosition();
            }
        }
    }
    
    public double getX() {
        return x;
    }

    public void setX(double x) {
        this.x = x;
    }

    public double getY() {
        return y;
    }

    public void setY(double y) {
        this.y = y;
    }

    public double getZ() {
        return z;
    }

    public void setZ(double z) {
        this.z = z;
    }
}