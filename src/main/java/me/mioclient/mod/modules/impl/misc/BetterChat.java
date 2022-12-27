package me.mioclient.mod.modules.impl.misc;

import com.mojang.realmsclient.gui.ChatFormatting;
import me.mioclient.api.events.impl.PacketEvent;
import me.mioclient.mod.modules.Category;
import me.mioclient.mod.modules.Module;
import me.mioclient.mod.modules.settings.Setting;
import net.minecraft.network.play.client.CPacketChatMessage;
import net.minecraft.util.text.TextComponentString;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.text.SimpleDateFormat;
import java.util.Date;

public class BetterChat extends Module {

    public static BetterChat INSTANCE;

    public final Setting<Boolean> rect =
            add(new Setting<>("Rect", true).setParent());
    public final Setting<Boolean> colorRect =
            add(new Setting<>("ColorRect", false, v -> rect.isOpen()));
    public final Setting<Boolean> infinite =
            add(new Setting<>("InfiniteChat", false));
    public final Setting<Boolean> suffix =
            add(new Setting<>("Suffix", false).setParent());
    public final Setting<Boolean> suffix2b =
            add(new Setting<>("2b2tSuffix", false, v -> suffix.isOpen()));
    public final Setting<Boolean> time =
            add(new Setting<>("TimeStamps", false).setParent());
    public final Setting<Bracket> bracket =
            add(new Setting<>("Bracket", Bracket.TRIANGLE, v -> time.isOpen()));

    public BetterChat() {
        super("BetterChat", "Modifies your chat", Category.MISC, true);
        INSTANCE = this;
    }

    private enum Bracket {
        SQUARE,
        TRIANGLE
    }

    @SubscribeEvent
    public void onPacketSend(PacketEvent.Send event) {
        if (event.getPacket() instanceof CPacketChatMessage) {
            String s = ((CPacketChatMessage) event.getPacket()).getMessage();
        }

        if ((suffix.getValue() && event.getPacket() instanceof CPacketChatMessage)) {
            if (suffix2b.getValue()) {
                CPacketChatMessage packet = event.getPacket();
                String message = packet.getMessage();

                if (message.startsWith("/") || message.startsWith("!")) {
                    return;
                }
                message = message + " | mio";

                if (message.length() >= 256) {
                    message = message.substring(0, 256);
                }
                packet.message = message;

            } else {
                CPacketChatMessage packet = event.getPacket();
                String message = packet.getMessage();

                if (message.startsWith("/") || message.startsWith("!")) {
                    return;
                }
                message = message + " ⋆ ᴍɪᴏ";

                if (message.length() >= 256) {
                    message = message.substring(0, 256);
                }
                packet.message = message;
            }
        }
    }

    @SubscribeEvent
    public void onClientChatReceived(ClientChatReceivedEvent event) {
        if (time.getValue()) {
            Date date = new Date();
            SimpleDateFormat dateFormatter = new SimpleDateFormat("HH:mm");
            String strDate = dateFormatter.format(date);
            String leBracket1 = bracket.getValue() == Bracket.TRIANGLE ? "<" : "[";
            String leBracket2 = bracket.getValue() == Bracket.TRIANGLE ? ">" : "]";
            TextComponentString time = new TextComponentString(ChatFormatting.GRAY + leBracket1 + ChatFormatting.WHITE + strDate + ChatFormatting.GRAY + leBracket2 + ChatFormatting.RESET + " ");
            event.setMessage(time.appendSibling(event.getMessage()));
        }
    }
}

