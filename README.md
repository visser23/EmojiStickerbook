# StoryBook Emoji App

An interactive storybook app for kids with emoji stickers and drawing features.

## Features

- **Colorful Book Interface**: Navigate through 8 vibrant pages with different background colors
- **Simple Emoji Placement**: Tap anywhere on a page to place emoji stickers
- **Drag and Drop**: Move emojis around the page after placement
- **Easy Removal**: Long-press an emoji to remove it
- **Intuitive Navigation**: Simple forward and backward page controls
- **Reset Option**: Clear all stickers from the current page

## Technical Details

- Built with Jetpack Compose for a modern, declarative UI
- Uses Android's native emoji library for consistent rendering
- Optimized for performance on various Android devices
- Minimal memory usage with no persistent storage requirements
- Compatible with Android 7.0 (API level 24) and above

## Getting Started

### Prerequisites

- Android Studio Arctic Fox (2021.3.1) or newer
- Kotlin 1.8.10 or newer
- Android SDK with minimum API level 21

### Installation

1. Clone the repository
2. Open the project in Android Studio
3. Run the app on an emulator or physical device

## Design Considerations

- **Child-Friendly UI**: Large touch targets and intuitive controls
- **Bright Colors**: Engaging visuals that appeal to young users
- **Simple Interactions**: Limited options to avoid overwhelming young users
- **Performance Optimization**: Smooth animations and transitions
- **No Ads or In-App Purchases**: Safe for children to use independently

## Future Enhancements

- Add sound effects for emoji placement and page turning
- Implement emoji scaling options
- Add pre-designed backgrounds or themes
- Add ability to save favorite pages
- Include simple drawing tools

## License

This project is licensed under the MIT License - see the LICENSE file for details.

## Setup for Development

1. Clone the repository
2. Open the project in Android Studio
3. Run the app on an emulator or physical device

## Building for Release

### Setting up the Keystore

Before building a release version, you need to set up a keystore for signing the app:

1. Run the `generate_keystore.bat` script to create a new keystore file:
   ```
   .\generate_keystore.bat
   ```
   
   This will:
   - Generate a new keystore file in the `keystore` directory
   - Create a `keystore.properties` file with the necessary information

2. **IMPORTANT**: The `keystore.properties` file contains sensitive information and should NOT be committed to version control. It is already added to `.gitignore`.

3. Keep your keystore file and passwords in a safe place. If you lose them, you won't be able to update your app on the Play Store.

### Building an AAB (Android App Bundle)

To build an AAB file for the Google Play Store:

1. Make sure you have set up the keystore as described above.

2. Run the `build_aab.bat` script:
   ```
   .\build_aab.bat
   ```

3. The AAB file will be generated at:
   ```
   app/build/outputs/bundle/release/app-release.aab
   ```

4. Upload this file to the Google Play Console.

## Security Best Practices

- Never commit the `keystore` directory or `keystore.properties` file to version control
- Use strong, unique passwords for your keystore
- Store your keystore file and passwords in a secure location (e.g., a password manager)
- Consider using environment variables for CI/CD pipelines

## Troubleshooting

If you encounter issues with the keystore:

1. Make sure the `keystore.properties` file exists and contains the correct information
2. Verify that the keystore file exists at the path specified in `keystore.properties`
3. Check that the passwords in `keystore.properties` match the ones used to create the keystore 