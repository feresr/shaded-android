package com.feresr.shaded.shaders

import android.content.Context
import android.opengl.GLES30
import com.feresr.shaded.Filter
import com.feresr.shaded.R

class FilterBrightness(
    context: Context,
    @Volatile var brightness: Float = 0f
) : Filter(context, R.raw.brightness) {

    private var value = 0
    override fun bindUniforms() {
        super.bindUniforms()
        value = GLES30.glGetUniformLocation(program, "brightness")
    }

    override fun setValues() {
        super.setValues()
        GLES30.glUniform1f(value, brightness)
    }
}
