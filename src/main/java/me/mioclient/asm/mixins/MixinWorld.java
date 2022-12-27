package me.mioclient.asm.mixins;

import com.google.common.base.Predicate;
import me.mioclient.api.events.impl.RenderSkyEvent;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.common.MinecraftForge;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(value = {World.class})
public class MixinWorld {

    @Redirect(method = {"getEntitiesWithinAABB(Ljava/lang/Class;Lnet/minecraft/util/math/AxisAlignedBB;Lcom/google/common/base/Predicate;)Ljava/util/List;"}, at = @At(value = "INVOKE", target = "Lnet/minecraft/world/chunk/Chunk;getEntitiesOfTypeWithinAABB(Ljava/lang/Class;Lnet/minecraft/util/math/AxisAlignedBB;Ljava/util/List;Lcom/google/common/base/Predicate;)V"))
    public <T extends Entity> void getEntitiesOfTypeWithinAABBHook(Chunk chunk, Class<? extends T> entityClass, AxisAlignedBB aabb, List<T> listToFill, Predicate<? super T> filter) {
        try {
            chunk.getEntitiesOfTypeWithinAABB(entityClass, aabb, listToFill, filter);

        } catch (Exception ignored) {

        }
    }

    @Inject(method = {"onEntityAdded"}, at = {@At(value = "HEAD")})
    private void onEntityAdded(Entity entityIn, CallbackInfo ci) {
    }

    @Inject(method = "getSkyColor", at = @At("HEAD"), cancellable = true)
    public void getSkyColorHook(Entity entityIn, float partialTicks, CallbackInfoReturnable<Vec3d> info) {
        RenderSkyEvent renderSkyEvent = new RenderSkyEvent();
        MinecraftForge.EVENT_BUS.post(renderSkyEvent);

        if (renderSkyEvent.isCanceled()) {
            info.cancel();
            info.setReturnValue(new Vec3d(renderSkyEvent.getColor().getRed() / 255D, renderSkyEvent.getColor().getGreen() / 255D, renderSkyEvent.getColor().getBlue() / 255D));
        }
    }
}

