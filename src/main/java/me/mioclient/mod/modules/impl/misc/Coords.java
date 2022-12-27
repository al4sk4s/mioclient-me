package me.mioclient.mod.modules.impl.misc;

import me.mioclient.mod.modules.Category;
import me.mioclient.mod.modules.Module;

import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;

public class Coords extends Module {

    public Coords() {
        super("Coords", "copies your current position to the clipboard", Category.MISC);
    }

    @Override
    public void onEnable() {
        int posX = (int) mc.player.posX;
        int posY = (int) mc.player.posY;
        int posZ = (int) mc.player.posZ;

        String coords = "X: " + posX + " Y: " + posY + " Z: " + posZ;

        StringSelection stringSelection = new StringSelection(coords);

        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        clipboard.setContents(stringSelection, null);

        toggle();
    }
}
