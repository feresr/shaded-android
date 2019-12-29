package com.feresr.filters.shaders

import android.content.Context
import android.opengl.GLES30
import com.feresr.filters.Filter
import com.feresr.filters.R

class FilterBrightness(context: Context, val brightness: () -> Float) :
    Filter(context, R.raw.brightness) {

    private var value = 0
    override fun bindAttributes() {
        super.bindAttributes()
        value = GLES30.glGetUniformLocation(program, "brightness")
    }

    override fun setValues() {
        super.setValues()
        GLES30.glUniform1f(value, brightness())
    }
}
