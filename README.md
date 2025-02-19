# Editz - Advanced Video Editor

<div align="center">
  <img src="app/src/main/res/mipmap-xxxhdpi/ic_launcher_round.png" alt="Editz Logo" width="120"/>
  
  ![Android](https://img.shields.io/badge/Android-3DDC84?style=for-the-badge&logo=android&logoColor=white)
  ![Kotlin](https://img.shields.io/badge/Kotlin-0095D5?style=for-the-badge&logo=kotlin&logoColor=white)
  ![Jetpack Compose](https://img.shields.io/badge/Jetpack%20Compose-4285F4?style=for-the-badge&logo=jetpackcompose&logoColor=white)
</div>

## 🎥 Overview

Editz is a powerful, modern video editing application for Android that combines professional-grade features with an intuitive user interface. Built with the latest Android technologies, it offers a seamless video editing experience with real-time previews and advanced editing capabilities.

## ✨ Features

### 🎬 Core Editing
- **Video Trimming**: Frame-accurate video cutting
- **Speed Control**: Adjust video playback speed (0.25x to 2x)
- **Volume Adjustment**: Fine-tune audio levels
- **Rotation**: 90-degree incremental rotation

### 🎨 Visual Effects
#### Filters
- Original
- Vintage (70% intensity)
- Dramatic (120% intensity)
- Cool (80% intensity)
- Warm (80% intensity)
- Vibrant (130% intensity)
- Muted (60% intensity)

#### Effects
- Blur
- Vignette
- Grain
- Glitch
- Pixelate

### 🛠️ Advanced Tools
- **Stitch**: Combine multiple videos
- **Mask**: Apply creative masks
- **Opacity**: Adjust transparency
- **Replace**: Smart content replacement
- **Voice Effect**: Audio modifications
- **Duplicate**: Clone segments
- **Rotate**: Orientation adjustments

## 🏗️ Technical Architecture

### Built With
- **UI**: Jetpack Compose with Material Design 3
- **Video Playback**: ExoPlayer
- **Processing**: MediaCodec for hardware-accelerated encoding
- **Architecture**: MVVM with Clean Architecture
- **Dependency Injection**: Hilt
- **State Management**: Kotlin Flow & State
- **Threading**: Coroutines for async operations

### Project Structure
```
app/
├── data/
│   └── VideoDetails.kt
├── theme/
│   └── EditzColors.kt
├── ui/
│   ├── editor/
│   │   ├── components/
│   │   ├── model/
│   │   └── VideoEditorScreen.kt
│   ├── preview/
│   │   └── VideoPreviewScreen.kt
│   └── home/
│       └── HomeActivity.kt
└── utils/
    └── VideoProcessor.kt
```

## 🚀 Getting Started

### Prerequisites
- Android Studio Arctic Fox or later
- Android SDK 21 or higher
- Kotlin 1.5.0 or higher

### Installation
1. Clone the repository
```bash
git clone https://github.com/yourusername/editz.git
```

2. Open in Android Studio

3. Sync Gradle and run the app

## 📱 Screenshots

[Screenshots will be added here]

## 🎯 Upcoming Features
- [ ] AI-powered video enhancement
- [ ] Cloud backup integration
- [ ] Advanced transition effects
- [ ] Multi-layer composition
- [ ] Export presets

## 🤝 Contributing
Contributions are welcome! Please feel free to submit a Pull Request.

## 📄 License
This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## 🙏 Acknowledgments
- ExoPlayer for video playback
- Material Design for UI components
- Android Jetpack libraries

## 📞 Contact
For support or queries, reach out to us at [support@editz.com](mailto:support@editz.com)

---
Made with ❤️ by the Editz Team
