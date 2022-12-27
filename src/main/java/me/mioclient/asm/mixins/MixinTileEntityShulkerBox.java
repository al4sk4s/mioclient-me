package me.mioclient.asm.mixins;

import me.mioclient.mod.modules.impl.movement.Velocity;
import net.minecraft.tileentity.TileEntityShulkerBox;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(TileEntityShulkerBox.class)
public abstract class MixinTileEntityShulkerBox {

    @Inject(method = "moveCollidedEntities", at = @At("HEAD"), cancellable = true)
    public void moveCollidedEntitiesHook(CallbackInfo info) {

        if (Velocity.INSTANCE.isOn()) info.cancel();
    }
}