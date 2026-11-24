package com.bhagraj.indiangirlswallpaper

import android.app.Activity
import android.app.DownloadManager
import android.app.WallpaperManager
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.ColorDrawable
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.os.Looper
import android.view.Gravity
import android.widget.*
import android.text.Editable
import android.text.TextWatcher
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import java.util.concurrent.Executors

data class Wallpaper(
    val title: String,
    val category: String,
    val previewUrl: String,
    val imageUrl: String
)

class MainActivity : Activity() {
    private val purple = Color.rgb(104, 55, 165)
    private val darkPurple = Color.rgb(66, 30, 112)
    private val apiBase = "https://indian-girls-wallpaper-api.bhagrajpunar.workers.dev/"
    private val worker = Executors.newFixedThreadPool(4)
    private val handler = Handler(Looper.getMainLooper())
    private val searchRunnable = Runnable { fetchWallpapers() }

    private lateinit var gallery: LinearLayout
    private lateinit var count: TextView
    private lateinit var status: TextView
    private lateinit var chips: LinearLayout
    private lateinit var scrollContainer: ScrollView
    private var category = "all"
    private var query = ""
    private var requestNumber = 0
    private var currentPage = 1
    private var loadedCount = 0
    private var totalAvailable = 0
    private var isLoading = false
    private var canLoadMore = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.statusBarColor = darkPurple
        window.navigationBarColor = Color.BLACK

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(Color.rgb(248, 246, 252))
        }
        scrollContainer = ScrollView(this)
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
            text = "MADE BY BHAGRAJ"
            textSize = 12f
            setTextColor(purple)
            typeface = Typeface.DEFAULT_BOLD
            setPadding(0, dp(4), 0, dp(2))
        })

        // Developer profile card using the launcher photo.
        val developerCard = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(dp(12), dp(10), dp(12), dp(10))
            background = rounded(Color.WHITE, 16)
            isClickable = true
            isFocusable = true
            setOnClickListener { showDeveloperProfile() }
        }
        developerCard.addView(ImageView(this).apply {
            layoutParams = LinearLayout.LayoutParams(dp(58), dp(58))
            setImageResource(R.drawable.ic_launcher)
            scaleType = ImageView.ScaleType.CENTER_CROP
            background = GradientDrawable().apply {
                shape = GradientDrawable.OVAL
                setColor(Color.LTGRAY)
            }
            clipToOutline = true
            contentDescription = "Bhagraj developer profile photo"
        })
        val developerText = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(12), 0, 0, 0)
        }
        developerText.addView(TextView(this).apply {
            text = "BHAGRAJ"
            textSize = 16f
            setTextColor(darkPurple)
            typeface = Typeface.DEFAULT_BOLD
        })
        developerText.addView(TextView(this).apply {
            text = "App Developer • Creator"
            textSize = 12f
            setTextColor(Color.DKGRAY)
            setPadding(0, dp(3), 0, 0)
        })
        developerCard.addView(developerText, LinearLayout.LayoutParams(0, -2, 1f))
        developerCard.addView(TextView(this).apply {
            text = "About"
            textSize = 12f
            typeface = Typeface.DEFAULT_BOLD
            setTextColor(purple)
        })
        content.addView(developerCard, LinearLayout.LayoutParams(-1, -2).apply {
            topMargin = dp(10)
            bottomMargin = dp(4)
        })

        content.addView(TextView(this).apply {
            text = "Beautiful portraits • HD collection"
            textSize = 14f
            setTextColor(Color.DKGRAY)
            setPadding(0, dp(5), 0, dp(16))
        })

        val search = EditText(this).apply {
            hint = "Search: saree, bridal, traditional..."
            isSingleLine = true
            textSize = 15f
            setPadding(dp(14), dp(10), dp(14), dp(10))
            background = rounded(Color.WHITE, 14)
        }
        content.addView(search, LinearLayout.LayoutParams(-1, dp(50)))

        content.addView(label("Explore Categories"))
        chips = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL }
        val chipScroll = HorizontalScrollView(this).apply {
            isHorizontalScrollBarEnabled = false
        }
        listOf(
            "All" to "all",
            "Traditional" to "traditional",
            "Saree" to "saree",
            "Lehenga" to "lehenga",
            "Bridal" to "bridal",
            "Modern" to "modern"
        ).forEach { (name, value) ->
            val chip = TextView(this).apply {
                text = name
                textSize = 13f
                gravity = Gravity.CENTER
                setPadding(dp(14), dp(8), dp(14), dp(8))
                setOnClickListener {
                    category = value
                    updateChips()
                    fetchWallpapers(reset = true)
                }
            }
            chips.addView(chip, LinearLayout.LayoutParams(-2, dp(40)).apply {
                rightMargin = dp(7)
            })
        }
        chipScroll.addView(chips)
        content.addView(chipScroll)

        val heading = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
        }
        heading.addView(label("Wallpapers"), LinearLayout.LayoutParams(0, -2, 1f))
        count = TextView(this).apply {
            textSize = 12f
            setTextColor(purple)
        }
        heading.addView(count)
        content.addView(heading)

        status = TextView(this).apply {
            text = "Loading HD wallpapers..."
            textSize = 13f
            setTextColor(Color.DKGRAY)
            setPadding(0, dp(4), 0, dp(10))
        }
        content.addView(status)

        gallery = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
        content.addView(gallery)

        content.addView(TextView(this).apply {
            text = "© MADE BY BHAGRAJ"
            gravity = Gravity.CENTER
            setTextColor(Color.GRAY)
            setPadding(0, dp(25), 0, dp(8))
        })

        search.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                query = s?.toString()?.trim() ?: ""
                handler.removeCallbacks(searchRunnable)
                handler.postDelayed(searchRunnable, 550)
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        scrollContainer.addView(content)
        scrollContainer.setOnScrollChangeListener { view, _, scrollY, _, _ ->
            val distanceToBottom = content.height - (scrollY + view.height)
            if (distanceToBottom < dp(650)) loadMoreWallpapers()
        }
        root.addView(scrollContainer)
        setContentView(root)
        updateChips()
        fetchWallpapers(reset = true)
    }

    private fun showDeveloperProfile() {
        val box = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER_HORIZONTAL
            setPadding(dp(24), dp(24), dp(24), dp(20))
            background = rounded(Color.WHITE, 20)
        }
        box.addView(ImageView(this).apply {
            layoutParams = LinearLayout.LayoutParams(dp(112), dp(112))
            setImageResource(R.drawable.ic_launcher)
            scaleType = ImageView.ScaleType.CENTER_CROP
            background = GradientDrawable().apply {
                shape = GradientDrawable.OVAL
                setColor(Color.LTGRAY)
            }
            clipToOutline = true
            contentDescription = "Bhagraj developer photo"
        })
        box.addView(TextView(this).apply {
            text = "MADE BY BHAGRAJ"
            textSize = 19f
            typeface = Typeface.DEFAULT_BOLD
            setTextColor(darkPurple)
            gravity = Gravity.CENTER
            setPadding(0, dp(14), 0, dp(4))
        })
        box.addView(TextView(this).apply {
            text = "Developer & Creator of Indian Girls Wallpaper"
            textSize = 13f
            setTextColor(Color.DKGRAY)
            gravity = Gravity.CENTER
        })
        box.addView(TextView(this).apply {
            text = "Thank you for using my app!"
            textSize = 12f
            setTextColor(purple)
            gravity = Gravity.CENTER
            setPadding(0, dp(10), 0, dp(4))
        })
        val dialog = android.app.Dialog(this)
        box.addView(action("Close") { dialog.dismiss() },
            LinearLayout.LayoutParams(-1, dp(44)).apply { topMargin = dp(14) })
        dialog.setContentView(box)
        dialog.show()
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        dialog.window?.setLayout((resources.displayMetrics.widthPixels * 0.88f).toInt(), -2)
    }

    private fun fetchWallpapers(reset: Boolean = true) {
        if (reset) {
            requestNumber++
            currentPage = 1
            loadedCount = 0
            totalAvailable = 0
            isLoading = false
            canLoadMore = true
            gallery.removeAllViews()
        }
        if (isLoading || !canLoadMore) return

        isLoading = true
        val thisRequest = requestNumber
        val pageToLoad = currentPage
        handler.post {
            status.text = if (pageToLoad == 1) "Searching HD wallpapers..." else "Loading more HD wallpapers..."
            if (pageToLoad == 1) count.text = ""
        }

        worker.execute {
            var connection: HttpURLConnection? = null
            try {
                val encodedQuery = URLEncoder.encode(query.ifBlank { "indian girl portrait" }, "UTF-8")
                val endpoint = apiBase +
                    "?category=" + URLEncoder.encode(category, "UTF-8") +
                    "&query=" + encodedQuery +
                    "&page=" + pageToLoad + "&perPage=80"

                connection = (URL(endpoint).openConnection() as HttpURLConnection).apply {
                    requestMethod = "GET"
                    connectTimeout = 15000
                    readTimeout = 20000
                    setRequestProperty("Accept", "application/json")
                }
                val code = connection.responseCode
                val body = (if (code in 200..299) connection.inputStream else connection.errorStream)
                    ?.bufferedReader()?.use { it.readText() } ?: ""
                if (code !in 200..299) throw Exception("Server returned HTTP $code")

                val json = JSONObject(body)
                if (!json.optBoolean("success", false)) {
                    throw Exception(json.optString("error", "Wallpaper search failed"))
                }
                val array = json.optJSONArray("wallpapers")
                    ?: throw Exception("No wallpaper list received")
                val apiTotal = json.optInt("totalHits", json.optInt("total", 0))

                val results = ArrayList<Wallpaper>()
                for (i in 0 until array.length()) {
                    val item = array.optJSONObject(i) ?: continue
                    val preview = item.optString("image").ifBlank {
                        item.optString("preview")
                    }
                    val full = item.optString("largeImage").ifBlank {
                        item.optString("image").ifBlank { item.optString("preview") }
                    }
                    if (preview.isNotBlank() && full.isNotBlank()) {
                        results.add(
                            Wallpaper(
                                title = item.optString("title", "Indian Wallpaper"),
                                category = category.replaceFirstChar { it.uppercase() },
                                previewUrl = preview,
                                imageUrl = full
                            )
                        )
                    }
                }
                handler.post {
                    if (thisRequest == requestNumber) {
                        if (apiTotal > 0) totalAvailable = minOf(apiTotal, 500)
                        showResults(results, append = pageToLoad > 1)
                        loadedCount += results.size
                        currentPage = pageToLoad + 1
                        canLoadMore = results.isNotEmpty() &&
                            (totalAvailable == 0 || loadedCount < totalAvailable)
                        count.text = if (totalAvailable > 0) {
                            "$loadedCount / $totalAvailable wallpapers"
                        } else {
                            "$loadedCount wallpapers"
                        }
                        status.text = when {
                            canLoadMore -> "Pixabay HD wallpapers • Scroll down for more"
                            loadedCount > 0 -> "You've reached the available wallpapers for this search"
                            else -> "Is search mein wallpaper nahi mila. Dusra naam try karein."
                        }
                        isLoading = false
                    }
                }
            } catch (error: Exception) {
                handler.post {
                    if (thisRequest == requestNumber) {
                        isLoading = false
                        status.text = if (pageToLoad == 1) {
                            "Wallpaper load nahi hua: ${error.message ?: "Internet check karein"}"
                        } else {
                            "More wallpapers load nahi hue. Retry par tap karein."
                        }
                        if (pageToLoad == 1) {
                            count.text = ""
                            gallery.removeAllViews()
                        }
                        gallery.addView(action("Retry loading wallpapers") {
                            fetchWallpapers(reset = pageToLoad == 1)
                        }, LinearLayout.LayoutParams(-1, dp(46)))
                    }
                }
            } finally {
                connection?.disconnect()
            }
        }
    }

    private fun loadMoreWallpapers() {
        if (!isLoading && canLoadMore && loadedCount > 0) {
            fetchWallpapers(reset = false)
        }
    }

    private fun showResults(results: List<Wallpaper>, append: Boolean = false) {
        if (!append) gallery.removeAllViews()
        if (results.isEmpty()) return

        results.chunked(2).forEach { pair ->
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
                load(item.previewUrl, image)
                card.addView(TextView(this).apply {
                    text = item.title
                    textSize = 12f
                    setTextColor(Color.DKGRAY)
                    setPadding(dp(9), dp(8), dp(5), dp(10))
                    maxLines = 2
                })
                row.addView(card, LinearLayout.LayoutParams(0, -2, 1f).apply {
                    leftMargin = dp(5)
                    rightMargin = dp(5)
                    bottomMargin = dp(12)
                })
            }
            if (pair.size == 1) {
                row.addView(android.view.View(this), LinearLayout.LayoutParams(0, 1, 1f))
            }
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
        val image = ImageView(this).apply {
            scaleType = ImageView.ScaleType.FIT_CENTER
            setBackgroundColor(Color.BLACK)
        }
        box.addView(image, LinearLayout.LayoutParams(-1, 0, 1f))
        load(item.imageUrl, image)
        box.addView(TextView(this).apply {
            text = item.title
            gravity = Gravity.CENTER
            textSize = 16f
            setTextColor(Color.WHITE)
            setPadding(0, dp(8), 0, dp(8))
        })
        val actions = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL }
        actions.addView(action("Download") { download(item) },
            LinearLayout.LayoutParams(0, dp(46), 1f).apply { rightMargin = dp(4) })
        actions.addView(action("Set Wallpaper") { setWallpaper(item) },
            LinearLayout.LayoutParams(0, dp(46), 1f).apply { leftMargin = dp(4) })
        box.addView(actions)
        box.addView(action("Share") { share(item) },
            LinearLayout.LayoutParams(-1, dp(44)).apply { topMargin = dp(7) })
        box.addView(action("Close") { dialog.dismiss() },
            LinearLayout.LayoutParams(-1, dp(42)).apply { topMargin = dp(7) })
        dialog.setContentView(box)
        dialog.show()
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        dialog.window?.setLayout(-1, (resources.displayMetrics.heightPixels * 0.88f).toInt())
    }

    private fun load(url: String, target: ImageView) {
        // Glide handles redirects, image decoding, downsampling and memory/disk caching.
        target.setImageDrawable(ColorDrawable(Color.rgb(235, 232, 240)))
        Glide.with(this)
            .load(url)
            .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
            .placeholder(ColorDrawable(Color.rgb(235, 232, 240)))
            .error(ColorDrawable(Color.rgb(225, 221, 232)))
            .into(target)
    }

    private fun download(item: Wallpaper) {
        try {
            val request = DownloadManager.Request(Uri.parse(item.imageUrl))
                .setTitle(item.title)
                .setDescription("Downloading HD wallpaper")
                .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                .setDestinationInExternalPublicDir(
                    Environment.DIRECTORY_PICTURES,
                    "IndianGirlsWallpaper/${item.title.replace(" ", "_")}.jpg"
                )
            (getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager).enqueue(request)
            toast("Download started. Check notifications.")
        } catch (_: Exception) {
            toast("Download could not start. Check internet.")
        }
    }

    private fun setWallpaper(item: Wallpaper) {
        toast("Downloading wallpaper image...")
        Glide.with(this)
            .asBitmap()
            .load(item.imageUrl)
            .override(1440, 2560)
            .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
            .into(object : CustomTarget<Bitmap>() {
                override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                    try {
                        WallpaperManager.getInstance(this@MainActivity).setBitmap(resource)
                        toast("Wallpaper set successfully!")
                    } catch (_: Exception) {
                        toast("Could not set wallpaper. Try again.")
                    }
                }

                override fun onLoadCleared(placeholder: android.graphics.drawable.Drawable?) {}

                override fun onLoadFailed(errorDrawable: android.graphics.drawable.Drawable?) {
                    toast("Image could not load. Check internet and retry.")
                }
            })
    }

    private fun share(item: Wallpaper) {
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, item.title + "\n" + item.imageUrl)
        }
        startActivity(Intent.createChooser(intent, "Share wallpaper"))
    }

    private fun updateChips() {
        for (i in 0 until chips.childCount) {
            val chip = chips.getChildAt(i) as TextView
            val active = when (chip.text.toString()) {
                "All" -> "all"
                "Traditional" -> "traditional"
                "Saree" -> "saree"
                "Lehenga" -> "lehenga"
                "Bridal" -> "bridal"
                else -> "modern"
            } == category
            chip.background = rounded(if (active) purple else Color.WHITE, 24)
            chip.setTextColor(if (active) Color.WHITE else darkPurple)
        }
    }

    private fun label(value: String) = TextView(this).apply {
        text = value
        textSize = 19f
        setTextColor(Color.BLACK)
        typeface = Typeface.DEFAULT_BOLD
        setPadding(0, dp(20), 0, dp(12))
    }

    private fun action(value: String, fn: () -> Unit) = TextView(this).apply {
        text = value
        gravity = Gravity.CENTER
        textSize = 13f
        typeface = Typeface.DEFAULT_BOLD
        setTextColor(Color.WHITE)
        background = rounded(purple, 12)
        setOnClickListener { fn() }
    }

    private fun rounded(color: Int, radius: Int) = GradientDrawable().apply {
        setColor(color)
        cornerRadius = dp(radius).toFloat()
    }

    private fun dp(value: Int) = (value * resources.displayMetrics.density).toInt()
    private fun toast(value: String) = Toast.makeText(this, value, Toast.LENGTH_SHORT).show()

    override fun onDestroy() {
        handler.removeCallbacks(searchRunnable)
        worker.shutdownNow()
        super.onDestroy()
    }
}
