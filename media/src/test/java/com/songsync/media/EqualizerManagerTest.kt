package com.songsync.media

import com.songsync.media.player.EqualizerBand
import com.songsync.media.player.EqualizerManager
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class EqualizerManagerTest {

    private lateinit var equalizerManager: EqualizerManager
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        equalizerManager = EqualizerManager()
    }

    @Test
    fun defaultState_isInitializedWithExpectedDefaults() = runTest(testDispatcher) {
        val state = equalizerManager.state.value
        assertFalse(state.isEnabled)
        assertEquals(0.toShort(), state.bassBoostStrength)
        assertEquals(0.toShort(), state.virtualizerStrength)
        assertEquals(0, state.loudnessGainMilliBels)
    }

    @Test
    fun setEnabled_updatesEnabledStateFlow() = runTest(testDispatcher) {
        equalizerManager.setEnabled(true)
        assertTrue(equalizerManager.state.value.isEnabled)

        equalizerManager.setEnabled(false)
        assertFalse(equalizerManager.state.value.isEnabled)
    }

    @Test
    fun release_resetsOrCleansUpSafely() = runTest(testDispatcher) {
        equalizerManager.release()
        assertFalse(equalizerManager.state.value.isEnabled)
    }
}
