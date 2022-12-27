package me.mioclient.api.managers.impl;

import com.mojang.realmsclient.gui.ChatFormatting;
import me.mioclient.api.util.math.Timer;
import me.mioclient.api.util.render.ColorUtil;
import me.mioclient.mod.Mod;
import me.mioclient.mod.gui.font.CustomFont;
import me.mioclient.mod.modules.impl.client.ClickGui;
import me.mioclient.mod.modules.impl.client.FontMod;
import me.mioclient.mod.modules.impl.player.NameProtect;
import net.minecraft.util.math.MathHelper;

import java.awt.*;
import java.util.regex.Pattern;

public class TextManager extends Mod {

    private final Timer idleTimer = new Timer();
    public int scaledWidth;
    public int scaledHeight;
    public int scaleFactor;
    private CustomFont customFont = new CustomFont(new Font("Verdana", 0, 17), true, true);
    private boolean idling;

    public final String syncCode = "\u00a7(";

    public TextManager() {
        updateResolution();
    }

    public void init() {

        if (FontMod.INSTANCE == null) {
            FontMod.INSTANCE = new FontMod();
        }

        FontMod fonts = FontMod.INSTANCE;

        try {
            setFontRenderer(new Font(fonts.font.getValue(), fonts.getFont(), fonts.size.getValue()), fonts.antiAlias.getValue(), fonts.metrics.getValue());
        } catch (Exception ignored) {

        }
    }

    public String getPrefix() {
        return "\u00a7r" + ChatFormatting.WHITE + "[" + "\u00a7r" + "Mio" + "\u00a7(" + "] " + ChatFormatting.RESET;
    }

    public String normalizeCases(Object o) {
        return Character.toUpperCase(o.toString().charAt(0)) + o.toString().toLowerCase().substring(1);
    }

    public float drawStringNoCFont(String text, float x, float y, int color, boolean shadow) {
        mc.fontRenderer.drawString(text, x, y, color, shadow);
        return x;
    }

    public void drawStringWithShadow(String text, float x, float y, int color) {
        drawString(text, x, y, color, true);
    }

    public float drawString(String text, float x, float y, int color, boolean shadow) {

        NameProtect nameProtect = NameProtect.INSTANCE;

        text = nameProtect.isOn()
                ? text.replaceAll(mc.getSession().getUsername(), nameProtect.name.getValue())
                : text;

        if (FontMod.INSTANCE.isOn()) {
            if (shadow) {
                customFont.drawStringWithShadow(text, x, y, color);

            } else {
                customFont.drawString(text, x, y, color);
            }
            return x;
        }
        mc.fontRenderer.drawString(text, x, y, color, shadow);

        return x;
    }

    public void drawRollingRainbowString(String text, float x, float y, boolean shadow) {
        Pattern.compile("(?i)\u00a7[0-9A-FK-OR]").matcher(text).replaceAll("");
        int[] arrayOfInt = {1};
        char[] stringToCharArray = (text).toCharArray();
        float f = 0.0f + x;
        for (char c : stringToCharArray) {
            drawString(String.valueOf(c), f,
                    y, ColorUtil.rainbow(arrayOfInt[0] * (ClickGui.INSTANCE).rainbowDelay.getValue()).getRGB(), shadow);
            f += getStringWidth(String.valueOf(c));
            arrayOfInt[0] = arrayOfInt[0] + 1;
        }
    }

    public int getStringWidth(String text) {

        NameProtect nameProtect = NameProtect.INSTANCE;

        text = nameProtect.isOn()
                ? text.replaceAll(mc.getSession().getUsername(), nameProtect.name.getValue())
                : text;

        if (FontMod.INSTANCE.isOn()) {
            return customFont.getStringWidth(text);
        }
        return mc.fontRenderer.getStringWidth(text);
    }

    public int getFontHeight() {
        if (FontMod.INSTANCE.isOn()) {
            String text = "A";
            return customFont.getStringHeight(text);
        }
        return mc.fontRenderer.FONT_HEIGHT;
    }

    public void setFontRenderer(Font font, boolean antiAlias, boolean fractionalMetrics) {
        customFont = new CustomFont(font, antiAlias, fractionalMetrics);
    }

    public Font getCurrentFont() {
        return customFont.getFont();
    }

    public void updateResolution() {
        scaledWidth = mc.displayWidth;
        scaledHeight = mc.displayHeight;
        scaleFactor = 1;
        boolean flag = mc.isUnicode();
        
        int i = mc.gameSettings.guiScale;
        
        if (i == 0) {
            i = 1000;
        }
        
        while (scaleFactor < i && scaledWidth / (scaleFactor + 1) >= 320 && scaledHeight / (scaleFactor + 1) >= 240) {
            ++scaleFactor;
        }
        
        if (flag && scaleFactor % 2 != 0 && scaleFactor != 1) {
            --scaleFactor;
        }
        
        double scaledWidthD = scaledWidth / scaleFactor;
        double scaledHeightD = scaledHeight / scaleFactor;
        
        scaledWidth = MathHelper.ceil(scaledWidthD);
        scaledHeight = MathHelper.ceil(scaledHeightD);
    }

    public String getIdleSign() {
        if (idleTimer.passedMs(500L)) {
            idling = !idling;
            idleTimer.reset();
        }
        if (idling) {
            return "_";
        }
        return "";
    }
}