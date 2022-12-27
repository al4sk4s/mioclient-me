package me.mioclient.mod.modules.impl.combat;

import me.mioclient.api.managers.Managers;
import me.mioclient.api.util.math.Timer;
import me.mioclient.api.util.world.InventoryUtil;
import me.mioclient.mod.modules.Category;
import me.mioclient.mod.modules.Module;
import me.mioclient.mod.modules.settings.Setting;
import net.minecraft.item.ItemExpBottle;
import net.minecraft.network.play.client.CPacketHeldItemChange;
import net.minecraft.network.play.client.CPacketPlayerTryUseItem;
import net.minecraft.util.EnumHand;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

/**
 * @author t.me/asphyxia1337
 */

public class PacketExp extends Module {

    public static PacketExp INSTANCE;

    protected final Setting<Mode> mode =
            add(new Setting<>("Mode", Mode.KEY));
    protected final Setting<Integer> delay =
            add(new Setting<>("Delay", 1, 0, 5));

    private final Timer delayTimer = new Timer();

    public PacketExp() {
        super("PacketExp", "Robot module", Category.COMBAT);
        INSTANCE = this;
    }

    protected enum Mode {
        KEY,
        MIDDLECLICK
    }

    @Override
    public String getInfo() {
        return Managers.TEXT.normalizeCases(mode.getValue());
    }

    @Override
    public void onTick() {
        if (!fullNullCheck() && mode.getValue() == Mode.MIDDLECLICK && Mouse.isButtonDown(2)) {
            throwExp();

        } else if (check() && mode.getValue() == Mode.KEY && Keyboard.isKeyDown(bind.getValue().getKey())) {
            enable();
            throwExp();
        }

        if (check() && mode.getValue() == Mode.KEY && !Keyboard.isKeyDown(bind.getValue().getKey())) {
            disable();
        }
    }

    private void throwExp() {
        int oldSlot = mc.player.inventory.currentItem;
        int newSlot = InventoryUtil.findHotbarBlock(ItemExpBottle.class);

        if (newSlot != -1 && delayTimer.passedMs(delay.getValue() * 20)) {
            mc.player.connection.sendPacket(new CPacketHeldItemChange(newSlot));
            mc.player.connection.sendPacket(new CPacketPlayerTryUseItem(EnumHand.MAIN_HAND));
            mc.player.connection.sendPacket(new CPacketHeldItemChange(oldSlot));

            delayTimer.reset();
        }
    }

    private boolean check() {
        if (nullCheck() || fullNullCheck()) return false;

        return bind.getValue().getKey() != -1;
    }
}
