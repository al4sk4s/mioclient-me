package me.mioclient.asm.mixins;

import me.mioclient.mod.modules.impl.render.Chams;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.renderer.entity.layers.LayerArmorBase;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.EntityEquipmentSlot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LayerArmorBase.class)
public class MixinLayerArmorBase {

    @Shadow
    private void renderArmorLayer(EntityLivingBase entityLivingBaseIn, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch, float scale, EntityEquipmentSlot slotIn) {
    }

    @Inject(method={"doRenderLayer"}, at=@At(value="INVOKE", target="Lnet/minecraft/client/renderer/entity/layers/LayerArmorBase;renderArmorLayer(Lnet/minecraft/entity/EntityLivingBase;FFFFFFFLnet/minecraft/inventory/EntityEquipmentSlot;)V", shift = At.Shift.BEFORE), cancellable = true)
    public void doRenderLayerHook(EntityLivingBase entity, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch, float scale, CallbackInfo info) {
        Chams mod = Chams.INSTANCE;

        if (mod.isOn() && entity instanceof EntityPlayer) {

            info.cancel();

            float newLimbSwing = mod.isOn() && mod.noInterp.getValue() ? 0.0f : limbSwing;
            float newLimbSwingAmount = mod.isOn() && mod.noInterp.getValue() ? 0.0f : limbSwingAmount;

            if (!mod.self.getValue() && entity instanceof EntityPlayerSP) {
                renderLayers(entity, limbSwing, limbSwingAmount, partialTicks, ageInTicks, netHeadYaw, headPitch, scale);
                return;
            }

            renderLayers(entity, newLimbSwing, newLimbSwingAmount, partialTicks, ageInTicks, netHeadYaw, headPitch, scale);

        } else {
            renderLayers(entity, limbSwing, limbSwingAmount, partialTicks, ageInTicks, netHeadYaw, headPitch, scale);
        }
    }

    private void renderLayers(EntityLivingBase entity, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch, float scale) {
        renderArmorLayer(entity, limbSwing, limbSwingAmount, partialTicks, ageInTicks, netHeadYaw, headPitch, scale, EntityEquipmentSlot.CHEST);
        renderArmorLayer(entity, limbSwing, limbSwingAmount, partialTicks, ageInTicks, netHeadYaw, headPitch, scale, EntityEquipmentSlot.LEGS);
        renderArmorLayer(entity, limbSwing, limbSwingAmount, partialTicks, ageInTicks, netHeadYaw, headPitch, scale, EntityEquipmentSlot.FEET);
        renderArmorLayer(entity, limbSwing, limbSwingAmount, partialTicks, ageInTicks, netHeadYaw, headPitch, scale, EntityEquipmentSlot.HEAD);
    }
}
