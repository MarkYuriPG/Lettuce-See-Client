package com.example.lettuce_see_client.api

import com.example.lettuce_see_client.models.DetectionResponse
import com.google.gson.Gson
import okhttp3.*
import org.json.JSONObject
import java.io.IOException
import java.util.concurrent.TimeUnit

class ApiService {
    private val client: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(45, TimeUnit.SECONDS)
        .writeTimeout(45, TimeUnit.SECONDS)
        .readTimeout(45, TimeUnit.SECONDS)
        .retryOnConnectionFailure(true)
        .addInterceptor{chain ->
            val original = chain.request()
            // Try up to 3 times
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

    fun detectLettuce(image: MultipartBody.Part, confidence: RequestBody, callback: ApiCallback) {
        println("Debug - Starting API request")

        val requestBody = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addPart(image)
            .addPart(MultipartBody.Part.createFormData("confidence", null, confidence))
            .build()

        val request = Request.Builder()
            .url("${BASE_URL}/detect")
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

                        // Pretty print JSON for better readability in logs
                        try {
                            val jsonObject = JSONObject(responseBody)
                            println("Debug - Pretty JSON:\n${jsonObject.toString(2)}")
                        } catch (e: Exception) {
                            println("Debug - Could not pretty print JSON")
                        }

                        val detectionResponse = gson.fromJson(responseBody, DetectionResponse::class.java)
                        println("Debug - Parsed response object: $detectionResponse")

                        if (detectionResponse.images == null) {
                            println("Debug - Warning: Parsed response has null images")
                        }

                        callback.onResponse(Result.success(detectionResponse))
                    } else {
                        val errorBody = response.body?.string()
                        println("Debug - Error response: $errorBody")
                        callback.onResponse(Result.failure(IOException("API Error: ${response.code} - $errorBody")))
                    }
                } catch (e: Exception) {
                    println("Debug - Error parsing response: ${e.message}")
                    println("Debug - Response body was: $responseBody")
                    e.printStackTrace()
                    callback.onResponse(Result.failure(e))
                }finally {
                    response.close() // Ensure the response is always closed
                }
            }
        })
    }

    companion object {
        private const val BASE_URL = "http://10.0.2.2:5000"
    }
}
