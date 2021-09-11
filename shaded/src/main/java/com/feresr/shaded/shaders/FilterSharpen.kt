package com.feresr.shaded.shaders

import android.content.Context
import com.feresr.shaded.Filter
import com.feresr.shaded.R

class FilterSharpen(
    context: Context,
    var value: Float = 1.0f,
) : Filter(context, R.raw.sharpen) {

    private var hLocation = 0

    override fun bindUniforms() {
        super.bindUniforms()
        hLocation = shader.getUniformLocation("value")
    }

    override fun updateUniforms() {
        super.updateUniforms()
        shader.setFloat(hLocation, value)
    }
}
