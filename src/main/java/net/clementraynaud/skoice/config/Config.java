/*
 * Copyright 2020, 2021, 2022 Clément "carlodrift" Raynaud, Lucas "Lucas_Cdry" Cadiry and contributors
 *
 * This file is part of Skoice.
 *
 * Skoice is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Skoice is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Skoice.  If not, see <https://www.gnu.org/licenses/>.
 */

package net.clementraynaud.skoice.config;

import net.clementraynaud.skoice.Skoice;
import net.clementraynaud.skoice.bot.Bot;
import net.dv8tion.jda.api.entities.Category;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.VoiceChannel;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.*;

public class Config {

    public static final String TOKEN_FIELD = "token";
    public static final String LANG_FIELD = "lang";
    public static final String LOBBY_ID_FIELD = "lobby-id";
    public static final String HORIZONTAL_RADIUS_FIELD = "horizontal-radius";
    public static final String VERTICAL_RADIUS_FIELD = "vertical-radius";
    public static final String ACTION_BAR_ALERT_FIELD = "action-bar-alert";
    public static final String CHANNEL_VISIBILITY_FIELD = "channel-visibility";
    public static final String LINK_MAP_FIELD = "link-map";
    public static final String TEMP_FIELD = "temp";
    public static final String TEMP_GUILD_ID_FIELD = Config.TEMP_FIELD + ".guild-id";
    public static final String TEMP_TEXT_CHANNEL_ID_FIELD = Config.TEMP_FIELD + ".text-channel-id";
    public static final String TEMP_MESSAGE_ID_FIELD = Config.TEMP_FIELD + ".message-id";

    private final Skoice plugin;
    private final FileConfiguration file;

    public Config(Skoice plugin, FileConfiguration file) {
        this.plugin = plugin;
        this.file = file;
    }

    public FileConfiguration getFile() {
        return this.file;
    }

    public void saveFile() {
        this.plugin.saveConfig();
    }

    public Map<String, String> getLinkMap() {
        Map<String, String> castedLinkMap = new HashMap<>();
        if (this.file.isSet(Config.LINK_MAP_FIELD)) {
            Map<String, Object> linkMap = new HashMap<>(this.file.getConfigurationSection(Config.LINK_MAP_FIELD).getValues(false));
            for (Map.Entry<String, Object> entry : linkMap.entrySet()) {
                castedLinkMap.put(entry.getKey(), entry.getValue().toString());
            }
        }
        return castedLinkMap;
    }

    public String getKeyFromValue(Map<String, String> map, String value) {
        for (Map.Entry<String, String> entry : map.entrySet()) {
            if (Objects.equals(value, entry.getValue())) {
                return entry.getKey();
            }
        }
        return null;
    }

    public Member getMember(UUID minecraftID) {
        String discordID = this.getLinkMap().get(minecraftID);
        Guild guild = this.getGuild();
        if (guild == null) {
            return null;
        }
        return discordID != null ? guild.getMemberById(discordID) : null;
    }

    public void setToken(String token) {
        byte[] tokenBytes = token.getBytes();
        for (int i = 0; i < tokenBytes.length; i++) {
            tokenBytes[i]++;
        }
        this.getFile().set(Config.TOKEN_FIELD, Base64.getEncoder().encodeToString(tokenBytes));
        this.saveFile();
    }

    public VoiceChannel getLobby() {
        if (Bot.getJda() == null) {
            return null;
        }
        String lobbyID = this.file.getString(Config.LOBBY_ID_FIELD);
        if (lobbyID == null) {
            return null;
        }
        VoiceChannel lobby = Bot.getJda().getVoiceChannelById(lobbyID);
        return lobby != null && lobby.getParent() != null ? lobby : null;
    }

    public Category getCategory() {
        if (Bot.getJda() == null) {
            return null;
        }
        VoiceChannel lobby = this.getLobby();
        return lobby != null ? lobby.getParent() : null;
    }

    public Guild getGuild() {
        VoiceChannel lobby = this.getLobby();
        return lobby != null ? lobby.getGuild() : null;
    }

    public int getHorizontalRadius() {
        return this.file.getInt(Config.HORIZONTAL_RADIUS_FIELD);
    }

    public int getVerticalRadius() {
        return this.file.getInt(Config.VERTICAL_RADIUS_FIELD);
    }

    public boolean getActionBarAlert() {
        return this.file.getBoolean(Config.ACTION_BAR_ALERT_FIELD);
    }

    public boolean getChannelVisibility() {
        return this.file.getBoolean(Config.CHANNEL_VISIBILITY_FIELD);
    }

    public void linkUser(String minecraftID, String discordID) {
        Map<String, String> linkMap = this.getLinkMap();
        linkMap.put(minecraftID, discordID);
        this.file.set(Config.LINK_MAP_FIELD, linkMap);
        this.saveFile();
    }

    public void unlinkUser(String minecraftID) {
        Map<String, String> linkMap = this.getLinkMap();
        linkMap.remove(minecraftID);
        this.file.set(Config.LINK_MAP_FIELD, linkMap);
        this.saveFile();
    }
}
