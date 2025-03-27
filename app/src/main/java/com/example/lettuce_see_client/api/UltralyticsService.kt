package com.example.lettuce_see_client.api

import com.example.lettuce_see_client.BuildConfig
import com.example.lettuce_see_client.models.DetectionResponse
import com.google.gson.Gson
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import org.json.JSONObject
import java.io.IOException
import java.util.concurrent.TimeUnit

class UltralyticsService {
    companion object {
        private const val BASE_URL = "https://predict.ultralytics.com"
        private const val MODEL = BuildConfig.MODEL
        private const val API_KEY = BuildConfig.YOLO_KEY
    }

    private val client: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(45, TimeUnit.SECONDS)
        .writeTimeout(45, TimeUnit.SECONDS)
        .readTimeout(45, TimeUnit.SECONDS)
        .retryOnConnectionFailure(true)
        .addInterceptor { chain ->
            val original = chain.request()
            var response: Response? = null
            var exception: Exception? = null

            for (attempt in 1..3) {
                try {
                    response = chain.proceed(original)
                    if (response.isSuccessful) {
                        return@addInterceptor response
                    } else {
                        response.close()
                    }
                } catch (e: Exception) {
                    exception = e
                    println("Debug - Attempt $attempt failed: ${e.message}")
                    if (attempt == 3) throw e
                }
            }

            response ?: throw exception!!
        }
        .build()

    private val gson = Gson()

    fun interface ApiCallback {
        fun onResponse(response: Result<DetectionResponse>)
    }

    fun detectObject(imageBytes: ByteArray, callback: ApiCallback) {
        println("Debug - Starting API request")

        val requestBody = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart(
                "file",
                "image.jpg",
                RequestBody.create("image/jpeg".toMediaTypeOrNull(), imageBytes)
            )
            .addFormDataPart("model", MODEL)
            .addFormDataPart("imgsz", "640")
            .addFormDataPart("conf", "0.25")
            .addFormDataPart("iou", "0.45")
            .build()

        val request = Request.Builder()
            .url(BASE_URL)
            .header("x-api-key", API_KEY)
            .post(requestBody)
            .build()

        println("Debug - Sending request to: ${request.url}")

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                println("Debug - API call failed: ${e.message}")
                e.printStackTrace()
                callback.onResponse(Result.failure(e))
            }

            override fun onResponse(call: Call, response: Response) {
                var responseBody: String? = null
                try {
                    println("Debug - Received response: ${response.code}")

                    if (response.isSuccessful) {
                        responseBody = response.body?.let { body ->
                            try {
                                body.string()
                            } catch (e: IOException) {
                                println("Debug - Error reading response body: ${e.message}")
                                throw e
                            }
                        }

                        if (responseBody == null) {
                            throw IOException("Empty response body")
                        }

                        println("Debug - Raw JSON response: $responseBody")

                        try {
                            val jsonObject = JSONObject(responseBody)
                            println("Debug - Pretty JSON:\n${jsonObject.toString(2)}")
                        } catch (e: Exception) {
                            println("Debug - Could not pretty print JSON")
                        }

                        val detectionResponse = gson.fromJson(responseBody, DetectionResponse::class.java)
                        println("Debug - Parsed response object: $detectionResponse")
                        callback.onResponse(Result.success(detectionResponse))
                    } else {
                        val error = IOException("API call failed with code: ${response.code}")
                        callback.onResponse(Result.failure(error))
                    }
                } catch (e: Exception) {
                    println("Debug - Error processing response: ${e.message}")
                    e.printStackTrace()
                    callback.onResponse(Result.failure(e))
                } finally {
                    response.close()
                }
            }
        })
    }
}
