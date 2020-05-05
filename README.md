# Loomo Voice Assistant

This app demonstrates basic speech assistant functionality for the Segway Loomo Robot. It can be used both offline and online (via Dialogflow and Google Cloud services). It also provides interfaces to control the robot movement via speech input.

## Installation

1. Clone the repository
2. Create an API Key for Google Cloud TTS and add the key to [gradle.properties](gradle.properties) (you may need to create the file first)
3. Create a service account for your Google Cloud project and download the [credentials.json](app/src/main/res/raw/credential.json) and save it to your [raw](app/src/main/res/raw) folder.
4. Build the app and have fun. :)

## Disclaimer

This app was built as part of an university project. There may be bugs, please use with caution.
