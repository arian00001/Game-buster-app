package com.devstyle.gamebooster

import android.app.Activity
import android.app.AppOpsManager
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.view.View
import android.webkit.JavascriptInterface
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import org.json.JSONArray
import org.json.JSONObject
import java.util.Calendar

class MainActivity : Activity() {
    private lateinit var web: WebView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.statusBarColor = android.graphics.Color.rgb(235, 238, 246)
        window.navigationBarColor = android.graphics.Color.WHITE
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR or View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR
        web = WebView(this).apply {
            setBackgroundColor(android.graphics.Color.rgb(235, 238, 246))
            settings.javaScriptEnabled = true
            settings.domStorageEnabled = true
            settings.allowFileAccess = true // Required only for the bundled local HTML UI.
            settings.allowContentAccess = false
            settings.setSupportZoom(false)
            webChromeClient = WebChromeClient()
            webViewClient = WebViewClient()
            addJavascriptInterface(Bridge(), "DevStyle")
            loadUrl("file:///android_asset/index.html")
        }
        setContentView(web)
    }

    override fun onResume() {
        super.onResume()
        if (::web.isInitialized) web.postDelayed({ web.evaluateJavascript("window.refreshNativeData && window.refreshNativeData()", null) }, 250)
    }

    inner class Bridge {
        @JavascriptInterface fun getApps(): String {
            val out = JSONArray()
            val apps = try {
                packageManager.getInstalledApplications(0)
                    .filter { it.packageName != packageName && packageManager.getLaunchIntentForPackage(it.packageName) != null }
                    .filter { !it.packageName.startsWith("com.android.") && !it.packageName.startsWith("com.google.android.") && it.packageName != "android" }
                    .sortedBy { packageManager.getApplicationLabel(it).toString().lowercase() }
            } catch (_: Exception) { emptyList<ApplicationInfo>() }
            for (app in apps.take(150)) out.put(JSONObject().put("name", packageManager.getApplicationLabel(app).toString()).put("packageName", app.packageName))
            return out.toString()
        }
        @JavascriptInterface fun launchApp(pkg: String): Boolean {
            return try {
                val intent = packageManager.getLaunchIntentForPackage(pkg) ?: return false
                runOnUiThread { try { startActivity(intent) } catch (_: Exception) { Toast.makeText(this@MainActivity, "Could not launch app", Toast.LENGTH_SHORT).show() } }
                true
            } catch (_: Exception) { false }
        }
        @JavascriptInterface fun openSetting(name: String): Boolean {
            return try {
                val action = when (name) {
                    "usage" -> Settings.ACTION_USAGE_ACCESS_SETTINGS
                    "wifi" -> Settings.ACTION_WIFI_SETTINGS
                    "wireless" -> Settings.ACTION_WIRELESS_SETTINGS
                    "battery" -> Settings.ACTION_BATTERY_SAVER_SETTINGS
                    "display" -> Settings.ACTION_DISPLAY_SETTINGS
                    "dnd" -> Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS
                    "app" -> Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                    else -> return false
                }
                val intent = if (name == "app") Intent(action, Uri.parse("package:$packageName")) else Intent(action)
                startActivity(intent)
                true
            } catch (_: Exception) { false }
        }
        @JavascriptInterface fun getUsage(): String {
            val allowed = try {
                val ops = getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
                ops.checkOpNoThrow("android:get_usage_stats", android.os.Process.myUid(), packageName) == AppOpsManager.MODE_ALLOWED
            } catch (_: Exception) { false }
            if (!allowed) return JSONObject().put("permission", false).put("totalMs", 0).toString()
            return try {
                val cal = Calendar.getInstance().apply { set(Calendar.HOUR_OF_DAY,0); set(Calendar.MINUTE,0); set(Calendar.SECOND,0); set(Calendar.MILLISECOND,0) }
                val manager = getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
                val stats = manager.queryUsageStats(UsageStatsManager.INTERVAL_DAILY, cal.timeInMillis, System.currentTimeMillis()) ?: emptyList()
                JSONObject().put("permission", true).put("totalMs", stats.sumOf { it.totalTimeInForeground }).toString()
            } catch (_: Exception) { JSONObject().put("permission", true).put("totalMs", 0).toString() }
        }
        @JavascriptInterface fun toast(message: String) { runOnUiThread { Toast.makeText(this@MainActivity, message.take(100), Toast.LENGTH_SHORT).show() } }
    }
}
