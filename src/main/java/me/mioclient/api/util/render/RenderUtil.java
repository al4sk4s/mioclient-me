package me.mioclient.api.util.render;

import me.mioclient.api.events.impl.Render3DEvent;
import me.mioclient.api.managers.Managers;
import me.mioclient.api.util.Wrapper;
import me.mioclient.api.util.interact.BlockUtil;
import me.mioclient.api.util.math.InterpolationUtil;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.culling.ICamera;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.Entity;
import net.minecraft.entity.passive.EntityOcelot;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.Display;
import org.lwjgl.util.glu.GLU;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.Arrays;
import java.util.Objects;

import static org.lwjgl.opengl.GL11.*;

public class RenderUtil implements Wrapper {

    public static RenderItem itemRender = mc.getRenderItem();
    public static ICamera camera = new Frustum();

    private static final FloatBuffer screenCoords = BufferUtils.createFloatBuffer(3);
    private static final IntBuffer viewport = BufferUtils.createIntBuffer(16);
    private static final FloatBuffer modelView = BufferUtils.createFloatBuffer(16);
    private static final FloatBuffer projection = BufferUtils.createFloatBuffer(16);

    //3D Stuff

    public static void drawBoxESP(BlockPos pos, Color color, boolean secondC, Color secondColor, float lineWidth, boolean outline, boolean box, int boxAlpha, boolean air, double height, boolean gradientBox, boolean gradientOutline, boolean invertGradientBox, boolean invertGradientOutline, int gradientAlpha) {
        if (box) {
            drawBox(pos, new Color(color.getRed(), color.getGreen(), color.getBlue(), boxAlpha), height, gradientBox, invertGradientBox, gradientAlpha);
        }
        if (outline) {
            drawBlockOutline(pos, secondC ? secondColor : color, lineWidth, air, height, gradientOutline, invertGradientOutline, gradientAlpha, false);
        }
    }

    public static void drawBoxESP(BlockPos pos, Color color, boolean secondC, Color secondColor, float lineWidth, boolean outline, boolean box, int boxAlpha, boolean air, double height) {
        if (box) {
            drawBox(pos, new Color(color.getRed(), color.getGreen(), color.getBlue(), boxAlpha), height, false, false, 0);
        }
        if (outline) {
            drawBlockOutline(pos, secondC ? secondColor : color, lineWidth, air, height, false, false, 0, false);
        }
    }

    public static void drawBox(BlockPos pos, Color color, double height, boolean fade, boolean invertFade, int alpha) {
        if (fade) {
            Color endColor = new Color(color.getRed(), color.getGreen(), color.getBlue(), alpha);
            drawFadingBox(pos, invertFade ? endColor : color, invertFade ? color : endColor, height);
            return;
        }

        AxisAlignedBB bb = new AxisAlignedBB(pos.getX() - mc.getRenderManager().viewerPosX, pos.getY() - mc.getRenderManager().viewerPosY, pos.getZ() - mc.getRenderManager().viewerPosZ, pos.getX() + 1 - mc.getRenderManager().viewerPosX, pos.getY() + 1 - mc.getRenderManager().viewerPosY + height, pos.getZ() + 1 - mc.getRenderManager().viewerPosZ);
        camera.setPosition(Objects.requireNonNull(mc.getRenderViewEntity()).posX, mc.getRenderViewEntity().posY, mc.getRenderViewEntity().posZ);

        if (camera.isBoundingBoxInFrustum(new AxisAlignedBB(bb.minX + mc.getRenderManager().viewerPosX, bb.minY + mc.getRenderManager().viewerPosY, bb.minZ + mc.getRenderManager().viewerPosZ, bb.maxX + mc.getRenderManager().viewerPosX, bb.maxY + mc.getRenderManager().viewerPosY, bb.maxZ + mc.getRenderManager().viewerPosZ))) {
            GlStateManager.pushMatrix();
            GlStateManager.enableBlend();
            GlStateManager.disableDepth();
            GlStateManager.tryBlendFuncSeparate(770, 771, 0, 1);
            GlStateManager.disableTexture2D();
            GlStateManager.depthMask(false);
            glEnable(2848);
            glHint(3154, 4354);
            RenderGlobal.renderFilledBox(bb, color.getRed() / 255.0f, color.getGreen() / 255.0f, color.getBlue() / 255.0f, color.getAlpha() / 255.0f);
            glDisable(2848);
            GlStateManager.depthMask(true);
            GlStateManager.enableDepth();
            GlStateManager.enableTexture2D();
            GlStateManager.disableBlend();
            GlStateManager.popMatrix();
        }
    }

    public static void drawFadingBox(BlockPos pos, Color startColor, Color endColor, double height) {
        for (EnumFacing face : EnumFacing.values()) {
            if (face != EnumFacing.UP) {
                drawFadingSide(pos, face, startColor, endColor, height);
            }
        }
    }

    public static void drawFadingSide(BlockPos pos, EnumFacing face, Color startColor, Color endColor, double height) {
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder builder = tessellator.getBuffer();
        IBlockState state = BlockUtil.getState(pos);

        Vec3d interp = InterpolationUtil.getInterpolatedPos(mc.player, mc.getRenderPartialTicks(), false);
        AxisAlignedBB bb = state.getSelectedBoundingBox(mc.world, pos).grow(0.0020000000949949026).offset(-interp.x, -interp.y, -interp.z).expand(0.0, 0.0, 0.0);
        float red = startColor.getRed() / 255.0f;
        float green = startColor.getGreen() / 255.0f;
        float blue = startColor.getBlue() / 255.0f;
        float alpha = startColor.getAlpha() / 255.0f;
        float red2 = endColor.getRed() / 255.0f;
        float green2 = endColor.getGreen() / 255.0f;
        float blue2 = endColor.getBlue() / 255.0f;
        float alpha2 = endColor.getAlpha() / 255.0f;
        double x1 = 0.0;
        double y1 = 0.0;
        double z1 = 0.0;
        double x2 = 0.0;
        double y2 = 0.0;
        double z2 = 0.0;

        if (face == EnumFacing.DOWN) {
            x1 = bb.minX;
            x2 = bb.maxX;
            y1 = bb.minY;
            y2 = bb.minY;
            z1 = bb.minZ;
            z2 = bb.maxZ;

        } else if (face == EnumFacing.UP) {
            x1 = bb.minX;
            x2 = bb.maxX;
            y1 = bb.maxY + height;
            y2 = bb.maxY + height;
            z1 = bb.minZ;
            z2 = bb.maxZ;

        } else if (face == EnumFacing.EAST) {
            x1 = bb.maxX;
            x2 = bb.maxX;
            y1 = bb.minY;
            y2 = bb.maxY + height;
            z1 = bb.minZ;
            z2 = bb.maxZ;

        } else if (face == EnumFacing.WEST) {
            x1 = bb.minX;
            x2 = bb.minX;
            y1 = bb.minY;
            y2 = bb.maxY + height;
            z1 = bb.minZ;
            z2 = bb.maxZ;

        } else if (face == EnumFacing.SOUTH) {
            x1 = bb.minX;
            x2 = bb.maxX;
            y1 = bb.minY;
            y2 = bb.maxY + height;
            z1 = bb.maxZ;
            z2 = bb.maxZ;

        } else if (face == EnumFacing.NORTH) {
            x1 = bb.minX;
            x2 = bb.maxX;
            y1 = bb.minY;
            y2 = bb.maxY + height;
            z1 = bb.minZ;
            z2 = bb.minZ;
        }
        GlStateManager.pushMatrix();
        GlStateManager.disableDepth();
        GlStateManager.disableTexture2D();
        GlStateManager.enableBlend();
        GlStateManager.disableAlpha();
        GlStateManager.depthMask(false);
        builder.begin(5, DefaultVertexFormats.POSITION_COLOR);

        if (face == EnumFacing.EAST || face == EnumFacing.WEST || face == EnumFacing.NORTH || face == EnumFacing.SOUTH) {
            builder.pos(x1, y1, z1).color(red, green, blue, alpha).endVertex();
            builder.pos(x1, y1, z1).color(red, green, blue, alpha).endVertex();
            builder.pos(x1, y1, z1).color(red, green, blue, alpha).endVertex();
            builder.pos(x1, y1, z2).color(red, green, blue, alpha).endVertex();
            builder.pos(x1, y2, z1).color(red2, green2, blue2, alpha2).endVertex();
            builder.pos(x1, y2, z2).color(red2, green2, blue2, alpha2).endVertex();
            builder.pos(x1, y2, z2).color(red2, green2, blue2, alpha2).endVertex();
            builder.pos(x1, y1, z2).color(red, green, blue, alpha).endVertex();
            builder.pos(x2, y2, z2).color(red2, green2, blue2, alpha2).endVertex();
            builder.pos(x2, y1, z2).color(red, green, blue, alpha).endVertex();
            builder.pos(x2, y1, z2).color(red, green, blue, alpha).endVertex();
            builder.pos(x2, y1, z1).color(red, green, blue, alpha).endVertex();
            builder.pos(x2, y2, z2).color(red2, green2, blue2, alpha2).endVertex();
            builder.pos(x2, y2, z1).color(red2, green2, blue2, alpha2).endVertex();
            builder.pos(x2, y2, z1).color(red2, green2, blue2, alpha2).endVertex();
            builder.pos(x2, y1, z1).color(red, green, blue, alpha).endVertex();
            builder.pos(x1, y2, z1).color(red2, green2, blue2, alpha2).endVertex();
            builder.pos(x1, y1, z1).color(red, green, blue, alpha).endVertex();
            builder.pos(x1, y1, z1).color(red, green, blue, alpha).endVertex();
            builder.pos(x2, y1, z1).color(red, green, blue, alpha).endVertex();
            builder.pos(x1, y1, z2).color(red, green, blue, alpha).endVertex();
            builder.pos(x2, y1, z2).color(red, green, blue, alpha).endVertex();
            builder.pos(x2, y1, z2).color(red, green, blue, alpha).endVertex();
            builder.pos(x1, y2, z1).color(red2, green2, blue2, alpha2).endVertex();
            builder.pos(x1, y2, z1).color(red2, green2, blue2, alpha2).endVertex();
            builder.pos(x1, y2, z2).color(red2, green2, blue2, alpha2).endVertex();
            builder.pos(x2, y2, z1).color(red2, green2, blue2, alpha2).endVertex();
            builder.pos(x2, y2, z2).color(red2, green2, blue2, alpha2).endVertex();
            builder.pos(x2, y2, z2).color(red2, green2, blue2, alpha2).endVertex();
            builder.pos(x2, y2, z2).color(red2, green2, blue2, alpha2).endVertex();
        }
        else if (face == EnumFacing.UP) {
            builder.pos(x1, y1, z1).color(red2, green2, blue2, alpha2).endVertex();
            builder.pos(x1, y1, z1).color(red2, green2, blue2, alpha2).endVertex();
            builder.pos(x1, y1, z1).color(red2, green2, blue2, alpha2).endVertex();
            builder.pos(x1, y1, z2).color(red2, green2, blue2, alpha2).endVertex();
            builder.pos(x1, y2, z1).color(red2, green2, blue2, alpha2).endVertex();
            builder.pos(x1, y2, z2).color(red2, green2, blue2, alpha2).endVertex();
            builder.pos(x1, y2, z2).color(red2, green2, blue2, alpha2).endVertex();
            builder.pos(x1, y1, z2).color(red2, green2, blue2, alpha2).endVertex();
            builder.pos(x2, y2, z2).color(red2, green2, blue2, alpha2).endVertex();
            builder.pos(x2, y1, z2).color(red2, green2, blue2, alpha2).endVertex();
            builder.pos(x2, y1, z2).color(red2, green2, blue2, alpha2).endVertex();
            builder.pos(x2, y1, z1).color(red2, green2, blue2, alpha2).endVertex();
            builder.pos(x2, y2, z2).color(red2, green2, blue2, alpha2).endVertex();
            builder.pos(x2, y2, z1).color(red2, green2, blue2, alpha2).endVertex();
            builder.pos(x2, y2, z1).color(red2, green2, blue2, alpha2).endVertex();
            builder.pos(x2, y1, z1).color(red2, green2, blue2, alpha2).endVertex();
            builder.pos(x1, y2, z1).color(red2, green2, blue2, alpha2).endVertex();
            builder.pos(x1, y1, z1).color(red2, green2, blue2, alpha2).endVertex();
            builder.pos(x1, y1, z1).color(red2, green2, blue2, alpha2).endVertex();
            builder.pos(x2, y1, z1).color(red2, green2, blue2, alpha2).endVertex();
            builder.pos(x1, y1, z2).color(red2, green2, blue2, alpha2).endVertex();
            builder.pos(x2, y1, z2).color(red2, green2, blue2, alpha2).endVertex();
            builder.pos(x2, y1, z2).color(red2, green2, blue2, alpha2).endVertex();
            builder.pos(x1, y2, z1).color(red2, green2, blue2, alpha2).endVertex();
            builder.pos(x1, y2, z1).color(red2, green2, blue2, alpha2).endVertex();
            builder.pos(x1, y2, z2).color(red2, green2, blue2, alpha2).endVertex();
            builder.pos(x2, y2, z1).color(red2, green2, blue2, alpha2).endVertex();
            builder.pos(x2, y2, z2).color(red2, green2, blue2, alpha2).endVertex();
            builder.pos(x2, y2, z2).color(red2, green2, blue2, alpha2).endVertex();
            builder.pos(x2, y2, z2).color(red2, green2, blue2, alpha2).endVertex();
        }
        else if (face == EnumFacing.DOWN) {
            builder.pos(x1, y1, z1).color(red, green, blue, alpha).endVertex();
            builder.pos(x1, y1, z1).color(red, green, blue, alpha).endVertex();
            builder.pos(x1, y1, z1).color(red, green, blue, alpha).endVertex();
            builder.pos(x1, y1, z2).color(red, green, blue, alpha).endVertex();
            builder.pos(x1, y2, z1).color(red, green, blue, alpha).endVertex();
            builder.pos(x1, y2, z2).color(red, green, blue, alpha).endVertex();
            builder.pos(x1, y2, z2).color(red, green, blue, alpha).endVertex();
            builder.pos(x1, y1, z2).color(red, green, blue, alpha).endVertex();
            builder.pos(x2, y2, z2).color(red, green, blue, alpha).endVertex();
            builder.pos(x2, y1, z2).color(red, green, blue, alpha).endVertex();
            builder.pos(x2, y1, z2).color(red, green, blue, alpha).endVertex();
            builder.pos(x2, y1, z1).color(red, green, blue, alpha).endVertex();
            builder.pos(x2, y2, z2).color(red, green, blue, alpha).endVertex();
            builder.pos(x2, y2, z1).color(red, green, blue, alpha).endVertex();
            builder.pos(x2, y2, z1).color(red, green, blue, alpha).endVertex();
            builder.pos(x2, y1, z1).color(red, green, blue, alpha).endVertex();
            builder.pos(x1, y2, z1).color(red, green, blue, alpha).endVertex();
            builder.pos(x1, y1, z1).color(red, green, blue, alpha).endVertex();
            builder.pos(x1, y1, z1).color(red, green, blue, alpha).endVertex();
            builder.pos(x2, y1, z1).color(red, green, blue, alpha).endVertex();
            builder.pos(x1, y1, z2).color(red, green, blue, alpha).endVertex();
            builder.pos(x2, y1, z2).color(red, green, blue, alpha).endVertex();
            builder.pos(x2, y1, z2).color(red, green, blue, alpha).endVertex();
            builder.pos(x1, y2, z1).color(red, green, blue, alpha).endVertex();
            builder.pos(x1, y2, z1).color(red, green, blue, alpha).endVertex();
            builder.pos(x1, y2, z2).color(red, green, blue, alpha).endVertex();
            builder.pos(x2, y2, z1).color(red, green, blue, alpha).endVertex();
            builder.pos(x2, y2, z2).color(red, green, blue, alpha).endVertex();
            builder.pos(x2, y2, z2).color(red, green, blue, alpha).endVertex();
            builder.pos(x2, y2, z2).color(red, green, blue, alpha).endVertex();
        }
        tessellator.draw();
        GlStateManager.depthMask(true);
        GlStateManager.disableBlend();
        GlStateManager.enableAlpha();
        GlStateManager.enableTexture2D();
        GlStateManager.enableDepth();
        GlStateManager.popMatrix();
    }

    public static void drawBlockOutline(BlockPos pos, Color color, float linewidth, boolean air, double height, boolean fade, boolean invertFade, int alpha, boolean depth) {
        if (fade) {
            Color endColor = new Color(color.getRed(), color.getGreen(), color.getBlue(), alpha);
            drawFadingOutline(pos, invertFade ? color : endColor, invertFade ? endColor : color, linewidth, height);

            return;
        }
        IBlockState state = mc.world.getBlockState(pos);

        if ((air || state.getMaterial() != Material.AIR) && mc.world.getWorldBorder().contains(pos)) {
            AxisAlignedBB blockAxis = new AxisAlignedBB(pos.getX() - mc.getRenderManager().viewerPosX, pos.getY() - mc.getRenderManager().viewerPosY, pos.getZ() - mc.getRenderManager().viewerPosZ, pos.getX() + 1 - mc.getRenderManager().viewerPosX, pos.getY() + 1 - mc.getRenderManager().viewerPosY + height, pos.getZ() + 1 - mc.getRenderManager().viewerPosZ);
            drawBlockOutline(blockAxis.grow(0.0020000000949949026), color, linewidth, depth);
        }
    }

    public static void drawBlockOutline(AxisAlignedBB bb, Color color, float linewidth, boolean depth) {
        float red = color.getRed() / 255.0f;
        float green = color.getGreen() / 255.0f;
        float blue = color.getBlue() / 255.0f;
        float alpha = color.getAlpha() / 255.0f;
        GlStateManager.pushMatrix();
        GlStateManager.enableBlend();

        if (depth) {
            GlStateManager.enableDepth();
            GlStateManager.tryBlendFuncSeparate(770, 771, 0, 1);
            GlStateManager.disableTexture2D();
            GlStateManager.depthMask(true);
        } else {
            GlStateManager.disableDepth();
            GlStateManager.tryBlendFuncSeparate(770, 771, 0, 1);
            GlStateManager.disableTexture2D();
            GlStateManager.depthMask(false);
        }

        glEnable(2848);
        glHint(3154, 4354);
        glLineWidth(linewidth);
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder bufferbuilder = tessellator.getBuffer();
        bufferbuilder.begin(3, DefaultVertexFormats.POSITION_COLOR);
        bufferbuilder.pos(bb.minX, bb.minY, bb.minZ).color(red, green, blue, alpha).endVertex();
        bufferbuilder.pos(bb.minX, bb.minY, bb.maxZ).color(red, green, blue, alpha).endVertex();
        bufferbuilder.pos(bb.maxX, bb.minY, bb.maxZ).color(red, green, blue, alpha).endVertex();
        bufferbuilder.pos(bb.maxX, bb.minY, bb.minZ).color(red, green, blue, alpha).endVertex();
        bufferbuilder.pos(bb.minX, bb.minY, bb.minZ).color(red, green, blue, alpha).endVertex();
        bufferbuilder.pos(bb.minX, bb.maxY, bb.minZ).color(red, green, blue, alpha).endVertex();
        bufferbuilder.pos(bb.minX, bb.maxY, bb.maxZ).color(red, green, blue, alpha).endVertex();
        bufferbuilder.pos(bb.minX, bb.minY, bb.maxZ).color(red, green, blue, alpha).endVertex();
        bufferbuilder.pos(bb.maxX, bb.minY, bb.maxZ).color(red, green, blue, alpha).endVertex();
        bufferbuilder.pos(bb.maxX, bb.maxY, bb.maxZ).color(red, green, blue, alpha).endVertex();
        bufferbuilder.pos(bb.minX, bb.maxY, bb.maxZ).color(red, green, blue, alpha).endVertex();
        bufferbuilder.pos(bb.maxX, bb.maxY, bb.maxZ).color(red, green, blue, alpha).endVertex();
        bufferbuilder.pos(bb.maxX, bb.maxY, bb.minZ).color(red, green, blue, alpha).endVertex();
        bufferbuilder.pos(bb.maxX, bb.minY, bb.minZ).color(red, green, blue, alpha).endVertex();
        bufferbuilder.pos(bb.maxX, bb.maxY, bb.minZ).color(red, green, blue, alpha).endVertex();
        bufferbuilder.pos(bb.minX, bb.maxY, bb.minZ).color(red, green, blue, alpha).endVertex();
        tessellator.draw();
        glDisable(2848);
        GlStateManager.depthMask(true);
        GlStateManager.enableDepth();
        GlStateManager.enableTexture2D();
        GlStateManager.disableBlend();
        GlStateManager.popMatrix();
    }

    public static void drawFadingOutline(BlockPos pos, Color startColor, Color endColor, float linewidth, double height) {
        IBlockState iblockstate = mc.world.getBlockState(pos);
        Vec3d interp = InterpolationUtil.getInterpolatedPos(mc.player, mc.getRenderPartialTicks(), false);
        drawFadingOutline(iblockstate.getSelectedBoundingBox(mc.world, pos).grow(0.0020000000949949026).offset(-interp.x, -interp.y, -interp.z).expand(0.0, 0.0, 0.0), startColor, endColor, linewidth, height);
    }

    public static void drawFadingOutline(AxisAlignedBB bb, Color startColor, Color endColor, float linewidth, double height) {
        float red = startColor.getRed() / 255.0f;
        float green = startColor.getGreen() / 255.0f;
        float blue = startColor.getBlue() / 255.0f;
        float alpha = startColor.getAlpha() / 255.0f;
        float red2 = endColor.getRed() / 255.0f;
        float green2 = endColor.getGreen() / 255.0f;
        float blue2 = endColor.getBlue() / 255.0f;
        float alpha2 = endColor.getAlpha() / 255.0f;
        GlStateManager.pushMatrix();
        GlStateManager.enableBlend();
        GlStateManager.disableDepth();
        GlStateManager.tryBlendFuncSeparate(770, 771, 0, 1);
        GlStateManager.disableTexture2D();
        GlStateManager.depthMask(false);
        glEnable(2848);
        glHint(3154, 4354);
        glLineWidth(linewidth);

        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder bufferbuilder = tessellator.getBuffer();

        bufferbuilder.begin(3, DefaultVertexFormats.POSITION_COLOR);

        bufferbuilder.pos(bb.minX, bb.minY, bb.minZ).color(red2, green2, blue2, alpha2).endVertex();
        bufferbuilder.pos(bb.minX, bb.minY, bb.maxZ).color(red2, green2, blue2, alpha2).endVertex();
        bufferbuilder.pos(bb.maxX, bb.minY, bb.maxZ).color(red2, green2, blue2, alpha2).endVertex();
        bufferbuilder.pos(bb.maxX, bb.minY, bb.minZ).color(red2, green2, blue2, alpha2).endVertex();
        bufferbuilder.pos(bb.minX, bb.minY, bb.minZ).color(red2, green2, blue2, alpha2).endVertex();
        bufferbuilder.pos(bb.minX, bb.maxY + height, bb.minZ).color(red, green, blue, alpha).endVertex();
        bufferbuilder.pos(bb.minX, bb.maxY + height, bb.maxZ).color(red, green, blue, alpha).endVertex();
        bufferbuilder.pos(bb.minX, bb.minY, bb.maxZ).color(red2, green2, blue2, alpha2).endVertex();
        bufferbuilder.pos(bb.maxX, bb.minY, bb.maxZ).color(red2, green2, blue2, alpha2).endVertex();
        bufferbuilder.pos(bb.maxX, bb.maxY + height, bb.maxZ).color(red, green, blue, alpha).endVertex();
        bufferbuilder.pos(bb.minX, bb.maxY + height, bb.maxZ).color(red, green, blue, alpha).endVertex();
        bufferbuilder.pos(bb.maxX, bb.maxY + height, bb.maxZ).color(red, green, blue, alpha).endVertex();
        bufferbuilder.pos(bb.maxX, bb.maxY + height, bb.minZ).color(red, green, blue, alpha).endVertex();
        bufferbuilder.pos(bb.maxX, bb.minY, bb.minZ).color(red2, green2, blue2, alpha2).endVertex();
        bufferbuilder.pos(bb.maxX, bb.maxY + height, bb.minZ).color(red, green, blue, alpha).endVertex();
        bufferbuilder.pos(bb.minX, bb.maxY + height, bb.minZ).color(red, green, blue, alpha).endVertex();

        tessellator.draw();
        glDisable(2848);
        GlStateManager.depthMask(true);
        GlStateManager.enableDepth();
        GlStateManager.enableTexture2D();
        GlStateManager.disableBlend();
        GlStateManager.popMatrix();
    }

    public static void drawSelectionBoxESP(BlockPos pos, Color color, boolean secondC, Color secondColor, float lineWidth, boolean outline, boolean box, int boxAlpha, boolean depth) {
        AxisAlignedBB bbPos = mc.world.getBlockState(pos).getSelectedBoundingBox(mc.world, pos).grow(0.002);
        AxisAlignedBB bb = InterpolationUtil.getInterpolatedAxis(bbPos);
        camera.setPosition(Objects.requireNonNull(mc.getRenderViewEntity()).posX, mc.getRenderViewEntity().posY, mc.getRenderViewEntity().posZ);

        if (camera.isBoundingBoxInFrustum(bbPos)) {
            GlStateManager.pushMatrix();
            GlStateManager.enableBlend();

            if (depth) {
                GlStateManager.enableDepth();
                GlStateManager.tryBlendFuncSeparate(770, 771, 0, 1);
                GlStateManager.disableTexture2D();
                GlStateManager.depthMask(true);
            } else {
                GlStateManager.disableDepth();
                GlStateManager.tryBlendFuncSeparate(770, 771, 0, 1);
                GlStateManager.disableTexture2D();
                GlStateManager.depthMask(false);
            }

            glEnable(2848);
            glHint(3154, 4354);
            glLineWidth(lineWidth);

            if (box) {
                RenderGlobal.renderFilledBox(
                        bb,
                        color.getRed() / 255.0f,
                        color.getGreen() / 255.0f,
                        color.getBlue() / 255.0f,
                        boxAlpha / 255.0f);
            }
            if (outline) {
                drawBlockOutline(
                        bb,
                        secondC ? secondColor : color,
                        lineWidth,
                        depth);
            }

            glDisable(2848);
            GlStateManager.depthMask(true);
            GlStateManager.enableDepth();
            GlStateManager.enableTexture2D();
            GlStateManager.disableBlend();
            GlStateManager.popMatrix();
        }
    }

    public static void drawBoxESP(AxisAlignedBB bbPos, Color color, boolean secondC, Color secondColor, float lineWidth, boolean outline, boolean box, int boxAlpha, boolean depth) {

        AxisAlignedBB axisAlignedBB = bbPos
                .offset(-mc.getRenderManager().viewerPosX, -mc.getRenderManager().viewerPosY, -mc.getRenderManager().viewerPosZ);

        camera.setPosition(Objects.requireNonNull(mc.getRenderViewEntity()).posX, mc.getRenderViewEntity().posY, mc.getRenderViewEntity().posZ);

        if (camera.isBoundingBoxInFrustum(bbPos)) {
            GlStateManager.pushMatrix();
            GlStateManager.enableBlend();

            if (depth) {
                GlStateManager.enableDepth();
                GlStateManager.tryBlendFuncSeparate(770, 771, 0, 1);
                GlStateManager.disableTexture2D();
                GlStateManager.depthMask(true);
            } else {
                GlStateManager.disableDepth();
                GlStateManager.tryBlendFuncSeparate(770, 771, 0, 1);
                GlStateManager.disableTexture2D();
                GlStateManager.depthMask(false);
            }

            glEnable(2848);
            glHint(3154, 4354);
            glLineWidth(lineWidth);

            if (box) {
                RenderGlobal.renderFilledBox(
                        axisAlignedBB,
                        color.getRed() / 255.0f,
                        color.getGreen() / 255.0f,
                        color.getBlue() / 255.0f,
                        boxAlpha / 255.0f);
            }

            if (outline) {
                drawBlockOutline(
                        axisAlignedBB,
                        secondC ? secondColor : color,
                        lineWidth,
                        depth);
            }

            glDisable(2848);
            GlStateManager.depthMask(true);
            GlStateManager.enableDepth();
            GlStateManager.enableTexture2D();
            GlStateManager.disableBlend();
            GlStateManager.popMatrix();
        }
    }

    public static void drawBlockWireframe(BlockPos pos, Color color, float lineWidth, double height, boolean onlyBottom) {
        GlStateManager.pushMatrix();
        GlStateManager.enableBlend();
        GlStateManager.disableDepth();
        GlStateManager.tryBlendFuncSeparate(770, 771, 0, 1);
        GlStateManager.disableTexture2D();
        GlStateManager.depthMask(false);
        glEnable(2848);
        glHint(3154, 4354);
        glLineWidth(lineWidth);

        double x = (double) pos.getX() - mc.getRenderManager().viewerPosX;
        double y = (double) pos.getY() - mc.getRenderManager().viewerPosY;
        double z = (double) pos.getZ() - mc.getRenderManager().viewerPosZ;

        int red = color.getRed();
        int green = color.getGreen();
        int blue = color.getBlue();
        int alpha = color.getAlpha();

        AxisAlignedBB bb = new AxisAlignedBB(x, y, z, x + 1.0, y + 1 + height, z + 1.0);
        net.minecraft.client.renderer.Tessellator tessellator = net.minecraft.client.renderer.Tessellator.getInstance();
        BufferBuilder bufferbuilder = tessellator.getBuffer();
        bufferbuilder.begin(1, DefaultVertexFormats.POSITION_COLOR);

        //bottom
        bufferbuilder.pos(bb.minX, bb.minY, bb.minZ).color(red, green, blue, alpha).endVertex();
        bufferbuilder.pos(bb.maxX, bb.minY, bb.maxZ).color(red, green, blue, alpha).endVertex();
        bufferbuilder.pos(bb.minX, bb.minY, bb.maxZ).color(red, green, blue, alpha).endVertex();
        bufferbuilder.pos(bb.maxX, bb.minY, bb.minZ).color(red, green, blue, alpha).endVertex();

        if (!onlyBottom) {

            //top
            bufferbuilder.pos(bb.minX, bb.maxY, bb.minZ).color(red, green, blue, alpha).endVertex();
            bufferbuilder.pos(bb.maxX, bb.maxY, bb.maxZ).color(red, green, blue, alpha).endVertex();
            bufferbuilder.pos(bb.minX, bb.maxY, bb.maxZ).color(red, green, blue, alpha).endVertex();
            bufferbuilder.pos(bb.maxX, bb.maxY, bb.minZ).color(red, green, blue, alpha).endVertex();

            //sides
            bufferbuilder.pos(bb.minX, bb.minY, bb.minZ).color(red, green, blue, alpha).endVertex();
            bufferbuilder.pos(bb.minX, bb.maxY, bb.maxZ).color(red, green, blue, alpha).endVertex();
            bufferbuilder.pos(bb.minX, bb.minY, bb.maxZ).color(red, green, blue, alpha).endVertex();
            bufferbuilder.pos(bb.minX, bb.maxY, bb.minZ).color(red, green, blue, alpha).endVertex();

            bufferbuilder.pos(bb.maxX, bb.minY, bb.minZ).color(red, green, blue, alpha).endVertex();
            bufferbuilder.pos(bb.maxX, bb.maxY, bb.maxZ).color(red, green, blue, alpha).endVertex();
            bufferbuilder.pos(bb.maxX, bb.minY, bb.maxZ).color(red, green, blue, alpha).endVertex();
            bufferbuilder.pos(bb.maxX, bb.maxY, bb.minZ).color(red, green, blue, alpha).endVertex();

            bufferbuilder.pos(bb.minX, bb.minY, bb.minZ).color(red, green, blue, alpha).endVertex();
            bufferbuilder.pos(bb.maxX, bb.maxY, bb.minZ).color(red, green, blue, alpha).endVertex();
            bufferbuilder.pos(bb.maxX, bb.minY, bb.minZ).color(red, green, blue, alpha).endVertex();
            bufferbuilder.pos(bb.minX, bb.maxY, bb.minZ).color(red, green, blue, alpha).endVertex();

            bufferbuilder.pos(bb.minX, bb.minY, bb.maxZ).color(red, green, blue, alpha).endVertex();
            bufferbuilder.pos(bb.maxX, bb.maxY, bb.maxZ).color(red, green, blue, alpha).endVertex();
            bufferbuilder.pos(bb.maxX, bb.minY, bb.maxZ).color(red, green, blue, alpha).endVertex();
            bufferbuilder.pos(bb.minX, bb.maxY, bb.maxZ).color(red, green, blue, alpha).endVertex();
        }

        tessellator.draw();

        glDisable(2848);
        GlStateManager.depthMask(true);
        GlStateManager.enableDepth();
        GlStateManager.enableTexture2D();
        GlStateManager.disableBlend();
        GlStateManager.popMatrix();
    }

    public static void drawEntityBoxESP(Entity entity, Color color, boolean secondC, Color secondColor, float lineWidth, boolean outline, boolean box, int boxAlpha) {
        Vec3d interp = InterpolationUtil.getInterpolatedPos(entity, mc.getRenderPartialTicks(), true);

        AxisAlignedBB bb = new AxisAlignedBB(
                entity.getEntityBoundingBox().minX - 0.05 - entity.posX + interp.x,
                entity.getEntityBoundingBox().minY - 0.0 - entity.posY + interp.y,
                entity.getEntityBoundingBox().minZ - 0.05 - entity.posZ + interp.z,
                entity.getEntityBoundingBox().maxX + 0.05 - entity.posX + interp.x,
                entity.getEntityBoundingBox().maxY + 0.1 - entity.posY + interp.y,
                entity.getEntityBoundingBox().maxZ + 0.05 - entity.posZ + interp.z);

        GlStateManager.pushMatrix();
        GlStateManager.enableBlend();
        GlStateManager.disableDepth();
        GlStateManager.tryBlendFuncSeparate(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA, 0, 1);
        GlStateManager.disableTexture2D();
        GlStateManager.depthMask(false);

        glEnable(GL_LINE_SMOOTH);
        glHint(GL_LINE_SMOOTH_HINT, GL_NICEST);

        glLineWidth(lineWidth);

        if (entity instanceof EntityPlayer && Managers.FRIENDS.isFriend(entity.getName())) {
            color = Managers.COLORS.getFriendColor(color.getAlpha());
        }

        if (box) {
            RenderGlobal.renderFilledBox(
                    bb,
                    color.getRed() / 255.0f,
                    color.getGreen() / 255.0f,
                    color.getBlue() / 255.0f,
                    boxAlpha / 255.0f);
        }

        glDisable(GL_LINE_SMOOTH);

        GlStateManager.depthMask(true);
        GlStateManager.enableDepth();
        GlStateManager.enableTexture2D();
        GlStateManager.disableBlend();
        GlStateManager.popMatrix();

        if (outline) {
            drawBlockOutline(
                    bb,
                    secondC ? secondColor : color,
                    lineWidth,
                    false);
        }
    }

    //2D Stuff

    public static void drawLine(float x, float y, float x1, float y1, float thickness, int hex) {
        float red = (hex >> 16 & 0xFF) / 255.0f;
        float green = (hex >> 8 & 0xFF) / 255.0f;
        float blue = (hex & 0xFF) / 255.0f;
        float alpha = (hex >> 24 & 0xFF) / 255.0f;
        GlStateManager.pushMatrix();
        GlStateManager.disableTexture2D();
        GlStateManager.enableBlend();
        GlStateManager.disableAlpha();
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
        GlStateManager.shadeModel(7425);
        glLineWidth(thickness);
        glEnable(2848);
        glHint(3154, 4354);
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder bufferbuilder = tessellator.getBuffer();
        bufferbuilder.begin(3, DefaultVertexFormats.POSITION_COLOR);
        bufferbuilder.pos(x, y, 0.0).color(red, green, blue, alpha).endVertex();
        bufferbuilder.pos(x1, y1, 0.0).color(red, green, blue, alpha).endVertex();
        tessellator.draw();
        GlStateManager.shadeModel(7424);
        glDisable(2848);
        GlStateManager.disableBlend();
        GlStateManager.enableAlpha();
        GlStateManager.enableTexture2D();
        GlStateManager.popMatrix();
    }

    public static void drawGradientLine(float x, float y, float x1, float y1, float thickness, int hex, int secondHex) {
        float red = (hex >> 16 & 0xFF) / 255.0f;
        float green = (hex >> 8 & 0xFF) / 255.0f;
        float blue = (hex & 0xFF) / 255.0f;
        float alpha = (hex >> 24 & 0xFF) / 255.0f;
        float red2 = (secondHex >> 16 & 0xFF) / 255.0f;
        float green2 = (secondHex >> 8 & 0xFF) / 255.0f;
        float blue2 = (secondHex & 0xFF) / 255.0f;
        float alpha2 = (secondHex >> 24 & 0xFF) / 255.0f;
        GlStateManager.pushMatrix();
        GlStateManager.disableTexture2D();
        GlStateManager.enableBlend();
        GlStateManager.disableAlpha();
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
        GlStateManager.shadeModel(7425);
        glLineWidth(thickness);
        glEnable(2848);
        glHint(3154, 4354);
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder bufferbuilder = tessellator.getBuffer();
        bufferbuilder.begin(3, DefaultVertexFormats.POSITION_COLOR);
        bufferbuilder.pos(x, y, 0.0).color(red, green, blue, alpha).endVertex();
        bufferbuilder.pos(x, y1, 0.0).color(red, green, blue, alpha).endVertex();
        bufferbuilder.pos(x1, y1, 0.0).color(red2, green2, blue2, alpha2).endVertex();
        bufferbuilder.pos(x1, y, 0.0).color(red2, green2, blue2, alpha2).endVertex();
        tessellator.draw();
        GlStateManager.shadeModel(7424);
        glDisable(2848);
        GlStateManager.disableBlend();
        GlStateManager.enableAlpha();
        GlStateManager.enableTexture2D();
        GlStateManager.popMatrix();
    }

    public static void drawRect(float x, float y, float w, float h, int color) {
        float alpha = (color >> 24 & 0xFF) / 255.0f;
        float red = (color >> 16 & 0xFF) / 255.0f;
        float green = (color >> 8 & 0xFF) / 255.0f;
        float blue = (color & 0xFF) / 255.0f;
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder bufferbuilder = tessellator.getBuffer();
        GlStateManager.enableBlend();
        GlStateManager.disableTexture2D();
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
        bufferbuilder.begin(7, DefaultVertexFormats.POSITION_COLOR);
        bufferbuilder.pos(x, h, 0.0).color(red, green, blue, alpha).endVertex();
        bufferbuilder.pos(w, h, 0.0).color(red, green, blue, alpha).endVertex();
        bufferbuilder.pos(w, y, 0.0).color(red, green, blue, alpha).endVertex();
        bufferbuilder.pos(x, y, 0.0).color(red, green, blue, alpha).endVertex();
        tessellator.draw();
        GlStateManager.enableTexture2D();
        GlStateManager.disableBlend();
    }

    public static void drawModalRect(int x, int y, float u, float v, int uWidth, int vHeight, int width, int height, float tileWidth, float tileHeight) {
        Gui.drawScaledCustomSizeModalRect(x, y, u, v, uWidth, vHeight, width, height, tileWidth, tileHeight);
    }

    public static void drawVGradientRect(float left, float top, float right, float bottom, int startColor, int endColor) {
        float f = (float) (startColor >> 24 & 255) / 255.0F;
        float f1 = (float) (startColor >> 16 & 255) / 255.0F;
        float f2 = (float) (startColor >> 8 & 255) / 255.0F;
        float f3 = (float) (startColor & 255) / 255.0F;
        float f4 = (float) (endColor >> 24 & 255) / 255.0F;
        float f5 = (float) (endColor >> 16 & 255) / 255.0F;
        float f6 = (float) (endColor >> 8 & 255) / 255.0F;
        float f7 = (float) (endColor & 255) / 255.0F;
        GlStateManager.disableTexture2D();
        GlStateManager.enableBlend();
        GlStateManager.disableAlpha();
        GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
        GlStateManager.shadeModel(7425);
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder bufferbuilder = tessellator.getBuffer();
        bufferbuilder.begin(7, DefaultVertexFormats.POSITION_COLOR);
        bufferbuilder.pos(right, top, 0).color(f1, f2, f3, f).endVertex();
        bufferbuilder.pos(left, top, 0).color(f1, f2, f3, f).endVertex();
        bufferbuilder.pos(left, bottom, 0).color(f5, f6, f7, f4).endVertex();
        bufferbuilder.pos(right, bottom, 0).color(f5, f6, f7, f4).endVertex();
        tessellator.draw();
        GlStateManager.shadeModel(7424);
        GlStateManager.disableBlend();
        GlStateManager.enableAlpha();
        GlStateManager.enableTexture2D();
    }

    public static void drawHGradientRect(float left, float top, float right, float bottom, int startColor, int endColor) {
        float f = (float) (startColor >> 24 & 255) / 255.0F;
        float f1 = (float) (startColor >> 16 & 255) / 255.0F;
        float f2 = (float) (startColor >> 8 & 255) / 255.0F;
        float f3 = (float) (startColor & 255) / 255.0F;
        float f4 = (float) (endColor >> 24 & 255) / 255.0F;
        float f5 = (float) (endColor >> 16 & 255) / 255.0F;
        float f6 = (float) (endColor >> 8 & 255) / 255.0F;
        float f7 = (float) (endColor & 255) / 255.0F;
        GlStateManager.disableTexture2D();
        GlStateManager.enableBlend();
        GlStateManager.disableAlpha();
        GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
        GlStateManager.shadeModel(7425);
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder bufferbuilder = tessellator.getBuffer();
        bufferbuilder.begin(7, DefaultVertexFormats.POSITION_COLOR);
        bufferbuilder.pos(left, top, 0).color(f1, f2, f3, f4).endVertex();
        bufferbuilder.pos(left, bottom, 0).color(f1, f2, f3, f4).endVertex();
        bufferbuilder.pos(right, bottom, 0).color(f5, f6, f7, f4).endVertex();
        bufferbuilder.pos(right, top, 0).color(f5, f6, f7, f4).endVertex();
        tessellator.draw();
        GlStateManager.shadeModel(7424);
        GlStateManager.disableBlend();
        GlStateManager.enableAlpha();
        GlStateManager.enableTexture2D();
    }

    public static void drawGlow(double x, double y, double x1, double y1, int color) {
        GlStateManager.disableTexture2D();
        GlStateManager.enableBlend();
        GlStateManager.disableAlpha();
        GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
        GlStateManager.shadeModel(7425);
        drawVGradientRect((int) x, (int) y, (int) x1, (int) (y + (y1 - y) / 2f), ColorUtil.toRGBA(new Color(color).getRed(), new Color(color).getGreen(), new Color(color).getBlue(), 0), color);
        drawVGradientRect((int) x, (int) (y + (y1 - y) / 2f), (int) x1, (int) y1, color, ColorUtil.toRGBA(new Color(color).getRed(), new Color(color).getGreen(), new Color(color).getBlue(), 0));
        int radius = (int) ((y1 - y) / 2f);
        drawPolygonPart(x, (y + (y1 - y) / 2f), radius, 0, color, ColorUtil.toRGBA(new Color(color).getRed(), new Color(color).getGreen(), new Color(color).getBlue(), 0));
        drawPolygonPart(x, (y + (y1 - y) / 2f), radius, 1, color, ColorUtil.toRGBA(new Color(color).getRed(), new Color(color).getGreen(), new Color(color).getBlue(), 0));
        drawPolygonPart(x1, (y + (y1 - y) / 2f), radius, 2, color, ColorUtil.toRGBA(new Color(color).getRed(), new Color(color).getGreen(), new Color(color).getBlue(), 0));
        drawPolygonPart(x1, (y + (y1 - y) / 2f), radius, 3, color, ColorUtil.toRGBA(new Color(color).getRed(), new Color(color).getGreen(), new Color(color).getBlue(), 0));
        GlStateManager.shadeModel(7424);
        GlStateManager.disableBlend();
        GlStateManager.enableAlpha();
        GlStateManager.enableTexture2D();
    }

    public static void drawPolygonPart(double x, double y, int radius, int part, int color, int endcolor) {
        float alpha = (float) (color >> 24 & 255) / 255.0F;
        float red = (float) (color >> 16 & 255) / 255.0F;
        float green = (float) (color >> 8 & 255) / 255.0F;
        float blue = (float) (color & 255) / 255.0F;
        float alpha1 = (float) (endcolor >> 24 & 255) / 255.0F;
        float red1 = (float) (endcolor >> 16 & 255) / 255.0F;
        float green1 = (float) (endcolor >> 8 & 255) / 255.0F;
        float blue1 = (float) (endcolor & 255) / 255.0F;
        GlStateManager.disableTexture2D();
        GlStateManager.enableBlend();
        GlStateManager.disableAlpha();
        GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
        GlStateManager.shadeModel(7425);
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder bufferbuilder = tessellator.getBuffer();
        bufferbuilder.begin(GL_TRIANGLE_FAN, DefaultVertexFormats.POSITION_COLOR);
        bufferbuilder.pos(x, y, 0).color(red, green, blue, alpha).endVertex();
        final double TWICE_PI = Math.PI * 2;
        for (int i = part * 90; i <= part * 90 + 90; i++) {
            double angle = (TWICE_PI * i / 360) + Math.toRadians(180);
            bufferbuilder.pos(x + Math.sin(angle) * radius, y + Math.cos(angle) * radius, 0).color(red1, green1, blue1, alpha1).endVertex();
        }
        tessellator.draw();
        GlStateManager.shadeModel(7424);
        GlStateManager.disableBlend();
        GlStateManager.enableAlpha();
        GlStateManager.enableTexture2D();
    }

    public static void drawCircle(float x, float y, float radius, int color) {
        float alpha = (float) (color >> 24 & 255) / 255.0F;
        float red = (float) (color >> 16 & 255) / 255.0F;
        float green = (float) (color >> 8 & 255) / 255.0F;
        float blue = (float) (color & 255) / 255.0F;
        GlStateManager.pushMatrix();
        GlStateManager.enableBlend();
        GlStateManager.disableTexture2D();
        GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
        glColor4f(red, green, blue, alpha);
        glBegin(GL_POLYGON);
        for (int i = 0; i <= 360; ++i) {
            glVertex2d(x + Math.sin((double) i * 3.141526 / 180.0) * (double) radius, y + Math.cos((double) i * 3.141526 / 180.0) * (double) radius);
        }
        glEnd();
        GlStateManager.resetColor();
        GlStateManager.enableTexture2D();
        GlStateManager.disableBlend();
        GlStateManager.popMatrix();
    }

    public static void drawNameTagOutline(float x, float y, float width, float height, float lineWidth, int color, int secondColor, boolean rainbow) {
        GlStateManager.enableBlend();
        GlStateManager.disableTexture2D();
        GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
        x *= 2;
        width *= 2;
        y *= 2;
        height *= 2;
        glScalef(0.5F, 0.5F, 0.5F);
        drawLine(x, y, x, height, lineWidth, (rainbow) ? ColorUtil.rainbow(5000).getRGB() : color);
        drawLine(width, y, width, height, lineWidth, (rainbow) ? ColorUtil.rainbow(1000).getRGB() : secondColor);
        drawGradientLine(x, y, width, y, lineWidth, (rainbow) ? ColorUtil.rainbow(5000).getRGB() : color, (rainbow) ? ColorUtil.rainbow(1000).getRGB() : secondColor);
        drawGradientLine(x, height, width, height, lineWidth, (rainbow) ? ColorUtil.rainbow(5000).getRGB() : color, (rainbow) ? ColorUtil.rainbow(1000).getRGB() : secondColor);
        glScalef(2.0F, 2.0F, 2.0F);
        GlStateManager.enableTexture2D();
        GlStateManager.disableBlend();
    }

    public static void drawArrowPointer(float x, float y, float size, float widthDiv, float heightDiv, boolean outline, float outlineWidth, int color) {
        boolean blend = glIsEnabled(3042);
        float alpha = (color >> 24 & 0xFF) / 255.0f;

        glEnable(3042);
        glDisable(3553);
        glBlendFunc(770, 771);
        glEnable(2848);
        glPushMatrix();

        glColor(color);

        glBegin(7);
        glVertex2d(x, y);
        glVertex2d(x - size / widthDiv, y + size);
        glVertex2d(x, y + size / heightDiv);
        glVertex2d(x + size / widthDiv, y + size);
        glVertex2d(x, y);
        glEnd();

        if (outline) {
            glLineWidth(outlineWidth);
            glColor4f(0.0f, 0.0f, 0.0f, alpha);
            glBegin(2);
            glVertex2d(x, y);
            glVertex2d(x - size / widthDiv, y + size);
            glVertex2d(x, y + size / heightDiv);
            glVertex2d(x + size / widthDiv, y + size);
            glVertex2d(x, y);
            glEnd();
        }

        glPopMatrix();
        glEnable(3553);

        if (!blend) {
            glDisable(3042);
        }

        glDisable(2848);
    }

    //Calc stuff

    public static ByteBuffer readImageToBuffer(InputStream in) throws IOException {

        BufferedImage bufferedimage = ImageIO.read(in);

        int[] pixelIndex = bufferedimage.getRGB(0, 0, bufferedimage.getWidth(), bufferedimage.getHeight(), null, 0, bufferedimage.getWidth());

        ByteBuffer bytebuffer = ByteBuffer.allocate(4 * pixelIndex.length);

        Arrays.stream(pixelIndex).map(i -> i << 8 | (i >> 24 & 255)).forEach(bytebuffer::putInt);

        bytebuffer.flip();

        return bytebuffer;
    }

    public static Vec3d get2DPos(double x, double y, double z) {
        glGetFloat(2982, modelView);
        glGetFloat(2983, projection);
        glGetInteger(2978, viewport);

        boolean out = GLU.gluProject((float)x, (float)y, (float)z, modelView, projection, viewport, screenCoords);

        if (out) {
            return new Vec3d(screenCoords.get(0), (Display.getHeight() - screenCoords.get(1)), screenCoords.get(2));
        }

        return null;
    }

    public static void glColor(Color color) {
        glColor4f(color.getRed() / 255.0f, color.getGreen() / 255.0f, color.getBlue() / 255.0f, color.getAlpha() / 255.0f);
    }

    public static void glColor(int color) {
        float red = (color >> 16 & 0xFF) / 255.0f;
        float green = (color >> 8 & 0xFF) / 255.0f;
        float blue = (color & 0xFF) / 255.0f;
        float alpha = (color >> 24 & 0xFF) / 255.0f;

        glColor4f(red, green, blue, alpha);
    }
}