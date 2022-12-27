package me.mioclient.api.managers.impl;

import com.mojang.realmsclient.gui.ChatFormatting;
import me.mioclient.mod.Mod;
import me.mioclient.mod.commands.Command;
import me.mioclient.mod.commands.impl.*;

import java.util.ArrayList;
import java.util.LinkedList;

public class CommandManager extends Mod {

    private final ArrayList<Command> commands = new ArrayList();
    private String clientMessage = "[Mio]";
    private String prefix = ";";

    public CommandManager() {
        super("Command");
        commands.add(new BindCommand());
        commands.add(new ModuleCommand());
        commands.add(new PrefixCommand());
        commands.add(new ConfigCommand());
        commands.add(new FriendCommand());
        commands.add(new HelpCommand());
        commands.add(new UnloadCommand());
        commands.add(new ReloadSoundCommand());
        commands.add(new CoordsCommand());
        commands.add(new ShrugCommand());
        commands.add(new WatermarkCommand());
    }

    public static String[] removeElement(String[] input, int indexToDelete) {
        LinkedList<String> result = new LinkedList<String>();
        for (int i = 0; i < input.length; ++i) {
            if (i == indexToDelete) continue;
            result.add(input[i]);
        }
        return result.toArray(input);
    }

    private static String strip(String str, String key) {
        if (str.startsWith(key) && str.endsWith(key)) {
            return str.substring(key.length(), str.length() - key.length());
        }
        return str;
    }

    public void executeCommand(String command) {
        String[] parts = command.split(" (?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)");
        String name = parts[0].substring(1);
        String[] args = removeElement(parts, 0);
        for (int i = 0; i < args.length; ++i) {
            if (args[i] == null) continue;
            args[i] = strip(args[i], "\"");
        }
        for (Command c : commands) {
            if (!c.getName().equalsIgnoreCase(name)) continue;
            c.execute(parts);
            return;
        }
        Command.sendMessage(ChatFormatting.GRAY + "Command not found, type 'help' for the commands list.");
    }

    public Command getCommandByName(String name) {
        for (Command command : commands) {
            if (!command.getName().equals(name)) continue;
            return command;
        }
        return null;
    }

    public ArrayList<Command> getCommands() {
        return commands;
    }

    public String getClientMessage() {
        return clientMessage;
    }

    public void setClientMessage(String clientMessage) {
        this.clientMessage = clientMessage;
    }

    public String getCommandPrefix() {
        return prefix;
    }

    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }
}

