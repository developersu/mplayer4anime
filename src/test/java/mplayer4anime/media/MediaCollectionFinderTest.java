package mplayer4anime.media;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class MediaCollectionFinderTest {

    @TempDir
    File tempDir;

    @Test
    void findsFilesInTheSameFolderAsVideo() throws IOException {
        var video = createFile(tempDir, "ep01.mkv");
        createFile(tempDir, "ep01.mka");

        var collections = MediaCollectionFinder.findCollections(
                List.of(video), new String[]{"*.mka"});

        assertEquals(1, collections.size());
        assertEquals(tempDir.getAbsoluteFile(), collections.getFirst().directory());
        assertArrayEquals(new File[]{new File(tempDir, "ep01.mka")},
                collections.getFirst().files());
    }

    @Test
    void findsFilesInSubFolderOfVideoFolder() throws IOException {
        var video = createFile(tempDir, "ep01.mkv");
        var sub = createFile(new File(tempDir, "RUS sound"), "ep01.mka");

        var collections = MediaCollectionFinder.findCollections(
                List.of(video), new String[]{"*.mka"});

        assertEquals(1, collections.size());
        assertEquals(new File(tempDir, "RUS sound").getAbsoluteFile(),
                collections.getFirst().directory());
        assertArrayEquals(new File[]{sub}, collections.getFirst().files());
    }

    @Test
    void reportsSeparateCollectionsPerFolderSortedByPath() throws IOException {
        var videoA = createFile(new File(tempDir, "A"), "ep01.mkv");
        var videoB = createFile(new File(tempDir, "B"), "ep02.mkv");
        var audioA = createFile(new File(tempDir, "A"), "ep01.mka");
        var audioB = createFile(new File(tempDir, "B"), "ep02.mka");

        var collections = MediaCollectionFinder.findCollections(
                Arrays.asList(videoA, videoB), new String[]{"*.mka"});

        assertEquals(2, collections.size());
        // Sorted by the directory path
        assertEquals(new File(tempDir, "A").getAbsoluteFile(),
                collections.get(0).directory());
        assertEquals(new File(tempDir, "B").getAbsoluteFile(),
                collections.get(1).directory());
        assertArrayEquals(new File[]{audioA}, collections.get(0).files());
        assertArrayEquals(new File[]{audioB}, collections.get(1).files());
    }

    @Test
    void doesNotReportTheSameCollectionTwice() throws IOException {
        // The same audio folder is reachable both directly (it contains a
        // video) and as a sub-folder of the video folder
        var shared = new File(tempDir, "shared");
        createFile(shared, "ep01.mka");
        createFile(new File(tempDir, "top"), "ep01.mkv");

        // Video files in both 'top' and 'shared' point at 'shared' twice
        var videoTop = new File(new File(tempDir, "top"), "ep01.mkv");
        var videoShared = createFile(shared, "ep02.mkv");

        var collections = MediaCollectionFinder.findCollections(
                Arrays.asList(videoTop, videoShared), new String[]{"*.mka"});

        assertEquals(1, collections.size());
        assertEquals(shared.getAbsoluteFile(), collections.getFirst().directory());
    }

    @Test
    void matchesExtensionsCaseInsensitively() throws IOException {
        var video = createFile(tempDir, "ep01.mkv");
        createFile(tempDir, "ep01.MKA");
        createFile(tempDir, "ep01.Mka");

        var collections = MediaCollectionFinder.findCollections(
                List.of(video), new String[]{"*.mka"});

        assertEquals(1, collections.size());
        assertArrayEquals(
                new File[]{new File(tempDir, "ep01.MKA"), new File(tempDir, "ep01.Mka")},
                collections.getFirst().files());
    }

    @Test
    void returnsEmptyListWhenNothingIsFound() throws IOException {
        var video = createFile(tempDir, "ep01.mkv");
        createFile(new File(tempDir, "subs"), "ep01.ass");

        var collections = MediaCollectionFinder.findCollections(
                List.of(video), new String[]{"*.mka"});

        assertTrue(collections.isEmpty());
    }

    @Test
    void ignoresFilesWithoutMatchingExtension() throws IOException {
        var video = createFile(tempDir, "ep01.mkv");
        createFile(tempDir, "ep01.txt");
        createFile(new File(tempDir, "subs"), "ep01.srt");

        var collections = MediaCollectionFinder.findCollections(
                List.of(video), new String[]{"*.mka"});

        assertTrue(collections.isEmpty());
    }

    @Test
    void findsCollectionsAtMaximumDepth() throws IOException {
        var video = createFile(tempDir, "ep01.mkv");
        // depth 5: sub1/sub2/sub3/sub4/sub5
        var deep = new File(tempDir, "sub1/sub2/sub3/sub4/sub5");
        deep.mkdirs();
        createFile(deep, "ep01.mka");

        var collections = MediaCollectionFinder.findCollections(
                List.of(video), new String[]{"*.mka"});

        assertEquals(1, collections.size());
        assertEquals(deep.getAbsoluteFile(), collections.get(0).directory());
    }

    @Test
    void doesNotGoBeyondMaximumDepth() throws IOException {
        var video = createFile(tempDir, "ep01.mkv");
        // depth 6: one level deeper than the search limit
        var tooDeep = new File(tempDir, "sub1/sub2/sub3/sub4/sub5/sub6");
        tooDeep.mkdirs();
        createFile(tooDeep, "ep01.mka");

        var collections = MediaCollectionFinder.findCollections(
                List.of(video), new String[]{"*.mka"});

        assertTrue(collections.isEmpty());
    }

    @Test
    void splitsFilesOfDifferentReleaseGroupsInTheSameFolder() throws IOException {
        createFile(tempDir, "[LittleTests] Show 01 [BD 1080p].rus.[TesTing Group].mka");
        createFile(tempDir, "[LittleTests] Show 02 [BD 1080p].rus.[TesTing Group].mka");
        createFile(tempDir, "[LittleTests] Show 01 [BD 1080p].rus.[Midori].mka");
        createFile(tempDir, "[LittleTests] Show 02 [BD 1080p].rus.[Midori].mka");

        var collections = MediaCollectionFinder.findCollections(
                List.of(createFile(tempDir, "ep01.mkv")), new String[]{"*.mka"});

        assertEquals(2, collections.size());
        // Same directory, sorted by the release marker
        assertEquals(tempDir.getAbsoluteFile(), collections.getFirst().directory());
        assertEquals("Midori", collections.get(0).label());
        assertEquals(2, collections.get(0).files().length);
        assertEquals(tempDir.getAbsoluteFile(), collections.get(1).directory());
        assertEquals("TesTing Group", collections.get(1).label());
        assertEquals(2, collections.get(1).files().length);
    }

    @Test
    void doesNotSplitOnPerEpisodeReleaseHashes() throws IOException {
        // One release whose file names carry a per-episode hash
        // (the SubsPlease/ReZero case) - must stay a single collection
        createFile(tempDir, "[SubsPlease] Show - 39 (1080p) [26C6BE62].mka");
        createFile(tempDir, "[SubsPlease] Show - 40 (1080p) [46BA828B].mka");
        createFile(tempDir, "[SubsPlease] Show - 41 (1080p) [0E288189].mka");

        var collections = MediaCollectionFinder.findCollections(
                List.of(createFile(tempDir, "ep01.mkv")), new String[]{"*.mka"});

        assertEquals(1, collections.size());
        assertNull(collections.getFirst().label());
        assertEquals(3, collections.getFirst().files().length);
    }

    @Test
    void doesNotSplitOnEpisodeNumberTokens() throws IOException {
        createFile(tempDir, "[Group] Show [01].mka");
        createFile(tempDir, "[Group] Show [02].mka");

        var collections = MediaCollectionFinder.findCollections(
                List.of(createFile(tempDir, "ep01.mkv")), new String[]{"*.mka"});

        assertEquals(1, collections.size());
        assertEquals(2, collections.getFirst().files().length);
    }

    @Test
    void leavesSingleGroupWithoutLabel() throws IOException {
        createFile(tempDir, "ep01.mka");
        createFile(tempDir, "ep02.mka");

        var collections = MediaCollectionFinder.findCollections(
                List.of(createFile(tempDir, "ep01.mkv")), new String[]{"*.mka"});

        assertEquals(1, collections.size());
        assertNull(collections.getFirst().label());
        assertEquals(2, collections.getFirst().files().length);
    }

    @Test
    void splitsOnMultipleDifferingMarkers() throws IOException {
        // Subtitles of two releases in one folder (the 'ENG Subs' case):
        // mostly one group plus a single file of another group
        for (var ep : new String[]{"01", "02", "03"})
            createFile(tempDir, "[LittleTests] Show " + ep + " [BD 1080p].eng.[LNS].ass");
        createFile(tempDir, "[LittleTests] Show S2 [BD 1080p].eng.[TestingSubs].ass");

        var collections = MediaCollectionFinder.findCollections(
                List.of(createFile(tempDir, "ep01.mkv")), new String[]{"*.ass"});

        assertEquals(2, collections.size());
        assertEquals("LNS", collections.get(0).label());
        assertEquals(3, collections.get(0).files().length);
        assertEquals("TestingSubs", collections.get(1).label());
        assertEquals(1, collections.get(1).files().length);
    }

    @Test
    void splitsFilesThatDifferOnlyByPlainWord() throws IOException {
        // Two audio sets in one folder without "[bracket]" markers:
        // the common part of the names ("Audio") is not a marker
        createFile(tempDir, "Audio 01.mka");
        createFile(tempDir, "Audio 02.mka");
        createFile(tempDir, "Audio 01 Laza.mka");
        createFile(tempDir, "Audio 02 Laza.mka");

        var collections = MediaCollectionFinder.findCollections(
                List.of(createFile(tempDir, "video1.mp4")), new String[]{"*.mka"});

        assertEquals(2, collections.size());
        // Same directory, the collection without a marker comes first
        assertEquals(tempDir.getAbsoluteFile(), collections.getFirst().directory());
        assertNull(collections.get(0).label());
        assertArrayEquals(new File[]{
                        new File(tempDir, "Audio 01.mka"), new File(tempDir, "Audio 02.mka")},
                collections.get(0).files());
        assertEquals("Laza", collections.get(1).label());
        assertArrayEquals(new File[]{
                        new File(tempDir, "Audio 01 Laza.mka"), new File(tempDir, "Audio 02 Laza.mka")},
                collections.get(1).files());
    }

    @Test
    void splitsFilesThatDifferOnlyByPlainWordBeforehand() throws IOException {
        createFile(tempDir, "Audio 01.mka");
        createFile(tempDir, "Audio 02.mka");
        createFile(tempDir, "Test Audio 01.mka");
        createFile(tempDir, "Test Audio 02.mka");

        var collections = MediaCollectionFinder.findCollections(
                List.of(createFile(tempDir, "video1.mp4")), new String[]{"*.mka"});

        assertEquals(2, collections.size());
        // Same directory, the collection without a marker comes first
        assertEquals(tempDir.getAbsoluteFile(), collections.getFirst().directory());
        assertNull(collections.get(0).label());
        assertArrayEquals(new File[]{
                        new File(tempDir, "Audio 01.mka"), new File(tempDir, "Audio 02.mka")},
                collections.get(0).files());
        assertEquals("Test", collections.get(1).label());
        assertArrayEquals(new File[]{
                        new File(tempDir, "Test Audio 01.mka"), new File(tempDir, "Test Audio 02.mka")},
                collections.get(1).files());
    }

    @Test
    void splitsPlainWordsWithinEachReleaseGroup() throws IOException {
        // Mixed case: a plain base set, a plain "Laza" set, and a
        // bracketed release in the same folder - the plain-word split is
        // applied within every release group
        createFile(tempDir, "Audio 01.mka");
        createFile(tempDir, "Audio 02.mka");
        createFile(tempDir, "Audio 01 Laza.mka");
        createFile(tempDir, "Audio 02 Laza.mka");
        createFile(tempDir, "Audio 01 [Malta70].mka");
        createFile(tempDir, "Audio 02 [Malta70].mka");

        var collections = MediaCollectionFinder.findCollections(
                List.of(createFile(tempDir, "video1.mp4")), new String[]{"*.mka"});

        assertEquals(3, collections.size());
        assertNull(collections.get(0).label());
        assertEquals(2, collections.get(0).files().length);
        assertEquals("Laza", collections.get(1).label());
        assertEquals(2, collections.get(1).files().length);
        assertEquals("Malta70", collections.get(2).label());
        assertEquals(2, collections.get(2).files().length);
    }

    @Test
    void labelsPlainWordVariantsOfAReleaseWithTheReleaseName() throws IOException {
        // Plain-word variants inside a bracketed release keep the release
        // name in the label
        createFile(tempDir, "Audio 01 [Malta70].mka");
        createFile(tempDir, "Audio 02 [Malta70].mka");
        createFile(tempDir, "Audio 01 Laza [Malta70].mka");

        var collections = MediaCollectionFinder.findCollections(
                List.of(createFile(tempDir, "video1.mp4")), new String[]{"*.mka"});

        assertEquals(2, collections.size());
        assertEquals("Malta70", collections.get(0).label());
        assertEquals(2, collections.get(0).files().length);
        assertEquals("Malta70 Laza", collections.get(1).label());
        assertEquals(1, collections.get(1).files().length);
    }

    @Test
    void doesNotLabelPlainWordsSharedWithinOneRelease() throws IOException {
        // The "RUS" set is a single release on its own: its own words are
        // common within the release, so the collection is not labeled
        createFile(tempDir, "Audio 01 RUS.mka");
        createFile(tempDir, "Audio 02 RUS.mka");
        createFile(tempDir, "Audio 01 [Malta70].mka");

        var collections = MediaCollectionFinder.findCollections(
                List.of(createFile(tempDir, "video1.mp4")), new String[]{"*.mka"});

        assertEquals(2, collections.size());
        assertNull(collections.get(0).label());
        assertEquals(2, collections.get(0).files().length);
        assertEquals("Malta70", collections.get(1).label());
        assertEquals(1, collections.get(1).files().length);
    }

    @Test
    void doesNotSplitOnPerEpisodePlainWords() throws IOException {
        // Words containing digits (episode numbers, quality marks) are not
        // release markers, so one release stays a single collection
        createFile(tempDir, "Show S03E01 (1080p) track.mka");
        createFile(tempDir, "Show S03E02 (1080p) track.mka");

        var collections = MediaCollectionFinder.findCollections(
                List.of(createFile(tempDir, "video1.mp4")), new String[]{"*.mka"});

        assertEquals(1, collections.size());
        assertNull(collections.getFirst().label());
        assertEquals(2, collections.getFirst().files().length);
    }

    private File createFile(File dir, String name) throws IOException {
        dir.mkdirs();
        var file = new File(dir, name);
        Files.writeString(file.toPath(), name);
        return file;
    }
}
