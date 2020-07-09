package com.feresr.shaded.shaders

import android.content.Context
import com.feresr.shaded.Filter
import com.feresr.shaded.R

class FilterVignette(context: Context, @Volatile var config: VignetteConfig) :
    Filter(context, R.raw.vignette) {

    private var vignetteCenter: Int = 0
    private var vignetteColor: Int = 0
    private var vignetteStart: Int = 0
    private var vignetteEnd: Int = 0

    override fun bindUniforms() {
        super.bindUniforms()
        vignetteCenter = shader.getUniformLocation("vignetteCenter")
        vignetteColor = shader.getUniformLocation("vignetteColor")
        vignetteStart = shader.getUniformLocation("vignetteStart")
        vignetteEnd = shader.getUniformLocation("vignetteEnd")
    }

    override fun updateUniforms() {
        super.updateUniforms()
        shader.setFloat(vignetteStart, config.start)
        shader.setFloat(vignetteEnd, config.end)
        shader.setFloat2(vignetteCenter, config.center.first, config.center.second)
        shader.setFloat3(
            vignetteCenter, config.vignetteColorR,
            config.vignetteColorG,
            config.vignetteColorB
        )
    }

    data class VignetteConfig(
        val start: Float = 0f,
        val end: Float = 0f,
        val center: Pair<Float, Float> = 0f to 0f,
        val vignetteColorR: Float = 0f,
        val vignetteColorG: Float = 0f,
        val vignetteColorB: Float = 0f
    )
}
