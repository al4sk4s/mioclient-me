package me.mioclient.mod.modules.impl.player;

import me.mioclient.api.util.math.Timer;
import me.mioclient.mod.modules.Category;
import me.mioclient.mod.modules.Module;
import me.mioclient.mod.modules.settings.Setting;
import net.minecraft.init.Items;
import net.minecraft.inventory.ClickType;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

import java.util.ArrayList;

public class Replenish extends Module {

    private final Setting<Integer> delay =
            add(new Setting<>("Delay", 2, 0, 10));
    private final Setting<Integer> gapThreshold =
            add(new Setting<>("GapStack", 50, 50, 64));
    private final Setting<Integer> expThreshold =
            add(new Setting<>("XPStack", 50, 50, 64));

    private final Timer timer = new Timer();
    private final ArrayList<Item> Hotbar = new ArrayList<>();

    public Replenish() {
        super("Replenish", "Replenishes your hotbar.", Category.PLAYER);
    }

    @Override
    public void onEnable() {
        if (fullNullCheck()) {
            return;
        }

        Hotbar.clear();

        for (int i = 0; i < 9; ++i) {

            ItemStack stack = mc.player.inventory.getStackInSlot(i);
            if (!stack.isEmpty() && !Hotbar.contains(stack.getItem())) {
                Hotbar.add(stack.getItem());
                continue;
            }
            Hotbar.add(Items.AIR);
        }
    }

    @Override
    public void onUpdate() {
        if (mc.currentScreen != null) {
            return;
        }
        if (!timer.passedMs(delay.getValue() * 1000)) {
            return;
        }
        for (int i = 0; i < 9; ++i) {
            if (!RefillSlotIfNeed(i)) continue;
            timer.reset();
            return;
        }
    }

    private boolean RefillSlotIfNeed(int slot) {
        ItemStack stack = mc.player.inventory.getStackInSlot(slot);
        if (stack.isEmpty() || stack.getItem() == Items.AIR) {
            return false;
        }
        if (!stack.isStackable()) {
            return false;
        }
        if (stack.getCount() >= stack.getMaxStackSize()) {
            return false;
        }
        if (stack.getItem().equals(Items.GOLDEN_APPLE) && stack.getCount() >= gapThreshold.getValue()) {
            return false;
        }
        if (stack.getItem().equals(Items.EXPERIENCE_BOTTLE) && stack.getCount() > expThreshold.getValue()) {
            return false;
        }

        for (int i = 9; i < 36; ++i) {
            ItemStack item = mc.player.inventory.getStackInSlot(i);

            if (item.isEmpty() || !CanItemBeMergedWith(stack, item)) continue;

            mc.playerController.windowClick(mc.player.inventoryContainer.windowId, i, 0, ClickType.QUICK_MOVE, mc.player);
            mc.playerController.updateController();
            return true;
        }
        return false;
    }

    private boolean CanItemBeMergedWith(ItemStack source, ItemStack stack) {
        return source.getItem() == stack.getItem() && source.getDisplayName().equals(stack.getDisplayName());
    }
}

