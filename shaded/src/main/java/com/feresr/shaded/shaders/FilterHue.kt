package com.feresr.shaded.shaders

import android.content.Context
import com.feresr.shaded.Filter
import com.feresr.shaded.R

class FilterHue(context: Context) : Filter(context, R.raw.hue) {

    private var hue = 0f
    private var location: Int = 0

    fun updateUniforms(vararg value: Float) {
        hue = value[0]
    }

    override fun bindUniforms() {
        super.bindUniforms()
        location = shader.getUniformLocation("hueAdjust")

    }

    override fun uploadUniforms() {
        super.uploadUniforms()
        shader.setFloat(location, hue)
    }
}
