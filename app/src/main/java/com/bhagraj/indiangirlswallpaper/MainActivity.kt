package com.bhagraj.indiangirlswallpaper

import android.app.Activity
import android.app.DownloadManager
import android.app.WallpaperManager
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.os.Looper
import android.view.Gravity
import android.view.ViewGroup
import android.widget.*
import android.text.Editable
import android.text.TextWatcher
import java.net.HttpURLConnection
import java.net.URL
import java.util.concurrent.Executors

data class Wallpaper(val title: String, val category: String, val url: String)

class MainActivity : Activity() {
    private val purple = Color.rgb(104, 55, 165)
    private val darkPurple = Color.rgb(66, 30, 112)
    private val worker = Executors.newFixedThreadPool(3)
    private val handler = Handler(Looper.getMainLooper())
    private lateinit var gallery: LinearLayout
    private lateinit var count: TextView
    private var category = "All"
    private var query = ""

    private val items = listOf(
        Wallpaper("Graceful Portrait", "Traditional", "https://images.unsplash.com/photo-1534528741775-53994a69daeb?auto=format&fit=crop&w=900&q=85"),
        Wallpaper("Elegant Style", "Modern", "https://images.unsplash.com/photo-1524504388940-b1c1722653e1?auto=format&fit=crop&w=900&q=85"),
        Wallpaper("Beautiful Look", "Saree", "https://images.unsplash.com/photo-1531123897727-8f129e1688ce?auto=format&fit=crop&w=900&q=85"),
        Wallpaper("Fashion Portrait", "Modern", "https://images.unsplash.com/photo-1529139574466-a303027c1d8b?auto=format&fit=crop&w=900&q=85"),
        Wallpaper("Classic Beauty", "Traditional", "https://images.unsplash.com/photo-1488426862026-3ee34a7d66df?auto=format&fit=crop&w=900&q=85"),
        Wallpaper("Soft Glamour", "Bridal", "https://images.unsplash.com/photo-1508214751196-bcfd4ca60f91?auto=format&fit=crop&w=900&q=85"),
        Wallpaper("Festive Mood", "Lehenga", "https://images.unsplash.com/photo-1517841905240-472988babdf9?auto=format&fit=crop&w=900&q=85"),
        Wallpaper("Beauty in Style", "Saree", "https://images.unsplash.com/photo-1524250502761-1ac6f2e30d43?auto=format&fit=crop&w=900&q=85"),
        Wallpaper("Natural Glow", "Traditional", "https://images.unsplash.com/photo-1544005313-94ddf0286df2?auto=format&fit=crop&w=900&q=85"),
        Wallpaper("Portrait Mood", "Bridal", "https://images.unsplash.com/photo-1512316609839-ce289d3d7b8c?auto=format&fit=crop&w=900&q=85")
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.statusBarColor = darkPurple
        window.navigationBarColor = Color.BLACK
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(Color.rgb(248, 246, 252))
        }
        val scroll = ScrollView(this)
        val content = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(16), dp(18), dp(16), dp(24))
        }
        content.addView(TextView(this).apply {
            text = "INDIAN GIRLS WALLPAPER"
            textSize = 22f
            setTextColor(darkPurple)
            typeface = Typeface.DEFAULT_BOLD
        })
        content.addView(TextView(this).apply {
            text = "Beautiful portraits • HD collection"
            textSize = 14f
            setTextColor(Color.DKGRAY)
            setPadding(0, dp(5), 0, dp(16))
        })
        val search = EditText(this).apply {
            hint = "Search wallpapers..."
            singleLine = true
            textSize = 15f
            setPadding(dp(14), dp(10), dp(14), dp(10))
            background = rounded(Color.WHITE, 14)
        }
        content.addView(search, LinearLayout.LayoutParams(-1, dp(50)))
        content.addView(label("Explore Categories"))
        val chips = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL }
        val chipScroll = HorizontalScrollView(this).apply { isHorizontalScrollBarEnabled = false }
        listOf("All", "Traditional", "Saree", "Lehenga", "Bridal", "Modern").forEach { name ->
            val chip = TextView(this).apply {
                text = name
                textSize = 13f
                setTextColor(Color.WHITE)
                setPadding(dp(14), dp(10), dp(14), dp(10))
                background = rounded(purple, 24)
                setOnClickListener { category = name; render(); updateChips(chips) }
            }
            chips.addView(chip, LinearLayout.LayoutParams(-2, dp(40)).apply { rightMargin = dp(7) })
        }
        chipScroll.addView(chips)
        content.addView(chipScroll)
        val heading = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL; gravity = Gravity.CENTER_VERTICAL }
        heading.addView(label("Popular Wallpapers"), LinearLayout.LayoutParams(0, -2, 1f))
        count = TextView(this).apply { textSize = 12f; setTextColor(purple) }
        heading.addView(count)
        content.addView(heading)
        gallery = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
        content.addView(gallery)
        content.addView(TextView(this).apply {
            text = "Made with ❤️ by BHAGRAJ"
            gravity = Gravity.CENTER
            setTextColor(Color.GRAY)
            setPadding(0, dp(25), 0, dp(8))
        })
        search.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                query = s?.toString()?.trim()?.lowercase() ?: ""
                render()
            }
            override fun afterTextChanged(s: Editable?) {}
        })
        scroll.addView(content)
        root.addView(scroll)
        setContentView(root)
        render()
        updateChips(chips)
    }

    private fun render() {
        if (!::gallery.isInitialized) return
        gallery.removeAllViews()
        val filtered = items.filter {
            (category == "All" || it.category.equals(category, true)) &&
            (query.isBlank() || it.title.lowercase().contains(query) || it.category.lowercase().contains(query))
        }
        count.text = "${filtered.size} wallpapers"
        if (filtered.isEmpty()) {
            gallery.addView(TextView(this).apply { text = "No wallpapers found"; gravity = Gravity.CENTER; setPadding(0, dp(30), 0, dp(30)) })
            return
        }
        filtered.chunked(2).forEach { pair ->
            val row = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL }
            pair.forEach { item ->
                val card = LinearLayout(this).apply {
                    orientation = LinearLayout.VERTICAL
                    background = rounded(Color.WHITE, 14)
                    clipToOutline = true
                    setOnClickListener { preview(item) }
                }
                val image = ImageView(this).apply {
                    layoutParams = LinearLayout.LayoutParams(-1, dp(220))
                    scaleType = ImageView.ScaleType.CENTER_CROP
                    setBackgroundColor(Color.LTGRAY)
                    contentDescription = item.title
                }
                card.addView(image)
                load(item.url, image)
                card.addView(TextView(this).apply {
                    text = item.title + "\n" + item.category
                    textSize = 12f
                    setTextColor(Color.DKGRAY)
                    setPadding(dp(9), dp(8), dp(5), dp(10))
                })
                row.addView(card, LinearLayout.LayoutParams(0, -2, 1f).apply {
                    leftMargin = dp(5); rightMargin = dp(5); bottomMargin = dp(12)
                })
            }
            if (pair.size == 1) row.addView(android.view.View(this), LinearLayout.LayoutParams(0, 1, 1f))
            gallery.addView(row)
        }
    }

    private fun preview(item: Wallpaper) {
        val dialog = android.app.Dialog(this)
        val box = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(12), dp(12), dp(12), dp(12))
            setBackgroundColor(Color.BLACK)
        }
        val image = ImageView(this).apply { scaleType = ImageView.ScaleType.FIT_CENTER; setBackgroundColor(Color.BLACK) }
        box.addView(image, LinearLayout.LayoutParams(-1, 0, 1f))
        load(item.url, image)
        box.addView(TextView(this).apply {
            text = item.title; gravity = Gravity.CENTER; textSize = 16f
            setTextColor(Color.WHITE); setPadding(0, dp(8), 0, dp(8))
        })
        val actions = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL }
        actions.addView(action("Download") { download(item) }, LinearLayout.LayoutParams(0, dp(46), 1f).apply { rightMargin = dp(4) })
        actions.addView(action("Set Wallpaper") { setWallpaper(item) }, LinearLayout.LayoutParams(0, dp(46), 1f).apply { leftMargin = dp(4) })
        box.addView(actions)
        box.addView(action("Share") { share(item) }, LinearLayout.LayoutParams(-1, dp(44)).apply { topMargin = dp(7) })
        box.addView(action("Close") { dialog.dismiss() }, LinearLayout.LayoutParams(-1, dp(42)).apply { topMargin = dp(7) })
        dialog.setContentView(box)
        dialog.show()
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        dialog.window?.setLayout(-1, (resources.displayMetrics.heightPixels * 0.88f).toInt())
    }

    private fun load(url: String, target: ImageView) {
        target.tag = url
        worker.execute {
            try {
                val c = URL(url).openConnection() as HttpURLConnection
                c.connectTimeout = 12000; c.readTimeout = 15000
                val bitmap = c.inputStream.use { BitmapFactory.decodeStream(it) }
                c.disconnect()
                handler.post { if (target.tag == url && bitmap != null) target.setImageBitmap(bitmap) }
            } catch (_: Exception) {
                handler.post { if (target.tag == url) target.setImageResource(android.R.drawable.ic_menu_gallery) }
            }
        }
    }

    private fun download(item: Wallpaper) {
        try {
            val request = DownloadManager.Request(Uri.parse(item.url))
                .setTitle(item.title)
                .setDescription("Downloading wallpaper")
                .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                .setDestinationInExternalPublicDir(Environment.DIRECTORY_PICTURES, "IndianGirlsWallpaper/${item.title.replace(" ", "_")}.jpg")
            (getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager).enqueue(request)
            toast("Download started. Check notifications.")
        } catch (_: Exception) { toast("Download could not start. Check internet.") }
    }

    private fun setWallpaper(item: Wallpaper) {
        toast("Setting wallpaper...")
        worker.execute {
            try {
                val c = URL(item.url).openConnection() as HttpURLConnection
                c.connectTimeout = 12000; c.readTimeout = 15000
                val bitmap = c.inputStream.use { BitmapFactory.decodeStream(it) }
                c.disconnect()
                if (bitmap == null) throw IllegalStateException()
                WallpaperManager.getInstance(this).setBitmap(bitmap)
                handler.post { toast("Wallpaper set successfully!") }
            } catch (_: Exception) { handler.post { toast("Could not set wallpaper. Try again.") } }
        }
    }

    private fun share(item: Wallpaper) {
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, item.title + "\n" + item.url)
        }
        startActivity(Intent.createChooser(intent, "Share wallpaper"))
    }

    private fun updateChips(row: LinearLayout) {
        for (i in 0 until row.childCount) {
            val chip = row.getChildAt(i) as TextView
            val active = chip.text.toString() == category
            chip.background = rounded(if (active) purple else Color.WHITE, 24)
            chip.setTextColor(if (active) Color.WHITE else darkPurple)
        }
    }

    private fun label(value: String) = TextView(this).apply {
        text = value; textSize = 19f; setTextColor(Color.BLACK)
        typeface = Typeface.DEFAULT_BOLD; setPadding(0, dp(20), 0, dp(12))
    }
    private fun action(value: String, fn: () -> Unit) = TextView(this).apply {
        text = value; gravity = Gravity.CENTER; textSize = 12f
        typeface = Typeface.DEFAULT_BOLD; setTextColor(Color.WHITE)
        background = rounded(purple, 12); setOnClickListener { fn() }
    }
    private fun rounded(color: Int, radius: Int) = GradientDrawable().apply {
        setColor(color); cornerRadius = dp(radius).toFloat()
    }
    private fun dp(value: Int) = (value * resources.displayMetrics.density).toInt()
    private fun toast(value: String) = Toast.makeText(this, value, Toast.LENGTH_SHORT).show()

    override fun onDestroy() {
        super.onDestroy()
        worker.shutdown()
    }
}