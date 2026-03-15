package com.humans.aura

import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.os.Build
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertTrue
import org.junit.Test

class ManifestPermissionsTest {

    @Test
    fun manifest_declares_internet_permission_for_ai_requests() {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        val packageInfo = context.packageManager.packageInfoForPermissions(context.packageName)

        assertTrue(
            packageInfo.requestedPermissions.orEmpty().contains(android.Manifest.permission.INTERNET),
        )
    }

    @Test
    fun manifest_declares_microphone_permission_for_voice_capture() {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        val packageInfo = context.packageManager.packageInfoForPermissions(context.packageName)

        assertTrue(
            packageInfo.requestedPermissions.orEmpty().contains(android.Manifest.permission.RECORD_AUDIO),
        )
    }
}

private fun PackageManager.packageInfoForPermissions(packageName: String): PackageInfo =
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        getPackageInfo(packageName, PackageManager.PackageInfoFlags.of(PackageManager.GET_PERMISSIONS.toLong()))
    } else {
        @Suppress("DEPRECATION")
        getPackageInfo(packageName, PackageManager.GET_PERMISSIONS)
    }
