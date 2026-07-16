#define MyAppName "OmniTune"
#ifndef AppVersion
#define AppVersion "0.0.0"
#endif
#ifndef SourceDir
#define SourceDir "..\..\composeApp\build\compose\binaries\main\app\OmniTune"
#endif
#ifndef OutputDir
#define OutputDir "..\..\build\release\windows\inno"
#endif
#ifndef Architecture
#define Architecture "x64"
#endif
#ifndef SetupIconFile
#define SetupIconFile "..\..\composeApp\src\desktopMain\resources\icon.ico"
#endif
#ifndef WizardImageFile
#define WizardImageFile "..\..\build\installer-branding\wizard-image.bmp"
#endif
#ifndef WizardSmallImageFile
#define WizardSmallImageFile "..\..\build\installer-branding\wizard-small-image.bmp"
#endif

[Setup]
AppId={{7A8B9C0D-1E2F-3A4B-5C6D-7E8F9A0B1C2D}
AppName={#MyAppName}
AppVersion={#AppVersion}
AppVerName={#MyAppName} {#AppVersion}
AppPublisher=OmniTune
AppPublisherURL=https://github.com/soupashh-ship-it/OmniTune-Windows
AppSupportURL=https://github.com/soupashh-ship-it/OmniTune-Windows/issues
AppUpdatesURL=https://github.com/soupashh-ship-it/OmniTune-Windows/releases
DefaultDirName={localappdata}\OmniTune
DefaultGroupName=OmniTune
DisableProgramGroupPage=yes
OutputDir={#OutputDir}
OutputBaseFilename=OmniTune-Setup-{#AppVersion}-windows-{#Architecture}-custom
Compression=lzma2/ultra64
SolidCompression=yes
SetupIconFile={#SetupIconFile}
WizardImageFile={#WizardImageFile}
WizardSmallImageFile={#WizardSmallImageFile}
ArchitecturesAllowed=x64compatible
ArchitecturesInstallIn64BitMode=x64compatible
PrivilegesRequired=lowest
PrivilegesRequiredOverridesAllowed=dialog
CloseApplications=yes
RestartApplications=no
UsePreviousAppDir=yes
DirExistsWarning=no
UninstallDisplayIcon={app}\OmniTune.exe
VersionInfoVersion={#AppVersion}
VersionInfoCompany=OmniTune
VersionInfoDescription=OmniTune Windows Installer
VersionInfoProductName=OmniTune
VersionInfoProductVersion={#AppVersion}
WizardStyle=modern

[Languages]
Name: "english"; MessagesFile: "compiler:Default.isl"

[Tasks]
Name: "desktopicon"; Description: "{cm:CreateDesktopIcon}"; GroupDescription: "{cm:AdditionalIcons}"; Flags: unchecked

[Files]
Source: "{#SourceDir}\*"; DestDir: "{app}"; Flags: ignoreversion recursesubdirs createallsubdirs

[Icons]
Name: "{group}\OmniTune"; Filename: "{app}\OmniTune.exe"; WorkingDir: "{app}"; IconFilename: "{app}\OmniTune.exe"
Name: "{autodesktop}\OmniTune"; Filename: "{app}\OmniTune.exe"; WorkingDir: "{app}"; IconFilename: "{app}\OmniTune.exe"; Tasks: desktopicon

[Run]
Filename: "{app}\OmniTune.exe"; Description: "{cm:LaunchProgram,OmniTune}"; Flags: nowait postinstall skipifsilent

[Code]
function InitializeSetup(): Boolean;
begin
  Result := True;
end;
