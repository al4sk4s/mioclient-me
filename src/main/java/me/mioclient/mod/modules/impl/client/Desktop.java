package me.mioclient.mod.modules.impl.client;

import me.mioclient.Mio;
import me.mioclient.api.managers.Managers;
import me.mioclient.mod.modules.Category;
import me.mioclient.mod.modules.Module;
import me.mioclient.mod.modules.settings.Setting;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.lwjgl.opengl.Display;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author t.me/asphyxia1337
 */

public class Desktop extends Module {

    private final Setting<Boolean> onlyTabbed =
            add(new Setting<>("OnlyTabbed", false));
    private final Setting<Boolean> visualRange =
            add(new Setting<>("VisualRange", true));
    private final Setting<Boolean> selfPop =
            add(new Setting<>("TotemPop", true));
    private final Setting<Boolean> mention =
            add(new Setting<>("Mention", true));
    private final Setting<Boolean> dm =
            add(new Setting<>("DM", true));

    private List<Entity> players;
    private final List<Entity> knownPlayers = new ArrayList<>();

    Image image = Toolkit.getDefaultToolkit().createImage("icon.png");
    TrayIcon icon = new TrayIcon(image, "Mio");

    public Desktop() {
        super("Desktop", "Desktop notifications.", Category.CLIENT, true);
    }

    @Override
    public void onDisable() {
        knownPlayers.clear();

        removeIcon();
    }

    @Override
    public void onEnable() {
        addIcon();
    }

    @Override
    public void onLoad() {
        if (isOn()) addIcon();
    }

    @Override
    public void onUnload() {
        onDisable();
    }

    @Override
    public void onUpdate() {
        if (fullNullCheck() || !visualRange.getValue()) return;

        try {
            if (!Display.isActive() && onlyTabbed.getValue()) {
                return;
            }

        } catch (Exception ignored) {

        }
        
        players = mc.world.loadedEntityList.stream().filter(entity -> entity instanceof EntityPlayer).collect(Collectors.toList());
        
        try {
            for (Entity entity : players) {

                if (entity instanceof EntityPlayer
                        && !entity.getName().equalsIgnoreCase(mc.player.getName())
                        && !knownPlayers.contains(entity)
                        && !Managers.FRIENDS.isFriend(entity.getName())) {

                    knownPlayers.add(entity);

                    icon.displayMessage("Mio", entity.getName() + " has entered your visual range!", TrayIcon.MessageType.INFO);
                }
            }
        } catch (Exception ignored) {

        }

        try {
            knownPlayers.removeIf(entity -> entity instanceof EntityPlayer
                            && !entity.getName().equalsIgnoreCase(mc.player.getName())
                            && !players.contains(entity));

        } catch (Exception ignored) {

        }
    }

    @Override
    public void onTotemPop(EntityPlayer player) {
        if (fullNullCheck() || player != mc.player || !selfPop.getValue()) return;

        icon.displayMessage("Mio", "You are popping!", TrayIcon.MessageType.WARNING);
    }

    @SubscribeEvent
    public void onClientChatReceived(ClientChatReceivedEvent event) {
        if (fullNullCheck()) return;

        String message = String.valueOf(event.getMessage());

        if (message.contains(mc.player.getName()) && mention.getValue()) {
            icon.displayMessage("Mio", "New chat mention!", TrayIcon.MessageType.INFO);
        }
        if (message.contains("whispers:") && dm.getValue()) {
            icon.displayMessage("Mio", "New direct message!", TrayIcon.MessageType.INFO);
        }
    }

    private void addIcon() {
        SystemTray tray = SystemTray.getSystemTray();

        icon.setImageAutoSize(true);
        icon.setToolTip("mioclient.me " + Mio.MODVER);

        try {
            tray.add(icon);

        } catch (AWTException e) {
            e.printStackTrace();
        }
    }

    private void removeIcon() {
        SystemTray tray = SystemTray.getSystemTray();
        tray.remove(icon);
    }
}

