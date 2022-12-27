package me.mioclient.mod.modules.impl.render;

import me.mioclient.api.events.impl.Render3DEvent;
import me.mioclient.api.managers.Managers;
import me.mioclient.api.util.render.ColorUtil;
import me.mioclient.api.util.render.RenderUtil;
import me.mioclient.mod.modules.Category;
import me.mioclient.mod.modules.Module;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.Entity;
import net.minecraft.item.*;
import net.minecraft.util.EntitySelectors;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import org.lwjgl.util.glu.Cylinder;
import org.lwjgl.util.glu.GLU;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

import static org.lwjgl.opengl.GL11.*;

public class Trajectories extends Module {
    
    public Trajectories() {
        super("Trajectories", "Draws trajectories for projectiles.", Category.RENDER, true);
    }

    @Override
    public void onRender3D(Render3DEvent event) {
        if (mc.player == null || mc.world == null || mc.gameSettings.thirdPersonView == 2) return;
        
        if (!((mc.player.getHeldItemMainhand() != ItemStack.EMPTY 
                && mc.player.getHeldItemMainhand().getItem() instanceof ItemBow) 
                || (mc.player.getHeldItemMainhand() != ItemStack.EMPTY && isThrowable(mc.player.getHeldItemMainhand().getItem())) 
                || (mc.player.getHeldItemOffhand() != ItemStack.EMPTY && isThrowable(mc.player.getHeldItemOffhand().getItem())))) return;
        
        double renderPosX = mc.getRenderManager().renderPosX;
        double renderPosY = mc.getRenderManager().renderPosY;
        double renderPosZ = mc.getRenderManager().renderPosZ;

        Item item = null;

        if (mc.player.getHeldItemMainhand() != ItemStack.EMPTY && (mc.player.getHeldItemMainhand().getItem() instanceof ItemBow || isThrowable(mc.player.getHeldItemMainhand().getItem()))) {
            item = mc.player.getHeldItemMainhand().getItem();

        } else if (mc.player.getHeldItemOffhand() != ItemStack.EMPTY && isThrowable(mc.player.getHeldItemOffhand().getItem())) {
            item = mc.player.getHeldItemOffhand().getItem();
        }

        if (item == null) return;

        glPushAttrib(GL_ALL_ATTRIB_BITS);
        glPushMatrix();
        glDisable(GL_ALPHA_TEST);
        glEnable(GL_BLEND);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
        glDisable(GL_TEXTURE_2D);
        glDisable(GL_DEPTH_TEST);
        glDepthMask(false);
        glEnable(GL_CULL_FACE);
        glEnable(GL_LINE_SMOOTH);
        glHint(GL_LINE_SMOOTH_HINT, GL_FASTEST);
        glDisable(GL_LIGHTING);

        double posX = renderPosX - MathHelper.cos(mc.player.rotationYaw / 180.0f * 3.1415927f) * 0.16f;
        double posY = renderPosY + mc.player.getEyeHeight() - 0.1000000014901161;
        double posZ = renderPosZ - MathHelper.sin(mc.player.rotationYaw / 180.0f * 3.1415927f) * 0.16f;

        float maxDist = getDistance(item);

        double motionX = -MathHelper.sin(mc.player.rotationYaw / 180.0f * 3.1415927f) * MathHelper.cos(mc.player.rotationPitch / 180.0f * 3.1415927f) * maxDist;
        double motionY = -MathHelper.sin((mc.player.rotationPitch - getPitch(item)) / 180.0f * 3.141593f) * maxDist;
        double motionZ = MathHelper.cos(mc.player.rotationYaw / 180.0f * 3.1415927f) * MathHelper.cos(mc.player.rotationPitch / 180.0f * 3.1415927f) * maxDist;

        int var6 = 72000 - mc.player.getItemInUseCount();

        float power = var6 / 20.0f;
        
        power = (power * power + power * 2.0f) / 3.0f;

        if (power > 1.0f) {
            power = 1.0f;
        }

        float distance = MathHelper.sqrt(motionX * motionX + motionY * motionY + motionZ * motionZ);

        motionX /= distance;
        motionY /= distance;
        motionZ /= distance;

        float pow = (item instanceof ItemBow ? (power * 2.0f) : 1.0f) * getVelocity(item);

        motionX *= pow;
        motionY *= pow;
        motionZ *= pow;
        if (!mc.player.onGround)
            motionY += mc.player.motionY;

        RenderUtil.glColor(Managers.COLORS.getCurrent());

        glEnable(GL_LINE_SMOOTH);

        float size = (float) ((item instanceof ItemBow) ? 0.3 : 0.25);

        boolean hasLanded = false;

        Entity landingOnEntity = null;

        RayTraceResult landingPosition = null;

        glBegin(GL_LINE_STRIP);

        while (!hasLanded && posY > 0.0) {

            Vec3d present = new Vec3d(posX, posY, posZ);
            Vec3d future = new Vec3d(posX + motionX, posY + motionY, posZ + motionZ);

            RayTraceResult possibleLandingStrip = mc.world.rayTraceBlocks(present, future, false, true, false);

            if (possibleLandingStrip != null && possibleLandingStrip.typeOfHit != RayTraceResult.Type.MISS) {
                landingPosition = possibleLandingStrip;
                hasLanded = true;
            }
            AxisAlignedBB arrowBox = new AxisAlignedBB(posX - size, posY - size, posZ - size, posX + size, posY + size, posZ + size);
            List<Entity> entities = getEntitiesWithinAABB(arrowBox.offset(motionX, motionY, motionZ).expand(1.0, 1.0, 1.0));

            for (Entity entity : entities) {

                if (entity.canBeCollidedWith() && entity != mc.player) {

                    float var7 = 0.3f;
                    AxisAlignedBB var8 = entity.getEntityBoundingBox().expand(var7, var7, var7);
                    RayTraceResult possibleEntityLanding = var8.calculateIntercept(present, future);

                    if (possibleEntityLanding == null) {
                        continue;
                    }
                    hasLanded = true;
                    landingOnEntity = entity;
                    landingPosition = possibleEntityLanding;
                }
            }

            posX += motionX;
            posY += motionY;
            posZ += motionZ;

            final float motionAdjustment = 0.99f;

            motionX *= motionAdjustment;
            motionY *= motionAdjustment;
            motionZ *= motionAdjustment;
            motionY -= getGravity(item);

            drawTracer(posX - renderPosX, posY - renderPosY, posZ - renderPosZ);
        }

        glEnd();

        if (landingPosition != null && landingPosition.typeOfHit == RayTraceResult.Type.BLOCK) {

            GlStateManager.translate(posX - renderPosX, posY - renderPosY, posZ - renderPosZ);

            int side = landingPosition.sideHit.getIndex();

            if (side == 2) {
                GlStateManager.rotate(90.0f, 1.0f, 0.0f, 0.0f);

            } else if (side == 3) {
                GlStateManager.rotate(90.0f, 1.0f, 0.0f, 0.0f);

            } else if (side == 4) {
                GlStateManager.rotate(90.0f, 0.0f, 0.0f, 1.0f);

            } else if (side == 5) {
                GlStateManager.rotate(90.0f, 0.0f, 0.0f, 1.0f);
            }

            Cylinder c = new Cylinder();

            GlStateManager.rotate(-90.0f, 1.0f, 0.0f, 0.0f);

            c.setDrawStyle(GLU.GLU_LINE);

            c.draw(0.5f, 0.2f, 0.0f, 4, 1);

            c.setDrawStyle(GLU.GLU_FILL);

            RenderUtil.glColor(ColorUtil.injectAlpha(Managers.COLORS.getCurrent(), 100));

            c.draw(0.5f, 0.2f, 0.0f, 4, 1);
        }
        glEnable(GL_LIGHTING);
        glDisable(GL_LINE_SMOOTH);
        glEnable(GL_TEXTURE_2D);
        glEnable(GL_DEPTH_TEST);
        glDisable(GL_BLEND);
        glEnable(GL_ALPHA_TEST);
        glDepthMask(true);
        glCullFace(GL_BACK);
        glPopMatrix();
        glPopAttrib();

        if (landingOnEntity != null) {
            RenderUtil.drawEntityBoxESP(
                    landingOnEntity,
                    Managers.COLORS.getCurrent(),
                    false,
                    new Color(-1),
                    1,
                    false,
                    true,
                    100);
        }
    }

    public void drawTracer(double x, double y, double z) {
        glVertex3d(x, y, z);
    }

    private boolean isThrowable(Item item) {
        return item instanceof ItemEnderPearl
                || item instanceof ItemExpBottle
                || item instanceof ItemSnowball
                || item instanceof ItemEgg
                || item instanceof ItemSplashPotion
                || item instanceof ItemLingeringPotion;
    }

    private float getDistance(Item item) {
        return item instanceof ItemBow ? 1.0f : 0.4f;
    }

    private float getVelocity(Item item) {
        if (item instanceof ItemSplashPotion 
                || item instanceof ItemLingeringPotion) {
            return 0.5f;
        }
        
        if (item instanceof ItemExpBottle) {
            return 0.59f;
        }
        
        return 1.5f;
    }

    private int getPitch(Item item) {
        if (item instanceof ItemSplashPotion 
                || item instanceof ItemLingeringPotion 
                || item instanceof ItemExpBottle) {
            return 20;
        }
        return 0;
    }

    private float getGravity(Item item) {
        if (item instanceof ItemBow 
                || item instanceof ItemSplashPotion 
                || item instanceof ItemLingeringPotion 
                || item instanceof ItemExpBottle) {
            return 0.05f;
        }
        return 0.03f;
    }

    private List<Entity> getEntitiesWithinAABB(AxisAlignedBB bb) {
        ArrayList<Entity> list = new ArrayList<>();
        
        int chunkMinX = MathHelper.floor((bb.minX - 2.0) / 16.0);
        int chunkMaxX = MathHelper.floor((bb.maxX + 2.0) / 16.0);
        int chunkMinZ = MathHelper.floor((bb.minZ - 2.0) / 16.0);
        int chunkMaxZ = MathHelper.floor((bb.maxZ + 2.0) / 16.0);
        
        for (int x = chunkMinX; x <= chunkMaxX; ++x) {
            
            for (int z = chunkMinZ; z <= chunkMaxZ; ++z) {
                
                if (mc.world.getChunkProvider().getLoadedChunk(x, z) != null) {
                    mc.world.getChunk(x, z).getEntitiesWithinAABBForEntity(mc.player, bb, list, EntitySelectors.NOT_SPECTATING);
                }
            }
        }
        return list;
    }
}