package com.supersci.calculator.gamehub

import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable

data class GameAppInfo(
    val packageName: String,
    val appName: String,
    val icon: Drawable?,
    val launchIntent: Intent?,
    val isFavorite: Boolean = false,
    val lastPlayed: Long = 0
)

class GameLauncherManager(private val context: Context) {

    private val pm: PackageManager = context.packageManager

    fun scanInstalledGames(): List<GameAppInfo> {
        val mainIntent = Intent(Intent.ACTION_MAIN, null).apply {
            addCategory(Intent.CATEGORY_LAUNCHER)
        }
        val resolvedApps = pm.queryIntentActivities(mainIntent, 0)
        val gameList = mutableListOf<GameAppInfo>()

        for (resolveInfo in resolvedApps) {
            val appInfo = resolveInfo.activityInfo.applicationInfo
            val packageName = appInfo.packageName

            // Exclude self
            if (packageName == context.packageName) continue

            val isCategoryGame = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                appInfo.category == ApplicationInfo.CATEGORY_GAME
            } else {
                (appInfo.flags and ApplicationInfo.FLAG_IS_GAME) != 0
            }

            val appName = resolveInfo.loadLabel(pm).toString()
            val lowerName = appName.lowercase()
            val lowerPkg = packageName.lowercase()

            val heuristicsMatch = lowerName.contains("free fire") ||
                    lowerName.contains("call of duty") ||
                    lowerName.contains("cod") ||
                    lowerName.contains("pubg") ||
                    lowerName.contains("blood strike") ||
                    lowerName.contains("game") ||
                    lowerPkg.contains("game") ||
                    isCategoryGame

            if (heuristicsMatch) {
                val icon = resolveInfo.loadIcon(pm)
                val launchIntent = pm.getLaunchIntentForPackage(packageName)
                gameList.add(
                    GameAppInfo(
                        packageName = packageName,
                        appName = appName,
                        icon = icon,
                        launchIntent = launchIntent
                    )
                )
            }
        }
        return gameList.sortedBy { it.appName }
    }

    fun getAllLaunchableApps(): List<GameAppInfo> {
        val mainIntent = Intent(Intent.ACTION_MAIN, null).apply {
            addCategory(Intent.CATEGORY_LAUNCHER)
        }
        val resolvedApps = pm.queryIntentActivities(mainIntent, 0)
        return resolvedApps
            .filter { it.activityInfo.applicationInfo.packageName != context.packageName }
            .map { resolveInfo ->
                val appName = resolveInfo.loadLabel(pm).toString()
                val pkg = resolveInfo.activityInfo.applicationInfo.packageName
                GameAppInfo(
                    packageName = pkg,
                    appName = appName,
                    icon = resolveInfo.loadIcon(pm),
                    launchIntent = pm.getLaunchIntentForPackage(pkg)
                )
            }
            .sortedBy { it.appName }
    }

    fun launchGame(packageName: String): Boolean {
        return try {
            val intent = pm.getLaunchIntentForPackage(packageName)
            if (intent != null) {
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(intent)
                true
            } else {
                false
            }
        } catch (e: Exception) {
            false
        }
    }
}
