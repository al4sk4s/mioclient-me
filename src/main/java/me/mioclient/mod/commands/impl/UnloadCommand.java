package me.mioclient.mod.commands.impl;

import me.mioclient.Mio;
import me.mioclient.mod.commands.Command;

public class UnloadCommand
        extends Command {
    public UnloadCommand() {
        super("unload", new String[0]);
    }

    @Override
    public void execute(String[] commands) {
        Mio.unload(true);
    }
}

