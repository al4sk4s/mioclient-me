package me.mioclient.api.util.render;

import me.mioclient.mod.modules.impl.client.ClickGui;

import java.awt.*;

public class ColorUtil {

    public static int toARGB(int r, int g, int b, int a) {
        return new Color(r, g, b, a).getRGB();
    }

    public static int toRGBA(int r, int g, int b) {
        return toRGBA(r, g, b, 255);
    }

    public static int toRGBA(int r, int g, int b, int a) {
        return (r << 16) + (g << 8) + b + (a << 24);
    }

    public static int toRGBA(float r, float g, float b, float a) {
        return toRGBA((int)(r * 255.0f), (int)(g * 255.0f), (int)(b * 255.0f), (int)(a * 255.0f));
    }

    public static int toHex(int r, int g, int b){
        return (0xff << 24) | ((r&0xff) << 16) | ((g&0xff) << 8) | (b&0xff);
    }

    public static Color injectAlpha(Color color, int alpha) {
        return new Color(color.getRed(), color.getGreen(), color.getBlue(), alpha);
    }

    public static int injectAlpha(int color, int alpha) {
        return toRGBA(new Color(color).getRed(), new Color(color).getGreen(), new Color(color).getBlue(), alpha);
    }

    public static Color rainbow(int delay) {
        double rainbowState = Math.ceil((System.currentTimeMillis() + delay) / 20.0);

        if (ClickGui.INSTANCE.rainbowMode.getValue() == ClickGui.Rainbow.DOUBLE) {
            return gradientColor(ClickGui.INSTANCE.color.getValue(),
                    ClickGui.INSTANCE.secondColor.getValue(),
                    Math.abs(((float) (System.currentTimeMillis() % 2000L) / 1000.0F + (float) 20 / (float) (((delay / 15) * 2) + 10) * 2.0F) % 2.0F - 1.0F));

        } else if (ClickGui.INSTANCE.rainbowMode.getValue() == ClickGui.Rainbow.PLAIN) {
            return pulseColor(ClickGui.INSTANCE.color.getValue(), 50, delay);

        } else {
            return Color.getHSBColor((float) (rainbowState % 360.0 / 360.0), ClickGui.INSTANCE.rainbowSaturation.getValue() / 255.0f, ClickGui.INSTANCE.rainbowBrightness.getValue() / 255.0f);
        }
    }

    public static int rainbow(int delay, int alpha) {
        double rainbowState = Math.ceil((System.currentTimeMillis() + delay) / 20.0);
        return toRGBA((int)(rainbowState % 360.0 / 360.0), ClickGui.INSTANCE.rainbowSaturation.getValue().intValue(), ClickGui.INSTANCE.rainbowBrightness.getValue().intValue(), alpha);
    }

    public static Color pulseColor(Color color, int index, int count) {
        float[] hsb = new float[3];
        Color.RGBtoHSB(color.getRed(), color.getGreen(), color.getBlue(), hsb);
        float brightness = Math.abs((System.currentTimeMillis() % ((long)1230675006 ^ 0x495A9BEEL) / Float.intBitsToFloat(Float.floatToIntBits(0.0013786979f) ^ 0x7ECEB56D) + index / (float)count * Float.intBitsToFloat(Float.floatToIntBits(0.09192204f) ^ 0x7DBC419F)) % Float.intBitsToFloat(Float.floatToIntBits(0.7858098f) ^ 0x7F492AD5) - Float.intBitsToFloat(Float.floatToIntBits(6.46708f) ^ 0x7F4EF252));
        brightness = Float.intBitsToFloat(Float.floatToIntBits(18.996923f) ^ 0x7E97F9B3) + Float.intBitsToFloat(Float.floatToIntBits(2.7958195f) ^ 0x7F32EEB5) * brightness;
        hsb[2] = brightness % Float.intBitsToFloat(Float.floatToIntBits(0.8992331f) ^ 0x7F663424);
        return new Color(Color.HSBtoRGB(hsb[0], hsb[1], hsb[2]));
    }

    public static Color gradientColor(Color color1, Color color2, double offset) {
        if (offset > 1) {
            double left = offset % 1;
            int off = (int) offset;
            offset = off % 2 == 0 ? left : 1 - left;
        }

        double inverse_percent = 1 - offset;
        int redPart = (int) (color1.getRed() * inverse_percent + color2.getRed() * offset);
        int greenPart = (int) (color1.getGreen() * inverse_percent + color2.getGreen() * offset);
        int bluePart = (int) (color1.getBlue() * inverse_percent + color2.getBlue() * offset);
        return new Color(redPart, greenPart, bluePart);
    }

    public static int toRGBA(Color color) {
        return toRGBA(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha());
    }

    public static Color interpolate(float value, Color start, Color end) {
        float sr = (float)start.getRed() / 255.0F;
        float sg = (float)start.getGreen() / 255.0F;
        float sb = (float)start.getBlue() / 255.0F;
        float sa = (float)start.getAlpha() / 255.0F;

        float er = (float)end.getRed() / 255.0F;
        float eg = (float)end.getGreen() / 255.0F;
        float eb = (float)end.getBlue() / 255.0F;
        float ea = (float)end.getAlpha() / 255.0F;

        float r = sr * value + er * (1.0F - value);
        float g = sg * value + eg * (1.0F - value);
        float b = sb * value + eb * (1.0F - value);
        float a = sa * value + ea * (1.0F - value);

        return new Color(r, g, b, a);
    }

}

