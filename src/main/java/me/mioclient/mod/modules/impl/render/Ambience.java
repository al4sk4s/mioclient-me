package me.mioclient.mod.modules.impl.render;

import me.mioclient.api.events.impl.RenderFogColorEvent;
import me.mioclient.api.events.impl.RenderSkyEvent;
import me.mioclient.api.managers.Managers;
import me.mioclient.mod.modules.Category;
import me.mioclient.mod.modules.Module;
import me.mioclient.mod.modules.settings.Setting;
import net.minecraftforge.client.event.EntityViewRenderEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.awt.*;

/**
 * @author t.me/asphyxia1337
 */

public class Ambience extends Module {

    public static Ambience INSTANCE;

    public final Setting<Boolean> noFog =
            add(new Setting<>("NoFog", false));

    public final Setting<Boolean> nightMode =
            add(new Setting<>("NightMode", false));

    public final Setting<Color> lightMap =
            add(new Setting<>("LightMap", new Color(0xDEC6D0FF, true)).injectBoolean(false).hideAlpha());

    public final Setting<Color> sky =
            add(new Setting<>("OverWorldSky", new Color(0x7D7DD5)).injectBoolean(true).hideAlpha());
    public final Setting<Boolean> skyRainbow =
            add(new Setting<>("SkyRainbow", false, v -> sky.isOpen()));

    public final Setting<Color> skyNether =
            add(new Setting<>("NetherSky", new Color(0x7D7DD5)).injectBoolean(true).hideAlpha());
    public final Setting<Boolean> netherRainbow =
            add(new Setting<>("NetherSkyRainbow", false, v -> skyNether.isOpen()));

    public final Setting<Color> fog =
            add(new Setting<>("OverWorldFog", new Color(0xCC7DD5)).injectBoolean(false).hideAlpha());
    public final Setting<Boolean> fogRainbow =
            add(new Setting<>("FogRainbow", false, v -> fog.isOpen()));

    public final Setting<Color> fogNether =
            add(new Setting<>("NetherFog", new Color(0xCC7DD5)).injectBoolean(false).hideAlpha());
    public final Setting<Boolean> fogNetherRainbow =
            add(new Setting<>("NetherFogRainbow", false, v -> sky.isOpen()));

    public Ambience() {
        super("Ambience", "Custom ambience.", Category.RENDER, true);
        INSTANCE = this;
    }

    @Override
    public void onTick() {
        if (nightMode.getValue()) {
            mc.world.setWorldTime(22000);
        }
    }

    @SubscribeEvent
    public void setFogColor(RenderFogColorEvent event) {
        if (fog.booleanValue && mc.player.dimension == 0) {
            event.setColor(fogRainbow.getValue() ? Managers.COLORS.getRainbow() : fog.getValue());
            event.cancel();
        } else if (fogNether.booleanValue && mc.player.dimension == -1) {
            event.setColor(fogNetherRainbow.getValue() ? Managers.COLORS.getRainbow() : fogNether.getValue());
            event.cancel();
        }
    }

    @SubscribeEvent
    public void setSkyColor(RenderSkyEvent event) {
        if (sky.booleanValue && mc.player.dimension == 0) {
            event.setColor(skyRainbow.getValue() ? Managers.COLORS.getRainbow() : sky.getValue());
            event.cancel();
        } else if (skyNether.booleanValue && mc.player.dimension == -1) {
            event.setColor(netherRainbow.getValue() ? Managers.COLORS.getRainbow() : skyNether.getValue());
            event.cancel();
        }
    }

    @SubscribeEvent
    public void setFogDensity(EntityViewRenderEvent.FogDensity event) {
        if (noFog.getValue()) {
            event.setDensity(0.0f);
            event.setCanceled(true);
        }
    }
}