package com.omnitune.app.platform

import uk.co.caprica.vlcj.factory.MediaPlayerFactory
import uk.co.caprica.vlcj.player.base.Equalizer
import uk.co.caprica.vlcj.player.base.MediaPlayer

class EqualizerManager(
    private val factory: MediaPlayerFactory,
    private val player: MediaPlayer
) {
    fun listPresets(): List<String> = factory.equalizer().presets()

    fun listBands(): List<Float> = factory.equalizer().bands()

    fun applyPreset(presetName: String) {
        val eq = factory.equalizer().newEqualizer(presetName)
        player.audio().setEqualizer(eq)
    }

    fun applyCustomEqualizer(preampDb: Float, bandGains: List<Float>) {
        val eq = factory.equalizer().newEqualizer()
        eq.setPreamp(preampDb.coerceIn(-20f, 20f))
        val bandCount = bandGains.size.coerceAtMost(10)
        for (i in 0 until bandCount) {
            eq.setAmp(i, bandGains[i].coerceIn(-20f, 20f))
        }
        player.audio().setEqualizer(eq)
    }

    fun disable() {
        player.audio().setEqualizer(null)
    }

    fun isEnabled(): Boolean = player.audio().equalizer() != null
}
