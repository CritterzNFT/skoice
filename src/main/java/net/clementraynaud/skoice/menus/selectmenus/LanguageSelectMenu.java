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
import net.clementraynaud.skoice.lang.Lang;
import net.dv8tion.jda.api.interactions.components.selections.SelectOption;
import net.dv8tion.jda.api.interactions.components.selections.SelectionMenu;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class LanguageSelectMenu extends SelectMenu {

    public LanguageSelectMenu() {
        super(false);
    }

    @Override
    public SelectionMenu get() {
        List<SelectOption> options = new ArrayList<>();
        for (Lang lang : Lang.values()) {
            options.add(SelectOption.of(lang.getFullName(), lang.name())
                    .withDescription(lang.name().equals(Lang.EN.name())
                            ? super.lang.getMessage("discord.select-option.default.description")
                            : null)
                    .withEmoji(lang.getEmoji()));
        }
        if (super.bot.isReady()) {
            return SelectionMenu.create(Menu.LANGUAGE.name() + "_SELECTION")
                    .addOptions(options)
                    .setDefaultValues(Collections.singleton(super.config.getFile().getString(ConfigField.LANG.get()))).build();
        } else {
            return SelectionMenu.create(Menu.LANGUAGE.name() + "_SELECTION")
                    .setPlaceholder(super.lang.getMessage("discord.menu.language.select-menu.placeholder"))
                    .addOptions(options).build();
        }
    }
}
