package com.feresr.shaded.shaders

import android.content.Context
import com.feresr.shaded.Filter
import com.feresr.shaded.R

class FilterVignette(context: Context) : Filter(context, R.raw.vignette) {

    private var config: VignetteConfig = VignetteConfig()

    private var vignetteCenter: Int = 0
    private var vignetteColor: Int = 0
    private var vignetteStart: Int = 0
    private var vignetteEnd: Int = 0

    fun updateUniforms(vararg value: Float) {
        config.start = value.elementAtOrNull(1) ?: 0f
        config.end = value[0]
        val centerX = value.elementAtOrNull(2) ?: 0.5f
        val centerY = value.elementAtOrNull(3) ?: 0.5f
        config.center = centerX to centerY
        config.vignetteColorR = value.elementAtOrNull(4) ?: 0f
        config.vignetteColorG = value.elementAtOrNull(5) ?: 0f
        config.vignetteColorB = value.elementAtOrNull(6) ?: 0f
    }

    override fun bindUniforms() {
        super.bindUniforms()
        vignetteCenter = shader.getUniformLocation("vignetteCenter")
        vignetteColor = shader.getUniformLocation("vignetteColor")
        vignetteStart = shader.getUniformLocation("vignetteStart")
        vignetteEnd = shader.getUniformLocation("vignetteEnd")
    }

    override fun uploadUniforms() {
        super.uploadUniforms()
        shader.setFloat(vignetteStart, config.start)
        shader.setFloat(vignetteEnd, config.end)
        shader.setFloat2(vignetteCenter, config.center.first, config.center.second)
        shader.setFloat3(
            vignetteColor, config.vignetteColorR,
            config.vignetteColorG,
            config.vignetteColorB
        )
    }

    internal data class VignetteConfig(
        var start: Float = 0f,
        var end: Float = 0f,
        var center: Pair<Float, Float> = 0f to 0f,
        var vignetteColorR: Float = 0f,
        var vignetteColorG: Float = 0f,
        var vignetteColorB: Float = 0f
    )
}
