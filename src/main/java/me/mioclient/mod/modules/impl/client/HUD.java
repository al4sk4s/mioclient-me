package me.mioclient.mod.modules.impl.client;

import com.mojang.realmsclient.gui.ChatFormatting;
import me.mioclient.Mio;
import me.mioclient.api.events.impl.Render2DEvent;
import me.mioclient.api.managers.Managers;
import me.mioclient.api.managers.impl.ModuleManager;
import me.mioclient.api.util.entity.EntityUtil;
import me.mioclient.api.util.math.MathUtil;
import me.mioclient.api.util.math.Timer;
import me.mioclient.api.util.render.ColorUtil;
import me.mioclient.api.util.render.RenderUtil;
import me.mioclient.mod.gui.screen.MioClickGui;
import me.mioclient.mod.modules.Category;
import me.mioclient.mod.modules.Module;
import me.mioclient.mod.modules.impl.combat.Aura;
import me.mioclient.mod.modules.impl.combat.AutoTrap;
import me.mioclient.mod.modules.settings.Setting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiChat;
import net.minecraft.client.gui.GuiMainMenu;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.math.MathHelper;

import java.awt.*;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.*;

public class HUD extends Module {

    public static HUD INSTANCE = new HUD();

    private final Setting<Page> page =
            add(new Setting<>("Page", Page.GLOBAL));

    //Stuff

    public final Setting<Boolean> potionIcons =
            add(new Setting<>("NoPotionIcons", false, v -> page.getValue() == Page.GLOBAL));

    private final Setting<Boolean> grayColors =
            add(new Setting<>("Gray", true, v -> page.getValue() == Page.GLOBAL));

    public Setting<Boolean> lowerCase =
            add(new Setting<>("LowerCase", false, v -> page.getValue() == Page.GLOBAL));

    private final Setting<Boolean> renderingUp =
            add(new Setting<>("RenderingUp", true, v -> page.getValue() == Page.GLOBAL));

    //Elements

    private final Setting<Boolean> skeetBar =
            add(new Setting<>("SkeetMode", false, v -> page.getValue() == Page.ELEMENTS).setParent());
    private final Setting<Boolean> jamie =
            add(new Setting<>("JamieColor", false, v -> page.getValue() == Page.ELEMENTS && skeetBar.isOpen()));

    private final Setting<Boolean> watermark =
            add(new Setting<>("Watermark", true, v -> page.getValue() == Page.ELEMENTS).setParent());

    public Setting<String> watermarkString =
            add(new Setting<>("Text", "Mio", v -> !(mc.currentScreen instanceof MioClickGui || mc.currentScreen instanceof GuiMainMenu)));

    private final Setting<Boolean> watermarkShort =
            add(new Setting<>("Shorten", false, v -> watermark.isOpen() && page.getValue() == Page.ELEMENTS));
    private final Setting<Boolean> watermarkVerColor =
            add(new Setting<>("VerColor", true, v -> watermark.isOpen() && page.getValue() == Page.ELEMENTS));
    private final Setting<Integer> waterMarkY =
            add(new Setting<>("Height", 2, 2, 12, v -> page.getValue() == Page.ELEMENTS && watermark.isOpen()));
    private final Setting<Boolean> idWatermark =
            add(new Setting<>("IdWatermark", true, v -> page.getValue() == Page.ELEMENTS));
    private final Setting<Boolean> pvp =
            add(new Setting<>("PvpInfo", true, v -> page.getValue() == Page.ELEMENTS));

    private final Setting<Boolean> textRadar =
            add(new Setting<>("TextRadar", false, v -> page.getValue() == Page.ELEMENTS));

    private final Setting<Boolean> coords =
            add(new Setting<>("Position(XYZ)", false, v -> page.getValue() == Page.ELEMENTS));
    private final Setting<Boolean> direction =
            add(new Setting<>("Direction", false, v -> page.getValue() == Page.ELEMENTS));
    
    private final Setting<Boolean> armor =
            add(new Setting<>("Armor", false, v -> page.getValue() == Page.ELEMENTS));
    private final Setting<Boolean> lag =
            add(new Setting<>("LagNotifier", false, v -> page.getValue() == Page.ELEMENTS));
    
    private final Setting<Boolean> greeter =
            add(new Setting<>("Welcomer", false, v -> page.getValue() == Page.ELEMENTS).setParent());
    private final Setting<GreeterMode> greeterMode =
            add(new Setting<>("Mode", GreeterMode.PLAYER, v -> page.getValue() == Page.ELEMENTS && greeter.isOpen()));
    private final Setting<Boolean> greeterNameColor =
            add(new Setting<>("NameColor", true, v -> greeter.isOpen() && greeterMode.getValue() == GreeterMode.PLAYER && page.getValue() == Page.ELEMENTS));
    private final Setting<String> greeterText =
            add(new Setting<>("WelcomerText", "i sniff coke and smoke dope i got 2 habbits", v -> greeter.isOpen() && greeterMode.getValue() == GreeterMode.CUSTOM && page.getValue() == Page.ELEMENTS));

    private final Setting<Boolean> arrayList =
            add(new Setting<>("ArrayList", false, v -> page.getValue() == Page.ELEMENTS).setParent());
    private final Setting<Boolean> jamieArray =
            add(new Setting<>("JamieArray", false, v -> page.getValue() == Page.ELEMENTS && arrayList.isOpen()));
    private final Setting<Boolean> forgeHax =
            add(new Setting<>("ForgeHax", false, v -> page.getValue() == Page.ELEMENTS && arrayList.isOpen()));
    private final Setting<Boolean> arrayListLine =
            add(new Setting<>("Outline", false, v -> page.getValue() == Page.ELEMENTS && arrayList.isOpen()));
    private final Setting<Boolean> arrayListRect =
            add(new Setting<>("Rect", false, v -> page.getValue() == Page.ELEMENTS && arrayList.isOpen()));
    private final Setting<Boolean> arrayListRectColor =
            add(new Setting<>("ColorRect", false, v -> page.getValue() == Page.ELEMENTS && arrayList.isOpen() && arrayListRect.getValue()));
    private final Setting<Boolean> arrayListGlow =
            add(new Setting<>("Glow", true, v -> page.getValue() == Page.ELEMENTS && arrayList.isOpen()));
    private final Setting<Boolean> hideInChat =
            add(new Setting<>("HideInChat", true, v -> page.getValue() == Page.ELEMENTS && arrayList.isOpen()));

    private final Setting<Boolean> potions =
            add(new Setting<>("Potions", false, v -> page.getValue() == Page.ELEMENTS).setParent());
    private final Setting<Boolean> potionColor =
            add(new Setting<>("PotionColor", false, v -> page.getValue() == Page.ELEMENTS && potions.isOpen()));
    
    private final Setting<Boolean> ping =
            add(new Setting<>("Ping", false, v -> page.getValue() == Page.ELEMENTS));
    private final Setting<Boolean> speed =
            add(new Setting<>("Speed", false, v -> page.getValue() == Page.ELEMENTS));
    private final Setting<Boolean> tps =
            add(new Setting<>("TPS", false, v -> page.getValue() == Page.ELEMENTS));
    private final Setting<Boolean> fps =
            add(new Setting<>("FPS", false, v -> page.getValue() == Page.ELEMENTS));
    private final Setting<Boolean> time =
            add(new Setting<>("Time", false, v -> page.getValue() == Page.ELEMENTS));

    public final Setting<ModuleManager.Ordering> ordering =
            add(new Setting<>("Ordering", ModuleManager.Ordering.LENGTH, v -> page.getValue() == Page.GLOBAL));
    public final Setting<Integer> lagTime =
            add(new Setting<>("LagTime", 1000, 0, 2000, v -> page.getValue() == Page.GLOBAL));

    private final Timer timer = new Timer();
    private Map<String, Integer> players = new HashMap<>();

    private int color;

    public HUD() {
        super("HUD", "HUD elements drawn on your screen", Category.CLIENT, true);
        setInstance();
    }

    public static HUD getInstance() {
        if (INSTANCE == null)
            INSTANCE = new HUD();
        return INSTANCE;
    }

    private void setInstance() {
        INSTANCE = this;
    }

    private  enum GreeterMode {
        PLAYER,
        CUSTOM
    }

    private enum Page {
        ELEMENTS,
        GLOBAL
    }

    @Override
    public void onUpdate() {
        if (timer.passedMs(500)) {
            players = getTextRadarMap();
            timer.reset();
        }
    }

    @Override
    public void onRender2D(Render2DEvent event) {
        if (fullNullCheck()) return;

        int width = Managers.TEXT.scaledWidth;
        int height = Managers.TEXT.scaledHeight;

        color = ColorUtil.toRGBA(ClickGui.INSTANCE.color.getValue().getRed(), ClickGui.INSTANCE.color.getValue().getGreen(), ClickGui.INSTANCE.color.getValue().getBlue());

        if (watermark.getValue()) {
            String mioString = watermarkString.getValue() + " ";
            String verColor = watermarkVerColor.getValue() ? "" + ChatFormatting.WHITE : "";
            String verString = verColor + (watermarkShort.getValue() ? Mio.MODVER.substring(0, 4) : Mio.MODVER + "+" + Mio.VERHASH);

            if ((ClickGui.INSTANCE).rainbow.getValue()) {
                if ((ClickGui.INSTANCE).hudRainbow.getValue() == ClickGui.HudRainbow.STATIC) {
                    Managers.TEXT.drawString((lowerCase.getValue() ? mioString.toLowerCase() : mioString) + verString, 2.0F, waterMarkY.getValue(), Managers.COLORS.getRainbow().getRGB(), true);
                } else {
                    if (watermarkVerColor.getValue()) {
                        drawDoubleRainbowRollingString((lowerCase.getValue() ? mioString.toLowerCase() : mioString), verString, 2.0f, waterMarkY.getValue(), true);
                    } else {
                        Managers.TEXT.drawRollingRainbowString((lowerCase.getValue() ? mioString.toLowerCase() : mioString) + verString, 2.0f, waterMarkY.getValue(), true);
                    }
                }
            } else {
                Managers.TEXT.drawString((lowerCase.getValue() ? mioString.toLowerCase() : mioString) + verString, 2.0F, waterMarkY.getValue(), color, true);
            }
        }

        Color color = new Color(ClickGui.INSTANCE.color.getValue().getRed(), ClickGui.INSTANCE.color.getValue().getGreen(), ClickGui.INSTANCE.color.getValue().getBlue());

        if (skeetBar.getValue()) {

            if (jamie.getValue()) {
                RenderUtil.drawHGradientRect(0, 0, width / 5.0f, 1, ColorUtil.toRGBA(0, 180, 255), ColorUtil.toRGBA(255, 180, 255));
                RenderUtil.drawHGradientRect(width / 5.0f, 0, (width / 5.0f) * 2.0f, 1, ColorUtil.toRGBA(255, 180, 255), ColorUtil.toRGBA(255, 255, 255));
                RenderUtil.drawHGradientRect((width / 5.0f) * 2.0f, 0, (width / 5.0f) * 3.0f, 1, ColorUtil.toRGBA(255, 255, 255), ColorUtil.toRGBA(255, 255, 255));
                RenderUtil.drawHGradientRect((width / 5.0f) * 3.0f, 0, (width / 5.0f) * 4.0f, 1, ColorUtil.toRGBA(255, 255, 255), ColorUtil.toRGBA(255, 180, 255));
                RenderUtil.drawHGradientRect((width / 5.0f) * 4.0f, 0, width, 1, ColorUtil.toRGBA(255, 180, 255), ColorUtil.toRGBA(0, 180, 255));
            }
            if (ClickGui.INSTANCE.rainbow.getValue() && ClickGui.INSTANCE.hudRainbow.getValue() == ClickGui.HudRainbow.ROLLING && !jamie.getValue()) {
                int[] arrayOfInt = { 1 };
                RenderUtil.drawHGradientRect(0, 0, width / 2.0f, 1, ColorUtil.rainbow(arrayOfInt[0] * (ClickGui.INSTANCE).rainbowDelay.getValue().intValue()).getRGB(), ColorUtil.rainbow(20 * (ClickGui.INSTANCE).rainbowDelay.getValue().intValue()).getRGB());
                RenderUtil.drawHGradientRect(width / 2.0f, 0, width, 1, ColorUtil.rainbow(20 * (ClickGui.INSTANCE).rainbowDelay.getValue().intValue()).getRGB(), ColorUtil.rainbow(40 * (ClickGui.INSTANCE).rainbowDelay.getValue().intValue()).getRGB());
                arrayOfInt[ 0 ] = arrayOfInt[ 0 ] + 1;
            }
            if (!ClickGui.INSTANCE.rainbow.getValue() && !jamie.getValue()) {
                RenderUtil.drawHGradientRect(0, 0, width / 2.0f, 1, ColorUtil.pulseColor(color, 50, 1000).getRGB(), ColorUtil.pulseColor(color, 200, 1).getRGB());
                RenderUtil.drawHGradientRect(width / 2.0f, 0, width, 1, ColorUtil.pulseColor(color, 200, 1).getRGB(), ColorUtil.pulseColor(color, 50, 1000).getRGB());
            }
        }

        if (textRadar.getValue()) drawTextRadar(watermark.getValue() ? waterMarkY.getValue() + 2 : 2);

        if (pvp.getValue()) drawPvPInfo();

        this.color = ColorUtil.toRGBA(ClickGui.INSTANCE.color.getValue().getRed(), ClickGui.INSTANCE.color.getValue().getGreen(), ClickGui.INSTANCE.color.getValue().getBlue());
        if (idWatermark.getValue()) {
            String mioString = "mioclient";
            String domainString = ChatFormatting.LIGHT_PURPLE + ".me";

            float offset = Managers.TEXT.scaledHeight / 2.0f - 30.0f;

            if ((ClickGui.INSTANCE).rainbow.getValue()) {
                if ((ClickGui.INSTANCE).hudRainbow.getValue() == ClickGui.HudRainbow.STATIC) {
                    Managers.TEXT.drawString(mioString + domainString, 2.0f, offset, Managers.COLORS.getRainbow().getRGB(), true);
                } else {
                    Managers.TEXT.drawRollingRainbowString(mioString, 2.0f, offset, true);
                    Managers.TEXT.drawString(domainString, Managers.TEXT.getStringWidth(mioString) + (FontMod.INSTANCE.isOn() ? -1.0f : 1.4f), offset, -1, true);
                }
            } else {
                Managers.TEXT.drawString(mioString + domainString, 2.0f, offset, this.color, true);
            }
        }

        int[] counter1 = { 1 };

        boolean inChat = mc.currentScreen instanceof GuiChat;
        long enabledMods = Managers.MODULES.modules.stream().filter(module -> module.isOn() && module.isDrawn()).count();
        int j = (inChat && !renderingUp.getValue()) ? 14 : 0;
        int rectColor = jamieArray.getValue() ? ColorUtil.injectAlpha(getJamieColor(counter1[0] + 1), 60) : arrayListRectColor.getValue() ? (ClickGui.INSTANCE).rainbow.getValue() ? (((ClickGui.INSTANCE).hudRainbow.getValue() == ClickGui.HudRainbow.ROLLING) ? ColorUtil.toRGBA(ColorUtil.rainbow(counter1[0] * (ClickGui.INSTANCE).rainbowDelay.getValue().intValue()).getRed(), ColorUtil.rainbow(counter1[0] * (ClickGui.INSTANCE).rainbowDelay.getValue().intValue()).getGreen(), ColorUtil.rainbow(counter1[0] * (ClickGui.INSTANCE).rainbowDelay.getValue().intValue()).getBlue(), 60) : ColorUtil.toRGBA(Managers.COLORS.getRainbow().getRed(), Managers.COLORS.getRainbow().getGreen(), Managers.COLORS.getRainbow().getBlue(), 60)) : ColorUtil.toRGBA((ColorUtil.pulseColor(color, 50, counter1[0]).getRed()), (ColorUtil.pulseColor(color, 50, counter1[0]).getGreen()), (ColorUtil.pulseColor(color, 50, counter1[0]).getBlue()), 60) : ColorUtil.toRGBA(10, 10, 10, 60);
        int glowColor = jamieArray.getValue() ? ColorUtil.injectAlpha(getJamieColor(counter1[0] + 1), 60) : (ClickGui.INSTANCE).rainbow.getValue() ? (((ClickGui.INSTANCE).hudRainbow.getValue() == ClickGui.HudRainbow.ROLLING) ? ColorUtil.toRGBA(ColorUtil.rainbow(counter1[0] * (ClickGui.INSTANCE).rainbowDelay.getValue().intValue()).getRed(), ColorUtil.rainbow(counter1[0] * (ClickGui.INSTANCE).rainbowDelay.getValue().intValue()).getGreen(), ColorUtil.rainbow(counter1[0] * (ClickGui.INSTANCE).rainbowDelay.getValue().intValue()).getBlue(), 60) : ColorUtil.toRGBA(Managers.COLORS.getRainbow().getRed(), Managers.COLORS.getRainbow().getGreen(), Managers.COLORS.getRainbow().getBlue(), 60)) : ColorUtil.toRGBA((ColorUtil.pulseColor(color, 50, counter1[0]).getRed()), (ColorUtil.pulseColor(color, 50, counter1[0]).getGreen()), (ColorUtil.pulseColor(color, 50, counter1[0]).getBlue()), 60);

        if (arrayList.getValue()) {
            if (renderingUp.getValue()) {
                if (inChat && hideInChat.getValue()) {
                    Managers.TEXT.drawString(enabledMods + " mods enabled", (width - 2 - Managers.TEXT.getStringWidth(enabledMods + " mods enabled")), 2 + j, (ClickGui.INSTANCE).rainbow.getValue() ? (((ClickGui.INSTANCE).hudRainbow.getValue() == ClickGui.HudRainbow.ROLLING) ? ColorUtil.rainbow(counter1[ 0 ] * (ClickGui.INSTANCE).rainbowDelay.getValue().intValue()).getRGB() : Managers.COLORS.getRainbow().getRGB()) : this.color, true);
                } else {
                    if (ordering.getValue() == ModuleManager.Ordering.ABC) {
                        for (int k = 0; k < Managers.MODULES.sortedAbc.size(); k++) {
                            String str = Managers.MODULES.sortedAbc.get(k);
                            if (forgeHax.getValue()) {
                                str = Managers.MODULES.sortedAbc.get(k) + ChatFormatting.RESET + "<";
                            }

                            if (arrayListRect.getValue()) {
                                Gui.drawRect((width - 2 - (lowerCase.getValue() ? Managers.TEXT.getStringWidth(str.toLowerCase()) : Managers.TEXT.getStringWidth(str))) - 1,
                                        j == 0 ? 0 : (2 + j * 10),
                                        width,
                                        (2 + j * 10) + 10,
                                        rectColor);
                            }

                            if (arrayListGlow.getValue()) {
                                RenderUtil.drawGlow((width - 2 - (lowerCase.getValue() ? Managers.TEXT.getStringWidth(str.toLowerCase()) : Managers.TEXT.getStringWidth(str))) - 1,
                                        j == 0 ? 0 : (2 + j * 10) - 4,
                                        width,
                                        (2 + j * 10) + 15,
                                        glowColor);
                            }

                            if (arrayListLine.getValue()) {
                                Gui.drawRect((width - 2 - (lowerCase.getValue() ? Managers.TEXT.getStringWidth(str.toLowerCase()) : Managers.TEXT.getStringWidth(str))) - 2, j == 0 ? 0 : (2 + j * 10) - 1,
                                        (width - 2 - (lowerCase.getValue() ? Managers.TEXT.getStringWidth(str.toLowerCase()) : Managers.TEXT.getStringWidth(str))) - 1, (2 + j * 10) + 10,
                                        jamieArray.getValue() ? getJamieColor(counter1[0] - 2) :
                                        (ClickGui.INSTANCE).rainbow.getValue() ? (((ClickGui.INSTANCE).hudRainbow.getValue() == ClickGui.HudRainbow.ROLLING) ? ColorUtil.rainbow(counter1[0] * (ClickGui.INSTANCE).rainbowDelay.getValue().intValue()).getRGB() : Managers.COLORS.getRainbow().getRGB()) : ColorUtil.pulseColor(color, 50, counter1[0]).getRGB());

                                int a = k + 1;
                                if (a >= Managers.MODULES.sortedAbc.size()) a = k;
                                String nextStr = Managers.MODULES.sortedAbc.get(a);
                                if (forgeHax.getValue()) {
                                    nextStr = Managers.MODULES.sortedAbc.get(a) + ChatFormatting.RESET + "<";
                                }

                                Gui.drawRect((width - 2 - (lowerCase.getValue() ? Managers.TEXT.getStringWidth(str.toLowerCase()) : Managers.TEXT.getStringWidth(str))) - 2, (2 + (j + 1) * 10) - 1,
                                        a == k ? width : (width - 2 - ((lowerCase.getValue() ? Managers.TEXT.getStringWidth(str.toLowerCase()) : Managers.TEXT.getStringWidth(str))) +
                                                (((lowerCase.getValue() ? Managers.TEXT.getStringWidth(str.toLowerCase()) : Managers.TEXT.getStringWidth(str)) -
                                                        (lowerCase.getValue() ? Managers.TEXT.getStringWidth(nextStr.toLowerCase()) : Managers.TEXT.getStringWidth(nextStr))))) - 1,
                                        (2 + (j + 1) * 10),
                                        jamieArray.getValue() ? getJamieColor(counter1[0] - 2) :
                                        (ClickGui.INSTANCE).rainbow.getValue() ? (((ClickGui.INSTANCE).hudRainbow.getValue() == ClickGui.HudRainbow.ROLLING) ? ColorUtil.rainbow(counter1[0] * (ClickGui.INSTANCE).rainbowDelay.getValue().intValue()).getRGB() : Managers.COLORS.getRainbow().getRGB()) : ColorUtil.pulseColor(color, 50, counter1[0]).getRGB());
                            }

                            Managers.TEXT.drawString(lowerCase.getValue() ? str.toLowerCase() : str,
                                    (width - 2 - (lowerCase.getValue() ? Managers.TEXT.getStringWidth(str.toLowerCase()) :  Managers.TEXT.getStringWidth(str))), (2 + j * 10),
                                    jamieArray.getValue() ? getJamieColor(counter1[0] - 2) :
                                    (ClickGui.INSTANCE).rainbow.getValue() ? (((ClickGui.INSTANCE).hudRainbow.getValue() == ClickGui.HudRainbow.ROLLING) ? ColorUtil.rainbow(counter1[ 0 ] * (ClickGui.INSTANCE).rainbowDelay.getValue().intValue()).getRGB() : Managers.COLORS.getRainbow().getRGB()) : ColorUtil.pulseColor(color, 50, counter1[0]).getRGB(), true);
                            j++;
                            counter1[ 0 ] = counter1[ 0 ] + 1;
                        }
                    } else {
                        for (int k = 0; k < Managers.MODULES.sortedLength.size(); k++) {
                            Module module = Managers.MODULES.sortedLength.get(k);
                            String str = module.getName() + ChatFormatting.GRAY + ((module.getInfo() != null) ? (" [" + ChatFormatting.WHITE + module.getInfo() + ChatFormatting.GRAY + "]") : "");
                            if (forgeHax.getValue()) {
                                str = module.getName() + ChatFormatting.GRAY + ((module.getInfo() != null) ? (" [" + ChatFormatting.WHITE + module.getInfo() + ChatFormatting.GRAY + "]" + ChatFormatting.RESET + "<") : ChatFormatting.RESET + "<");
                            }

                            if (arrayListRect.getValue()) {
                                Gui.drawRect((width - 2 - (lowerCase.getValue() ? Managers.TEXT.getStringWidth(str.toLowerCase()) : Managers.TEXT.getStringWidth(str))) - 1,
                                        j == 0 ? 0 : (2 + j * 10),
                                        width,
                                        (2 + j * 10) + 10,
                                        rectColor);
                            }

                            if (arrayListGlow.getValue()) {
                                RenderUtil.drawGlow((width - 2 - (lowerCase.getValue() ? Managers.TEXT.getStringWidth(str.toLowerCase()) : Managers.TEXT.getStringWidth(str))) - 1,
                                        j == 0 ? 0 : (2 + j * 10) - 4,
                                        width,
                                        (2 + j * 10) + 15,
                                        glowColor);
                            }

                            if (arrayListLine.getValue()) {
                                Gui.drawRect((width - 2 - (lowerCase.getValue() ? Managers.TEXT.getStringWidth(str.toLowerCase()) : Managers.TEXT.getStringWidth(str))) - 2, j == 0 ? 0 : (2 + j * 10),
                                        (width - 2 - (lowerCase.getValue() ? Managers.TEXT.getStringWidth(str.toLowerCase()) : Managers.TEXT.getStringWidth(str))) - 1, (2 + j * 10) + 10,
                                        jamieArray.getValue() ? getJamieColor(counter1[0] - 2) :
                                        (ClickGui.INSTANCE).rainbow.getValue() ? (((ClickGui.INSTANCE).hudRainbow.getValue() == ClickGui.HudRainbow.ROLLING) ? ColorUtil.rainbow(counter1[0] * (ClickGui.INSTANCE).rainbowDelay.getValue().intValue()).getRGB() : Managers.COLORS.getRainbow().getRGB()) : ColorUtil.pulseColor(color, 50, counter1[0]).getRGB());

                                int a = k + 1;
                                if (a >= Managers.MODULES.sortedLength.size()) a = k;
                                Module nextModule = Managers.MODULES.sortedLength.get(a);
                                String nextStr = nextModule.getName() + ChatFormatting.GRAY + ((nextModule.getInfo() != null) ? (" [" + ChatFormatting.WHITE + nextModule.getInfo() + ChatFormatting.GRAY + "]") : "");
                                if (forgeHax.getValue()) {
                                    nextStr = nextModule.getName() + ChatFormatting.GRAY + ((nextModule.getInfo() != null) ? (" [" + ChatFormatting.WHITE + nextModule.getInfo() + ChatFormatting.GRAY + "]" + ChatFormatting.RESET + "<") : ChatFormatting.RESET + "<");
                                }

                                Gui.drawRect((width - 2 - (lowerCase.getValue() ? Managers.TEXT.getStringWidth(str.toLowerCase()) : Managers.TEXT.getStringWidth(str))) - 2, (2 + (j + 1) * 10),
                                        a == k ? width : (width - 2 - ((lowerCase.getValue() ? Managers.TEXT.getStringWidth(str.toLowerCase()) : Managers.TEXT.getStringWidth(str))) +
                                                (((lowerCase.getValue() ? Managers.TEXT.getStringWidth(str.toLowerCase()) : Managers.TEXT.getStringWidth(str)) -
                                                        (lowerCase.getValue() ? Managers.TEXT.getStringWidth(nextStr.toLowerCase()) : Managers.TEXT.getStringWidth(nextStr))))) - 1,
                                        (2 + (j + 1) * 10) + 1,
                                        jamieArray.getValue() ? getJamieColor(counter1[0] - 2) :
                                        (ClickGui.INSTANCE).rainbow.getValue() ? (((ClickGui.INSTANCE).hudRainbow.getValue() == ClickGui.HudRainbow.ROLLING) ? ColorUtil.rainbow(counter1[0] * (ClickGui.INSTANCE).rainbowDelay.getValue().intValue()).getRGB() : Managers.COLORS.getRainbow().getRGB()) : ColorUtil.pulseColor(color, 50, counter1[0]).getRGB());
                            }

                            Managers.TEXT.drawString(lowerCase.getValue() ? str.toLowerCase() : str,
                                    (width - 2 - (lowerCase.getValue() ? Managers.TEXT.getStringWidth(str.toLowerCase()) :  Managers.TEXT.getStringWidth(str))), (2 + j * 10),
                                    jamieArray.getValue() ? getJamieColor(counter1[0] - 2) :
                                    (ClickGui.INSTANCE).rainbow.getValue() ? (((ClickGui.INSTANCE).hudRainbow.getValue() == ClickGui.HudRainbow.ROLLING) ? ColorUtil.rainbow(counter1[ 0 ] * (ClickGui.INSTANCE).rainbowDelay.getValue().intValue()).getRGB() : Managers.COLORS.getRainbow().getRGB()) : ColorUtil.pulseColor(color, 50, counter1[0]).getRGB(), true);
                            j++;
                            counter1[ 0 ] = counter1[ 0 ] + 1;
                        }
                    }
                }
            } else {
                if (inChat && hideInChat.getValue()) {
                    Managers.TEXT.drawString(enabledMods + " mods enabled", (width - 2 - Managers.TEXT.getStringWidth(enabledMods + " mods enabled")), height - j - 11, (ClickGui.INSTANCE).rainbow.getValue() ? (((ClickGui.INSTANCE).hudRainbow.getValue() == ClickGui.HudRainbow.ROLLING) ? ColorUtil.rainbow(counter1[ 0 ] * (ClickGui.INSTANCE).rainbowDelay.getValue().intValue()).getRGB() : Managers.COLORS.getRainbow().getRGB()) : this.color, true);
                } else {
                    if (ordering.getValue() == ModuleManager.Ordering.ABC) {
                        for (int k = 0; k < Managers.MODULES.sortedAbc.size(); k++) {
                            String str = Managers.MODULES.sortedAbc.get(k);
                            if (forgeHax.getValue()) {
                                str = Managers.MODULES.sortedAbc.get(k) + ChatFormatting.RESET + "<";
                            }
                            j += 10;

                            if (arrayListRect.getValue()) {
                                Gui.drawRect((width - 2 - (lowerCase.getValue() ? Managers.TEXT.getStringWidth(str.toLowerCase()) : Managers.TEXT.getStringWidth(str))) - 1,
                                        (height - j),
                                        width,
                                        j == 1 ? height : (height - j) + 10,
                                        rectColor);
                            }

                            if (arrayListGlow.getValue()) {
                                RenderUtil.drawGlow((width - 2 - (lowerCase.getValue() ? Managers.TEXT.getStringWidth(str.toLowerCase()) : Managers.TEXT.getStringWidth(str))) - 1,
                                        (height - j) - 4,
                                        width,
                                        j == 1 ? height : (height - j) + 15,
                                        glowColor);
                            }

                            if (arrayListLine.getValue()) {
                                Gui.drawRect((width - 2 - (lowerCase.getValue() ? Managers.TEXT.getStringWidth(str.toLowerCase()) : Managers.TEXT.getStringWidth(str))) - 2, (height - j),
                                        (width - 2 - (lowerCase.getValue() ? Managers.TEXT.getStringWidth(str.toLowerCase()) : Managers.TEXT.getStringWidth(str))) - 1, j == 1 ? height : (height - j) + 10,
                                        jamieArray.getValue() ? getJamieColor(counter1[0] - 2) :
                                        (ClickGui.INSTANCE).rainbow.getValue() ? (((ClickGui.INSTANCE).hudRainbow.getValue() == ClickGui.HudRainbow.ROLLING) ? ColorUtil.rainbow(counter1[0] * (ClickGui.INSTANCE).rainbowDelay.getValue().intValue()).getRGB() : Managers.COLORS.getRainbow().getRGB()) : ColorUtil.pulseColor(color, 50, counter1[0]).getRGB());

                                int a = k + 1;
                                if (a >= Managers.MODULES.sortedAbc.size()) a = k;
                                String nextStr = Managers.MODULES.sortedAbc.get(a);
                                if (forgeHax.getValue()) {
                                    nextStr = Managers.MODULES.sortedAbc.get(a) + ChatFormatting.RESET + "<";
                                }

                                Gui.drawRect((width - 2 - (lowerCase.getValue() ? Managers.TEXT.getStringWidth(str.toLowerCase()) : Managers.TEXT.getStringWidth(str))) - 2, (height - j) - 1,
                                        a == k ? width : (width - 2 - ((lowerCase.getValue() ? Managers.TEXT.getStringWidth(str.toLowerCase()) : Managers.TEXT.getStringWidth(str))) +
                                                (((lowerCase.getValue() ? Managers.TEXT.getStringWidth(str.toLowerCase()) : Managers.TEXT.getStringWidth(str)) -
                                                        (lowerCase.getValue() ? Managers.TEXT.getStringWidth(nextStr.toLowerCase()) : Managers.TEXT.getStringWidth(nextStr))))) - 1,
                                        j == 1 ? height : (height - j),
                                        jamieArray.getValue() ? getJamieColor(counter1[0] - 2) :
                                        (ClickGui.INSTANCE).rainbow.getValue() ? (((ClickGui.INSTANCE).hudRainbow.getValue() == ClickGui.HudRainbow.ROLLING) ? ColorUtil.rainbow(counter1[0] * (ClickGui.INSTANCE).rainbowDelay.getValue().intValue()).getRGB() : Managers.COLORS.getRainbow().getRGB()) : ColorUtil.pulseColor(color, 50, counter1[0]).getRGB());
                            }

                            Managers.TEXT.drawString(lowerCase.getValue() ? str.toLowerCase() : str,
                                    (width - 2 - (lowerCase.getValue() ? Managers.TEXT.getStringWidth(str.toLowerCase()) :  Managers.TEXT.getStringWidth(str))), (height - j),
                                    jamieArray.getValue() ? getJamieColor(counter1[0] - 2) :
                                    (ClickGui.INSTANCE).rainbow.getValue() ? (((ClickGui.INSTANCE).hudRainbow.getValue() == ClickGui.HudRainbow.ROLLING) ? ColorUtil.rainbow(counter1[ 0 ] * (ClickGui.INSTANCE).rainbowDelay.getValue().intValue()).getRGB() : Managers.COLORS.getRainbow().getRGB()) : ColorUtil.pulseColor(color, 50, counter1[0]).getRGB(), true);
                            counter1[ 0 ] = counter1[ 0 ] + 1;
                        }
                    } else {
                        for (int k = 0; k < Managers.MODULES.sortedLength.size(); k++) {
                            Module module = Managers.MODULES.sortedLength.get(k);
                            String str = module.getName() + ChatFormatting.GRAY + ((module.getInfo() != null) ? (" [" + ChatFormatting.WHITE + module.getInfo() + ChatFormatting.GRAY + "]") : "");
                            if (forgeHax.getValue()) {
                                str = module.getName() + ChatFormatting.GRAY + ((module.getInfo() != null) ? (" [" + ChatFormatting.WHITE + module.getInfo() + ChatFormatting.GRAY + "]" + ChatFormatting.RESET + "<") : ChatFormatting.RESET + "<");
                            }
                            j += 10;

                            if (arrayListRect.getValue()) {
                                Gui.drawRect((width - 2 - (lowerCase.getValue() ? Managers.TEXT.getStringWidth(str.toLowerCase()) : Managers.TEXT.getStringWidth(str))) - 1,
                                        (height - j),
                                        width,
                                        j == 1 ? height : (height - j) + 10,
                                        rectColor);
                            }

                            if (arrayListGlow.getValue()) {
                                RenderUtil.drawGlow((width - 2 - (lowerCase.getValue() ? Managers.TEXT.getStringWidth(str.toLowerCase()) : Managers.TEXT.getStringWidth(str))) - 1,
                                        (height - j) - 4,
                                        width,
                                        j == 1 ? height : (height - j) + 15,
                                        glowColor);
                            }

                            if (arrayListLine.getValue()) {
                                Gui.drawRect((width - 2 - (lowerCase.getValue() ? Managers.TEXT.getStringWidth(str.toLowerCase()) : Managers.TEXT.getStringWidth(str))) - 2, (height - j),
                                        (width - 2 - (lowerCase.getValue() ? Managers.TEXT.getStringWidth(str.toLowerCase()) : Managers.TEXT.getStringWidth(str))) - 1, j == 1 ? height : (height - j) + 10,
                                        jamieArray.getValue() ? getJamieColor(counter1[0] - 2) :
                                        (ClickGui.INSTANCE).rainbow.getValue() ? (((ClickGui.INSTANCE).hudRainbow.getValue() == ClickGui.HudRainbow.ROLLING) ? ColorUtil.rainbow(counter1[0] * (ClickGui.INSTANCE).rainbowDelay.getValue().intValue()).getRGB() : Managers.COLORS.getRainbow().getRGB()) : ColorUtil.pulseColor(color, 50, counter1[0]).getRGB());

                                int a = k + 1;
                                if (a >= Managers.MODULES.sortedLength.size()) a = k;
                                Module nextModule = Managers.MODULES.sortedLength.get(a);
                                String nextStr = nextModule.getName() + ChatFormatting.GRAY + ((nextModule.getInfo() != null) ? (" [" + ChatFormatting.WHITE + nextModule.getInfo() + ChatFormatting.GRAY + "]") : "");
                                if (forgeHax.getValue()) {
                                    nextStr = nextModule.getName() + ChatFormatting.GRAY + ((nextModule.getInfo() != null) ? (" [" + ChatFormatting.WHITE + nextModule.getInfo() + ChatFormatting.GRAY + "]" + ChatFormatting.RESET + "<") : ChatFormatting.RESET + "<");
                                }

                                Gui.drawRect((width - 2 - (lowerCase.getValue() ? Managers.TEXT.getStringWidth(str.toLowerCase()) : Managers.TEXT.getStringWidth(str))) - 2, (height - j) - 1,
                                        a == k ? width : (width - 2 - ((lowerCase.getValue() ? Managers.TEXT.getStringWidth(str.toLowerCase()) : Managers.TEXT.getStringWidth(str))) +
                                                (((lowerCase.getValue() ? Managers.TEXT.getStringWidth(str.toLowerCase()) : Managers.TEXT.getStringWidth(str)) -
                                                        (lowerCase.getValue() ? Managers.TEXT.getStringWidth(nextStr.toLowerCase()) : Managers.TEXT.getStringWidth(nextStr))))) - 1,
                                        (height - j),
                                        jamieArray.getValue() ? getJamieColor(counter1[0] - 2) :
                                        (ClickGui.INSTANCE).rainbow.getValue() ? (((ClickGui.INSTANCE).hudRainbow.getValue() == ClickGui.HudRainbow.ROLLING) ? ColorUtil.rainbow(counter1[0] * (ClickGui.INSTANCE).rainbowDelay.getValue().intValue()).getRGB() : Managers.COLORS.getRainbow().getRGB()) : ColorUtil.pulseColor(color, 50, counter1[0]).getRGB());
                            }

                            Managers.TEXT.drawString(lowerCase.getValue() ? str.toLowerCase() : str,
                                    (width - 2 - (lowerCase.getValue() ? Managers.TEXT.getStringWidth(str.toLowerCase()) :  Managers.TEXT.getStringWidth(str))), (height - j),
                                    jamieArray.getValue() ? getJamieColor(counter1[0] - 2) :
                                    (ClickGui.INSTANCE).rainbow.getValue() ? (((ClickGui.INSTANCE).hudRainbow.getValue() == ClickGui.HudRainbow.ROLLING) ? ColorUtil.rainbow(counter1[ 0 ] * (ClickGui.INSTANCE).rainbowDelay.getValue().intValue()).getRGB() : Managers.COLORS.getRainbow().getRGB()) : ColorUtil.pulseColor(color, 50, counter1[0]).getRGB(), true);
                            counter1[ 0 ] = counter1[ 0 ] + 1;
                        }
                    }
                }
            }
        }
        String grayString = grayColors.getValue() ? String.valueOf(ChatFormatting.GRAY) : "";
        int i = (mc.currentScreen instanceof net.minecraft.client.gui.GuiChat && renderingUp.getValue()) ? 13 : (renderingUp.getValue() ? -2 : 0);

        if (renderingUp.getValue()) {
            if (potions.getValue()) {
                List<PotionEffect> effects = new ArrayList<>((Minecraft.getMinecraft()).player.getActivePotionEffects());
                for (PotionEffect potionEffect : effects) {
                    String str = getColoredPotionString(potionEffect);
                    i += 10;

                    Managers.TEXT.drawString(lowerCase.getValue() ? str.toLowerCase() : str,
                            (width - (lowerCase.getValue() ? Managers.TEXT.getStringWidth(str.toLowerCase()) :  Managers.TEXT.getStringWidth(str)) - 2),
                            (height - 2 - i),
                            potionColor.getValue() ? potionEffect.getPotion().getLiquidColor() :
                                    ((ClickGui.INSTANCE).rainbow.getValue() ? (((ClickGui.INSTANCE).hudRainbow.getValue() == ClickGui.HudRainbow.ROLLING) ? ColorUtil.rainbow(counter1[ 0 ] * (ClickGui.INSTANCE).rainbowDelay.getValue().intValue()).getRGB() : Managers.COLORS.getRainbow().getRGB()) : ColorUtil.pulseColor(color, 50, counter1[0]).getRGB()), true);
                    counter1[ 0 ] = counter1[ 0 ] + 1;
                }
            }
            if (speed.getValue()) {
                String str = grayString + "Speed " + ChatFormatting.WHITE + Managers.SPEED.getSpeedKpH() + " km/h";
                i += 10;

                Managers.TEXT.drawString(lowerCase.getValue() ? str.toLowerCase() : str,
                        (width - (lowerCase.getValue() ? Managers.TEXT.getStringWidth(str.toLowerCase()) :  Managers.TEXT.getStringWidth(str)) - 2),
                        (height - 2 - i), (ClickGui.INSTANCE).rainbow.getValue() ? (((ClickGui.INSTANCE).hudRainbow.getValue() == ClickGui.HudRainbow.ROLLING) ? ColorUtil.rainbow(counter1[ 0 ] * (ClickGui.INSTANCE).rainbowDelay.getValue().intValue()).getRGB() : Managers.COLORS.getRainbow().getRGB()) : ColorUtil.pulseColor(color, 50, counter1[0]).getRGB(), true);
                counter1[ 0 ] = counter1[ 0 ] + 1;
            }
            if (time.getValue()) {
                String str = grayString + "Time " + ChatFormatting.WHITE + (new SimpleDateFormat("h:mm a")).format(new Date());
                i += 10;

                Managers.TEXT.drawString(lowerCase.getValue() ? str.toLowerCase() : str,
                        (width - (lowerCase.getValue() ? Managers.TEXT.getStringWidth(str.toLowerCase()) :  Managers.TEXT.getStringWidth(str)) - 2),
                        (height - 2 - i), (ClickGui.INSTANCE).rainbow.getValue() ? (((ClickGui.INSTANCE).hudRainbow.getValue() == ClickGui.HudRainbow.ROLLING) ? ColorUtil.rainbow(counter1[ 0 ] * (ClickGui.INSTANCE).rainbowDelay.getValue().intValue()).getRGB() : Managers.COLORS.getRainbow().getRGB()) : ColorUtil.pulseColor(color, 50, counter1[0]).getRGB(), true);
                counter1[ 0 ] = counter1[ 0 ] + 1;
            }
            if (tps.getValue()) {
                String str = grayString + "TPS " + ChatFormatting.WHITE + Managers.SERVER.getTPS();
                i += 10;

                Managers.TEXT.drawString(lowerCase.getValue() ? str.toLowerCase() : str,
                        (width - (lowerCase.getValue() ? Managers.TEXT.getStringWidth(str.toLowerCase()) :  Managers.TEXT.getStringWidth(str)) - 2),
                        (height - 2 - i), (ClickGui.INSTANCE).rainbow.getValue() ? (((ClickGui.INSTANCE).hudRainbow.getValue() == ClickGui.HudRainbow.ROLLING) ? ColorUtil.rainbow(counter1[ 0 ] * (ClickGui.INSTANCE).rainbowDelay.getValue().intValue()).getRGB() : Managers.COLORS.getRainbow().getRGB()) : ColorUtil.pulseColor(color, 50, counter1[0]).getRGB(), true);
                counter1[ 0 ] = counter1[ 0 ] + 1;
            }

            String fpsText = grayString + "FPS " + ChatFormatting.WHITE + Managers.FPS.getFPS();
            String str1 = grayString + "Ping " + ChatFormatting.WHITE + Managers.SERVER.getPing();

            if (Managers.TEXT.getStringWidth(str1) > Managers.TEXT.getStringWidth(fpsText)) {
                if (ping.getValue()) {
                 i += 10;

                 Managers.TEXT.drawString(lowerCase.getValue() ? str1.toLowerCase() : str1,
                         (width - (lowerCase.getValue() ? Managers.TEXT.getStringWidth(str1.toLowerCase()) :  Managers.TEXT.getStringWidth(str1)) - 2),
                         (height - 2 - i), (ClickGui.INSTANCE).rainbow.getValue() ? (((ClickGui.INSTANCE).hudRainbow.getValue() == ClickGui.HudRainbow.ROLLING) ? ColorUtil.rainbow(counter1[0] * (ClickGui.INSTANCE).rainbowDelay.getValue().intValue()).getRGB() : Managers.COLORS.getRainbow().getRGB()) : ColorUtil.pulseColor(color, 50, counter1[0]).getRGB(), true);
                 counter1[0] = counter1[0] + 1;
                 }
                if (fps.getValue()) {
                    i += 10;

                    Managers.TEXT.drawString(lowerCase.getValue() ? fpsText.toLowerCase() : fpsText,
                            (width - (lowerCase.getValue() ? Managers.TEXT.getStringWidth(fpsText.toLowerCase()) :  Managers.TEXT.getStringWidth(fpsText)) - 2),
                            (height - 2 - i), (ClickGui.INSTANCE).rainbow.getValue() ? (((ClickGui.INSTANCE).hudRainbow.getValue() == ClickGui.HudRainbow.ROLLING) ? ColorUtil.rainbow(counter1[ 0 ] * (ClickGui.INSTANCE).rainbowDelay.getValue().intValue()).getRGB() : Managers.COLORS.getRainbow().getRGB()) : ColorUtil.pulseColor(color, 50, counter1[0]).getRGB(), true);
                    counter1[ 0 ] = counter1[ 0 ] + 1;
                }
            } else {
                if (fps.getValue()) {
                    i += 10;

                    Managers.TEXT.drawString(lowerCase.getValue() ? fpsText.toLowerCase() : fpsText,
                            (width - (lowerCase.getValue() ? Managers.TEXT.getStringWidth(fpsText.toLowerCase()) :  Managers.TEXT.getStringWidth(fpsText)) - 2),
                            (height - 2 - i), (ClickGui.INSTANCE).rainbow.getValue() ? (((ClickGui.INSTANCE).hudRainbow.getValue() == ClickGui.HudRainbow.ROLLING) ? ColorUtil.rainbow(counter1[ 0 ] * (ClickGui.INSTANCE).rainbowDelay.getValue().intValue()).getRGB() : Managers.COLORS.getRainbow().getRGB()) : ColorUtil.pulseColor(color, 50, counter1[0]).getRGB(), true);
                    counter1[ 0 ] = counter1[ 0 ] + 1;
                }
                if (ping.getValue()) {
                 i += 10;

                 Managers.TEXT.drawString(lowerCase.getValue() ? str1.toLowerCase() : str1,
                         (width - (lowerCase.getValue() ? Managers.TEXT.getStringWidth(str1.toLowerCase()) :  Managers.TEXT.getStringWidth(str1)) - 2),
                         (height - 2 - i), (ClickGui.INSTANCE).rainbow.getValue() ? (((ClickGui.INSTANCE).hudRainbow.getValue() == ClickGui.HudRainbow.ROLLING) ? ColorUtil.rainbow(counter1[0] * (ClickGui.INSTANCE).rainbowDelay.getValue().intValue()).getRGB() : Managers.COLORS.getRainbow().getRGB()) : ColorUtil.pulseColor(color, 50, counter1[0]).getRGB(), true);
                 counter1[0] = counter1[0] + 1;
                 }
            }
        } else {
            if (potions.getValue()) {
                List<PotionEffect> effects = new ArrayList<>((Minecraft.getMinecraft()).player.getActivePotionEffects());
                for (PotionEffect potionEffect : effects) {
                    String str = getColoredPotionString(potionEffect);

                    Managers.TEXT.drawString(lowerCase.getValue() ? str.toLowerCase() : str,
                            (width - (lowerCase.getValue() ? Managers.TEXT.getStringWidth(str.toLowerCase()) :  Managers.TEXT.getStringWidth(str)) - 2),
                            (2 + i++ * 10),
                            potionColor.getValue() ? potionEffect.getPotion().getLiquidColor() :
                                    ((ClickGui.INSTANCE).rainbow.getValue() ? (((ClickGui.INSTANCE).hudRainbow.getValue() == ClickGui.HudRainbow.ROLLING) ? ColorUtil.rainbow(counter1[ 0 ] * (ClickGui.INSTANCE).rainbowDelay.getValue().intValue()).getRGB() : Managers.COLORS.getRainbow().getRGB()) : ColorUtil.pulseColor(color, 50, counter1[0]).getRGB()), true);
                    counter1[ 0 ] = counter1[ 0 ] + 1;
                }
            }
            if (speed.getValue()) {
                String str = grayString + "Speed " + ChatFormatting.WHITE + Managers.SPEED.getSpeedKpH() + " km/h";

                Managers.TEXT.drawString(lowerCase.getValue() ? str.toLowerCase() : str,
                        (width - (lowerCase.getValue() ? Managers.TEXT.getStringWidth(str.toLowerCase()) :  Managers.TEXT.getStringWidth(str)) - 2),
                        (2 + i++ * 10), (ClickGui.INSTANCE).rainbow.getValue() ? (((ClickGui.INSTANCE).hudRainbow.getValue() == ClickGui.HudRainbow.ROLLING) ? ColorUtil.rainbow(counter1[ 0 ] * (ClickGui.INSTANCE).rainbowDelay.getValue().intValue()).getRGB() : Managers.COLORS.getRainbow().getRGB()) : ColorUtil.pulseColor(color, 50, counter1[0]).getRGB(), true);
                counter1[ 0 ] = counter1[ 0 ] + 1;
            }
            if (time.getValue()) {
                String str = grayString + "Time " + ChatFormatting.WHITE + (new SimpleDateFormat("h:mm a")).format(new Date());

                Managers.TEXT.drawString(lowerCase.getValue() ? str.toLowerCase() : str,
                        (width - (lowerCase.getValue() ? Managers.TEXT.getStringWidth(str.toLowerCase()) :  Managers.TEXT.getStringWidth(str)) - 2),
                        (2 + i++ * 10), (ClickGui.INSTANCE).rainbow.getValue() ? (((ClickGui.INSTANCE).hudRainbow.getValue() == ClickGui.HudRainbow.ROLLING) ? ColorUtil.rainbow(counter1[ 0 ] * (ClickGui.INSTANCE).rainbowDelay.getValue().intValue()).getRGB() : Managers.COLORS.getRainbow().getRGB()) : ColorUtil.pulseColor(color, 50, counter1[0]).getRGB(), true);
                counter1[ 0 ] = counter1[ 0 ] + 1;
            }
            if (tps.getValue()) {
                String str = grayString + "TPS " + ChatFormatting.WHITE + Managers.SERVER.getTPS();

                Managers.TEXT.drawString(lowerCase.getValue() ? str.toLowerCase() : str,
                        (width - (lowerCase.getValue() ? Managers.TEXT.getStringWidth(str.toLowerCase()) :  Managers.TEXT.getStringWidth(str)) - 2),
                        (2 + i++ * 10), (ClickGui.INSTANCE).rainbow.getValue() ? (((ClickGui.INSTANCE).hudRainbow.getValue() == ClickGui.HudRainbow.ROLLING) ? ColorUtil.rainbow(counter1[ 0 ] * (ClickGui.INSTANCE).rainbowDelay.getValue().intValue()).getRGB() : Managers.COLORS.getRainbow().getRGB()) : ColorUtil.pulseColor(color, 50, counter1[0]).getRGB(), true);
                counter1[ 0 ] = counter1[ 0 ] + 1;
            }

            String fpsText = grayString + "FPS " + ChatFormatting.WHITE + Managers.FPS.getFPS();
            String str1 = grayString + "Ping " + ChatFormatting.WHITE + Managers.SERVER.getPing();

            if (Managers.TEXT.getStringWidth(str1) > Managers.TEXT.getStringWidth(fpsText)) {
                if (ping.getValue()) {
                 Managers.TEXT.drawString(lowerCase.getValue() ? str1.toLowerCase() : str1,
                         (width - (lowerCase.getValue() ? Managers.TEXT.getStringWidth(str1.toLowerCase()) :  Managers.TEXT.getStringWidth(str1)) - 2),
                         (2 + i++ * 10), (ClickGui.INSTANCE).rainbow.getValue() ? (((ClickGui.INSTANCE).hudRainbow.getValue() == ClickGui.HudRainbow.ROLLING) ? ColorUtil.rainbow(counter1[0] * (ClickGui.INSTANCE).rainbowDelay.getValue().intValue()).getRGB() : Managers.COLORS.getRainbow().getRGB()) : ColorUtil.pulseColor(color, 50, counter1[0]).getRGB(), true);
                 counter1[0] = counter1[0] + 1;
                 }
                if (fps.getValue()) {
                    Managers.TEXT.drawString(lowerCase.getValue() ? fpsText.toLowerCase() : fpsText,
                            (width - (lowerCase.getValue() ? Managers.TEXT.getStringWidth(fpsText.toLowerCase()) :  Managers.TEXT.getStringWidth(fpsText)) - 2),
                            (2 + i++ * 10), (ClickGui.INSTANCE).rainbow.getValue() ? (((ClickGui.INSTANCE).hudRainbow.getValue() == ClickGui.HudRainbow.ROLLING) ? ColorUtil.rainbow(counter1[ 0 ] * (ClickGui.INSTANCE).rainbowDelay.getValue().intValue()).getRGB() : Managers.COLORS.getRainbow().getRGB()) : ColorUtil.pulseColor(color, 50, counter1[0]).getRGB(), true);
                    counter1[ 0 ] = counter1[ 0 ] + 1;
                }
            } else {
                if (fps.getValue()) {
                    Managers.TEXT.drawString(lowerCase.getValue() ? fpsText.toLowerCase() : fpsText,
                            (width - (lowerCase.getValue() ? Managers.TEXT.getStringWidth(fpsText.toLowerCase()) :  Managers.TEXT.getStringWidth(fpsText)) - 2),
                            (2 + i++ * 10), (ClickGui.INSTANCE).rainbow.getValue() ? (((ClickGui.INSTANCE).hudRainbow.getValue() == ClickGui.HudRainbow.ROLLING) ? ColorUtil.rainbow(counter1[ 0 ] * (ClickGui.INSTANCE).rainbowDelay.getValue().intValue()).getRGB() : Managers.COLORS.getRainbow().getRGB()) : ColorUtil.pulseColor(color, 50, counter1[0]).getRGB(), true);
                    counter1[ 0 ] = counter1[ 0 ] + 1;
                }
                if (ping.getValue()) {
                 Managers.TEXT.drawString(lowerCase.getValue() ? str1.toLowerCase() : str1,
                         (width - (lowerCase.getValue() ? Managers.TEXT.getStringWidth(str1.toLowerCase()) :  Managers.TEXT.getStringWidth(str1)) - 2),
                         (2 + i++ * 10), (ClickGui.INSTANCE).rainbow.getValue() ? (((ClickGui.INSTANCE).hudRainbow.getValue() == ClickGui.HudRainbow.ROLLING) ? ColorUtil.rainbow(counter1[0] * (ClickGui.INSTANCE).rainbowDelay.getValue().intValue()).getRGB() : Managers.COLORS.getRainbow().getRGB()) : ColorUtil.pulseColor(color, 50, counter1[0]).getRGB(), true);
                 counter1[0] = counter1[0] + 1;
                 }
            }
        }

        boolean inHell = mc.world.getBiome(mc.player.getPosition()).getBiomeName().equals("Hell");

        int posX = ( int ) mc.player.posX;
        int posY = ( int ) mc.player.posY;
        int posZ = ( int ) mc.player.posZ;

        float nether = !inHell ? 0.125F : 8.0F;

        int hposX = ( int ) (mc.player.posX * nether);
        int hposZ = ( int ) (mc.player.posZ * nether);
        int yawPitch = ( int ) MathHelper.wrapDegrees(mc.player.rotationYaw);
        int p = coords.getValue() ? 0 : 11;

        i = (mc.currentScreen instanceof net.minecraft.client.gui.GuiChat) ? 14 : 0;

        String coordinates = (lowerCase.getValue() ? "XYZ: ".toLowerCase() : "XYZ: ") + ChatFormatting.WHITE + (inHell ? (posX + ", " + posY + ", " + posZ + ChatFormatting.GRAY + " [" + ChatFormatting.WHITE + hposX + ", " + hposZ + ChatFormatting.GRAY + "]" + ChatFormatting.WHITE) : (posX + ", " + posY + ", " + posZ + ChatFormatting.GRAY + " [" + ChatFormatting.WHITE + hposX + ", " + hposZ + ChatFormatting.GRAY + "]"));
        String direction = this.direction.getValue() ? Managers.ROTATIONS.getDirection4D(false) : "";
        String yaw = this.direction.getValue() ? (lowerCase.getValue() ? "Yaw: ".toLowerCase() : "Yaw: ") + ChatFormatting.WHITE + yawPitch : "";
        String coords = this.coords.getValue() ? coordinates : "";

        i += 10;

        if (mc.currentScreen instanceof net.minecraft.client.gui.GuiChat && this.direction.getValue()) {
            yaw = "";
            direction = (lowerCase.getValue() ? "Yaw: ".toLowerCase() : "Yaw: ") + ChatFormatting.WHITE + yawPitch + ChatFormatting.RESET + " " + getFacingDirectionShort();
        }
        if ((ClickGui.INSTANCE).rainbow.getValue()) {
            String rainbowCoords = this.coords.getValue() ? ((lowerCase.getValue() ? "XYZ: ".toLowerCase() : "XYZ: ") + ChatFormatting.WHITE + (inHell ? (posX + ", " + posY + ", " + posZ + ChatFormatting.GRAY + " [" + ChatFormatting.WHITE + hposX + ", " + hposZ + ChatFormatting.GRAY + "]" + ChatFormatting.WHITE) : (posX + ", " + posY + ", " + posZ + ChatFormatting.GRAY + " [" + ChatFormatting.WHITE + hposX + ", " + hposZ + ChatFormatting.GRAY + "]"))) : "";
            if ((ClickGui.INSTANCE).hudRainbow.getValue() == ClickGui.HudRainbow.STATIC) {
                Managers.TEXT.drawString(direction, 2.0F, (height - i - 11 + p), Managers.COLORS.getRainbow().getRGB(), true);
                Managers.TEXT.drawString(yaw, 2.0F, (height - i - 22 + p), Managers.COLORS.getRainbow().getRGB(), true);
                Managers.TEXT.drawString(rainbowCoords, 2.0F, (height - i), Managers.COLORS.getRainbow().getRGB(), true);
            } else {
                if (mc.currentScreen instanceof net.minecraft.client.gui.GuiChat && this.direction.getValue()) {
                    drawDoubleRainbowRollingString((lowerCase.getValue() ? "Yaw: ".toLowerCase() : "Yaw: "), "" + ChatFormatting.WHITE + yawPitch, 2.0f, (height - i - 11 + p), true);
                    String uh = "Yaw: " + ChatFormatting.WHITE + yawPitch;
                    Managers.TEXT.drawRollingRainbowString(" " + getFacingDirectionShort(), 2.0f + Managers.TEXT.getStringWidth(uh), (height - i - 11 + p), true);
                    } else {
                    Managers.TEXT.drawRollingRainbowString(this.direction.getValue() ? direction : "", 2.0f, (height - i - 11 + p), true);
                    drawDoubleRainbowRollingString(this.direction.getValue() ? (lowerCase.getValue() ? "Yaw: ".toLowerCase() : "Yaw: ") : "",this.direction.getValue() ?  ("" + ChatFormatting.WHITE + yawPitch) : "", 2.0f, (height - i - 22 + p), true);
                }
                drawDoubleRainbowRollingString(this.coords.getValue() ? (lowerCase.getValue() ? "XYZ: ".toLowerCase() : "XYZ: ") : "", this.coords.getValue() ? ("" + ChatFormatting.WHITE + (inHell ? (posX + ", " + posY + ", " + posZ + ChatFormatting.GRAY + " [" + ChatFormatting.WHITE + hposX + ", " + hposZ + ChatFormatting.GRAY + "]" + ChatFormatting.WHITE) : (posX + ", " + posY + ", " + posZ + ChatFormatting.GRAY + " [" + ChatFormatting.WHITE + hposX + ", " + hposZ + ChatFormatting.GRAY + "]"))) : "", 2.0F, (height - i), true);
            }
        } else {
            Managers.TEXT.drawString(direction, 2.0F, (height - i - 11 + p), this.color, true);
            Managers.TEXT.drawString(yaw, 2.0F, (height - i - 22 + p), this.color, true);
            Managers.TEXT.drawString(coords, 2.0F, (height - i), this.color, true);
        }

        if (armor.getValue()) drawArmorHUD();

        if (greeter.getValue()) drawWelcomer();

        if (lag.getValue()) drawLagOMeter();
    }

    private void drawWelcomer() {
        int width = Managers.TEXT.scaledWidth;
        String nameColor = greeterNameColor.getValue() ? "" + ChatFormatting.WHITE : "";
        String text = (lowerCase.getValue() ? "Welcome, ".toLowerCase() : "Welcome, ");

        if (greeterMode.getValue() == GreeterMode.PLAYER) {
            if (greeter.getValue())
            text = text + nameColor + mc.player.getDisplayNameString();

            if ((ClickGui.INSTANCE).rainbow.getValue()) {

                if ((ClickGui.INSTANCE).hudRainbow.getValue() == ClickGui.HudRainbow.STATIC) {
                    Managers.TEXT.drawString(text + ChatFormatting.RESET + " :')", width / 2.0F - Managers.TEXT.getStringWidth(text) / 2.0F + 2.0F, 2.0F, Managers.COLORS.getRainbow().getRGB(), true);
                } else {

                    if (greeterNameColor.getValue()) {
                        drawDoubleRainbowRollingString((lowerCase.getValue() ? "Welcome,".toLowerCase() : "Welcome,"), ((FontMod.INSTANCE.isOn() ? "" : " ")) + ChatFormatting.WHITE + mc.player.getDisplayNameString(), width / 2.0F - Managers.TEXT.getStringWidth(text) / 2.0F + 2.0F, 2.0F, true);
                        Managers.TEXT.drawRollingRainbowString(" :')", width / 2.0F - Managers.TEXT.getStringWidth(text) / 2.0F + 1.5f + Managers.TEXT.getStringWidth(text) - (FontMod.INSTANCE.isOn() ? 1.5f : 0.0f), 2.0f, true);
                    } else {
                        Managers.TEXT.drawRollingRainbowString((lowerCase.getValue() ? "Welcome,".toLowerCase() : "Welcome, ") + mc.player.getDisplayNameString() + " :')", width / 2.0F - Managers.TEXT.getStringWidth(text) / 2.0F + 2.0F, 2.0F, true);
                    }
                }
            } else {
                Managers.TEXT.drawString(text + ChatFormatting.RESET + " :')", width / 2.0F - Managers.TEXT.getStringWidth(text) / 2.0F + 2.0F, 2.0F, color, true);
            }
        } else {
            String lel = greeterText.getValue();
            if (greeter.getValue())
                lel = greeterText.getValue();
            if ((ClickGui.INSTANCE).rainbow.getValue()) {
                if ((ClickGui.INSTANCE).hudRainbow.getValue() == ClickGui.HudRainbow.STATIC) {
                    Managers.TEXT.drawString(lel, width / 2.0F - Managers.TEXT.getStringWidth(lel) / 2.0F + 2.0F, 2.0F, Managers.COLORS.getRainbow().getRGB(), true);
                } else {
                    Managers.TEXT.drawRollingRainbowString(lel, width / 2.0F - Managers.TEXT.getStringWidth(lel) / 2.0F + 2.0F, 2.0F, true);
                }
            } else {
                Managers.TEXT.drawString(lel, width / 2.0F - Managers.TEXT.getStringWidth(lel) / 2.0F + 2.0F, 2.0F, color, true);
            }
        }
    }

    private void drawLagOMeter() {
        int width = Managers.TEXT.scaledWidth;
        if (Managers.SERVER.isServerNotResponding()) {
            String text = ChatFormatting.RED + (lowerCase.getValue() ? "Server is lagging for ".toLowerCase() : "Server is lagging for ") + MathUtil.round(( float ) Managers.SERVER.serverRespondingTime() / 1000.0F, 1) + "s.";
            Managers.TEXT.drawString(text, width / 2.0F - Managers.TEXT.getStringWidth(text) / 2.0F + 2.0F, 20.0F, color, true);
        }
    }

    private void drawArmorHUD() {
        int width = Managers.TEXT.scaledWidth;
        int height = Managers.TEXT.scaledHeight;
        GlStateManager.enableTexture2D();
        int i = width / 2;
        int iteration = 0;
        int y = height - 55 - (mc.player.isInWater() && mc.playerController.gameIsSurvivalOrAdventure() ? 10 : 0);
        for (ItemStack is : mc.player.inventory.armorInventory) {
            iteration++;
            if (is.isEmpty()) continue;
            int x = i - 90 + (9 - iteration) * 20 + 2;
            GlStateManager.enableDepth();
            RenderUtil.itemRender.zLevel = 200F;
            RenderUtil.itemRender.renderItemAndEffectIntoGUI(is, x, y);
            RenderUtil.itemRender.renderItemOverlayIntoGUI(mc.fontRenderer, is, x, y, "");
            RenderUtil.itemRender.zLevel = 0F;
            GlStateManager.enableTexture2D();
            GlStateManager.disableLighting();
            GlStateManager.disableDepth();
            String s = is.getCount() > 1 ? is.getCount() + "" : "";
            Managers.TEXT.drawStringWithShadow(s, x + 19 - 2 - Managers.TEXT.getStringWidth(s), y + 9, 0xffffff);

            if (true) {
                int dmg = 0;
                int itemDurability = is.getMaxDamage() - is.getItemDamage();
                float green = (( float ) is.getMaxDamage() - ( float ) is.getItemDamage()) / ( float ) is.getMaxDamage();
                float red = 1 - green;
                if (true) {
                    dmg = 100 - ( int ) (red * 100);
                } else {
                    dmg = itemDurability;
                }
                Managers.TEXT.drawStringWithShadow(dmg + "", x + 8 - Managers.TEXT.getStringWidth(dmg + "") / 2, y - 11, ColorUtil.toRGBA(( int ) (red * 255), ( int ) (green * 255), 0));
            }
        }
        GlStateManager.enableDepth();
        GlStateManager.disableLighting();
    }

    private void drawPvPInfo() {
        float yOffset = Managers.TEXT.scaledHeight / 2.0f;

        int totemCount = mc.player.inventory.mainInventory.stream().filter((itemStack) -> itemStack.getItem() == Items.TOTEM_OF_UNDYING).mapToInt(ItemStack::getCount).sum();

        if (mc.player.getHeldItemOffhand().getItem() == Items.TOTEM_OF_UNDYING) {
            totemCount += mc.player.getHeldItemOffhand().getCount();
        }

        int pingCount = Managers.SERVER.getPing();

        EntityPlayer target = EntityUtil.getClosestEnemy(7);

        //Strings

        String totemString = "" + (totemCount != 0 ? ChatFormatting.GREEN : ChatFormatting.RED) + totemCount;
        String pingColor;
        String safetyColor;

        String htrColor = String.valueOf(
                (target != null && mc.player.getDistance(target) <= Aura.INSTANCE.range.getValue())
                        ? ChatFormatting.GREEN
                        : ChatFormatting.DARK_RED);

        String plrColor = String.valueOf(
                (target != null && mc.player.getDistance(target) <= 5 && AutoTrap.INSTANCE.isOn())
                        ? ChatFormatting.GREEN
                        : ChatFormatting.DARK_RED);

        String htr = "HTR";
        String plr = "PLR";

        //Stuff

        if (pingCount < 40) {
            pingColor = String.valueOf(ChatFormatting.GREEN);

        } else if (pingCount < 65) {
            pingColor = String.valueOf(ChatFormatting.DARK_GREEN);

        } else if (pingCount < 80) {
            pingColor = String.valueOf(ChatFormatting.YELLOW);

        } else if (pingCount < 110) {
            pingColor = String.valueOf(ChatFormatting.RED);

        } else if (pingCount < 160) {
            pingColor = String.valueOf(ChatFormatting.DARK_RED);

        } else {
            pingColor = String.valueOf(ChatFormatting.DARK_RED);
        }

        if (!EntityUtil.isSafe(mc.player, 0, true)) {
            safetyColor = String.valueOf(ChatFormatting.DARK_RED);

        } else {
            safetyColor = String.valueOf(ChatFormatting.GREEN);
        }

        //HTR

        Managers.TEXT.drawString(
                htrColor + htr,
                2.0f,
                yOffset - 20.0f,
                color,
                true);

        //PLR

        Managers.TEXT.drawString(
                plrColor + plr,
                2.0f,
                yOffset - 10.0f,
                color,
                true);

        //Ping

        Managers.TEXT.drawString(
                pingColor + pingCount + " MS",
                2.0F,
                yOffset,
                color,
                true);

        //Totems

        Managers.TEXT.drawString(
                totemString,
                2.0F,
                yOffset + 10.0f,
                color,
                true);

        //Safety

        Managers.TEXT.drawString(
                safetyColor + "LBY",
                2.0F,
                yOffset + 20.0f,
                color,
                true);
    }

    private void drawDoubleRainbowRollingString(String first, String second, float x, float y, boolean shadow) {
        Managers.TEXT.drawRollingRainbowString(first, x, y, shadow);
        Managers.TEXT.drawString(second, x + Managers.TEXT.getStringWidth(first), y, -1, shadow);
    }

    private void drawTextRadar(int yOffset) {

        if (!players.isEmpty()) {

            int y = Managers.TEXT.getFontHeight() + 7 + yOffset;

            for (Map.Entry<String, Integer> player : players.entrySet()) {

                String text = player.getKey() + " ";

                int textHeight = Managers.TEXT.getFontHeight() + 1;

                if ((ClickGui.INSTANCE).rainbow.getValue()) {

                    if ((ClickGui.INSTANCE).hudRainbow.getValue() == ClickGui.HudRainbow.STATIC) {
                        Managers.TEXT.drawString(text, 2.0F, ( float ) y, ColorUtil.rainbow((ClickGui.INSTANCE).rainbowDelay.getValue()).getRGB(), true);
                        y += textHeight;

                    } else {
                        Managers.TEXT.drawString(text, 2.0F, ( float ) y, ColorUtil.rainbow((ClickGui.INSTANCE).rainbowDelay.getValue()).getRGB(), true);
                        y += textHeight;
                    }

                } else {
                    Managers.TEXT.drawString(text, 2.0F, ( float ) y, color, true);
                    y += textHeight;
                }
            }
        }
    }

    private Map<String, Integer> getTextRadarMap() {
        Map<String, Integer> retval = new HashMap<>();

        DecimalFormat dfDistance = new DecimalFormat("#.#");
        dfDistance.setRoundingMode(RoundingMode.CEILING);
        StringBuilder distanceSB = new StringBuilder();

        for (EntityPlayer player : mc.world.playerEntities) {
            if (player.isInvisible() || player.getName().equals(mc.player.getName())) continue;

            int distanceInt = (int) mc.player.getDistance(player);
            String distance = dfDistance.format(distanceInt);

            if (distanceInt >= 25) {
                distanceSB.append(ChatFormatting.GREEN);

            } else if (distanceInt > 10) {
                distanceSB.append(ChatFormatting.YELLOW);

            } else {
                distanceSB.append(ChatFormatting.RED);
            }
            distanceSB.append(distance);

            retval.put(
                    (Managers.FRIENDS.isCool(player.getName()) ? ChatFormatting.GOLD + "< > " + ChatFormatting.RESET : "")
                            + (Managers.FRIENDS.isFriend(player) ? ChatFormatting.AQUA : ChatFormatting.RESET)
                            + player.getName()
                            + " "
                            + ChatFormatting.WHITE
                            + "["
                            + ChatFormatting.RESET
                            + distanceSB
                            + "m"
                            + ChatFormatting.WHITE
                            + "] "
                            + ChatFormatting.GREEN,
                    (int) mc.player.getDistance(player));

            distanceSB.setLength(0);
        }

        if (!retval.isEmpty()) {
            retval = MathUtil.sortByValue(retval, false);
        }

        return retval;
    }

    private int getJamieColor(int n) {
        int n2 = Managers.MODULES.getEnabledModules().size();
        int n3 = new Color(91, 206, 250).getRGB();
        int n4 = Color.WHITE.getRGB();
        int n5 = new Color(245, 169, 184).getRGB();
        int n6 = n2 / 5;
        if (n < n6) {
            return n3;
        }
        if (n < n6 * 2) {
            return n5;
        }
        if (n < n6 * 3) {
            return n4;
        }
        if (n < n6 * 4) {
            return n5;
        }
        if (n < n6 * 5) {
            return n3;
        }
        return n3;
    }

    private String getFacingDirectionShort() {
        int dirnumber = Managers.ROTATIONS.getYaw4D();

        if (dirnumber == 0) {
            return "(+Z)";
        }
        if (dirnumber == 1) {
            return "(-X)";
        }
        if (dirnumber == 2) {
            return "(-Z)";
        }
        if (dirnumber == 3) {
            return "(+X)";
        }
        return "Loading...";
    }

    private String getColoredPotionString(PotionEffect effect) {
        Potion potion = effect.getPotion();
        return I18n.format(potion.getName()) + " " + (effect.getAmplifier() + 1) + " " + ChatFormatting.WHITE + Potion.getPotionDurationString(effect, 1.0f);
    }
}
