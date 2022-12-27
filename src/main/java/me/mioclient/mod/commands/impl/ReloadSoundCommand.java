package me.mioclient.mod.commands.impl;

import com.mojang.realmsclient.gui.ChatFormatting;
import me.mioclient.mod.commands.Command;
import net.minecraft.client.audio.SoundHandler;
import net.minecraft.client.audio.SoundManager;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;

public class ReloadSoundCommand extends Command {
    public ReloadSoundCommand() {
        super("sound", new String[0]);
    }

    @Override
    public void execute(String[] commands) {
        try {
            SoundManager sndManager = ObfuscationReflectionHelper.getPrivateValue(SoundHandler.class, ReloadSoundCommand.mc.getSoundHandler(), new String[]{"sndManager", "sndManager"});
            sndManager.reloadSoundSystem();
            Command.sendMessage(ChatFormatting.GREEN + "Reloaded Sound System.");
        } catch (Exception e) {
            System.out.println(ChatFormatting.RED + "Could not restart sound manager: " + e);
            e.printStackTrace();
            Command.sendMessage(ChatFormatting.RED + "Couldnt Reload Sound System!");
        }
    }
}
