package me.mioclient.api.managers.impl;

import me.mioclient.api.util.entity.ProfileUtil;
import me.mioclient.mod.Mod;
import me.mioclient.mod.modules.settings.Setting;
import net.minecraft.entity.player.EntityPlayer;

import java.util.*;

public class FriendManager extends Mod {

    private List<Friend> friends = new ArrayList<>();

    public FriendManager() {
        super("Friends");
    }

    public boolean isCool(String name) {

        List<String> coolList = Arrays.asList(
                "asphyxia1337",
                "Olype",
                "AsphyxiasKitty",
                "rootbeerguy1212",
                "FemboyPride",
                "BibleThot",
                "z75",
                "Megyn",
                "antikurdish",
                "D3R6",
                "tr011",
                "FourSixFive",
                "ThePeaceKeepers",
                "solar1z");

        return coolList.contains(name);
    }

    public boolean isFriend(String name) {
        cleanFriends();
        return friends.stream().anyMatch(friend -> friend.username.equalsIgnoreCase(name));
    }

    public boolean isFriend(EntityPlayer player) {
        return isFriend(player.getName());
    }


    public void addFriend(String name) {
        Friend friend = getFriendByName(name);
        if (friend != null) {
            friends.add(friend);
        }
        cleanFriends();
    }

    public void removeFriend(String name) {
        cleanFriends();
        for (Friend friend : friends) {
            if (!friend.getUsername().equalsIgnoreCase(name)) continue;
            friends.remove(friend);
            break;
        }
    }

    public void onLoad() {
        friends = new ArrayList<>();
        resetSettings();
    }

    public void saveFriends() {
        resetSettings();
        cleanFriends();
        for (Friend friend : friends) {
            add(new Setting<>(friend.getUuid().toString(), friend.getUsername()));
        }
    }

    public void cleanFriends() {
        friends.stream().filter(Objects::nonNull).filter(friend -> friend.getUsername() != null);
    }

    public List<Friend> getFriends() {
        cleanFriends();
        return friends;
    }

    public Friend getFriendByName(String input) {
        UUID uuid = ProfileUtil.getUUIDFromName(input);
        if (uuid != null) {
            Friend friend = new Friend(input, uuid);
            return friend;
        }
        return null;
    }

    public void addFriend(Friend friend) {
        friends.add(friend);
    }

    public static class Friend {
        private final String username;
        private final UUID uuid;

        public Friend(String username, UUID uuid) {
            this.username = username;
            this.uuid = uuid;
        }

        public String getUsername() {
            return username;
        }

        public UUID getUuid() {
            return uuid;
        }
    }
}

