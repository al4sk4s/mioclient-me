package me.mioclient.mod.modules.impl.render;

import com.google.common.collect.Maps;
import me.mioclient.api.events.impl.Render2DEvent;
import me.mioclient.api.events.impl.Render3DEvent;
import me.mioclient.api.managers.Managers;
import me.mioclient.api.util.render.ColorUtil;
import me.mioclient.api.util.render.RenderUtil;
import me.mioclient.mod.modules.Category;
import me.mioclient.mod.modules.Module;
import me.mioclient.mod.modules.settings.Setting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.GL11;

import java.awt.*;
import java.util.Map;

public class Tracers extends Module {

    private final Setting<Mode> mode =
            add(new Setting<>("Mode", Mode.ARROW));
    private final Setting<Integer> range =
            add(new Setting<>("Range", 100, 10, 200));
    private final Setting<Color> color =
            add(new Setting<>("Color", new Color(0xB61F1F)));
    private final Setting<Integer> radius =
            add(new Setting<>("Radius", 80, 10, 200, v -> mode.getValue() == Mode.ARROW));
    private final Setting<Float> size =
            add(new Setting<>("Size", 7.5f, 5.0f, 25.0f, v -> mode.getValue() == Mode.ARROW));
    private final Setting<Boolean> outline =
            add(new Setting<>("Outline", true, v -> mode.getValue() == Mode.ARROW));

    private final EntityListener entityListener = new EntityListener();

    private final Frustum frustum = new Frustum();

    public Tracers() {
        super("Tracers", "Points to the players on your screen", Category.RENDER);
    }

    private enum Mode {
        TRACER,
        ARROW
    }

    @Override
    public String getInfo() {
        return String.valueOf(mode.getValue());
    }

    @Override
    public void onRender2D(Render2DEvent event) {
        if (mode.getValue() == Mode.ARROW) {

            entityListener.render();

            mc.world.loadedEntityList.forEach(o -> {
                if (o instanceof EntityPlayer && isValid((EntityPlayer) o)) {

                    EntityPlayer entity = (EntityPlayer) o;
                    Vec3d pos = entityListener.getEntityLowerBounds().get(entity);

                    if (pos != null && !isOnScreen(pos) && !isInViewFrustum(entity)) {

                        int alpha = (int) MathHelper.clamp(255.0f - 255.0f / (float) range.getValue() * mc.player.getDistance(entity), 100.0f, 255.0f);
                        Color friendColor = new Color(0, 191, 255);
                        Color color = Managers.FRIENDS.isFriend(entity.getName())
                                ? ColorUtil.injectAlpha(friendColor, alpha)
                                : ColorUtil.injectAlpha(this.color.getValue(), alpha);
                        
                        int x = Display.getWidth() / 2 / (mc.gameSettings.guiScale == 0 ? 1 : mc.gameSettings.guiScale);
                        int y = Display.getHeight() / 2 / (mc.gameSettings.guiScale == 0 ? 1 : mc.gameSettings.guiScale);
                        float yaw = getRotations(entity) - mc.player.rotationYaw;
                        GL11.glTranslatef((float) x, (float) y, 0.0f);
                        GL11.glRotatef(yaw, 0.0f, 0.0f, 1.0f);
                        GL11.glTranslatef((float) (-x), (float) (-y), 0.0f);
                        RenderUtil.drawArrowPointer(x, y - radius.getValue(), size.getValue(), 2.0f, 1.0f, outline.getValue(), 1.1f, color.getRGB());
                        GL11.glTranslatef((float) x, (float) y, 0.0f);
                        GL11.glRotatef(-yaw, 0.0f, 0.0f, 1.0f);
                        GL11.glTranslatef((float) (-x), (float) (-y), 0.0f);
                    }
                }
            });
        }
    }

    @Override
    public void onRender3D(Render3DEvent event) {
        if (mode.getValue() == Mode.TRACER) {
            GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
            GL11.glEnable(GL11.GL_BLEND);
            GL11.glEnable(GL11.GL_LINE_SMOOTH);
            GL11.glHint(GL11.GL_LINE_SMOOTH_HINT, GL11.GL_NICEST);
            GL11.glLineWidth(0.6f);
            GL11.glDisable(GL11.GL_TEXTURE_2D);
            GL11.glDisable(GL11.GL_DEPTH_TEST);
            GL11.glDepthMask(false);
            GL11.glBegin(GL11.GL_LINES);

            for (EntityPlayer entity : mc.world.playerEntities) {
                if (entity != mc.player) {
                    drawTraces(entity);
                }
            }
            GL11.glEnd();

            GL11.glEnable(GL11.GL_TEXTURE_2D);
            GL11.glDisable(GL11.GL_LINE_SMOOTH);
            GL11.glEnable(GL11.GL_DEPTH_TEST);
            GL11.glDepthMask(true);
            GL11.glDisable(GL11.GL_BLEND);
            GL11.glColor4f(1.0f, 1.0f, 1.0f, 1.0f);
        }
    }

    private boolean isOnScreen(Vec3d pos) {
        if (!(pos.x > -1.0)) return false;
        if (!(pos.y < 1.0)) return false;
        if (!(pos.x > -1.0)) return false;
        if (!(pos.z < 1.0)) return false;

        int n = mc.gameSettings.guiScale == 0 ? 1 : mc.gameSettings.guiScale;
        if (!(pos.x / (double) n >= 0.0)) return false;
        int n2 = mc.gameSettings.guiScale == 0 ? 1 : mc.gameSettings.guiScale;
        if (!(pos.x / (double) n2 <= (double) Display.getWidth())) return false;
        int n3 = mc.gameSettings.guiScale == 0 ? 1 : mc.gameSettings.guiScale;
        if (!(pos.y / (double) n3 >= 0.0)) return false;
        int n4 = mc.gameSettings.guiScale == 0 ? 1 : mc.gameSettings.guiScale;

        return pos.y / (double) n4 <= (double) Display.getHeight();
    }

    private boolean isInViewFrustum(Entity entity) {
        Entity current = Minecraft.getMinecraft().getRenderViewEntity();
        frustum.setPosition(current.posX, current.posY, current.posZ);
        return frustum.isBoundingBoxInFrustum(entity.getEntityBoundingBox()) || entity.ignoreFrustumCheck;
    }

    private boolean isValid(EntityPlayer entity) {
        return entity != mc.player && entity.isEntityAlive();
    }

    private float getRotations(EntityLivingBase ent) {
        double x = ent.posX - mc.player.posX;
        double z = ent.posZ - mc.player.posZ;
        return (float) (-(Math.atan2(x, z) * 57.29577951308232));
    }

    private static class EntityListener {
        private final Map<Entity, Vec3d> entityUpperBounds = Maps.newHashMap();
        private final Map<Entity, Vec3d> entityLowerBounds = Maps.newHashMap();

        private EntityListener() {
        }

        private void render() {
            if (!entityUpperBounds.isEmpty()) {
                entityUpperBounds.clear();
            }
            if (!entityLowerBounds.isEmpty()) {
                entityLowerBounds.clear();
            }
            for (Entity e : mc.world.loadedEntityList) {
                Vec3d bound = getEntityRenderPosition(e);
                bound.add(new Vec3d(0.0, (double) e.height + 0.2, 0.0));
                Vec3d upperBounds = RenderUtil.get2DPos(bound.x, bound.y, bound.z);
                Vec3d lowerBounds = RenderUtil.get2DPos(bound.x, bound.y - 2.0, bound.z);
                if (upperBounds == null || lowerBounds == null) continue;
                entityUpperBounds.put(e, upperBounds);
                entityLowerBounds.put(e, lowerBounds);
            }
        }

        private Vec3d getEntityRenderPosition(Entity entity) {
            double partial = mc.timer.renderPartialTicks;
            double x = entity.lastTickPosX + (entity.posX - entity.lastTickPosX) * partial - mc.getRenderManager().viewerPosX;
            double y = entity.lastTickPosY + (entity.posY - entity.lastTickPosY) * partial - mc.getRenderManager().viewerPosY;
            double z = entity.lastTickPosZ + (entity.posZ - entity.lastTickPosZ) * partial - mc.getRenderManager().viewerPosZ;
            return new Vec3d(x, y, z);
        }

        public Map<Entity, Vec3d> getEntityLowerBounds() {
            return entityLowerBounds;
        }
    }

    private void drawTraces(Entity entity) {
        double x = (entity.lastTickPosX + (entity.posX - entity.lastTickPosX) * mc.getRenderPartialTicks() - mc.getRenderManager().viewerPosX);
        double y = (entity.lastTickPosY + (entity.posY - entity.lastTickPosY) * mc.getRenderPartialTicks() - mc.getRenderManager().viewerPosY);
        double z = (entity.lastTickPosZ + (entity.posZ - entity.lastTickPosZ) * mc.getRenderPartialTicks() - mc.getRenderManager().viewerPosZ);

        Vec3d eyes = new Vec3d(0, 0, 1)
                .rotatePitch(-(float) Math.toRadians(mc.getRenderViewEntity().rotationPitch))
                .rotateYaw(-(float) Math.toRadians(mc.getRenderViewEntity().rotationYaw));

        boolean isFriend = Managers.FRIENDS.isFriend(entity.getName());

        GL11.glColor4f((isFriend ? 0.0f : color.getValue().getRed()) / 255.0f,
                (isFriend ? 191.0f : color.getValue().getGreen()) / 255.0f,
                (isFriend ? 255.0f : color.getValue().getBlue()) / 255.0f,
                MathHelper.clamp(255.0f - 255.0f / (float) range.getValue().intValue() * mc.player.getDistance(entity), 100.0f, 255.0f));

        GL11.glVertex3d(eyes.x, eyes.y + mc.getRenderViewEntity().getEyeHeight(), eyes.z);
        GL11.glVertex3d(x, y, z);
    }
}

