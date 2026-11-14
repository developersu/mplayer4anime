; Include Modern UI
!include "MUI.nsh"
Unicode true

; Name and file
!define APPNAME "mplayer4anime"
!define COMPANYNAME "Dmitry Isaenko"

; Version is passed from create_nsis.sh via makensis /DVERSIONMAJOR /DVERSIONMINOR
; (falls back to the values below when built without them)
!ifndef VERSIONMAJOR
  !define VERSIONMAJOR 9999
!endif
!ifndef VERSIONMINOR
  !define VERSIONMINOR 9999
!endif

; Output file base name
!ifndef FILEOUT
  !define FILEOUT "Installer"
!endif

Name "mplayer4anime"
OutFile "${FILEOUT}-${VERSIONMAJOR}.${VERSIONMINOR}.exe"

; Default installation folder
InstallDir "$PROGRAMFILES64\${APPNAME}"

; Get installation folder from registry if available
InstallDirRegKey HKCU "Software\${APPNAME}" ""

; Request application privileges for Windows Vista
RequestExecutionLevel admin

!define MUI_ICON installer_logo.ico
!define MUI_UNICON uninstaller_logo.ico
; !define MUI_FINISHPAGE_NOAUTOCLOSE

!define MUI_WELCOMEFINISHPAGE_BITMAP "leftbar.bmp"
!define MUI_UNWELCOMEFINISHPAGE_BITMAP "leftbar_uninstall.bmp"

!define MUI_FINISHPAGE_LINK "mplayer4anime at GitHub"
!define MUI_FINISHPAGE_LINK_LOCATION https://github.com/developersu/mplayer4anime/

!define MUI_FINISHPAGE_TEXT $(l10n_FinishText)
!define MUI_FINISHPAGE_RUN "$INSTDIR\mplayer4anime.exe"
!define MUI_FINISHPAGE_SHOWREADME
!define MUI_FINISHPAGE_SHOWREADME_TEXT $(l10n_CreateShortcut)
!define MUI_FINISHPAGE_SHOWREADME_FUNCTION CreateDesktopShortCut
!define MUI_FINISHPAGE_SHOWREADME_NOTCHECKED
;--------------------------------
; Interface Settings
;--------------------------------
!define MUI_ABORTWARNING

;--------------------------------
; Language Selection Dialog Settings
;--------------------------------
; Remember the installer language
!define MUI_LANGDLL_REGISTRY_ROOT "HKCU"
!define MUI_LANGDLL_REGISTRY_KEY "Software\${APPNAME}"
!define MUI_LANGDLL_REGISTRY_VALUENAME "Installer Language"

;--------------------------------
; Pages
;--------------------------------
!insertmacro MUI_PAGE_WELCOME
!insertmacro MUI_PAGE_LICENSE "license.txt"
!insertmacro MUI_PAGE_DIRECTORY
!insertmacro MUI_PAGE_INSTFILES
; FinishPagePre adds the MPlayer / mpv download links
!define MUI_PAGE_CUSTOMFUNCTION_PRE FinishPagePre
!insertmacro MUI_PAGE_FINISH

!insertmacro MUI_UNPAGE_CONFIRM
!insertmacro MUI_UNPAGE_INSTFILES
!insertmacro MUI_UNPAGE_FINISH

;--------------------------------
; Languages
;--------------------------------
!insertmacro MUI_LANGUAGE "English"
!insertmacro MUI_LANGUAGE "Russian"
!insertmacro MUI_LANGUAGE "Korean"

; Language strings
LangString l10n_CreateShortcut ${LANG_ENGLISH} "Create Desktop Shortcut"
LangString l10n_CreateShortcut ${LANG_RUSSIAN}  "Создать ярлык на Рабочем столе"
LangString l10n_CreateShortcut ${LANG_KOREAN}   "데스크톱에 바로가기 만들기"

LangString l10n_FinishText ${LANG_ENGLISH} "A video player is required. Use the links below to download MPlayer or mpv."
LangString l10n_FinishText ${LANG_RUSSIAN}  "Требуется видеоплеер. Воспользуйтесь ссылками ниже, чтобы скачать MPlayer или mpv."
LangString l10n_FinishText ${LANG_KOREAN}   "동영상 재생기가 필요합니다. 아래 링크에서 MPlayer 또는 mpv 를 다운로드하세요."

LangString l10n_LinkMPlayer ${LANG_ENGLISH} "MPlayer download page (mplayerhq.hu)"
LangString l10n_LinkMPlayer ${LANG_RUSSIAN}  "Страница загрузки MPlayer (mplayerhq.hu)"
LangString l10n_LinkMPlayer ${LANG_KOREAN}   "MPlayer 다운로드 페이지 (mplayerhq.hu)"

LangString l10n_LinkMpv ${LANG_ENGLISH} "mpv download page (GitHub)"
LangString l10n_LinkMpv ${LANG_RUSSIAN}  "Страница загрузки mpv (GitHub)"
LangString l10n_LinkMpv ${LANG_KOREAN}   "mpv 다운로드 페이지 (GitHub)"

BrandingText "mplayer4anime v${VERSIONMAJOR}.${VERSIONMINOR}"

;--------------------------------
; Install Section
;--------------------------------
Section "mplayer4anime" Install

  SetOutPath "$INSTDIR"
  ; Bundled JRE (created via jlink, see ../HOWTO_JRE.md)
  file /r \assembly\jdk
  ; Windows launcher (built by maven 'windows' profile, see ../create_nsis.sh)
  file mplayer4anime.exe
  file logo.ico

  ; Registry information for add/remove programs
  WriteRegStr HKLM "Software\Microsoft\Windows\CurrentVersion\Uninstall\${APPNAME}" "DisplayName"     "${APPNAME}"
  WriteRegStr HKLM "Software\Microsoft\Windows\CurrentVersion\Uninstall\${APPNAME}" "DisplayVersion"  "${VERSIONMAJOR}.${VERSIONMINOR}"
  WriteRegStr HKLM "Software\Microsoft\Windows\CurrentVersion\Uninstall\${APPNAME}" "UninstallString" "$INSTDIR\Uninstall.exe"
  WriteRegStr HKLM "Software\Microsoft\Windows\CurrentVersion\Uninstall\${APPNAME}" "DisplayIcon"     "$INSTDIR\logo.ico"
  WriteRegStr HKLM "Software\Microsoft\Windows\CurrentVersion\Uninstall\${APPNAME}" "Publisher"       "${COMPANYNAME}"
  WriteRegStr HKLM "Software\Microsoft\Windows\CurrentVersion\Uninstall\${APPNAME}" "VersionMajor"    "${VERSIONMAJOR}"
  WriteRegStr HKLM "Software\Microsoft\Windows\CurrentVersion\Uninstall\${APPNAME}" "VersionMinor"    "${VERSIONMINOR}"

  ; Start Menu
  CreateDirectory "$SMPROGRAMS\${APPNAME}"
  CreateShortCut "$SMPROGRAMS\${APPNAME}\${APPNAME}.lnk"  "$INSTDIR\mplayer4anime.exe" "" "$INSTDIR\logo.ico"
  CreateShortCut "$SMPROGRAMS\${APPNAME}\Uninstall.lnk"   "$INSTDIR\Uninstall.exe"

  ; Store installation folder
  WriteRegStr HKCU "Software\${APPNAME}" "" $INSTDIR

  ; Create uninstaller
  WriteUninstaller "$INSTDIR\Uninstall.exe"

SectionEnd

;--------------------------------
; Installer Functions
;--------------------------------
Function .onInit
  ; Set mandatory installation rule to main section
  SectionSetFlags ${Install} 17
FunctionEnd

Function un.onInit
  !insertmacro MUI_UNGETLANGUAGE
FunctionEnd

Function CreateDesktopShortCut
  CreateShortcut "$DESKTOP\mplayer4anime.lnk" "$INSTDIR\mplayer4anime.exe"
FunctionEnd

; Finish page custom function (see MUI_PAGE_CUSTOMFUNCTION_PRE):
; adds the player backend download links as InstallOptions fields 7-8 in the
; empty space between the "Create Desktop Shortcut" checkbox (field 5,
; bottom 120) and the GitHub link (field 6, top 175). A "Link" field's State
; is the URL opened on click, so no extra handler functions are needed.
; This hook runs after MUI wrote the standard fields (Numfields=6) but before
; the dialog is shown, so it is safe to append fields and bump Numfields.
Function FinishPagePre
  ; MPlayer download link
  !insertmacro INSTALLOPTIONS_WRITE "ioSpecial.ini" "Field 7" "Type" "Link"
  !insertmacro INSTALLOPTIONS_WRITE_CONVERT "ioSpecial.ini" "Field 7" "Text" "$(l10n_LinkMPlayer)"
  !insertmacro INSTALLOPTIONS_WRITE "ioSpecial.ini" "Field 7" "Left" "120"
  !insertmacro INSTALLOPTIONS_WRITE "ioSpecial.ini" "Field 7" "Right" "315"
  !insertmacro INSTALLOPTIONS_WRITE "ioSpecial.ini" "Field 7" "Top" "132"
  !insertmacro INSTALLOPTIONS_WRITE "ioSpecial.ini" "Field 7" "Bottom" "142"
  !insertmacro INSTALLOPTIONS_WRITE "ioSpecial.ini" "Field 7" "State" "https://mplayerhq.hu/design7/dload.html"

  ; mpv download link
  !insertmacro INSTALLOPTIONS_WRITE "ioSpecial.ini" "Field 8" "Type" "Link"
  !insertmacro INSTALLOPTIONS_WRITE_CONVERT "ioSpecial.ini" "Field 8" "Text" "$(l10n_LinkMpv)"
  !insertmacro INSTALLOPTIONS_WRITE "ioSpecial.ini" "Field 8" "Left" "120"
  !insertmacro INSTALLOPTIONS_WRITE "ioSpecial.ini" "Field 8" "Right" "315"
  !insertmacro INSTALLOPTIONS_WRITE "ioSpecial.ini" "Field 8" "Top" "152"
  !insertmacro INSTALLOPTIONS_WRITE "ioSpecial.ini" "Field 8" "Bottom" "162"
  !insertmacro INSTALLOPTIONS_WRITE "ioSpecial.ini" "Field 8" "State" "https://github.com/mpv-player/mpv/releases/"

  ; MUI set Numfields=6; bump it to 8 so the two new links are shown
  !insertmacro INSTALLOPTIONS_WRITE "ioSpecial.ini" "Settings" "Numfields" "8"
FunctionEnd

;--------------------------------
; Uninstaller Section
;--------------------------------
Section "Uninstall"

  ; Start Menu
  Delete "$SMPROGRAMS\${APPNAME}\${APPNAME}.lnk"
  Delete "$SMPROGRAMS\${APPNAME}\Uninstall.lnk"
  Delete "$DESKTOP\mplayer4anime.lnk"
  RMDir  "$SMPROGRAMS\${APPNAME}"

  ; Delete installed files
  RMDir  /r "$INSTDIR\jdk"
  Delete "$INSTDIR\mplayer4anime.exe"
  Delete "$INSTDIR\logo.ico"
  Delete "$INSTDIR\Uninstall.exe"

  RMDir "$INSTDIR"

  DeleteRegKey HKCU "Software\${APPNAME}"
  ; Cleanup records stored for uninstaller from the registry
  DeleteRegKey HKLM "Software\Microsoft\Windows\CurrentVersion\Uninstall\${APPNAME}"

SectionEnd

;--------------------------------
; Uninstaller Functions
;--------------------------------
