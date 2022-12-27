package me.mioclient.mod.modules.impl.render;

import me.mioclient.api.events.impl.Render3DEvent;
import me.mioclient.api.managers.Managers;
import me.mioclient.api.util.interact.BlockUtil;
import me.mioclient.api.util.math.InterpolationUtil;
import me.mioclient.api.util.render.ColorUtil;
import me.mioclient.api.util.render.RenderUtil;
import me.mioclient.mod.modules.Category;
import me.mioclient.mod.modules.Module;
import me.mioclient.mod.modules.settings.Setting;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityEnderPearl;
import net.minecraft.entity.item.EntityExpBottle;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.item.EntityXPOrb;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

import java.awt.*;

import static org.lwjgl.opengl.GL11.GL_TEXTURE_2D;
import static org.lwjgl.opengl.GL11.glEnable;

/**
 * @author t.me/asphyxia1337
 */

public class ESP extends Module {

    private final Setting<Page> page =
            add(new Setting<>("Settings", Page.GLOBAL));

    private final Setting<Items> items =
            add(new Setting<>("Items", Items.BOX, v -> page.getValue() == Page.GLOBAL));
    private final Setting<Boolean> xpOrbs =
            add(new Setting<>("ExpOrbs", false, v -> page.getValue() == Page.GLOBAL));
    private final Setting<Boolean> xp =
            add(new Setting<>("ExpBottles", false, v -> page.getValue() == Page.GLOBAL));
    private final Setting<Boolean> pearls =
            add(new Setting<>("Pearls", true, v -> page.getValue() == Page.GLOBAL));
    private final Setting<Players> players =
            add(new Setting<>("Players", Players.BOX, v -> page.getValue() == Page.GLOBAL));
    private final Setting<Burrow> burrow =
            add(new Setting<>("Burrow", Burrow.PRETTY, v -> page.getValue() == Page.GLOBAL));

    private final Setting<Color> textColor =
            add(new Setting<>("TextColor", new Color(-1), v -> page.getValue() == Page.COLORS).injectBoolean(false));

    private final Setting<Color> color =
            add(new Setting<>("Color", new Color(125, 125, 213, 150), v -> page.getValue() == Page.COLORS));

    private final Setting<Color> lineColor =
            add(new Setting<>("LineColor", new Color(0xA6FFFFFF, true), v -> page.getValue() == Page.COLORS).injectBoolean(false));

    public ESP() {
        super("ESP", "Highlights entities through walls in several modes.", Category.RENDER);
    }

    public enum Page {
        COLORS,
        GLOBAL
    }

    public enum Items {
        BOX,
        TEXT,
        OFF
    }

    public enum Burrow {
        PRETTY,
        TEXT,
        OFF
    }

    public enum Players {
        BOX,
        OFF
    }

    @Override
    public void onRender3D(Render3DEvent event) {

        if (fullNullCheck()) return;

        //Box ESP

        for (Entity entity : mc.world.loadedEntityList) {

            if (entity != mc.player
                && entity instanceof EntityPlayer
                && players.getValue() == Players.BOX
                && !((EntityPlayer) entity).isSpectator()
                || entity instanceof EntityExpBottle
                && xp.getValue()
                || entity instanceof EntityXPOrb
                && xpOrbs.getValue()
                || entity instanceof EntityEnderPearl
                && pearls.getValue()
                || entity instanceof EntityItem
                && items.getValue() == Items.BOX) {

                RenderUtil.drawEntityBoxESP(
                        entity,
                        color.getValue(),
                        lineColor.booleanValue,
                        lineColor.getValue(),
                        1.0f,
                        true,
                        true,
                        color.getValue().getAlpha());
            }
        }

        //Text ESP

        for (Entity entity : mc.world.loadedEntityList) {

            if (entity instanceof EntityItem && items.getValue() == Items.TEXT) {

                ItemStack stack = ((EntityItem) entity).getItem();

                String text = stack.getDisplayName()
                        + ((stack.isStackable() && stack.getCount() >= 2) ? (" x" + stack.getCount()) : "");

                Vec3d vec = InterpolationUtil.getInterpolatedPos(entity, mc.getRenderPartialTicks(), true);

                drawNameTag(text, vec);
            }
        }

        //Burrow ESP

        for (EntityPlayer player : mc.world.playerEntities) {

            BlockPos feetPos = new BlockPos(
                    Math.floor(player.posX),
                    Math.floor(player.posY + 0.2),
                    Math.floor(player.posZ));

            if (!player.isSpectator()
                    && !player.isRiding()
                    && !(player == mc.player)
                    && BlockUtil.getBlock(feetPos) != Blocks.AIR
                    && !BlockUtil.canReplace(feetPos)
                    && !BlockUtil.isStair(BlockUtil.getBlock(feetPos))
                    && !BlockUtil.isSlab(BlockUtil.getBlock(feetPos))
                    && !BlockUtil.isFence(BlockUtil.getBlock(feetPos))
                    && mc.player.getDistanceSq(feetPos) <= 200) {

                if (burrow.getValue() == Burrow.PRETTY) {
                    drawBurrowESP(feetPos);

                } else if (burrow.getValue() == Burrow.TEXT) {
                    drawNameTag(
                            BlockUtil.getBlock(feetPos) == Blocks.WEB ? "Web" : "Burrow",
                            feetPos);
                }
            }
        }
    }

    private void drawBurrowESP(BlockPos pos) {
        GlStateManager.pushMatrix();

        RenderHelper.enableStandardItemLighting();

        GlStateManager.enablePolygonOffset();
        GlStateManager.doPolygonOffset(1.0f, -1500000.0f);

        GlStateManager.disableLighting();
        GlStateManager.disableDepth();

        double x = pos.getX() + 0.5;
        double y = pos.getY() + 0.4;
        double z = pos.getZ() + 0.5;

        int distance = (int) mc.player.getDistance(x, y, z);
        double scale = 0.0018f + 0.002f * distance;

        if (distance <= 8.0) {
            scale = 0.0245;
        }

        GlStateManager.translate(
                x - mc.getRenderManager().renderPosX,
                y - mc.getRenderManager().renderPosY,
                z - mc.getRenderManager().renderPosZ);

        GlStateManager.glNormal3f(0.0f, 1.0f, 0.0f);

        GlStateManager.rotate(
                -mc.getRenderManager().playerViewY,
                0.0f,
                1.0f,
                0.0f);

        GlStateManager.rotate(
                mc.getRenderManager().playerViewX,
                mc.gameSettings.thirdPersonView == 2 ? -1.0f : 1.0f,
                0.0f,
                0.0f);

        GlStateManager.scale(-scale, -scale, -scale);

        RenderUtil.glColor(color.getValue());

        RenderUtil.drawCircle(
                1.5f,
                -5,
                16.0f,
                ColorUtil.injectAlpha(color.getValue().getRGB(), 100));

        GlStateManager.enableAlpha();

        Block block = BlockUtil.getBlock(pos);

        if (block == Blocks.ENDER_CHEST) {
            mc.getTextureManager().bindTexture(new ResourceLocation("textures/mio/constant/ingame/echest.png"));

        } else if (block == Blocks.WEB) {
            mc.getTextureManager().bindTexture(new ResourceLocation("textures/mio/constant/ingame/web.png"));

        } else {
            mc.getTextureManager().bindTexture(new ResourceLocation("textures/mio/constant/ingame/obby.png"));
        }

        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);

        RenderUtil.drawModalRect(-10, -17, 0, 0, 12, 12, 24, 24, 12, 12);

        GlStateManager.disableAlpha();
        GlStateManager.enableDepth();
        GlStateManager.disableBlend();
        GlStateManager.disablePolygonOffset();
        GlStateManager.doPolygonOffset(1.0f, 1500000.0f);
        GlStateManager.popMatrix();
    }

    private void drawNameTag(String text, BlockPos pos) {
        GlStateManager.pushMatrix();

        RenderHelper.enableStandardItemLighting();

        GlStateManager.enablePolygonOffset();
        GlStateManager.doPolygonOffset(1.0f, -1500000.0f);

        GlStateManager.disableLighting();
        GlStateManager.disableDepth();

        glEnable(GL_TEXTURE_2D);

        double x = pos.getX() + 0.5;
        double y = pos.getY() + 0.7;
        double z = pos.getZ() + 0.5;

        float scale = 0.016666668f * (1.85f);

        GlStateManager.translate(
                x - mc.getRenderManager().renderPosX,
                y - mc.getRenderManager().renderPosY,
                z - mc.getRenderManager().renderPosZ);

        GlStateManager.glNormal3f(0.0f, 1.0f, 0.0f);

        GlStateManager.rotate(
                -mc.player.rotationYaw,
                0.0f,
                1.0f,
                0.0f);

        GlStateManager.rotate(
                mc.player.rotationPitch,
                mc.gameSettings.thirdPersonView == 2 ? -1.0f : 1.0f,
                0.0f,
                0.0f);

        GlStateManager.scale(-scale, -scale, scale);

        int distance = (int) mc.player.getDistance(x, y, z);
        float scaleD = (distance / 2.0f) / (2.0f + (2.0f - 1));

        if (scaleD < 1.0f) {
            scaleD = 1;
        }

        GlStateManager.scale(scaleD, scaleD, scaleD);

        GlStateManager.translate(-(Managers.TEXT.getStringWidth((text)) / 2.0), 0, 0);

        Managers.TEXT.drawStringWithShadow((text), 0.0f, 6.0f, textColor.booleanValue ? textColor.getValue().getRGB() : -1);

        GlStateManager.enableDepth();

        GlStateManager.disableBlend();

        GlStateManager.disablePolygonOffset();
        GlStateManager.doPolygonOffset(1.0f, 1500000.0f);

        GlStateManager.popMatrix();

        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
    }

    private void drawNameTag(String text, Vec3d vec) {
        GlStateManager.pushMatrix();

        RenderHelper.enableStandardItemLighting();

        GlStateManager.enablePolygonOffset();
        GlStateManager.doPolygonOffset(1.0f, -1500000.0f);

        GlStateManager.disableLighting();
        GlStateManager.disableDepth();

        glEnable(GL_TEXTURE_2D);

        double x = vec.x;
        double y = vec.y;
        double z = vec.z;

        Entity camera = mc.getRenderViewEntity();

        assert camera != null;

        double distance = camera.getDistance(
                x + mc.getRenderManager().viewerPosX,
                y + mc.getRenderManager().viewerPosY,
                z + mc.getRenderManager().viewerPosZ);

        double scale = 0.0018 + 0.003f * distance;

        int textWidth = Managers.TEXT.getStringWidth(text) / 2;

        if (distance <= 8.0) {
            scale = 0.0245;
        }

        GlStateManager.translate(
                x,
                y + 0.4f,
                z);

        GlStateManager.rotate(
                -mc.getRenderManager().playerViewY,
                0.0f,
                1.0f,
                0.0f);

        GlStateManager.rotate(
                mc.getRenderManager().playerViewX,
                mc.gameSettings.thirdPersonView == 2 ? -1.0f : 1.0f,
                0.0f,
                0.0f);

        GlStateManager.scale(-scale, -scale, scale);

        Managers.TEXT.drawStringWithShadow(text, -textWidth - 0.1f, -(mc.fontRenderer.FONT_HEIGHT - 1), textColor.booleanValue ? textColor.getValue().getRGB() : -1);

        GlStateManager.enableDepth();
        GlStateManager.disableBlend();
        GlStateManager.disablePolygonOffset();
        GlStateManager.doPolygonOffset(1.0f, 1500000.0f);
        GlStateManager.popMatrix();

        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
    }
}

