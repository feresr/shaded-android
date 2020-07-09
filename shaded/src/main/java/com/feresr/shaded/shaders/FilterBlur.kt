package com.feresr.shaded.shaders

import android.content.Context
import com.feresr.shaded.Filter
import com.feresr.shaded.R

class FilterBlur(
    context: Context,
    @Volatile var x: Float = 0f,
    @Volatile var y: Float = 0f
) : Filter(context, R.raw.blur) {

    private var location = 0

    override fun bindUniforms() {
        super.bindUniforms()
        location = shader.getUniformLocation("blurRadius")
    }

    override fun updateUniforms() {
        super.updateUniforms()
        shader.setFloat2(location, x, y)
    }
}
