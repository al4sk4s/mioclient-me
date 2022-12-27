package me.mioclient.asm.mixins;

import me.mioclient.api.managers.Managers;
import me.mioclient.api.util.render.RenderUtil;
import me.mioclient.mod.modules.impl.render.Chams;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderLivingBase;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.awt.*;

import static me.mioclient.api.util.Wrapper.mc;
import static org.lwjgl.opengl.GL11.*;

@Mixin({RenderLivingBase.class})
public abstract class MixinRenderLivingBase<T extends EntityLivingBase> extends Render<T> {

    protected MixinRenderLivingBase(RenderManager renderManager) {
        super(renderManager);
    }

    /**
     * @author t.me/asphyxia1337
     * @reason why not
     **/

    @Inject(method = "renderModel", at = @At(value="INVOKE", target="Lnet/minecraft/client/model/ModelBase;render(Lnet/minecraft/entity/Entity;FFFFFF)V", shift = At.Shift.BEFORE), cancellable = true)
    private void renderModelHook(T entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, float scale, CallbackInfo info) {
        Chams mod = Chams.INSTANCE;

        RenderLivingBase<?> renderLiving = RenderLivingBase.class.cast(this);
        ModelBase model = renderLiving.getMainModel();

        if (mod.isOn() && entity instanceof EntityPlayer) {
            info.cancel();

            boolean isFriend = Managers.FRIENDS.isFriend(entity.getName());

            float newLimbSwing = mod.noInterp.getValue() ? 0.0f : limbSwing;
            float newLimbSwingAmount = mod.noInterp.getValue() ? 0.0f : limbSwingAmount;

            //Prevent it from rendering the chams on ourselves if the Self setting is disabled
            if (!mod.self.getValue() && entity instanceof EntityPlayerSP) {
                model.render(entity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scale);
                return;
            }

            //Troll
            if (mod.sneak.getValue()) {
                entity.setSneaking(true);
            }
            
            //Model
            if (mod.model.getValue() == Chams.Model.VANILLA) {
                model.render(entity, newLimbSwing, newLimbSwingAmount, ageInTicks, netHeadYaw, headPitch, scale);

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

                model.render(entity, newLimbSwing, newLimbSwingAmount, ageInTicks, netHeadYaw, headPitch, scale);

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

                model.render(entity, newLimbSwing, newLimbSwingAmount, ageInTicks, netHeadYaw, headPitch, scale);

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

                model.render(entity, newLimbSwing, newLimbSwingAmount, ageInTicks, netHeadYaw, headPitch, scale);

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

                float f = (float) entity.ticksExisted + mc.getRenderPartialTicks();

                mc.getRenderManager().renderEngine.bindTexture(new ResourceLocation("textures/misc/enchanted_item_glint.png"));

                for (int i = 0; i < 2; ++i) {
                    GlStateManager.matrixMode(GL_TEXTURE);

                    GlStateManager.loadIdentity();

                    glScalef(1.0f, 1.0f, 1.0f);
                    GlStateManager.rotate(30.0f - i * 60.0f, 0.0f, 0.0f, 1.0f);
                    GlStateManager.translate(0.0F, f * (0.001F + (float) i * 0.003F) * 20.0F, 0.0F);

                    GlStateManager.matrixMode(GL_MODELVIEW);

                    model.render(entity, newLimbSwing, newLimbSwingAmount, ageInTicks, netHeadYaw, headPitch, scale);
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
            model.render(entity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scale);
        }
    }
}