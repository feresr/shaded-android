package com.feresr.shaded.shaders

import android.content.Context
import com.feresr.shaded.Filter
import com.feresr.shaded.R

class FilterTemperature(context: Context) : Filter(context, R.raw.temperature) {

    private var temperature: Float = 0.5f
    private var tint: Float = 0.5f

    private var tintLocation = 0
    private var tempLocation = 0

    override fun updateUniforms(value: FloatArray) {
        temperature = value[0]
        tint = value[1]
    }

    override fun bindUniforms() {
        super.bindUniforms()
        tempLocation = shader.getUniformLocation("temperature")
        tintLocation = shader.getUniformLocation("tint")
    }

    override fun uploadUniforms() {
        super.uploadUniforms()
        shader.setFloat(tempLocation, temperature)
        shader.setFloat(tintLocation, tint)
    }
}
