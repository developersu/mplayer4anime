# mplayer4anime

mplayer4anime is mplayer launcher to play video file with audio layer and/or subtitles (.ass and .srt) at once.

## License

Source code spreads under the GNU General Public License v.3. You can find it in LICENSE file or just visit www.gnu.org (it should be there for sure).

Note: Since 0.10 application supports playlists management and implements own json-based format that (somehow) could be used in third-party application,
it would be nice to leave it as is. At least, I would prefer to have .alpr file extension used for this. As for the format of playlist format, please
refer to WFTPL license.

## Used libraries
GSON: https://github.com/google/gson
Pay attention, that this lib uses Apache-2.0 license

## Requirements

JRE and JavaFX should be installed on your PC.
For example, in Debian Stretch you should install 'openjfx' package.

## Usage

Just start it as usual Java application:
```
$ java -jar mplayer4anime.jar
```

## Run on windows
Step 1.
Download and install JRE (8 or later):
http://www.oracle.com/technetwork/java/javase/downloads/jre8-downloads-2133155.html

Step 2.
Download and install (unpack) mplayer:
http://oss.netfarm.it/mplayer/
(see 'Build selection table', for example click 'generic')

Step 3.
Download and run jar file.
