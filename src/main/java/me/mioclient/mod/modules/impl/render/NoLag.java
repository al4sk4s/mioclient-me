package me.mioclient.mod.modules.impl.render;

import com.mojang.realmsclient.gui.ChatFormatting;
import me.mioclient.api.events.impl.PacketEvent;
import me.mioclient.api.events.impl.RenderEntityEvent;
import me.mioclient.mod.commands.Command;
import me.mioclient.mod.modules.Category;
import me.mioclient.mod.modules.Module;
import me.mioclient.mod.modules.settings.Setting;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityTNTPrimed;
import net.minecraft.entity.passive.EntityParrot;
import net.minecraft.entity.projectile.EntityWitherSkull;
import net.minecraft.network.play.server.SPacketChat;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

/**
 * @author t.me/asphyxia1337
 */

public class NoLag extends Module {

    public static NoLag INSTANCE;

    public Setting<Boolean> antiChina =
            add(new Setting<>("AntiChina", true));
    public Setting<Boolean> skulls =
            add(new Setting<>("WitherSkulls", false));
    public Setting<Boolean> tnt =
            add(new Setting<>("PrimedTNT", false));
    public Setting<Boolean> scoreBoards =
            add(new Setting<>("ScoreBoards", true));
    public Setting<Boolean> glowing =
            add(new Setting<>("GlowingEntities", true));
    public Setting<Boolean> parrots =
            add(new Setting<>("Parrots", true));

    public NoLag() {
        super("NoLag", "Removes several things that may cause fps drops.", Category.RENDER, true);
        INSTANCE = this;
    }

    @Override
    public void onTick() {

        if (!glowing.getValue()) return;

        for (Entity entity : mc.world.loadedEntityList) {

            if (entity.isGlowing()) {
                entity.setGlowing(false);
            }
        }
    }

    @SubscribeEvent
    public void onPacketReceive(PacketEvent.Receive event) {
        String chat;

        if (event.getPacket() instanceof SPacketChat && antiChina.getValue()) {
            chat = ((SPacketChat) event.getPacket()).chatComponent.getUnformattedText();

            if (chat.contains("\u3F01") || chat.contains("\u3801") || chat.contains("\u6301") || chat.contains("\u6401") || chat.contains("\u1B01") || chat.contains("\u1201") || chat.contains("\u0101") || chat.contains("\u5B01")) {

                event.cancel();
                ((SPacketChat) (event.getPacket())).chatComponent = null;

                Command.sendMessageWithID("[" + getName() + "] " + ChatFormatting.RED + "Removed some chinese text :')", -343435);
            }
        }
    }

    @SubscribeEvent
    public void onRenderEntity(RenderEntityEvent event) {

        if (event.getEntity() != null) {

            if (skulls.getValue() && event.getEntity() instanceof EntityWitherSkull) {
                event.cancel();
            }

            if (tnt.getValue() && event.getEntity() instanceof EntityTNTPrimed) {
                event.cancel();
            }

            if (parrots.getValue() && event.getEntity() instanceof EntityParrot) {
                event.cancel();
            }
        }
    }
}
