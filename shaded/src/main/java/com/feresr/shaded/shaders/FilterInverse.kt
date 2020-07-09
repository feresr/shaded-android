package com.feresr.shaded.shaders

import android.content.Context
import com.feresr.shaded.Filter
import com.feresr.shaded.R

class FilterInverse(
    context: Context,
    @Volatile var alpha: Float
) : Filter(context, R.raw.inverse) {

    private var location: Int = 0;

    override fun bindUniforms() {
        super.bindUniforms()
        location = shader.getUniformLocation("alpha")
    }

    override fun updateUniforms() {
        super.updateUniforms()
        shader.setFloat(location, alpha)
    }
}
