package me.mioclient.api.util.entity;

import com.mojang.authlib.GameProfile;
import me.mioclient.api.managers.Managers;
import me.mioclient.api.util.Wrapper;
import me.mioclient.api.util.interact.BlockUtil;
import me.mioclient.api.util.math.MathUtil;
import net.minecraft.block.*;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityAgeable;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.EnumCreatureType;
import net.minecraft.entity.item.EntityBoat;
import net.minecraft.entity.item.EntityMinecart;
import net.minecraft.entity.monster.EntityEnderman;
import net.minecraft.entity.monster.EntityIronGolem;
import net.minecraft.entity.monster.EntityPigZombie;
import net.minecraft.entity.passive.EntityAmbientCreature;
import net.minecraft.entity.passive.EntitySquid;
import net.minecraft.entity.passive.EntityWolf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.projectile.EntityFireball;
import net.minecraft.entity.projectile.EntityShulkerBullet;
import net.minecraft.init.Enchantments;
import net.minecraft.init.Items;
import net.minecraft.item.*;
import net.minecraft.network.play.client.CPacketEntityAction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

/**
 * Managing entities, mainly players.
 * Really goofy util.
 */

public class EntityUtil implements Wrapper {

    //Checkers

    public static boolean isArmorLow(EntityPlayer player, int durability) {
        for (ItemStack piece : player.inventory.armorInventory) {

            if (piece == null) {
                return true;
            }

            if (getDamagePercent(piece) >= durability) continue;
            return true;
        }
        return false;
    }

    public static boolean isFeetVisible(Entity entity) {
        return mc.world.rayTraceBlocks(
                new Vec3d(mc.player.posX, mc.player.posX + (double) mc.player.getEyeHeight(), mc.player.posZ),
                new Vec3d(entity.posX, entity.posY, entity.posZ),
                false,
                true,
                false) == null;
    }

    public static boolean isValid(Entity entity, double range) {
        boolean invalid = entity == null
                || isDead(entity)
                || entity.equals(mc.player)
                || entity instanceof EntityPlayer && Managers.FRIENDS.isFriend(entity.getName())
                || mc.player.getDistanceSq(entity) > MathUtil.square(range);

        return !invalid;
    }

    public static boolean isInHole(Entity entity) {
        return BlockUtil.isHole(new BlockPos(entity.posX, entity.posY, entity.posZ));
    }

    public static boolean isTrapped(EntityPlayer player, boolean antiScaffold, boolean antiStep, boolean legs, boolean platform, boolean antiDrop) {
        return getUntrappedBlocks(player, antiScaffold, antiStep, legs, platform, antiDrop).size() == 0;
    }

    public static boolean isHoldingWeapon(EntityPlayer player) {
        return player.getHeldItemMainhand().getItem() instanceof ItemSword || player.getHeldItemMainhand().getItem() instanceof ItemAxe;
    }

    public static boolean isHolding32k(EntityPlayer player) {
        return EnchantmentHelper.getEnchantmentLevel(Enchantments.SHARPNESS, player.getHeldItemMainhand()) >= 1000;
    }

    public static boolean isSafe(Entity entity, int height, boolean floor) {
        return getUnsafeBlocksList(entity, height, floor).size() == 0;
    }

    public static boolean isSafe(Entity entity) {
        return isSafe(entity, 0, false);
    }

    public static boolean isPassive(Entity entity) {
        if (entity instanceof EntityWolf && ((EntityWolf) entity).isAngry()) {
            return false;
        }
        if (entity instanceof EntityAgeable || entity instanceof EntityAmbientCreature || entity instanceof EntitySquid) {
            return true;
        }
        return entity instanceof EntityIronGolem && ((EntityIronGolem) entity).getRevengeTarget() == null;
    }

    public static boolean isMobAggressive(Entity entity) {
        if (entity instanceof EntityPigZombie) {
            if (((EntityPigZombie) entity).isArmsRaised() || ((EntityPigZombie) entity).isAngry()) {
                return true;
            }
        } else {
            if (entity instanceof EntityWolf) {
                return ((EntityWolf) entity).isAngry() && !mc.player.equals(((EntityWolf) entity).getOwner());
            }
            if (entity instanceof EntityEnderman) {
                return ((EntityEnderman) entity).isScreaming();
            }
        }
        return isHostileMob(entity);
    }

    public static boolean isNeutralMob(Entity entity) {
        return entity instanceof EntityPigZombie || entity instanceof EntityWolf || entity instanceof EntityEnderman;
    }

    public static boolean isProjectile(Entity entity) {
        return entity instanceof EntityShulkerBullet || entity instanceof EntityFireball;
    }

    public static boolean isVehicle(Entity entity) {
        return entity instanceof EntityBoat || entity instanceof EntityMinecart;
    }

    public static boolean isHostileMob(Entity entity) {
        return entity.isCreatureType(EnumCreatureType.MONSTER, false) && !isNeutralMob(entity);
    }

    public static boolean isLiving(Entity entity) {
        return entity instanceof EntityLivingBase;
    }

    public static boolean isAlive(Entity entity) {
        return isLiving(entity) && !entity.isDead && ((EntityLivingBase) entity).getHealth() > 0.0f;
    }

    public static boolean isDead(Entity entity) {
        return !isAlive(entity);
    }

    //Getters

    public static EntityPlayer getCopiedPlayer(EntityPlayer player) {
        int count = player.getItemInUseCount();

        EntityPlayer copied = new EntityPlayer(mc.world, new GameProfile(UUID.randomUUID(), player.getName())) {

            @Override
            public boolean isSpectator() {
                return false;
            }

            @Override
            public boolean isCreative() {
                return false;
            }

            @Override
            public int getItemInUseCount() {
                return count;
            }
        };

        copied.setSneaking(player.isSneaking());
        copied.swingProgress = player.swingProgress;
        copied.limbSwing = player.limbSwing;
        copied.limbSwingAmount = player.prevLimbSwingAmount;
        copied.inventory.copyInventory(player.inventory);

        copied.setPrimaryHand(player.getPrimaryHand());
        copied.ticksExisted = player.ticksExisted;
        copied.setEntityId(player.getEntityId());
        copied.copyLocationAndAnglesFrom(player);

        return copied;
    }

    public static int getHitCoolDown(EntityPlayer player) {
        Item item = player.getHeldItemMainhand().getItem();

        if (item instanceof ItemSword) {
            return 600;
        }
        if (item instanceof ItemPickaxe) {
            return 850;
        }
        if (item == Items.IRON_AXE) {
            return 1100;
        }
        if (item == Items.STONE_HOE) {
            return 500;
        }
        if (item == Items.IRON_HOE) {
            return 350;
        }
        if (item == Items.WOODEN_AXE || item == Items.STONE_AXE) {
            return 1250;
        }
        if (item instanceof ItemSpade || item == Items.GOLDEN_AXE || item == Items.DIAMOND_AXE || item == Items.WOODEN_HOE || item == Items.GOLDEN_HOE) {
            return 1000;
        }
        return 250;
    }

    public static EntityPlayer getClosestEnemy(double distance) {
        EntityPlayer closest = null;

        for (EntityPlayer player : mc.world.playerEntities) {
            if (!isValid(player, distance)) continue;

            if (closest == null) {
                closest = player;
                continue;
            }

            if (!(mc.player.getDistanceSq(player) < mc.player.getDistanceSq(closest))) continue;

            closest = player;
        }
        return closest;
    }

    public static List<Vec3d> getUntrappedBlocks(EntityPlayer player, boolean extraTop, boolean antiStep, boolean legs, boolean platform, boolean antiDrop) {
        ArrayList<Vec3d> vec3ds = new ArrayList<>();

        if (!antiStep && getUnsafeBlocksList(player, 2, false).size() == 4) {
            vec3ds.addAll(getUnsafeBlocksList(player, 2, false));
        }

        for (int i = 0; i < getTrapOffsets(extraTop, antiStep, legs, platform, antiDrop).length; ++i) {
            Vec3d vector = getTrapOffsets(extraTop, antiStep, legs, platform, antiDrop)[i];
            BlockPos targetPos = new BlockPos(player.getPositionVector()).add(vector.x, vector.y, vector.z);
            Block block = mc.world.getBlockState(targetPos).getBlock();

            if (!(block instanceof BlockAir)
                    && !(block instanceof BlockLiquid)
                    && !(block instanceof BlockTallGrass)
                    && !(block instanceof BlockFire)
                    && !(block instanceof BlockDeadBush)
                    && !(block instanceof BlockSnow)) continue;

            vec3ds.add(vector);
        }
        return vec3ds;
    }

    public static List<Vec3d> getTrapOffsetList(Vec3d vec, boolean extraTop, boolean antiStep, boolean legs, boolean platform, boolean antiDrop, boolean raytrace) {
        ArrayList<Vec3d> retval = new ArrayList<>();

        if (!antiStep) {

            List<Vec3d> offset = getUnsafeBlocksList(vec, 2, false);

            if (offset.size() == 4) {

                for (Vec3d vector : offset) {
                    BlockPos pos = new BlockPos(vec).add(vector.x, vector.y, vector.z);

                    switch (BlockUtil.getPlaceAbility(pos, raytrace)) {

                        case 0:
                            break;

                        case -1:

                        case 1:

                        case 2:
                            continue;

                        case 3:
                            retval.add(vec.add(vector));
                            break;
                    }
                    Collections.addAll(
                            retval,
                            MathUtil.convertVectors(vec, getTrapOffsets(extraTop, false, legs, platform, antiDrop)));

                    return retval;
                }
            }
        }

        Collections.addAll(
                retval,
                MathUtil.convertVectors(vec, getTrapOffsets(extraTop, antiStep, legs, platform, antiDrop)));

        return retval;
    }

    public static Vec3d[] getTrapOffsets(boolean extraTop, boolean antiStep, boolean legs, boolean platform, boolean antiDrop) {
        ArrayList<Vec3d> offsetArray = new ArrayList<>(getOffsetList(1, false));
        offsetArray.add(new Vec3d(0.0, 2.0, 0.0));

        if (extraTop) {
            offsetArray.add(new Vec3d(0.0, 3.0, 0.0));
        }
        if (antiStep) {
            offsetArray.addAll(getOffsetList(2, false));
        }
        if (legs) {
            offsetArray.addAll(getOffsetList(0, false));
        }
        if (platform) {
            offsetArray.addAll(getOffsetList(-1, false));
            offsetArray.add(new Vec3d(0.0, -1.0, 0.0));
        }
        if (antiDrop) {
            offsetArray.add(new Vec3d(0.0, -2.0, 0.0));
        }

        Vec3d[] array = new Vec3d[((List<Vec3d>) offsetArray).size()];

        return ((List<Vec3d>) offsetArray).toArray(array);
    }

    public static List<Vec3d> getOffsetList(int y, boolean floor) {
        ArrayList<Vec3d> offsets = new ArrayList<>();
        offsets.add(new Vec3d(-1.0, y, 0.0));
        offsets.add(new Vec3d(1.0, y, 0.0));
        offsets.add(new Vec3d(0.0, y, -1.0));
        offsets.add(new Vec3d(0.0, y, 1.0));

        if (floor) {
            offsets.add(new Vec3d(0.0, y - 1, 0.0));
        }

        return offsets;
    }

    public static Vec3d[] getOffsets(int y, boolean floor) {
        List<Vec3d> offsets = getOffsetList(y, floor);
        Vec3d[] array = new Vec3d[offsets.size()];
        return offsets.toArray(array);
    }

    public static List<Vec3d> getUnsafeBlocksList(Vec3d pos, int height, boolean floor) {
        ArrayList<Vec3d> vec3ds = new ArrayList<>();
        for (Vec3d vector : getOffsets(height, floor)) {
            BlockPos targetPos = new BlockPos(pos).add(vector.x, vector.y, vector.z);
            Block block = mc.world.getBlockState(targetPos).getBlock();
            if (!(block instanceof BlockAir) && !(block instanceof BlockLiquid) && !(block instanceof BlockTallGrass) && !(block instanceof BlockFire) && !(block instanceof BlockDeadBush) && !(block instanceof BlockSnow))
                continue;
            vec3ds.add(vector);
        }
        return vec3ds;
    }

    public static List<Vec3d> getUnsafeBlocksList(Entity entity, int height, boolean floor) {
        return getUnsafeBlocksList(entity.getPositionVector(), height, floor);
    }

    public static float getHealth(Entity entity) {
        if (isLiving(entity)) {
            EntityLivingBase livingBase = (EntityLivingBase) entity;
            return livingBase.getHealth() + livingBase.getAbsorptionAmount();
        }
        return 0.0f;
    }

    public static BlockPos getRoundedPos(Entity entity) {
        return new BlockPos(MathUtil.roundVec(entity.getPositionVector(), 0));
    }

    public static int getDamagePercent(ItemStack stack) {
        return (int) ((stack.getMaxDamage() - stack.getItemDamage()) / Math.max(0.1, stack.getMaxDamage()) * 100.0f);
    }

    //Setters

    public static boolean stopSneaking(boolean isSneaking) {
        if (isSneaking && mc.player != null) {
            mc.player.connection.sendPacket(new CPacketEntityAction(mc.player, CPacketEntityAction.Action.STOP_SNEAKING));
        }
        return false;
    }
}