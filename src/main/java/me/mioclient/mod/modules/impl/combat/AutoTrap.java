package me.mioclient.mod.modules.impl.combat;

import com.mojang.realmsclient.gui.ChatFormatting;
import me.mioclient.api.events.impl.PacketEvent;
import me.mioclient.api.events.impl.Render3DEvent;
import me.mioclient.api.managers.Managers;
import me.mioclient.api.util.entity.EntityUtil;
import me.mioclient.api.util.interact.BlockUtil;
import me.mioclient.api.util.math.MathUtil;
import me.mioclient.api.util.math.Timer;
import me.mioclient.api.util.render.RenderUtil;
import me.mioclient.api.util.world.InventoryUtil;
import me.mioclient.mod.commands.Command;
import me.mioclient.mod.modules.Category;
import me.mioclient.mod.modules.Module;
import me.mioclient.mod.modules.settings.Setting;
import net.minecraft.block.BlockEnderChest;
import net.minecraft.block.BlockObsidian;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.network.play.server.SPacketBlockChange;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.awt.*;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class AutoTrap extends Module {

    public static AutoTrap INSTANCE;

    private final Setting<Integer> tickDelay =
            add(new Setting<>("TickDelay", 50, 0, 250));
    private final Setting<Integer> blocksPerTick =
            add(new Setting<>("BPT", 2, 1, 30));
    private final Setting<Boolean> rotate =
            add(new Setting<>("Rotate", true));
    private final Setting<Boolean> raytrace =
            add(new Setting<>("Raytrace", false));
    private final Setting<Boolean> extraTop =
            add(new Setting<>("Extra", false));
    private final Setting<Boolean> antiStep =
            add(new Setting<>("AntiStep", false));
    private final Setting<Boolean> legs =
            add(new Setting<>("Legs", false));
    private final Setting<Boolean> packet =
            add(new Setting<>("Packet", false));
    private final Setting<Boolean> clean =
            add(new Setting<>("Clean", true));
    private final Setting<Boolean> eChests =
            add(new Setting<>("EChests", true));

    private final Setting<Boolean> render =
            add(new Setting<>("Render", true).setParent());
    private final Setting<Boolean> box =
            add(new Setting<>("Box", true, v -> render.isOpen()));
    private final Setting<Boolean> line =
            add(new Setting<>("Line", true, v -> render.isOpen()));

    public static boolean isPlacing;

    private final Timer delayTimer = new Timer();
    private final Map<BlockPos, Integer> retryMap = new HashMap<>();

    private final Map<BlockPos, Long> renderBlocks = new ConcurrentHashMap<>();
    private final Timer renderTimer = new Timer();

    private final Timer retryTimer = new Timer();

    private EntityPlayer target;

    private boolean didPlace;
    private boolean isSneaking;
    private int lastHotbarSlot;
    private int placements;

    private BlockPos startPos;

    public AutoTrap() {
        super("AutoTrap", "Traps other players", Category.COMBAT, true);
        INSTANCE = this;
    }

    @Override
    public void onEnable() {
        if (fullNullCheck()) return;

        startPos = EntityUtil.getRoundedPos(mc.player);
        lastHotbarSlot = mc.player.inventory.currentItem;
        retryMap.clear();
    }

    @Override
    public void onDisable() {
        if (fullNullCheck()) return;

        isPlacing = false;
        isSneaking = EntityUtil.stopSneaking(isSneaking);

        Managers.ROTATIONS.resetRotationsPacket();
    }

    @Override
    public void onTick() {
        if (fullNullCheck()) return;

        doTrap();
    }

    @Override
    public String getInfo() {
        if (target != null) {
            return target.getName();
        }
        return null;
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

    private void doTrap() {
        if (nullCheck() || check()) return;

        for (Vec3d pos : getOffsets()) {

            BlockPos currentPos = new BlockPos(pos);

            int obbySlot = InventoryUtil.findHotbarBlock(BlockObsidian.class);
            int eChestSlot = InventoryUtil.findHotbarBlock(BlockEnderChest.class);
            int oldSlot = mc.player.inventory.currentItem;

            if (obbySlot == -1 && eChestSlot == -1) {
                Command.sendMessage("[" + getName() + "] " + ChatFormatting.RED + "No obi in hotbar. disabling...");
                disable();
            }

            if (BlockUtil.getPlaceAbility(currentPos, raytrace.getValue()) == 1 && (retryMap.get(currentPos) == null || retryMap.get(currentPos) < 4)) {
                placeBlock(currentPos, ((obbySlot == -1 && eChests.getValue()) ? eChestSlot : obbySlot), oldSlot);

                retryMap.put(currentPos, retryMap.get(currentPos) == null ? 1 : retryMap.get(currentPos) + 1);
                retryTimer.reset();
                continue;
            }

            if (BlockUtil.getPlaceAbility(currentPos, raytrace.getValue()) != 3) continue;

            renderBlocks.put(currentPos, System.currentTimeMillis());
            placeBlock(currentPos, ((obbySlot == -1 && eChests.getValue()) ? eChestSlot : obbySlot), oldSlot);
        }

        if (didPlace) {
            delayTimer.reset();
        }
    }

    private List<Vec3d> getOffsets() {
        boolean onEChest = BlockUtil.getBlock(new BlockPos(target.getPositionVector())) == Blocks.ENDER_CHEST && target.posY - (double) ((int) target.posY) > 0.5;

        List<Vec3d> vec = EntityUtil.getTrapOffsetList(target.getPositionVector().add(0, onEChest ? 1 : 0, 0), extraTop.getValue(), antiStep.getValue(), legs.getValue(), false, false, raytrace.getValue());
        
        vec.sort((vec3d, vec3d2) -> Double.compare(mc.player.getDistanceSq(vec3d2.x, vec3d2.y, vec3d2.z), mc.player.getDistanceSq(vec3d.x, vec3d.y, vec3d.z)));
        vec.sort(Comparator.comparingDouble(vec3d -> vec3d.y));

        return vec;
    }

    private boolean check() {
        isPlacing = false;
        didPlace = false;
        placements = 0;

        int obbySlot = InventoryUtil.findHotbarBlock(BlockObsidian.class);
        if (isOff()) {
            return true;
        }
        if (!startPos.equals(EntityUtil.getRoundedPos(mc.player))) {
            disable();
            return true;
        }
        if (retryTimer.passedMs(2000L)) {
            retryMap.clear();
            retryTimer.reset();
        }
        if (mc.player.inventory.currentItem != lastHotbarSlot && mc.player.inventory.currentItem != obbySlot) {
            lastHotbarSlot = mc.player.inventory.currentItem;
        }
        isSneaking = EntityUtil.stopSneaking(isSneaking);
        target = getTarget(10.0, true);
        return target == null || !delayTimer.passedMs(tickDelay.getValue());
    }

    private EntityPlayer getTarget(double range, boolean trapped) {
        EntityPlayer target = null;
        double distance = Math.pow(range, 2.0) + 1.0;
        for (EntityPlayer player : mc.world.playerEntities) {
            if (!EntityUtil.isValid(player, range) || trapped && EntityUtil.isTrapped(player, extraTop.getValue(), antiStep.getValue(), false, false, false) || Managers.SPEED.getPlayerSpeed(player) > 10.0)
                continue;
            if (target == null) {
                target = player;
                distance = mc.player.getDistanceSq(player);
                continue;
            }
            if (!(mc.player.getDistanceSq(player) < distance)) continue;
            target = player;
            distance = mc.player.getDistanceSq(player);
        }
        return target;
    }

    private void placeBlock(BlockPos pos, int blockSlot, int oldSlot) {
        if (placements < blocksPerTick.getValue() && mc.player.getDistanceSq(pos) <= MathUtil.square(5.0)) {
            isPlacing = true;

            if (BlockUtil.checkForEntities(pos)) return;

            mc.player.inventory.currentItem = blockSlot;
            mc.playerController.updateController();

            renderBlocks.put(pos, System.currentTimeMillis());

            Managers.INTERACTIONS.placeBlock(pos, rotate.getValue(), packet.getValue(), clean.getValue());

            mc.player.inventory.currentItem = oldSlot;
            mc.playerController.updateController();
        }

        didPlace = true;
        ++placements;
    }
}
