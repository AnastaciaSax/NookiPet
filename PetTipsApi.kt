package eng.nookipet.lab7.api

import eng.nookipet.lab7.data.entity.PetTip
import retrofit2.Response
import retrofit2.http.GET

interface PetTipsApi {

    /**
     * Returns the list of pet care tips.
     * The MockInterceptor intercepts this call and returns JSON from assets.
     * In production, base URL + interceptor are replaced — no UI code changes.
     */
    @GET("tips")
    suspend fun getTips(): Response<List<PetTip>>
}