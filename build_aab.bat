@echo off
echo Building Android App Bundle (AAB) for StoryBookEmoji app...
echo.

echo Cleaning project...
call gradlew clean
echo.

echo Building release AAB...
call gradlew bundleRelease
echo.

echo Build completed!
echo.
echo If successful, your AAB file should be located at:
echo app\build\outputs\bundle\release\app-release.aab
echo.
echo This file can be uploaded to the Google Play Console.
echo.

pause 