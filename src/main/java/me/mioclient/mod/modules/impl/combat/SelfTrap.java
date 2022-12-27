package me.mioclient.mod.modules.impl.combat;

import me.mioclient.api.events.impl.UpdateWalkingPlayerEvent;
import me.mioclient.api.managers.Managers;
import me.mioclient.api.util.entity.EntityUtil;
import me.mioclient.api.util.interact.BlockUtil;
import me.mioclient.api.util.math.Timer;
import me.mioclient.api.util.world.InventoryUtil;
import me.mioclient.mod.modules.Category;
import me.mioclient.mod.modules.Module;
import me.mioclient.mod.modules.settings.Setting;
import net.minecraft.block.BlockEnderChest;
import net.minecraft.block.BlockObsidian;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3i;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.*;

public class SelfTrap extends Module {

    private final Setting<Integer> blocksPerTick =
            add(new Setting<>("BlocksPerTick", 8, 1, 20));
    private final Setting<Integer> delay =
            add(new Setting<>("Delay", 50, 0, 250));
    private final Setting<Boolean> rotate =
            add(new Setting<>("Rotate", true));
    private final Setting<Integer> disableTime =
            add(new Setting<>("DisableTime", 200, 50, 300));
    private final Setting<Boolean> disable =
            add(new Setting<>("AutoDisable", true));
    private final Setting<Boolean> packet =
            add(new Setting<>("Packet", false));

    private final Timer offTimer = new Timer();
    private final Timer timer = new Timer();
    private final Map<BlockPos, Integer> retries = new HashMap<>();
    private final Timer retryTimer = new Timer();
    private int blocksThisTick;
    private boolean isSneaking;

    public SelfTrap() {
        super("SelfTrap", "Lure your enemies in!", Category.COMBAT, true);
    }

    @Override
    public void onEnable() {
        if (fullNullCheck()) {
            disable();
        }
        offTimer.reset();
    }

    @Override
    public void onDisable() {
        isSneaking = EntityUtil.stopSneaking(isSneaking);
        retries.clear();
    }

    @Override
    public void onTick() {
        if (isOn() && (blocksPerTick.getValue() != 1 || !rotate.getValue())) {
            doSelfTrap();
        }
    }

    @SubscribeEvent
    public void onUpdateWalkingPlayer(UpdateWalkingPlayerEvent event) {
        if (isOn() && event.getStage() == 0 && blocksPerTick.getValue() == 1 && rotate.getValue()) {
            doSelfTrap();
        }
    }

    private void doSelfTrap() {
        if (check()) {
            return;
        }
        for (BlockPos position : getPositions()) {
            int placeability = BlockUtil.getPlaceAbility(position, false);
            if (placeability == 1 && (retries.get(position) == null || retries.get(position) < 4)) {
                placeBlock(position);
                retries.put(position, retries.get(position) == null ? 1 : retries.get(position) + 1);
            }
            if (placeability != 3) continue;
            placeBlock(position);
        }
    }

    private List<BlockPos> getPositions() {
        ArrayList<BlockPos> positions = new ArrayList<>();
        positions.add(new BlockPos(mc.player.posX, mc.player.posY + 2.0, mc.player.posZ));
        int placeability = BlockUtil.getPlaceAbility(positions.get(0), false);
        switch (placeability) {
            case 0: {
                return new ArrayList<>();
            }
            case 3: {
                return positions;
            }
            case 1: {
                if (BlockUtil.getPlaceAbility(positions.get(0), false, false) == 3) {
                    return positions;
                }
            }
            case 2: {
                positions.add(new BlockPos(mc.player.posX + 1.0, mc.player.posY + 1.0, mc.player.posZ));
                positions.add(new BlockPos(mc.player.posX + 1.0, mc.player.posY + 2.0, mc.player.posZ));
            }
        }
        positions.sort(Comparator.comparingDouble(Vec3i::getY));
        return positions;
    }

    private void placeBlock(BlockPos pos) {
        if (blocksThisTick < blocksPerTick.getValue()) {
            int originalSlot = mc.player.inventory.currentItem;
            int obbySlot = InventoryUtil.findHotbarBlock(BlockObsidian.class);
            int eChestSot = InventoryUtil.findHotbarBlock(BlockEnderChest.class);
            if (obbySlot == -1 && eChestSot == -1) {
                toggle();
            }
            mc.player.inventory.currentItem = obbySlot == -1 ? eChestSot : obbySlot;
            mc.playerController.updateController();
            
            Managers.INTERACTIONS.placeBlock(pos, rotate.getValue(), packet.getValue(), true);
            
            mc.player.inventory.currentItem = originalSlot;
            mc.playerController.updateController();
            timer.reset();
            ++blocksThisTick;
        }
    }

    private boolean check() {
        if (fullNullCheck()) {
            disable();
            return true;
        }
        int obbySlot = InventoryUtil.findHotbarBlock(BlockObsidian.class);
        int eChestSot = InventoryUtil.findHotbarBlock(BlockEnderChest.class);
        if (obbySlot == -1 && eChestSot == -1) {
            toggle();
        }
        blocksThisTick = 0;
        isSneaking = EntityUtil.stopSneaking(isSneaking);
        if (retryTimer.passedMs(2000L)) {
            retries.clear();
            retryTimer.reset();
        }
        if (!EntityUtil.isSafe(mc.player)) {
            offTimer.reset();
            return true;
        }
        if (disable.getValue() && offTimer.passedMs(disableTime.getValue())) {
            disable();
            return true;
        }
        return !timer.passedMs(delay.getValue());
    }
}

