package eng.nookipet.lab7

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.work.Data
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit
import eng.nookipet.lab7.ui.home.HomeFragment
import eng.nookipet.lab7.ui.pets.PetsFragment
import eng.nookipet.lab7.ui.reminders.RemindersFragment
import eng.nookipet.lab7.ui.settings.SettingsFragment
import eng.nookipet.lab7.ui.stats.StatsFragment
import eng.nookipet.lab7.ui.vet.BookVetFragment
import eng.nookipet.lab7.ui.vet.NearbyClinicsFragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.repeatOnLifecycle
import eng.nookipet.lab7.data.repository.SelectedPetRepository
import eng.nookipet.lab7.ui.theme.PetPaletteCatalog
import android.content.res.ColorStateList
import eng.nookipet.lab7.R
import androidx.work.ExistingWorkPolicy
import eng.nookipet.lab7.data.entity.Reminder
import java.util.Calendar
import eng.nookipet.lab7.ui.tips.PetTipsFragment
import androidx.core.view.WindowInsetsControllerCompat
import eng.nookipet.lab7.ui.theme.PetPalette

class MainActivity : AppCompatActivity() {

    private lateinit var bottomNav: BottomNavigationView
    // Toolbar — used to tint it with the active pet's palette
    private lateinit var toolbar: Toolbar

    // Permission launcher for POST_NOTIFICATIONS on Android 13+.
    private val notificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        // If granted — notifications work. If not — we silently skip.
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        applyTheme()  // before super.onCreate and setContentView
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        Log.d("MVVM", "MainActivity.onCreate called (savedInstanceState=${savedInstanceState != null})")

        NotificationHelper.createNotificationChannel(this)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }

        // Note: we assign to the class field, not a local variable
        toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.title = "NookiPet"

// Reactively recolor Toolbar with the active pet's palette.
// Orbs, cards and charts already do this — Toolbar joins them.
        observeToolbarPalette()

        bottomNav = findViewById(R.id.bottomNav)
        setupBottomNav(savedInstanceState)

// Schedule notifications for seeded reminders — only runs once
// thanks to enqueueUniqueWork(REPLACE), so no duplicates on relaunch.
        scheduleSeededReminders()
    }

    /**
     * Wires up the BottomNavigationView.
     *
     * On first launch (savedInstanceState == null):
     *   - install HomeFragment
     *   - mark Home tab as selected
     *
     * On rotation (savedInstanceState != null):
     *   - BottomNav restores its own selection automatically
     *   - FragmentManager restores the fragment automatically
     *   - we do nothing
     */
    private fun setupBottomNav(savedInstanceState: Bundle?) {
        bottomNav.setOnItemSelectedListener { item ->
            val fragment = when (item.itemId) {
                R.id.nav_home -> HomeFragment()
                R.id.nav_pets -> PetsFragment()
                R.id.nav_stats -> StatsFragment()
                R.id.nav_settings -> SettingsFragment()
                else -> return@setOnItemSelectedListener false
            }

            // Only replace if the target differs from what's currently shown.
            // This prevents re-creating the same fragment on repeated taps.
            val current = supportFragmentManager
                .findFragmentById(R.id.fragmentContainer)
            val currentTag = current?.javaClass?.simpleName
            val targetTag = fragment.javaClass.simpleName

            if (currentTag != targetTag) {
                supportFragmentManager.beginTransaction()
                    .replace(R.id.fragmentContainer, fragment)
                    // no addToBackStack: tab switching should not pile up in back stack
                    .commit()
            }
            true
        }

        if (savedInstanceState == null) {
            // Fresh launch — install Home and select Home tab
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragmentContainer, HomeFragment())
                .commit()
            bottomNav.selectedItemId = R.id.nav_home
        }
        // On rotation nothing to do: BottomNav restores its selected id,
        // and FragmentManager restores the fragment.
    }

    // ──────────────────────────────────────────────────────────────
    // Menu
    // ──────────────────────────────────────────────────────────────

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_add -> {
                navigateToAddFragment()
                true
            }
            R.id.action_remind -> {
                scheduleReminder("Time to check humidity in Finley's terrarium", 10)
                Toast.makeText(this, "Reminder set for 10 seconds", Toast.LENGTH_SHORT).show()
                true
            }
            R.id.action_reminders -> {
                navigateToReminders()
                true
            }
            R.id.action_vet -> {
                navigateToBookVet()
                true
            }
            R.id.action_load_posts -> {
                navigateToTips()
                true
            }
            R.id.action_toggle_theme -> {
                val current = AppPreferences.isDarkThemeEnabled(this)
                AppPreferences.setDarkTheme(this, !current)
                applyTheme()
                Toast.makeText(
                    this,
                    if (!current) "Dark theme enabled" else "Light theme enabled",
                    Toast.LENGTH_SHORT
                ).show()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    // ──────────────────────────────────────────────────────────────
    // Theme
    // ──────────────────────────────────────────────────────────────

    private fun applyTheme() {
        val dark = AppPreferences.isDarkThemeEnabled(this)
        AppCompatDelegate.setDefaultNightMode(
            if (dark) AppCompatDelegate.MODE_NIGHT_YES
            else AppCompatDelegate.MODE_NIGHT_NO
        )
    }

    // ──────────────────────────────────────────────────────────────
    // Navigation helpers
    // ──────────────────────────────────────────────────────────────

    fun navigateToAddFragment() {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, AttachmentAddFragment())
            .addToBackStack(null)
            .commit()
    }

    fun navigateToTips() {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, PetTipsFragment())
            .addToBackStack(null)
            .commit()
    }

    fun navigateToStats() {
        bottomNav.selectedItemId = R.id.nav_stats
    }

    fun navigateToSettings() {
        bottomNav.selectedItemId = R.id.nav_settings
    }

    fun navigateToReminders() {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, RemindersFragment())
            .addToBackStack(null)
            .commit()
    }

    fun navigateToBookVet() {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, BookVetFragment())
            .addToBackStack(null)
            .commit()
    }

    fun navigateToNearbyClinics() {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, NearbyClinicsFragment())
            .addToBackStack(null)
            .commit()
    }

    // ──────────────────────────────────────────────────────────────
    // Old attachment helpers (kept for demos in previous labs)
    // ──────────────────────────────────────────────────────────────

    fun onAttachmentCreated(attachment: Attachment) {
        supportFragmentManager.popBackStackImmediate()
        val listFragment = supportFragmentManager
            .findFragmentById(R.id.fragmentContainer) as? AttachmentListFragment
        listFragment?.addAttachment(attachment)
    }
    /**
     * Subscribes to the active pet and repaints the Toolbar with its palette.
     * Falls back to MINT when there is no pet yet.
     *
     * Runs in Activity's lifecycle — safe against configuration changes.
     */
    /**
     * Subscribes to the active pet and repaints:
     *   - Toolbar background
     *   - BottomNav active tab
     *   - System status bar (top)
     *   - System navigation bar (bottom)
     *
     * Falls back to MINT when there is no pet yet.
     */
    private fun observeToolbarPalette() {
        val db = AppDatabase.getDatabase(this)
        val selectedPetRepo = SelectedPetRepository(this, db.petDao())

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                selectedPetRepo.selected.collect { pet ->
                    val palette = PetPaletteCatalog.byId(pet?.paletteId)

                    // Toolbar
                    toolbar.setBackgroundColor(palette.primary)

                    // BottomNav — active tab colored, inactive stays muted
                    val activeColor = ColorStateList(
                        arrayOf(
                            intArrayOf(android.R.attr.state_checked),
                            intArrayOf()
                        ),
                        intArrayOf(
                            palette.primary,
                            ContextCompat.getColor(this@MainActivity, R.color.bottom_nav_inactive)
                        )
                    )
                    bottomNav.itemIconTintList = activeColor
                    bottomNav.itemTextColor = activeColor

                    // System bars
                    applyWindowColors(palette)
                }
            }
        }
    }

    /**
     * Colors the two system-level bars that frame every screen.
     *
     * Uses the darker shade of the palette so that white status bar icons
     * remain readable. Forces light icons (false) for both bars.
     *
     * On Android 15+ (API 35) these APIs are no-ops due to edge-to-edge;
     * on older versions they work as expected. We suppress deprecation
     * warnings and let the OS decide on new versions.
     */
    @Suppress("DEPRECATION")
    private fun applyWindowColors(palette: PetPalette) {
        // Top bar — darker shade for good contrast with white icons
        window.statusBarColor = palette.primaryDark

        // Bottom system bar — same treatment
        window.navigationBarColor = palette.primaryDark

        // Force white icons on both bars (light icons on dark background)
        WindowInsetsControllerCompat(window, window.decorView).apply {
            isAppearanceLightStatusBars = false
            isAppearanceLightNavigationBars = false
        }
    }

    private fun scheduleReminder(reminderText: String, delaySeconds: Long) {
        val data = Data.Builder()
            .putString("reminder_text", reminderText)
            .build()

        val workRequest = OneTimeWorkRequestBuilder<ReminderWorker>()
            .setInputData(data)
            .setInitialDelay(delaySeconds, TimeUnit.SECONDS)
            .build()

        WorkManager.getInstance(this).enqueue(workRequest)
    }
    /**
     * Reads all enabled reminders from Room and schedules a WorkManager task
     * for each — using enqueueUniqueWork(REPLACE), so calling this again
     * on the next launch simply refreshes existing tasks instead of duplicating.
     *
     * This makes the app "just work": seeded reminders fire without user action.
     */
    private fun scheduleSeededReminders() {
        val db = AppDatabase.getDatabase(this)
        lifecycleScope.launch {
            db.reminderDao().getAll().collect { reminders ->
                // Collect once — schedule then bail out. Room's Flow never stops,
                // so we cancel this collection after the first emission.
                scheduleAll(reminders)
            }
        }
    }

    private suspend fun scheduleAll(reminders: List<Reminder>) {
        val wm = WorkManager.getInstance(this)
        val now = Calendar.getInstance()

        reminders.filter { it.enabled }.forEach { r ->
            val target = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, r.hourOfDay)
                set(Calendar.MINUTE, r.minute)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }
            if (target.timeInMillis - now.timeInMillis < 30_000L) {
                target.add(Calendar.DAY_OF_YEAR, 1)
            }
            val delaySeconds = (target.timeInMillis - now.timeInMillis) / 1000

            val data = Data.Builder()
                .putString("reminder_text", "${r.title} — scheduled reminder")
                .build()

            val request = OneTimeWorkRequestBuilder<ReminderWorker>()
                .setInputData(data)
                .setInitialDelay(delaySeconds, TimeUnit.SECONDS)
                .build()

            wm.enqueueUniqueWork("reminder_${r.id}", ExistingWorkPolicy.REPLACE, request)
        }
    }
}