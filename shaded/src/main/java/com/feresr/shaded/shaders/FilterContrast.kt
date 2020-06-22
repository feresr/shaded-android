package com.feresr.shaded.shaders

import android.content.Context
import android.opengl.GLES30
import com.feresr.shaded.Filter
import com.feresr.shaded.R

class FilterContrast(context: Context, @Volatile var contrast: Float) :
    Filter(context, R.raw.contrast) {

    private var value = 0

    override fun bindUniforms() {
        super.bindUniforms()
        value = GLES30.glGetUniformLocation(program, "contrast")
    }

    override fun updateUniforms() {
        super.updateUniforms()
        GLES30.glUniform1f(value, contrast)
    }
}
