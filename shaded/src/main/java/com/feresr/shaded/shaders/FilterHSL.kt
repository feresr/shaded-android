package com.feresr.shaded.shaders

import android.content.Context
import com.feresr.shaded.Filter
import com.feresr.shaded.R

class FilterHSL(context: Context) : Filter(context, R.raw.hsl) {

    private var location = 0
    private val values = floatArrayOf(
        0f, 0f, 0f,   // RED
        0f, 0f, 0f,         // ORANGE
        0f, 0f, 0f,         // YELLOW
        0f, 0f, 0f,         // GREEN
        0f, 0f, 0f,         // CYAN
        0f, 0f, 0f,         // BLUE
        0f, 0f, 0f,         // PURPLE
        0f, 0f, 0f          // FUCHSIA
    )

    override fun updateUniforms(value: FloatArray) {
        value.copyInto(values)
    }

    override fun bindUniforms() {
        super.bindUniforms()
        location = shader.getUniformLocation("values")
    }


    override fun uploadUniforms() {
        super.uploadUniforms()
        shader.setVec3Array(location, 8, values)
    }
}
