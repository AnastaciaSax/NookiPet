# NookiPet 🐾

**NookiPet** is a comprehensive Android application designed for pet owners to manage the daily care, health, and documentation of their pets. It supports a wide variety of animals — from common cats and dogs to exotic reptiles, birds, fish, and even snails.

The app was built as a semester-long project, integrating modern Android development practices, from basic UI to complex architectural patterns.

## ✨ Key Features

- **Multi-Pet Support**: Manage multiple pets. Each pet gets its own profile, events, reminders, and documents.
- **Dynamic UI Palettes**: The entire app's theme (Toolbar, system bars, FABs, charts, and atmospheric background) dynamically changes its color palette based on the currently selected pet.
- **Event Tracking**: Log daily events like feeding, misting, cleaning, medications, and vet visits with ease.
- **Smart Reminders**: Schedule reminders for specific times. Integrated with `WorkManager` to deliver reliable notifications, even when the app is closed.
- **Statistics & Charts**: Custom-built, animated bar charts display feeding frequencies and real weight dynamics over Week, Month, and Year periods.
- **Document Management**: Securely attach real files (vet records, photos, test results) to a pet's profile using the system file picker. Files are copied to app-private storage and can be opened via external viewers.
- **Vet Booking & Clinics**: Book a vet visit (saved as an event) and explore a real interactive map (OpenStreetMap) showing nearby veterinary clinics.
- **Pet Care Guide**: A built-in knowledge base featuring real, actionable care tips for different species (fetched via Retrofit).
- **Dark Mode**: Full support for system-level light and dark themes.

## 🛠 Tech Stack & Architecture

The project is built on the **MVVM (Model-View-ViewModel)** architecture and strictly follows modern Android guidelines.

- **Language**: Kotlin
- **UI**: XML, Material Design 3, ConstraintLayout, Custom Views (`OrbBackgroundView`, `BarChartView`)
- **Architecture**: MVVM, `StateFlow`, `Flow`, Coroutines
- **Local Storage**: Room Database (4 Entities), SharedPreferences
- **Network**: Retrofit 2, Gson, OkHttp (MockInterceptor for local JSON)
- **Background Processing**: WorkManager, NotificationManager
- **Maps**: OSMDroid (OpenStreetMap)
- **Navigation**: BottomNavigationView + FragmentManager

### Project Structure
```text
app/src/main/java/eng/nookipet/lab7/
├── api/            # Retrofit interfaces and MockInterceptor
├── data/
│   ├── common/     # Resource wrappers, constants
│   ├── content/    # PetCareGuide knowledge base
│   ├── dao/        # Room Data Access Objects
│   ├── entity/     # Room Entities (Pet, Event, Reminder, PetTip)
│   └── repository/ # Repositories (Single source of truth)
├── ui/
│   ├── documents/  # Document management
│   ├── event/      # Add event form
│   ├── home/       # Main screen
│   ├── pets/       # Pet list & editing
│   ├── reminders/  # Reminders list
│   ├── settings/   # App settings
│   ├── stats/      # Charts and statistics
│   ├── theme/      # Dynamic palettes and UI coloring
│   ├── tips/       # Pet care tips
│   ├── vet/        # Vet booking and map
│   └── widget/     # Custom views (Orbs, Charts)
├── AppDatabase.kt  # Room DB setup & Seed data
├── AppPreferences.kt # SharedPreferences wrapper
├── MainActivity.kt # Single Activity container
└── NookiPetApp.kt  # Application class (OSMDroid init)
