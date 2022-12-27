package me.mioclient.mod.modules.impl.player;

import me.mioclient.api.events.impl.MotionUpdateEvent;
import me.mioclient.api.events.impl.MoveEvent;
import me.mioclient.api.events.impl.Render3DEvent;
import me.mioclient.api.events.impl.UpdateWalkingPlayerEvent;
import me.mioclient.api.managers.Managers;
import me.mioclient.api.util.math.Timer;
import me.mioclient.api.util.render.RenderUtil;
import me.mioclient.asm.accessors.IEntityPlayerSP;
import me.mioclient.mod.modules.Category;
import me.mioclient.mod.modules.Module;
import me.mioclient.mod.modules.impl.client.ClickGui;
import me.mioclient.mod.modules.settings.Setting;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.client.CPacketEntityAction;
import net.minecraft.network.play.client.CPacketHeldItemChange;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.awt.*;

public class Scaffold extends Module {

    private final Setting<Boolean> tower =
            add(new Setting<>("Tower", true));
    private final Setting<Boolean> stopMotion =
            add(new Setting<>("StopMotion", true));
    private final Setting<Boolean> rotate =
            add(new Setting<>("Rotate", true));
    private final Setting<Boolean> autoSwitch =
            add(new Setting<>("AutoSwap", true));
    private final Setting<Boolean> ignoreWebs =
            add(new Setting<>("IgnoreWebs", true));
    private final Setting<Boolean> ignoreEChests =
            add(new Setting<>("IgnoreEChests", true));

    private final Setting<Boolean> render =
            add(new Setting<>("Render", true).setParent());
    private final Setting<Boolean> box =
            add(new Setting<>("Box", true, v -> render.isOpen()));
    private final Setting<Boolean> line =
            add(new Setting<>("Line", true, v -> render.isOpen()));
    
    private BlockPosWithFacing current;

    private final Timer timer = new Timer();

    public Scaffold() {
        super("Scaffold", "Block fly.", Category.PLAYER, true);
    }

    @Override
    public String getInfo() {
        return String.valueOf(getValidBlocks());
    }

    @Override
    public void onRender3D(Render3DEvent event) {
        if (render.getValue() && current != null) {
            GlStateManager.pushMatrix();

            RenderUtil.drawBoxESP(
                    current.pos,
                    ClickGui.INSTANCE.rainbow.getValue() ? Managers.COLORS.getRainbow() : Managers.COLORS.getCurrent(),
                    true,
                    new Color(255, 255, 255, 255),
                    0.9f,
                    line.getValue(),
                    box.getValue(),
                    80,
                    true,
                    0.0);

            GlStateManager.popMatrix();
        }
    }

    @SubscribeEvent
    public void onUpdateWalkingPlayer(UpdateWalkingPlayerEvent event) {

        if (event.getStage() == 0) {
            if (rotate.getValue() && current != null && getValidBlocks() > 0) {
                float[] rotations = getRotations(current.pos, current.facing);

                float packetYaw = ((IEntityPlayerSP) mc.player).getLastReportedYaw();
                float diff = MathHelper.wrapDegrees(rotations[0] - packetYaw);

                if (Math.abs(diff) > 180 * 0.5f) {
                    rotations[0] = (packetYaw + (diff * ((180 * 0.5f) / Math.abs(diff))));
                }

                Managers.ROTATIONS.setRotations(rotations[0], rotations[1]);
            }
        }
    }

    @SubscribeEvent
    public void onMove(MoveEvent event) {
        if (nullCheck()) return;

        if (stopMotion.getValue()) stopMotion(event);
    }

    @SubscribeEvent
    public void onMotionUpdate(MotionUpdateEvent event) {
        if (nullCheck()) return;

        block31: {
            BlockPos blockPos;
            Scaffold scaffold;
            int n;

            block37: {
                block36: {
                    block35: {
                        block34: {
                            block33: {
                                block30: {
                                    BlockPos blockPos2;

                                    block32: {
                                        block29: {
                                            block28: {
                                                block27: {
                                                    block26: {
                                                        if (getValidBlocks() <= 0) break block26;
                                                        if (Double.compare(mc.player.posY, 257.0) <= 0) break block27;
                                                    }
                                                    current = null;
                                                    return;
                                                }
                                                if (getValidBlocks() <= 0) break block28;
                                                if (autoSwitch.getValue()) break block29;
                                                if (mc.player.getHeldItemMainhand().getItem() instanceof ItemBlock) break block29;
                                            }
                                            return;
                                        }
                                        if (event.stage != 0) break block30;
                                        current = null;

                                        if (mc.player.isSneaking()) break block31;

                                        int n2 = getBlockSlot();
                                        if (n2 == -1) break block31;
                                        Item item = mc.player.inventory.getStackInSlot(n2).getItem();

                                        if (!(item instanceof ItemBlock)) break block31;

                                        Block block = ((ItemBlock)item).getBlock();
                                        boolean full = block.getDefaultState().isFullBlock();

                                        double offset = full ? 1.0 : 0.01;
                                        blockPos2 = new BlockPos(mc.player.posX, mc.player.posY - offset, mc.player.posZ);

                                        if (!mc.world.getBlockState(blockPos2).getMaterial().isReplaceable()) break block31;

                                        if (full) break block32;

                                        if (!blockCheck(n2)) break block31;
                                    }
                                    Scaffold scaffold2 = this;
                                    scaffold2.current = extendNeighbours(blockPos2);

                                    if (scaffold2.current != null) {

                                        if (rotate.getValue()) {
                                            float[] rotations = getRotations(current.pos, current.facing);
                                            event.rotationYaw = rotations[0];
                                            event.rotationPitch = rotations[1];
                                            return;
                                        }
                                    }
                                    break block31;
                                }

                                if (current == null) break block31;

                                n = mc.player.inventory.currentItem;

                                if (!(mc.player.getHeldItemMainhand().getItem() instanceof ItemBlock)) break block33;

                                if (isBlockValid(((ItemBlock)mc.player.getHeldItemMainhand().getItem()).getBlock())) break block34;
                            }

                            if (autoSwitch.getValue()) {
                                int n3 = getBlockSlot();
                                if (n3 != -1) {
                                    mc.player.inventory.currentItem = n3;
                                    mc.player.connection.sendPacket(new CPacketHeldItemChange(mc.player.inventory.currentItem));
                                }
                            }
                        }

                        if (!mc.player.movementInput.jump) break block35;

                        if (mc.player.moveForward != 0.0f) break block35;

                        if (mc.player.moveStrafing != 0.0f) break block35;

                        if (!tower.getValue()) break block35;

                        mc.player.setVelocity(0.0, 0.42, 0.0);

                        if (!timer.passed(1500)) break block36;

                        mc.player.motionY = -0.28;

                        scaffold = this;

                        timer.reset();
                        break block37;
                    }
                    timer.reset();
                }
                scaffold = this;
            }
            BlockPos blockPos3 = blockPos = scaffold.current.pos;

            boolean shouldSneak = mc.world.getBlockState(blockPos).getBlock().onBlockActivated(mc.world, blockPos3, mc.world.getBlockState(blockPos3), mc.player, EnumHand.MAIN_HAND, EnumFacing.DOWN, 0.0f, 0.0f, 0.0f);

            if (shouldSneak) {
                mc.player.connection.sendPacket(new CPacketEntityAction(mc.player, CPacketEntityAction.Action.START_SNEAKING));
            }

            mc.playerController.processRightClickBlock(mc.player, mc.world, blockPos, current.facing, new Vec3d((double)blockPos.getX() + Math.random(), mc.world.getBlockState(blockPos).getSelectedBoundingBox(mc.world, blockPos).maxY - 0.01, (double)blockPos.getZ() + Math.random()), EnumHand.MAIN_HAND);
            mc.player.swingArm(EnumHand.MAIN_HAND);

            if (shouldSneak) {
                mc.player.connection.sendPacket(new CPacketEntityAction(mc.player, CPacketEntityAction.Action.STOP_SNEAKING));
            }

            mc.player.inventory.currentItem = n;
            mc.player.connection.sendPacket(new CPacketHeldItemChange(mc.player.inventory.currentItem));
        }
    }

    private boolean isOffsetBBEmpty(double x, double y, double z) {
        return mc.world.getCollisionBoxes(mc.player, mc.player.getEntityBoundingBox().offset(x, y, z)).isEmpty();
    }

    private void stopMotion(MoveEvent event) {
        double x = event.motionX;
        double y = event.motionY;
        double z = event.motionZ;

        if (mc.player.onGround && !mc.player.noClip) {

            double increment;
            for (increment = 0.05D; x != 0.0D && isOffsetBBEmpty(x, -2, 0.0D);) {
                if (x < increment && x >= -increment) {
                    x = 0.0D;
                } else if (x > 0.0D) {
                    x -= increment;
                } else {
                    x += increment;
                }
            }

            while (z != 0.0D && isOffsetBBEmpty(0.0D, -2, z)) {
                if (z < increment && z >= -increment) {
                    z = 0.0D;
                } else if (z > 0.0D) {
                    z -= increment;
                } else {
                    z += increment;
                }
            }

            while (x != 0.0D && z != 0.0D && isOffsetBBEmpty(x, -2, z)) {
                if (x < increment && x >= -increment) {
                    x = 0.0D;
                } else if (x > 0.0D) {
                    x -= increment;
                } else {
                    x += increment;
                }
                if (z < increment && z >= -increment) {
                    z = 0.0D;
                } else if (z > 0.0D) {
                    z -= increment;
                } else {
                    z += increment;
                }
            }
        }
        event.motionX = x;
        event.motionY = y;
        event.motionZ = z;
    }

    private boolean isBlockValid(Block block) {
        return block.getDefaultState().getMaterial().isSolid();
    }

    private BlockPosWithFacing checkForNeighbours(BlockPos pos) {
        if (isBlockValid(mc.world.getBlockState(pos.add(0, -1, 0)).getBlock()))
            return new BlockPosWithFacing(pos.add(0, -1, 0), EnumFacing.UP);
        else if (isBlockValid(mc.world.getBlockState(pos.add(-1, 0, 0)).getBlock()))
            return new BlockPosWithFacing(pos.add(-1, 0, 0), EnumFacing.EAST);
        else if (isBlockValid(mc.world.getBlockState(pos.add(1, 0, 0)).getBlock()))
            return new BlockPosWithFacing(pos.add(1, 0, 0), EnumFacing.WEST);
        else if (isBlockValid(mc.world.getBlockState(pos.add(0, 0, 1)).getBlock()))
            return new BlockPosWithFacing(pos.add(0, 0, 1), EnumFacing.NORTH);
        else if (isBlockValid(mc.world.getBlockState(pos.add(0, 0, -1)).getBlock()))
            return new BlockPosWithFacing(pos.add(0, 0, -1), EnumFacing.SOUTH);

        return null;
    }

    private BlockPosWithFacing extendNeighbours(BlockPos pos) {
        BlockPosWithFacing extended;

        extended = checkForNeighbours(pos);
        if (extended != null) return extended;

        extended = checkForNeighbours(pos.add(-1, 0, 0));
        if (extended != null) return extended;

        extended = checkForNeighbours(pos.add(1, 0, 0));
        if (extended != null) return extended;

        extended = checkForNeighbours(pos.add(0, 0, 1));
        if (extended != null) return extended;

        extended = checkForNeighbours(pos.add(0, 0, -1));
        if (extended != null) return extended;

        extended = checkForNeighbours(pos.add(-2, 0, 0));
        if (extended != null) return extended;

        extended = checkForNeighbours(pos.add(2, 0, 0));
        if (extended != null) return extended;

        extended = checkForNeighbours(pos.add(0, 0, 2));
        if (extended != null) return extended;

        extended = checkForNeighbours(pos.add(0, 0, -2));
        if (extended != null) return extended;

        extended = checkForNeighbours(pos.add(0, -1, 0));
        BlockPos blockPos2 = pos.add(0, -1, 0);

        if (extended != null) return extended;

        extended = checkForNeighbours(blockPos2.add(1, 0, 0));
        if (extended != null) return extended;

        extended = checkForNeighbours(blockPos2.add(-1, 0, 0));
        if (extended != null) return extended;

        extended = checkForNeighbours(blockPos2.add(0, 0, 1));
        if (extended != null) return extended;

        return checkForNeighbours(blockPos2.add(0, 0, -1));
    }

    private int getBlockSlot() {

        if (mc.player.getHeldItemMainhand().getItem() instanceof ItemBlock) {
            if (isBlockValid(((ItemBlock)mc.player.getHeldItemMainhand().getItem()).getBlock()))
                return mc.player.inventory.currentItem;
        }

        int n = 0;
        int n2 = 0;

        while (n2 < 9) {

            if (mc.player.inventory.getStackInSlot(n).getCount() != 0) {

                if (mc.player.inventory.getStackInSlot(n).getItem() instanceof ItemBlock) {

                    if (!ignoreEChests.getValue() || (ignoreEChests.getValue() && !mc.player.inventory.getStackInSlot(n).getItem().equals(Item.getItemFromBlock(Blocks.ENDER_CHEST)))) {

                        if (isBlockValid(((ItemBlock) mc.player.inventory.getStackInSlot(n).getItem()).getBlock()))
                            return n;
                    }

                    if (!ignoreWebs.getValue() || (ignoreWebs.getValue() && !mc.player.inventory.getStackInSlot(n).getItem().equals(Item.getItemFromBlock(Blocks.WEB)))) {

                        if (isBlockValid(((ItemBlock) mc.player.inventory.getStackInSlot(n).getItem()).getBlock()))
                            return n;
                    }
                }
            }

            n2 = ++n;
        }

        return -1;
    }

    private boolean blockCheck(int in) {
        Item item = mc.player.inventory.getStackInSlot(in).getItem();

        if (item instanceof ItemBlock) {
            Vec3d vec3d = mc.player.getPositionVector();
            Block block = ((ItemBlock)item).getBlock();

            return mc.world.rayTraceBlocks(vec3d, vec3d.add(0.0, -block.getDefaultState().getSelectedBoundingBox(mc.world, BlockPos.ORIGIN).maxY, 0.0), false, true, false) == null;
        }

        return false;
    }

    private int getValidBlocks() {
        int n = 36;
        int n2 = 0;

        while (n < 45) {

            if (mc.player.inventoryContainer.getSlot(n).getHasStack()) {

                ItemStack itemStack = mc.player.inventoryContainer.getSlot(n).getStack();
                if (itemStack.getItem() instanceof ItemBlock) {

                    if (isBlockValid(((ItemBlock) itemStack.getItem()).getBlock())) {
                        n2 += itemStack.getCount();
                    }
                }
            }
            n++;
        }
        return n2;
    }

    private Vec3d getPositionEyes() {
        return new Vec3d(mc.player.posX, mc.player.posY + mc.player.getEyeHeight(), mc.player.posZ);
    }

    private float[] getRotations(BlockPos pos, EnumFacing facing) {
        Vec3d vec3d = new Vec3d((double)pos.getX() + 0.5, mc.world.getBlockState(pos).getSelectedBoundingBox(mc.world, pos).maxY - 0.01, (double)pos.getZ() + 0.5);
        vec3d = vec3d.add(new Vec3d(facing.getDirectionVec()).scale(0.5));

        Vec3d eyes = getPositionEyes();

        double d = vec3d.x - eyes.x;
        double d2 = vec3d.y - eyes.y;
        double d3 = vec3d.z - eyes.z;
        double d6 = Math.sqrt(d * d + d3 * d3);

        float f = (float)(Math.toDegrees(Math.atan2(d3, d)) - 90.0f);
        float f2 = (float)(-Math.toDegrees(Math.atan2(d2, d6)));

        float[] ret = new float[2];
        ret[0] = mc.player.rotationYaw + MathHelper.wrapDegrees((f - mc.player.rotationYaw));
        ret[1] = mc.player.rotationPitch + MathHelper.wrapDegrees((f2 - mc.player.rotationPitch));

        return ret;
    }

    public static class BlockPosWithFacing {

        public BlockPos pos;
        public EnumFacing facing;

        public BlockPosWithFacing(BlockPos pos, EnumFacing facing) {
            this.pos = pos;
            this.facing = facing;
        }
    }
}