package com.feresr.shaded.shaders

import android.content.Context
import com.feresr.shaded.Filter
import com.feresr.shaded.R

class FilterHue(
    context: Context,
    var value: Float
) : Filter(context, R.raw.hue) {

    private var hue = 0

    override fun bindUniforms() {
        super.bindUniforms()
        hue = shader.getUniformLocation("hueAdjust")

    }

    override fun updateUniforms() {
        super.updateUniforms()
        shader.setFloat(hue, value)
    }
}
