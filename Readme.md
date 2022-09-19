# mplayer4anime

![License](https://img.shields.io/badge/License-GPLv3-blue.svg) ![Releases](https://img.shields.io/github/downloads/developersu/mplayer4anime/total.svg) [![Build Status](https://ci.redrise.ru/api/badges/desu/mplayer4anime/status.svg)](https://ci.redrise.ru/desu/mplayer4anime)

mplayer4anime is mplayer launcher to play video file with audio layer and/or subtitles (.ass, .srt, etc.) at once.

![Application screenshot 1](screenshots/1.png)

### License

Source code spreads under the GNU General Public License v.3. You can find it in LICENSE file.

Note: Since 0.10 application supports playlists management and implements own json-based format that (somehow) could be used in third-party application,
it would be nice to leave it as is. At least, I would prefer to have .alpr file extension used for this. As for the format of playlist, please
refer to WFTPL license.

### Used libraries
* [GSON](https://github.com/google/gson) (library distributes under Apache-2.0 license)
* [Material design icons](https://materialdesignicons.com/)
* Since v0.12: [OpenJFX](https://wiki.openjdk.java.net/display/OpenJFX/Main)

### Requirements

For latest versions of this app use JRE or JDK 8 or higher.

For older versions (prior to v0.12) please use JRE/JDK 8 (and JavaFX if you're NOT using Windows. For example: in Debian Stretch you should install 'openjfx' package).

### Running on Linux

Just start it as regular Java application:
```
$ java -jar mplayer4anime.jar
```

### Running on Windows
Step 1.

Download and install JRE 8 or later:
http://www.oracle.com/technetwork/java/javase/downloads/jre8-downloads-2133155.html

Step 2.

Download and install (unpack) mplayer:
http://oss.netfarm.it/mplayer/
(see 'Build selection table', for example click 'generic')

Step 3.

Download and run jar file.

Step 4.

If using v0.12 or higher:
Somehow set file associations for this application to all *.alpr files. Allow netowork usage when start application (using socket 65042 for inter-process communication within localhost)

OR

Wait for native installer. No ETA. 


### Thanks!

* [DDinghoya](https://github.com/DDinghoya), who translated this application to Korean!