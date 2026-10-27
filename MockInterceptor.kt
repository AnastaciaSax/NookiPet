package eng.nookipet.lab7.api

import android.content.Context
import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.Protocol
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody

/**
 * Returns JSON from a local asset file instead of hitting the network.
 *
 * Used to prove Retrofit works end-to-end with real JSON:
 *   Request → Interceptor → Mock response → Gson parser → Kotlin objects.
 *
 * In production, this interceptor is removed and the real API URL is used.
 * The UI and ViewModel code do not change.
 */
class MockInterceptor(
    private val context: Context,
    private val fileName: String
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val json = context.assets
            .open(fileName)
            .bufferedReader()
            .use { it.readText() }

        return Response.Builder()
            .request(chain.request())
            .protocol(Protocol.HTTP_1_1)
            .code(200)
            .message("OK")
            .body(json.toResponseBody("application/json".toMediaType()))
            .addHeader("content-type", "application/json")
            .build()
    }
}