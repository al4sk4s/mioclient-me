package me.mioclient.mod.modules.impl.movement;

import me.mioclient.api.events.impl.PacketEvent;
import me.mioclient.api.util.math.MathUtil;
import me.mioclient.asm.accessors.ISPacketEntityVelocity;
import me.mioclient.asm.accessors.ISPacketExplosion;
import me.mioclient.mod.modules.Category;
import me.mioclient.mod.modules.Module;
import me.mioclient.mod.modules.settings.Setting;
import net.minecraft.entity.projectile.EntityFishHook;
import net.minecraft.network.play.server.SPacketEntityVelocity;
import net.minecraft.network.play.server.SPacketExplosion;
import net.minecraftforge.client.event.PlayerSPPushOutOfBlocksEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

/**
 * @author t.me/asphyxia1337
 */

public class Velocity extends Module {

    public static Velocity INSTANCE;

    private final Setting<Float> horizontal =
            add(new Setting<>("Horizontal", 0.0f, 0.0f, 100.0f));
    private final Setting<Float> vertical =
            add(new Setting<>("Vertical", 0.0f, 0.0f, 100.0f));
    private final Setting<Boolean> blockPush =
            add(new Setting<>("BlockPush", true));

    public Velocity() {
        super("Velocity", "Cancels all the pushing your player receives.", Category.MOVEMENT, true);
        INSTANCE = this;
    }

    @Override
    public String getInfo() {
        return "H" + MathUtil.round(horizontal.getValue(), 1) + "%V" + MathUtil.round(vertical.getValue(), 1) + "%";
    }

    @SubscribeEvent
    public void onPacketReceive(PacketEvent.Receive event) {
        if (fullNullCheck()) return;

        float h = horizontal.getValue() / 100;
        float v = vertical.getValue() / 100;

        if (event.getPacket() instanceof EntityFishHook) {

            event.cancel();
        }

        if (event.getPacket() instanceof SPacketExplosion) {
            ISPacketExplosion packet = event.getPacket();

            packet.setX(packet.getX() * h);
            packet.setY(packet.getY() * v);
            packet.setZ(packet.getZ() * h);
        }

        if (event.getPacket() instanceof SPacketEntityVelocity) {
            ISPacketEntityVelocity packet = event.getPacket();

            if (packet.getEntityID() == mc.player.getEntityId()) {

                if (horizontal.getValue() == 0 && vertical.getValue() == 0) {
                    event.cancel();

                } else {
                    packet.setX((int) (packet.getX() * h));
                    packet.setY((int) (packet.getY() * v));
                    packet.setZ((int) (packet.getZ() * h));
                }
            }
        }
    }

    @SubscribeEvent
    public void onPushOutOfBlocks(PlayerSPPushOutOfBlocksEvent event) {
        if (blockPush.getValue()) event.setCanceled(true);
    }
}
