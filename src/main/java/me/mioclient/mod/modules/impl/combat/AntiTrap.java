package me.mioclient.mod.modules.impl.combat;

import me.mioclient.api.events.impl.UpdateWalkingPlayerEvent;
import me.mioclient.api.managers.Managers;
import me.mioclient.api.util.entity.EntityUtil;
import me.mioclient.api.util.interact.BlockUtil;
import me.mioclient.api.util.math.MathUtil;
import me.mioclient.api.util.math.Timer;
import me.mioclient.api.util.world.InventoryUtil;
import me.mioclient.mod.modules.Category;
import me.mioclient.mod.modules.Module;
import me.mioclient.mod.modules.settings.Setting;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.ItemEndCrystal;
import net.minecraft.network.play.client.CPacketPlayer;
import net.minecraft.network.play.client.CPacketPlayerTryUseItemOnBlock;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.*;

public class AntiTrap extends Module {

    private final Setting<Rotate> rotate =
            add(new Setting<>("Rotate", Rotate.NORMAL));
    private final Setting<Integer> coolDown =
            add(new Setting<>("CoolDown", 400, 0, 1000));
    private final Setting<Boolean> sortY =
            add(new Setting<>("SortY", true));

    public static Set<BlockPos> placedPos = new HashSet<>();

    private final Vec3d[] surroundTargets = new Vec3d[]{new Vec3d(1.0, 0.0, 0.0), new Vec3d(0.0, 0.0, 1.0), new Vec3d(-1.0, 0.0, 0.0), new Vec3d(0.0, 0.0, -1.0), new Vec3d(1.0, 0.0, -1.0), new Vec3d(1.0, 0.0, 1.0), new Vec3d(-1.0, 0.0, -1.0), new Vec3d(-1.0, 0.0, 1.0), new Vec3d(1.0, 1.0, 0.0), new Vec3d(0.0, 1.0, 1.0), new Vec3d(-1.0, 1.0, 0.0), new Vec3d(0.0, 1.0, -1.0), new Vec3d(1.0, 1.0, -1.0), new Vec3d(1.0, 1.0, 1.0), new Vec3d(-1.0, 1.0, -1.0), new Vec3d(-1.0, 1.0, 1.0)};

    private int lastHotbarSlot = -1;

    private boolean switchedItem;
    private boolean offhand;

    private final Timer timer = new Timer();

    public AntiTrap() {
        super("AntiTrap", "best useful module shout out sam", Category.COMBAT, true);
    }

    private enum Rotate {
        NONE,
        NORMAL,
        PACKET
    }

    @Override
    public void onEnable() {
        if (fullNullCheck() || !timer.passedMs(coolDown.getValue())) {
            disable();
            return;
        }
        
        lastHotbarSlot = mc.player.inventory.currentItem;
    }

    @Override
    public void onDisable() {
        if (fullNullCheck()) {
            return;
        }
        
        doSwap(lastHotbarSlot);
    }

    @SubscribeEvent
    public void onUpdateWalkingPlayer(UpdateWalkingPlayerEvent event) {
        if (!fullNullCheck() && event.getStage() == 0) {
            doAntiTrap();
        }
    }

    private void doAntiTrap() {
        
        offhand = mc.player.getHeldItemOffhand().getItem() == Items.END_CRYSTAL;
        
        if (!offhand && InventoryUtil.findHotbarBlock(ItemEndCrystal.class) == -1) {
            disable();
            return;
        }
        
        lastHotbarSlot = mc.player.inventory.currentItem;
        
        ArrayList<Vec3d> targets = new ArrayList<>();
        Collections.addAll(targets, MathUtil.convertVectors(mc.player.getPositionVector(), surroundTargets));
        EntityPlayer closestPlayer = EntityUtil.getClosestEnemy(6.0);
        
        if (closestPlayer != null) {
            
            targets.sort((vec3d, vec3d2) -> Double.compare(closestPlayer.getDistanceSq(vec3d2.x, vec3d2.y, vec3d2.z), closestPlayer.getDistanceSq(vec3d.x, vec3d.y, vec3d.z)));
            
            if (sortY.getValue()) {
                targets.sort(Comparator.comparingDouble(vec3d -> vec3d.y));
            }
        }
        
        for (Vec3d vec3d3 : targets) {

            int crystalSlot = InventoryUtil.findItemInHotbar(Items.END_CRYSTAL);

            if (crystalSlot == -1) {
                disable();
                return;
            }
            
            BlockPos pos = new BlockPos(vec3d3);
            
            if (!BlockUtil.canPlaceCrystal(pos)) continue;

            doSwap(InventoryUtil.findItemInHotbar(Items.END_CRYSTAL));

            placeCrystal(pos);

            doSwap(lastHotbarSlot);

            disable();
            break;
        }
    }

    private void placeCrystal(BlockPos pos) {
        
        boolean mainHand = mc.player.getHeldItemMainhand().getItem() == Items.END_CRYSTAL;
        
        if (!(mainHand || offhand)) {
            disable();
            return;
        }
        
        RayTraceResult result = mc.world.rayTraceBlocks(new Vec3d(mc.player.posX, mc.player.posY + (double)mc.player.getEyeHeight(), mc.player.posZ), new Vec3d((double)pos.getX() + 0.5, (double)pos.getY() - 0.5, (double)pos.getZ() + 0.5));
        EnumFacing facing = result == null || result.sideHit == null ? EnumFacing.UP : result.sideHit;
        
        float[] angle = MathUtil.calcAngle(mc.player.getPositionEyes(mc.getRenderPartialTicks()), new Vec3d(((float)pos.getX() + 0.5f), ((float)pos.getY() - 0.5f), ((float)pos.getZ() + 0.5f)));
        
        switch (rotate.getValue()) {
            
            case NONE: 
                break;
            
            case NORMAL: 
                Managers.ROTATIONS.setRotations(angle[0], angle[1]);
                break;
            
            case PACKET: 
                mc.player.connection.sendPacket(new CPacketPlayer.Rotation(angle[0], (float)MathHelper.normalizeAngle(((int)angle[1]), 360), mc.player.onGround));
                break;
        }
        
        placedPos.add(pos);
        
        mc.player.connection.sendPacket(new CPacketPlayerTryUseItemOnBlock(pos, facing, offhand ? EnumHand.OFF_HAND : EnumHand.MAIN_HAND, 0.0f, 0.0f, 0.0f));
        mc.player.swingArm(EnumHand.MAIN_HAND);
        
        timer.reset();
    }

    private void doSwap(int slot) {
        mc.player.inventory.currentItem = slot;

        mc.playerController.updateController();
    }
}