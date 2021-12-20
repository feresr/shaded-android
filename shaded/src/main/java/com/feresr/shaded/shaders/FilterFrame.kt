package com.feresr.shaded.shaders

import android.content.Context
import com.feresr.shaded.Filter
import com.feresr.shaded.R

class FilterFrame(context: Context) : Filter(context, vshader = R.raw.frame) {

    private var adjust: Float = 0.0f
    private var location = 0

    override fun updateUniforms(value: FloatArray) {
        adjust = value[0]
    }

    override fun bindUniforms() {
        super.bindUniforms()
        location = shader.getUniformLocation("frameAdjust")
    }

    override fun uploadUniforms() {
        super.uploadUniforms()
        shader.setFloat(location, 1.0f - adjust)
    }
}
