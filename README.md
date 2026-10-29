# Tappi - Kunal's Sassy Wife AI (Android)

A high-performance Android AI assistant app featuring **Tappi**, Kunal's witty, sharp-tongued, and brilliant wife persona. Built with **Kotlin**, **Jetpack Compose**, and Material Design 3.

## ✨ Features Ported

1. **Sassy Wife AI Persona:**
   - Powered by Gemini 2.5 with Google Search grounding.
   - Sassy, witty, bossy wife persona that remembers she is Kunal's "better half".
   - Escalating annoyance levels on repetitive regenerations ("Regenerate (Tappi is annoyed)").
2. **Animated Splash Screen:**
   - Custom avatar with bounce animation, "Boss Mode On" badge, witty quote, and pulsing indicator dots.
3. **Interactive Chat:**
   - Chat history with auto-scroll and distinct styling for Kunal (emerald-teal gradient) and Tappi (warm surface with orange accent).
   - Real-time search filter across all messages in conversation history.
   - Thinking indicator with animated bouncing dots.
4. **Voice & Audio Capabilities:**
   - Offline & online Speech-to-Text (STT) voice input via Android `RecognizerIntent`.
   - Text-to-Speech (TTS) voice playback of Tappi's responses with sassy pitch and speech rate.
5. **Wisdom & Organization:**
   - "Tappi's Saved Wisdom": Star quotes and save them to the sidebar with quick delete.
   - Export & copy all messages transcript to Android clipboard.
   - Clear chat history with sassy confirmation dialog.
   - Dark Mode / Light Mode support and Google Search grounding toggle.
6. **Adaptive App Icon:**
   - Custom Material You adaptive icon generated with `@drawable/tappi_avatar` on `#EA580C`.

## 🛠️ Tech Stack & Architecture

- **Language:** Kotlin
- **UI Framework:** Jetpack Compose (Material Design 3)
- **Architecture:** MVVM with `StateFlow` and Coroutines
- **Networking:** OkHttp & REST API with Gemini 2.5 Flash
- **Image Loading:** Coil Compose
- **Build System:** Gradle (Kotlin DSL) with Version Catalog (`libs.versions.toml`)

## 🚀 Building & Running

1. Open project in Android Studio (Ladybug or newer).
2. Set your `GEMINI_API_KEY` in the Secrets panel in AI Studio or via environment variable.
3. Run on an Android device or emulator running Android 8.0+ (API 26+).
