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

package net.clementraynaud.skoice.menus.selectmenus;

import net.clementraynaud.skoice.config.ConfigField;
import net.clementraynaud.skoice.menus.Menu;
import net.clementraynaud.skoice.menus.MenuEmoji;
import net.clementraynaud.skoice.lang.DiscordLang;
import net.dv8tion.jda.api.interactions.components.selections.SelectOption;
import net.dv8tion.jda.api.interactions.components.selections.SelectionMenu;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class ModeSelectMenu extends SelectMenu {

    private static final String VANILLA_MODE_ID = "VANILLA_MODE";
    private static final String MINIGAME_MODE_ID = "MINIGAME_MODE";
    private static final String CUSTOMIZE_ID = "CUSTOMIZE";

    public ModeSelectMenu() {
        super(false);
    }

    @Override
    public SelectionMenu get() {
        List<SelectOption> modes = new ArrayList<>(Arrays.asList(SelectOption.of(DiscordLang.VANILLA_MODE_FIELD_TITLE.toString(), ModeSelectMenu.VANILLA_MODE_ID)
                        .withDescription(DiscordLang.VANILLA_MODE_SELECT_MENU_DESCRIPTION.toString())
                        .withEmoji(MenuEmoji.MAP.getEmojiFromUnicode()),
                SelectOption.of(DiscordLang.MINIGAME_MODE_FIELD_TITLE.toString(), ModeSelectMenu.MINIGAME_MODE_ID)
                        .withDescription(DiscordLang.MINIGAME_MODE_SELECT_MENU_DESCRIPTION.toString())
                        .withEmoji(MenuEmoji.CROSSED_SWORDS.getEmojiFromUnicode())));
        if (super.bot.isReady()) {
            String defaultValue;
            if (super.config.getFile().getInt(ConfigField.HORIZONTAL_RADIUS.get()) == 80
                    && super.config.getFile().getInt(ConfigField.VERTICAL_RADIUS.get()) == 40
                    && !Menu.customizeRadius) {
                defaultValue = ModeSelectMenu.VANILLA_MODE_ID;
                modes.add(SelectOption.of(DiscordLang.CUSTOMIZE_FIELD_TITLE.toString(), ModeSelectMenu.CUSTOMIZE_ID)
                        .withDescription(DiscordLang.CUSTOMIZE_SELECT_MENU_DESCRIPTION.toString())
                        .withEmoji(MenuEmoji.PENCIL2.getEmojiFromUnicode()));
            } else if (super.config.getFile().getInt(ConfigField.HORIZONTAL_RADIUS.get()) == 40
                    && super.config.getFile().getInt(ConfigField.VERTICAL_RADIUS.get()) == 20
                    && !Menu.customizeRadius) {
                defaultValue = ModeSelectMenu.MINIGAME_MODE_ID;
                modes.add(SelectOption.of(DiscordLang.CUSTOMIZE_FIELD_TITLE.toString(), ModeSelectMenu.CUSTOMIZE_ID)
                        .withDescription(DiscordLang.CUSTOMIZE_SELECT_MENU_DESCRIPTION.toString())
                        .withEmoji(MenuEmoji.PENCIL2.getEmojiFromUnicode()));
            } else {
                defaultValue = ModeSelectMenu.CUSTOMIZE_ID;
                modes.add(SelectOption.of(DiscordLang.CUSTOMIZE_FIELD_TITLE.toString(), ModeSelectMenu.CUSTOMIZE_ID)
                        .withDescription(String.format(DiscordLang.CUSTOMIZE_SELECT_MENU_ALTERNATIVE_DESCRIPTION.toString(),
                                super.config.getFile().getInt(ConfigField.HORIZONTAL_RADIUS.get()),
                                super.config.getFile().getInt(ConfigField.VERTICAL_RADIUS.get())))
                        .withEmoji(MenuEmoji.PENCIL2.getEmojiFromUnicode()));
            }
            return SelectionMenu.create(Menu.MODE.name() + "_SELECTION")
                    .setPlaceholder(DiscordLang.MODE_SELECT_OPTION_PLACEHOLDER.toString())
                    .addOptions(modes)
                    .setDefaultValues(Collections.singleton(defaultValue)).build();
        } else {
            return SelectionMenu.create(Menu.MODE.name() + "_SELECTION")
                    .setPlaceholder(DiscordLang.MODE_SELECT_OPTION_PLACEHOLDER.toString())
                    .addOptions(modes).build();
        }
    }
}
