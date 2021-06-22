package com.feresr.shaded.shaders

import android.content.Context
import com.feresr.shaded.Filter
import com.feresr.shaded.R

class FilterTemperature(
    context: Context,
    var temperature: Float = 0.5f,
    var tint: Float = 0.5f
) :
    Filter(context, R.raw.temperature) {

    private var tintLocation = 0
    private var tempLocation = 0

    override fun bindUniforms() {
        super.bindUniforms()
        tempLocation = shader.getUniformLocation("temperature")
        tintLocation = shader.getUniformLocation("tint")
    }

    override fun updateUniforms() {
        super.updateUniforms()
        shader.setFloat(tempLocation, temperature)
        shader.setFloat(tintLocation, tint)
    }
}
