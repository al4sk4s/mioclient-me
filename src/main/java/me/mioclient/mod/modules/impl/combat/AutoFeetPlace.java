package me.mioclient.mod.modules.impl.combat;

import me.mioclient.api.events.impl.PacketEvent;
import me.mioclient.api.events.impl.Render3DEvent;
import me.mioclient.api.events.impl.StepEvent;
import me.mioclient.api.managers.Managers;
import me.mioclient.api.util.entity.EntityUtil;
import me.mioclient.api.util.interact.BlockUtil;
import me.mioclient.api.util.math.MathUtil;
import me.mioclient.api.util.math.Timer;
import me.mioclient.api.util.render.RenderUtil;
import me.mioclient.mod.modules.Category;
import me.mioclient.mod.modules.Module;
import me.mioclient.mod.modules.settings.Setting;
import net.minecraft.client.entity.EntityOtherPlayerMP;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityEnderCrystal;
import net.minecraft.entity.item.EntityExpBottle;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.item.EntityXPOrb;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.init.Blocks;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.Item;
import net.minecraft.network.play.client.CPacketAnimation;
import net.minecraft.network.play.client.CPacketHeldItemChange;
import net.minecraft.network.play.client.CPacketPlayer;
import net.minecraft.network.play.client.CPacketUseEntity;
import net.minecraft.network.play.server.*;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author t.me/asphyxia1337
 */

public class AutoFeetPlace extends Module {

    private final Setting<Timing> timing =
            add(new Setting<>("Timing", Timing.VANILLA));
    private final Setting<Integer> tickDelay =
            add(new Setting<>("TickDelay", 50, 0, 250));
    private final Setting<Integer> blocksPerTick =
            add(new Setting<>("BPT", 2, 1, 30));
    private final Setting<Boolean> rotate =
            add(new Setting<>("Rotate", true));
    private final Setting<Boolean> packet =
            add(new Setting<>("Packet", true));
    private final Setting<Boolean> noLag =
            add(new Setting<>("NoLag", true));
    private final Setting<Boolean> clean =
            add(new Setting<>("Clean", true));
    private final Setting<Boolean> jumpDisable =
            add(new Setting<>("JumpDisable", true));
    private final Setting<Swap> swap =
            add(new Setting<>("Swap", Swap.NORMAL));
    private final Setting<Center> center =
            add(new Setting<>("Center", Center.NONE));

    private final Setting<Boolean> render =
            add(new Setting<>("Render", true).setParent());
    private final Setting<Boolean> box =
            add(new Setting<>("Box", true, v -> render.isOpen()));
    private final Setting<Boolean> line =
            add(new Setting<>("Line", true, v -> render.isOpen()));

    private final Map<BlockPos, Long> renderBlocks = new ConcurrentHashMap<>();
    private final List<BlockPos> activeBlocks = new ArrayList<>();

    private final Timer renderTimer = new Timer();
    private final Timer lagTimer = new Timer();
    private final Timer delayTimer = new Timer();
    private final Timer hitTimer = new Timer();

    private boolean isSneaking;

    private EntityPlayer interceptedBy;

    public AutoFeetPlace() {
        super("AutoFeetPlace", "Surrounds your feet with obby.", Category.COMBAT, true);
    }

    private enum Timing {
        VANILLA,
        SEQUENTIAL
    }

    private enum Swap {
        PACKET,
        NORMAL
    }

    private enum Center {
        NONE,
        MOTION,
        TELEPORT
    }

    @Override
    public String getInfo() {
        return String.valueOf(renderBlocks.size());
    }

    @Override
    public void onEnable() {
        if (fullNullCheck() || nullCheck()) {
            return;
        }

        interceptedBy = null;

        if (center.getValue() != Center.NONE) {

            double centerX = Math.floor(mc.player.posX) + 0.5;
            double centerZ = Math.floor(mc.player.posZ) + 0.5;

            switch (center.getValue()) {
                case NONE:
                default:
                    break;
                case MOTION:
                    mc.player.motionX = (centerX - mc.player.posX) / 2;
                    mc.player.motionZ = (centerZ - mc.player.posZ) / 2;
                    break;
                case TELEPORT:
                    mc.player.setPosition(centerX, mc.player.posY, centerZ);
                    mc.player.connection.sendPacket(new CPacketPlayer.Position(centerX, mc.player.posY, centerZ, mc.player.onGround));
                    break;
            }
        }
    }

    @Override
    public void onDisable() {
        if (nullCheck()) {
            return;
        }
        isSneaking = EntityUtil.stopSneaking(isSneaking);
    }

    @Override
    public void onUpdate() {
        if (!fullNullCheck()) {
            doFeetPlace();
        }
    }

    @Override
    public void onRender3D(Render3DEvent event) {

        if (render.getValue()) {
            renderTimer.reset();

            renderBlocks.forEach((pos, time) -> {
                int lineA = 255;
                int fillA = 80;

                if (System.currentTimeMillis() - time > 400) {
                    renderTimer.reset();
                    renderBlocks.remove(pos);

                } else {
                    long endTime = System.currentTimeMillis() - time - (100);
                    double normal = MathUtil.normalize((double) endTime, 0.0D, (500));

                    normal = MathHelper.clamp(normal, 0.0D, 1.0D);
                    normal = -normal + 1.0D;

                    int firstAl = (int) (normal * (double) lineA);
                    int secondAl = (int) (normal * (double) fillA);

                    RenderUtil.drawBoxESP(
                            new BlockPos(pos),
                            Managers.COLORS.getCurrent(),
                            true,
                            new Color(255, 255, 255, firstAl),
                            0.7f,
                            line.getValue(),
                            box.getValue(),
                            secondAl,
                            true,
                            0.0);
                }
            });
        }
    }

    @SubscribeEvent
    public void onStep(StepEvent event) {
        if (event.getStage() == 0 && jumpDisable.getValue() && mc.player.stepHeight > 1) {
            disable();
        }
    }

    @SubscribeEvent
    public void onPacketReceive(PacketEvent.Receive event) {
        if (fullNullCheck() || nullCheck()) {
            return;
        }

        if (event.getPacket() instanceof SPacketPlayerPosLook) {
            lagTimer.reset();
        }

        if (event.getPacket() instanceof SPacketBlockChange) {

            if (renderBlocks.containsKey(((SPacketBlockChange) event.getPacket()).getBlockPosition())) {
                renderTimer.reset();

                if (((SPacketBlockChange) event.getPacket()).getBlockState().getBlock() != Blocks.AIR && renderTimer.passedMs(400)) {
                    renderBlocks.remove(((SPacketBlockChange) event.getPacket()).getBlockPosition());
                }
            }

            if (timing.getValue() == Timing.SEQUENTIAL) {

                if (!lagTimer.passedMs(500) && noLag.getValue()) return;

                BlockPos changePos = ((SPacketBlockChange) event.getPacket()).getBlockPosition();

                if (((SPacketBlockChange) event.getPacket()).getBlockState().getMaterial().isReplaceable()) {

                    if (mc.world.getEntitiesWithinAABB(EntityPlayerSP.class, new AxisAlignedBB(changePos)).isEmpty()) {

                        if (getOffsets().contains(changePos)) {
                            activeBlocks.clear();

                            int oldSlot = mc.player.inventory.currentItem;
                            int blockSlot = getSlot();

                            activeBlocks.add(changePos);
                            renderBlocks.put(changePos, System.currentTimeMillis());
                            placeBlock(changePos, blockSlot, oldSlot);
                        }
                    }
                }
            }

            if (event.getPacket() instanceof SPacketMultiBlockChange) {

                if (timing.getValue() == Timing.SEQUENTIAL) {

                    if (!lagTimer.passedMs(500) && noLag.getValue()) return;

                    for (SPacketMultiBlockChange.BlockUpdateData blockUpdateData : ((SPacketMultiBlockChange) event.getPacket()).getChangedBlocks()) {

                        BlockPos changePos = blockUpdateData.getPos();

                        if (blockUpdateData.getBlockState().getMaterial().isReplaceable()) {

                            if (getOffsets().contains(changePos)) {
                                activeBlocks.clear();

                                int oldSlot = mc.player.inventory.currentItem;
                                int blockSlot = getSlot();

                                activeBlocks.add(changePos);
                                renderBlocks.put(changePos, System.currentTimeMillis());
                                placeBlock(changePos, blockSlot, oldSlot);
                            }
                        }
                    }
                }
            }
        }

        if (event.getPacket() instanceof SPacketSoundEffect && timing.getValue() == Timing.SEQUENTIAL && clean.getValue()) {
            SPacketSoundEffect packet = event.getPacket();
            if (packet.getCategory() == SoundCategory.BLOCKS && packet.getSound() == SoundEvents.ENTITY_GENERIC_EXPLODE) {
                List<BlockPos> offsets = getOffsets();

                for (BlockPos pos : offsets) {

                    for (EntityEnderCrystal crystal : mc.world.getEntitiesWithinAABB(EntityEnderCrystal.class, new AxisAlignedBB(pos))) {

                        if (offsets.contains(crystal.getPosition())) {
                            crystal.setDead();
                        }
                    }
                }
            }
        }

        if (event.getPacket() instanceof SPacketSpawnObject && timing.getValue() == Timing.SEQUENTIAL && clean.getValue()) {
            SPacketSpawnObject packet = event.getPacket();

            List<BlockPos> offsets = getOffsets();

            for (BlockPos pos : offsets) {

                for (EntityEnderCrystal crystal : mc.world.getEntitiesWithinAABB(EntityEnderCrystal.class, new AxisAlignedBB(pos))) {

                    if (packet.getEntityID() == crystal.getEntityId() && hitTimer.passedMs(150)) {

                        mc.player.connection.sendPacket(new CPacketUseEntity(crystal));
                        mc.player.connection.sendPacket(new CPacketAnimation(EnumHand.MAIN_HAND));
                        hitTimer.reset();
                        break;
                    }
                }
            }
        }
    }

    private void doFeetPlace() {
        if (mc.player.motionY > 0 && jumpDisable.getValue()) {
            disable();
            return;
        }

        if (!lagTimer.passedMs(500) && noLag.getValue()) return;

        interceptedBy = null;

        if (delayTimer.passedMs(tickDelay.getValue())) {
            activeBlocks.clear();

            int oldSlot = mc.player.inventory.currentItem;
            int blockSlot = getSlot();

            if (blockSlot == -1) {
                disable();
                return;
            }
            int blocksInTick = 0;
            isSneaking = EntityUtil.stopSneaking(isSneaking);

            for (int i = 0; i < 1; ++i) {
                List<BlockPos> offsets = getOffsets();

                for (BlockPos pos : offsets) {
                    if (blocksInTick > blocksPerTick.getValue()) {
                        continue;
                    }
                    if (!isPlaceable(pos)) {
                        continue;
                    }
                    activeBlocks.add(pos);

                    boolean intercepted = false;

                    for (Entity entity : mc.world.getEntitiesWithinAABB(Entity.class, new AxisAlignedBB(pos))) {
                        if (entity instanceof EntityEnderCrystal && clean.getValue() && hitTimer.passedMs(150)) {

                            mc.player.connection.sendPacket(new CPacketUseEntity(entity));
                            mc.player.connection.sendPacket(new CPacketAnimation(EnumHand.MAIN_HAND));
                            hitTimer.reset();
                            break;
                        }

                        if (entity instanceof EntityPlayer && entity != mc.player) {
                            interceptedBy = (EntityPlayer) entity;
                            intercepted = true;
                        }
                    }

                    if (intercepted) continue;

                    renderBlocks.put(pos, System.currentTimeMillis());
                    placeBlock(pos, blockSlot, oldSlot);
                    ++blocksInTick;
                }

                if (interceptedBy != null) {
                    List<BlockPos> enemyOffsets = getEnemyOffsets(interceptedBy);
                    int maxStep = enemyOffsets.size();
                    int offsetStep = 0;

                    while (blocksInTick <= blocksPerTick.getValue()) {

                        if (offsetStep >= maxStep) {
                            break;
                        }

                        BlockPos newPos = enemyOffsets.get(offsetStep++);

                        boolean foundSomeone = false;
                        for (Object entity : mc.world.getEntitiesWithinAABB(Entity.class, new AxisAlignedBB(newPos))) {
                            if (entity instanceof EntityPlayer) {
                                foundSomeone = true;
                                break;
                            }
                        }

                        if (foundSomeone || !mc.world.getBlockState(newPos).getMaterial().isReplaceable() || !isPlaceable(newPos)) {
                            continue;
                        }

                        activeBlocks.add(newPos);

                        boolean interceptedByCrystal = false;

                        for (EntityEnderCrystal crystal : mc.world.getEntitiesWithinAABB(EntityEnderCrystal.class, new AxisAlignedBB(newPos))) {

                            interceptedByCrystal = true;

                            if (hitTimer.passedMs(150) && clean.getValue()) {

                                mc.player.connection.sendPacket(new CPacketUseEntity(crystal));
                                mc.player.connection.sendPacket(new CPacketAnimation(EnumHand.MAIN_HAND));
                                hitTimer.reset();
                                break;
                            }
                        }

                        if (interceptedByCrystal) continue;

                        renderBlocks.put(newPos, System.currentTimeMillis());
                        placeBlock(newPos, blockSlot, oldSlot);
                        ++blocksInTick;
                    }
                }
            }
            delayTimer.reset();
        }
    }

    private void doSwap(int slot) {
        if (swap.getValue() == Swap.NORMAL) {
            mc.player.inventory.currentItem = slot;

        } else {
            mc.getConnection().sendPacket(new CPacketHeldItemChange(slot));
        }
        mc.playerController.updateController();
    }

    private void placeBlock(BlockPos pos, int blockSlot, int oldSlot) {
        if (BlockUtil.checkForEntities(pos) || blockSlot == -1) {
            return;
        }

        doSwap(blockSlot);

        Managers.INTERACTIONS.placeBlock(pos, rotate.getValue(), packet.getValue(), clean.getValue());

        doSwap(oldSlot);
    }

    private int getSlot() {
        int slot = -1;
        slot = getHotbarItemSlot(Item.getItemFromBlock(Blocks.OBSIDIAN));

        if (slot == -1) {
            slot = getHotbarItemSlot(Item.getItemFromBlock(Blocks.ENDER_CHEST));
        }

        return slot;
    }

    private int getHotbarItemSlot(Item item) {
        int slot = -1;
        for (int i = 0; i < 9; ++i) {
            if (!mc.player.inventory.getStackInSlot(i).getItem().equals(item)) {
                continue;
            }
            slot = i;
            break;
        }
        return slot;
    }

    private boolean isInterceptedByOther(BlockPos pos) {
        for (Entity entity : mc.world.loadedEntityList) {

            if (entity instanceof EntityOtherPlayerMP || entity instanceof EntityItem || entity instanceof EntityEnderCrystal || entity instanceof EntityXPOrb || entity instanceof EntityExpBottle || entity instanceof EntityArrow) {
                continue;
            }

            if (new AxisAlignedBB(pos).intersects(entity.getEntityBoundingBox())) {
                return true;
            }
        }
        return false;
    }

    private boolean isPlaceable(BlockPos pos) {
        boolean placeable = mc.world.getBlockState(pos).getMaterial().isReplaceable();

        if (isInterceptedByOther(pos)) {
            placeable = false;
        }
        return placeable;
    }

    private List<BlockPos> getOffsets() {
        double calcPosX = mc.player.posX;
        double calcPosZ = mc.player.posZ;

        BlockPos playerPos = getPlayerPos();
        ArrayList<BlockPos> offsets = new ArrayList<>();
        int z;
        int x;
        double decimalX = Math.abs(calcPosX) - Math.floor(Math.abs(calcPosX));
        double decimalZ = Math.abs(calcPosZ) - Math.floor(Math.abs(calcPosZ));
        int lengthXPos = calcLength(decimalX, false);
        int lengthXNeg = calcLength(decimalX, true);
        int lengthZPos = calcLength(decimalZ, false);
        int lengthZNeg = calcLength(decimalZ, true);
        ArrayList<BlockPos> tempOffsets = new ArrayList<>();
        offsets.addAll(getOverlapPos());
        for (x = 1; x < lengthXPos + 1; ++x) {
            tempOffsets.add(addToPlayer(playerPos, x, 0.0, 1 + lengthZPos));
            tempOffsets.add(addToPlayer(playerPos, x, 0.0, -(1 + lengthZNeg)));
        }
        for (x = 0; x <= lengthXNeg; ++x) {
            tempOffsets.add(addToPlayer(playerPos, -x, 0.0, 1 + lengthZPos));
            tempOffsets.add(addToPlayer(playerPos, -x, 0.0, -(1 + lengthZNeg)));
        }
        for (z = 1; z < lengthZPos + 1; ++z) {
            tempOffsets.add(addToPlayer(playerPos, 1 + lengthXPos, 0.0, z));
            tempOffsets.add(addToPlayer(playerPos, -(1 + lengthXNeg), 0.0, z));
        }
        for (z = 0; z <= lengthZNeg; ++z) {
            tempOffsets.add(addToPlayer(playerPos, 1 + lengthXPos, 0.0, -z));
            tempOffsets.add(addToPlayer(playerPos, -(1 + lengthXNeg), 0.0, -z));
        }
        for (BlockPos pos2 : tempOffsets) {
            if (!isSurrounded(pos2)) {
                offsets.add(pos2.add(0, -1, 0));
            }
            offsets.add(pos2);
        }
        return offsets;
    }

    private List<BlockPos> getOverlapPos() {
        double calcPosX = mc.player.posX;
        double calcPosZ = mc.player.posZ;

        ArrayList<BlockPos> positions = new ArrayList<>();
        double decimalX = calcPosX - Math.floor(calcPosX);
        double decimalZ = calcPosZ - Math.floor(calcPosZ);
        int offX = calcOffset(decimalX);
        int offZ = calcOffset(decimalZ);
        positions.add(getPlayerPos());
        for (int x = 0; x <= Math.abs(offX); ++x) {
            for (int z = 0; z <= Math.abs(offZ); ++z) {
                int properX = x * offX;
                int properZ = z * offZ;
                positions.add(getPlayerPos().add(properX, -1, properZ));
            }
        }
        return positions;
    }

    private BlockPos getPlayerPos() {
        double calcPosX = mc.player.posX;
        double calcPosY = mc.player.posY;
        double calcPosZ = mc.player.posZ;

        double decimalPoint = calcPosY - Math.floor(calcPosY);
        return new BlockPos(calcPosX, decimalPoint > 0.8 ? Math.floor(calcPosY) + 1.0 : Math.floor(calcPosY), calcPosZ);
    }

    private List<BlockPos> getEnemyOffsets(EntityPlayer e) {
        if (e == mc.player){
            return null;
        }
        BlockPos playerPos = getEnemyPos(e);
        ArrayList<BlockPos> offsets = new ArrayList<>();

        int z;
        int x;

        double decimalX = Math.abs(e.posX) - Math.floor(Math.abs(e.posX));
        double decimalZ = Math.abs(e.posZ) - Math.floor(Math.abs(e.posZ));

        int lengthXPos = calcLength(decimalX, false);
        int lengthXNeg = calcLength(decimalX, true);
        int lengthZPos = calcLength(decimalZ, false);
        int lengthZNeg = calcLength(decimalZ, true);

        ArrayList<BlockPos> tempOffsets = new ArrayList<>();

        offsets.addAll(getEnemyOverlapPos(e));

        for (x = 1; x < lengthXPos + 1; ++x) {
            tempOffsets.add(addToPlayer(playerPos, x, 0.0, 1 + lengthZPos));
            tempOffsets.add(addToPlayer(playerPos, x, 0.0, -(1 + lengthZNeg)));
        }
        for (x = 0; x <= lengthXNeg; ++x) {
            tempOffsets.add(addToPlayer(playerPos, -x, 0.0, 1 + lengthZPos));
            tempOffsets.add(addToPlayer(playerPos, -x, 0.0, -(1 + lengthZNeg)));
        }
        for (z = 1; z < lengthZPos + 1; ++z) {
            tempOffsets.add(addToPlayer(playerPos, 1 + lengthXPos, 0.0, z));
            tempOffsets.add(addToPlayer(playerPos, -(1 + lengthXNeg), 0.0, z));
        }
        for (z = 0; z <= lengthZNeg; ++z) {
            tempOffsets.add(addToPlayer(playerPos, 1 + lengthXPos, 0.0, -z));
            tempOffsets.add(addToPlayer(playerPos, -(1 + lengthXNeg), 0.0, -z));
        }

        offsets.addAll(tempOffsets);

        return offsets;
    }

    private List<BlockPos> getEnemyOverlapPos(EntityPlayer e) {
        ArrayList<BlockPos> positions = new ArrayList<>();

        double decimalX = e.posX - Math.floor(e.posX);
        double decimalZ = e.posZ - Math.floor(e.posZ);

        int offX = calcOffset(decimalX);
        int offZ = calcOffset(decimalZ);
        positions.add(getEnemyPos(e));
        for (int x = 0; x <= Math.abs(offX); ++x) {
            for (int z = 0; z <= Math.abs(offZ); ++z) {
                int properX = x * offX;
                int properZ = z * offZ;
                positions.add(getEnemyPos(e).add(properX, -1, properZ));
            }
        }
        return positions;
    }

    private BlockPos getEnemyPos(EntityPlayer e) {
        double decimalPoint = mc.player.posY - Math.floor(mc.player.posY);
        return new BlockPos(e.posX, decimalPoint > 0.8 ? Math.floor(mc.player.posY) + 1.0 : Math.floor(mc.player.posY), e.posZ);
    }

    private boolean isSurrounded(BlockPos pos) {
        for (EnumFacing facing : EnumFacing.VALUES) {
            if (mc.world.getBlockState(pos.offset(facing)).getBlock() == Blocks.AIR) {
                continue;
            }
            return true;
        }
        return false;
    }

    private BlockPos addToPlayer(BlockPos playerPos, double x, double y, double z) {
        if (playerPos.getX() < 0) {
            x = -x;
        }
        if (playerPos.getY() < 0) {
            y = -y;
        }
        if (playerPos.getZ() < 0) {
            z = -z;
        }
        return playerPos.add(x, y, z);
    }

    private int calcLength(double decimal, boolean negative) {
        if (negative) {
            return decimal <= 0.3 ? 1 : 0;
        }
        return decimal >= 0.7 ? 1 : 0;
    }

    private int calcOffset(double dec) {
        return dec >= 0.7 ? 1 : (dec <= 0.3 ? -1 : 0);
    }
}