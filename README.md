# 🧠 Smaran

**Remember. Act. Complete.**

Smaran (स्मरण — *remembrance*) is a modern, lightweight, offline-first Android task reminder app. Create tasks, receive reliable reminders, snooze or reschedule unfinished work, and keep a complete history of your productivity.

## ✨ Features

- 🔔 **Actionable reminders** — snooze (15/30/60 min) or reschedule directly from the notification
- 📅 **Task scheduling** — date, time, priority, category, and notes
- 🔁 **Recurring tasks** — daily, weekly, monthly, and custom rules
- 📜 **Complete task history** — every snooze, reschedule, and completion is tracked
- 📊 **Productivity insights** — completion rates, streaks, and statistics
- 📴 **Offline-first** — no account, no cloud, no internet required
- 🔐 **Privacy-focused** — all data stays on-device

## 📸 Demo


## 🛠️ Tech Stack

Kotlin · Jetpack Compose · Material 3 · Room · MVVM · Coroutines/Flow · AlarmManager · Navigation Compose · DataStore

## 🚀 Getting Started

**Prerequisites:** Android Studio (latest stable) · JDK 17+

```bash
# Build the app
./gradlew assembleDebug

# Install on a connected device
./gradlew installDebug
```

**Requirements:** Android SDK 26+ (min) / 35 (target) · Java 17 · Kotlin 2.0+ with Compose plugin

## 📁 Project Structure

```
app/src/main/java/com/smaran/app/
├── core/          # Navigation, constants, extensions, validation
├── data/          # Local storage, mappers, models, repositories
├── domain/        # Domain models, use cases, repository interfaces
├── home/          # Home screen
├── profile/       # Profile preferences
├── reminder/      # Alarm scheduling, notifications, receivers
├── settings/      # App, appearance, and reminder settings
├── task/          # Task lifecycle (create, edit, complete, snooze, recurring)
└── ui/            # Shared UI — history, task details
```

## 🤝 Contributing

Ideas, bug reports, and pull requests are welcome. For bugs, include what happened, expected behavior, steps to reproduce, and Android version/device.

## 🧑‍💻 Developer

**Akash Pramanik**

[![Instagram](https://img.shields.io/badge/akash.098p-E4405F?style=flat&logo=instagram&logoColor=white)](https://instagram.com/akash.098p)
[![Email](https://img.shields.io/badge/akashpramanik098%40gmail.com-D14836?style=flat&logo=gmail&logoColor=white)](mailto:akashpramanik098@gmail.com)