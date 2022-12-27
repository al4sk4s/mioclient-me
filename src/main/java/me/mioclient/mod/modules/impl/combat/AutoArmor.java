
package me.mioclient.mod.modules.impl.combat;

import me.mioclient.api.managers.Managers;
import me.mioclient.api.util.entity.EntityUtil;
import me.mioclient.api.util.math.MathUtil;
import me.mioclient.api.util.math.Timer;
import me.mioclient.api.util.world.InventoryUtil;
import me.mioclient.mod.gui.screen.MioClickGui;
import me.mioclient.mod.modules.Category;
import me.mioclient.mod.modules.Module;
import me.mioclient.mod.modules.impl.exploit.XCarry;
import me.mioclient.mod.modules.settings.Bind;
import me.mioclient.mod.modules.settings.Setting;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.gui.inventory.GuiInventory;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemExpBottle;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public class AutoArmor extends Module {

    private final Setting<Boolean> autoMend =
            add(new Setting<>("AutoMend", false).setParent());
    private final Setting<Integer> closestEnemy =
            add(new Setting<>("EnemyRange", 8, 1, 20, v -> autoMend.isOpen()));
    private final Setting<Integer> helmetThreshold =
            add(new Setting<>("Helmet%", 80, 1, 100, v -> autoMend.isOpen()));
    private final Setting<Integer> chestThreshold =
            add(new Setting<>("Chest%", 80, 1, 100, v -> autoMend.isOpen()));
    private final Setting<Integer> legThreshold =
            add(new Setting<>("Legs%", 80, 1, 100, v -> autoMend.isOpen()));
    private final Setting<Integer> bootsThreshold =
            add(new Setting<>("Boots%", 80, 1, 100, v -> autoMend.isOpen()));

    private final Setting<Boolean> save =
            add(new Setting<>("Save", false).setParent());
    private final Setting<Integer> saveThreshold =
            add(new Setting<>("Save%", 5, 1, 10, v -> save.isOpen()));

    private final Setting<Integer> delay =
            add(new Setting<>("Delay", 50, 0, 500));
    private final Setting<Integer> actions =
            add(new Setting<>("Actions", 3, 1, 12));
    private final Setting<Boolean> curse =
            add(new Setting<>("CurseOfBinding", false));
    private final Setting<Boolean> tps =
            add(new Setting<>("TpsSync", true));
    private final Setting<Boolean> updateController =
            add(new Setting<>("Update", true));
    private final Setting<Boolean> shiftClick =
            add(new Setting<>("ShiftClick", false));

    private final Setting<Bind> elytraBind =
            add(new Setting<>("Elytra", new Bind(-1)));
    private final Setting<Bind> noHelmBind =
            add(new Setting<>("NoHelmet", new Bind(-1)));

    private final Timer timer = new Timer();
    private final Timer elytraTimer = new Timer();

    private final Queue<InventoryUtil.QueuedTask> queuedTaskList = new ConcurrentLinkedQueue<>();
    private final List<Integer> doneSlots = new ArrayList<>();

    private boolean elytraOn, helmOff;

    public AutoArmor() {
        super("AutoArmor", "Puts Armor on for you.", Category.COMBAT, true);
    }

    @Override
    public void onDisable() {
        queuedTaskList.clear();
        doneSlots.clear();
        elytraOn = false;
        helmOff = false;
    }

    @Override
    public String getInfo() {
        if (elytraOn) {
            return "Elytra";
        }
        return null;
    }

    @Override
    public void onLogout() {
        queuedTaskList.clear();
        doneSlots.clear();
    }

    @Override
    public void onLogin() {
        timer.reset();
        elytraTimer.reset();
    }

    @Override
    public void onTick() {
        if (fullNullCheck() || mc.currentScreen instanceof GuiContainer && !(mc.currentScreen instanceof GuiInventory)) {
            return;
        }
        if (queuedTaskList.isEmpty()) {
            int slot;
            int slot2;
            int slot3;
            int slot4;

            ItemStack chest;
            
            PacketExp packetExp = PacketExp.INSTANCE;

            boolean throwingExp = ((InventoryUtil.holdingItem(ItemExpBottle.class) && mc.gameSettings.keyBindUseItem.isKeyDown()) ||
                    (packetExp.mode.getValue() == PacketExp.Mode.KEY && packetExp.isOn() && packetExp.bind.getValue().getKey() != -1) ||
                    (packetExp.isOn() && packetExp.mode.getValue() == PacketExp.Mode.MIDDLECLICK && Mouse.isButtonDown(2)));

            if (autoMend.getValue() && throwingExp && (isSafe() || EntityUtil.isSafe(mc.player, 1, false))) {
                ItemStack helm = mc.player.inventoryContainer.getSlot(5).getStack();

                if (!helm.isEmpty && EntityUtil.getDamagePercent(helm) >= helmetThreshold.getValue()) {
                    takeOffSlot(5);
                }

                ItemStack chest2 = mc.player.inventoryContainer.getSlot(6).getStack();

                if (!chest2.isEmpty && EntityUtil.getDamagePercent(chest2) >= chestThreshold.getValue()) {
                    takeOffSlot(6);
                }

                ItemStack legging2 = mc.player.inventoryContainer.getSlot(7).getStack();

                if (!legging2.isEmpty && EntityUtil.getDamagePercent(legging2) >= legThreshold.getValue()) {
                    takeOffSlot(7);
                }

                ItemStack feet2 = mc.player.inventoryContainer.getSlot(8).getStack();

                if (!feet2.isEmpty && EntityUtil.getDamagePercent(feet2) >= bootsThreshold.getValue()) {
                    takeOffSlot(8);
                }

                return;
            }

            if (save.getValue()) {
                ItemStack helm = mc.player.inventoryContainer.getSlot(5).getStack();

                if (save.getValue() && !helm.isEmpty && EntityUtil.getDamagePercent(helm) <= saveThreshold.getValue()) {
                    takeOffSlot(5);
                }

                ItemStack chest2 = mc.player.inventoryContainer.getSlot(6).getStack();

                if (save.getValue() && !chest2.isEmpty && EntityUtil.getDamagePercent(chest2) <= saveThreshold.getValue()) {
                    takeOffSlot(6);
                }

                ItemStack legging2 = mc.player.inventoryContainer.getSlot(7).getStack();

                if (save.getValue() && !legging2.isEmpty && EntityUtil.getDamagePercent(legging2) <= saveThreshold.getValue()) {
                    takeOffSlot(7);
                }

                ItemStack feet2 = mc.player.inventoryContainer.getSlot(8).getStack();

                if (save.getValue() && !feet2.isEmpty && EntityUtil.getDamagePercent(feet2) <= saveThreshold.getValue()) {
                    takeOffSlot(8);
                }
            }
            ItemStack helm = mc.player.inventoryContainer.getSlot(5).getStack();

            if (!helmOff && helm.getItem() == Items.AIR && (slot4 = InventoryUtil.findArmorSlot(EntityEquipmentSlot.HEAD, curse.getValue(), XCarry.getInstance().isOn())) != -1) {
                getSlotOn(5, slot4);

            } else if (helmOff) {

                if (helm.getItem() != Items.AIR) {
                    takeOffSlot(5);
                }
            }

            if ((chest = mc.player.inventoryContainer.getSlot(6).getStack()).getItem() == Items.AIR) {

                if (queuedTaskList.isEmpty()) {

                    if (elytraOn && elytraTimer.passedMs(500L)) {

                        int elytraSlot = InventoryUtil.findItemInventorySlot(Items.ELYTRA, false, XCarry.getInstance().isOn());

                        if (elytraSlot != -1) {
                            if (elytraSlot < 5 && elytraSlot > 1 || !shiftClick.getValue()) {
                                queuedTaskList.add(new InventoryUtil.QueuedTask(elytraSlot));
                                queuedTaskList.add(new InventoryUtil.QueuedTask(6));

                            } else {
                                queuedTaskList.add(new InventoryUtil.QueuedTask(elytraSlot, true));
                            }

                            if (updateController.getValue()) {
                                queuedTaskList.add(new InventoryUtil.QueuedTask());
                            }
                            elytraTimer.reset();
                        }

                    } else if (!elytraOn && (slot3 = InventoryUtil.findArmorSlot(EntityEquipmentSlot.CHEST, curse.getValue(), XCarry.getInstance().isOn())) != -1 ) {
                        getSlotOn(6, slot3);
                    }
                }

            } else if (elytraOn && chest.getItem() != Items.ELYTRA && elytraTimer.passedMs(500L)) {

                if (queuedTaskList.isEmpty()) {
                    slot3 = InventoryUtil.findItemInventorySlot(Items.ELYTRA, false, XCarry.getInstance().isOn());

                    if (slot3 != -1) {
                        queuedTaskList.add(new InventoryUtil.QueuedTask(slot3));
                        queuedTaskList.add(new InventoryUtil.QueuedTask(6));
                        queuedTaskList.add(new InventoryUtil.QueuedTask(slot3));

                        if (updateController.getValue()) {
                            queuedTaskList.add(new InventoryUtil.QueuedTask());
                        }
                    }
                    elytraTimer.reset();
                }

            } else if (!elytraOn && chest.getItem() == Items.ELYTRA && elytraTimer.passedMs(500L) && queuedTaskList.isEmpty()) {
                slot3 = InventoryUtil.findItemInventorySlot(Items.DIAMOND_CHESTPLATE, false, XCarry.getInstance().isOn());

                if (slot3 == -1 && (slot3 = InventoryUtil.findItemInventorySlot(Items.IRON_CHESTPLATE, false, XCarry.getInstance().isOn())) == -1 && (slot3 = InventoryUtil.findItemInventorySlot(Items.GOLDEN_CHESTPLATE, false, XCarry.getInstance().isOn())) == -1 && (slot3 = InventoryUtil.findItemInventorySlot(Items.CHAINMAIL_CHESTPLATE, false, XCarry.getInstance().isOn())) == -1) {
                    slot3 = InventoryUtil.findItemInventorySlot(Items.LEATHER_CHESTPLATE, false, XCarry.getInstance().isOn());
                }

                if (slot3 != -1) {
                    queuedTaskList.add(new InventoryUtil.QueuedTask(slot3));
                    queuedTaskList.add(new InventoryUtil.QueuedTask(6));
                    queuedTaskList.add(new InventoryUtil.QueuedTask(slot3));
                    if (updateController.getValue()) {
                        queuedTaskList.add(new InventoryUtil.QueuedTask());
                    }
                }
                elytraTimer.reset();
            }
            mc.player.inventoryContainer.getSlot(7).getStack();

            if (mc.player.inventoryContainer.getSlot(7).getStack().getItem() == Items.AIR && (slot2 = InventoryUtil.findArmorSlot(EntityEquipmentSlot.LEGS, curse.getValue(), XCarry.getInstance().isOn())) != -1) {
                getSlotOn(7, slot2);
            }
            mc.player.inventoryContainer.getSlot(8).getStack();

            if (mc.player.inventoryContainer.getSlot(8).getStack().getItem() == Items.AIR && (slot = InventoryUtil.findArmorSlot(EntityEquipmentSlot.FEET, curse.getValue(), XCarry.getInstance().isOn())) != -1) {
                getSlotOn(8, slot);
            }
        }
        if (timer.passedMs((int)((float) delay.getValue() * (tps.getValue() ? Managers.SERVER.getTpsFactor() : 1.0f)))) {
            if (!queuedTaskList.isEmpty()) {
                for (int i = 0; i < actions.getValue(); ++i) {
                    InventoryUtil.QueuedTask queuedTask = queuedTaskList.poll();
                    if (queuedTask == null) continue;
                    queuedTask.run();
                }
            }
            timer.reset();
        }
    }

    @SubscribeEvent
    public void onKeyInput(InputEvent.KeyInputEvent event) {
        if (Keyboard.getEventKeyState() && !(mc.currentScreen instanceof MioClickGui) && elytraBind.getValue().getKey() == Keyboard.getEventKey()) {
            elytraOn = !elytraOn;
        }
        if (Keyboard.getEventKeyState() && !(mc.currentScreen instanceof MioClickGui) && noHelmBind.getValue().getKey() == Keyboard.getEventKey()) {
            helmOff = !helmOff;
        }
    }

    private void takeOffSlot(int slot) {
        if (queuedTaskList.isEmpty()) {
            int target = -1;
            for (int i : InventoryUtil.findEmptySlots(XCarry.getInstance().isOn())) {
                if (doneSlots.contains(target)) continue;
                target = i;
                doneSlots.add(i);
            }
            if (target != -1) {
                if (target < 5 && target > 0 || !shiftClick.getValue()) {
                    queuedTaskList.add(new InventoryUtil.QueuedTask(slot));
                    queuedTaskList.add(new InventoryUtil.QueuedTask(target));
                } else {
                    queuedTaskList.add(new InventoryUtil.QueuedTask(slot, true));
                }
                if (updateController.getValue()) {
                    queuedTaskList.add(new InventoryUtil.QueuedTask());
                }
            }
        }
    }

    private void getSlotOn(int slot, int target) {
        if (queuedTaskList.isEmpty()) {
            doneSlots.remove((Object)target);
            if (target < 5 && target > 0 || !shiftClick.getValue()) {
                queuedTaskList.add(new InventoryUtil.QueuedTask(target));
                queuedTaskList.add(new InventoryUtil.QueuedTask(slot));
            } else {
                queuedTaskList.add(new InventoryUtil.QueuedTask(target, true));
            }
            if (updateController.getValue()) {
                queuedTaskList.add(new InventoryUtil.QueuedTask());
            }
        }
    }

    private boolean isSafe() {
        EntityPlayer closest = EntityUtil.getClosestEnemy(closestEnemy.getValue());
        if (closest == null) {
            return true;
        }
        return mc.player.getDistanceSq(closest) >= MathUtil.square(closestEnemy.getValue());
    }
}

