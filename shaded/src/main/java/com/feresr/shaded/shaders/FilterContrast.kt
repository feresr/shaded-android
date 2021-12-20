package com.feresr.shaded.shaders

import android.content.Context
import com.feresr.shaded.Filter
import com.feresr.shaded.R

class FilterContrast(context: Context) : Filter(context, R.raw.contrast) {

    private var contrast: Float = 0.5f
    private var location = 0

    override fun updateUniforms(vararg value: Float) {
        contrast = value[0]
    }

    override fun bindUniforms() {
        super.bindUniforms()
        location = shader.getUniformLocation("contrast")
    }

    override fun uploadUniforms() {
        super.uploadUniforms()
        shader.setFloat(location, contrast)
    }
}
