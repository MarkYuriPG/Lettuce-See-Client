package com.example.lettuce_see_client

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Switch
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import com.example.lettuce_see_client.ColorPickerDialog
import com.example.lettuce_see_client.R

class SettingsActivity : AppCompatActivity() {

    private val sharedPreferences by lazy {
        getSharedPreferences("SettingsPrefs", Context.MODE_PRIVATE)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        // Load current settings
        val savedPath = sharedPreferences.getString("save_path", "defaultPath") ?: "defaultPath"
        val savedTheme = sharedPreferences.getString("theme", "light") ?: "light"
        val savedBoundingColor = sharedPreferences.getInt("bounding_box_color", Color.RED)

        // Initialize UI elements
        val pathEditText: EditText = findViewById(R.id.pathEditText)
        val themeSwitch: Switch = findViewById(R.id.themeSwitch)
        val boundingColorButton: Button = findViewById(R.id.boundingColorButton)

        // Set values
        pathEditText.setText(savedPath)
        themeSwitch.isChecked = savedTheme == "dark"
        boundingColorButton.setBackgroundColor(savedBoundingColor)

        // Set up color picker button
        boundingColorButton.setOnClickListener {
            val colorPickerDialog = ColorPickerDialog(this) { color ->
                boundingColorButton.setBackgroundColor(color)
            }
            colorPickerDialog.show()
        }

        // Save settings when changed
        findViewById<Button>(R.id.saveSettingsButton).setOnClickListener {
            val newPath = pathEditText.text.toString()
            val newTheme = if (themeSwitch.isChecked) "dark" else "light"
            val newBoundingColor = (boundingColorButton.background as ColorDrawable).color

            // Save new settings to SharedPreferences
            sharedPreferences.edit().apply {
                putString("save_path", newPath)
                putString("theme", newTheme)
                putInt("bounding_box_color", newBoundingColor)
            }.apply()

            // Optionally, apply the theme changes immediately
            if (newTheme == "dark") {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            } else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            }

            // Go back to previous screen
            finish()
        }
    }
}

