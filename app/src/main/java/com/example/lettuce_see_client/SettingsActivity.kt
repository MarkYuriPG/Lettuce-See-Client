package com.example.lettuce_see_client

import android.content.Context
import android.content.SharedPreferences
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Switch
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import com.example.lettuce_see_client.ColorPickerDialog
import com.example.lettuce_see_client.R

class SettingsActivity : AppCompatActivity() {

    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        sharedPreferences = getSharedPreferences("SettingsPrefs", Context.MODE_PRIVATE)

        // Initialize UI elements
        val pathEditText: EditText = findViewById(R.id.pathEditText)
        val themeSwitch: Switch = findViewById(R.id.themeSwitch)

        val healthyColorBtn: Button = findViewById(R.id.colorHealthyButton)
        val unhealthyColorBtn: Button = findViewById(R.id.colorUnhealthyButton)
        val weedColorBtn: Button = findViewById(R.id.colorWeedButton)

        val savedPath = sharedPreferences.getString("save_path", "defaultPath") ?: "defaultPath"
        val savedTheme = sharedPreferences.getString("theme", "light") ?: "light"
        val healthyColor = sharedPreferences.getInt("color_healthy", Color.GREEN)
        val unhealthyColor = sharedPreferences.getInt("color_unhealthy", Color.RED)
        val weedColor = sharedPreferences.getInt("color_weed", Color.YELLOW)

        // Set values
        pathEditText.setText(savedPath)
        themeSwitch.isChecked = savedTheme == "dark"

        healthyColorBtn.setBackgroundColor(healthyColor)
        unhealthyColorBtn.setBackgroundColor(unhealthyColor)
        weedColorBtn.setBackgroundColor(weedColor)

        // Set up color picker button
        var selectedHealthyColor = healthyColor
        healthyColorBtn.setOnClickListener {
            ColorPickerDialog(this) { color ->
                healthyColorBtn.setBackgroundColor(color)
                selectedHealthyColor = color
            }.show()
        }
        var selectedUnhealthyColor = unhealthyColor
        unhealthyColorBtn.setOnClickListener {
            ColorPickerDialog(this) { color ->
                unhealthyColorBtn.setBackgroundColor(color)
                selectedUnhealthyColor = color
            }.show()
        }
        var selectedWeedColor = weedColor
        weedColorBtn.setOnClickListener {
            ColorPickerDialog(this) { color ->
                weedColorBtn.setBackgroundColor(color)
                selectedWeedColor = color
            }.show()
        }

        // Save settings when changed
        findViewById<Button>(R.id.saveSettingsButton).setOnClickListener {
            val newPath = pathEditText.text.toString()
            val newHealthyColor = selectedHealthyColor
            val newUnhealthyColor = selectedUnhealthyColor
            val newWeedColor = selectedWeedColor

            Toast.makeText(
                this,
                "Settings Saved!",
                Toast.LENGTH_LONG
            ).show()

            // Save new settings to SharedPreferences
            sharedPreferences.edit().apply {
                putString("save_path", newPath)
                putInt("color_healthy", newHealthyColor)
                putInt("color_unhealthy", newUnhealthyColor)
                putInt("color_weed", newWeedColor)
            }.apply()

            // Go back to previous screen
            finish()
        }
    }
}

