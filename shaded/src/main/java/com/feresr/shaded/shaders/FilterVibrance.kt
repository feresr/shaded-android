package com.feresr.shaded.shaders

import android.content.Context
import com.feresr.shaded.Filter
import com.feresr.shaded.R

class FilterVibrance(context: Context, @Volatile var vibrance: Float = 0.5f) :
    Filter(context, R.raw.vibrance) {

    private var value = 0

    override fun bindUniforms() {
        super.bindUniforms()
        value = shader.getUniformLocation("vibrance")
    }

    override fun updateUniforms() {
        super.updateUniforms()
        shader.setFloat(value, vibrance)
    }
}
