package me.mioclient.mod.modules.impl.player;

import me.mioclient.api.managers.Managers;
import me.mioclient.api.util.world.InventoryUtil;
import me.mioclient.mod.modules.Category;
import me.mioclient.mod.modules.Module;
import me.mioclient.mod.modules.settings.Setting;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.ItemEnderPearl;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.RayTraceResult;
import org.lwjgl.input.Mouse;

public class KeyPearl extends Module {

    private final Setting<Mode> mode =
            add(new Setting<>("Mode", Mode.MIDDLECLICK));
    private final Setting<Boolean> antiFriend =
            add(new Setting<>("NoPlayerTrace", true));

    private boolean clicked;

    public KeyPearl() {
        super("KeyPearl", "Throws a pearl.", Category.PLAYER);
    }

    private enum Mode {
        KEY,
        MIDDLECLICK
    }

    @Override
    public String getInfo() {
        return Managers.TEXT.normalizeCases(mode.getValue());
    }

    @Override
    public void onEnable() {
        if (!fullNullCheck() && mode.getValue() == Mode.KEY) {
            throwPearl();
            disable();
        }
    }

    @Override
    public void onTick() {
        if (mode.getValue() == Mode.MIDDLECLICK) {
            if (Mouse.isButtonDown(2)) {
                if (!clicked) {
                    throwPearl();
                }
                clicked = true;
            } else {
                clicked = false;
            }
        }
    }

    private void throwPearl() {
        boolean offhand;
        Entity entity;
        RayTraceResult result;
        if (antiFriend.getValue() && (result = KeyPearl.mc.objectMouseOver) != null && result.typeOfHit == RayTraceResult.Type.ENTITY && (entity = result.entityHit) instanceof EntityPlayer) {
            return;
        }
        int pearlSlot = InventoryUtil.findHotbarBlock(ItemEnderPearl.class);
        boolean bl = offhand = KeyPearl.mc.player.getHeldItemOffhand().getItem() == Items.ENDER_PEARL;
        if (pearlSlot != -1 || offhand) {
            int oldslot = KeyPearl.mc.player.inventory.currentItem;
            if (!offhand) {
                InventoryUtil.switchToHotbarSlot(pearlSlot, false);
            }
            KeyPearl.mc.playerController.processRightClick(KeyPearl.mc.player, KeyPearl.mc.world, offhand ? EnumHand.OFF_HAND : EnumHand.MAIN_HAND);
            if (!offhand) {
                InventoryUtil.switchToHotbarSlot(oldslot, false);
            }
        }
    }
}