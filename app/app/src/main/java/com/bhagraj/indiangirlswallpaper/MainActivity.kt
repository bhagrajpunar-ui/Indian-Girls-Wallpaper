
package com.bhagraj.indiangirlswallpaper

import android.app.Activity
import android.os.Bundle
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.view.Gravity
import android.widget.*

class MainActivity : Activity() {

    private val purple = Color.rgb(105, 45, 160)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        window.statusBarColor = purple
        window.navigationBarColor = Color.BLACK

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(Color.rgb(248, 245, 252))
        }

        val scroll = ScrollView(this)
        val content = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(18, 24, 18, 24)
        }

        val title = TextView(this).apply {
            text = "Indian Girls Wallpaper"
            textSize = 25f
            setTextColor(purple)
            typeface = Typeface.DEFAULT_BOLD
        }
        content.addView(title)

        val subtitle = TextView(this).apply {
            text = "Beautiful HD & 4K Wallpapers"
            textSize = 14f
            setTextColor(Color.DKGRAY)
            setPadding(0, 8, 0, 20)
        }
        content.addView(subtitle)

        val search = EditText(this).apply {
            hint = "Search wallpapers..."
            textSize = 15f
            setSingleLine(true)
            setPadding(16, 12, 16, 12)
            background = GradientDrawable().apply {
                setColor(Color.WHITE)
                cornerRadius = 18f
            }
        }
        content.addView(search)

        val heading = TextView(this).apply {
            text = "Explore Categories"
            textSize = 20f
            setTextColor(Color.BLACK)
            typeface = Typeface.DEFAULT_BOLD
            setPadding(0, 24, 0, 14)
        }
        content.addView(heading)

        val categories = listOf(
            "All Wallpapers",
            "Traditional",
            "Saree",
            "Lehenga",
            "Bridal",
            "Modern"
        )

        categories.forEach { category ->
            val button = TextView(this).apply {
                text = "  $category  "
                textSize = 16f
                setTextColor(purple)
                setPadding(12, 12, 12, 12)
                setBackgroundColor(Color.WHITE)
            }
            content.addView(button)
        }

        val popular = TextView(this).apply {
            text = "Popular Wallpapers"
            textSize = 20f
            setTextColor(Color.BLACK)
            typeface = Typeface.DEFAULT_BOLD
            setPadding(0, 25, 0, 12)
        }
        content.addView(popular)

        val info = TextView(this).apply {
            text = "HD wallpapers coming soon!"
            textSize = 16f
            gravity = Gravity.CENTER
            setTextColor(purple)
            setPadding(15, 40, 15, 40)
            setBackgroundColor(Color.WHITE)
        }
        content.addView(info)

        val footer = TextView(this).apply {
            text = "Made with ❤️ by BHAGRAJ"
            textSize = 14f
            gravity = Gravity.CENTER
            setTextColor(Color.DKGRAY)
            setPadding(10, 25, 10, 10)
        }
        content.addView(footer)

        scroll.addView(content)
        root.addView(scroll)
        setContentView(root)
    }
}
