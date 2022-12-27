package me.mioclient.mod.modules.impl.player;

import me.mioclient.api.util.math.Timer;
import me.mioclient.api.util.world.InventoryUtil;
import me.mioclient.mod.modules.Category;
import me.mioclient.mod.modules.Module;
import me.mioclient.mod.modules.settings.Setting;
import net.minecraft.item.ItemExpBottle;

public class FastExp extends Module {

    private final Setting<Integer> delay =
            add(new Setting<>("Delay", 1, 0, 5));

    private final Timer delayTimer = new Timer();

    public FastExp() {
        super("FastExp", "Fast projectile.", Category.PLAYER);
    }

    @Override
    public void onUpdate() {
        if (fullNullCheck()) {
            return;
        }
        if (InventoryUtil.holdingItem(ItemExpBottle.class) && delayTimer.passedMs(delay.getValue() * 20)) {
            mc.rightClickDelayTimer = 1;
            delayTimer.reset();
        }
    }
}

