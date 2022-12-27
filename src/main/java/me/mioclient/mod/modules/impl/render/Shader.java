package me.mioclient.mod.modules.impl.render;

import me.mioclient.api.events.impl.Render3DEvent;
import me.mioclient.api.events.impl.RenderItemInFirstPersonEvent;
import me.mioclient.api.managers.Managers;
import me.mioclient.api.util.math.InterpolationUtil;
import me.mioclient.api.util.render.shader.framebuffer.impl.ItemShader;
import me.mioclient.asm.accessors.IEntityRenderer;
import me.mioclient.mod.modules.Category;
import me.mioclient.mod.modules.Module;
import me.mioclient.mod.modules.settings.Setting;
import net.minecraft.client.gui.GuiDownloadTerrain;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityEnderCrystal;
import net.minecraft.entity.item.EntityExpBottle;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.lwjgl.opengl.Display;

import java.awt.*;

public class Shader extends Module {

    public static Shader INSTANCE;

    private final Setting<Boolean> players =
            add(new Setting<>("Players", false));
    private final Setting<Boolean> crystals =
            add(new Setting<>("Crystals", false));
    private final Setting<Boolean> xp =
            add(new Setting<>("Exp", false));
    private final Setting<Boolean> items =
            add(new Setting<>("Items", false));
    private final Setting<Boolean> self =
            add(new Setting<>("Self", true));

    private final Setting<Color> color =
            add(new Setting<>("Color", new Color(0xFF7D7DD5, true)));

    private final Setting<Boolean> glow =
            add(new Setting<>("Glow", true).setParent());
    private final Setting<Float> radius =
            add(new Setting<>("Radius", 4.0f, 0.1f, 6.0f, v -> glow.isOpen()));
    private final Setting<Float> smoothness =
            add(new Setting<>("Smoothness", 1.0f, 0.1f, 1.0f, v -> glow.isOpen()));
    private final Setting<Integer> alpha =
            add(new Setting<>("Alpha", 50, 1, 50, v -> glow.isOpen()));

    private final Setting<Boolean> model =
            add(new Setting<>("Model", true));

    private final Setting<Integer> range =
            add(new Setting<>("Range", 75, 5, 250));

    private final Setting<Boolean> fovOnly =
            add(new Setting<>("FOVOnly", false));

    private boolean forceRender;

    public Shader() {
        super("Shader", "Is in beta test stage.", Category.RENDER, true);
        INSTANCE = this;
    }

    @SubscribeEvent
    public void renderItemInFirstPerson(RenderItemInFirstPersonEvent event) {
        if (fullNullCheck() || !isOn() || event.getStage() != 0 || forceRender || !self.getValue()) return;

        event.cancel();
    }

    @SubscribeEvent
    public void onRenderWorldLastEvent(RenderWorldLastEvent event) {
        if (fullNullCheck() || !isOn()) return;

        if (((Display.isActive() || Display.isVisible()) && mc.gameSettings.thirdPersonView == 0) && !(mc.currentScreen instanceof GuiDownloadTerrain)) {
            GlStateManager.pushMatrix();
            GlStateManager.pushAttrib();
            GlStateManager.enableBlend();
            GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
            GlStateManager.enableDepth();
            GlStateManager.depthMask(true);
            GlStateManager.enableAlpha();

            ItemShader shader = ItemShader.INSTANCE;

            shader.mix = color.getValue().getAlpha() / 255.0f;
            shader.alpha = (205 + alpha.getValue()) / 255.0f;
            shader.model = model.getValue();
            shader.startDraw(mc.getRenderPartialTicks());

            forceRender = true;
            mc.world.loadedEntityList.stream().filter(entity ->
                    entity != null
                    && ((entity != mc.player
                    || entity != mc.getRenderViewEntity())
                    && mc.getRenderManager().getEntityRenderObject(entity) != null)
                    && (entity instanceof EntityPlayer && players.getValue()
                    && !((EntityPlayer) entity).isSpectator()
                    || entity instanceof EntityEnderCrystal
                    && crystals.getValue()
                    || entity instanceof EntityExpBottle
                    && xp.getValue()
                    || entity instanceof EntityItem
                    && items.getValue())).forEach(entity -> {

                if ((entity.getDistance(mc.player) > range.getValue()) || (fovOnly.getValue() && !Managers.ROTATIONS.isInFov(entity.getPosition()))) return;

                Vec3d vector = InterpolationUtil.getInterpolatedRenderPos(entity, event.getPartialTicks());

                if (entity instanceof EntityPlayer) {
                    ((EntityPlayer) entity).hurtTime = 0;
                }

                Render<Entity> render = mc.getRenderManager().getEntityRenderObject(entity);

                if (render != null) {
                    try {
                        render.doRender(entity, vector.x, vector.y, vector.z, entity.rotationYaw, event.getPartialTicks());
                    } catch (Exception ignored) {

                    }
                }
            });

            if (self.getValue()) {
                ((IEntityRenderer) mc.entityRenderer).invokeRenderHand(mc.getRenderPartialTicks(), 2);
            }

            forceRender = false;
            shader.stopDraw(color.getValue(), glow.getValue() ? radius.getValue() : 0.0f, smoothness.getValue());

            GlStateManager.disableBlend();
            GlStateManager.disableAlpha();
            GlStateManager.disableDepth();
            GlStateManager.popAttrib();
            GlStateManager.popMatrix();
        }
    }
}