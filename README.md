# ALS-Battery

A battery monitoring application for Android that helps users track their device's battery health, charging patterns, and usage. Built with modern Android development practices using Jetpack Compose.

## Overview

ALS-Battery provides real-time monitoring of your device's battery health, charging patterns, and usage statistics through an intuitive Material 3 interface. The app features four main sections: Health, Charging, Discharging, and History.

## Features

### Battery Health Screen
- Battery health percentage with circular indicator
- Current and design capacity comparison
- Temperature monitoring with status
- Charge cycles tracking
- Smart health recommendations

### Charging Screen
- Real-time battery percentage with visual circle
- Battery model information
- Current voltage and temperature
- Charging duration estimation
- Cycle wear monitoring

### Discharging Screen
- Current battery usage stats
- Deep sleep consumption
- Screen on/off power usage
- Battery drain analysis
- Time remaining estimation

### History Screen
- Daily/Weekly/Monthly views
- Interactive battery level charts
- Temperature trends
- Charging session history
- Detailed usage statistics

## Technical Stack

### Core Technologies
- Kotlin
- Jetpack Compose
- Material Design 3
- Room Database
- Kotlin Coroutines & Flow
- MVVM Architecture

### Key Dependencies
```gradle
dependencies {
    def roomVersion = "2.6.1"
    
    implementation("androidx.room:room-runtime:$roomVersion")
    implementation("androidx.room:room-ktx:$roomVersion")
    implementation("androidx.compose.material3:material3:1.1.2")
    implementation("com.patrykandpatrick.vico:compose:1.12.0")
    implementation("com.patrykandpatrick.vico:compose-m3:1.12.0")
}
```

## Requirements
- Android 7.0 (API 24) or higher
- Gradle 8.2.2 or higher
- Android Studio Hedgehog or newer

## Setup Instructions

1. Clone the repository
2. Open project in Android Studio
3. Sync Gradle files
4. Run on device or emulator

## Permissions Required
```xml
<uses-permission android:name="android.permission.BATTERY_STATS" />
<uses-permission android:name="android.permission.FOREGROUND_SERVICE" />
<uses-permission android:name="android.permission.READ_PHONE_STATE" />
```

## Project Structure

```
app/
├── src/main/
│   ├── java/xyz/batterychecker/
│   │   ├── database/          # Room Database
│   │   ├── screens/           # Compose UI Screens
│   │   ├── viewmodel/         # ViewModels
│   │   └── ui/theme/          # Material 3 Theme
│   └── res/                   # Resources
```

## Architecture

The app follows MVVM architecture:
- UI Layer: Compose screens
- ViewModel Layer: Business logic
- Database Layer: Room for persistence
- System Services: Battery information

## Contributing

Feel free to contribute to this project:
1. Fork the repository
2. Create your feature branch
3. Commit your changes
4. Push to the branch
5. Create a Pull Request

## License

MIT License

Copyright (c) 2024 ALS-Battery

Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction.

The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED.

---

For more information or support, please open an issue in the repository.
