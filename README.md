# 🧠 Smaran

**Remember. Act. Complete.**

Smaran (स्मरण — *remembrance*) is a modern, lightweight, offline-first Android task reminder app. It helps you create tasks, receive reliable reminders, snooze or reschedule unfinished work, and keep a complete history of your productivity.


---

## ✨ Features

- 🔔 **Actionable reminders** — snooze (15/30/60 min) or reschedule directly from the notification
- 📅 **Task scheduling** — date, time, priority, category, and notes
- 🔁 **Recurring tasks** — daily, weekly, monthly, and custom rules
- 📜 **Complete task history** — every snooze, reschedule, and completion is tracked
- 📊 **Productivity insights** — completion rates, streaks, and statistics
- 📴 **Offline-first** — no account, no cloud, no internet required
- 🔐 **Privacy-focused** — all data stays on-device

---

## 📹 Live Demo

<p align="center">
  <img src="https://github.com/user-attachments/assets/c56fdedd-3014-46a5-97b3-c9ee303df9b5" width="49%">
  <img src="https://github.com/user-attachments/assets/5d6e8eb1-67ba-41ae-abcc-f665eca46959" width="49%">
</p>




---

## 🛠️ Tech Stack

| Technology | Purpose |
|---|---|
| Kotlin | Primary language |
| Jetpack Compose | Modern UI |
| Material 3 | Design system |
| Room | Local database |
| MVVM + ViewModel | Architecture |
| Coroutines / Flow | Async & reactive state |
| AlarmManager | Scheduled reminders |
| Notification API | Reminder notifications |
| Navigation Compose | Navigation |
| DataStore | Preferences |

---

### 🧰 Technologies & Capabilities

<p align="center">
  <img src="https://img.shields.io/badge/Kotlin-7F52FF?style=for-the-badge&logo=kotlin&logoColor=white" alt="Kotlin" />
  <img src="https://img.shields.io/badge/Jetpack_Compose-4285F4?style=for-the-badge&logo=android&logoColor=white" alt="Jetpack Compose" />
  <img src="https://img.shields.io/badge/Material_3-6750A4?style=for-the-badge&logo=materialdesign&logoColor=white" alt="Material 3" />
  <img src="https://img.shields.io/badge/Room-3DDC84?style=for-the-badge&logo=android&logoColor=white" alt="Room" />
  <img src="https://img.shields.io/badge/Offline--First-2E7D32?style=for-the-badge&logo=android&logoColor=white" alt="Offline First" />
  <img src="https://img.shields.io/badge/Mobile-Responsive-00A67E?style=for-the-badge&logo=android&logoColor=white" alt="Mobile" />
</p>

---

## 🚀 Getting Started

### Prerequisites

- **Android Studio** (latest stable)
- **JDK 17+**

### Build & Install

```bash
# Build the app
./gradlew assembleDebug

# Install on a connected device
./gradlew installDebug
```

### Project Requirements

- **Android SDK 26+** (min) / **35** (target)
- **Java 17** compatibility
- **Kotlin 2.0+** with Compose plugin

---

## 📁 Project Structure

```
app/src/main/java/com/smaran/app/
├── core/          # Navigation, constants, extensions, validation, result types
├── data/          # Local storage, mappers, models, preferences, repositories
├── domain/        # Domain models, use cases, repository interfaces
├── home/          # Home screen (components, UI, view model)
├── profile/       # Profile preferences
├── reminder/      # Alarm scheduling, notifications, receivers, rescheduling
├── settings/      # App, appearance, and reminder settings
├── task/          # Task lifecycle (create, edit, delete, complete, snooze, reschedule, recurring)
└── ui/            # Shared UI — history, task details
```

---

## 🤝 Contributing

Ideas, bug reports, and pull requests are welcome.

For bugs, include:
1. What happened
2. Expected behavior
3. Steps to reproduce
4. Android version / device

For feature requests, describe the problem it solves and how you expect it to work.

---

## 🧑‍💻 Developer

**Akash Pramanik**

<p>
  <strong>For questions or support: </strong>
<a href="https://instagram.com/akash.098p" target="_blank">
  <img src="https://img.shields.io/badge/akash.098p-E4405F?style=flat&logo=instagram&logoColor=white"/>
</a>

<a href="mailto:akashpramanik098@gmail.com">
  <img src="https://img.shields.io/badge/akashpramanik422%40gmail.com-D14836?style=flat&logo=gmail&logoColor=white"/>
</a>
</p>

---
