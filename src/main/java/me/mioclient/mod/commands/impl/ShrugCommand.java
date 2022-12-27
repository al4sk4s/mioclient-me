package me.mioclient.mod.commands.impl;

import com.mojang.realmsclient.gui.ChatFormatting;
import me.mioclient.mod.commands.Command;

import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;


public class ShrugCommand
        extends Command {
    public ShrugCommand() {
        super("shrug");
    }

    @Override
    public void execute(String[] commands) {
        String shrug = "\u00af\\_(\u30c4)_/\u00af";
        StringSelection stringSelection = new StringSelection(shrug);
        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        clipboard.setContents(stringSelection, null);
        Command.sendMessage(ChatFormatting.GRAY + "copied le shrug to ur clipboard");
    }
}
