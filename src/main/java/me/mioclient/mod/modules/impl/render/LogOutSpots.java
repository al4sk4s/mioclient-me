package me.mioclient.mod.modules.impl.render;

import com.mojang.realmsclient.gui.ChatFormatting;
import me.mioclient.api.events.impl.ConnectionEvent;
import me.mioclient.api.events.impl.Render3DEvent;
import me.mioclient.api.managers.Managers;
import me.mioclient.api.util.Wrapper;
import me.mioclient.api.util.entity.EntityUtil;
import me.mioclient.api.util.math.InterpolationUtil;
import me.mioclient.api.util.render.ColorUtil;
import me.mioclient.api.util.render.RenderUtil;
import me.mioclient.api.util.render.entity.StaticModelPlayer;
import me.mioclient.mod.modules.Category;
import me.mioclient.mod.modules.Module;
import me.mioclient.mod.modules.impl.client.FontMod;
import me.mioclient.mod.modules.settings.Setting;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import static org.lwjgl.opengl.GL11.*;

/**
 * @author t.me/asphyxia1337
 * Shout out phobos for the solution for constant updating.
 */

public class LogOutSpots extends Module {

    private final Setting<Float> range =
            add(new Setting<>("Range", 150.0f, 50.0f, 500.0f));

    private final Setting<Boolean> rect =
            add(new Setting<>("Rectangle", true));
    private final Setting<Boolean> outline =
            add(new Setting<>("Outline", true));
    private final Setting<Boolean> time =
            add(new Setting<>("Time", true));
    private final Setting<Boolean> coords =
            add(new Setting<>("Coords", true));

    private final Setting<Boolean> box =
            add(new Setting<>("Box", true));

    private final Setting<Color> color =
            add(new Setting<>("Color", new Color(0x96B61F1F, true)));
    private final Setting<Boolean> rainbow =
            add(new Setting<>("Rainbow", false));

    private final Setting<Boolean> chams =
            add(new Setting<>("Chams", true).setParent());
    private final Setting<Color> fillColor =
            add(new Setting<>("ChamsColor", new Color(190, 0, 0, 100), v -> chams.isOpen()));
    private final Setting<Color> lineColor =
            add(new Setting<>("LineColor", new Color(255, 255, 255, 120), v -> chams.isOpen()).injectBoolean(false));

    final Date date = new Date();

    protected final Map<UUID, LogOutSpot> spots = new ConcurrentHashMap<>();

    public LogOutSpots() {
        super("LogOutSpots", "Displays logout spots for players.", Category.RENDER, true);
    }

    @Override
    public void onEnable() {
        spots.clear();
    }

    @Override
    public void onDisable() {
        spots.clear();
    }

    @Override
    public void onLogout() {
        spots.clear();
    }

    @Override
    public void onTick() {
        for (LogOutSpot spot : spots.values()) {

            if (mc.player.getDistanceSq(spot.getPlayer()) >= range.getValue()) {
                spots.remove(spot.getPlayer().getUniqueID());
            }
        }
    }

    @Override
    public void onRender3D(Render3DEvent event) {

        for (LogOutSpot spot : spots.values()) {
            AxisAlignedBB bb = InterpolationUtil.getInterpolatedAxis(spot.getBoundingBox());

            if (chams.getValue()) {

                StaticModelPlayer model = spot.getModel();

                double x = spot.getX() - mc.getRenderManager().viewerPosX;
                double y = spot.getY() - mc.getRenderManager().viewerPosY;
                double z = spot.getZ() - mc.getRenderManager().viewerPosZ;

                glPushMatrix();
                glPushAttrib(GL_ALL_ATTRIB_BITS);

                glDisable(GL_TEXTURE_2D);
                glDisable(GL_LIGHTING);
                glDisable(GL_DEPTH_TEST);
                glEnable(GL_LINE_SMOOTH);
                glEnable(GL_BLEND);

                GlStateManager.blendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);

                GlStateManager.translate(x, y, z);
                GlStateManager.rotate(180 - model.getYaw(), 0, 1, 0);

                GlStateManager.enableRescaleNormal();
                GlStateManager.scale(-1.0F, -1.0F, 1.0F);

                double widthX = bb.maxX - bb.minX + 1;
                double widthZ = bb.maxZ - bb.minZ + 1;

                GlStateManager.scale(widthX, bb.maxY - bb.minY, widthZ);

                GlStateManager.translate(0.0F, -1.501F, 0.0F);

                Color fill = rainbow.getValue()
                        ? ColorUtil.injectAlpha(Managers.COLORS.getRainbow(), fillColor.getValue().getAlpha())
                        : fillColor.getValue();

                Color line = rainbow.getValue()
                        ? ColorUtil.injectAlpha(Managers.COLORS.getRainbow(), (lineColor.booleanValue ? lineColor.getValue().getAlpha() : fillColor.getValue().getAlpha()))
                        : lineColor.booleanValue ? lineColor.getValue() : fillColor.getValue();

                RenderUtil.glColor(fill);

                glPolygonMode(GL_FRONT_AND_BACK, GL_FILL);

                model.render(0.0625f);

                RenderUtil.glColor(line);

                glLineWidth(1.0f);

                glPolygonMode(GL_FRONT_AND_BACK, GL_LINE);

                model.render(0.0625f);

                //https://www.youtube.com/watch?v=fwdV2vWNvZw

                glPopAttrib();
                glPopMatrix();
            }

            if (box.getValue()) {
                RenderUtil.drawBlockOutline(
                        bb,
                        rainbow.getValue() ? Managers.COLORS.getRainbow() : color.getValue(),
                        1.0f,
                        false);
            }

            double x = InterpolationUtil.getInterpolatedDouble(
                    spot.getPlayer().lastTickPosX,
                    spot.getPlayer().posX,
                    event.getPartialTicks())
                    - mc.getRenderManager().renderPosX;

            double y = InterpolationUtil.getInterpolatedDouble(
                    spot.getPlayer().lastTickPosY,
                    spot.getPlayer().posY,
                    event.getPartialTicks())
                    - mc.getRenderManager().renderPosY;

            double z = InterpolationUtil.getInterpolatedDouble(
                    spot.getPlayer().lastTickPosZ,
                    spot.getPlayer().posZ,
                    event.getPartialTicks())
                    - mc.getRenderManager().renderPosZ;

            drawNameTag(
                    spot.getName(),
                    x,
                    y,
                    z);
        }
    }

    @SubscribeEvent
    public void onConnection(ConnectionEvent event) {
        if (event.getStage() == 0) {

            if (event.getName().equals(mc.getSession().getProfile().getName())) return;

            spots.remove(event.getUuid());

        } else if (event.getStage() == 1) {

            EntityPlayer player = event.getPlayer();

            if (player != null) {
                LogOutSpot spot = new LogOutSpot(player);

                spots.put(player.getUniqueID(), spot);
            }
        }
    }

    private void drawNameTag(String name, double x, double y, double z) {

        y += 0.7;

        Entity camera = mc.getRenderViewEntity();

        assert (camera != null);

        double originalPositionX = camera.posX;
        double originalPositionY = camera.posY;
        double originalPositionZ = camera.posZ;

        camera.posX = InterpolationUtil.getInterpolatedDouble(
                camera.prevPosX,
                camera.posX,
                mc.getRenderPartialTicks());

        camera.posY = InterpolationUtil.getInterpolatedDouble(
                camera.prevPosY,
                camera.posY,
                mc.getRenderPartialTicks());

        camera.posZ = InterpolationUtil.getInterpolatedDouble(
                camera.prevPosZ,
                camera.posZ,
                mc.getRenderPartialTicks());

        String displayTag = name
                        + (coords.getValue()
                        ? (" XYZ: " + (int) x + ", " + (int) y + ", " + (int) z)
                        : "")
                        + (time.getValue()
                        ? " " + ChatFormatting.GRAY + "(" + getLogOutTime() + ")"
                        : "");

        double distance = camera.getDistance(
                x + mc.getRenderManager().viewerPosX,
                y + mc.getRenderManager().viewerPosY,
                z + mc.getRenderManager().viewerPosZ);

        int width = Managers.TEXT.getStringWidth(displayTag) / 2;

        double scale = (0.0018 + (double) 5.0f * (distance * (double) 0.6f)) / 1000.0;

        if (distance <= 8.0) {
            scale = 0.0245;
        }

        GlStateManager.pushMatrix();

        RenderHelper.enableStandardItemLighting();

        GlStateManager.enablePolygonOffset();
        GlStateManager.doPolygonOffset(1.0f, -1500000.0f);

        GlStateManager.disableLighting();

        GlStateManager.translate((float) x, (float) y + 1.4f, (float) z);

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

        GlStateManager.disableDepth();

        GlStateManager.enableBlend();

        GlStateManager.enableBlend();

        if (rect.getValue()) {
            RenderUtil.drawRect(
                    -width - 2,
                    -(Managers.TEXT.getFontHeight() + 1),
                    (float) width + 2.0f,
                    1.5f,
                    0x55000000);
        }

        if (outline.getValue()) {
            RenderUtil.drawNameTagOutline(
                    -width - 2,
                    -(Managers.TEXT.getFontHeight() + 1),
                    (float) width + 2.0f,
                    1.5f,
                    0.8f,
                    color.getValue().getRGB(),
                    color.getValue().darker().getRGB(),
                    rainbow.getValue());
        }

        GlStateManager.disableBlend();

        Managers.TEXT.drawStringWithShadow(
                displayTag,
                -width,
                FontMod.INSTANCE.isOn() ? -(Managers.TEXT.getFontHeight() + 1) : -(Managers.TEXT.getFontHeight() - 1),
                rainbow.getValue() ? Managers.COLORS.getRainbow().getRGB() : color.getValue().getRGB());

        camera.posX = originalPositionX;
        camera.posY = originalPositionY;
        camera.posZ = originalPositionZ;

        GlStateManager.enableDepth();

        GlStateManager.disableBlend();

        GlStateManager.disablePolygonOffset();
        GlStateManager.doPolygonOffset(1.0f, 1500000.0f);

        GlStateManager.popMatrix();
    }

    private String getLogOutTime() {
        SimpleDateFormat dateFormatter = new SimpleDateFormat("HH:mm:ss");

        return dateFormatter.format(date);
    }

    protected static class LogOutSpot implements Wrapper {

        private final String name;
        private final StaticModelPlayer model;
        private final AxisAlignedBB boundingBox;
        private final EntityPlayer player;

        private final double x;
        private final double y;
        private final double z;

        public LogOutSpot(EntityPlayer player) {

            name = player.getName();

            model = new StaticModelPlayer(
                    EntityUtil.getCopiedPlayer(player),
                    player instanceof AbstractClientPlayer && ((AbstractClientPlayer) player).getSkinType().equals("slim"),
                    0);

            model.disableArmorLayers();
            boundingBox = player.getEntityBoundingBox();

            x = player.posX;
            y = player.posY;
            z = player.posZ;

            this.player = player;
        }

        public String getName() {
            return name;
        }

        public double getX() {
            return x;
        }

        public double getY() {
            return y;
        }

        public double getZ() {
            return z;
        }

        public double getDistance() {
            return mc.player.getDistance(x, y, z);
        }

        public AxisAlignedBB getBoundingBox() {
            return boundingBox;
        }

        public StaticModelPlayer getModel() {
            return model;
        }

        public EntityPlayer getPlayer() {
            return player;
        }
    }
}