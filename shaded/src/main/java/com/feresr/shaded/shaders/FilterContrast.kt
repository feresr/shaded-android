package com.feresr.shaded.shaders

import android.content.Context
import android.opengl.GLES30
import com.feresr.shaded.Filter
import com.feresr.shaded.R

class FilterContrast(context: Context, @Volatile var contrast: Float = 0.5f) :
    Filter(context, R.raw.contrast) {

    private var value = 0

    override fun bindUniforms() {
        super.bindUniforms()
        value = shader.getUniformLocation("contrast")
    }

    override fun updateUniforms() {
        super.updateUniforms()
        shader.setFloat(value, contrast)
    }
}
