package me.mioclient.mod.modules.impl.render;

import com.google.common.collect.Maps;
import me.mioclient.api.events.impl.DamageBlockEvent;
import me.mioclient.api.events.impl.PacketEvent;
import me.mioclient.api.events.impl.Render2DEvent;
import me.mioclient.api.events.impl.Render3DEvent;
import me.mioclient.api.util.interact.BlockUtil;
import me.mioclient.api.util.math.InterpolationUtil;
import me.mioclient.api.util.render.RenderUtil;
import me.mioclient.mod.modules.Category;
import me.mioclient.mod.modules.Module;
import me.mioclient.mod.modules.settings.Setting;
import net.minecraft.block.Block;
import net.minecraft.block.BlockAir;
import net.minecraft.init.Blocks;
import net.minecraft.network.play.server.SPacketBlockBreakAnim;
import net.minecraft.network.play.server.SPacketBlockChange;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.awt.*;
import java.util.ArrayList;
import java.util.Map;

public class BreakingESP extends Module {

    private final Setting<Boolean> showSelf =
            add(new Setting<>("ShowSelf", true));

    private final Setting<Mode> mode =
            add(new Setting<>("Mode", Mode.OUT));

    private final Setting<Boolean> box =
            add(new Setting<>("Box", true));

    private final Setting<Boolean> line =
            add(new Setting<>("Line", true));

    private final Setting<Float> lineWidth =
            add(new Setting<>("LineWidth", 1.0f, 0.1f, 3f));

    private final Setting<Double> range =
            add(new Setting<>("Range", 20d, 1d, 50d));

    private final Setting<ColorMode> colorMode =
            add(new Setting<>("ColorMode", ColorMode.PROGRESS));

    private final Setting<Color> color =
            add(new Setting<>("Color", new Color(0x887D7DD5, true), v -> colorMode.getValue() != ColorMode.PROGRESS));

    private final Map<BlockPos, Integer> blocks = Maps.newHashMap();

    ArrayList<ArrayList<Object>> packets = new ArrayList<>();

    public BreakingESP() {
        super("BreakingESP", "Highlights the blocks being broken around you.", Category.RENDER, true);
    }

    private enum Mode {
        IN,
        OUT
    }

    private enum ColorMode {
        PROGRESS,
        CUSTOM
    }

    @Override
    public void onRender3D(Render3DEvent event) {

        blocks.forEach((pos, progress) -> {
            if (pos != null && progress != null) {

                if (BlockUtil.getBlock(pos) == Blocks.AIR) {
                    return;
                }

                if (pos.getDistance((int) mc.player.posX, (int) mc.player.posY, (int) mc.player.posZ) <= range.getValue()) {

                    float preDamage = mc.playerController.curBlockDamageMP;

                    float damage = InterpolationUtil.getInterpolatedFloat(preDamage, mc.playerController.curBlockDamageMP, event.getPartialTicks());

                    drawESP(pos, damage);
                }
            }
        });

        for (int i = 0; i < packets.size(); i++) {

            BlockPos pos = (BlockPos) packets.get(i).get(0);
            int ticks = (int) packets.get(i).get(1);

            if (BlockUtil.getBlock(pos) instanceof BlockAir) {
                packets.remove(i);
                i--;
                continue;
            }

            if (!blocks.containsKey(pos)) {

                if (pos.getDistance((int) mc.player.posX, (int) mc.player.posY, (int) mc.player.posZ) <= range.getValue()) {
                    drawESP(pos, Math.min(ticks, 140) / 140.0f);
                }

            } else {
                packets.get(i).set(1, ++ticks);
            }

            if (++ticks > 140) {
                packets.remove(i);
                i--;

            } else {
                packets.get(i).set(1, ticks);
            }
        }
    }

    @SubscribeEvent
    public void onPacketReceive(PacketEvent.Receive event) {
        if (event.getPacket() instanceof SPacketBlockChange) {
            if (blocks.containsKey(((SPacketBlockChange) event.getPacket()).getBlockPosition())) {

                if (((SPacketBlockChange) event.getPacket()).getBlockState().getBlock() != Blocks.AIR) {
                    blocks.remove(((SPacketBlockChange) event.getPacket()).getBlockPosition());
                }
            }
        }

        if (event.getPacket() instanceof SPacketBlockBreakAnim) {
            SPacketBlockBreakAnim packet = event.getPacket();

            BlockPos pos = packet.getPosition();

            Block block = BlockUtil.getBlock(pos);

            if (!renderingPos(pos) && block != Blocks.BEDROCK && block != Blocks.BARRIER && block != Blocks.AIR) {

                if (!showSelf.getValue() && mc.world.getEntityByID(packet.getBreakerId()) == mc.player) return;

                packets.add(new ArrayList<Object>() {
                    {
                        add(packet.getPosition());
                        add(0);
                    }
                });
            }
        }
    }

    @SubscribeEvent
    public void onDamageBlock(DamageBlockEvent event) {
        if (fullNullCheck() || mc.player.getDistanceSq(event.getPosition()) > range.getValue()) return;

        if (!showSelf.getValue() && mc.world.getEntityByID(event.getBreakerId()) == mc.player) return;

        if (event.getProgress() > 0 && event.getProgress() < 9) {

            blocks.putIfAbsent(event.getPosition(), event.getProgress());

        } else {
            blocks.remove(event.getPosition(), event.getProgress());
        }
    }

    private void drawESP(BlockPos pos, float damage) {
        AxisAlignedBB bb = mc.world.getBlockState(pos).getSelectedBoundingBox(mc.world, pos);

        double x = bb.minX + (bb.maxX - bb.minX) / 2;
        double y = bb.minY + (bb.maxY - bb.minY) / 2;
        double z = bb.minZ + (bb.maxZ - bb.minZ) / 2;

        double sizeX = damage * ((bb.maxX - x));
        double sizeY = damage * ((bb.maxY - y));
        double sizeZ = damage * ((bb.maxZ - z));

        Color color = colorMode.getValue() == ColorMode.PROGRESS
                ? new Color(damage <= 0.75f ? 200 : 0, damage >= 0.751f ? 200 : 0, 0, this.color.getValue().getAlpha())
                : this.color.getValue();

        AxisAlignedBB inBB = bb.shrink(damage * bb.getAverageEdgeLength() * 0.5);

        AxisAlignedBB outBB = new AxisAlignedBB(x - sizeX, y - sizeY, z - sizeZ, x + sizeX, y + sizeY, z + sizeZ);

        RenderUtil.drawBoxESP(
                mode.getValue() == Mode.IN ? inBB : outBB,
                color,
                false,
                new Color(-1),
                lineWidth.getValue(),
                line.getValue(),
                box.getValue(),
                color.getAlpha(),
                false);
    }

    private boolean renderingPos(BlockPos pos) {
        for (ArrayList<Object> part : packets) {

            BlockPos temp = (BlockPos) part.get(0);

            if (temp.getX() == pos.getX() && temp.getY() == pos.getY() && temp.getZ() == pos.getZ()) {
                return true;
            }
        }
        return false;
    }
}
