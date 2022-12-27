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
import net.minecraft.block.BlockWeb;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.network.play.server.SPacketBlockChange;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.awt.*;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class WebTrap extends Module {

    public static boolean isPlacing;

    private final Setting<Integer> delay =
            add(new Setting<>("TickDelay", 50, 0, 250));
    private final Setting<Integer> blocksPerPlace =
            add(new Setting<>("BPT", 1, 1, 30));
    private final Setting<Boolean> packet =
            add(new Setting<>("Packet", false));
    private final Setting<Boolean> disable =
            add(new Setting<>("AutoDisable", false));
    private final Setting<Boolean> rotate =
            add(new Setting<>("Rotate", true));
    private final Setting<Boolean> raytrace =
            add(new Setting<>("Raytrace", false));
    private final Setting<Boolean> feet =
            add(new Setting<>("Feet", true));
    private final Setting<Boolean> face =
            add(new Setting<>("Face", false));

    private final Setting<Boolean> render =
            add(new Setting<>("Render", true).setParent());
    private final Setting<Boolean> box =
            add(new Setting<>("Box", true, v -> render.isOpen()));
    private final Setting<Boolean> line =
            add(new Setting<>("Line", true, v -> render.isOpen()));

    private final Timer timer = new Timer();

    private EntityPlayer target;

    private boolean didPlace;
    private boolean isSneaking;
    private int lastHotbarSlot;
    private int placements;

    private BlockPos startPos;

    private final Map<BlockPos, Long> renderBlocks = new ConcurrentHashMap<>();
    private final Timer renderTimer = new Timer();

    public WebTrap() {
        super("WebTrap", "Traps other players in webs", Category.COMBAT, true);
    }

    @Override
    public void onEnable() {
        if (fullNullCheck()) {
            return;
        }
        startPos = EntityUtil.getRoundedPos(mc.player);
        lastHotbarSlot = mc.player.inventory.currentItem;
    }

    @Override
    public void onDisable() {
        isPlacing = false;
        isSneaking = EntityUtil.stopSneaking(isSneaking);
        doSwap(lastHotbarSlot);
    }

    @Override
    public void onTick() {
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
        if (check()) {
            return;
        }

        doWebTrap();

        if (didPlace) {
            timer.reset();
        }

    }

    private void doWebTrap() {
        List<Vec3d> placeTargets = getPlacements();
        placeList(placeTargets);
    }

    private List<Vec3d> getPlacements() {
        ArrayList<Vec3d> list = new ArrayList<>();
        Vec3d baseVec = target.getPositionVector();
        if (feet.getValue()) {
            list.add(baseVec);
        }
        if (face.getValue()) {
            list.add(baseVec.add(0.0, 1.0, 0.0));
        }
        return list;
    }

    private void placeList(List<Vec3d> list) {
        list.sort((vec3d, vec3d2) -> Double.compare(mc.player.getDistanceSq(vec3d2.x, vec3d2.y, vec3d2.z), mc.player.getDistanceSq(vec3d.x, vec3d.y, vec3d.z)));
        list.sort(Comparator.comparingDouble(vec3d -> vec3d.y));
        for (Vec3d vec3d3 : list) {
            BlockPos position = new BlockPos(vec3d3);
            int placeability = BlockUtil.getPlaceAbility(position, raytrace.getValue());
            if (placeability != 3 && placeability != 1) continue;

            int webSlot = InventoryUtil.findHotbarBlock(BlockWeb.class);

            doSwap(webSlot);

            renderBlocks.put(position, System.currentTimeMillis());
            placeBlock(position);

            doSwap(lastHotbarSlot);
        }
    }

    private boolean check() {
        isPlacing = false;
        didPlace = false;
        placements = 0;

        int webSlot = InventoryUtil.findHotbarBlock(BlockWeb.class);

        if (isOff()) {
            return true;
        }
        if (disable.getValue() && !startPos.equals(EntityUtil.getRoundedPos(mc.player))) {
            disable();
            return true;
        }
        if (webSlot == -1) {
            Command.sendMessage("[" + getName() + "] " + ChatFormatting.RED + "No webs in hotbar. disabling...");
            toggle();
            return true;
        }
        if (mc.player.inventory.currentItem != lastHotbarSlot && mc.player.inventory.currentItem != webSlot) {
            lastHotbarSlot = mc.player.inventory.currentItem;
        }
        isSneaking = EntityUtil.stopSneaking(isSneaking);
        target = getTarget(10.0);
        return target == null || !timer.passedMs(delay.getValue());
    }

    private EntityPlayer getTarget(double range) {
        EntityPlayer target = null;
        double distance = Math.pow(range, 2.0) + 1.0;

        for (EntityPlayer player : mc.world.playerEntities) {

            if (!EntityUtil.isValid(player, range) || player.isInWeb || Managers.SPEED.getPlayerSpeed(player) > 30.0) continue;

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

    private void placeBlock(BlockPos pos) {
        if (placements < blocksPerPlace.getValue() && mc.player.getDistanceSq(pos) <= MathUtil.square(6.0)) {
            isPlacing = true;

            Managers.INTERACTIONS.placeBlock(pos, rotate.getValue(), packet.getValue(), false, true);

            didPlace = true;
            ++placements;
        }
    }
    
    private void doSwap(int slot) {
        mc.player.inventory.currentItem = slot;

        mc.playerController.updateController();
    }
}

