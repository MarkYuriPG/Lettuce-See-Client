package com.example.lettuce_see_client

import android.app.Activity
import android.content.Context
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import android.graphics.*
import android.provider.MediaStore
import android.widget.Toast
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.layout.ContentScale
import androidx.core.content.FileProvider
//import com.example.lettuce_see_client.api.ApiService
import com.example.lettuce_see_client.models.DetectionResponse
import com.example.lettuce_see_client.ui.theme.LettuceSeeClientTheme
import java.io.File
import java.io.FileOutputStream
import androidx.compose.runtime.rememberCoroutineScope
import com.example.lettuce_see_client.api.UltralyticsService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.math.max
import android.os.Environment
import androidx.activity.compose.ManagedActivityResultLauncher
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.provider.Settings
import androidx.annotation.DrawableRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.core.content.ContextCompat
import androidx.compose.material3.AlertDialog
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.colorResource
import java.io.OutputStream
import android.content.ContentValues
import androidx.compose.ui.text.style.TextAlign

class MainActivity : ComponentActivity() {
    private val ultralyticsService = UltralyticsService()
    private var selectedTab by mutableStateOf(BottomNavItem.TakePhoto)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            LettuceSeeClientTheme {
                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    bottomBar = { BottomNavigationBar() { selectedOption -> handleNavSelection(selectedOption) } }
                ) { innerPadding ->
                    MainScreen(
                        modifier = Modifier.padding(innerPadding),
                        ultralyticsService = ultralyticsService,
                        selectedTab = selectedTab
                    )
                }
            }
        }
    }

    private fun handleNavSelection(selectedOption: BottomNavItem) {
        selectedTab = selectedOption
    }
}

@Composable
fun MainScreen(modifier: Modifier = Modifier, ultralyticsService: UltralyticsService, selectedTab: BottomNavItem) {
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var processedBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    var showPermissionDialog by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val requiredPermissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        arrayOf(
            Manifest.permission.CAMERA,
            Manifest.permission.READ_MEDIA_IMAGES
        )
    } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        arrayOf(
            Manifest.permission.CAMERA,
            Manifest.permission.READ_EXTERNAL_STORAGE
        )
    } else {
        arrayOf(
            Manifest.permission.CAMERA,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        )
    }

    // Camera launcher
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) {
            selectedImageUri?.let { uri ->
                isLoading = true
                processImage(
                    uri = uri,
                    context = context,
                    ultralyticsService = ultralyticsService,
                    onResult = { bitmap ->
                        processedBitmap = bitmap
                        isLoading = false
                    },
                    onError = { error ->
                        isLoading = false
                        scope.launch(Dispatchers.Main) {
                            Toast.makeText(context, "Error: ${error.message}", Toast.LENGTH_LONG).show()
                        }
                    }
                )
            }
        }
    }

    val multiplePermissionsLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val areGranted = permissions.entries.all { it.value }
        if (areGranted) {
            // All permissions granted, launch camera
            launchCameraWithUri(context) { uri ->
                selectedImageUri = uri
                cameraLauncher.launch(uri)
            }
        } else {
            // Show dialog if permissions are denied
            showPermissionDialog = true
        }
    }

    // Gallery launcher
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            selectedImageUri = it
            isLoading = true
            processImage(
                uri = it,
                context = context,
                ultralyticsService = ultralyticsService,
                onResult = { bitmap ->
                    processedBitmap = bitmap
                    isLoading = false
                },
                onError = { error ->
                    isLoading = false
                    scope.launch(Dispatchers.Main) {
                        Toast.makeText(context, "Error: ${error.message}", Toast.LENGTH_LONG).show()
                    }
                }
            )
        }
    }

    // Permission Dialog
    if (showPermissionDialog) {
        AlertDialog(
            onDismissRequest = { showPermissionDialog = false },
            title = { Text("Permissions Required") },
            text = { Text("Camera and storage permissions are required to use this feature. Please grant them in Settings.") },
            confirmButton = {
                Button(
                    onClick = {
                        showPermissionDialog = false
                        openAppSettings(context)
                    }
                ) {
                    Text("Open Settings")
                }
            },
            dismissButton = {
                Button(
                    onClick = { showPermissionDialog = false }
                ) {
                    Text("Cancel")
                }
            }
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        when {
            isLoading -> {
                CircularProgressIndicator(
                    color = colorResource(R.color.nav_item_color)
                )
            }
            processedBitmap != null -> {
                processedBitmap ?. let { bitmap ->
                    Image(
                        bitmap = bitmap.asImageBitmap(),
                        contentDescription = "Processed Image",
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        contentScale = ContentScale.Fit
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Button(
                        onClick = {
                            saveImageToGallery(context, bitmap)
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = colorResource(R.color.nav_item_color)
                        )
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.baseline_file_download_24),
                            contentDescription = "Download Icon"
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Save")
                    }
                }
            }
            else -> {
                Text(
                    text = "Welcome! \n\nTap the 📷 icon to take a photo or 🖼️ to choose from gallery.\nWe'll process it for lettuce health & detection magic!",
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(16.dp)
                )
            }
        }

        LaunchedEffect(selectedTab) {
            when (selectedTab) {
                BottomNavItem.TakePhoto -> {
                    val hasPermissions = requiredPermissions.all {
                        ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED
                    }
                    if (hasPermissions) {
                        launchCameraWithUri(context) { uri ->
                            selectedImageUri = uri
                            cameraLauncher.launch(uri)
                        }
                    } else {
                        showPermissionDialog = true
                        multiplePermissionsLauncher.launch(requiredPermissions)
                    }
                }

                BottomNavItem.ChooseGallery -> galleryLauncher.launch("image/*")
                BottomNavItem.Settings -> {
                    val intent = Intent(context, SettingsActivity::class.java)
                    context.startActivity(intent)
                }
            }
        }
    }
}

private fun launchCameraWithUri(
    context: Context,
    onUriCreated: (Uri) -> Unit
) {
    try {
        val photoFile = createImageFile(context)
        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            photoFile
        )
        onUriCreated(uri)
    } catch (e: Exception) {
        e.printStackTrace()
        Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_LONG).show()
    }
}

private fun createImageFile(context: Context): File {
    val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
    val imageFileName = "JPEG_${timeStamp}_"
    val storageDir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
    return File.createTempFile(
        imageFileName,
        ".jpg",
        storageDir
    )
}

private fun openAppSettings(context: Context) {
    try {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = Uri.fromParts("package", context.packageName, null)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
    } catch (e: Exception) {
        Toast.makeText(
            context,
            "Unable to open settings. Please open settings manually.",
            Toast.LENGTH_LONG
        ).show()
    }
}


private fun processImage(
    uri: Uri,
    context: Context,
    ultralyticsService: UltralyticsService,
    onResult: (Bitmap) -> Unit,
    onError: (Exception) -> Unit
) {
    try {
        val imageBytes = context.contentResolver.openInputStream(uri)?.use {
            it.readBytes()
        } ?: throw Exception("Failed to read image")

        ultralyticsService.detectObject(imageBytes) { result ->
            result.fold(
                onSuccess = { response ->
                    try {
                        println("Debug - API Response received: $response")

                        // Make sure to run bitmap operations on the main thread
                        (context as? Activity)?.runOnUiThread {
                            try {
                                val originalBitmap = MediaStore.Images.Media.getBitmap(context.contentResolver, uri)
                                println("Debug - Original bitmap size: ${originalBitmap.width}x${originalBitmap.height}")

                                val processedBitmap = drawDetections(originalBitmap, response)
                                println("Debug - Processed bitmap created")

                                onResult(processedBitmap)
                            } catch (e: Exception) {
                                println("Debug - Error processing bitmap: ${e.message}")
                                e.printStackTrace()
                                onError(e)
                            }
                        }
                    } catch (e: Exception) {
                        println("Debug - Error in response handling: ${e.message}")
                        e.printStackTrace()
                        onError(e)
                    }
                },
                onFailure = { exception ->
                    println("Debug - API call failed: ${exception.message}")
                    exception.printStackTrace()
                    onError(exception as Exception)
                }
            )
        }
    } catch (e: Exception) {
        println("Debug - Error in image processing: ${e.message}")
        e.printStackTrace()
        onError(e)
    }
}


private fun drawDetections(originalBitmap: Bitmap, response: DetectionResponse): Bitmap {
    val mutableBitmap = originalBitmap.copy(Bitmap.Config.ARGB_8888, true)
    val canvas = Canvas(mutableBitmap)

    // Define colors for different classes
    val healthyColor = Color.GREEN
    val unhealthyColor = Color.RED
    val weedColor = Color.YELLOW

    response.images?.firstOrNull()?.results?.forEach { detection ->
        // Set color and label based on detection name
        val (color, label) = when (detection.name) {
            "normal_lettuce" -> Pair(healthyColor, "healthy")
            "disease_lettuce" -> Pair(unhealthyColor, "unhealthy")
            "weed" -> Pair(weedColor, "weed")
            else -> Pair(unhealthyColor, "unknown") // fallback case
        }

        // Configure paints with appropriate color
        val boxPaint = Paint().apply {
            this.color = color
            style = Paint.Style.STROKE
            strokeWidth = 5f
        }

        val textPaint = Paint().apply {
            this.color = color
            textSize = 80f
            style = Paint.Style.FILL_AND_STROKE
            strokeWidth = 5f
        }

        val left = detection.box.x1
        val top = detection.box.y1
        val right = detection.box.x2
        val bottom = detection.box.y2

        println("Debug - Drawing box at: ($left, $top, $right, $bottom)")

        // Draw the detection box
        canvas.drawRect(left, top, right, bottom, boxPaint)

        // Draw label with status and confidence
        val displayText = "$label ${(detection.confidence * 100).toInt()}%"
        canvas.drawText(displayText, left, top - 20f, textPaint)

        println("Debug - Drew box for ${detection.name} ($label) with confidence ${detection.confidence}")
    }

    return mutableBitmap
}

fun saveImageToGallery(context: Context, bitmap: Bitmap) {
    val filename = "processed_image_${System.currentTimeMillis()}.jpg"
    val fos: OutputStream?

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, filename)
            put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
            put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/LettuceSee")
        }

        val imageUri: Uri? = context.contentResolver.insert(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues
        )

        fos = imageUri?.let { context.contentResolver.openOutputStream(it) }
    } else {
        val imagesDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).toString()
        val image = File(imagesDir, filename)
        fos = FileOutputStream(image)
    }

    fos?.use {
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, it)
        Toast.makeText(context, "Image saved to gallery", Toast.LENGTH_SHORT).show()
    } ?: Toast.makeText(context, "Failed to save image", Toast.LENGTH_SHORT).show()
}

@Composable
fun BottomNavigationBar(onItemSelected: (BottomNavItem) -> Unit) {
    var selectedTab by remember { mutableStateOf<BottomNavItem?>(null) }

    NavigationBar {
        BottomNavItem.values().forEach { item ->
            NavigationBarItem(
                selected = selectedTab == item,
                onClick = {
                    onItemSelected(item)
                    selectedTab = item

                },
                icon = {
                    Icon(
                        painter = painterResource(id = item.iconRes),
                        contentDescription = item.label,
                        tint = if (selectedTab == item) colorResource(R.color.nav_item_color) else colorResource(R.color.gray)
                    )
                },
                label = { Text(
                    item.label,
                    color = if (selectedTab == item) colorResource(R.color.nav_item_color) else colorResource(R.color.gray)
                ) },
                alwaysShowLabel = false,
                colors = NavigationBarItemDefaults.colors(
                    indicatorColor = colorResource(R.color.transparent)
                )
            )
        }
    }
}

enum class BottomNavItem(val label: String, @DrawableRes val iconRes: Int) {
    Settings("Settings", R.drawable.baseline_settings_24),
    TakePhoto("Take Photo", R.drawable.baseline_camera_alt_24),
    ChooseGallery("Gallery", R.drawable.baseline_add_photo_alternate_24)
}

//private fun createImageFile(context: Context): File {
//    val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
//    val storageDir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
//    return File.createTempFile(
//        "JPEG_${timeStamp}_",
//        ".jpg",
//        storageDir
//    ).apply {
//        deleteOnExit() // Optional: delete the file when the app exits
//    }
//}
