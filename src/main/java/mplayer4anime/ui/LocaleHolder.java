/*
    Copyright 2026 Dmitry Isaenko

    This file is part of mplayer4anime.

    mplayer4anime is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    mplayer4anime is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with mplayer4anime.  If not, see <https://www.gnu.org/licenses/>.
 */
package mplayer4anime.ui;

import java.util.Locale;

public class LocaleHolder {

    private final Locale locale;
    private final String localeCode;
    private final String languageName;

    public LocaleHolder(String localeFileName) {
        var language = localeFileName.substring(7, 9);
        var country = localeFileName.substring(10, 12);
        this.locale = Locale.of(language, country);
        this.localeCode = locale.toString();
        this.languageName = locale.getDisplayLanguage(locale) + " (" + locale + ")";
    }

    @Override
    public String toString(){
        return languageName;
    }

    public String getLocaleCode(){
        return localeCode;
    }

    public Locale getLocale() {
        return locale;
    }
}
