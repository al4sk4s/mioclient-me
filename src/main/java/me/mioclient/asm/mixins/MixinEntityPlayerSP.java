package me.mioclient.asm.mixins;

import me.mioclient.api.events.impl.MotionUpdateEvent;
import me.mioclient.api.events.impl.MoveEvent;
import me.mioclient.api.events.impl.UpdateWalkingPlayerEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.entity.MoverType;
import net.minecraft.stats.RecipeBook;
import net.minecraft.stats.StatisticsManager;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = {EntityPlayerSP.class}, priority = 9998)
public abstract class MixinEntityPlayerSP extends AbstractClientPlayer {

    public MixinEntityPlayerSP(Minecraft p_i47378_1_, World p_i47378_2_, NetHandlerPlayClient p_i47378_3_, StatisticsManager p_i47378_4_, RecipeBook p_i47378_5_) {
        super(p_i47378_2_, p_i47378_3_.getGameProfile());
    }

    @Redirect( method = "move", at = @At( value = "INVOKE", target = "Lnet/minecraft/client/entity/AbstractClientPlayer;move(Lnet/minecraft/entity/MoverType;DDD)V" ) )
    public void move(AbstractClientPlayer player, MoverType type, double x, double y, double z) {
        MoveEvent event = new MoveEvent(x, y, z);
        MinecraftForge.EVENT_BUS.post(event);
        super.move(type, event.motionX, event.motionY, event.motionZ);
    }

    @Inject( method = "onUpdate", at = @At( value = "INVOKE", target = "Lnet/minecraft/client/entity/EntityPlayerSP;onUpdateWalkingPlayer()V", shift = At.Shift.BEFORE ) )
    public void onPreMotionUpdate(CallbackInfo info) {
        MotionUpdateEvent event = new MotionUpdateEvent(Minecraft.getMinecraft().player.rotationYaw, Minecraft.getMinecraft().player.rotationPitch, Minecraft.getMinecraft().player.posX, Minecraft.getMinecraft().player.posY, Minecraft.getMinecraft().player.posZ, Minecraft.getMinecraft().player.onGround, Minecraft.getMinecraft().player.noClip, 0);
        MinecraftForge.EVENT_BUS.post(event);
    }

    @Inject( method = "onUpdate", at = @At( value = "INVOKE", target = "Lnet/minecraft/client/entity/EntityPlayerSP;onUpdateWalkingPlayer()V", shift = At.Shift.AFTER ) )
    public void onPostMotionUpdate(CallbackInfo info) {
        MotionUpdateEvent event = new MotionUpdateEvent(Minecraft.getMinecraft().player.rotationYaw, Minecraft.getMinecraft().player.rotationPitch,
                Minecraft.getMinecraft().player.posX, Minecraft.getMinecraft().player.posY, Minecraft.getMinecraft().player.posZ, Minecraft.getMinecraft().player.onGround, Minecraft.getMinecraft().player.noClip, 1);
        MinecraftForge.EVENT_BUS.post(event);
    }

    @Inject(method = {"onUpdateWalkingPlayer"}, at = {@At(value = "HEAD")})
    private void preMotion(CallbackInfo info) {
        UpdateWalkingPlayerEvent event = new UpdateWalkingPlayerEvent(0);
        MinecraftForge.EVENT_BUS.post(event);
    }

    @Inject(method = {"onUpdateWalkingPlayer"}, at = {@At(value = "RETURN")})
    private void postMotion(CallbackInfo info) {
        UpdateWalkingPlayerEvent event = new UpdateWalkingPlayerEvent(1);
        MinecraftForge.EVENT_BUS.post(event);
    }
    @Inject(method={"onUpdate"}, at={@At(value="HEAD")})
    public void update(CallbackInfo ci) {

        }

    @Inject(method={"onUpdate"}, at={@At(value="RETURN")})
    public void update2(CallbackInfo ci) {

        }
    }


