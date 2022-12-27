package me.mioclient.asm.mixins;

import com.google.common.base.Predicate;
import me.mioclient.api.events.impl.PerspectiveEvent;
import me.mioclient.mod.modules.impl.exploit.NoHitBox;
import me.mioclient.mod.modules.impl.render.Ambience;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.client.renderer.EntityRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemPickaxe;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraftforge.common.MinecraftForge;
import org.lwjgl.util.glu.Project;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.ArrayList;
import java.util.List;

@Mixin(value = {EntityRenderer.class})
public class MixinEntityRenderer {

    Minecraft mc = Minecraft.getMinecraft();

    @Redirect(method = {"getMouseOver"}, at = @At(value = "INVOKE", target = "Lnet/minecraft/client/multiplayer/WorldClient;getEntitiesInAABBexcluding(Lnet/minecraft/entity/Entity;Lnet/minecraft/util/math/AxisAlignedBB;Lcom/google/common/base/Predicate;)Ljava/util/List;"))
    public List<Entity> getEntitiesInAABBexcluding(WorldClient worldClient, Entity entityIn, AxisAlignedBB boundingBox, Predicate predicate) {
        NoHitBox mod = NoHitBox.INSTANCE;

        if (mod.isOn()
                && (mc.player.getHeldItemMainhand().getItem() instanceof ItemPickaxe && mod.pickaxe.getValue()
                || mc.player.getHeldItemMainhand().getItem() == Items.END_CRYSTAL && mod.crystal.getValue()
                || mc.player.getHeldItemMainhand().getItem() == Items.GOLDEN_APPLE && mod.gapple.getValue()
                || mc.player.getHeldItemMainhand().getItem() == Item.getItemFromBlock(Blocks.OBSIDIAN) && mod.obby.getValue()
                || mc.player.getHeldItemMainhand().getItem() == Items.FLINT_AND_STEEL
                || mc.player.getHeldItemMainhand().getItem() == Items.TNT_MINECART)) {

            return new ArrayList<>();
        }
        return worldClient.getEntitiesInAABBexcluding(entityIn, boundingBox, predicate);
    }

    @Redirect(method = { "setupCameraTransform" },  at = @At(value = "INVOKE",  target = "Lorg/lwjgl/util/glu/Project;gluPerspective(FFFF)V"))
    private void onSetupCameraTransform(float f, float f2, float f3, float f4) {
        PerspectiveEvent perspectiveEvent = new PerspectiveEvent(mc.displayWidth / (float) mc.displayHeight);
        MinecraftForge.EVENT_BUS.post(perspectiveEvent);
        Project.gluPerspective(f,  perspectiveEvent.getAngle(),  f3,  f4);
    }

    @Redirect(method = { "renderWorldPass" },  at = @At(value = "INVOKE",  target = "Lorg/lwjgl/util/glu/Project;gluPerspective(FFFF)V"))
    private void onRenderWorldPass(float f, float f2, float f3, float f4) {
        PerspectiveEvent perspectiveEvent = new PerspectiveEvent(mc.displayWidth / (float) mc.displayHeight);
        MinecraftForge.EVENT_BUS.post(perspectiveEvent);
        Project.gluPerspective(f,  perspectiveEvent.getAngle(),  f3,  f4);
    }

    @Redirect(method = { "renderCloudsCheck" },  at = @At(value = "INVOKE",  target = "Lorg/lwjgl/util/glu/Project;gluPerspective(FFFF)V"))
    private void onRenderCloudsCheck(float f, float f2, float f3, float f4) {
        PerspectiveEvent perspectiveEvent = new PerspectiveEvent(mc.displayWidth / (float) mc.displayHeight);
        MinecraftForge.EVENT_BUS.post(perspectiveEvent);
        Project.gluPerspective(f,  perspectiveEvent.getAngle(),  f3,  f4);
    }

    @ModifyVariable(method = "updateLightmap", at = @At("STORE"), index = 20)
    public int red(int red) {
        Ambience mod = Ambience.INSTANCE;

        if (mod.isOn() && mod.lightMap.booleanValue) {
            red = mod.lightMap.getValue().getRed();
        }
        return red;
    }

    @ModifyVariable(method = "updateLightmap", at = @At("STORE"), index = 21)
    public int green(int green) {
        Ambience mod = Ambience.INSTANCE;

        if (mod.isOn() && mod.lightMap.booleanValue) {
            green = mod.lightMap.getValue().getGreen();
        }
        return green;
    }

    @ModifyVariable(method = "updateLightmap", at = @At("STORE"), index = 22)
    public int blue(int blue) {
        Ambience mod = Ambience.INSTANCE;

        if (mod.isOn() && mod.lightMap.booleanValue) {
            blue = mod.lightMap.getValue().getBlue();
        }
        return blue;
    }
}








