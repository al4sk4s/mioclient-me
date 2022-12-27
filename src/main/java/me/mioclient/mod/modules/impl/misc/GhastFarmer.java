package me.mioclient.mod.modules.impl.misc;

import com.mojang.realmsclient.gui.ChatFormatting;
import me.mioclient.mod.commands.Command;
import me.mioclient.mod.modules.Category;
import me.mioclient.mod.modules.Module;
import me.mioclient.mod.modules.settings.Setting;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.monster.EntityGhast;
import net.minecraft.init.Items;
import net.minecraft.init.SoundEvents;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.stream.Collectors;

public class GhastFarmer extends Module {

    private final Setting<Boolean> notifySound =
            add(new Setting<>("Sound", false));

    public int currentX;
    public int currentY;
    public int currentZ;
    public int itemX;
    public int itemY;
    public int itemZ;
    public int ghastX;
    public int ghastY;
    public int ghastZ;
    public boolean ding;

    public GhastFarmer() {
        super("GhastFarmer", "Auto Ghast Farmer", Category.MISC);
    }

    @Override
    public void onEnable() {
        block3: {
            block2: {
                if (GhastFarmer.mc.player == null) break block2;
                if (GhastFarmer.mc.world != null) break block3;
            }
            return;
        }
        currentX = (int)GhastFarmer.mc.player.posX;
        currentY = (int)GhastFarmer.mc.player.posY;
        currentZ = (int)GhastFarmer.mc.player.posZ;
    }

    @Override
    public void onDisable() {
        block3: {
            block2: {
                if (GhastFarmer.mc.player == null) break block2;
                if (GhastFarmer.mc.world != null) break block3;
            }
            return;
        }
        GhastFarmer.mc.player.sendChatMessage("#stop");
    }

    @Override
    public void onUpdate() {
        try {
            Class.forName("baritone.api.BaritoneAPI");

                if (GhastFarmer.mc.player == null || GhastFarmer.mc.world == null) {
                    return;
                }
                Entity ghastEnt = null;
                double dist = Double.longBitsToDouble(Double.doubleToLongBits(0.017520017079696953) ^ 0x7FC8F0C47187D7FBL);
                for (Entity entity : GhastFarmer.mc.world.loadedEntityList) {
                    double ghastDist;
                    if (!(entity instanceof EntityGhast) || !((ghastDist = GhastFarmer.mc.player.getDistance(entity)) < dist))
                        continue;
                    dist = ghastDist;
                    ghastEnt = entity;
                    ghastX = (int) entity.posX;
                    ghastY = (int) entity.posY;
                    ghastZ = (int) entity.posZ;
                    ding = true;
                }
                if (ding) {
                    if (notifySound.getValue()) {
                        GhastFarmer.mc.player.playSound(SoundEvents.BLOCK_NOTE_BELL, Float.intBitsToFloat(Float.floatToIntBits(5.2897425f) ^ 0x7F294592), Float.intBitsToFloat(Float.floatToIntBits(5.5405655f) ^ 0x7F314C50));
                    }
                    ding = false;
                }
                ArrayList entityItems = new ArrayList();
                entityItems.addAll(GhastFarmer.mc.world.loadedEntityList.stream().filter(GhastFarmer::lambda$onUpdate$0).map(GhastFarmer::lambda$onUpdate$1).filter(GhastFarmer::lambda$onUpdate$2).collect(Collectors.toList()));
                Entity itemEnt = null;
                Iterator iterator = entityItems.iterator();
                while (iterator.hasNext()) {
                    Entity item;
                    itemEnt = item = (Entity) iterator.next();
                    itemX = (int) item.posX;
                    itemY = (int) item.posY;
                    itemZ = (int) item.posZ;
                }
                if (ghastEnt != null) {
                    GhastFarmer.mc.player.sendChatMessage("#goto " + ghastX + " " + ghastY + " " + ghastZ);
                } else if (itemEnt != null) {
                    GhastFarmer.mc.player.sendChatMessage("#goto " + itemX + " " + itemY + " " + itemZ);
                } else {
                    GhastFarmer.mc.player.sendChatMessage("#goto " + currentX + " " + currentY + " " + currentZ);
                }

        } catch (Exception e) {
            Command.sendMessage("[" + getName() + "] " + ChatFormatting.RED + "This mod needs Baritone API! Download at: " + ChatFormatting.DARK_AQUA + "https://github.com/cabaletta/baritone/releases/download/v1.2.15/baritone-api-forge-1.2.15.jar");
            disable();
        }
    }

    private static boolean lambda$onUpdate$2(EntityItem entityItem) {
        EntityItem entityItem2 = null;
        return entityItem2.getItem().getItem() == Items.GHAST_TEAR;
    }

    private static EntityItem lambda$onUpdate$1(Entity entity) {
        Entity entity2 = null;
        return (EntityItem)entity2;
    }

    private static boolean lambda$onUpdate$0(Entity entity) {
        Entity entity2 = null;
        return entity2 instanceof EntityItem;
    }
}