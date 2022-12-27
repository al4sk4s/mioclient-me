package me.mioclient.mod.modules;

import com.mojang.realmsclient.gui.ChatFormatting;
import me.mioclient.api.events.impl.ClientEvent;
import me.mioclient.api.events.impl.Render2DEvent;
import me.mioclient.api.events.impl.Render3DEvent;
import me.mioclient.mod.Mod;
import me.mioclient.mod.commands.Command;
import me.mioclient.mod.modules.settings.Bind;
import me.mioclient.mod.modules.settings.Setting;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.common.MinecraftForge;
import org.lwjgl.input.Keyboard;

public abstract class Module extends Mod {

    public Setting<Boolean> enabled =
            add(new Setting<>("Enabled", getName().equalsIgnoreCase("HUD")));
    public Setting<Boolean> drawn =
            add(new Setting<>("Drawn", true));
    public Setting<Bind> bind =
            add(new Setting<>("Keybind", getName().equalsIgnoreCase("ClickGui") ? new Bind(Keyboard.KEY_Y) : new Bind(-1)));

    private final String description;
    private final Category category;

    private final boolean shouldListen;

    //General stuff

    public Module(String name, String description, Category category, boolean listen) {
        super(name);

        this.description = description;
        this.category = category;
        shouldListen = listen;
    }

    public Module(String name, String description, Category category) {
        super(name);

        this.description = description;
        this.category = category;
        shouldListen = false;
    }

    public void enable() {
        enabled.setValue(true);
        onEnable();

        Command.sendMessageWithID(ChatFormatting.DARK_AQUA
                + getName()
                + "\u00a7r"
                + ".enabled ="
                + "\u00a7r"
                + ChatFormatting.GREEN
                + " true.", hashCode());

        if (isOn() && shouldListen) {
            MinecraftForge.EVENT_BUS.register(this);
        }
    }

    public void disable() {
        enabled.setValue(false);
        onDisable();

        Command.sendMessageWithID(ChatFormatting.DARK_AQUA
                + getName()
                + "\u00a7r"
                + ".enabled ="
                + "\u00a7r"
                + ChatFormatting.RED
                + " false.", hashCode());

        if (shouldListen) {
            MinecraftForge.EVENT_BUS.unregister(this);
        }
    }

    public void toggle() {
        ClientEvent event = new ClientEvent(!isOn() ? 1 : 0, this);
        MinecraftForge.EVENT_BUS.post(event);

        if (!event.isCanceled()) {

            if (!isOn()) {
                enable();

            } else {
                disable();
            }
        }
    }

    public boolean isOn() {
        return enabled.getValue();
    }

    public boolean isOff() {
        return !enabled.getValue();
    }

    public boolean isDrawn() {
        return drawn.getValue();
    }

    public boolean isListening() {
        return shouldListen && isOn();
    }

    //Override-able methods

    public void onEnable() {

    }

    public void onDisable() {

    }

    public void onLoad() {

    }

    public void onTick() {

    }

    public void onLogin() {

    }

    public void onLogout() {

    }

    public void onUpdate() {

    }

    public void onUnload() {

    }

    public void onRender2D(Render2DEvent event) {

    }

    public void onRender3D(Render3DEvent event) {

    }

    public void onTotemPop(EntityPlayer player) {

    }

    public void onDeath(EntityPlayer player) {

    }

    public String getInfo() {
        return null;
    }

    //Getters

    public String getArrayListInfo() {
        return getName()
                + ChatFormatting.GRAY
                + (getInfo() != null
                ? " ["
                + ChatFormatting.WHITE
                + getInfo()
                + ChatFormatting.GRAY
                + "]"
                : "");
    }

    public Category getCategory() {
        return category;
    }

    public Bind getBind() {
        return bind.getValue();
    }

    public String getDescription() {
        return description;
    }

    //Setters

    public void setBind(int key) {
        bind.setValue(new Bind(key));
    }
}

