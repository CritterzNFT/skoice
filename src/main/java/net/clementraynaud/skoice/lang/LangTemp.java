package net.clementraynaud.skoice.lang;

import net.clementraynaud.skoice.Skoice;
import org.bukkit.ChatColor;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;

public class LangTemp {

    public static final String CHAT_PREFIX = ChatColor.LIGHT_PURPLE + "Skoice " + ChatColor.DARK_GRAY + "• " + ChatColor.GRAY;

    private final FileConfiguration englishMessages = new YamlConfiguration();
    private final FileConfiguration messages = new YamlConfiguration();

    private final Skoice plugin;

    public LangTemp(Skoice plugin) {
        this.plugin = plugin;
    }

    public void load(Lang lang) {
        File englishLangFile = new File(String.valueOf(this.plugin.getResource("lang" + File.separator + Lang.EN.name() + ".yml")));
        try {
            this.englishMessages.load(englishLangFile);
        } catch (IOException | InvalidConfigurationException ignored) {
        }
        if (lang != Lang.EN) {
            File langFile = new File(this.plugin.getResource(lang.name()) + ".yml");
            try {
                this.messages.load(langFile);
            } catch (IOException | InvalidConfigurationException ignored) {
            }
        }
    }

    public String getMessage(String path) {
        return this.messages.contains(path) ? this.messages.getString(path) : this.englishMessages.getString(path);
    }
}
