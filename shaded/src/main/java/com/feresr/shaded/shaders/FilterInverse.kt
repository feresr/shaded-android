package com.feresr.shaded.shaders

import android.content.Context
import com.feresr.shaded.Filter
import com.feresr.shaded.R

class FilterInverse(context: Context) : Filter(context, R.raw.inverse) {

    private var location: Int = 0
    private var alpha: Float = 0.0f

    fun updateUniforms(vararg value: Float) {
        alpha = value[0]
    }

    override fun bindUniforms() {
        super.bindUniforms()
        location = shader.getUniformLocation("alpha")
    }

    override fun uploadUniforms() {
        super.uploadUniforms()
        shader.setFloat(location, alpha)
    }
}
