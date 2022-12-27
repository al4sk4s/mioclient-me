package me.mioclient.asm.mixins;

import me.mioclient.api.events.impl.StepEvent;
import me.mioclient.api.events.impl.TurnEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.MoverType;
import net.minecraftforge.common.MinecraftForge;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static me.mioclient.api.util.Wrapper.mc;

@Mixin(value = {Entity.class}, priority = Integer.MAX_VALUE)
public abstract class MixinEntity {
    @Shadow
    private int entityId;

    @Shadow
    protected boolean isInWeb;

    @Shadow
    public void move(MoverType type, double x, double y, double z) {}

    @Shadow
    public double motionX;

    @Shadow
    public double motionY;

    @Shadow
    public double motionZ;

    @Shadow
    public abstract boolean equals(Object paramObject);

    @Shadow public abstract int getEntityId();

    @Inject(method = "turn", at = @At("HEAD"), cancellable = true)
    public void onTurnHook(float yaw, float pitch, CallbackInfo info) {
        TurnEvent event = new TurnEvent(yaw, pitch);

        MinecraftForge.EVENT_BUS.post(event);

        if (event.isCanceled()) {
            info.cancel();
        }
    }

    @Inject(method = "move", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/Entity;getEntityBoundingBox()Lnet/minecraft/util/math/AxisAlignedBB;", ordinal = 12, shift = At.Shift.BEFORE))
    public void onStepPre(MoverType type, double x, double y, double z, CallbackInfo info) {
        if (Entity.class.cast(this).equals(mc.player)) {
            StepEvent event = new StepEvent(0);

            MinecraftForge.EVENT_BUS.post(event);
        }
    }

    @Inject(method = "move", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/Entity;setEntityBoundingBox(Lnet/minecraft/util/math/AxisAlignedBB;)V", ordinal = 7, shift = At.Shift.AFTER))
    public void onStepPost(MoverType type, double x, double y, double z, CallbackInfo info) {
        if (Entity.class.cast(this).equals(mc.player)) {
            StepEvent event = new StepEvent(1);

            MinecraftForge.EVENT_BUS.post(event);
        }
    }
}