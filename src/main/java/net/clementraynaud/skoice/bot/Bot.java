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

package net.clementraynaud.skoice.bot;

import net.clementraynaud.skoice.Skoice;
import net.clementraynaud.skoice.commands.ConfigureCommand;
import net.clementraynaud.skoice.commands.InviteCommand;
import net.clementraynaud.skoice.config.Config;
import net.clementraynaud.skoice.config.ConfigField;
import net.clementraynaud.skoice.listeners.interaction.ButtonClickListener;
import net.clementraynaud.skoice.menus.MenuEmoji;
import net.clementraynaud.skoice.menus.Response;
import net.clementraynaud.skoice.listeners.interaction.SelectMenuListener;
import net.clementraynaud.skoice.listeners.ReconnectedListener;
import net.clementraynaud.skoice.listeners.channel.voice.lobby.VoiceChannelDeleteListener;
import net.clementraynaud.skoice.listeners.channel.voice.lobby.update.VoiceChannelUpdateParentListener;
import net.clementraynaud.skoice.listeners.guild.GuildJoinListener;
import net.clementraynaud.skoice.listeners.guild.GuildLeaveListener;
import net.clementraynaud.skoice.listeners.message.guild.GuildMessageDeleteListener;
import net.clementraynaud.skoice.listeners.message.guild.GuildMessageReceivedListener;
import net.clementraynaud.skoice.lang.LoggerLang;
import net.clementraynaud.skoice.lang.DiscordLang;
import net.clementraynaud.skoice.lang.MinecraftLang;
import net.clementraynaud.skoice.commands.LinkCommand;
import net.clementraynaud.skoice.commands.UnlinkCommand;
import net.clementraynaud.skoice.listeners.message.priv.PrivateMessageReceivedListener;
import net.clementraynaud.skoice.tasks.UpdateNetworksTask;
import net.clementraynaud.skoice.system.Network;
import net.clementraynaud.skoice.util.MapUtil;
import net.clementraynaud.skoice.util.MessageUtil;
import net.clementraynaud.skoice.util.UpdateUtil;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.exceptions.ErrorHandler;
import net.dv8tion.jda.api.exceptions.ErrorResponseException;
import net.dv8tion.jda.api.requests.ErrorResponse;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.MemberCachePolicy;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;

import javax.security.auth.login.LoginException;
import java.awt.*;
import java.io.IOException;
import java.net.URL;
import java.util.*;

public class Bot {

    private static final int TICKS_BETWEEN_VERSION_CHECKING = 720000;

    private JDA jda;
    private boolean isReady;
    private boolean isOnMultipleGuilds;

    private final Skoice plugin;
    private final Config config;

    public Bot(Skoice plugin, Config config) {
        this.plugin = plugin;
        this.config = config;
    }

    public JDA getJda() {
        return this.jda;
    }

    public void setReady(boolean ready) {
        this.isReady = ready;
    }

    public boolean isReady() {
        return this.isReady;
    }

    public boolean isOnMultipleGuilds() {
        return this.isOnMultipleGuilds;
    }

    public void connect(CommandSender sender) {
        if (this.config.getFile().contains(ConfigField.TOKEN.get())) {
            byte[] base64TokenBytes = Base64.getDecoder().decode(this.config.getFile().getString(ConfigField.TOKEN.get()));
            for (int i = 0; i < base64TokenBytes.length; i++) {
                base64TokenBytes[i]--;
            }
            try {
                this.jda = JDABuilder.createDefault(new String(base64TokenBytes))
                        .enableIntents(GatewayIntent.GUILD_MEMBERS)
                        .setMemberCachePolicy(MemberCachePolicy.ALL)
                        .build()
                        .awaitReady();
                this.plugin.getLogger().info(LoggerLang.BOT_CONNECTED_INFO.toString());
            } catch (LoginException e) {
                if (sender == null) {
                    this.plugin.getLogger().severe(LoggerLang.BOT_COULD_NOT_CONNECT_ERROR.toString());
                } else {
                    sender.sendMessage(MinecraftLang.BOT_COULD_NOT_CONNECT.toString());
                    this.config.getFile().set(ConfigField.TOKEN.get(), null);
                    this.config.saveFile();
                }
            } catch (IllegalStateException e) {

            } catch (ErrorResponseException e) {
                if (sender == null) {
                    this.plugin.getLogger().severe(LoggerLang.DISCORD_API_TIMED_OUT_ERROR.toString());
                } else {
                    try {
                        TextComponent discordStatusPage = new TextComponent("§bpage");
                        MessageUtil.setHoverEvent(discordStatusPage, "§8☀ §bOpen in web browser: §7https://discordstatus.com");
                        discordStatusPage.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, "https://discordstatus.com"));
                        sender.spigot().sendMessage(new ComponentBuilder("§dSkoice §8• §7Discord seems to §cbe experiencing an outage§7. Find more information on this ")
                                .append(discordStatusPage)
                                .append("§7.").event((HoverEvent) null).create());
                    } catch (NoSuchMethodError e2) {
                        sender.sendMessage(MinecraftLang.DISCORD_API_TIMED_OUT_LINK.toString());
                    }
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public void setup(boolean startup, CommandSender sender) {
        this.setDefaultAvatar();
        new Response(this.plugin, this.config, this).deleteMessage();
        this.updateGuildUniquenessStatus();
        this.checkForValidLobby();
        this.checkForUnlinkedUsersInLobby();
        this.jda.getGuilds().forEach(new Commands(this.plugin, this)::register);
        this.jda.addEventListener(Arrays.asList(new ReconnectedListener(this.plugin, this.config, this), new GuildJoinListener(this.plugin, this),
                new GuildLeaveListener(this.plugin, this), new PrivateMessageReceivedListener(),
                new GuildMessageReceivedListener(this.plugin, this.config, this), new GuildMessageDeleteListener(this.config),
                new VoiceChannelDeleteListener(this.plugin, this.config, this), new VoiceChannelUpdateParentListener(this.plugin, this.config, this),
                new ConfigureCommand(this.plugin, this.config, this), new InviteCommand(), new LinkCommand(this.config, this), new UnlinkCommand(this.config),
                new ButtonClickListener(this.plugin, this.config, this), new SelectMenuListener(this.plugin, this.config, this)));
        Bukkit.getScheduler().runTaskLater(this.plugin, () ->
                        Bukkit.getScheduler().runTaskTimerAsynchronously(
                                this.plugin,
                                new UpdateNetworksTask(this.config)::run,
                                0,
                                10
                        ),
                0
        );
        Bukkit.getScheduler().runTaskLater(this.plugin, () ->
                        Bukkit.getScheduler().runTaskTimerAsynchronously(
                                this.plugin,
                                new UpdateUtil(this.plugin, Skoice.RESSOURCE_ID, LoggerLang.OUTDATED_VERSION_WARNING.toString())::checkVersion,
                                Bot.TICKS_BETWEEN_VERSION_CHECKING,
                                Bot.TICKS_BETWEEN_VERSION_CHECKING
                        ),
                0
        );
        this.retrieveNetworks();
        this.plugin.updateConfigurationStatus(startup);
        if (sender != null && this.jda != null) {
            if (this.isReady) {
                sender.sendMessage(MinecraftLang.BOT_CONNECTED.toString());
            } else {
                sender.sendMessage(MinecraftLang.BOT_CONNECTED_INCOMPLETE_CONFIGURATION_DISCORD.toString());
            }
        }
    }

    private void setDefaultAvatar() {
        if (this.jda.getSelfUser().getDefaultAvatarUrl().equals(this.jda.getSelfUser().getEffectiveAvatarUrl())) {
            try {
                this.jda.getSelfUser().getManager()
                        .setAvatar(Icon.from(new URL("https://www.spigotmc.org/data/resource_icons/82/82861.jpg?1597701409")
                                .openStream())).queue();
            } catch (IOException ignored) {
            }
        }
    }

    public void updateGuildUniquenessStatus() {
        this.isOnMultipleGuilds = this.jda.getGuilds().size() > 1;
    }

    public void checkForValidLobby() {
        if (this.config.getReader().getLobby() == null && this.config.getFile().contains(ConfigField.LOBBY_ID.get())) {
            this.config.getFile().set(ConfigField.LOBBY_ID.get(), null);
            this.config.saveFile();
        }
    }

    public void checkForUnlinkedUsersInLobby() {
        VoiceChannel lobby = this.config.getReader().getLobby();
        if (lobby != null) {
            for (Member member : lobby.getMembers()) {
                String minecraftID = new MapUtil().getKeyFromValue(this.config.getReader().getLinkMap(), member.getId());
                if (minecraftID == null) {
                    EmbedBuilder embed = new EmbedBuilder().setTitle(MenuEmoji.LINK + DiscordLang.LINKING_PROCESS_EMBED_TITLE.toString())
                            .setColor(Color.RED);
                    Guild guild = this.config.getReader().getGuild();
                    if (guild != null) {
                        embed.addField(MenuEmoji.WARNING + DiscordLang.ACCOUNT_NOT_LINKED_FIELD_TITLE.toString(),
                                String.format(DiscordLang.ACCOUNT_NOT_LINKED_FIELD_ALTERNATIVE_DESCRIPTION.toString(), guild.getName()), false);
                    } else {
                        embed.addField(MenuEmoji.WARNING + DiscordLang.ACCOUNT_NOT_LINKED_FIELD_TITLE.toString(),
                                DiscordLang.ACCOUNT_NOT_LINKED_FIELD_GENERIC_ALTERNATIVE_DESCRIPTION.toString(), false);
                    }
                    member.getUser().openPrivateChannel().complete()
                            .sendMessageEmbeds(embed.build())
                            .queue(null, new ErrorHandler().ignore(ErrorResponse.CANNOT_SEND_TO_USER));
                }
            }
        }
    }

    private void retrieveNetworks() {
        Category category = this.config.getReader().getCategory();
        if (category != null) {
            category.getVoiceChannels().stream()
                    .filter(channel -> {
                        try {
                            UUID.fromString(channel.getName());
                            return true;
                        } catch (IllegalArgumentException e) {
                            return false;
                        }
                    })
                    .forEach(channel -> Network.networks.add(new Network(this.config, channel.getId())));
        }
    }
}
