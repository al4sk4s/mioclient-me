package me.mioclient.api.managers;

import me.mioclient.api.managers.impl.*;

/**
 * Filler class just not to make a mess in the main one
 * @author t.me/asphyxia1337
 */

public class Managers {

    private static boolean loaded = true;

    public static InteractionManager INTERACTIONS;
    public static RotationManager ROTATIONS;
    public static CommandManager COMMANDS;
    public static ModuleManager MODULES;
    public static ConfigManager CONFIGS;
    public static FriendManager FRIENDS;
    public static ColorManager COLORS;
    public static EventManager EVENTS;
    public static FileManager FILES;
    public static PositionManager POSITION;
    public static ReloadManager RELOAD;
    public static ServerManager SERVER;
    public static TimerManager TIMER;
    public static SpeedManager SPEED;
    public static TextManager TEXT;
    public static FpsManager FPS;

    //Initializing stuff

    public static void load() {

        loaded = true;

        if (RELOAD != null) {
            RELOAD.unload();
            RELOAD = null;
        }

        //We're loading event manager and text manager first to prevent crashes
        EVENTS = new EventManager();
        TEXT = new TextManager();

        INTERACTIONS = new InteractionManager();
        ROTATIONS = new RotationManager();
        POSITION = new PositionManager();
        COMMANDS = new CommandManager();
        CONFIGS = new ConfigManager();
        MODULES = new ModuleManager();
        FRIENDS = new FriendManager();
        SERVER = new ServerManager();
        COLORS = new ColorManager();
        SPEED = new SpeedManager();
        TIMER = new TimerManager();
        FILES = new FileManager();
        FPS = new FpsManager();

        MODULES.init();
        CONFIGS.init();
        EVENTS.init();
        TEXT.init();

        MODULES.onLoad();
    }

    public static void unload(boolean force) {

        if (force) {
            RELOAD = new ReloadManager();
            RELOAD.init(COMMANDS != null ? COMMANDS.getCommandPrefix() : ".");
        }

        onUnload();

        INTERACTIONS = null;
        ROTATIONS = null;
        POSITION = null;
        COMMANDS = null;
        CONFIGS = null;
        MODULES = null;
        FRIENDS = null;
        SERVER = null;
        COLORS = null;
        EVENTS = null;
        SPEED = null;
        TIMER = null;
        FILES = null;
        TEXT = null;
        FPS = null;
    }

    public static void onUnload() {

        if (isLoaded()) {
            EVENTS.onUnload();
            MODULES.onUnloadPre();
            CONFIGS.saveConfig(CONFIGS.config.replaceFirst("hvhlegend/", ""));
            MODULES.onUnloadPost();

            loaded = false;
        }
    }

    //Getters

    public static boolean isLoaded() {
        return loaded;
    }
}
