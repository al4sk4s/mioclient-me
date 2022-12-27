package me.mioclient.asm.accessors;

import net.minecraft.network.play.client.CPacketPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value={CPacketPlayer.class})
public interface ICPacketPlayer {

    @Accessor(value = "onGround")void setOnGround(boolean onGround);

    @Accessor(value = "x")void setX(double x);

    @Accessor(value = "y")void setY(double y);

    @Accessor(value = "z")void setZ(double z);

    @Accessor(value = "yaw")void setYaw(float yaw);

    @Accessor(value = "pitch")void setPitch(float pitch);
}