package me.mioclient.mod.modules.impl.combat;

import com.mojang.realmsclient.gui.ChatFormatting;
import me.mioclient.api.managers.Managers;
import me.mioclient.api.util.entity.EntityUtil;
import me.mioclient.api.util.interact.BlockUtil;
import me.mioclient.mod.commands.Command;
import me.mioclient.mod.modules.Category;
import me.mioclient.mod.modules.Module;
import me.mioclient.mod.modules.settings.Setting;
import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.client.CPacketEntityAction;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

import java.util.Arrays;
import java.util.List;

public class SelfWeb extends Module {

    private final Setting<Boolean> rotate =
            add(new Setting<>("Rotate", true));
    private final Setting<Boolean> smart =
            add(new Setting<>("Smart", false).setParent());
    private final Setting<Integer> enemyRange =
            add(new Setting<>("EnemyRange", 4, 0, 8, v -> smart.isOpen()));

    public SelfWeb() {
        super("SelfWeb", "Places webs at your feet", Category.COMBAT);
    }

    private int newSlot = -1;

    private boolean sneak;

    public final List<Block> blackList = Arrays.asList(Blocks.ENDER_CHEST, Blocks.CHEST, Blocks.TRAPPED_CHEST, Blocks.CRAFTING_TABLE, Blocks.ANVIL, Blocks.BREWING_STAND, Blocks.HOPPER, Blocks.DROPPER, Blocks.DISPENSER);

    @Override
    public void onEnable() {
        if (mc.player != null) {

            newSlot = getHotbarItem();

            if (newSlot == -1) {
                Command.sendMessage("[" + getName() + "] " + ChatFormatting.RED + "No Webs in hotbar. disabling...");
                toggle();
            }
        }
    }

    @Override
    public void onDisable() {
        if (mc.player != null) {
            if (sneak) {
                mc.player.connection.sendPacket(new CPacketEntityAction(mc.player, CPacketEntityAction.Action.STOP_SNEAKING));
                sneak = false;
            }
        }
    }

    @Override
    public void onUpdate() {
        if (fullNullCheck()) return;

        if (smart.getValue()) {

            EntityPlayer target = getClosestTarget();

            if (target == null) return;
            if (Managers.FRIENDS.isFriend(target.getName())) return;

            if (mc.player.getDistance(target) < enemyRange.getValue() && isSafe()) {
                int last_slot = mc.player.inventory.currentItem;
                mc.player.inventory.currentItem = newSlot;
                mc.playerController.updateController();
                placeBlock(getFloorPos());
                mc.player.inventory.currentItem = last_slot;
            }

        } else {
            int last_slot = mc.player.inventory.currentItem;
            mc.player.inventory.currentItem = newSlot;
            mc.playerController.updateController();
            placeBlock(getFloorPos());
            mc.player.inventory.currentItem = last_slot;
            disable();
        }
    }

    private EntityPlayer getClosestTarget() {
        if (mc.world.playerEntities.isEmpty()) return null;

        EntityPlayer closestTarget = null;

        for (EntityPlayer target : mc.world.playerEntities) {
            if (target == mc.player)
                continue;

            if (!EntityUtil.isLiving(target))
                continue;

            if (target.getHealth() <= 0.0f)
                continue;

            if (closestTarget != null)
                if (mc.player.getDistance(target) > mc.player.getDistance(closestTarget))
                    continue;

            closestTarget = target;
        }

        return closestTarget;
    }

    private int getHotbarItem() {
        for (int i = 0; i < 9; ++i) {
            ItemStack stack = mc.player.inventory.getStackInSlot(i);
            if (stack.getItem() == Item.getItemById(30)) {
                return i;
            }
        }
        return -1;
    }

    private boolean isSafe() {
        BlockPos player_block = getFloorPos();

        return mc.world.getBlockState(player_block.east()).getBlock() != Blocks.AIR
                && mc.world.getBlockState(player_block.west()).getBlock() != Blocks.AIR
                && mc.world.getBlockState(player_block.north()).getBlock() != Blocks.AIR
                && mc.world.getBlockState(player_block.south()).getBlock() != Blocks.AIR
                && mc.world.getBlockState(player_block).getBlock() == Blocks.AIR;
    }

    private void placeBlock(BlockPos pos) {
        if (!mc.world.getBlockState(pos).getMaterial().isReplaceable()) {
            return;
        }

        if (!checkForNeighbours(pos)) {
            return;
        }

        for (EnumFacing side : EnumFacing.values()) {

            BlockPos neighbor = pos.offset(side);

            EnumFacing side2 = side.getOpposite();

            if (!canBeClicked(neighbor)) continue;

            if (blackList.contains(mc.world.getBlockState(neighbor).getBlock())) {
                mc.player.connection.sendPacket(new CPacketEntityAction(mc.player, CPacketEntityAction.Action.START_SNEAKING));
                sneak = true;
            }

            Vec3d hitVec = new Vec3d(neighbor).add(0.5, 0.5, 0.5).add(new Vec3d(side2.getDirectionVec()).scale(0.5));

            if (rotate.getValue()) {
                Managers.ROTATIONS.lookAtVec3dPacket(hitVec, false);
            }

            mc.playerController.processRightClickBlock(mc.player, mc.world, neighbor, side2, hitVec, EnumHand.MAIN_HAND);
            mc.player.swingArm(EnumHand.MAIN_HAND);

            return;
        }
    }

    private boolean checkForNeighbours(BlockPos blockPos) {
        if (!hasNeighbour(blockPos)) {
            for (EnumFacing side : EnumFacing.values()) {
                BlockPos neighbour = blockPos.offset(side);
                if (hasNeighbour(neighbour)) {
                    return true;
                }
            }
            return false;
        }
        return true;
    }

    private boolean hasNeighbour(BlockPos blockPos) {
        for (EnumFacing side : EnumFacing.values()) {
            BlockPos neighbour = blockPos.offset(side);
            if (!mc.world.getBlockState(neighbour).getMaterial().isReplaceable()) {
                return true;
            }
        }
        return false;
    }

    private boolean canBeClicked(BlockPos pos) {
        return BlockUtil.getBlock(pos).canCollideCheck(BlockUtil.getState(pos), false);
    }

    private BlockPos getFloorPos() {
        return new BlockPos(Math.floor(mc.player.posX), Math.floor(mc.player.posY), Math.floor(mc.player.posZ));
    }
}