@echo off
echo Generating keystore for StoryBookEmoji app...

set KEYSTORE_PATH=keystore/release.keystore
set KEY_ALIAS=storybook_key
set KEYSTORE_PASSWORD=PurpleFluffyDinosaurs123
set KEY_PASSWORD=PurpleFluffyDinosaurs123
set VALIDITY=10000

echo This will create a keystore file at: %KEYSTORE_PATH%
echo Key alias: %KEY_ALIAS%
echo Validity: %VALIDITY% days

echo.
echo IMPORTANT: Keep this keystore file and passwords safe!
echo If you lose them, you won't be able to update your app on the Play Store.
echo.

pause

keytool -genkeypair ^
  -alias %KEY_ALIAS% ^
  -keyalg RSA ^
  -keysize 2048 ^
  -validity %VALIDITY% ^
  -keystore %KEYSTORE_PATH% ^
  -storepass %KEYSTORE_PASSWORD% ^
  -keypass %KEY_PASSWORD% ^
  -dname "CN=Your Name, OU=Your Organizational Unit, O=Your Organization, L=Your City, S=Your State, C=Your Country Code"

echo.
echo Keystore generation complete!
echo.

echo Updating keystore.properties file with the new values...
(
echo # Keystore configuration for app signing
echo # IMPORTANT: This file should NOT be committed to version control
echo # It contains sensitive information
echo.
echo # Path to the keystore file
echo storeFile=../keystore/release.keystore
echo.
echo # Keystore password - use a strong password
echo storePassword=%KEYSTORE_PASSWORD%
echo.
echo # Key alias
echo keyAlias=%KEY_ALIAS%
echo.
echo # Key password - use a strong password
echo keyPassword=%KEY_PASSWORD%
) > keystore.properties

echo.
echo keystore.properties file has been updated.
echo.
echo REMEMBER: Do not commit keystore.properties to version control!
echo.

pause 