package com.feresr.shaded.shaders

import android.content.Context
import com.feresr.shaded.Filter
import com.feresr.shaded.R

class FilterBrightness(
    context: Context,
) : Filter(context, R.raw.brightness) {

    var brightness: Float = 0.5f
    private var location = 0

    override fun updateUniforms(vararg value: Float) {
        brightness = value[0]
    }

    override fun bindUniforms() {
        super.bindUniforms()
        location = shader.getUniformLocation("brightness")
    }

    override fun uploadUniforms() {
        super.uploadUniforms()
        shader.setFloat(location, brightness)
    }
}
