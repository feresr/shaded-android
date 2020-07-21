package com.feresr.shaded.shaders

import android.content.Context
import com.feresr.shaded.Filter
import com.feresr.shaded.R

class FilterGrain(
    context: Context,
    @Volatile var grain: Float = 0f
) : Filter(context, R.raw.grain) {

    var location = 0
    override fun bindUniforms() {
        super.bindUniforms()
        location = shader.getUniformLocation("grain")

    }

    override fun updateUniforms() {
        super.updateUniforms()
        shader.setFloat(location, grain)
    }
}
