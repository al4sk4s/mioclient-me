package me.mioclient.mod.modules.impl.movement;

import me.mioclient.api.events.impl.PacketEvent;
import me.mioclient.api.managers.Managers;
import me.mioclient.api.util.math.Timer;
import me.mioclient.mod.modules.Category;
import me.mioclient.mod.modules.Module;
import me.mioclient.mod.modules.settings.Setting;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.network.play.server.SPacketPlayerPosLook;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.HashMap;
import java.util.Map;

/**
 * @author t.me/asphyxia1337
 */

public class FastFall extends Module {

    private final Setting<Mode> mode =
            add(new Setting<>("Mode", Mode.FAST));
    private final Setting<Boolean> noLag =
            add(new Setting<>("NoLag", true, v -> mode.getValue() == Mode.FAST));
    private final Setting<Integer> height =
            add(new Setting<>("Height", 10, 1, 20));

    private final Timer lagTimer = new Timer();

    private boolean useTimer;

    public FastFall() {
        super("FastFall", "Miyagi son simulator.", Category.MOVEMENT, true);
    }

    private enum Mode {
        FAST,
        STRICT
    }

    @Override
    public void onDisable() {
        Managers.TIMER.reset();
        useTimer = false;
    }

    @Override
    public String getInfo() {
        return Managers.TEXT.normalizeCases(mode.getValue());
    }

    @Override
    public void onTick() {
        if ((height.getValue() > 0 && (traceDown() > height.getValue()))
                || mc.player.isEntityInsideOpaqueBlock()
                || mc.player.isInWater()
                || mc.player.isInLava()
                || mc.player.isOnLadder()
                || !lagTimer.passedMs(1000)
                || fullNullCheck()) {

            Managers.TIMER.reset();
            return;
        }

        if (mc.player.isInWeb) return;

        if (mc.player.onGround) {

            if (mode.getValue() == Mode.FAST) {

                mc.player.motionY -= (noLag.getValue() ? 0.62f : 1);
            }
        }

        if (traceDown() != 0 && traceDown() <= height.getValue() && trace() && mc.player.onGround) {
            mc.player.motionX *= 0.05f;
            mc.player.motionZ *= 0.05f;
        }

        if (mode.getValue() == Mode.STRICT) {

            if (!mc.player.onGround) {

                if (mc.player.motionY < 0 && useTimer) {
                    Managers.TIMER.set(2.5f);
                    return;

                } else {
                    useTimer = false;
                }

            } else {
                mc.player.motionY = -0.08;
                useTimer = true;
            }
        }

        Managers.TIMER.reset();
    }

    @SubscribeEvent
    public void onPacket(PacketEvent event) {
        if (!fullNullCheck()) {
            if (event.getPacket() instanceof SPacketPlayerPosLook) {
                lagTimer.reset();
            }
        }
    }

    /**
     * traces
     * @author mrnv
     */

    private int traceDown() {
        int retval = 0;

        int y = (int)Math.round(mc.player.posY) - 1;

        for(int tracey = y; tracey >= 0; tracey--) {

            RayTraceResult trace = mc.world.rayTraceBlocks(
                    mc.player.getPositionVector(),
                    new Vec3d(mc.player.posX, tracey, mc.player.posZ),
                    false);

            if (trace != null && trace.typeOfHit == RayTraceResult.Type.BLOCK) return retval;

            retval++;
        }
        return retval;
    }

    private boolean trace() {
        AxisAlignedBB bbox = mc.player.getEntityBoundingBox();
        Vec3d basepos = bbox.getCenter();

        double minX = bbox.minX;
        double minZ = bbox.minZ;
        double maxX = bbox.maxX;
        double maxZ = bbox.maxZ;

        Map<Vec3d, Vec3d> positions = new HashMap<>();

        positions.put(
                basepos,
                new Vec3d(basepos.x, basepos.y - 1, basepos.z));

        positions.put(
                new Vec3d(minX, basepos.y, minZ),
                new Vec3d(minX, basepos.y - 1, minZ));

        positions.put(
                new Vec3d(maxX, basepos.y, minZ),
                new Vec3d(maxX, basepos.y - 1, minZ));

        positions.put(
                new Vec3d(minX, basepos.y, maxZ),
                new Vec3d(minX, basepos.y - 1, maxZ));

        positions.put(
                new Vec3d(maxX, basepos.y, maxZ),
                new Vec3d(maxX, basepos.y - 1, maxZ));

        for (Vec3d key : positions.keySet()) {

            RayTraceResult result = mc.world.rayTraceBlocks(key,positions.get(key), true);

            if (result != null && result.typeOfHit == RayTraceResult.Type.BLOCK) return false;
        }

        IBlockState state = mc.world.getBlockState(new BlockPos(mc.player.posX, mc.player.posY - 1, mc.player.posZ));

        return state.getBlock() == Blocks.AIR;
    }
}