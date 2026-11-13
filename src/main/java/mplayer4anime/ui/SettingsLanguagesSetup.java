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

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import mplayer4anime.AppPreferences;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.net.URLDecoder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.Objects;
import java.util.jar.JarFile;

import static java.nio.charset.StandardCharsets.UTF_8;

public class SettingsLanguagesSetup {
    private static final Logger log = LoggerFactory.getLogger(SettingsLanguagesSetup.class);

    private final ObservableList<LocaleHolder> languages;
    private File thisApplicationFile;
    private LocaleHolder recentlyUsedLanguageHolder;

    public SettingsLanguagesSetup() {
        this.languages = FXCollections.observableArrayList();
        parseFiles();
        sortLanguages();
        defineRecentlyUsedLanguageHolder();
    }

    private void parseFiles() {
        if (isApplicationIsJar())   // Executed as JAR file
            parseFilesInsideJar();
        else                       // Executed within IDE
            parseFilesInFilesystem();
    }

    private boolean isApplicationIsJar() {
        var encodedJarLocation =
                getClass().getProtectionDomain().getCodeSource().getLocation().getPath().replace("+", "%2B");
        thisApplicationFile = new File(URLDecoder.decode(encodedJarLocation, UTF_8));
        return thisApplicationFile.isFile();
    }

    private void parseFilesInsideJar() {
        try (var jar = new JarFile(thisApplicationFile)) {
            jar.stream()
                    .filter(entry -> entry.getName().startsWith("locale_"))
                    .map(entry -> new LocaleHolder(entry.getName()))
                    .forEach(languages::add);
        }
        catch (IOException e) {
            log.error("Failed to read locale entries from JAR", e);
        }
    }

    private void parseFilesInFilesystem() {
        var pathToResource = Objects.requireNonNull(getClass().getResource("/"))
                .getPath();
        try (var streamDirs = Files.newDirectoryStream(Path.of(pathToResource))) {
            streamDirs.forEach(jarFileName -> {
                var fileName = ""+jarFileName.getFileName();
                if (fileName.startsWith("locale_"))
                    languages.add(new LocaleHolder(fileName));
            });
        }
        catch (Exception e){
            log.error("Failed to read locale files from filesystem", e);
        }
    }

    private void sortLanguages() {
        languages.sort(Comparator.comparing(LocaleHolder::toString));
    }

    private void defineRecentlyUsedLanguageHolder() {
        recentlyUsedLanguageHolder = languages.stream()
                .filter(localeHolder -> localeHolder.getLocale().equals(
                        AppPreferences.get().getLocale()))
                .findFirst()
                .orElse(null);

        if (recentlyUsedLanguageHolder == null) {
            recentlyUsedLanguageHolder = languages.stream()
                    .filter(localeHolder -> localeHolder.getLocaleCode().equals("en_US"))
                    .findFirst()
                    .orElseThrow(() -> new RuntimeException("Unable to get default locale (en_US)"));
        }
    }

    public ObservableList<LocaleHolder> getLanguages() {
        return languages;
    }

    public LocaleHolder getRecentLanguage() {
        return recentlyUsedLanguageHolder;
    }
}
