# LUXX PEQ

Offline-first Android parametric EQ interface and response-curve prototype.

## Build APK
Run the GitHub Actions workflow **Build LUXX PEQ APK**. Download the `luxx-peq-debug` artifact.

## Current scope
- 10 editable bands
- Gain: -20 dB to +20 dB
- Frequency: 20 Hz to 20 kHz
- Q: 0.1 to 10
- Peak, low shelf, high shelf, notch
- Preamp and combined response graph
- No network permission or backend

## Important audio limitation
This first build is a PEQ design/response-preview prototype. It calculates the filter response but does **not yet process audio from Spotify, Tidal, or other apps**. A real-time audio engine and Android audio routing/effect integration are a separate stage. USB DAC/system-wide processing depends on Android device routing and cannot be guaranteed by a standalone UI app.
