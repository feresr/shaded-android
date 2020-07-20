package com.feresr.shaded.shaders

import android.content.Context
import com.feresr.shaded.Filter
import com.feresr.shaded.R

class FilterTemperature(context: Context, @Volatile var temperature: Float = 0.5f) :
    Filter(context, R.raw.temperature) {

    private var value = 0

    override fun bindUniforms() {
        super.bindUniforms()
        value = shader.getUniformLocation("temperature")
    }

    override fun updateUniforms() {
        super.updateUniforms()
        shader.setFloat(value, temperature)
    }
}
