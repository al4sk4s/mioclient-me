package me.mioclient.asm.mixins;

import me.mioclient.api.events.impl.RenderItemInFirstPersonEvent;
import me.mioclient.api.managers.Managers;
import me.mioclient.api.util.render.RenderUtil;
import me.mioclient.mod.modules.impl.render.Chams;
import me.mioclient.mod.modules.impl.render.Model;
import me.mioclient.mod.modules.impl.render.Shader;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.ItemRenderer;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.awt.*;

import static org.lwjgl.opengl.GL11.*;

@Mixin(value = {ItemRenderer.class})
public abstract class MixinItemRenderer {

    @Shadow
    @Final
    public Minecraft mc;
    private boolean injection = true;

    @Shadow
    public abstract void renderItemInFirstPerson(AbstractClientPlayer player, float partialTicks, float rotationPitch, EnumHand hand, float swingProgress, ItemStack stack, float equippedProgress);

    @Redirect(method = "renderItemInFirstPerson(Lnet/minecraft/client/entity/AbstractClientPlayer;FFLnet/minecraft/util/EnumHand;FLnet/minecraft/item/ItemStack;F)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/ItemRenderer;renderItemSide(Lnet/minecraft/entity/EntityLivingBase;Lnet/minecraft/item/ItemStack;Lnet/minecraft/client/renderer/block/model/ItemCameraTransforms$TransformType;Z)V"))
    public void renderItemInFirstPerson(ItemRenderer itemRenderer, EntityLivingBase entitylivingbaseIn, ItemStack heldStack, ItemCameraTransforms.TransformType transform, boolean leftHanded) {
        RenderItemInFirstPersonEvent eventPre = new RenderItemInFirstPersonEvent(entitylivingbaseIn, heldStack, transform, leftHanded, 0);
        MinecraftForge.EVENT_BUS.post(eventPre);

        if (!eventPre.isCanceled()) {
            itemRenderer.renderItemSide(entitylivingbaseIn, eventPre.getStack(), eventPre.getTransformType(), leftHanded);
        }

        RenderItemInFirstPersonEvent eventPost = new RenderItemInFirstPersonEvent(entitylivingbaseIn, heldStack, transform, leftHanded, 1);
        MinecraftForge.EVENT_BUS.post(eventPost);
    }

    @Inject(method={"renderFireInFirstPerson"}, at={@At(value="HEAD")}, cancellable=true)
    public void renderFireInFirstPersonHook(CallbackInfo info) {
        if (Shader.INSTANCE.isOn()) {
            info.cancel();
        }
    }

    @Inject(method = "renderItemInFirstPerson(Lnet/minecraft/client/entity/AbstractClientPlayer;FFLnet/minecraft/util/EnumHand;FLnet/minecraft/item/ItemStack;F)V", at = @At(value = "HEAD"), cancellable = true)
    public void renderItemInFirstPersonHook(AbstractClientPlayer player, float partialTicks, float rotationPitch, EnumHand hand, float swingProgress, ItemStack stack, float equippedProgress, CallbackInfo info) {
        Chams mod = Chams.INSTANCE;

        if (injection) {

            info.cancel();

            boolean isFriend = Managers.FRIENDS.isFriend(player.getName());

            injection = false;

            if (mod.isOn() && mod.self.getValue() && hand == EnumHand.MAIN_HAND && stack.isEmpty()) {

                //Model
                if (mod.model.getValue() == Chams.Model.VANILLA) {
                    renderItemInFirstPerson(player, partialTicks, rotationPitch, hand, swingProgress, stack, equippedProgress);

                } else if (mod.model.getValue() == Chams.Model.XQZ) {

                    glEnable(GL_POLYGON_OFFSET_FILL);
                    GlStateManager.enablePolygonOffset();
                    glPolygonOffset(1.0f, -1000000);

                    if (mod.modelColor.booleanValue) {

                        Color rainbow = Managers.COLORS.getRainbow();
                        Color color = isFriend ?
                                Managers.COLORS.getFriendColor(mod.modelColor.getValue().getAlpha()) :
                                mod.rainbow.getValue() ?
                                        new Color(rainbow.getRed(), rainbow.getGreen(), rainbow.getBlue(), mod.modelColor.getValue().getAlpha()) :
                                        new Color(mod.modelColor.getValue().getRed(), mod.modelColor.getValue().getGreen(), mod.modelColor.getValue().getBlue(), mod.modelColor.getValue().getAlpha());

                        RenderUtil.glColor(color);
                    }

                    renderItemInFirstPerson(player, partialTicks, rotationPitch, hand, swingProgress, stack, equippedProgress);

                    glDisable(GL_POLYGON_OFFSET_FILL);
                    GlStateManager.disablePolygonOffset();
                    glPolygonOffset(1.0f, 1000000);
                }

                //Wireframe
                if (mod.wireframe.getValue()) {

                    Color rainbow = Managers.COLORS.getRainbow();
                    Color color = isFriend ?
                            Managers.COLORS.getFriendColor(mod.lineColor.booleanValue ? mod.lineColor.getValue().getAlpha() : mod.color.getValue().getAlpha()) :
                            mod.rainbow.getValue() ?
                                    new Color(rainbow.getRed(), rainbow.getGreen(), rainbow.getBlue(), mod.color.getValue().getAlpha()) :
                                    mod.lineColor.booleanValue ?
                                            new Color(mod.lineColor.getValue().getRed(), mod.lineColor.getValue().getGreen(), mod.lineColor.getValue().getBlue(), mod.lineColor.getValue().getAlpha()) :
                                            new Color(mod.color.getValue().getRed(), mod.color.getValue().getGreen(), mod.color.getValue().getBlue(), mod.color.getValue().getAlpha());

                    glPushMatrix();
                    glPushAttrib(GL_ALL_ATTRIB_BITS);

                    glPolygonMode(GL_FRONT_AND_BACK, GL_LINE);
                    glDisable(GL_TEXTURE_2D);
                    glDisable(GL_LIGHTING);
                    glDisable(GL_DEPTH_TEST);
                    glEnable(GL_LINE_SMOOTH);
                    glEnable(GL_BLEND);

                    GlStateManager.blendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);

                    RenderUtil.glColor(color);

                    GlStateManager.glLineWidth(mod.lineWidth.getValue());

                    renderItemInFirstPerson(player, partialTicks, rotationPitch, hand, swingProgress, stack, equippedProgress);

                    glPopAttrib();
                    glPopMatrix();
                }

                //Fill
                if (mod.fill.getValue()) {

                    Color rainbow = Managers.COLORS.getRainbow();
                    Color color = isFriend ?
                            Managers.COLORS.getFriendColor(mod.color.getValue().getAlpha()) :
                            mod.rainbow.getValue() ?
                                    new Color(rainbow.getRed(), rainbow.getGreen(), rainbow.getBlue(), mod.color.getValue().getAlpha()) :
                                    new Color(mod.color.getValue().getRed(), mod.color.getValue().getGreen(), mod.color.getValue().getBlue(), mod.color.getValue().getAlpha());

                    glPushAttrib(GL_ALL_ATTRIB_BITS);
                    glDisable(GL_ALPHA_TEST);
                    glDisable(GL_TEXTURE_2D);
                    glDisable(GL_LIGHTING);
                    glEnable(GL_BLEND);
                    glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);

                    glLineWidth(1.5f);

                    glEnable(GL_STENCIL_TEST);

                    if (mod.xqz.getValue()) {
                        glDisable(GL_DEPTH_TEST);
                        glDepthMask(false);
                    }

                    glEnable(GL_POLYGON_OFFSET_LINE);
                    RenderUtil.glColor(color);

                    renderItemInFirstPerson(player, partialTicks, rotationPitch, hand, swingProgress, stack, equippedProgress);

                    if (mod.xqz.getValue()) {
                        glEnable(GL_DEPTH_TEST);
                        glDepthMask(true);
                    }

                    glEnable(GL_BLEND);
                    glEnable(GL_LIGHTING);
                    glEnable(GL_TEXTURE_2D);
                    glEnable(GL_ALPHA_TEST);
                    glPopAttrib();

                }

                //Glint
                if (mod.glint.getValue()) {

                    Color rainbow = Managers.COLORS.getRainbow();
                    Color color = isFriend ?
                            Managers.COLORS.getFriendColor(mod.color.getValue().getAlpha()) :
                            mod.rainbow.getValue() ?
                                    new Color(rainbow.getRed(), rainbow.getGreen(), rainbow.getBlue(), mod.color.getValue().getAlpha()) :
                                    new Color(mod.color.getValue().getRed(), mod.color.getValue().getGreen(), mod.color.getValue().getBlue(), mod.color.getValue().getAlpha());

                    glPushMatrix();
                    glPushAttrib(GL_ALL_ATTRIB_BITS);
                    glPolygonMode(GL_FRONT_AND_BACK, GL_FILL);
                    glDisable(GL_LIGHTING);

                    glDepthRange(0, 0.1);

                    glEnable(GL_BLEND);

                    RenderUtil.glColor(color);

                    GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_COLOR, GlStateManager.DestFactor.ONE);

                    float f = (float) player.ticksExisted + mc.getRenderPartialTicks();

                    mc.getRenderManager().renderEngine.bindTexture(new ResourceLocation("textures/misc/enchanted_item_glint.png"));

                    for (int i = 0; i < 2; ++i) {
                        GlStateManager.matrixMode(GL_TEXTURE);

                        GlStateManager.loadIdentity();

                        glScalef(1.0f, 1.0f, 1.0f);
                        GlStateManager.rotate(30.0f - i * 60.0f, 0.0f, 0.0f, 1.0f);
                        GlStateManager.translate(0.0F, f * (0.001F + (float) i * 0.003F) * 20.0F, 0.0F);

                        GlStateManager.matrixMode(GL_MODELVIEW);

                        renderItemInFirstPerson(player, partialTicks, rotationPitch, hand, swingProgress, stack, equippedProgress);
                    }

                    GlStateManager.matrixMode(GL_TEXTURE);
                    GlStateManager.loadIdentity();
                    GlStateManager.matrixMode(GL_MODELVIEW);

                    glDisable(GL_BLEND);

                    glDepthRange(0, 1);

                    glEnable(GL_LIGHTING);

                    glPopAttrib();
                    glPopMatrix();
                }

            } else {
                renderItemInFirstPerson(player, partialTicks, rotationPitch, hand, swingProgress, stack, equippedProgress);
            }

            injection = true;
        }
    }

    @Inject(method = "rotateArm", at = @At("HEAD"), cancellable = true)
    public void rotateArmHook(float partialTicks, CallbackInfo info) {
        Model mod = Model.INSTANCE;

        if (mod.isOn() && mod.noSway.getValue()) {
            info.cancel();
        }
    }
}



