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

public enum ConfigField {
    TOKEN("token"),
    LANG("lang"),
    LOBBY_ID("lobby-id"),
    HORIZONTAL_RADIUS("horizontal-radius"),
    VERTICAL_RADIUS("vertical-radius"),
    ACTION_BAR_ALERT("action-bar-alert"),
    CHANNEL_VISIBILITY("channel-visibility"),
    LINK_MAP("link-map"),
    TEMP("temp"),
    TEMP_MESSAGE_ID(ConfigField.TEMP + ".message-id"),
    TEMP_TEXT_CHANNEL_ID(ConfigField.TEMP + ".text-channel-id"),
    TEMP_GUILD_ID(ConfigField.TEMP + ".guild-id");

    private final String field;

    ConfigField(String field) {
        this.field = field;
    }

    public String get() {
        return this.field;
    }
}
