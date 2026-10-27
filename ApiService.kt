package eng.nookipet.lab7

import android.content.Context
import eng.nookipet.lab7.api.MockInterceptor
import eng.nookipet.lab7.api.PetTipsApi
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

/**
 * Retrofit singleton.
 *
 * In this project the base URL is a placeholder — the MockInterceptor
 * returns JSON from assets/pet_tips.json. In production, remove the
 * mock interceptor and set the real base URL — everything else stays.
 */
object ApiService {

    private const val BASE_URL = "https://api.nookipet.local/"

    private lateinit var appContext: Context

    /** Called once from NookiPetApp.onCreate. */
    fun init(context: Context) {
        appContext = context.applicationContext
    }

    private val logging = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val client: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .addInterceptor(MockInterceptor(appContext, "pet_tips.json"))
            .addInterceptor(logging)
            .build()
    }

    val petTipsApi: PetTipsApi by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(PetTipsApi::class.java)
    }
}