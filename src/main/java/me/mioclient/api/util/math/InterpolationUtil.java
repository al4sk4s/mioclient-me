package me.mioclient.api.util.math;

import me.mioclient.api.util.Wrapper;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.Vec3d;

public class InterpolationUtil implements Wrapper {

    public static Vec3d getInterpolatedPos(Entity entity, float partialTicks, boolean wrap) {
        Vec3d amount = new Vec3d(
                (entity.posX - entity.lastTickPosX) * partialTicks,
                (entity.posY - entity.lastTickPosY) * partialTicks,
                (entity.posZ - entity.lastTickPosZ) * partialTicks);

        Vec3d vec = new Vec3d(
                entity.lastTickPosX,
                entity.lastTickPosY,
                entity.lastTickPosZ)
                .add(amount);

        if (wrap) {
            return vec.subtract(
                    mc.getRenderManager().renderPosX,
                    mc.getRenderManager().renderPosY,
                    mc.getRenderManager().renderPosZ);
        }

        return vec;
    }

    public static AxisAlignedBB getInterpolatedAxis(AxisAlignedBB bb) {
        return new AxisAlignedBB(
                bb.minX - mc.getRenderManager().viewerPosX,
                bb.minY - mc.getRenderManager().viewerPosY,
                bb.minZ - mc.getRenderManager().viewerPosZ,
                bb.maxX - mc.getRenderManager().viewerPosX,
                bb.maxY - mc.getRenderManager().viewerPosY,
                bb.maxZ - mc.getRenderManager().viewerPosZ);
    }

    public static Vec3d getInterpolatedRenderPos(Entity entity, float ticks) {
        return interpolateEntity(entity, ticks).subtract(mc.getRenderManager().renderPosX, mc.getRenderManager().renderPosY, mc.getRenderManager().renderPosZ);
    }

    public static Vec3d interpolateEntity(Entity entity, float time) {
        return new Vec3d(entity.lastTickPosX + (entity.posX - entity.lastTickPosX) * time, entity.lastTickPosY + (entity.posY - entity.lastTickPosY) * time, entity.lastTickPosZ + (entity.posZ - entity.lastTickPosZ) * time);
    }

    public static double getInterpolatedDouble(double pre, double current, float partialTicks) {
        return pre + (current - pre) * (double) partialTicks;
    }

    public static float getInterpolatedFloat(float pre, float current, float partialTicks) {
        return pre + (current - pre) * partialTicks;
    }
}
