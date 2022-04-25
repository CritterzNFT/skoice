/*
 * Copyright 2020, 2021, 2022 Clément "carlodrift" Raynaud, Lucas "Lucas_Cdry" Cadiry and contributors
 * Copyright 2016, 2017, 2018, 2019, 2020, 2021 Austin "Scarsz" Shapiro
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

package net.clementraynaud.skoice.listeners.guild.voice;

import net.clementraynaud.skoice.config.Config;
import net.clementraynaud.skoice.lang.LangFile;
import net.clementraynaud.skoice.system.Network;
import net.clementraynaud.skoice.util.MapUtil;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceLeaveEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;

import java.util.UUID;

public class GuildVoiceLeaveListener extends ListenerAdapter {

    private final Config config;
    private final LangFile lang;

    public GuildVoiceLeaveListener(Config config, LangFile lang) {
        this.config = config;
        this.lang = lang;
    }

    @Override
    public void onGuildVoiceLeave(GuildVoiceLeaveEvent event) {
        if (event.getChannelLeft().getParent() == null
                || !event.getChannelLeft().getParent().equals(this.config.getReader().getCategory())) {
            return;
        }
        String minecraftID = new MapUtil().getKeyFromValue(this.config.getReader().getLinkMap(), event.getMember().getId());
        if (minecraftID == null) {
            return;
        }
        OfflinePlayer player = Bukkit.getOfflinePlayer(UUID.fromString(minecraftID));
        if (player.isOnline()) {
            Network.networks.stream()
                    .filter(network -> network.contains(player.getPlayer()))
                    .forEach(network -> network.remove(player.getPlayer()));
            if (event.getChannelLeft().equals(this.config.getReader().getLobby())
                    || Network.networks.stream().anyMatch(network -> network.getChannel().equals(event.getChannelLeft()))) {
                player.getPlayer().sendMessage(this.lang.getMessage("minecraft.chat.player.disconnected-from-proximity-voice-chat"));
            }
        }
    }
}
