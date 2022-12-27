package me.mioclient.asm.mixins;

import com.mojang.authlib.GameProfile;
import me.mioclient.api.events.impl.JumpEvent;
import me.mioclient.api.managers.Managers;
import me.mioclient.mod.modules.impl.player.TpsSync;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value={EntityPlayer.class})
public abstract class MixinEntityPlayer extends EntityLivingBase {

    public MixinEntityPlayer(World worldIn, GameProfile gameProfileIn) {
        super(worldIn);
    }

    @Inject(method={"getCooldownPeriod"}, at={@At(value="HEAD")}, cancellable=true)
    private void getCooldownPeriodHook(CallbackInfoReturnable<Float> callbackInfoReturnable) {

        if (TpsSync.INSTANCE == null) {
            TpsSync.INSTANCE = new TpsSync();
        }

        TpsSync tpsSync = TpsSync.INSTANCE;

        if (tpsSync.isOn() && tpsSync.attack.getValue()) {
            callbackInfoReturnable.setReturnValue((float) (1.0 / (EntityPlayer.class.cast(this)).getEntityAttribute(SharedMonsterAttributes.ATTACK_SPEED).getBaseValue() * 20.0 * (double) Managers.SERVER.getTpsFactor()));
        }
    }

    @Inject(method = "jump", at = @At("HEAD"))
    public void onJump(CallbackInfo ci){
        if (Minecraft.getMinecraft().player.getName().equals(getName())){
            MinecraftForge.EVENT_BUS.post(new JumpEvent(motionX, motionY));
        }
    }
}
