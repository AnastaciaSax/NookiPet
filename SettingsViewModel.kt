package eng.nookipet.lab7.ui.settings

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.Data
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import eng.nookipet.lab7.AppPreferences
import eng.nookipet.lab7.ReminderWorker
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

/**
 * Backs the Settings screen.
 *
 * Extends AndroidViewModel because we need an Application context
 * to write to SharedPreferences and enqueue WorkManager requests.
 */
class SettingsViewModel(app: Application) : AndroidViewModel(app) {

    private val context = app.applicationContext

    /** User name as a StateFlow — Settings UI updates live while typing. */
    val userName: StateFlow<String> = AppPreferences.userNameFlow(context)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "")

    /** Current theme state from SharedPreferences. */
    fun isDarkTheme(): Boolean = AppPreferences.isDarkThemeEnabled(context)

    fun setDarkTheme(enabled: Boolean) {
        AppPreferences.setDarkTheme(context, enabled)
    }

    fun setUserName(name: String) {
        AppPreferences.setUserName(context, name)
    }

    /**
     * Sends a demo notification in 5 seconds via WorkManager.
     * Used by "Test notification" button — proves the whole pipeline works.
     */
    fun sendTestNotification(onEnqueued: () -> Unit) {
        val data = Data.Builder()
            .putString("reminder_text", "This is a test reminder from NookiPet")
            .build()

        val request = OneTimeWorkRequestBuilder<ReminderWorker>()
            .setInputData(data)
            .setInitialDelay(5, TimeUnit.SECONDS)
            .build()

        viewModelScope.launch {
            WorkManager.getInstance(context).enqueue(request)
            onEnqueued()
        }
    }
}