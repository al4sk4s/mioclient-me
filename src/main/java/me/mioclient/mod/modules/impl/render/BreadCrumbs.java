package me.mioclient.mod.modules.impl.render;

import me.mioclient.api.util.math.MathUtil;
import me.mioclient.api.util.render.ColorUtil;
import me.mioclient.mod.modules.Category;
import me.mioclient.mod.modules.Module;
import me.mioclient.mod.modules.settings.Setting;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityEnderPearl;
import net.minecraft.entity.item.EntityExpBottle;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.lwjgl.opengl.GL11;

import java.awt.*;
import java.util.List;
import java.util.*;

public class BreadCrumbs extends Module {

    private final Setting<Float> lineWidth =
            add(new Setting<>("Width", 0.8f, 0.1f, 3.0f));
    private final Setting<Integer> timeExisted =
            add(new Setting<>("Delay", 1000, 100, 3000));

    private final Setting<Boolean> xp =
            add(new Setting<>("Exp", true));
    private final Setting<Boolean> arrow =
            add(new Setting<>("Arrows", true));
    private final Setting<Boolean> self =
            add(new Setting<>("Self", true));

    private final Setting<Color> color =
            add(new Setting<>("Color", new Color(125, 125, 213)).hideAlpha());
    private final Setting<Color> secondColor =
            add(new Setting<>("SecondColor", new Color(0xBF80FF)).injectBoolean(false).hideAlpha());

    protected Map trails = new HashMap<>();

    public BreadCrumbs() {
        super("BreadCrumbs", "Draws trails behind projectiles and you (bread crumbs)", Category.RENDER, true);
    }

    @Override
    public void onTick() {
        if (!nullCheck()) {

            for (Entity entity : mc.world.loadedEntityList) {
                if (isValid(entity)) {
                    if (trails.containsKey(entity.getUniqueID())) {
                        if (entity.isDead) {
                            if (((ItemTrail) trails.get(entity.getUniqueID())).timer.isPaused()) {
                                ((ItemTrail) trails.get(entity.getUniqueID())).timer.resetDelay();
                            }

                            ((ItemTrail) trails.get(entity.getUniqueID())).timer.setPaused(false);
                        } else {
                            ((ItemTrail) trails.get(entity.getUniqueID())).positions.add(new Position(entity.getPositionVector()));
                        }
                    } else {
                        trails.put(entity.getUniqueID(), new ItemTrail(entity));
                    }
                }
            }

            if (self.getValue()) {

                if (trails.containsKey(mc.player.getUniqueID())) {

                    BreadCrumbs.ItemTrail playerTrail = (BreadCrumbs.ItemTrail) trails.get(mc.player.getUniqueID());
                    playerTrail.timer.resetDelay();
                    List toRemove = new ArrayList();

                    for (Object o : playerTrail.positions) {
                        Position position = (Position) o;
                        if (System.currentTimeMillis() - position.time > timeExisted.getValue()) {
                            toRemove.add(position);
                        }
                    }

                    playerTrail.positions.removeAll(toRemove);
                    playerTrail.positions.add(new BreadCrumbs.Position(mc.player.getPositionVector()));

                } else {
                    trails.put(mc.player.getUniqueID(), new BreadCrumbs.ItemTrail(mc.player));
                }

            } else trails.remove(mc.player.getUniqueID());

        }
    }

    @SubscribeEvent
    public void onRenderWorld(RenderWorldLastEvent event) {
        if (!nullCheck()) {

            for (Object o : trails.entrySet()) {
                Map.Entry entry = (Map.Entry) o;
                if (((ItemTrail) entry.getValue()).entity.isDead || mc.world.getEntityByID(((ItemTrail) entry.getValue()).entity.getEntityId()) == null) {

                    if (((ItemTrail) entry.getValue()).timer.isPaused()) {
                        ((ItemTrail) entry.getValue()).timer.resetDelay();
                    }
                    ((ItemTrail) entry.getValue()).timer.setPaused(false);
                }

                if (!((ItemTrail) entry.getValue()).timer.isPassed()) {
                    drawTrail((ItemTrail) entry.getValue());
                }
            }

        }
    }

    public void drawTrail(BreadCrumbs.ItemTrail trail) {
        double fadeAmount = MathUtil.normalize((double)(System.currentTimeMillis() - trail.timer.getStartTime()), 0.0D, timeExisted.getValue());
        int alpha = (int)(fadeAmount * 255.0D);
        alpha = MathHelper.clamp(alpha, 0, 255);
        alpha = 255 - alpha;
        alpha = trail.timer.isPaused() ? 255 : alpha;
        Color fadeColor = new Color(secondColor.getValue().getRed(), secondColor.getValue().getGreen(), secondColor.getValue().getBlue(), alpha);

        GlStateManager.pushMatrix();
        GlStateManager.disableDepth();
        GlStateManager.disableLighting();
        GlStateManager.depthMask(false);
        GlStateManager.disableAlpha();
        GlStateManager.disableCull();
        GlStateManager.enableBlend();
        GL11.glDisable(3553);
        GL11.glEnable(2848);
        GL11.glBlendFunc(770, 771);

        GL11.glLineWidth(((Number) lineWidth.getValue()).floatValue());
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);

        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder builder = tessellator.getBuffer();

        builder.begin(3, DefaultVertexFormats.POSITION_COLOR);
        buildBuffer(builder, trail, new Color(color.getValue().getRGB()), secondColor.booleanValue ? fadeColor : new Color(color.getValue().getRGB()));
        tessellator.draw();

        GlStateManager.depthMask(true);
        GlStateManager.enableLighting();
        GlStateManager.enableDepth();
        GlStateManager.enableAlpha();
        GlStateManager.popMatrix();
        GL11.glEnable(3553);
        GL11.glPolygonMode(1032, 6914);
    }

    public void buildBuffer(BufferBuilder builder, BreadCrumbs.ItemTrail trail, Color start, Color end) {

        for (Object o : trail.positions) {
            Position p = (Position) o;
            Vec3d pos = updateToCamera(p.pos);
            double value = MathUtil.normalize(trail.positions.indexOf(p), 0.0D, trail.positions.size());
            addBuilderVertex(builder, pos.x, pos.y, pos.z, ColorUtil.interpolate((float) value, start, end));
        }

    }

    boolean isValid(Entity e) {
        return e instanceof EntityEnderPearl || e instanceof EntityExpBottle && xp.getValue() || e instanceof EntityArrow && arrow.getValue() && e.ticksExisted <= timeExisted.getValue();
    }

    public static class Position {
        public Vec3d pos;
        public long time;

        public Position(Vec3d pos) {
            this.pos = pos;
            time = System.currentTimeMillis();
        }

        public boolean equals(Object o) {
            if (this == o) {
                return true;
            } else if (o != null && getClass() == o.getClass()) {
                BreadCrumbs.Position position = (BreadCrumbs.Position)o;
                return time == position.time && Objects.equals(pos, position.pos);
            } else {
                return false;
            }
        }

        public int hashCode() {
            return Objects.hash(pos, time);
        }
    }

    public class ItemTrail {
        public Entity entity;
        public List positions;
        public Timer timer;

        public ItemTrail(Entity entity) {
            this.entity = entity;
            positions = new ArrayList();
            timer = new Timer();
            timer.setDelay(timeExisted.getValue());
            timer.setPaused(true);
        }
    }

    public class Timer {
        long startTime = System.currentTimeMillis();
        long delay;
        boolean paused;

        public boolean isPassed() {
            return !paused && System.currentTimeMillis() - startTime >= delay;
        }

        public long getTime() {
            return System.currentTimeMillis() - startTime;
        }

        public void resetDelay() {
            startTime = System.currentTimeMillis();
        }

        public void setDelay(long delay) {
            this.delay = delay;
        }

        public void setPaused(boolean paused) {
            this.paused = paused;
        }

        public boolean isPaused() {
            return paused;
        }

        public long getStartTime() {
            return startTime;
        }
    }

    public static Vec3d updateToCamera(Vec3d vec) {
        return new Vec3d(vec.x - mc.getRenderManager().viewerPosX, vec.y - mc.getRenderManager().viewerPosY, vec.z - mc.getRenderManager().viewerPosZ);
    }

    public static void addBuilderVertex(BufferBuilder bufferBuilder, double x, double y, double z, Color color) {
        bufferBuilder.pos(x, y, z).color((float)color.getRed() / 255.0F, (float)color.getGreen() / 255.0F, (float)color.getBlue() / 255.0F, (float)color.getAlpha() / 255.0F).endVertex();
    }
}
