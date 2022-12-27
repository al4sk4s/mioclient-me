package me.mioclient.api.managers.impl;

import me.mioclient.mod.Mod;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.MathHelper;

import java.util.HashMap;

public class SpeedManager extends Mod {

    public static Minecraft mc = Minecraft.getMinecraft();
    public static final double LAST_JUMP_INFO_DURATION_DEFAULT = 3.0;
    public static boolean didJumpThisTick;
    public static boolean isJumping;
    private final int distancer = 20;
    public double firstJumpSpeed;
    public double lastJumpSpeed;
    public double percentJumpSpeedChanged;
    public double jumpSpeedChanged;
    public boolean didJumpLastTick;
    public long jumpInfoStartTime;
    public boolean wasFirstJump = true;
    public double speedometerCurrentSpeed;
    public HashMap<EntityPlayer, Double> playerSpeeds = new HashMap();

    public static void setDidJumpThisTick(boolean val) {
        didJumpThisTick = val;
    }

    public static void setIsJumping(boolean val) {
        isJumping = val;
    }

    public float lastJumpInfoTimeRemaining() {
        return (float) (Minecraft.getSystemTime() - jumpInfoStartTime) / 1000.0f;
    }

    public void updateValues() {
        double distTraveledLastTickX = mc.player.posX - mc.player.prevPosX;
        double distTraveledLastTickZ = mc.player.posZ - mc.player.prevPosZ;
        speedometerCurrentSpeed = distTraveledLastTickX * distTraveledLastTickX + distTraveledLastTickZ * distTraveledLastTickZ;
        if (didJumpThisTick && (!mc.player.onGround || isJumping)) {
            if (didJumpThisTick && !didJumpLastTick) {
                wasFirstJump = lastJumpSpeed == 0.0;
                percentJumpSpeedChanged = speedometerCurrentSpeed != 0.0 ? speedometerCurrentSpeed / lastJumpSpeed - 1.0 : -1.0;
                jumpSpeedChanged = speedometerCurrentSpeed - lastJumpSpeed;
                jumpInfoStartTime = Minecraft.getSystemTime();
                lastJumpSpeed = speedometerCurrentSpeed;
                firstJumpSpeed = wasFirstJump ? lastJumpSpeed : 0.0;
            }
            didJumpLastTick = didJumpThisTick;
        } else {
            didJumpLastTick = false;
            lastJumpSpeed = 0.0;
        }
        updatePlayers();
    }

    public void updatePlayers() {
        for (EntityPlayer player : mc.world.playerEntities) {
            if (!(mc.player.getDistanceSq(player) < (double) (distancer * distancer)))
                continue;
            double distTraveledLastTickX = player.posX - player.prevPosX;
            double distTraveledLastTickZ = player.posZ - player.prevPosZ;
            double playerSpeed = distTraveledLastTickX * distTraveledLastTickX + distTraveledLastTickZ * distTraveledLastTickZ;
            playerSpeeds.put(player, playerSpeed);
        }
    }

    public double getPlayerSpeed(EntityPlayer player) {
        if (playerSpeeds.get(player) == null) {
            return 0.0;
        }
        return turnIntoKpH(playerSpeeds.get(player));
    }

    public double turnIntoKpH(double input) {
        return (double) MathHelper.sqrt(input) * 71.2729367892;
    }

    public double getSpeedKpH() {
        double speedometerkphdouble = turnIntoKpH(speedometerCurrentSpeed);
        speedometerkphdouble = (double) Math.round(10.0 * speedometerkphdouble) / 10.0;
        return speedometerkphdouble;
    }

    public double getSpeedMpS() {
        double speedometerMpsdouble = turnIntoKpH(speedometerCurrentSpeed) / 3.6;
        speedometerMpsdouble = (double) Math.round(10.0 * speedometerMpsdouble) / 10.0;
        return speedometerMpsdouble;
    }
}

