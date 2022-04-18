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

package net.clementraynaud.skoice.listeners.interaction;

import net.clementraynaud.skoice.Skoice;
import net.clementraynaud.skoice.bot.Bot;
import net.clementraynaud.skoice.bot.Commands;
import net.clementraynaud.skoice.config.Config;
import net.clementraynaud.skoice.menus.ErrorEmbeds;
import net.clementraynaud.skoice.menus.Menu;
import net.clementraynaud.skoice.lang.DiscordLang;
import net.clementraynaud.skoice.lang.LoggerLang;
import net.clementraynaud.skoice.menus.Response;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.VoiceChannel;
import net.dv8tion.jda.api.events.interaction.SelectionMenuEvent;
import net.dv8tion.jda.api.exceptions.ErrorResponseException;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.components.selections.SelectOption;

public class SelectMenuListener extends ListenerAdapter {

    private final Skoice plugin;
    private final Config config;
    private final Bot bot;

    public SelectMenuListener(Skoice plugin, Config config, Bot bot) {
        this.plugin = plugin;
        this.config = config;
        this.bot = bot;
    }

    @Override
    public void onSelectionMenu(SelectionMenuEvent event) {
        Member member = event.getMember();
        if (member != null && member.hasPermission(Permission.MANAGE_SERVER)) {
            if (this.config.getFile().contains(Config.TEMP_MESSAGE_ID_FIELD)
                    && this.config.getFile().getString(Config.TEMP_MESSAGE_ID_FIELD).equals(event.getMessageId())
                    && event.getSelectedOptions() != null) {
                String componentID = event.getComponentId();
                switch (componentID) {
                    case "SERVER_SELECTION":
                        if (this.bot.getJda().getGuildById(event.getSelectedOptions().get(0).getValue()) != null) {
                            for (SelectOption server : event.getComponent().getOptions()) {
                                if (!event.getGuild().getId().equals(server.getValue())
                                        && this.bot.getJda().getGuilds().contains(this.bot.getJda().getGuildById(server.getValue()))) {
                                    try {
                                        this.bot.getJda().getGuildById(server.getValue()).leave()
                                                .queue(success -> event.editMessage(new Response(this.plugin, this.config, this.bot).getMessage()).queue());
                                    } catch (ErrorResponseException ignored) {
                                    }
                                }
                            }
                        }
                        break;
                    case "LANGUAGE_SELECTION":
                        this.config.getFile().set(Config.LANG_FIELD, event.getSelectedOptions().get(0).getValue());
                        this.config.saveFile();
                        this.plugin.updateConfigurationStatus(false);
                        new Commands(this.plugin, this.bot).register(event.getGuild());
                        event.editMessage(new Response(this.plugin, this.config, this.bot).getMessage()).queue();
                        break;
                    case "LOBBY_SELECTION":
                        Guild guild = event.getGuild();
                        if (guild != null) {
                            if ("GENERATE" .equals(event.getSelectedOptions().get(0).getValue())) {
                                String categoryID = guild.createCategory(DiscordLang.DEFAULT_CATEGORY_NAME.toString())
                                        .complete().getId();
                                String lobbyID = guild.createVoiceChannel(DiscordLang.DEFAULT_LOBBY_NAME.toString(), event.getGuild().getCategoryById(categoryID))
                                        .complete().getId();
                                this.config.getFile().set(Config.LOBBY_ID_FIELD, lobbyID);
                                this.config.saveFile();
                                this.plugin.updateConfigurationStatus(false);
                            } else if ("REFRESH" .equals(event.getSelectedOptions().get(0).getValue())) {
                                event.editMessage(new Response(this.plugin, this.config, this.bot).getMessage()).queue();
                            } else {
                                VoiceChannel lobby = guild.getVoiceChannelById(event.getSelectedOptions().get(0).getValue());
                                if (lobby != null && lobby.getParent() != null) {
                                    this.config.getFile().set(Config.LOBBY_ID_FIELD, event.getSelectedOptions().get(0).getValue());
                                    this.config.saveFile();
                                    this.plugin.updateConfigurationStatus(false);
                                }
                            }
                        }
                        event.editMessage(new Response(this.plugin, this.config, this.bot).getMessage()).queue();
                        break;
                    case "MODE_SELECTION":
                        if ("VANILLA_MODE" .equals(event.getSelectedOptions().get(0).getValue())) {
                            this.config.getFile().set(Config.HORIZONTAL_RADIUS_FIELD, 80);
                            this.config.getFile().set(Config.VERTICAL_RADIUS_FIELD, 40);
                            this.config.saveFile();
                            this.plugin.updateConfigurationStatus(false);
                            event.editMessage(new Response(this.plugin, this.config, this.bot).getMessage()).queue();
                        } else if ("MINIGAME_MODE" .equals(event.getSelectedOptions().get(0).getValue())) {
                            this.config.getFile().set(Config.HORIZONTAL_RADIUS_FIELD, 40);
                            this.config.getFile().set(Config.VERTICAL_RADIUS_FIELD, 20);
                            this.config.saveFile();
                            this.plugin.updateConfigurationStatus(false);
                            event.editMessage(new Response(this.plugin, this.config, this.bot).getMessage()).queue();
                        } else if ("CUSTOMIZE" .equals(event.getSelectedOptions().get(0).getValue())) {
                            Menu.customizeRadius = true;
                            event.editMessage(Menu.MODE.getMessage()).queue();
                        }
                        break;
                    case "ACTION_BAR_ALERT":
                        if ("true" .equals(event.getSelectedOptions().get(0).getValue())) {
                            this.config.getFile().set(Config.ACTION_BAR_ALERT_FIELD, true);
                        } else if ("false" .equals(event.getSelectedOptions().get(0).getValue())) {
                            this.config.getFile().set(Config.ACTION_BAR_ALERT_FIELD, false);
                        }
                        this.config.saveFile();
                        event.editMessage(new Response(this.plugin, this.config, this.bot).getMessage()).queue();
                        break;
                    case "CHANNEL_VISIBILITY":
                        if ("true" .equals(event.getSelectedOptions().get(0).getValue())) {
                            this.config.getFile().set(Config.CHANNEL_VISIBILITY_FIELD, true);
                        } else if ("false" .equals(event.getSelectedOptions().get(0).getValue())) {
                            this.config.getFile().set(Config.CHANNEL_VISIBILITY_FIELD, false);
                        }
                        this.config.saveFile();
                        event.editMessage(new Response(this.plugin, this.config, this.bot).getMessage()).queue();
                        break;
                    default:
                        throw new IllegalStateException(String.format(LoggerLang.UNEXPECTED_VALUE.toString(), componentID));
                }
            }
        } else {
            event.replyEmbeds(ErrorEmbeds.getAccessDeniedEmbed()).setEphemeral(true).queue();
        }
    }
}
