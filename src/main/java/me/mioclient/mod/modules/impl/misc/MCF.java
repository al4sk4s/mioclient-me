package me.mioclient.mod.modules.impl.misc;

import com.mojang.realmsclient.gui.ChatFormatting;
import me.mioclient.api.managers.Managers;
import me.mioclient.mod.commands.Command;
import me.mioclient.mod.modules.Category;
import me.mioclient.mod.modules.Module;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.RayTraceResult;
import org.lwjgl.input.Mouse;

public class MCF extends Module {

    private boolean didClick;

    public MCF() {
        super("MCF", "Middle click your fellow friends.", Category.MISC);
    }

    @Override
    public void onUpdate() {
        if (Mouse.isButtonDown(2)) {

            if (!didClick && mc.currentScreen == null) {
                onClick();
            }
            didClick = true;

        } else {
            didClick = false;
        }
    }

    private void onClick() {
        Entity entity;
        RayTraceResult result = mc.objectMouseOver;

        if (result != null && result.typeOfHit == RayTraceResult.Type.ENTITY && (entity = result.entityHit) instanceof EntityPlayer) {

            if (Managers.FRIENDS.isFriend(entity.getName())) {
                Managers.FRIENDS.removeFriend(entity.getName());
                Command.sendMessage(ChatFormatting.RED + entity.getName() + ChatFormatting.RED + " has been unfriended.");

            } else {
                Managers.FRIENDS.addFriend(entity.getName());
                Command.sendMessage(ChatFormatting.AQUA + entity.getName() + ChatFormatting.GREEN + " has been friended.");
            }
        }
        didClick = true;
    }
}

