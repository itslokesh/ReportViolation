package com.example.reportviolation.data.remote

import android.content.Context
import androidx.work.*
import java.util.concurrent.TimeUnit

object CacheManager {
    fun schedulePeriodicCleanup(context: Context) {
        val request = PeriodicWorkRequestBuilder<HttpCacheCleanupWorker>(
            6, TimeUnit.HOURS
        ).setConstraints(
            Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()
        ).build()
        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            "http_cache_cleanup",
            ExistingPeriodicWorkPolicy.KEEP,
            request
        )
    }
}

class HttpCacheCleanupWorker(appContext: Context, params: WorkerParameters) : CoroutineWorker(appContext, params) {
    override suspend fun doWork(): Result {
        return try {
            val client = ApiClient.retrofitBuilder // access to ensure initialized
            // Clear HTTP cache if above threshold
            val cacheField = OkHttpClientHolder.getCache()
            cacheField?.let { cache ->
                val maxBytes = 10L * 1024L * 1024L
                if (cache.size() > (maxBytes * 0.8).toLong()) {
                    cache.evictAll()
                }
            }
            Result.success()
        } catch (_: Throwable) {
            Result.retry()
        }
    }
}

// Helper to access the single OkHttp cache instance
internal object OkHttpClientHolder {
    fun getCache(): okhttp3.Cache? {
        return try {
            val baseClientField = ApiClient::class.java.getDeclaredField("baseClient")
            baseClientField.isAccessible = true
            val client = baseClientField.get(ApiClient) as okhttp3.OkHttpClient
            client.cache
        } catch (_: Throwable) { null }
    }
}


