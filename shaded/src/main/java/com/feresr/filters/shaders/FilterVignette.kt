package com.feresr.filters.shaders

import android.content.Context
import android.opengl.GLES20
import android.opengl.GLES30
import com.feresr.filters.Filter
import com.feresr.filters.R
import java.nio.FloatBuffer

class FilterVignette(context: Context, val config: () -> VignetteConfig) :
    Filter(context, R.raw.vignette) {

    private var vignetteCenter: Int = 0
    private var vignetteColor: Int = 0
    private var vignetteStart: Int = 0
    private var vignetteEnd: Int = 0

    override fun bindAttributes() {
        super.bindAttributes()
        vignetteCenter = GLES30.glGetUniformLocation(program, "vignetteCenter")
        vignetteColor = GLES30.glGetUniformLocation(program, "vignetteColor")
        vignetteStart = GLES30.glGetUniformLocation(program, "vignetteStart")
        vignetteEnd = GLES30.glGetUniformLocation(program, "vignetteEnd")
    }

    override fun setValues() {
        super.setValues()
        val config = config()
        GLES30.glUniform1f(vignetteStart, config.start)
        GLES30.glUniform1f(vignetteEnd, config.end)
        GLES20.glUniform2fv(
            vignetteCenter,
            1,
            FloatBuffer.wrap(floatArrayOf(config.center.first, config.center.second))
        )
        GLES20.glUniform3fv(
            vignetteColor,
            1,
            FloatBuffer.wrap(
                floatArrayOf(
                    config.vignetteColorR,
                    config.vignetteColorG,
                    config.vignetteColorB
                )
            )
        )
    }

    data class VignetteConfig(
        val start: Float,
        val end: Float,
        val center: Pair<Float, Float>,
        val vignetteColorR: Float,
        val vignetteColorG: Float,
        val vignetteColorB: Float
    )
}
