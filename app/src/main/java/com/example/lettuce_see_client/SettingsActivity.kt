package com.example.lettuce_see_client

import android.content.Context
import android.content.Intent
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
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
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

        fun tintCircleButton(context: Context, button: Button, color: Int) {
            val baseDrawable = ContextCompat.getDrawable(context, R.drawable.circle_button)!!.mutate()
            val wrappedDrawable = DrawableCompat.wrap(baseDrawable)
            DrawableCompat.setTint(wrappedDrawable, color)
            button.background = wrappedDrawable
        }

        tintCircleButton(this, healthyColorBtn, healthyColor)
        tintCircleButton(this, unhealthyColorBtn, unhealthyColor)
        tintCircleButton(this, weedColorBtn, weedColor)

        var selectedHealthyColor = healthyColor
        var selectedUnhealthyColor = unhealthyColor
        var selectedWeedColor = weedColor

        healthyColorBtn.setOnClickListener {
            ColorPickerDialog(this) { color ->
                tintCircleButton(this, healthyColorBtn, color)
                selectedHealthyColor = color
            }.show()
        }
        unhealthyColorBtn.setOnClickListener {
            ColorPickerDialog(this) { color ->
                tintCircleButton(this, unhealthyColorBtn, color)
                selectedUnhealthyColor = color
            }.show()
        }
        weedColorBtn.setOnClickListener {
            ColorPickerDialog(this) { color ->
                tintCircleButton(this, weedColorBtn, color)
                selectedWeedColor = color
            }.show()
        }

        // Save settings when changed
        findViewById<Button>(R.id.saveSettingsButton).setOnClickListener {
            val newPath = pathEditText.text.toString()
            val isDarkMode = themeSwitch.isChecked
            val newTheme = if (isDarkMode) "dark" else "light"
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
                putString("theme", newTheme)
                putInt("color_healthy", newHealthyColor)
                putInt("color_unhealthy", newUnhealthyColor)
                putInt("color_weed", newWeedColor)
            }.apply()

            // Restart MainActivity to apply theme change
            val intent = Intent(this, MainActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
            finish() // finish SettingsActivity
        }
    }
}

