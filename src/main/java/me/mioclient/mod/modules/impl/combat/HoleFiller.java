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
import net.minecraft.block.BlockWeb;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.network.play.server.SPacketBlockChange;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import static net.minecraft.util.EnumHand.MAIN_HAND;


public class HoleFiller extends Module {

    private final Setting<Boolean> rotate =
            add(new Setting<>("Rotate", false));
    private final Setting<Boolean> packet =
            add(new Setting<>("Packet", false));
    private final Setting<Boolean> webs =
            add(new Setting<>("Webs", false));
    private final Setting<Boolean> autoDisable =
            add(new Setting<>("AutoDisable", true));
    private final Setting<Double> range =
            add(new Setting<>("Radius", 4.0, 0.0, 6));

    private final Setting<Boolean> smart =
            add(new Setting<>("Smart", false).setParent());
    private final Setting<Logic> logic =
            add(new Setting<>("Logic", Logic.PLAYER, v -> smart.isOpen()));
    private final Setting<Integer> smartRange =
            add(new Setting<>("EnemyRange", 4, 0, 6, v -> smart.isOpen()));

    private final Setting<Boolean> render =
            add(new Setting<>("Render", true).setParent());
    private final Setting<Boolean> box =
            add(new Setting<>("Box", true, v -> render.isOpen()));
    private final Setting<Boolean> line =
            add(new Setting<>("Line", true, v -> render.isOpen()));

    private final Map<BlockPos, Long> renderBlocks = new ConcurrentHashMap<>();
    private final Timer renderTimer = new Timer();

    private EntityPlayer closestTarget;

    public HoleFiller() {
        super("HoleFiller", "Fills all safe spots in radius.", Category.COMBAT, true);
    }

    private enum Logic {
        PLAYER,
        HOLE
    }

    @Override
    public void onEnable() {
        super.onEnable();
    }

    @Override
    public void onDisable() {
        closestTarget = null;
        Managers.ROTATIONS.resetRotationsPacket();
    }

    @Override
    public String getInfo() {
        if (smart.getValue()) {
            return Managers.TEXT.normalizeCases(logic.getValue());

        } else {
            return "Normal";
        }
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
                            0.7f,
                            line.getValue(),
                            box.getValue(),
                            secondAl,
                            true,
                            0.0);
                }
            });
        }
    }

    @Override
    public void onUpdate() {
        if (mc.world == null) {
            return;
        }
        if (smart.getValue()) {
            findClosestTarget();
        }
        List<BlockPos> blocks = getPlacePositions();
        BlockPos q = null;

        int obbySlot = InventoryUtil.findHotbarBlock(BlockObsidian.class);
        int eChestSlot = InventoryUtil.findHotbarBlock(BlockEnderChest.class);
        int webSlot = InventoryUtil.findHotbarBlock(BlockWeb.class);

        if (obbySlot == -1 && eChestSlot == -1) return;

        if (webs.getValue() && webSlot == -1 && obbySlot == -1 && eChestSlot == -1) return;

        int originalSlot = mc.player.inventory.currentItem;

        for (BlockPos blockPos : blocks) {
            if (!mc.world.getEntitiesWithinAABB(Entity.class, new AxisAlignedBB(blockPos)).isEmpty()) continue;
            if (smart.getValue() && isInRange(blockPos)) {
                q = blockPos;
                continue;
            } else if (smart.getValue() && isInRange(blockPos) && logic.getValue() == Logic.HOLE && closestTarget.getDistanceSq(blockPos) <= smartRange.getValue()) {
                q = blockPos;
                continue;
            }
            q = blockPos;
        }

        if (q != null && mc.player.onGround) {

            mc.player.inventory.currentItem = webs.getValue() ? (webSlot == -1 ? (obbySlot == -1 ? eChestSlot : obbySlot) : webSlot) : (obbySlot == -1 ? eChestSlot : obbySlot);

            mc.playerController.updateController();
            renderBlocks.put(q, System.currentTimeMillis());

            Managers.INTERACTIONS.placeBlock(q, rotate.getValue(), packet.getValue(), false);

            if (mc.player.inventory.currentItem != originalSlot) {
                mc.player.inventory.currentItem = originalSlot;
                mc.playerController.updateController();
            }
            mc.player.swingArm(MAIN_HAND);
            mc.player.inventory.currentItem = originalSlot;
        }
        if (q == null && autoDisable.getValue() && !smart.getValue()) {
            disable();
        }
    }

    @SubscribeEvent
    public void onPacketReceive(PacketEvent.Receive event) {
        if (event.getPacket() instanceof SPacketBlockChange) {
            if (renderBlocks.containsKey(((SPacketBlockChange) event.getPacket()).getBlockPosition())) {
                renderTimer.reset();

                if (((SPacketBlockChange) event.getPacket()).getBlockState().getBlock() != Blocks.AIR && renderTimer.passedMs(400)) {
                    renderBlocks.remove(((SPacketBlockChange) event.getPacket()).getBlockPosition());
                }
            }
        }
    }

    private boolean isHole(BlockPos pos) {
        BlockPos boost = pos.add(0, 1, 0);
        BlockPos boost2 = pos.add(0, 0, 0);
        BlockPos boost3 = pos.add(0, 0, -1);
        BlockPos boost4 = pos.add(1, 0, 0);
        BlockPos boost5 = pos.add(-1, 0, 0);
        BlockPos boost6 = pos.add(0, 0, 1);
        BlockPos boost7 = pos.add(0, 2, 0);
        BlockPos boost8 = pos.add(0.5, 0.5, 0.5);
        BlockPos boost9 = pos.add(0, -1, 0);
        return !(mc.world.getBlockState(boost).getBlock() != Blocks.AIR || mc.world.getBlockState(boost2).getBlock() != Blocks.AIR || mc.world.getBlockState(boost7).getBlock() != Blocks.AIR || mc.world.getBlockState(boost3).getBlock() != Blocks.OBSIDIAN && mc.world.getBlockState(boost3).getBlock() != Blocks.BEDROCK || mc.world.getBlockState(boost4).getBlock() != Blocks.OBSIDIAN && mc.world.getBlockState(boost4).getBlock() != Blocks.BEDROCK || mc.world.getBlockState(boost5).getBlock() != Blocks.OBSIDIAN && mc.world.getBlockState(boost5).getBlock() != Blocks.BEDROCK || mc.world.getBlockState(boost6).getBlock() != Blocks.OBSIDIAN && mc.world.getBlockState(boost6).getBlock() != Blocks.BEDROCK || mc.world.getBlockState(boost8).getBlock() != Blocks.AIR || mc.world.getBlockState(boost9).getBlock() != Blocks.OBSIDIAN && mc.world.getBlockState(boost9).getBlock() != Blocks.BEDROCK);
    }

    private BlockPos getPlayerPos() {
        return new BlockPos(Math.floor(mc.player.posX), Math.floor(mc.player.posY), Math.floor(mc.player.posZ));
    }

    private BlockPos getClosestTargetPos() {
        if (closestTarget != null) {
            return new BlockPos(Math.floor(closestTarget.posX), Math.floor(closestTarget.posY), Math.floor(closestTarget.posZ));
        }
        return null;
    }

    private void findClosestTarget() {
        List<EntityPlayer> playerList = mc.world.playerEntities;

        closestTarget = null;

        for (EntityPlayer target : playerList) {
            if (target == mc.player || Managers.FRIENDS.isFriend(target.getName()) || !EntityUtil.isLiving(target) || target.getHealth() <= 0.0f) continue;
            if (closestTarget == null) {
                closestTarget = target;
                continue;
            }

            if (!(mc.player.getDistance(target) < mc.player.getDistance(closestTarget))) continue;
            closestTarget = target;
        }
    }

    private boolean isInRange(BlockPos blockPos) {
        NonNullList positions = NonNullList.create();

        positions.addAll(getSphere(getPlayerPos(), range.getValue().floatValue(), range.getValue().intValue()).stream().filter(this::isHole).collect(Collectors.toList()));
        return positions.contains(blockPos);
    }

    private List<BlockPos> getPlacePositions() {
        NonNullList positions = NonNullList.create();

        if (smart.getValue() && closestTarget != null) {
            positions.addAll(getSphere(getClosestTargetPos(), smartRange.getValue().floatValue(), range.getValue().intValue()).stream().filter(this::isHole).filter(this::isInRange).collect(Collectors.toList()));
        } else if (!smart.getValue()) {
            positions.addAll(getSphere(getPlayerPos(), range.getValue().floatValue(), range.getValue().intValue()).stream().filter(this::isHole).collect(Collectors.toList()));
        }
        return positions;
    }

    private List<BlockPos> getSphere(BlockPos loc, float r, int h) {

        ArrayList<BlockPos> circleBlocks = new ArrayList<>();

        int cx = loc.getX();
        int cy = loc.getY();
        int cz = loc.getZ();
        int x = cx - (int)r;

        while ((float)x <= (float)cx + r) {

            int z = cz - (int)r;

            while ((float)z <= (float)cz + r) {

                int y = cy - (int)r;

                while (true) {

                    float f = y;
                    float f2 = (float)cy + r;

                    if (!(f < f2)) break;

                    double dist = (cx - x) * (cx - x) + (cz - z) * (cz - z) + ((cy - y) * (cy - y));

                    if (dist < (double) (r * r)) {
                        BlockPos l = new BlockPos(x, y, z);
                        circleBlocks.add(l);
                    }
                    ++y;
                }
                ++z;
            }
            ++x;
        }
        return circleBlocks;
    }
}