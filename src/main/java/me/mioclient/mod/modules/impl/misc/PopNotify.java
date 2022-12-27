package me.mioclient.mod.modules.impl.misc;

import com.mojang.realmsclient.gui.ChatFormatting;
import me.mioclient.api.managers.Managers;
import me.mioclient.mod.commands.Command;
import me.mioclient.mod.modules.Category;
import me.mioclient.mod.modules.Module;
import net.minecraft.entity.player.EntityPlayer;

import java.util.HashMap;

public class PopNotify extends Module {

    public static HashMap<String, Integer> totemPops = new HashMap<>();

    public PopNotify() {
        super("PopNotify", "Counts other players totem pops.", Category.MISC);
    }

    @Override
    public void onEnable() {
        totemPops.clear();
    }

    @Override
    public void onDeath(EntityPlayer player) {
        boolean isFriend = (Managers.FRIENDS.isFriend(player.getName()));

        if (totemPops.containsKey(player.getName())) {
            int popCount = totemPops.get(player.getName());
            totemPops.remove(player.getName());

            Command.sendMessageWithID("\u00a7r"
                    + (isFriend ? ChatFormatting.AQUA : ChatFormatting.WHITE)
                    + player.getName()
                    + "\u00a7r" + " died after popping " + "\u00a7("
                    + (isFriend ? ChatFormatting.AQUA : ChatFormatting.WHITE)
                    + ChatFormatting.BOLD + popCount + ChatFormatting.RESET + "\u00a7r"
                    + (popCount == 1 ? " totem." : " totems."), -123456789);
        }
    }

    @Override
    public void onTotemPop(EntityPlayer player) {
        int popCount = 1;
        int id = player.getEntityId();
        boolean isFriend = (Managers.FRIENDS.isFriend(player.getName()));

        if (fullNullCheck() || mc.player.equals(player)) {
            return;
        }

        if (totemPops.containsKey(player.getName())) {
            popCount = totemPops.get(player.getName());
            totemPops.put(player.getName(), ++popCount);
        } else {
            totemPops.put(player.getName(), popCount);
        }

        Command.sendMessageWithID("\u00a7r"
                + (isFriend ? ChatFormatting.AQUA : ChatFormatting.WHITE)
                + player.getName()
                + "\u00a7r" + " has popped " + "\u00a7("
                + (isFriend ? ChatFormatting.AQUA : ChatFormatting.WHITE)
                + ChatFormatting.BOLD + popCount + ChatFormatting.RESET + "\u00a7r"
                + (popCount == 1 ? " totem!" : " totems!"), id);
    }
}


