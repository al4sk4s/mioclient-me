package me.mioclient.mod.commands.impl;

import com.mojang.realmsclient.gui.ChatFormatting;
import me.mioclient.mod.commands.Command;

import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;

public class CoordsCommand
        extends Command {
    public CoordsCommand() {
        super("coords");
    }
    String coords;

    @Override
    public void execute(String[] commands) {
        int posX = (int) mc.player.posX;
        int posY = (int) mc.player.posY;
        int posZ = (int) mc.player.posZ;
        coords = "X: " + posX + " Y: " + posY + " Z: " + posZ;
        String myString = coords;
        StringSelection stringSelection = new StringSelection(myString);
        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        clipboard.setContents(stringSelection, null);
        Command.sendMessage(ChatFormatting.GRAY + "Coords copied.");
    }
}
