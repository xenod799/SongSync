package com.songsync.media

import android.content.Context
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.mock

@OptIn(ExperimentalCoroutinesApi::class)
class EqualizerManagerTest {

    private lateinit var context: Context
    private lateinit var equalizerManager: EqualizerManager
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        context = mock(Context::class.java)
        equalizerManager = EqualizerManager(context)
    }

    @Test
    fun defaultState_isInitializedWithExpectedFallbacks() = runTest(testDispatcher) {
        val bands = equalizerManager.bands.value
        assertEquals(5, bands.size)
        assertEquals(60, bands[0].centerFreqHz)
        assertEquals(230, bands[1].centerFreqHz)
        assertEquals(910, bands[2].centerFreqHz)
        assertEquals(3600, bands[3].centerFreqHz)
        assertEquals(14000, bands[4].centerFreqHz)
    }

    @Test
    fun setEnabled_updatesEnabledStateFlow() = runTest(testDispatcher) {
        equalizerManager.setEnabled(true)
        assertTrue(equalizerManager.isEnabled.value)

        equalizerManager.setEnabled(false)
        assertFalse(equalizerManager.isEnabled.value)
    }

    @Test
    fun setBassBoost_clampsAndUpdatesStateFlow() = runTest(testDispatcher) {
        equalizerManager.setBassBoost(500)
        assertEquals(500, equalizerManager.bassBoostStrength.value)

        equalizerManager.setBassBoost(1500)
        assertEquals(1000, equalizerManager.bassBoostStrength.value)

        equalizerManager.setBassBoost(-200)
        assertEquals(0, equalizerManager.bassBoostStrength.value)
    }

    @Test
    fun setVirtualizer_clampsAndUpdatesStateFlow() = runTest(testDispatcher) {
        equalizerManager.setVirtualizer(800)
        assertEquals(800, equalizerManager.virtualizerStrength.value)

        equalizerManager.setVirtualizer(2000)
        assertEquals(1000, equalizerManager.virtualizerStrength.value)
    }

    @Test
    fun applyPreset_updatesAllBandLevels() = runTest(testDispatcher) {
        val rockPreset = EqualizerPreset(
            name = "Rock",
            bandLevels = listOf(400, 200, -100, 200, 400)
        )
        equalizerManager.applyPreset(rockPreset)

        val bands = equalizerManager.bands.value
        assertEquals(400, bands[0].level)
        assertEquals(200, bands[1].level)
        assertEquals(-100, bands[2].level)
        assertEquals(200, bands[3].level)
        assertEquals(400, bands[4].level)
        assertEquals("Rock", equalizerManager.selectedPresetName.value)
    }
}
