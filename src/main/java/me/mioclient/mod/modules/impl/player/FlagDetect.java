package me.mioclient.mod.modules.impl.player;

import com.mojang.realmsclient.gui.ChatFormatting;
import me.mioclient.api.events.impl.PacketEvent;
import me.mioclient.api.events.impl.Render3DEvent;
import me.mioclient.api.util.entity.CopyOfPlayer;
import me.mioclient.api.util.entity.EntityUtil;
import me.mioclient.api.util.render.ColorUtil;
import me.mioclient.api.util.render.RenderUtil;
import me.mioclient.api.util.render.entity.StaticModelPlayer;
import me.mioclient.mod.commands.Command;
import me.mioclient.mod.modules.Category;
import me.mioclient.mod.modules.Module;
import me.mioclient.mod.modules.impl.exploit.Clip;
import me.mioclient.mod.modules.settings.Setting;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.play.server.SPacketPlayerPosLook;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.awt.*;

import static org.lwjgl.opengl.GL11.*;

/**
 * @author t.me/asphyxia1337
 */

public class FlagDetect extends Module {

    private final Setting<Boolean> notify =
            add(new Setting<>("ChatNotify", true));

    private final Setting<Boolean> chams =
            add(new Setting<>("Chams", true).setParent());
    private final Setting<Integer> fadeTime =
            add(new Setting<>("FadeTime", 15, 1, 50, v -> chams.isOpen()));
    private final Setting<Color> color =
            add(new Setting<>("Color", new Color(190, 0, 0, 100), v -> chams.isOpen()));
    private final Setting<Color> lineColor =
            add(new Setting<>("LineColor", new Color(255, 255, 255, 120), v -> chams.isOpen()).injectBoolean(false));

    private CopyOfPlayer player;

    public FlagDetect() {
        super("FlagDetect", "Detects & notifies you when your player is being flagged.", Category.PLAYER, true);
    }

    @SubscribeEvent
    public void onPacket(PacketEvent event) {
        if (!fullNullCheck() && spawnCheck() && !Clip.INSTANCE.isOn()) {
            
            if (event.getPacket() instanceof SPacketPlayerPosLook) {

                if (notify.getValue()) {
                    Command.sendMessageWithID(ChatFormatting.RED + "Server lagged you back!", -123);
                }

                if (chams.getValue()) {

                    player = new CopyOfPlayer(
                            EntityUtil.getCopiedPlayer(mc.player),
                            System.currentTimeMillis(),
                            mc.player.posX,
                            mc.player.posY,
                            mc.player.posZ,
                            mc.player.getSkinType().equals("slim"));
                }
            }
        }
    }

    @Override
    public void onRender3D(Render3DEvent event) {

        if (fullNullCheck() || !chams.getValue() || player == null) return;

        EntityPlayer player = this.player.getPlayer();
        StaticModelPlayer model = this.player.getModel();

        double x = this.player.getX() - mc.getRenderManager().viewerPosX;
        double y = this.player.getY() - mc.getRenderManager().viewerPosY;
        double z = this.player.getZ() - mc.getRenderManager().viewerPosZ;

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

        Color boxColor = color.getValue();
        Color outlineColor = lineColor.booleanValue ? lineColor.getValue() : color.getValue();

        float maxBoxAlpha = boxColor.getAlpha();
        float maxOutlineAlpha = outlineColor.getAlpha();

        float alphaBoxAmount = maxBoxAlpha / (fadeTime.getValue() * 100);
        float alphaOutlineAmount = maxOutlineAlpha / (fadeTime.getValue() * 100);

        int fadeBoxAlpha = MathHelper.clamp((int) (alphaBoxAmount * (this.player.getTime() + (fadeTime.getValue() * 100) - System.currentTimeMillis())), 0, (int) maxBoxAlpha);
        int fadeOutlineAlpha = MathHelper.clamp((int) (alphaOutlineAmount * (this.player.getTime() + (fadeTime.getValue() * 100) - System.currentTimeMillis())), 0, (int) maxOutlineAlpha);

        Color box = ColorUtil.injectAlpha(boxColor, fadeBoxAlpha);
        Color line = ColorUtil.injectAlpha(outlineColor, fadeOutlineAlpha);

        GlStateManager.enableRescaleNormal();
        GlStateManager.scale(-1.0F, -1.0F, 1.0F);

        double widthX = player.getEntityBoundingBox().maxX - player.getRenderBoundingBox().minX + 1;
        double widthZ = player.getEntityBoundingBox().maxZ - player.getEntityBoundingBox().minZ + 1;

        GlStateManager.scale(widthX, player.height, widthZ);

        GlStateManager.translate(0.0F, -1.501F, 0.0F);

        RenderUtil.glColor(box);

        glPolygonMode(GL_FRONT_AND_BACK, GL_FILL);

        model.render(0.0625f);

        RenderUtil.glColor(line);

        glLineWidth(0.8f);

        glPolygonMode(GL_FRONT_AND_BACK, GL_LINE);

        model.render(0.0625f);

        glPopAttrib();
        glPopMatrix();

        if (this.player.getTime() + (fadeTime.getValue() * 100) < System.currentTimeMillis()) this.player = null;
    }
}


