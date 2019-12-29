package com.feresr.filters.shaders

import android.content.Context
import android.opengl.GLES30
import com.feresr.filters.Filter
import com.feresr.filters.R

class FilterContrast(context: Context, val contrast: () -> Float) :
    Filter(context, R.raw.contrast) {

    private var value = 0
    override fun bindAttributes() {
        super.bindAttributes()
        value = GLES30.glGetUniformLocation(program, "contrast")
    }

    override fun setValues() {
        super.setValues()
        GLES30.glUniform1f(value, contrast())
    }
}
