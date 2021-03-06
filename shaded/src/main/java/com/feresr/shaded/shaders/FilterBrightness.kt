package com.feresr.shaded.shaders

import android.content.Context
import com.feresr.shaded.Filter
import com.feresr.shaded.R

class FilterBrightness(
    context: Context,
    @Volatile var brightness: Float = 0.5f
) : Filter(context, R.raw.brightness) {

    private var value = 0
    override fun bindUniforms() {
        super.bindUniforms()
        value = shader.getUniformLocation("brightness")
    }

    override fun updateUniforms() {
        super.updateUniforms()
        shader.setFloat(value, brightness)
    }
}
