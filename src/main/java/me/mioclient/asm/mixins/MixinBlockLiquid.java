package me.mioclient.asm.mixins;

import me.mioclient.mod.modules.impl.exploit.LiquidInteract;
import me.mioclient.mod.modules.impl.movement.Velocity;
import net.minecraft.block.Block;
import net.minecraft.block.BlockLiquid;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = BlockLiquid.class)
public class MixinBlockLiquid extends Block {

    protected MixinBlockLiquid(Material materialIn) {
        super(materialIn);
    }

    @Inject(method = "modifyAcceleration", at = @At("HEAD"), cancellable = true)
    public void modifyAccelerationHook(World worldIn, BlockPos pos, Entity entityIn, Vec3d motion, CallbackInfoReturnable<Vec3d> info) {

        if (Velocity.INSTANCE.isOn()) {
            info.setReturnValue(motion);
        }
    }

    @Inject(method = "canCollideCheck", at = @At("HEAD"), cancellable = true)
    public void canCollideCheckHook(IBlockState blockState, boolean hitIfLiquid, CallbackInfoReturnable<Boolean> info) {
        info.setReturnValue(hitIfLiquid && (Integer) blockState.getValue((IProperty) BlockLiquid.LEVEL) == 0 || LiquidInteract.INSTANCE.isOn());
    }
}

