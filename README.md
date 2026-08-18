# 🧠 Smaran

### Remember. Act. Complete.

**Smaran** is a modern, lightweight, offline-first Android task reminder and scheduling application designed to help users remember important tasks, receive reliable reminders, snooze or reschedule unfinished tasks, and maintain a complete history of their productivity.

The name **Smaran (स्मरण)** comes from Sanskrit and represents **remembrance, remembering, or recalling** — the core purpose of the application.

---

## ✨ What is Smaran?

Most to-do applications stop at a simple checklist:

```text
☐ Study Java
```

Smaran is designed around the complete lifecycle of a task:

```text
Create Task
     ↓
Schedule
     ↓
Reminder Triggered
     ↓
 ┌───────────────┬────────────────┬────────────────┐
 ↓               ↓                ↓
DONE           SNOOZE         RESCHEDULE
 ↓               ↓                ↓
Completed    New Reminder      New Schedule
                 ↓                ↓
              Reminder        Reminder
                                 ↓
                              Completed
                                 ↓
                              History
```

The idea is simple:

> **A task should have a history, not just a checkbox.**

---

# 🎯 Project Goals

Smaran aims to provide a fast and reliable way to manage time-sensitive tasks without unnecessary complexity.

- 🔔 Reliable task reminders
- ⏰ Quick snooze actions
- 📅 Easy rescheduling
- ✅ Completion tracking
- ⏳ Pending and missed task tracking
- 📜 Detailed task history
- 🔁 Recurring tasks
- 📴 Offline-first operation
- 🪶 Lightweight architecture
- 🎨 Modern Android UI
- 🔐 Privacy-focused local storage
- 📊 Productivity insights

---

# 🚀 Core Features

## 📝 1. Create a Task

Create a task with:

- Task title
- Description
- Date
- Time
- Priority
- Category
- Notes
- Repeat settings
- Reminder settings

Example:

```text
Task: Submit DBMS Assignment
Date: 20 August 2026
Time: 10:00 AM
Priority: High
Category: College
```

---

## 🔔 2. Actionable Reminders

When the scheduled time arrives, Smaran should provide an actionable reminder instead of a passive notification.

```text
🔔 REMINDER

Submit DBMS Assignment

Scheduled: 10:00 AM

[ ✓ DONE ]

[ 15 MIN ] [ 30 MIN ]
[ 1 HOUR ]

[ 📅 RESCHEDULE ]
```

The main reminder actions should be available directly from the notification or reminder screen whenever Android permits the interaction.

---

## ⏱️ 3. Quick Snooze

Users can postpone a reminder with one tap:

- 15 minutes
- 30 minutes
- 1 hour

Possible future options:

- 5 minutes
- 2 hours
- Tonight
- Tomorrow
- Custom duration

Example:

```text
10:00 AM
   ↓
Reminder
   ↓
30 Minute Snooze
   ↓
10:30 AM
   ↓
Reminder Again
```

---

## 📅 4. Reschedule Unfinished Tasks

If a user cannot complete a task, they can create a new schedule without losing the previous reminder history.

Example:

```text
Study Java

Original:
19 August — 8:00 PM

Reschedule:
→ Tomorrow
→ Next Week
→ Custom Date
→ Custom Time
```

The old event remains in the task timeline.

---

## 🔁 5. Recurring Tasks

Support recurring tasks such as:

- Every day
- Every weekday
- Every week
- Every month
- Selected days of the week
- Custom recurrence rules

Example:

```text
Study Java
Monday • Wednesday • Friday
8:00 PM
```

Future recurrence options could include:

```text
Every 2 days
Every 3 weeks
First Monday of every month
Last day of every month
```

---

## 📜 6. Complete Task History

Smaran should record meaningful task events instead of only saving the final state.

Example:

```text
Submit Assignment

10:00 AM
Reminder triggered

10:02 AM
Snoozed for 30 minutes

10:30 AM
Reminder triggered

10:35 AM
Rescheduled

22 August — 10:00 AM
Reminder triggered

22 August — 10:08 AM
Completed ✅
```

This timeline is a core feature of Smaran.

---

## 📌 7. Task Statuses

Recommended task states:

```text
DRAFT
SCHEDULED
REMINDER_TRIGGERED
SNOOZED
RESCHEDULED
COMPLETED
MISSED
CANCELLED
```

The exact implementation may evolve as development progresses.

---

## 📋 8. Task Filters

Users should be able to view tasks by:

- All
- Today
- Upcoming
- Completed
- Pending
- Missed
- Rescheduled
- High Priority
- Category

---

## 🗓️ 9. Calendar View

A calendar view can show scheduled tasks by date.

```text
        AUGUST 2026

 M   T   W   T   F   S   S
17  18  19  20  21  22  23
        •   •       •

19 AUGUST

08:00 AM  🔔 Study Java
10:00 AM  🔔 Submit Assignment
06:00 PM  🔔 Call College
```

---

## ⚠️ 10. Missed Tasks

If a reminder is not completed or handled, Smaran can mark the task as missed according to the configured behavior.

```text
⚠️ You missed this task.

[ Complete ]
[ Reschedule ]
[ Delete ]
```

---

## 🔄 11. Unfinished Task Review

Smaran can provide a simple summary of unfinished tasks.

```text
You have 3 unfinished tasks.

❌ Submit Assignment
❌ Study Java
❌ Call College

[ RESCHEDULE ALL ]
```

A future version may suggest new times based on the user's preferences or previous behavior.

---

## ⭐ 12. Priority System

Tasks can have priorities such as:

- 🔴 High
- 🟡 Medium
- 🟢 Low

Priority can be used for sorting, dashboard display, and future statistics.

---

## 🏷️ 13. Categories

Suggested categories:

- 🎓 College
- 💼 Work
- 🏠 Personal
- 🛒 Shopping
- 💰 Finance
- 📚 Study
- 🏃 Fitness
- 📝 Important
- 📦 Other

Users should also be able to create custom categories.

---

## 📊 14. Productivity Statistics

Smaran can provide useful insights without becoming overly complicated.

Example:

```text
THIS WEEK

Completed       24
Pending          5
Missed           2
Rescheduled     11

Completion Rate
82%
```

Possible metrics:

- Daily completion rate
- Weekly completion rate
- Monthly completion rate
- Most productive day
- Most productive time
- Snooze count
- Reschedule count
- Missed tasks
- Completion streak
- Category performance

---

## 🔥 15. Completion Streaks

Optional motivation feature:

```text
🔥 7 Day Completion Streak

Mon ✓
Tue ✓
Wed ✓
Thu ✓
Fri ✓
Sat ✓
Sun ✓
```

---

# 🧱 Android-First Architecture

The initial Smaran release is intentionally designed as a **standalone Android application**.

```text
                 ┌─────────────────────┐
                 │       UI Layer      │
                 │    Jetpack Compose  │
                 └──────────┬──────────┘
                            │
                            ▼
                 ┌─────────────────────┐
                 │    ViewModel Layer  │
                 │        MVVM         │
                 └──────────┬──────────┘
                            │
                            ▼
                 ┌─────────────────────┐
                 │   Domain / Logic    │
                 │ Task + Reminder     │
                 └──────────┬──────────┘
                            │
               ┌────────────┴────────────┐
               ▼                         ▼
       ┌────────────────┐       ┌──────────────────┐
       │   Repository   │       │ Reminder Engine  │
       └───────┬────────┘       └────────┬─────────┘
               │                         │
               ▼                         ▼
       ┌────────────────┐       ┌──────────────────┐
       │ Room Database  │       │ Android Alarm &  │
       │                │       │ Notifications    │
       └────────────────┘       └──────────────────┘
```

---

# 🛠️ Technology Stack

| Technology | Purpose |
|---|---|
| **Kotlin** | Primary language |
| **Jetpack Compose** | Modern Android UI |
| **Material 3** | Design system |
| **Room** | Local database |
| **MVVM** | App architecture |
| **ViewModel** | UI state management |
| **Coroutines** | Async operations |
| **Flow / StateFlow** | Reactive state |
| **AlarmManager** | Scheduled reminders |
| **Notification API** | Reminder notifications |
| **Navigation Compose** | Navigation |
| **DataStore** | Preferences |
| **Android SDK** | Platform APIs |

Library versions will be documented as implementation progresses.

---

# 📴 Offline-First Design

The core app should work without internet access.

Available offline:

- Create tasks
- Edit tasks
- Delete tasks
- Schedule reminders
- Snooze reminders
- Reschedule tasks
- Complete tasks
- View history
- View calendar
- View statistics

The MVP does not require:

- ❌ Account creation
- ❌ Cloud server
- ❌ Telegram
- ❌ Web dashboard
- ❌ Continuous internet access

This keeps the application lightweight and dependable.

---

# 🔋 Battery & Reminder Philosophy

A reminder application should not continuously run a timer in the background.

Conceptually:

```text
Create Task
     ↓
Schedule Alarm
     ↓
App becomes idle
     ↓
Android wakes the relevant component
     ↓
Reminder / Notification
```

The implementation should use Android's appropriate scheduling mechanisms and account for platform restrictions instead of polling every second.

---

# 🗃️ Proposed Database Model

## Tasks

```text
Task
├── id
├── title
├── description
├── scheduledDate
├── scheduledTime
├── priority
├── category
├── status
├── recurrenceRule
├── createdAt
├── updatedAt
└── completedAt
```

## Reminders

```text
Reminder
├── id
├── taskId
├── reminderTime
├── reminderType
├── status
├── triggeredAt
└── completedAt
```

## Task History

```text
TaskHistory
├── id
├── taskId
├── action
├── timestamp
├── previousDate
├── previousTime
├── newDate
└── newTime
```

The schema is a starting design and may evolve during implementation.

---

# 📱 Planned Screens

## 1. Home / Dashboard

Should show:

- Today's tasks
- Next upcoming reminder
- Pending tasks
- Overdue/missed tasks
- Quick Add button
- Productivity summary

## 2. Add Task

```text
Task Title
Description
Date
Time
Priority
Category
Repeat
Reminder settings

[ SAVE TASK ]
```

## 3. Task Details

Show task data, status, schedule, and the full task timeline.

## 4. Reminder Screen

Focused on fast decisions:

```text
🔔 REMINDER

Task Name

[ DONE ]

[ 15 MIN ] [ 30 MIN ] [ 1 HOUR ]

[ RESCHEDULE ]
```

## 5. Calendar

Browse tasks by date.

## 6. History

Browse completed, pending, missed, snoozed, and rescheduled activity.

## 7. Statistics

Show productivity metrics and trends.

## 8. Settings

Possible settings:

- Theme
- Default snooze duration
- Reminder sound
- Vibration
- Notification behavior
- Date format
- Time format
- Backup / export
- Privacy options

---

# 🎨 Design Philosophy

### Simple

A reminder should be created in seconds.

### Modern

Clean Material 3 UI, clear hierarchy, and purposeful animation.

### Lightweight

Avoid unnecessary services, dependencies, and background work.

### Reliable

Reminder behavior is a core feature, not an afterthought.

### Private

Personal task data should stay local by default.

### Offline-first

Core functionality should not require internet access.

---

# 🧠 Future Smart Features

## Smart Rescheduling

If a task is repeatedly postponed:

```text
Study Java
8:00 PM → Snoozed
8:30 PM → Snoozed
9:00 PM → Snoozed
```

Smaran could suggest:

> You have postponed this task 3 times. Schedule it for tomorrow at 8:00 PM?

## Smart Time Suggestions

Based on local task history, Smaran could eventually suggest a useful time for new tasks.

Example:

```text
You often complete study tasks
between 7 PM and 9 PM.

Suggested time: 8:00 PM
```

## Daily Planning

```text
Good morning 👋

You have 7 tasks today.

High Priority: 2
Medium Priority: 3
Low Priority: 2
```

## End-of-Day Review

```text
DAY SUMMARY

Completed: 6
Pending: 2
Missed: 1
Rescheduled: 2

[ PLAN TOMORROW ]
```

---

# 📤 Backup & Export

Possible future support:

- JSON export
- CSV export
- Local backup
- Restore backup
- Android backup integration

---

# ☁️ Optional Future Cloud Sync

Cloud synchronization is not required for the core app.

A future version could optionally support:

```text
Android
   ↓
Cloud API
   ↓
Secure Database
   ↓
Other Devices
```

Possible future technologies include a REST API, Firebase, Supabase, or a custom backend.

---

# 🤖 Optional Telegram Integration

Telegram is **not part of the core MVP**.

It may be introduced later as an optional notification and remote-action channel.

Possible workflow:

```text
Smaran
   ↓
Optional Backend
   ↓
Telegram Bot
   ↓
🔔 Task Reminder
```

Telegram could offer actions such as:

```text
[ DONE ]
[ 15 MIN ]
[ 30 MIN ]
[ 1 HOUR ]
[ RESCHEDULE ]
```

The Android app must remain functional without Telegram.

---

# 🌐 Optional Future Web Dashboard

A future web dashboard could provide cross-device management:

```text
                 Smaran Backend
                       │
             ┌─────────┴─────────┐
             │                   │
          Android              Web
             │                   │
             └────── Sync ──────┘
```

Possible features:

- Task management
- Calendar
- History
- Statistics
- Cloud sync
- Account management

This is an optional expansion, not an MVP dependency.

---

# 🗺️ Development Roadmap

## Phase 1 — Foundation

- [ ] Initialize Android project
- [ ] Kotlin configuration
- [ ] Jetpack Compose setup
- [ ] Material 3 theme
- [ ] Project architecture
- [ ] Navigation
- [ ] Theme system

## Phase 2 — Task Management

- [ ] Create task
- [ ] Edit task
- [ ] Delete task
- [ ] View task
- [ ] Task status
- [ ] Priority
- [ ] Categories
- [ ] Date selection
- [ ] Time selection

## Phase 3 — Reminder Engine

- [ ] Notification channel
- [ ] Schedule reminder
- [ ] Trigger reminder
- [ ] Reminder sound
- [ ] Vibration
- [ ] 15-minute snooze
- [ ] 30-minute snooze
- [ ] 1-hour snooze
- [ ] Cancel reminder
- [ ] Reschedule reminder

## Phase 4 — Database

- [ ] Room database
- [ ] Task entity
- [ ] Reminder entity
- [ ] History entity
- [ ] DAO implementation
- [ ] Repository layer
- [ ] Migration strategy

## Phase 5 — History

- [ ] Completed history
- [ ] Pending history
- [ ] Missed tasks
- [ ] Snooze history
- [ ] Reschedule history
- [ ] Task timeline
- [ ] History filtering

## Phase 6 — Recurring Tasks

- [ ] Daily recurrence
- [ ] Weekly recurrence
- [ ] Monthly recurrence
- [ ] Weekday recurrence
- [ ] Custom recurrence
- [ ] Recurring task management

## Phase 7 — Calendar & Statistics

- [ ] Calendar screen
- [ ] Daily summary
- [ ] Weekly summary
- [ ] Monthly statistics
- [ ] Completion rate
- [ ] Streaks
- [ ] Category statistics

## Phase 8 — Polish

- [ ] Animations
- [ ] Empty states
- [ ] Error handling
- [ ] Accessibility
- [ ] Performance optimization
- [ ] Battery optimization
- [ ] Notification customization
- [ ] Dark mode
- [ ] Light mode

## Phase 9 — Backup & Intelligence

- [ ] JSON backup
- [ ] CSV export
- [ ] Restore backup
- [ ] Smart rescheduling
- [ ] Daily planning
- [ ] End-of-day review

## Phase 10 — Optional Ecosystem

- [ ] Cloud sync
- [ ] User accounts
- [ ] Multi-device support
- [ ] Web dashboard
- [ ] Telegram integration
- [ ] Secure API
- [ ] Optional cloud backup

---

# 📂 Proposed Project Structure

```text
Smaran/
│
├── app/
│
├── data/
│   ├── database/
│   │   ├── dao/
│   │   ├── entity/
│   │   └── SmaranDatabase
│   ├── repository/
│   └── preferences/
│
├── domain/
│   ├── model/
│   └── usecase/
│
├── reminder/
│   ├── AlarmScheduler
│   ├── ReminderReceiver
│   └── NotificationHelper
│
├── ui/
│   ├── home/
│   ├── addtask/
│   ├── taskdetails/
│   ├── reminder/
│   ├── calendar/
│   ├── history/
│   ├── statistics/
│   └── settings/
│
├── navigation/
├── theme/
└── MainActivity
```

The structure can be refined during implementation.

---

# 🧪 Testing Strategy

## Unit Tests

Test:

- Task creation
- Task status transitions
- Snooze calculations
- Rescheduling logic
- Recurrence logic
- History generation

## Database Tests

Test:

- Insert
- Update
- Delete
- Queries
- Relationships
- Migrations

## Reminder Tests

Test:

- Correct alarm time
- Snooze calculation
- Rescheduling
- Cancellation
- Device reboot recovery
- Time/date changes

## UI Tests

Test:

- Create task
- Complete task
- Snooze task
- Reschedule task
- Open history
- Calendar navigation

---

# 🛡️ Android Reliability Considerations

Because reminders are the main purpose of the app, the implementation should account for Android behavior around:

- Notification permissions
- Alarm permissions where applicable
- Notification channels
- Battery optimization
- Doze mode
- Background restrictions
- Device reboot
- Time zone changes
- System clock changes
- Daylight-saving changes where relevant

Reminder scheduling should be restored or reconciled when appropriate after system events.

---

# 📋 Example User Journey

```text
1. Open Smaran
        ↓
2. Create "Submit Assignment"
        ↓
3. Choose 20 August — 10:00 AM
        ↓
4. Smaran schedules the reminder
        ↓
5. 10:00 AM → 🔔 Reminder
        ↓
6. User selects 30 MIN
        ↓
7. Smaran schedules 10:30 AM
        ↓
8. 10:30 AM → 🔔 Reminder
        ↓
9. User selects RESCHEDULE
        ↓
10. New schedule: 22 August — 10:00 AM
        ↓
11. Reminder appears again
        ↓
12. User selects DONE
        ↓
13. Task becomes COMPLETED
        ↓
14. Full timeline remains in Task History
```

---

# 💡 The Core Product Idea

Smaran is built around one simple principle:

> **Remember → Schedule → Act → Reschedule → Complete → Learn**

The goal is not to create another complicated productivity platform.

The goal is to create a **lightweight, reliable personal reminder system** that helps users make sure important things are not forgotten.

---

# 🚧 Project Status

**Smaran is currently in the early planning/foundation stage.**

The initial development focus is:

```text
Android App
    ↓
Task Management
    ↓
Room Database
    ↓
Reliable Reminders
    ↓
15 / 30 / 60 Minute Snooze
    ↓
Rescheduling
    ↓
Task History
```

Cloud sync, Telegram, and web functionality are intentionally kept as optional future phases so the core Android application remains lightweight.

---

# 🤝 Contributing

Ideas, improvements, bug reports, and pull requests are welcome.

For bugs, please include:

1. What happened
2. Expected behavior
3. Steps to reproduce
4. Android version/device information when relevant

For feature requests, describe the problem the feature should solve and how you expect it to work.

---

# 👨‍💻 Author

**Akash Pramanik**

GitHub: [@akash098p](https://github.com/akash098p)

Repository: [Smaran](https://github.com/akash098p/Smaran)

---

<div align="center">

## 🧠 Smaran

### Remember. Act. Complete.

**Built to help you remember what matters.**

⭐ Star the repository if you like the idea.

</div>
