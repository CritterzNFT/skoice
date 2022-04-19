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
import net.dv8tion.jda.api.exceptions.ErrorResponseException;

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

    private Config() {
    }

    public static Map<String, String> getLinkMap() {
        Map<String, String> castedLinkMap = new HashMap<>();
        if (Skoice.getPlugin().getConfig().isSet(Config.LINK_MAP_FIELD)) {
            Map<String, Object> linkMap = new HashMap<>(Skoice.getPlugin().getConfig().getConfigurationSection(Config.LINK_MAP_FIELD).getValues(false));
            for (Map.Entry<String, Object> entry : linkMap.entrySet()) {
                castedLinkMap.put(entry.getKey(), entry.getValue().toString());
            }
        }
        return castedLinkMap;
    }

    public static String getKeyFromValue(Map<String, String> map, String value) {
        for (Map.Entry<String, String> entry : map.entrySet()) {
            if (Objects.equals(value, entry.getValue())) {
                return entry.getKey();
            }
        }
        return null;
    }

    public static Member getMember(UUID minecraftID) {
        String discordID = Config.getLinkMap().get(minecraftID.toString());
        if (discordID == null) {
            return null;
        }
        Guild guild = Config.getGuild();
        if (guild == null) {
            return null;
        }
        Member member = null;
        try {
            member = guild.retrieveMemberById(discordID).complete();
        } catch (ErrorResponseException ignored) {
        }
        return member;
    }

    public static void linkUser(String minecraftID, String discordID) {
        Skoice.getPlugin().getConfig().set(Config.LINK_MAP_FIELD + "." + minecraftID, discordID);
        Skoice.getPlugin().saveConfig();
    }

    public static void unlinkUser(String minecraftID) {
        Skoice.getPlugin().getConfig().set(Config.LINK_MAP_FIELD + "." + minecraftID, null);
        Skoice.getPlugin().saveConfig();
    }

    public static void setToken(String token) {
        byte[] tokenBytes = token.getBytes();
        for (int i = 0; i < tokenBytes.length; i++) {
            tokenBytes[i]++;
        }
        Skoice.getPlugin().getConfig().set(Config.TOKEN_FIELD, Base64.getEncoder().encodeToString(tokenBytes));
        Skoice.getPlugin().saveConfig();
    }

    public static VoiceChannel getLobby() {
        if (Bot.getJda() == null) {
            return null;
        }
        String lobbyID = Skoice.getPlugin().getConfig().getString(Config.LOBBY_ID_FIELD);
        if (lobbyID == null) {
            return null;
        }
        VoiceChannel lobby = Bot.getJda().getVoiceChannelById(lobbyID);
        if (lobby == null) {
            return null;
        }
        if (lobby.getParent() == null) {
            return null;
        }
        return lobby;
    }

    public static Category getCategory() {
        if (Bot.getJda() == null) {
            return null;
        }
        VoiceChannel lobby = Config.getLobby();
        if (lobby == null) {
            return null;
        }
        return lobby.getParent();
    }

    public static Guild getGuild() {
        VoiceChannel lobby = Config.getLobby();
        if (lobby == null) {
            return null;
        }
        return lobby.getGuild();
    }

    public static int getHorizontalRadius() {
        return Skoice.getPlugin().getConfig().getInt(Config.HORIZONTAL_RADIUS_FIELD);
    }

    public static int getVerticalRadius() {
        return Skoice.getPlugin().getConfig().getInt(Config.VERTICAL_RADIUS_FIELD);
    }

    public static boolean getActionBarAlert() {
        return Skoice.getPlugin().getConfig().getBoolean(Config.ACTION_BAR_ALERT_FIELD);
    }

    public static boolean getChannelVisibility() {
        return Skoice.getPlugin().getConfig().getBoolean(Config.CHANNEL_VISIBILITY_FIELD);
    }
}
