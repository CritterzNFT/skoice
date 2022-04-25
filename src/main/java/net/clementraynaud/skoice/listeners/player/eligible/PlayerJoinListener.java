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

package net.clementraynaud.skoice.listeners.player.eligible;

import net.clementraynaud.skoice.config.Config;
import net.clementraynaud.skoice.lang.LangFile;
import net.clementraynaud.skoice.system.EligiblePlayers;
import net.dv8tion.jda.api.entities.GuildVoiceState;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.VoiceChannel;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class PlayerJoinListener implements Listener {

    private final Config config;
    private final LangFile lang;

    public PlayerJoinListener(Config config, LangFile lang) {
        this.config = config;
        this.lang = lang;
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        new EligiblePlayers().add(player);
        Member member = this.config.getReader().getMember(player.getUniqueId());
        if (member != null) {
            GuildVoiceState voiceState = member.getVoiceState();
            if (voiceState != null) {
                VoiceChannel voiceChannel = voiceState.getChannel();
                if (voiceChannel != null && voiceChannel.equals(this.config.getReader().getLobby())) {
                    player.sendMessage(this.lang.getMessage("minecraft.chat.player.connected-to-proximity-voice-chat"));
                }
            }
        }
    }
}
