package me.mioclient.api.util.entity;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.mojang.util.UUIDTypeAdapter;
import me.mioclient.api.util.Wrapper;
import me.mioclient.mod.commands.Command;
import net.minecraft.client.network.NetworkPlayerInfo;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Objects;
import java.util.Scanner;
import java.util.UUID;

public class ProfileUtil implements Wrapper {

    //Getters

    public static String getStringFromStream(InputStream is) {
        Scanner s = (new Scanner(is)).useDelimiter("\\A");

        return s.hasNext() ? s.next() : "/";
    }

    public static UUID getUUIDFromName(String name) {
        try {
            UUIDFinder process = new UUIDFinder(name);
            Thread thread = new Thread(process);
            thread.start();
            thread.join();

            return process.getUUID();

        } catch (Exception e) {
            return null;
        }
    }

    public static String requestIDs(String data) {
        try {
            String query = "https://api.mojang.com/profiles/minecraft";
            URL url = new URL(query);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setConnectTimeout(5000);
            conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
            conn.setDoOutput(true);
            conn.setDoInput(true);
            conn.setRequestMethod("POST");
            OutputStream os = conn.getOutputStream();
            os.write(data.getBytes(StandardCharsets.UTF_8));
            os.close();

            InputStream stream = new BufferedInputStream(conn.getInputStream());
            String retval = getStringFromStream(stream);

            stream.close();
            conn.disconnect();

            return retval;

        } catch (Exception e) {
            return null;
        }
    }

    //Inner classes

    public static class UUIDFinder implements Runnable {

        private final String name;
        private volatile UUID uuid;

        public UUIDFinder(String name) {
            this.name = name;
        }

        public void run() {
            NetworkPlayerInfo profile;

            try {
                ArrayList<NetworkPlayerInfo> infoMap = new ArrayList<>(Objects.requireNonNull(mc.getConnection()).getPlayerInfoMap());
                profile = infoMap.stream().filter(networkPlayerInfo -> networkPlayerInfo.getGameProfile().getName().equalsIgnoreCase(name)).findFirst().orElse(null);

                assert profile != null;

                uuid = profile.getGameProfile().getId();

            } catch (Exception e) {
                profile = null;
            }

            if (profile == null) {
                Command.sendMessage("Player isn't online. Looking up UUID..");
                String s = requestIDs("[\"" + name + "\"]");

                if (s == null || s.isEmpty()) {
                    Command.sendMessage("Couldn't find player ID. Are you connected to the internet? (0)");

                } else {
                    JsonElement element = (new JsonParser()).parse(s);

                    if (element.getAsJsonArray().size() == 0) {
                        Command.sendMessage("Couldn't find player ID. (1)");

                    } else {

                        try {
                            String id = element.getAsJsonArray().get(0).getAsJsonObject().get("id").getAsString();
                            uuid = UUIDTypeAdapter.fromString(id);

                        } catch (Exception e) {
                            e.printStackTrace();
                            Command.sendMessage("Couldn't find player ID. (2)");
                        }
                    }
                }
            }
        }

        public UUID getUUID() {
            return uuid;
        }

        public String getName() {
            return name;
        }
    }
}
