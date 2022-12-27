package me.mioclient.mod.modules.impl.combat;

import me.mioclient.api.events.impl.PacketEvent;
import me.mioclient.api.managers.Managers;
import me.mioclient.api.util.world.InventoryUtil;
import me.mioclient.asm.accessors.ICPacketPlayer;
import me.mioclient.mod.modules.Category;
import me.mioclient.mod.modules.Module;
import me.mioclient.mod.modules.settings.Setting;
import net.minecraft.init.Items;
import net.minecraft.network.Packet;
import net.minecraft.network.play.client.CPacketPlayer;
import net.minecraft.network.play.client.CPacketPlayerDigging;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityShulkerBox;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class AntiRegear extends Module {

    private final Setting<Integer> range =
            add(new Setting<>("Range", 5, 0, 8));
    private final Setting<Boolean> autoSwap =
            add(new Setting<>("AutoSwap", true));
    private final Setting<Boolean> rotate =
            add(new Setting<>("Rotate", true));

    private float yaw, pitch;

    public AntiRegear() {
        super("AntiRegear", "Shulker nuker.", Category.COMBAT, true);
    }

    @Override
    public void onDisable() {
        if (rotate.getValue()) {
            yaw = mc.player.rotationYaw;
            pitch = mc.player.rotationPitch;
        }
    }

    @Override
    public void onUpdate() {
        
        if (getBlock() != null) {
            int oldSlot = mc.player.inventory.currentItem;

            if (autoSwap.getValue() && InventoryUtil.findItemInHotbar(Items.DIAMOND_PICKAXE) != -1) {
                mc.player.inventory.currentItem = InventoryUtil.findItemInHotbar(Items.DIAMOND_PICKAXE);
            }
            
            if (rotate.getValue()) {

                Vec3d vec = new Vec3d(getBlock().getPos().getX() + .5,
                        getBlock().getPos().getY() - 1,
                        getBlock().getPos().getZ() + .5);
                
                float[] rotations = Managers.ROTATIONS.getAngle(vec);

                yaw = rotations[0];
                pitch = rotations[1];
            }
            
            mc.player.connection.sendPacket(new CPacketPlayerDigging(CPacketPlayerDigging.Action.START_DESTROY_BLOCK, getBlock().getPos(), EnumFacing.SOUTH));
            mc.player.connection.sendPacket(new CPacketPlayerDigging(CPacketPlayerDigging.Action.STOP_DESTROY_BLOCK, getBlock().getPos(), EnumFacing.SOUTH));
            
            mc.player.inventory.currentItem = oldSlot;
        }

        if (rotate.getValue() && getBlock() == null) {
            yaw = mc.player.rotationYaw;
            pitch = mc.player.rotationPitch;    
        }
    }

    @SubscribeEvent
    public void onPacketSend(PacketEvent.Send event) {
        if (rotate.getValue()) {
            Packet packet = event.getPacket();

            if (packet instanceof CPacketPlayer) {
                ((ICPacketPlayer) packet).setYaw(yaw);
                ((ICPacketPlayer) packet).setPitch(pitch);
            }
        }
    }

    private TileEntity getBlock() {
        TileEntity out = null;

        for (TileEntity entity : mc.world.loadedTileEntityList) {
            
            if (entity instanceof TileEntityShulkerBox) {
                
                if (entity.getDistanceSq(mc.player.posX, mc.player.posY, mc.player.posZ) <= (range.getValue() * range.getValue())) {
                    out = entity;
                }
            }
        }
        return out;
    }
}