package me.mioclient.mod.modules.impl.client;

import club.minnced.discord.rpc.DiscordEventHandlers;
import club.minnced.discord.rpc.DiscordRichPresence;
import me.mioclient.Mio;
import me.mioclient.mod.modules.Category;
import me.mioclient.mod.modules.Module;
import net.minecraft.client.gui.GuiMainMenu;

import java.util.Random;

public class DiscordRPC extends Module {

    private final club.minnced.discord.rpc.DiscordRPC rpc = club.minnced.discord.rpc.DiscordRPC.INSTANCE;

    private final DiscordRichPresence presence = new DiscordRichPresence();

    private Thread thread;

    private final String[] state = {
            "butterfly v9",
            "1110101001001010",
            "jordohooks",
            "SX-Hack v3.6b",
            "hvhlegende est. 2021",
            "flying over thousands of blocks with the power of miohake$$",
            "OskarMajewskiWare",
            "Gaming",
            "w/ the fellas",
            "mioclient",
            "Hazelwood Drive, Ballyspilliane, Killarney, Co. Kerry",
            "Owned By Alexander Pravshin",
            "195.155.194.117"
    };

    public DiscordRPC() {
        super("DiscordRPC", "Discord rich presence", Category.CLIENT);
    }

    @Override
    public void onEnable() {
        super.onEnable();
        start();
    }

    @Override
    public void onDisable() {
        super.onDisable();
        stop();
    }

    @Override
    public void onLoad() {
        super.onLoad();

        if (isOn()) {
            start();
        }
    }

    private void start() {
        DiscordEventHandlers handlers = new DiscordEventHandlers();

        rpc.Discord_Initialize("1016673155693158420", handlers, true, "");

        presence.startTimestamp = System.currentTimeMillis() / 1000L;

        presence.details = "Mio " + Mio.MODVER;

        presence.state = state[new Random().nextInt(state.length)];

        presence.largeImageKey = "big";
        presence.largeImageText = "mio " + Mio.MODVER;

        presence.smallImageKey = ((mc.currentScreen instanceof GuiMainMenu ? "idling" :
                (mc.currentServerData != null ? "multiplayer" : "singleplayer")));

        presence.smallImageText = ((mc.currentScreen instanceof GuiMainMenu ? "Idling." :
                (mc.currentServerData != null ? "Playing multiplayer." : "Playing singleplayer.")));

        rpc.Discord_UpdatePresence(presence);

        thread = new Thread(() -> {
            while (!Thread.currentThread().isInterrupted()) {
                rpc.Discord_RunCallbacks();

                presence.details = "Mio " + Mio.MODVER;

                presence.state = state[new Random().nextInt(state.length)];

                presence.smallImageKey = ((mc.currentScreen instanceof GuiMainMenu ? "iding" :
                        (mc.currentServerData != null ? "multiplayer" : "singleplayer")));

                presence.smallImageText = ((mc.currentScreen instanceof GuiMainMenu ? "Iding." :
                        (mc.currentServerData != null ? "Playing multiplayer." : "Playing singleplayer.")));

                rpc.Discord_UpdatePresence(presence);

                try {
                    Thread.sleep(2000L);

                } catch (InterruptedException ignored) {

                }
            }
        }, "DiscordRPC-Callback-Handler");

        thread.start();
    }

    private void stop() {
        if (thread != null && !thread.isInterrupted()) {
            thread.interrupt();
        }

        rpc.Discord_Shutdown();
    }
}