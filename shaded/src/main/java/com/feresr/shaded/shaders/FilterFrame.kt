package com.feresr.shaded.shaders

import android.content.Context
import com.feresr.shaded.Filter
import com.feresr.shaded.R

class FilterFrame(
    context: Context,
    var adjust: Float = 0.0f
) : Filter(context, vshader = R.raw.frame) {

    private var value = 0
    override fun bindUniforms() {
        super.bindUniforms()
        value = shader.getUniformLocation("frameAdjust")
    }

    override fun updateUniforms() {
        super.updateUniforms()
        shader.setFloat(value, 1.0f - adjust)
    }
}
