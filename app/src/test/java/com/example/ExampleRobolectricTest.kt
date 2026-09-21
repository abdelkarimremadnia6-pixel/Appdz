package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.domain.license.LicenseManager
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("Hanouti 40", appName)
  }

  @Test
  fun `license key generation and validation`() {
    val key = "H40-PRO-7K4M-X9QP-2F8D"
    val context = ApplicationProvider.getApplicationContext<Context>()
    val result = LicenseManager.validateKey(key, context)
    assertTrue(result.isValid)
    assertEquals(com.example.domain.license.LicenseStatus.ACTIVE, result.status)
  }
}

