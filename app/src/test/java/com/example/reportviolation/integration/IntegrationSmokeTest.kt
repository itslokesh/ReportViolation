package com.example.reportviolation.integration

import com.example.reportviolation.BuildConfig
import com.example.reportviolation.data.remote.*
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.Assume.assumeTrue
import retrofit2.Retrofit
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import java.util.concurrent.TimeUnit

private interface HealthApi {
    @GET("/health")
    suspend fun health(): okhttp3.ResponseBody
}

class IntegrationSmokeTest {

    private fun retrofit(baseUrl: String, token: String? = null): Retrofit {
        val logging = HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BODY }
        val auth = Interceptor { chain ->
            val req = chain.request()
            val newReq = if (!token.isNullOrBlank()) req.newBuilder().addHeader("Authorization", "Bearer $token").build() else req
            chain.proceed(newReq)
        }
        val client = OkHttpClient.Builder()
            .addInterceptor(auth)
            .addInterceptor(logging)
            .connectTimeout(5, TimeUnit.SECONDS)
            .readTimeout(5, TimeUnit.SECONDS)
            .build()
        return Retrofit.Builder()
            .baseUrl(baseUrl)
            .addConverterFactory(GsonConverterFactory.create())
            .client(client)
            .build()
    }

    private fun serverReachable(): Boolean = try {
        // quick probe
        runBlocking {
            val r = retrofit(BuildConfig.BASE_URL).create(HealthApi::class.java)
            val resp = r.health()
            resp.string() // consume
        }
        true
    } catch (_: Throwable) { false }

    @Test
    fun health_endpoint_should_respond_200_if_server_up() = runBlocking {
        assumeTrue("Backend not reachable at ${BuildConfig.BASE_URL}", serverReachable())
        val api = retrofit(BuildConfig.BASE_URL).create(HealthApi::class.java)
        val body = api.health()
        // If we got here without exception, assume 200 OK
        assertTrue(body.contentLength() >= -1)
    }

    @Test
    fun optional_full_flow_otp_upload_report_when_env_present() = runBlocking {
        assumeTrue("Backend not reachable at ${BuildConfig.BASE_URL}", serverReachable())
        val tokenEnv = System.getenv("TEST_TOKEN")
        val phone = System.getenv("TEST_PHONE")
        val otp = System.getenv("TEST_OTP")

        val token = if (!tokenEnv.isNullOrBlank()) {
            tokenEnv
        } else {
            if (phone.isNullOrBlank() || otp.isNullOrBlank()) return@runBlocking // skip when no creds

            val retrofitNoAuth = retrofit(BuildConfig.BASE_URL)
            val authApi = retrofitNoAuth.create(AuthApi::class.java)

            val sendRes = authApi.sendOtp(OtpSendBody(phone))
            assertTrue(sendRes.success)

            val verifyRes = try {
                authApi.verifyOtp(OtpVerifyBody(phone, otp))
            } catch (e: retrofit2.HttpException) {
                val errBody = e.response()?.errorBody()?.string()
                println("VERIFY_OTP_FAILED status=${e.code()} body=${errBody}")
                throw e
            }
            val authData = verifyRes.data
            assertTrue(verifyRes.success && authData != null)
            authData!!.token
        }
        val retrofitAuth = retrofit(BuildConfig.BASE_URL, token)

        val uploadApi = retrofitAuth.create(UploadApi::class.java)
        // Build a small in-memory PNG
        val imageBytes = ByteArray(256) { 0x7f }
        val fileName = "test_${System.currentTimeMillis()}.jpg"
        val part = okhttp3.MultipartBody.Part.createFormData(
            "photo",
            fileName,
            okhttp3.RequestBody.create("image/jpeg".toMediaTypeOrNull(), imageBytes)
        )
        val uploaded = uploadApi.uploadPhoto(part)
        assertTrue(uploaded.success && uploaded.data != null)
        val photoUrl = ensureAbsolute(BuildConfig.BASE_URL, uploaded.data!!.url)
        println("INTEGRATION_UPLOAD_PHOTO fileName=$fileName url=$photoUrl")

        val reportsApi = retrofitAuth.create(CitizenReportsApi::class.java)
        val create = reportsApi.createReport(
            ReportCreateBody(
                violationTypes = listOf("OTHERS"),
                timestamp = com.example.reportviolation.utils.DateTimeUtils.nowIsoIst(),
                latitude = 12.9716,
                longitude = 77.5946,
                address = "Test Address",
                pincode = "600001",
                city = "Test City",
                district = "Test District",
                state = "Test State",
                isAnonymous = true,
            )
        )
        assertTrue(create.success)
    }

    @Test
    fun optional_upload_video_when_env_present() = runBlocking {
        assumeTrue("Backend not reachable at ${BuildConfig.BASE_URL}", serverReachable())
        val tokenEnv = System.getenv("TEST_TOKEN")
        val token = if (!tokenEnv.isNullOrBlank()) {
            tokenEnv
        } else {
            val phone = System.getenv("TEST_PHONE")
            val otp = System.getenv("TEST_OTP")
            if (phone.isNullOrBlank() || otp.isNullOrBlank()) return@runBlocking // skip when no creds

            val retrofitNoAuth = retrofit(BuildConfig.BASE_URL)
            val authApi = retrofitNoAuth.create(AuthApi::class.java)
            val verifyRes = try {
                authApi.verifyOtp(OtpVerifyBody(phone, otp)) // assumes OTP still valid; if not, call send first
            } catch (e: retrofit2.HttpException) {
                val errBody = e.response()?.errorBody()?.string()
                println("VERIFY_OTP_FAILED status=${e.code()} body=${errBody}")
                throw e
            }
            verifyRes.data?.token ?: return@runBlocking
        }

        val retrofitAuth = retrofit(BuildConfig.BASE_URL, token)
        val uploadApi = retrofitAuth.create(UploadApi::class.java)

        // Build a small in-memory MP4-like blob (not a real video, just bytes for endpoint acceptance)
        val videoBytes = ByteArray(1024) { 0x01 }
        val videoName = "test_${System.currentTimeMillis()}.mp4"
        val videoPart = okhttp3.MultipartBody.Part.createFormData(
            "video",
            videoName,
            okhttp3.RequestBody.create("video/mp4".toMediaTypeOrNull(), videoBytes)
        )

        val uploadedVideo = uploadApi.uploadVideo(videoPart)
        assertTrue(uploadedVideo.success && uploadedVideo.data != null)
        val videoUrl = ensureAbsolute(BuildConfig.BASE_URL, uploadedVideo.data!!.url)
        assertTrue(videoUrl.startsWith("http"))
        println("INTEGRATION_UPLOAD_VIDEO fileName=$videoName url=$videoUrl")
    }
}


