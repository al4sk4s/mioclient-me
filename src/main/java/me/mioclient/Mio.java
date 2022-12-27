package me.mioclient;

import me.mioclient.api.managers.Managers;
import me.mioclient.api.util.git.GitUtil;
import me.mioclient.api.util.render.RenderUtil;
import me.mioclient.mod.gui.screen.MioClickGui;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Util;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.opengl.Display;

import java.io.InputStream;
import java.nio.ByteBuffer;

@Mod(modid = Mio.MODID, name = "Mio", version = Mio.MODVER)
public class Mio {

    @Mod.Instance
    public static Mio INSTANCE;

    public static final String MODID = "mioclient.me";
    public static final String MODVER = "v0.6.9-pub";
    public static final String VERHASH = GitUtil.GIT_SHA.substring(0, 12);
    public static final Logger LOGGER = LogManager.getLogger("Mio");

    public static void load() {
        LOGGER.info("Loading Mio...");

        Managers.load();

        if (MioClickGui.INSTANCE == null) {
            MioClickGui.INSTANCE = new MioClickGui();
        }

        LOGGER.info("Mio successfully loaded!\n");
    }

    public static void unload(boolean force) {
        LOGGER.info("Unloading Mio...");

        Managers.unload(force);

        LOGGER.info("Mio successfully unloaded!\n");
    }

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        Display.setTitle(MODID + ": Loading...");
    }

    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
        Display.setTitle(MODID);

        if (Util.getOSType() != Util.EnumOS.OSX) {

            try (InputStream inputStream16x = Minecraft.class.getResourceAsStream("/assets/minecraft/textures/mio/constant/icon16x.png"); InputStream inputStream32x = Minecraft.class.getResourceAsStream("/assets/minecraft/textures/mio/constant/icon32x.png")) {

                ByteBuffer[] icons = new ByteBuffer[] {
                        RenderUtil.readImageToBuffer(inputStream16x), RenderUtil.readImageToBuffer(inputStream32x)
                };

                Display.setIcon(icons);

            } catch (Exception e) {
                LOGGER.error("Mio couldn't set the window icon!", e);
            }
        }
        load();
    }
}

