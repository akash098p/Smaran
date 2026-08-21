# 🧠 Smaran

**Remember. Act. Complete.**

Smaran (स्मरण — *remembrance*) is a modern, lightweight, offline-first Android task reminder app. It helps you create tasks, receive reliable reminders, snooze or reschedule unfinished work, and keep a complete history of your productivity.

> A task should have a history, not just a checkbox.

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

## 🚀 Getting Started

### Prerequisites

- Android Studio (latest stable)
- JDK 17+

### Build

```bash
./gradlew assembleDebug
```

### Install

```bash
./gradlew installDebug
```

---

## 📁 Project Structure

```
app/
├── data/          # Room database, repositories, preferences
├── domain/        # Models and use cases
├── reminder/      # Alarm scheduling & notifications
├── ui/            # Compose screens (home, add, details, calendar, …)
├── navigation/
└── theme/
```

---

## 🗺️ Roadmap

- [x] Project foundation (Compose, Material 3, navigation)
- [x] Task management (create, edit, delete, statuses)
- [x] Reminder engine (snooze, reschedule, cancel)
- [x] Room database & repository layer
- [ ] Task history & timeline
- [ ] Recurring tasks
- [ ] Calendar & statistics
- [ ] Backup & export
- [ ] Polish (animations, accessibility, dark mode)

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

## 👨‍💻 Author

**Akash Pramanik** — [@akash098p](https://github.com/akash098p)

Repository: [Smaran](https://github.com/akash098p/Smaran)

---

<div align="center">

**Built to help you remember what matters.**

⭐ Star the repository if you like the idea.

</div>