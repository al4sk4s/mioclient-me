package me.mioclient.mod.commands.impl;

import com.mojang.realmsclient.gui.ChatFormatting;
import me.mioclient.api.managers.Managers;
import me.mioclient.api.managers.impl.FriendManager;
import me.mioclient.mod.commands.Command;

public class FriendCommand
        extends Command {
    public FriendCommand() {
        super("friend", new String[]{"<add/del/name/clear>", "<name>"});
    }

    @Override
    public void execute(String[] commands) {
        if (commands.length == 1) {
            if (Managers.FRIENDS.getFriends().isEmpty()) {
                sendMessage("Friend list empty D:.");
            } else {
                String f = "Friends: ";
                for (FriendManager.Friend friend : Managers.FRIENDS.getFriends()) {
                    try {
                        f = f + friend.getUsername() + ", ";
                    } catch (Exception exception) {
                    }
                }
                sendMessage(f);
            }
            return;
        }
        if (commands.length == 2) {
            if ("reset".equals(commands[0])) {
                Managers.FRIENDS.onLoad();
                sendMessage("Friends got reset.");
                return;
            }
            sendMessage(commands[0] + (Managers.FRIENDS.isFriend(commands[0]) ? " is friended." : " isn't friended."));
            return;
        }
        if (commands.length >= 2) {
            switch (commands[0]) {
                case "add": {
                    Managers.FRIENDS.addFriend(commands[1]);
                    sendMessage(ChatFormatting.GREEN + commands[1] + " has been friended");
                    return;
                }
                case "del": {
                    Managers.FRIENDS.removeFriend(commands[1]);
                    sendMessage(ChatFormatting.RED + commands[1] + " has been unfriended");
                    return;
                }
            }
            sendMessage("Unknown Command, try friend add/del (name)");
        }
    }
}

