package me.mioclient.mod.modules.impl.combat;

import me.mioclient.api.events.impl.PacketEvent;
import me.mioclient.api.events.impl.Render3DEvent;
import me.mioclient.api.managers.Managers;
import me.mioclient.api.util.entity.EntityUtil;
import me.mioclient.api.util.math.MathUtil;
import me.mioclient.api.util.math.Timer;
import me.mioclient.api.util.render.RenderUtil;
import me.mioclient.api.util.world.InventoryUtil;
import me.mioclient.mod.modules.Category;
import me.mioclient.mod.modules.Module;
import me.mioclient.mod.modules.settings.Setting;
import net.minecraft.block.BlockEnderChest;
import net.minecraft.block.BlockObsidian;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityEnderCrystal;
import net.minecraft.init.Blocks;
import net.minecraft.network.play.client.CPacketAnimation;
import net.minecraft.network.play.client.CPacketUseEntity;
import net.minecraft.network.play.server.SPacketBlockBreakAnim;
import net.minecraft.network.play.server.SPacketBlockChange;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.awt.*;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author t.me/asphyxia1337
 */

public class Blocker extends Module {

    private final Setting<Boolean> extend =
            add(new Setting<>("Extend", true));
    private final Setting<Boolean> face =
            add(new Setting<>("Face", true));
    private final Setting<Boolean> packet =
            add(new Setting<>("Packet", true));
    private final Setting<Boolean> rotate =
            add(new Setting<>("Rotate", false));

    private final Setting<Boolean> render =
            add(new Setting<>("Render", true).setParent());
    private final Setting<Boolean> box =
            add(new Setting<>("Box", true, v -> render.isOpen()));
    private final Setting<Boolean> line =
            add(new Setting<>("Line", true, v -> render.isOpen()));

    private final Map<BlockPos, Long> renderBlocks = new ConcurrentHashMap<>();
    private final Timer renderTimer = new Timer();

    public Blocker() {
        super("Blocker", "Attempts to extend your surround when it's being broken.", Category.COMBAT, true);
    }

    @Override
    public void onRender3D(Render3DEvent event) {
        if (render.getValue()) {
            renderTimer.reset();

            renderBlocks.forEach((pos, time) -> {
                int lineA = 255;
                int fillA = 80;

                if (System.currentTimeMillis() - time > 400) {
                    renderTimer.reset();
                    renderBlocks.remove(pos);

                } else {
                    long endTime = System.currentTimeMillis() - time - (100);
                    double normal = MathUtil.normalize((double) endTime, 0.0D, (500));

                    normal = MathHelper.clamp(normal, 0.0D, 1.0D);
                    normal = -normal + 1.0D;

                    int firstAl = (int) (normal * (double) lineA);
                    int secondAl = (int) (normal * (double) fillA);

                    RenderUtil.drawBoxESP(
                            new BlockPos(pos),
                            Managers.COLORS.getCurrent(),
                            true,
                            new Color(255, 255, 255, firstAl),
                            0.7f, line.getValue(),
                            box.getValue(),
                            secondAl,
                            true,
                            0.0);
                }
            });
        }
    }

    @SubscribeEvent
    public void onPacketReceive(PacketEvent.Receive event) {
        if (event.getPacket() instanceof SPacketBlockBreakAnim && EntityUtil.isInHole(mc.player)) {
            SPacketBlockBreakAnim packet = event.getPacket();
            BlockPos pos = packet.getPosition();

            if (mc.world.getBlockState(pos).getBlock() == (Blocks.BEDROCK) || mc.world.getBlockState(pos).getBlock() == (Blocks.AIR)) return;

            BlockPos playerPos = new BlockPos(mc.player.posX, mc.player.posY, mc.player.posZ);
            BlockPos placePos = null;

            if (extend.getValue()) {
                if (pos.equals(playerPos.north()))
                    placePos = (playerPos.north().north());

                if (pos.equals(playerPos.east()))
                    placePos = (playerPos.east().east());

                if (pos.equals(playerPos.west()))
                    placePos = (playerPos.west().west());

                if (pos.equals(playerPos.south()))
                    placePos = (playerPos.south().south());
            }

            if (face.getValue()) {
                if (pos.equals(playerPos.north()))
                    placePos = (playerPos.north().add(0, 1, 0));

                if (pos.equals(playerPos.east()))
                    placePos = (playerPos.east().add(0, 1, 0));

                if (pos.equals(playerPos.west()))
                    placePos = (playerPos.west().add(0, 1, 0));

                if (pos.equals(playerPos.south()))
                    placePos = (playerPos.south().add(0, 1, 0));
            }

            if (placePos != null) {
                placeBlock(placePos);
            }
        }

        if (event.getPacket() instanceof SPacketBlockChange) {
            if (renderBlocks.containsKey(((SPacketBlockChange) event.getPacket()).getBlockPosition())) {
                renderTimer.reset();

                if (((SPacketBlockChange) event.getPacket()).getBlockState().getBlock() != Blocks.AIR && renderTimer.passedMs(400)) {
                    renderBlocks.remove(((SPacketBlockChange) event.getPacket()).getBlockPosition());
                }
            }
        }
    }

    private void placeBlock(BlockPos pos){
        if (!mc.world.isAirBlock(pos)) return;

        int oldSlot = mc.player.inventory.currentItem;

        int obbySlot = InventoryUtil.findHotbarBlock(BlockObsidian.class);
        int eChestSlot = InventoryUtil.findHotbarBlock(BlockEnderChest.class);

        if (obbySlot == -1 && eChestSlot == 1) return;

        for (Entity entity : mc.world.getEntitiesWithinAABB(Entity.class, new AxisAlignedBB(pos))) {
            if (entity instanceof EntityEnderCrystal) {
                mc.player.connection.sendPacket(new CPacketUseEntity(entity));
                mc.player.connection.sendPacket(new CPacketAnimation(EnumHand.MAIN_HAND));
            }
        }

        mc.player.inventory.currentItem = obbySlot == -1 ? eChestSlot : obbySlot;

        mc.playerController.updateController();
        renderBlocks.put(pos, System.currentTimeMillis());

        Managers.INTERACTIONS.placeBlock(pos, rotate.getValue(), packet.getValue(), true);

        if (mc.player.inventory.currentItem != oldSlot) {
            mc.player.inventory.currentItem = oldSlot;
            mc.playerController.updateController();
        }

        mc.player.inventory.currentItem = oldSlot;
    }
}