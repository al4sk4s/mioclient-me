package me.mioclient.api.managers.impl;

import com.mojang.realmsclient.gui.ChatFormatting;
import me.mioclient.Mio;
import me.mioclient.api.events.impl.PacketEvent;
import me.mioclient.mod.Mod;
import me.mioclient.mod.commands.Command;
import net.minecraft.network.play.client.CPacketChatMessage;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class ReloadManager
        extends Mod {
    public String prefix;

    public void init(String prefix) {
        this.prefix = prefix;
        MinecraftForge.EVENT_BUS.register(this);
        if (!fullNullCheck()) {
            Command.sendMessage(ChatFormatting.RED + "Mio has been unloaded. Type " + prefix + "reload to reload.");
        }
    }

    public void unload() {
        MinecraftForge.EVENT_BUS.unregister(this);
    }

    @SubscribeEvent
    public void onPacketSend(PacketEvent.Send event) {
        CPacketChatMessage packet;
        if (event.getPacket() instanceof CPacketChatMessage && (packet = event.getPacket()).getMessage().startsWith(prefix) && packet.getMessage().contains("reload")) {
            Mio.load();
            event.cancel();
        }
    }
}

