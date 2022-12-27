package me.mioclient.mod.commands;

import com.mojang.realmsclient.gui.ChatFormatting;
import me.mioclient.api.managers.Managers;
import me.mioclient.mod.Mod;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentBase;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public abstract class Command extends Mod {

    protected String name;
    protected String[] commands;

    public Command(String name) {
        super(name);
        this.name = name;
        commands = new String[]{""};
    }

    public Command(String name, String[] commands) {
        super(name);
        this.name = name;
        this.commands = commands;
    }

    public static void sendMessage(String message) {
        sendSilentMessage(Managers.TEXT.getPrefix() + ChatFormatting.GRAY + message);
    }

    public static void sendSilentMessage(String message) {
        if (nullCheck()) {
            return;
        }
        Command.mc.player.sendMessage(new ChatMessage(message));
    }

    public static String getCommandPrefix() {
        return Managers.COMMANDS.getCommandPrefix();
    }

    public static void sendMessageWithID(String message, int id) {
        if (!nullCheck()) {
            mc.ingameGUI.getChatGUI().printChatMessageWithOptionalDeletion(new ChatMessage(Managers.TEXT.getPrefix() + ChatFormatting.GRAY + message), id);
        }
    }

    public abstract void execute(String[] var1);

    public String complete(String str) {
        if (name.toLowerCase().startsWith(str)) {
            return name;
        }
        for (String command : commands) {
            if (command.toLowerCase().startsWith(str)) {
                return command;
            }
        }
        return null;
    }

    @Override
    public String getName() {
        return name;
    }

    public String[] getCommands() {
        return commands;
    }

    public static class ChatMessage extends TextComponentBase {

        private final String text;

        public ChatMessage(String text) {
            Pattern pattern = Pattern.compile("&[0123456789abcdefrlosmk]");
            Matcher matcher = pattern.matcher(text);
            StringBuffer stringBuffer = new StringBuffer();
            while (matcher.find()) {
                String replacement = matcher.group().substring(1);
                matcher.appendReplacement(stringBuffer, replacement);
            }
            matcher.appendTail(stringBuffer);
            this.text = stringBuffer.toString();
        }

        public String getUnformattedComponentText() {
            return text;
        }

        public ITextComponent createCopy() {
            return null;
        }

        public ITextComponent shallowCopy() {
            return new ChatMessage(text);
        }
    }
}