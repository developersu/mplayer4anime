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
package mplayer4anime.media;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Looks for external media files (audio tracks or subtitles) in the
 * folder(s) where the video files are located and in their sub-folders.
 * <p>
 * Every directory that directly contains matching files is reported as a
 * separate {@link MediaCollection}. If a directory contains files of
 * several releases (e.g. audio tracks of different release groups), each
 * release becomes its own collection. A file's release is recognised by
 * the "[token]" markers in its name that are neither an episode number
 * (e.g. "[01]") nor a per-episode release hash (e.g. "[26C6BE62]").
 * <p>
 * The files of every release (or of the files without markers) are
 * additionally split by the plain words in their names that are not
 * episode numbers or release hashes (e.g. "Audio 01.mka" and "Audio 01
 * Awesome.mka" form two collections).
 * <p>
 * The search is depth-limited so a huge tree of folders does not get
 * traversed.
 */
public final class MediaCollectionFinder {
    // How many levels below the video folder to search
    private static final int MAX_SEARCH_DEPTH = 5;

    private static final Pattern BRACKET_TOKEN = Pattern.compile("\\[([^\\[\\]]+)\\]");
    // Episode numbers such as [01] or [12]
    private static final Pattern EPISODE_TOKEN = Pattern.compile("\\d+");
    // Per-episode release hashes such as [26C6BE62] (6-12 hex characters)
    private static final Pattern HASH_TOKEN = Pattern.compile("[0-9a-f]{6,12}");

    private MediaCollectionFinder() {
    }

    /**
     * @param videoFiles video files the external media is searched for
     * @param extensions patterns in the form of "*.ext" (case-insensitive)
     * @return found collections sorted by the directory path, then by the collection label
     * (so collections of the same folder stay together)
     */
    public static List<MediaCollection> findCollections(List<File> videoFiles, String[] extensions) {
        var videoFolders = new LinkedHashSet<File>();
        for (var videoFile : videoFiles) {
            var parent = videoFile.getParentFile();
            if (parent != null && parent.isDirectory())
                videoFolders.add(parent);
        }

        var found = new ArrayList<MediaCollection>();
        var knownPaths = new HashSet<String>();
        for (var folder : videoFolders)
            searchFolder(folder, 0, extensions, found, knownPaths);

        found.sort(Comparator
                .comparing((MediaCollection c) -> c.directory().getAbsolutePath())
                .thenComparing(c -> c.label() == null ? "" : c.label(),
                        String.CASE_INSENSITIVE_ORDER));
        return found;
    }

    private static void searchFolder(File folder, int depth, String[] extensions,
                                    List<MediaCollection> found, Set<String> knownPaths) {
        var path = folder.getAbsolutePath();
        if (knownPaths.add(path)) {
            var matching = listMatchingFiles(folder, extensions);
            if (matching.length > 0)
                found.addAll(splitIntoCollections(folder, matching));
        }

        if (depth >= MAX_SEARCH_DEPTH)
            return;

        var subDirs = folder.listFiles(File::isDirectory);
        if (subDirs == null)
            return;

        Arrays.sort(subDirs, Comparator.comparing(File::getName));
        for (var subDir : subDirs)
            searchFolder(subDir, depth + 1, extensions, found, knownPaths);
    }

    /**
     * Splits the files found in one directory into collections. Files of different release markers in their names
     * are put into separate collections; files that only differ by an episode number or a per-episode release hash
     * stay in one collection. The files of every release are additionally split by the plain words in their names
     * that are not episode numbers or release hashes (e.g. "Audio 01.mka" and "Audio 01 Awesome.mka"). A
     * collection's label is the word(s) that distinguish it from the common part of the names; a directory with a
     * single collection stays unlabeled.
     */
    private static List<MediaCollection> splitIntoCollections(File folder, File[] matching) {
        var groups = groupFiles(matching, MediaCollectionFinder::releaseKey);
        var result = new ArrayList<MediaCollection>();
        for (var group : groups.entrySet()) {
            var groupFiles = group.getValue();

            // Try splitting the release's files by the plain words
            var plainGroups = groupFiles(groupFiles.toArray(new File[0]),
                    MediaCollectionFinder::plainTokenKey);
            if (plainGroups.size() > 1) {
                // Words shared by every file of the release are part of the common name, not a marker
                var common = new HashSet<>(plainTokens(groupFiles.getFirst().getName()));
                for (var file : groupFiles)
                    common.retainAll(plainTokens(file.getName()));
                for (var plain : plainGroups.entrySet())
                    result.add(new MediaCollection(folder,
                            plain.getValue().toArray(new File[0]),
                            label(group.getKey(), plainLabel(plain.getKey(), common))));
            }
            else {
                result.add(new MediaCollection(folder,
                        groupFiles.toArray(new File[0]), label(group.getKey(), "")));
            }
        }

        // A directory with a single collection does not need a label
        if (result.size() == 1)
            return List.of(new MediaCollection(folder, result.getFirst().files()));
        return result;
    }

    /**
     * Joins the release marker and the plain words that distinguish the collection within the release
     * into a single label. Returns {@code null} when the collection has no marker at all.
     */
    private static String label(String releaseKey, String plain) {
        var release = releaseLabel(releaseKey);
        if (release.isEmpty())
            return plain.isEmpty() ? null : plain;
        return plain.isEmpty() ? release : release + " " + plain;
    }

    private static LinkedHashMap<String, List<File>> groupFiles(File[] files, Function<String, String> key) {
        var groups = new LinkedHashMap<String, List<File>>();
        for (var file : files)
            groups.computeIfAbsent(key.apply(file.getName()), _ -> new ArrayList<>()).add(file);
        return groups;
    }

    /**
     * The part of a file name that identifies its release: all "[token]" markers that are neither an episode number
     * (e.g. "[01]") nor a per-episode release hash (e.g. "[26C6BE62]"), joined by "|".
     */
    static String releaseKey(String fileName) {
        var tokens = BRACKET_TOKEN.matcher(fileName);
        var kept = new ArrayList<String>();
        while (tokens.find()) {
            var token = tokens.group(1).trim();
            if (!token.isEmpty()
                    && !EPISODE_TOKEN.matcher(token).matches()
                    && !HASH_TOKEN.matcher(token.toLowerCase()).matches())
                kept.add(token);
        }
        return String.join("|", kept);
    }

    /** The last release marker of a key, used to display the collection. */
    static String releaseLabel(String key) {
        var idx = key.lastIndexOf('|');
        return idx >= 0 ? key.substring(idx + 1) : key;
    }

    /**
     * The plain words of a file name (without the extension and without the "[bracket]" parts) that may identify 
     * a release, in the order they appear. E.g. "Audio 01 Awesome.mka" yields ["Audio", "Awesome"]. Words that contain 
     * digits (episode numbers like "ep01", quality marks like "1080p") are not release markers.
     */
    static Set<String> plainTokens(String fileName) {
        var name = fileName;
        var dot = name.lastIndexOf('.');
        if (dot >= 0)
            name = dot == 0 ? "" : name.substring(0, dot);
        // The "[bracket]" parts are handled separately by the release key
        var plain = BRACKET_TOKEN.matcher(name).replaceAll(" ");
        var tokens = new LinkedHashSet<String>();
        for (var raw : plain.split("\\s+")) {
            var token = raw.trim();
            if (!token.isEmpty()
                    && !EPISODE_TOKEN.matcher(token).matches()
                    && !HASH_TOKEN.matcher(token.toLowerCase()).matches()
                    && token.chars().anyMatch(Character::isLetter)
                    && token.chars().noneMatch(Character::isDigit))
                tokens.add(token);
        }
        return tokens;
    }

    /** All plain tokens of a file name joined by "|". */
    static String plainTokenKey(String fileName) {
        return String.join("|", plainTokens(fileName));
    }

    /**
     * The tokens of a plain-token key that are not shared by every file of the directory (the {@code common} set),
     * joined by a space. Empty for the collection whose name matches the common part (it stays unlabeled).
     */
    static String plainLabel(String key, Set<String> common) {
        if (key.isEmpty())
            return "";
        return Arrays.stream(key.split("\\|"))
                .filter(t -> !t.isEmpty() && !common.contains(t))
                .collect(Collectors.joining(" "));
    }

    private static File[] listMatchingFiles(File folder, String[] extensions) {
        var files = folder.listFiles((dir, name) -> matchesExtension(name, extensions));
        if (files == null)
            return new File[0];
        // listFiles() does not guarantee any order
        Arrays.sort(files, Comparator.comparing(File::getName));
        return files;
    }

    /**
     * Checks if a file name ends with any of the given patterns in the form of "*.ext".
     * The comparison is case-insensitive.
     */
    static boolean matchesExtension(String name, String[] extensions) {
        if (name == null)
            return false;
        var lowerName = name.toLowerCase();
        for (var ext : extensions) {
            if (ext == null)
                continue;
            var pattern = ext.startsWith("*") ? ext.substring(1) : ext;
            if (!pattern.isEmpty() && lowerName.endsWith(pattern.toLowerCase()))
                return true;
        }
        return false;
    }
}
