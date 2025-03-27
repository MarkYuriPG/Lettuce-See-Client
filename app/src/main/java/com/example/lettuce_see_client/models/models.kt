package com.example.lettuce_see_client.models

// DetectionResponse.kt
data class DetectionResponse(
    val images: List<ImageData>? = null,
    val metadata: Any? = null  // Using Any since we don't need the metadata details
)

data class ImageData(
    val results: List<Detection>? = null,
    val shape: List<Int>? = null,
    val speed: Speed? = null
)

data class Detection(
    val `class`: Int = 0,
    val name: String = "",
    val confidence: Float = 0f,
    val box: Box = Box(),
    val segments: Segments? = null
)

data class Box(
    val x1: Float = 0f,
    val y1: Float = 0f,
    val x2: Float = 0f,
    val y2: Float = 0f
)

data class Segments(
    val x: List<Float>? = null,
    val y: List<Float>? = null
)

data class Speed(
    val inference: Float = 0f,
    val preprocess: Float = 0f,
    val postprocess: Float = 0f
)
