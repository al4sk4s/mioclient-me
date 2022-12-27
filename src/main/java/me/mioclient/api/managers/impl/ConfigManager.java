package me.mioclient.api.managers.impl;

import com.google.gson.*;
import me.mioclient.Mio;
import me.mioclient.api.managers.Managers;
import me.mioclient.api.util.Wrapper;
import me.mioclient.mod.Mod;
import me.mioclient.mod.modules.Module;
import me.mioclient.mod.modules.settings.Bind;
import me.mioclient.mod.modules.settings.EnumConverter;
import me.mioclient.mod.modules.settings.Setting;

import java.awt.*;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.*;
import java.util.stream.Collectors;

public class ConfigManager implements Wrapper {

    public ArrayList<Mod> mods = new ArrayList<>();

    public String config = "hvhlegend/config/";

    public static void setValueFromJson(Mod mod, Setting setting, JsonElement element) {
        String str;
        switch (setting.getType()) {

            case "Boolean":
                setting.setValue(Boolean.valueOf(element.getAsBoolean()));
                return;

            case "Double":
                setting.setValue(Double.valueOf(element.getAsDouble()));
                return;

            case "Float":
                setting.setValue(Float.valueOf(element.getAsFloat()));
                return;

            case "Integer":
                setting.setValue(Integer.valueOf(element.getAsInt()));
                return;

            case "String":
                try {
                    str = element.getAsString();
                    setting.setValue(str.replace("_", " "));
                    
                } catch (Exception ignored) {
                    
                }
                return;

            case "Bind":
                try {
                    setting.setValue((new Bind.BindConverter()).doBackward(element));

                } catch (Exception ignored) {
                    
                }
                return;

            case "Enum":
                try {
                    EnumConverter converter = new EnumConverter(((Enum) setting.getValue()).getClass());
                    Enum value = converter.doBackward(element);
                    setting.setValue((value == null) ? setting.getDefaultValue() : value);

                } catch (Exception ignored) {
                    
                }
                return;

            case "Color": {
                try {

                    if (setting.hasBoolean) {
                        setting.injectBoolean(element.getAsBoolean());
                    }

                    try {
                        setting.setValue(new Color(element.getAsInt(), true));

                    } catch (Exception ignored) {
                        
                    }

                } catch (Exception ignored) {
                    
                }
                return;
            }
        }
        Mio.LOGGER.error("Unknown Setting type for: " + mod.getName() + " : " + setting.getName());
    }

    private static void loadFile(JsonObject input, Mod mod) {
        for (Map.Entry<String, JsonElement> entry : input.entrySet()) {
            String settingName = entry.getKey();
            JsonElement element = entry.getValue();

            if (mod instanceof FriendManager) {

                try {
                    Managers.FRIENDS.addFriend(new FriendManager.Friend(element.getAsString(), UUID.fromString(settingName)));

                } catch (Exception e) {
                    e.printStackTrace();
                }
                continue;
            }
            boolean settingFound = false;

            for (Setting setting : mod.getSettings()) {

                if (settingName.equals(setting.getName())) {

                    try {
                        setValueFromJson(mod, setting, element);

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    settingFound = true;
                }

                if (settingName.equals("should" + setting.getName())) {
                    try {
                        setValueFromJson(mod, setting, element);

                    } catch (Exception ignored) {

                    }
                    settingFound = true;
                }
            }
            if (settingFound) ;
        }
    }

    public void loadConfig(String name) {
        List<File> files = Arrays.stream(Objects.requireNonNull(new File("hvhlegend").listFiles())).filter(File::isDirectory).collect(Collectors.toList());

        if (files.contains(new File("hvhlegend/" + name + "/"))) {
            config = "hvhlegend/" + name + "/";

        } else {
            config = "hvhlegend/config/";
        }

        Managers.FRIENDS.onLoad();

        for (Mod mod : mods) {
            try {
                loadSettings(mod);

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        saveCurrentConfig();
    }

    public boolean configExists(String name) {
        List<File> files = Arrays.stream(Objects.requireNonNull(new File("hvhlegend").listFiles())).filter(File::isDirectory).collect(Collectors.toList());
        return files.contains(new File("hvhlegend/" + name + "/"));
    }

    public void saveConfig(String name) {
        config = "hvhlegend/" + name + "/";
        File path = new File(config);

        if (!path.exists()) path.mkdir();

        Managers.FRIENDS.saveFriends();

        for (Mod mod : mods) {
            try {
                saveSettings(mod);

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        saveCurrentConfig();
    }

    public void saveCurrentConfig() {
        File currentConfig = new File("hvhlegend/currentconfig.txt");

        try {
            if (currentConfig.exists()) {
                FileWriter writer = new FileWriter(currentConfig);
                String tempConfig = config.replaceAll("/", "");
                writer.write(tempConfig.replaceAll("hvhlegend", ""));
                writer.close();

            } else {
                currentConfig.createNewFile();
                FileWriter writer = new FileWriter(currentConfig);
                String tempConfig = config.replaceAll("/", "");
                writer.write(tempConfig.replaceAll("hvhlegend", ""));
                writer.close();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String loadCurrentConfig() {
        File currentConfig = new File("hvhlegend/currentconfig.txt");
        String name = "config";

        try {
            if (currentConfig.exists()) {
                Scanner reader = new Scanner(currentConfig);

                while (reader.hasNextLine())
                    name = reader.nextLine();
                reader.close();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return name;
    }

    public void saveSettings(Mod mod) throws IOException {
        JsonObject object = new JsonObject();
        File directory = new File(config + getDirectory(mod));

        if (!directory.exists()) directory.mkdir();

        String modName = config + getDirectory(mod) + mod.getName() + ".json";

        Path outputFile = Paths.get(modName);

        if (!Files.exists(outputFile)) Files.createFile(outputFile);

        Gson gson = (new GsonBuilder()).setPrettyPrinting().create();
        String json = gson.toJson(writeSettings(mod));

        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(Files.newOutputStream(outputFile)));

        writer.write(json);
        writer.close();
    }

    public void init() {
        mods.addAll(Managers.MODULES.modules);
        mods.add(Managers.FRIENDS);

        String name = loadCurrentConfig();

        loadConfig(name);

        Mio.LOGGER.info("Config loaded.");
    }

    private void loadSettings(Mod mod) throws IOException {
        String modName = config + getDirectory(mod) + mod.getName() + ".json";
        Path modPath = Paths.get(modName);

        if (!Files.exists(modPath)) return;

        loadPath(modPath, mod);
    }

    private void loadPath(Path path, Mod mod) throws IOException {
        InputStream stream = Files.newInputStream(path);

        try {
            loadFile((new JsonParser()).parse(new InputStreamReader(stream)).getAsJsonObject(), mod);

        } catch (IllegalStateException e) {
            Mio.LOGGER.error("Bad Config File for: " + mod.getName() + ". Resetting...");
            loadFile(new JsonObject(), mod);
        }
        stream.close();
    }

    public JsonObject writeSettings(Mod mod) {
        JsonObject object = new JsonObject();
        JsonParser jp = new JsonParser();

        for (Setting setting : mod.getSettings()) {

            if (setting.getValue() instanceof Color) {

                object.add(setting.getName(), jp.parse(String.valueOf(((Color)setting.getValue()).getRGB())));

                if (setting.hasBoolean) {
                    object.add("should" + setting.getName(), jp.parse(String.valueOf(setting.booleanValue)));
                }
                continue;
            }

            if (setting.isEnumSetting()) {
                EnumConverter converter = new EnumConverter(((Enum) setting.getValue()).getClass());
                object.add(setting.getName(), converter.doForward((Enum) setting.getValue()));

                continue;
            }

            if (setting.isStringSetting()) {
                String str = (String) setting.getValue();
                setting.setValue(str.replace(" ", "_"));
            }

            try {
                object.add(setting.getName(), jp.parse(setting.getValue().toString()));

            } catch (Exception ignored) {
                
            }
        }
        return object;
    }

    public String getDirectory(Mod mod) {
        String directory = "";
        if (mod instanceof Module)
            directory = directory + ((Module) mod).getCategory().getName() + "/";
        return directory;
    }
}