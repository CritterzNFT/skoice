package net.clementraynaud.skoice.lang;

import net.clementraynaud.skoice.Skoice;
import org.bukkit.ChatColor;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;

public class LangFile {

    private static final String CHAT_PREFIX = ChatColor.LIGHT_PURPLE + "Skoice " + ChatColor.DARK_GRAY + "• " + ChatColor.GRAY;

    private final FileConfiguration englishMessages = new YamlConfiguration();
    private final FileConfiguration messages = new YamlConfiguration();

    private final Skoice plugin;

    public LangFile(Skoice plugin) {
        this.plugin = plugin;
    }

    public void load(Lang lang) {
        File englishLangFile = new File(String.valueOf(this.plugin.getResource("lang" + File.separator + Lang.EN.name() + ".yml")));
        try {
            this.englishMessages.load(englishLangFile);
        } catch (IOException | InvalidConfigurationException ignored) {
        }
        if (lang != Lang.EN) {
            File langFile = new File(String.valueOf(this.plugin.getResource("lang" + File.separator + lang.name() + ".yml")));
            try {
                this.messages.load(langFile);
            } catch (IOException | InvalidConfigurationException ignored) {
            }
        }
    }

    public String getMessage(String path) {
        String message = this.messages.contains(path) ? this.messages.getString(path) : this.englishMessages.getString(path);
        if (path.startsWith("minecraft.chat.") && message != null) {
            return String.format(message, LangFile.CHAT_PREFIX);
        }
        return message;
    }

    public String getMessage(String path, String... args) {
        String message = this.messages.contains(path) ? this.messages.getString(path) : this.englishMessages.getString(path);
        if (message == null) {
            return null;
        }
        if (path.startsWith("minecraft.chat.")) {
            return String.format(message, LangFile.CHAT_PREFIX, Arrays.toString(args));
        }
        return String.format(message, (Object) args);
    }
}
