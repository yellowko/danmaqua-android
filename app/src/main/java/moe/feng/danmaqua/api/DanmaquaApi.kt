package moe.feng.danmaqua.api

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import moe.feng.danmaqua.model.OnlinePatternRules
import moe.feng.danmaqua.model.Recommendation
import moe.feng.danmaqua.model.VTuberCatalog
import moe.feng.danmaqua.model.VTuberGroup
import moe.feng.danmaqua.util.HttpUtils
import okhttp3.Request

object DanmaquaApi {

    const val API_HOST_CN = "https://raw.kgithub.com/yellowko/danmaqua-data/master"
    const val API_HOST_INTERNATIONAL = "https://raw.githubusercontent.com/yellowko/danmaqua-data/master"

    private suspend inline fun <reified T : Any> apiRequest(
        requestPath: String
    ) = withContext(Dispatchers.IO) {
        val cnRequest = Request.Builder()
            .url(API_HOST_CN + requestPath)
            .build()
        val intlRequest = Request.Builder()
            .url(API_HOST_INTERNATIONAL + requestPath)
            .build()

        try {
            HttpUtils.requestAsJson<T>(cnRequest)
        } catch (e: Exception) {
            HttpUtils.requestAsJson<T>(cnRequest)
        }
    }

    suspend fun getRecommendation(): Recommendation {
        return apiRequest("/room/recommendation.json")
    }

    suspend fun getVTuberCatalog(): VTuberCatalog {
        return apiRequest("/room/vtubers_catalog.json")
    }

    suspend fun getVTuberGroup(name: String): VTuberGroup {
        return apiRequest("/room/vtubers/$name.json")
    }

    suspend fun getPatternRules(): OnlinePatternRules {
        return apiRequest<OnlinePatternRules>("/rule/patterns.json").apply {
            data.forEach { it.local = false }
        }
    }

}