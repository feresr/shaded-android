package com.feresr.shaded.shaders

import android.content.Context
import com.feresr.shaded.Filter
import com.feresr.shaded.R

class FilterExposure(context: Context) : Filter(context, R.raw.exposure) {

    private var location = 0
    private var exposure: Float = 0.5f

    override fun updateUniforms(vararg value: Float) {
        exposure = value[0]
    }

    override fun bindUniforms() {
        super.bindUniforms()
        location = shader.getUniformLocation("exposure")
    }

    override fun uploadUniforms() {
        super.uploadUniforms()
        shader.setFloat(location, exposure)
    }
}
