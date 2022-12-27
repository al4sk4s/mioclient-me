package me.mioclient.asm.mixins;

import me.mioclient.mod.modules.impl.render.Model;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderItem;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({ RenderItem.class })
public class MixinRenderItem {

    private float angle;

    Minecraft mc = Minecraft.getMinecraft();

    @Inject(method = "renderItemModel", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/RenderItem;renderItem(Lnet/minecraft/item/ItemStack;Lnet/minecraft/client/renderer/block/model/IBakedModel;)V", shift = At.Shift.BEFORE))
    private void renderCustom(ItemStack stack, IBakedModel bakedmodel, ItemCameraTransforms.TransformType transform, boolean leftHanded, CallbackInfo ci) {
        Model mod = Model.INSTANCE;

        float scale = 1.0f;
        float xOffset = 0.0f;
        float yOffset = 0.0f;

        if (leftHanded && mod.isOn() && mc.player.getHeldItemOffhand() == stack) {
            scale = mod.offScale.getValue().floatValue();
            xOffset = mod.offX.getValue().floatValue();
            yOffset = mod.offY.getValue().floatValue();

        } else if (mod.isOn() && mc.player.getHeldItemMainhand() == stack) {
            scale = mod.mainScale.getValue().floatValue();
            xOffset -= mod.mainX.getValue().floatValue();
            yOffset = mod.mainY.getValue().floatValue();
        }

        if (mod.isOn()) {
            GlStateManager.scale(scale, scale, scale);

            if (mc.player.getActiveItemStack() != stack) {
                GlStateManager.translate(xOffset, yOffset, 0.0f);
            }
        }
    }

    @Inject(method = {"renderItemModel"}, at = @At(value = "HEAD"))
    public void renderItem(ItemStack stack, IBakedModel bakedmodel, ItemCameraTransforms.TransformType transform, boolean leftHanded, CallbackInfo ci) {
        Model mod = Model.INSTANCE;

        if (mod.isOn() && (transform == ItemCameraTransforms.TransformType.FIRST_PERSON_LEFT_HAND || transform == ItemCameraTransforms.TransformType.FIRST_PERSON_RIGHT_HAND)) {

            if (transform == ItemCameraTransforms.TransformType.FIRST_PERSON_LEFT_HAND) {

                if (mc.player.getActiveHand() == EnumHand.OFF_HAND && mc.player.isHandActive()) {
                    return;
                    }
                }

                if (mod.isOn() && (mod.spinX.getValue() || mod.spinY.getValue())) {
                    GlStateManager.rotate(angle, mod.spinX.getValue() ? angle : 0, mod.spinY.getValue() ? angle : 0, 0);
                    angle++;
                }

            } else {

                if (mc.player.getActiveHand() == EnumHand.MAIN_HAND && mc.player.isHandActive()) {
                    return;
                }

                if (mod.isOn() && (mod.spinX.getValue() || mod.spinY.getValue())) {
                    GlStateManager.rotate(angle, mod.spinX.getValue() ? angle : 0, mod.spinY.getValue() ? angle : 0, 0);
                    angle++;
            }
        }
    }
}
