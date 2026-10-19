package com.cesoft.cesrunner

import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import java.util.Locale

class UtilTest {

    private lateinit var defaultLocale: Locale

    @Before
    fun setUp() {
        defaultLocale = Locale.getDefault()
        Locale.setDefault(Locale.US)
    }

    @After
    fun tearDown() {
        Locale.setDefault(defaultLocale)
    }

    @Test
    fun `toTimeStr formats hours minutes and seconds`() {
        assertEquals("0h 0m 0s", 0L.toTimeStr())
        assertEquals("0h 0m 59s", 59_999L.toTimeStr())
        assertEquals("1h 2m 3s", ((3600 + 120 + 3) * 1000L).toTimeStr())
    }

    @Test
    fun `toTimeStr adds days when needed`() {
        assertEquals("1d 0h 0m 1s", ((24 * 3600 + 1) * 1000L).toTimeStr())
    }

    @Test
    fun `toDistanceStr uses meters below one kilometer`() {
        assertEquals("0 m", 0.toDistanceStr())
        assertEquals("999 m", 999.toDistanceStr())
    }

    @Test
    fun `toDistanceStr uses kilometers without decimals when exact`() {
        assertEquals("1 Km", 1000.toDistanceStr())
        assertEquals("5 Km", 5099.toDistanceStr())
    }

    @Test
    fun `toDistanceStr uses one decimal truncated to hundreds of meters`() {
        assertEquals("1.5 Km", 1500.toDistanceStr())
        assertEquals("5.6 Km", 5627.toDistanceStr())
    }

    @Test
    fun `toDateStr formats with the system time zone`() {
        val time = java.time.LocalDateTime.of(2025, 1, 22, 16, 42, 6)
            .atZone(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli()
        assertEquals("22/01/2025 16:42:06", time.toDateStr())
    }
}
