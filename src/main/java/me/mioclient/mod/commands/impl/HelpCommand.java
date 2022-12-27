package me.mioclient.mod.commands.impl;

import com.mojang.realmsclient.gui.ChatFormatting;
import me.mioclient.api.managers.Managers;
import me.mioclient.mod.commands.Command;

public class HelpCommand
        extends Command {
    public HelpCommand() {
        super("help");
    }

    @Override
    public void execute(String[] commands) {
        sendMessage("Commands: ");
        for (Command command : Managers.COMMANDS.getCommands()) {
            sendMessage(ChatFormatting.GRAY + Managers.COMMANDS.getCommandPrefix() + command.getName());
        }
    }
}

