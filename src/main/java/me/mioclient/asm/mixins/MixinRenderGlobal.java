package me.mioclient.asm.mixins;

import me.mioclient.api.events.impl.DamageBlockEvent;
import me.mioclient.asm.accessors.IRenderGlobal;
import net.minecraft.client.renderer.DestroyBlockProgress;
import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.MinecraftForge;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import javax.annotation.Nonnull;
import java.util.Map;

@Mixin(value = RenderGlobal.class)
public abstract class MixinRenderGlobal implements IRenderGlobal {

    @Nonnull
    @Override
    @Accessor(value = "damagedBlocks")
    public abstract Map<Integer, DestroyBlockProgress> getDamagedBlocks();

    @Inject(method = "sendBlockBreakProgress", at = @At("HEAD"))
    public void onSendingBlockBreakProgressPre(int breakerId, BlockPos pos, int progress, CallbackInfo ci) {

        DamageBlockEvent event = new DamageBlockEvent(pos, progress, breakerId);
        MinecraftForge.EVENT_BUS.post(event);
    }
}