package me.mioclient.mod.commands.impl;

import com.mojang.realmsclient.gui.ChatFormatting;
import me.mioclient.mod.commands.Command;
import me.mioclient.mod.modules.impl.client.FontMod;
import me.mioclient.mod.modules.impl.client.HUD;

/**
 * Someone please rewrite it dear god it's so bad
 * - asphyxia
 */

public class WatermarkCommand extends Command {

    public WatermarkCommand() {
        super("watermark", new String[]{"<watermark>"});
    }

    public void execute(String[] commands) {
        if (commands.length == 2) {

            FontMod fontMod = FontMod.INSTANCE;

            boolean customFont = fontMod.isOn();
            //Тут костыль конечно пиздец ахахахахахах

            if (commands[0] != null) {
                if (customFont) {
                    fontMod.disable();
                }

                HUD.getInstance().watermarkString.setValue(commands[0]);

                if (customFont) {
                    fontMod.enable();
                }

                sendMessage("Watermark set to " + ChatFormatting.GREEN + commands[0]);

            } else {
                sendMessage("Not a valid command... Possible usage: <New Watermark>");
            }
        }
    }
}
