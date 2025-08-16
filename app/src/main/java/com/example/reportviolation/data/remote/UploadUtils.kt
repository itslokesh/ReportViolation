package com.example.reportviolation.data.remote

import android.content.Context
import android.net.Uri
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody

fun ensureAbsolute(baseUrl: String, pathOrUrl: String): String =
    if (pathOrUrl.startsWith("http")) pathOrUrl else baseUrl.trimEnd('/') + pathOrUrl

suspend fun uriToPart(context: Context, uri: Uri, fieldName: String = "photo"): MultipartBody.Part {
    val contentResolver = context.contentResolver
    val type = contentResolver.getType(uri) ?: "image/jpeg"
    val input = contentResolver.openInputStream(uri)!!
    val bytes = input.readBytes()
    val req = RequestBody.create(type.toMediaTypeOrNull(), bytes)
    val filename = (if (fieldName == "video") "video_" else "photo_") + System.currentTimeMillis().toString() + if (fieldName == "video") ".mp4" else ".jpg"
    return MultipartBody.Part.createFormData(fieldName, filename, req)
}


