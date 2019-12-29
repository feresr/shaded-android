package com.feresr.shaded.shaders

import android.content.Context
import android.opengl.GLES30
import com.feresr.shaded.Filter
import com.feresr.shaded.R

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
