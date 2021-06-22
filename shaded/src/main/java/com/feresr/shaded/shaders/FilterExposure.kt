package com.feresr.shaded.shaders

import android.content.Context
import com.feresr.shaded.Filter
import com.feresr.shaded.R

class FilterExposure(
    context: Context,
    var exposure: Float = 0.5f
) : Filter(context, R.raw.exposure) {

    private var value = 0
    override fun bindUniforms() {
        super.bindUniforms()
        value = shader.getUniformLocation("exposure")
    }

    override fun updateUniforms() {
        super.updateUniforms()
        shader.setFloat(value, exposure)
    }
}
