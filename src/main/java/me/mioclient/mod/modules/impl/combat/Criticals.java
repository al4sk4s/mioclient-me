package me.mioclient.mod.modules.impl.combat;

import me.mioclient.api.events.impl.PacketEvent;
import me.mioclient.api.managers.Managers;
import me.mioclient.mod.modules.Category;
import me.mioclient.mod.modules.Module;
import me.mioclient.mod.modules.settings.Setting;
import net.minecraft.block.BlockWeb;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityEnderCrystal;
import net.minecraft.network.play.client.CPacketPlayer;
import net.minecraft.network.play.client.CPacketUseEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

/**
 * @author t.me/asphyxia1337
 */

public class Criticals extends Module {

    private final Setting<Mode> mode = 
            add(new Setting<>("Mode", Mode.PACKET));
    private final Setting<Boolean> webs = 
            add(new Setting<>("Webs", false, v -> mode.getValue() == Mode.NCP));
    private final Setting<Boolean> onlyAura = 
            add(new Setting<>("OnlyAura", false));
    private final Setting<Boolean> vehicles = 
            add(new Setting<>("Vehicles", true).setParent());

    public Criticals() {
        super("Criticals", "Always do as much damage as you can!", Category.COMBAT, true);
    }

    private enum Mode {
        PACKET,
        NCP
    }

    @Override
    public String getInfo() {
        return mode.getValue() == Mode.NCP ? String.valueOf(mode.getValue()) : Managers.TEXT.normalizeCases(mode.getValue());
    }

    @SubscribeEvent
    public void onPacketSend(PacketEvent.Send event) {
        if (nullCheck() || fullNullCheck()) return;
        
        if (Aura.target == null && onlyAura.getValue()) return;
        
        if (event.getPacket() instanceof CPacketUseEntity && ((CPacketUseEntity) event.getPacket()).getAction() == CPacketUseEntity.Action.ATTACK && mc.player.onGround && mc.player.collidedVertically && !mc.player.isInLava() && !mc.player.isInWater()) {

            Entity attackedEntity = ((CPacketUseEntity) event.getPacket()).getEntityFromWorld(mc.world);

            if (attackedEntity instanceof EntityEnderCrystal || attackedEntity == null) return;

            switch (mode.getValue()) {

                case PACKET:
                    mc.player.connection.sendPacket(new CPacketPlayer.Position(mc.player.posX, mc.player.posY + 0.0625101D, mc.player.posZ, false));
                    mc.player.connection.sendPacket(new CPacketPlayer.Position(mc.player.posX, mc.player.posY, mc.player.posZ, false));
                    mc.player.connection.sendPacket(new CPacketPlayer.Position(mc.player.posX, mc.player.posY + 0.0125D, mc.player.posZ, false));
                    mc.player.connection.sendPacket(new CPacketPlayer.Position(mc.player.posX, mc.player.posY, mc.player.posZ, false));
                    break;
                    
                case NCP:
                    if (webs.getValue()) {
                        
                        if (mc.world.getBlockState(new BlockPos(mc.player)).getBlock() instanceof BlockWeb) {
                            mc.player.connection.sendPacket(new CPacketPlayer.Position(mc.player.posX, mc.player.posY + 0.0625101D, mc.player.posZ, false));
                            mc.player.connection.sendPacket(new CPacketPlayer.Position(mc.player.posX, mc.player.posY, mc.player.posZ, false));
                            mc.player.connection.sendPacket(new CPacketPlayer.Position(mc.player.posX, mc.player.posY + 0.0125D, mc.player.posZ, false));
                            mc.player.connection.sendPacket(new CPacketPlayer.Position(mc.player.posX, mc.player.posY, mc.player.posZ, false));
                            break;
                        }
                    }
                    
                    mc.player.connection.sendPacket(new CPacketPlayer.Position(mc.player.posX, mc.player.posY + 0.11D, mc.player.posZ, false));
                    mc.player.connection.sendPacket(new CPacketPlayer.Position(mc.player.posX, mc.player.posY + 0.1100013579D, mc.player.posZ, false));
                    mc.player.connection.sendPacket(new CPacketPlayer.Position(mc.player.posX, mc.player.posY + 0.0000013579D, mc.player.posZ, false));
                    break;
            }

            mc.player.onCriticalHit(attackedEntity);
        }
    }
}
