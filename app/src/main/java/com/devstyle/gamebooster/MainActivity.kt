package com.devstyle.gamebooster

import android.app.Activity
import android.app.AppOpsManager
import android.app.usage.UsageStats
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.view.Gravity
import android.view.View
import android.view.animation.DecelerateInterpolator
import android.widget.*
import java.util.Calendar
import java.util.Locale

class MainActivity : Activity() {
    private val ink = Color.rgb(25, 28, 36)
    private val muted = Color.rgb(125, 132, 145)
    private val cyan = Color.rgb(55, 220, 220)
    private val pink = Color.rgb(238, 104, 183)
    private val paper = Color.rgb(241, 243, 248)
    private lateinit var content: LinearLayout
    private var currentTab = "Games"
    private var searchText = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.statusBarColor = paper
        window.navigationBarColor = Color.WHITE
        window.decorView.systemUiVisibility =
            View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR or View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(paper)
        }
        val scroll = ScrollView(this).apply { clipToPadding = false }
        content = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(18), dp(12), dp(18), dp(24))
        }
        scroll.addView(content)
        root.addView(scroll, LinearLayout.LayoutParams(-1, 0, 1f))
        root.addView(bottomNav())
        setContentView(root)
        render()
    }

    override fun onResume() {
        super.onResume()
        if (::content.isInitialized) render()
    }

    private fun render() {
        content.removeAllViews()
        content.addView(topBar())
        when (currentTab) {
            "Games" -> renderGames()
            "Engine" -> renderEngine()
            "Stats" -> renderStats()
            else -> renderSettings()
        }
    }

    private fun topBar(): View {
        val outer = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(dp(2), dp(8), dp(2), dp(18))
        }
        val brandColumn = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
        brandColumn.addView(TextView(this).apply {
            text = "DEV STYLE"
            textSize = 23f
            setTextColor(ink)
            typeface = Typeface.create("sans-serif", Typeface.BOLD)
            letterSpacing = .045f
        })
        brandColumn.addView(TextView(this).apply {
            text = "GAMING EXPERIENCE"
            textSize = 9f
            setTextColor(muted)
            letterSpacing = .18f
        })
        outer.addView(brandColumn, LinearLayout.LayoutParams(0, -2, 1f))
        val status = TextView(this).apply {
            text = "● READY"
            textSize = 10f
            setTextColor(Color.rgb(15, 151, 148))
            setPadding(dp(10), dp(8), dp(10), dp(8))
            background = rounded(Color.WHITE, 20, Color.rgb(224, 229, 237), 1)
        }
        outer.addView(status)
        return outer
    }

    private fun renderGames() {
        content.addView(sectionLabel("YOUR GAME SPACE"))
        content.addView(gap(8))
        val hero = panel()
        hero.addView(TextView(this).apply {
            text = "DAR-LINK ENGINE"
            textSize = 10f
            letterSpacing = .14f
            setTextColor(muted)
        })
        hero.addView(gap(7))
        hero.addView(text("Ready for your next match.", 24, ink, true))
        hero.addView(gap(5))
        hero.addView(text("Launch your installed games and open real Android gaming controls.", 13, muted, false))
        hero.addView(gap(15))
        hero.addView(button("OPEN GAME ENGINE", cyan) { currentTab = "Engine"; render() })
        content.addView(hero)
        content.addView(gap(15))

        val heading = LinearLayout(this).apply { gravity = Gravity.CENTER_VERTICAL }
        heading.addView(text("Installed games", 20, ink, true), LinearLayout.LayoutParams(0, -2, 1f))
        heading.addView(TextView(this).apply {
            text = "REFRESH ↻"
            textSize = 11f
            setTextColor(ink)
            setPadding(dp(12), dp(8), dp(12), dp(8))
            background = rounded(Color.WHITE, 14, Color.rgb(226, 230, 239), 1)
            setOnClickListener { searchText = ""; render() }
        })
        content.addView(heading)
        content.addView(gap(8))

        val search = EditText(this).apply {
            hint = "Search installed apps"
            textSize = 14f
            singleLine = true
            setTextColor(ink)
            setHintTextColor(muted)
            setPadding(dp(14), dp(10), dp(14), dp(10))
            background = rounded(Color.WHITE, 14, Color.rgb(226, 230, 239), 1)
            setText(searchText)
            addTextChangedListener(object : android.text.TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) { searchText = s?.toString().orEmpty() }
                override fun afterTextChanged(s: android.text.Editable?) {}
            })
        }
        content.addView(search)
        content.addView(gap(10))

        val games = discoverLaunchableApps().filter {
            packageManager.getApplicationLabel(it).toString().contains(searchText, ignoreCase = true)
        }
        if (games.isEmpty()) {
            val empty = panel()
            empty.addView(text("No matching apps found", 17, ink, true))
            empty.addView(gap(4))
            empty.addView(text("Check that your game is installed, then refresh the list.", 13, muted, false))
            content.addView(empty)
        } else {
            games.take(60).forEachIndexed { index, appInfo ->
                val item = gameCard(appInfo)
                item.alpha = 0f
                item.translationX = dp(34).toFloat()
                content.addView(item)
                item.animate().alpha(1f).translationX(0f)
                    .setStartDelay((index.coerceAtMost(8) * 35L))
                    .setDuration(280).setInterpolator(DecelerateInterpolator()).start()
                content.addView(gap(8))
            }
        }
        content.addView(gap(10))
        content.addView(text("Built for real Android capabilities. No fake FPS or network boost claims.", 12, muted, false))
    }

    private fun gameCard(info: ApplicationInfo): View {
        val row = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(dp(12), dp(12), dp(12), dp(12))
            background = rounded(Color.WHITE, 19, Color.rgb(229, 233, 241), 1)
        }
        val icon = ImageView(this).apply {
            setImageDrawable(packageManager.getApplicationIcon(info))
            scaleType = ImageView.ScaleType.FIT_CENTER
        }
        row.addView(icon, LinearLayout.LayoutParams(dp(49), dp(49)))
        val labels = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(12), 0, dp(5), 0)
        }
        labels.addView(text(packageManager.getApplicationLabel(info).toString(), 15, ink, true))
        labels.addView(text(info.packageName, 9, muted, false))
        row.addView(labels, LinearLayout.LayoutParams(0, -2, 1f))
        row.addView(TextView(this).apply {
            text = "START"
            textSize = 11f
            typeface = Typeface.DEFAULT_BOLD
            gravity = Gravity.CENTER
            setTextColor(ink)
            setPadding(dp(14), dp(12), dp(14), dp(12))
            background = rounded(cyan, 12, cyan, 1)
            setOnClickListener {
                val launchIntent = packageManager.getLaunchIntentForPackage(info.packageName)
                if (launchIntent == null) {
                    toast("Android did not expose a launch activity for this app.")
                } else {
                    try {
                        launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        startActivity(launchIntent)
                    } catch (e: Exception) {
                        toast("Could not open this app: ${e.localizedMessage ?: "launch blocked"}")
                    }
                }
            }
        })
        return row
    }

    private fun renderEngine() {
        content.addView(text("Dar-Link Engine", 25, ink, true))
        content.addView(gap(5))
        content.addView(text("Real device controls. Access is always managed by Android.", 13, muted, false))
        content.addView(gap(14))
        content.addView(engineCard("Notification focus", "Open Android Do Not Disturb access settings.", "OPEN DND SETTINGS") {
            openSetting(Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS)
        })
        content.addView(engineCard("Memory management", "Android automatically manages memory. Inspect device app controls instead of using a fake RAM cleaner.", "APP SETTINGS") {
            openSetting(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:$packageName"))
        })
        content.addView(engineCard("Network controls", "Open Android wireless settings to manage Wi-Fi and mobile data. This cannot guarantee lower ping.", "NETWORK SETTINGS") {
            openSetting(Settings.ACTION_WIRELESS_SETTINGS)
        })
        content.addView(engineCard("Battery and thermal", "Check battery settings. Thermal protection is controlled by the device and cannot be safely overridden by an ordinary app.", "BATTERY SETTINGS") {
            openSetting(Settings.ACTION_BATTERY_SAVER_SETTINGS)
        })
        content.addView(engineCard("Usage access", "Grant permission to read Android-reported usage statistics.", "USAGE ACCESS") {
            openSetting(Settings.ACTION_USAGE_ACCESS_SETTINGS)
        })
        content.addView(gap(4))
        content.addView(text("System limitation", 17, ink, true))
        content.addView(text("An ordinary third-party app cannot universally overclock CPU/GPU, unlock FPS, change a game's internal graphics settings, or force lower latency. Some phone brands provide their own privileged game mode.", 13, muted, false))
    }

    private fun renderStats() {
        content.addView(text("Usage analytics", 25, ink, true))
        content.addView(gap(5))
        content.addView(text("Statistics come from Android Usage Access; they are not guessed.", 13, muted, false))
        content.addView(gap(14))
        val allowed = hasUsageAccess()
        val summary = panel()
        summary.addView(sectionLabel("TODAY • APP USAGE"))
        summary.addView(gap(6))
        summary.addView(text(if (allowed) totalUsageToday() else "Permission required", 28, ink, true))
        summary.addView(gap(5))
        summary.addView(text(if (allowed) "Total foreground usage reported by Android." else "Grant Usage Access to view available usage records.", 13, muted, false))
        summary.addView(gap(12))
        summary.addView(button(if (allowed) "REFRESH STATISTICS" else "GRANT USAGE ACCESS", cyan) {
            if (!hasUsageAccess()) openSetting(Settings.ACTION_USAGE_ACCESS_SETTINGS) else render()
        })
        content.addView(summary)
        content.addView(gap(8))
        content.addView(text("Most-used apps today", 18, ink, true))
        content.addView(gap(8))
        if (!allowed) {
            content.addView(text("Open Usage Access settings, enable Dev Style, then return to this screen.", 13, muted, false))
        } else {
            val stats = todayUsage().sortedByDescending { it.totalTimeInForeground }.take(12)
            if (stats.isEmpty()) content.addView(text("Android has not provided usage records for today yet.", 13, muted, false))
            stats.forEach { stat ->
                val name = try {
                    packageManager.getApplicationLabel(packageManager.getApplicationInfo(stat.packageName, 0)).toString()
                } catch (_: Exception) { stat.packageName }
                val p = panel()
                p.addView(text(name, 15, ink, true))
                p.addView(text(formatDuration(stat.totalTimeInForeground), 13, muted, false))
                content.addView(p)
                content.addView(gap(7))
            }
        }
        content.addView(gap(6))
        content.addView(text("Per-game live network traffic is intentionally not shown: Android does not expose it consistently to ordinary apps across all devices.", 12, muted, false))
    }

    private fun renderSettings() {
        content.addView(text("Settings", 25, ink, true))
        content.addView(gap(5))
        content.addView(text("Dev Style • Game Booster • v2.0.0", 13, muted, false))
        content.addView(gap(14))
        content.addView(engineCard("App permissions", "Manage permissions from Android system settings.", "APP SETTINGS") {
            openSetting(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:$packageName"))
        })
        content.addView(engineCard("Display settings", "Open your device's display settings.", "DISPLAY SETTINGS") {
            openSetting(Settings.ACTION_DISPLAY_SETTINGS)
        })
        content.addView(engineCard("Wi-Fi settings", "Manage the current Wi-Fi connection.", "WI-FI SETTINGS") {
            openSetting(Settings.ACTION_WIFI_SETTINGS)
        })
        content.addView(engineCard("About Dev Style", "No account or remote backend is included. Usage statistics remain on-device; access is controlled by Android.", "OK") {
            toast("Dev Style Game Booster v2.0.0")
        })
    }

    private fun engineCard(title: String, subtitle: String, buttonText: String, action: () -> Unit): View {
        val p = panel()
        p.addView(text(title, 17, ink, true))
        p.addView(gap(4))
        p.addView(text(subtitle, 13, muted, false))
        p.addView(gap(12))
        p.addView(button(buttonText, Color.WHITE, action))
        return p
    }

    private fun bottomNav(): View {
        val bar = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER
            setPadding(dp(7), dp(7), dp(7), dp(7))
            setBackgroundColor(Color.WHITE)
        }
        listOf("Games", "Engine", "Stats", "Settings").forEach { tab ->
            val label = when (tab) {
                "Games" -> "▦\nGames"
                "Engine" -> "◇\nEngine"
                "Stats" -> "▥\nStats"
                else -> "⚙\nSettings"
            }
            bar.addView(TextView(this).apply {
                text = label
                textSize = 11f
                gravity = Gravity.CENTER
                setTextColor(if (currentTab == tab) Color.rgb(13, 151, 151) else muted)
                setPadding(dp(4), dp(4), dp(4), dp(4))
                setOnClickListener { currentTab = tab; render() }
            }, LinearLayout.LayoutParams(0, dp(48), 1f))
        }
        return bar
    }

    private fun discoverLaunchableApps(): List<ApplicationInfo> {
        return try {
            packageManager.getInstalledApplications(0)
                .filter { it.packageName != packageName && packageManager.getLaunchIntentForPackage(it.packageName) != null }
                .filter {
                    val flags = it.flags
                    // Keep both user-installed apps and system games/launchers, but skip core Android packages.
                    !(it.packageName.startsWith("com.android.") ||
                      it.packageName.startsWith("com.google.android.") ||
                      it.packageName == "android")
                }
                .sortedBy { packageManager.getApplicationLabel(it).toString().lowercase(Locale.getDefault()) }
        } catch (_: Exception) { emptyList() }
    }

    private fun hasUsageAccess(): Boolean {
        val ops = getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
        return try {
            ops.checkOpNoThrow("android:get_usage_stats", android.os.Process.myUid(), packageName) == AppOpsManager.MODE_ALLOWED
        } catch (_: Exception) { false }
    }

    private fun todayUsage(): List<UsageStats> {
        return try {
            val manager = getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
            val cal = Calendar.getInstance()
            cal.set(Calendar.HOUR_OF_DAY, 0)
            cal.set(Calendar.MINUTE, 0)
            cal.set(Calendar.SECOND, 0)
            cal.set(Calendar.MILLISECOND, 0)
            manager.queryUsageStats(UsageStatsManager.INTERVAL_DAILY, cal.timeInMillis, System.currentTimeMillis()) ?: emptyList()
        } catch (_: Exception) { emptyList() }
    }

    private fun totalUsageToday(): String = formatDuration(todayUsage().sumOf { it.totalTimeInForeground })

    private fun formatDuration(ms: Long): String {
        val minutes = ms.coerceAtLeast(0) / 60000
        return if (minutes >= 60) "${minutes / 60}h ${minutes % 60}m" else "${minutes}m"
    }

    private fun openSetting(action: String, uri: Uri? = null) {
        try {
            val intent = if (uri != null) Intent(action, uri) else Intent(action)
            startActivity(intent)
        } catch (_: Exception) {
            toast("This setting is not available on this device.")
        }
    }

    private fun panel(): LinearLayout = LinearLayout(this).apply {
        orientation = LinearLayout.VERTICAL
        setPadding(dp(16), dp(16), dp(16), dp(16))
        background = rounded(Color.WHITE, 22, Color.rgb(229, 233, 241), 1)
    }

    private fun text(value: String, size: Int, color: Int, bold: Boolean) = TextView(this).apply {
        text = value
        textSize = size.toFloat()
        setTextColor(color)
        typeface = if (bold) Typeface.create("sans-serif", Typeface.BOLD) else Typeface.create("sans-serif", Typeface.NORMAL)
        includeFontPadding = true
    }

    private fun sectionLabel(value: String) = TextView(this).apply {
        text = value
        textSize = 10f
        letterSpacing = .13f
        typeface = Typeface.DEFAULT_BOLD
        setTextColor(muted)
    }

    private fun gap(height: Int) = Space(this).apply { layoutParams = LinearLayout.LayoutParams(1, dp(height)) }

    private fun button(value: String, fill: Int, action: () -> Unit) = TextView(this).apply {
        text = value
        textSize = 11f
        typeface = Typeface.DEFAULT_BOLD
        letterSpacing = .035f
        gravity = Gravity.CENTER
        setTextColor(ink)
        setPadding(dp(12), dp(14), dp(12), dp(14))
        background = rounded(fill, 13, if (fill == cyan) cyan else Color.rgb(226, 230, 239), 1)
        setOnClickListener { action() }
    }

    private fun rounded(fill: Int, radius: Int, stroke: Int, strokeWidth: Int) = GradientDrawable().apply {
        setColor(fill)
        cornerRadius = dp(radius).toFloat()
        setStroke(dp(strokeWidth), stroke)
    }

    private fun dp(value: Int) = (value * resources.displayMetrics.density).toInt()
    private fun toast(value: String) = Toast.makeText(this, value, Toast.LENGTH_SHORT).show()
}
