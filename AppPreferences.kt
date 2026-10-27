package eng.nookipet.lab7

import android.content.Context
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/**
 * Light key-value storage for user preferences.
 *
 * Exposes two shared StateFlows:
 *   - userNameFlow()       — greeting on Home
 *   - selectedPetIdFlow()  — active pet across all screens
 *
 * Because both flows are cached singletons, every subscriber — every
 * ViewModel, every Fragment, MainActivity — sees updates instantly.
 */
object AppPreferences {

    private const val PREFS_NAME = "nookipet_settings"
    private const val KEY_DARK_THEME = "dark_theme"
    private const val KEY_SELECTED_PET_ID = "selected_pet_id"
    private const val KEY_USER_NAME = "user_name"

    // ── Theme ─────────────────────────────────────────────────────

    fun setDarkTheme(context: Context, enabled: Boolean) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit().putBoolean(KEY_DARK_THEME, enabled).apply()
    }

    fun isDarkThemeEnabled(context: Context): Boolean =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getBoolean(KEY_DARK_THEME, false)

    // ── Selected pet (shared reactive flow) ───────────────────────

    private var cachedSelectedPetIdFlow: MutableStateFlow<Long>? = null

    /**
     * Global flow of the selected pet id.
     * First call reads from SharedPreferences, then keeps it in memory.
     */
    fun selectedPetIdFlow(context: Context): StateFlow<Long> {
        val existing = cachedSelectedPetIdFlow
        if (existing != null) return existing
        val flow = MutableStateFlow(getSelectedPetId(context))
        cachedSelectedPetIdFlow = flow
        return flow
    }

    fun getSelectedPetId(context: Context): Long =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getLong(KEY_SELECTED_PET_ID, 0L)

    fun setSelectedPetId(context: Context, petId: Long) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit().putLong(KEY_SELECTED_PET_ID, petId).apply()
        cachedSelectedPetIdFlow?.value = petId
    }

    // ── User name ─────────────────────────────────────────────────

    private var cachedUserNameFlow: MutableStateFlow<String>? = null

    fun userNameFlow(context: Context): StateFlow<String> {
        val existing = cachedUserNameFlow
        if (existing != null) return existing
        val flow = MutableStateFlow(getUserName(context))
        cachedUserNameFlow = flow
        return flow
    }

    fun getUserName(context: Context): String =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getString(KEY_USER_NAME, "") ?: ""

    fun setUserName(context: Context, name: String) {
        val trimmed = name.trim()
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit().putString(KEY_USER_NAME, trimmed).apply()

        val flow = cachedUserNameFlow ?: MutableStateFlow(trimmed).also {
            cachedUserNameFlow = it
        }
        flow.value = trimmed
    }
}