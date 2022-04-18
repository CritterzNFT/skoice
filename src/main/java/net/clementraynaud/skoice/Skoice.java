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

package net.clementraynaud.skoice;

import net.clementraynaud.skoice.bot.Bot;
import net.clementraynaud.skoice.commands.skoice.SkoiceCommand;
import net.clementraynaud.skoice.config.Config;
import net.clementraynaud.skoice.config.OutdatedConfig;
import net.clementraynaud.skoice.lang.DiscordLang;
import net.clementraynaud.skoice.lang.LoggerLang;
import net.clementraynaud.skoice.listeners.channel.voice.network.VoiceChannelDeleteListener;
import net.clementraynaud.skoice.listeners.guild.voice.GuildVoiceJoinListener;
import net.clementraynaud.skoice.listeners.guild.voice.GuildVoiceLeaveListener;
import net.clementraynaud.skoice.listeners.guild.voice.GuildVoiceMoveListener;
import net.clementraynaud.skoice.listeners.player.eligible.PlayerJoinListener;
import net.clementraynaud.skoice.listeners.player.eligible.PlayerMoveListener;
import net.clementraynaud.skoice.listeners.player.eligible.PlayerQuitListener;
import net.clementraynaud.skoice.listeners.player.eligible.PlayerTeleportListener;
import net.clementraynaud.skoice.menus.Menu;
import net.clementraynaud.skoice.menus.MenuEmoji;
import net.clementraynaud.skoice.menus.Response;
import net.clementraynaud.skoice.tasks.InterruptSystemTask;
import net.clementraynaud.skoice.util.UpdateUtil;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.exceptions.ErrorHandler;
import net.dv8tion.jda.api.requests.ErrorResponse;
import org.bstats.bukkit.Metrics;
import org.bukkit.Bukkit;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.java.JavaPlugin;

import java.awt.*;
import java.util.Objects;

public class Skoice extends JavaPlugin {

    private static final int SERVICE_ID = 11380;
    public static final int RESSOURCE_ID = 82861;

    protected Skoice plugin;
    protected Config config;
    protected Bot bot;

    private boolean isTokenSet;
    private boolean isBotReady;
    private boolean isGuildUnique;

    public Skoice getPlugin() {
        return this.plugin;
    }

    public boolean isTokenSet() {
        return this.isTokenSet;
    }

    public void setTokenBoolean(boolean isTokenSet) {
        this.isTokenSet = isTokenSet;
    }

    public boolean isBotReady() {
        return this.isBotReady;
    }

    public boolean isGuildUnique() {
        return this.isGuildUnique;
    }

    public void setGuildUnique(boolean guildUnique) {
        this.isGuildUnique = guildUnique;
    }

    @Override
    public void onEnable() {
        new Metrics(this, Skoice.SERVICE_ID);
        this.getLogger().info(LoggerLang.PLUGIN_ENABLED_INFO.toString());
        this.plugin = this;
        this.initializeConfig();
        new OutdatedConfig(this, this.config).update();
        this.isTokenSet = this.config.getFile().contains(Config.TOKEN_FIELD);
        this.initializeBot();
        this.initializeSkoiceCommand();
        new UpdateUtil(this, Skoice.RESSOURCE_ID, LoggerLang.OUTDATED_VERSION_WARNING.toString()).checkVersion();
    }

    @Override
    public void onDisable() {
        if (this.bot.getJda() != null) {
            new InterruptSystemTask(this.config).run();
            this.bot.getJda().shutdown();
        }
        this.getLogger().info(LoggerLang.PLUGIN_DISABLED_INFO.toString());
    }

    private void initializeConfig() {
        this.config = new Config(this, this.getConfig(), this.bot);
        this.config.getFile().options().copyDefaults(true);
        this.config.saveFile();
    }

    private void initializeBot() {
        this.bot = new Bot(this, this.config);
        this.bot.connect(true, null);
    }

    private void initializeSkoiceCommand() {
        SkoiceCommand skoiceCommand = new SkoiceCommand();
        this.getCommand("skoice").setExecutor(skoiceCommand);
        this.getCommand("skoice").setTabCompleter(skoiceCommand);
    }

    public void updateConfigurationStatus(boolean startup) {
        boolean wasBotReady = this.isBotReady;
        if (!this.config.getFile().contains(Config.TOKEN_FIELD)) {
            this.isTokenSet = false;
            this.isBotReady = false;
            this.getLogger().warning(LoggerLang.NO_TOKEN_WARNING.toString());
        } else if (this.bot.getJda() == null) {
            this.isBotReady = false;
        } else if (!this.isGuildUnique()) {
            this.isBotReady = false;
            this.getLogger().warning(LoggerLang.MULTIPLE_GUILDS_WARNING.toString());
        } else if (!this.config.getFile().contains(Config.LOBBY_ID_FIELD)) {
            this.isBotReady = false;
            this.getLogger().warning(LoggerLang.NO_LOBBY_ID_WARNING.toString());
        } else if (!this.config.getFile().contains(Config.HORIZONTAL_RADIUS_FIELD)
                || !this.config.getFile().contains(Config.VERTICAL_RADIUS_FIELD)) {
            this.isBotReady = false;
            this.getLogger().warning(LoggerLang.NO_RADIUS_WARNING.toString());
        } else {
            this.isBotReady = true;
        }
        this.updateActivity();
        this.updateListeners(startup, wasBotReady);
    }

    private void updateActivity() {
        if (this.bot.getJda() != null) {
            Activity activity = this.bot.getJda().getPresence().getActivity();
            if (this.isBotReady && !Objects.equals(activity, Activity.listening("/link"))) {
                this.bot.getJda().getPresence().setActivity(Activity.listening("/link"));
            } else if (!this.isBotReady && !Objects.equals(activity, Activity.listening("/configure"))) {
                this.bot.getJda().getPresence().setActivity(Activity.listening("/configure"));
            }
        }
    }

    private void updateListeners(boolean startup, boolean wasBotReady) {
        if (startup) {
            if (this.isBotReady) {
                this.registerEligiblePlayerListeners();
                this.bot.getJda().addEventListener(new GuildVoiceJoinListener(this.config), new GuildVoiceLeaveListener(this.config),
                        new GuildVoiceMoveListener(this.config), new VoiceChannelDeleteListener());
            } else {
                Bukkit.getPluginManager().registerEvents(new net.clementraynaud.skoice.listeners.player.PlayerJoinListener(this, this.bot), this);
                if (this.bot.getJda() != null) {
                    Menu.MODE.refreshFields();
                }
            }
        } else if (!wasBotReady && this.isBotReady) {
            HandlerList.unregisterAll(new net.clementraynaud.skoice.listeners.player.PlayerJoinListener(this, this.bot));
            this.registerEligiblePlayerListeners();
            this.bot.getJda().addEventListener(new GuildVoiceJoinListener(this.config), new GuildVoiceLeaveListener(this.config),
                    new GuildVoiceMoveListener(this.config), new VoiceChannelDeleteListener());
            Menu.MODE.refreshFields();
            this.getLogger().info(LoggerLang.CONFIGURATION_COMPLETE_INFO.toString());
            Message configurationMessage = new Response(this, this.config, this.bot).getConfigurationMessage();
            if (configurationMessage != null) {
                configurationMessage.getInteraction().getUser().openPrivateChannel().complete()
                        .sendMessageEmbeds(new EmbedBuilder().setTitle(MenuEmoji.GEAR + DiscordLang.CONFIGURATION_EMBED_TITLE.toString())
                                .addField(MenuEmoji.HEAVY_CHECK_MARK + DiscordLang.CONFIGURATION_COMPLETE_FIELD_TITLE.toString(),
                                        DiscordLang.CONFIGURATION_COMPLETE_FIELD_DESCRIPTION.toString(), false)
                                .setColor(Color.GREEN).build())
                        .queue(null, new ErrorHandler().ignore(ErrorResponse.CANNOT_SEND_TO_USER));
            }
        } else if (wasBotReady && !this.isBotReady) {
            new Response(this, this.config, this.bot).deleteMessage();
            this.unregisterEligiblePlayerListeners();
            Bukkit.getPluginManager().registerEvents(new net.clementraynaud.skoice.listeners.player.PlayerJoinListener(this, this.bot), this);
            if (this.bot.getJda() != null) {
                this.bot.getJda().removeEventListener(new GuildVoiceJoinListener(this.config), new GuildVoiceLeaveListener(this.config),
                        new GuildVoiceMoveListener(this.config), new VoiceChannelDeleteListener());
                Menu.MODE.refreshFields();
            }
            new InterruptSystemTask(this.config).run();
        }
    }

    private void registerEligiblePlayerListeners() {
        Bukkit.getPluginManager().registerEvents(new PlayerJoinListener(this.config), this);
        Bukkit.getPluginManager().registerEvents(new PlayerQuitListener(this), this);
        Bukkit.getPluginManager().registerEvents(new PlayerMoveListener(), this);
        Bukkit.getPluginManager().registerEvents(new PlayerTeleportListener(), this);
    }

    private void unregisterEligiblePlayerListeners() {
        HandlerList.unregisterAll(new PlayerJoinListener(this.config));
        HandlerList.unregisterAll(new PlayerQuitListener(this));
        HandlerList.unregisterAll(new PlayerMoveListener());
        HandlerList.unregisterAll(new PlayerTeleportListener());
    }
}
