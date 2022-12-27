package me.mioclient.api.managers.impl;

import com.google.common.base.Strings;
import com.mojang.realmsclient.gui.ChatFormatting;
import me.mioclient.api.events.impl.*;
import me.mioclient.api.managers.Managers;
import me.mioclient.api.util.math.Timer;
import me.mioclient.api.util.render.RenderUtil;
import me.mioclient.mod.Mod;
import me.mioclient.mod.commands.Command;
import me.mioclient.mod.modules.impl.client.HUD;
import net.minecraft.client.gui.GuiDownloadTerrain;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.play.server.SPacketEntityStatus;
import net.minecraft.network.play.server.SPacketPlayerListItem;
import net.minecraftforge.client.event.ClientChatEvent;
import net.minecraftforge.client.event.EntityViewRenderEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.common.network.FMLNetworkEvent;
import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.Display;

import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

public class EventManager extends Mod {

    private final Timer logoutTimer = new Timer();
    private final AtomicBoolean tickOngoing;

    public EventManager() {
        tickOngoing = new AtomicBoolean(false);
    }

    public void init() {
        MinecraftForge.EVENT_BUS.register(this);
    }

    public boolean ticksOngoing() {
        return tickOngoing.get();
    }

    public void onUnload() {
        MinecraftForge.EVENT_BUS.unregister(this);
    }

    @SubscribeEvent
    public void onUpdate(LivingEvent.LivingUpdateEvent event) {
        if (!fullNullCheck() && (event.getEntity().getEntityWorld()).isRemote && event.getEntityLiving().equals(mc.player)) {
            Managers.MODULES.onUpdate();

            Managers.MODULES.sortModules(HUD.getInstance().ordering.getValue());
        }
    }

    @SubscribeEvent
    public void onClientConnect(FMLNetworkEvent.ClientConnectedToServerEvent event) {
        logoutTimer.reset();
        Managers.MODULES.onLogin();
    }

    @SubscribeEvent
    public void onClientDisconnect(FMLNetworkEvent.ClientDisconnectionFromServerEvent event) {
        Managers.CONFIGS.saveConfig(Managers.CONFIGS.config.replaceFirst("hvhlegend/", ""));
        Managers.MODULES.onLogout();
    }

    @SubscribeEvent
    public void onTick(TickEvent.ClientTickEvent event) {
        if (fullNullCheck()) return;

        Managers.MODULES.onTick();

        for (EntityPlayer player : mc.world.playerEntities) {
            if (player == null || player.getHealth() > 0.0F) continue;

            MinecraftForge.EVENT_BUS.post(new DeathEvent(player));

            Managers.MODULES.onDeath(player);
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public void onUpdateWalkingPlayer(UpdateWalkingPlayerEvent event) {
        if (fullNullCheck()) return;

        if (event.getStage() == 0) {
            Managers.SPEED.updateValues();
            Managers.ROTATIONS.updateRotations();
            Managers.POSITION.updatePosition();
        }
        if (event.getStage() == 1) {
            Managers.ROTATIONS.resetRotations();
            Managers.POSITION.restorePosition();
        }
    }

    @SubscribeEvent
    public void onPacketReceive(PacketEvent.Receive event) {
        if (event.getStage() != 0) return;

        Managers.SERVER.onPacketReceived();

        if (event.getPacket() instanceof SPacketEntityStatus) {
            SPacketEntityStatus packet = event.getPacket();

            if (packet.getOpCode() == 35 && packet.getEntity(mc.world) instanceof EntityPlayer) {
                EntityPlayer player = (EntityPlayer) packet.getEntity(mc.world);

                MinecraftForge.EVENT_BUS.post(new TotemPopEvent(player));

                Managers.MODULES.onTotemPop(player);
            }
        }
        if (event.getPacket() instanceof SPacketPlayerListItem && !fullNullCheck() && logoutTimer.passedS(1.0D)) {
            SPacketPlayerListItem packet = event.getPacket();
            if (SPacketPlayerListItem.Action.ADD_PLAYER != packet.getAction() && SPacketPlayerListItem.Action.REMOVE_PLAYER != packet.getAction())
                return;
            packet.getEntries().stream().filter(Objects::nonNull).filter(data -> (!Strings.isNullOrEmpty(data.getProfile().getName()) || data.getProfile().getId() != null))
                    .forEach(data -> {
                        String name;
                        EntityPlayer entity;
                        UUID id = data.getProfile().getId();
                        switch (packet.getAction()) {
                            case ADD_PLAYER:
                                name = data.getProfile().getName();
                                MinecraftForge.EVENT_BUS.post(new ConnectionEvent(0, id, name));
                                break;
                            case REMOVE_PLAYER:
                                entity = mc.world.getPlayerEntityByUUID(id);
                                if (entity != null) {
                                    String logoutName = entity.getName();
                                    MinecraftForge.EVENT_BUS.post(new ConnectionEvent(1, entity, id, logoutName));
                                    break;
                                }
                                MinecraftForge.EVENT_BUS.post(new ConnectionEvent(2, id, null));
                                break;
                        }
                    });
        }

        if (event.getPacket() instanceof net.minecraft.network.play.server.SPacketTimeUpdate) Managers.SERVER.update();
    }

    @SubscribeEvent
    public void onWorldRender(RenderWorldLastEvent event) {
        if (event.isCanceled()) return;

        Managers.FPS.update();

        mc.profiler.startSection("hvhlegend");
        GlStateManager.disableTexture2D();
        GlStateManager.enableBlend();
        GlStateManager.disableAlpha();
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
        GlStateManager.shadeModel(7425);
        GlStateManager.disableDepth();
        GlStateManager.glLineWidth(1.0F);

        Render3DEvent render3dEvent = new Render3DEvent(event.getPartialTicks());
        Managers.MODULES.onRender3D(render3dEvent);

        GlStateManager.glLineWidth(1.0F);
        GlStateManager.shadeModel(7424);
        GlStateManager.disableBlend();
        GlStateManager.enableAlpha();
        GlStateManager.enableTexture2D();
        GlStateManager.enableDepth();
        GlStateManager.enableCull();
        GlStateManager.enableCull();
        GlStateManager.depthMask(true);
        GlStateManager.enableTexture2D();
        GlStateManager.enableBlend();
        GlStateManager.enableDepth();
        mc.profiler.endSection();
    }

    @SubscribeEvent
    public void renderHUD(RenderGameOverlayEvent.Post event) {
        if (event.getType() == RenderGameOverlayEvent.ElementType.HOTBAR)
            Managers.TEXT.updateResolution();
    }

    @SubscribeEvent(priority = EventPriority.LOW)
    public void onRenderGameOverlayEvent(RenderGameOverlayEvent.Text event) {
        if (event.getType() == RenderGameOverlayEvent.ElementType.TEXT) {
            ScaledResolution resolution = new ScaledResolution(mc);
            Render2DEvent render2DEvent = new Render2DEvent(event.getPartialTicks(), resolution);
            Managers.MODULES.onRender2D(render2DEvent);
            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        }
    }

    @SubscribeEvent(priority = EventPriority.NORMAL, receiveCanceled = true)
    public void onKeyInput(InputEvent.KeyInputEvent event) {
        if (Keyboard.getEventKeyState())
            Managers.MODULES.onKeyInput(Keyboard.getEventKey());
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onChatSent(ClientChatEvent event) {
        if (event.getMessage().startsWith(Command.getCommandPrefix())) {
            event.setCanceled(true);

            try {
                mc.ingameGUI.getChatGUI().addToSentMessages(event.getMessage());

                if (event.getMessage().length() > 1) {
                    Managers.COMMANDS.executeCommand(event.getMessage().substring(Command.getCommandPrefix().length() - 1));

                } else {
                    Command.sendMessage("Please enter a command.");
                }

            } catch (Exception e) {
                e.printStackTrace();
                Command.sendMessage(ChatFormatting.RED + "An error occurred while running this command. Check the log!");
            }
        }
    }

    @SubscribeEvent
    public void onFogColor(EntityViewRenderEvent.FogColors event) {
        RenderFogColorEvent fogColorEvent = new RenderFogColorEvent();
        MinecraftForge.EVENT_BUS.post(fogColorEvent);

        if (fogColorEvent.isCanceled()) {
            event.setRed(fogColorEvent.getColor().getRed() / 255F);
            event.setGreen(fogColorEvent.getColor().getGreen() / 255F);
            event.setBlue(fogColorEvent.getColor().getBlue() / 255F);
        }
    }
}
