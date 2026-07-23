package com.salahabusaif.financemanager.core.data.preferences

import com.salahabusaif.financemanager.core.data.proto.StoredAppPreferences
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class AppPreferencesSerializerTest {
    @Test fun defaultsAreArabicAndSystem() = runTest {
        assertEquals(StoredAppPreferences.Language.ARABIC, AppPreferencesSerializer.defaultValue.language)
        assertEquals(StoredAppPreferences.Theme.SYSTEM, AppPreferencesSerializer.defaultValue.theme)
    }

    @Test fun roundTripsStoredPreferences() = runTest {
        val bytes = ByteArrayOutputStream()
        AppPreferencesSerializer.writeTo(StoredAppPreferences.newBuilder().setHideAmounts(true).build(), bytes)
        assertEquals(true, AppPreferencesSerializer.readFrom(ByteArrayInputStream(bytes.toByteArray())).hideAmounts)
    }
}
