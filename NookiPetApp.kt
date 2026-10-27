package eng.nookipet.lab7

import android.app.Application
import android.preference.PreferenceManager
import org.osmdroid.config.Configuration
import java.io.File

/**
 * Custom Application class.
 * Initializes OSMDroid exactly once at process start:
 *   - userAgent — required by OSM tile servers (they block clients without a UA)
 *   - tile cache path — places cached tiles in the app's private storage
 *
 * Must be registered in AndroidManifest — see step 3.2.
 */
class NookiPetApp : Application() {

    override fun onCreate() {
        super.onCreate()

        // OSMDroid recommends loading config from SharedPreferences first.
        // This restores user preferences from previous sessions.
        Configuration.getInstance().load(
            applicationContext,
            PreferenceManager.getDefaultSharedPreferences(applicationContext)
        )

        // Every app that fetches tiles from OSM must identify itself.
        // Using the package name is the common convention and prevents blocks.
        Configuration.getInstance().userAgentValue = packageName

        // Store tiles in app-private files dir — no permission prompts,
        // works on Android 10+ where external storage is scoped.
        val osmBase = File(filesDir, "osmdroid")
        if (!osmBase.exists()) osmBase.mkdirs()
        Configuration.getInstance().osmdroidBasePath = osmBase
        Configuration.getInstance().osmdroidTileCache = File(osmBase, "tiles")
        // Wire up Retrofit singleton with an application context
        ApiService.init(this)
    }
}