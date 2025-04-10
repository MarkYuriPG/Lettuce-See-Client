package com.example.lettuce_see_client

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.view.ViewGroup
import android.widget.Button
import android.widget.GridLayout

class ColorPickerDialog(private val context: Context, private val listener: (Int) -> Unit) : Dialog(context) {

    private lateinit var colorPalette: GridLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.dialog_color_picker)

        colorPalette = findViewById(R.id.colorPalette)

        // Add colors to palette
        val colors = listOf(
            Color.RED, Color.GREEN, Color.BLUE, Color.YELLOW, Color.CYAN, Color.MAGENTA,
            Color.BLACK, Color.WHITE, Color.GRAY, Color.parseColor("#FF6347"), Color.parseColor("#4682B4")
        )

        // Dynamically create color buttons
        for (color in colors) {
            val colorButton = Button(context)
            colorButton.setBackgroundColor(color)
            colorButton.layoutParams = ViewGroup.LayoutParams(100, 100) // Set size for the button
            colorButton.setOnClickListener {
                listener(color)
                dismiss() // Close the dialog when a color is picked
            }
            colorPalette.addView(colorButton)
        }
    }
}
