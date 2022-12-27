package me.mioclient.mod.modules.impl.misc;

import com.mojang.realmsclient.gui.ChatFormatting;
import me.mioclient.api.managers.Managers;
import me.mioclient.mod.commands.Command;
import me.mioclient.mod.modules.Category;
import me.mioclient.mod.modules.Module;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityEnderPearl;
import net.minecraft.entity.player.EntityPlayer;

public class PearlNotify extends Module {

    private boolean flag;

    public PearlNotify() {
        super("PearlNotify", "Notifies pearl throws.", Category.MISC);
    }

    @Override
    public void onEnable() {
        flag = true;
    }

    @Override
    public void onUpdate() {
        Entity enderPearl = null;

        if (mc.world == null || mc.player == null) {
            return;
        }

        for (Entity e : mc.world.loadedEntityList) {
            if (e instanceof EntityEnderPearl) {
                enderPearl = e;
                break;
            }
        }
        if (enderPearl == null) {
            flag = true;
            return;
        }

        EntityPlayer closestPlayer = null;
        for (EntityPlayer entity : mc.world.playerEntities) {
            if (closestPlayer == null) {
                closestPlayer = entity;
            } else {
                if (closestPlayer.getDistance(enderPearl) <= entity.getDistance(enderPearl)) {
                    continue;
                }
                closestPlayer = entity;
            }
        }

        if (closestPlayer == mc.player) {
            flag = false;
        }

        if (closestPlayer != null && flag) {
            String faceing = enderPearl.getHorizontalFacing().toString();

            if (faceing.equals("West")) {
                faceing = "East";
            } else if (faceing.equals("East")) {
                faceing = "West";
            }

            Command.sendMessageWithID(
                    Managers.FRIENDS.isFriend(closestPlayer.getName()) ?
                    (ChatFormatting.AQUA + closestPlayer.getName()
                    + ChatFormatting.GRAY
                    + " has just thrown a pearl heading " + ChatFormatting.AQUA + faceing + "!") :

                    (ChatFormatting.RED + closestPlayer.getName()
                    + ChatFormatting.GRAY
                    + " has just thrown a pearl heading " + ChatFormatting.RED + faceing + "!"),
                    closestPlayer.getEntityId());

            flag = false;
        }
    }
}